package com.adyanta.jira.service;

import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.model.JiraStory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service class for reading Jira stories using the REST API.
 * Provides methods to fetch story details including acceptance criteria and descriptions.
 */
public class JiraStoryReaderService {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraStoryReaderService.class);
    
    private final JiraConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public JiraStoryReaderService(JiraConfig config) {
        this.config = config;
        this.config.validate();
        this.httpClient = createHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    
    private HttpClient createHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionTimeToLive(30, TimeUnit.SECONDS)
                .setMaxConnTotal(20)
                .setMaxConnPerRoute(10);
        
        // Set up authentication
        if (config.isUseApiToken()) {
            // For API token authentication, we'll use Basic Auth with username:token
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUsername(), config.getApiToken())
            );
            builder.setDefaultCredentialsProvider(credentialsProvider);
        } else {
            // For password authentication
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUsername(), config.getPassword())
            );
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
        
        return builder.build();
    }
    
    /**
     * Fetch a single Jira story by its key (e.g., "PROJ-123")
     */
    public JiraStory getStoryByKey(String storyKey) throws JiraApiException {
        logger.info("Fetching story: {}", storyKey);
        
        String url = String.format("%s/rest/api/3/issue/%s", config.getJiraUrl(), storyKey);
        
        try {
            String response = makeHttpRequest(url);
            JiraStory story = objectMapper.readValue(response, JiraStory.class);
            
            logger.info("Successfully fetched story: {} - {}", story.getKey(), 
                    story.getFields() != null ? story.getFields().getSummary() : "No summary");
            
            return story;
            
        } catch (IOException e) {
            logger.error("Failed to fetch story {}: {}", storyKey, e.getMessage());
            throw new JiraApiException("Failed to fetch story: " + storyKey, e);
        }
    }
    
    /**
     * Fetch multiple Jira stories by their keys
     */
    public List<JiraStory> getStoriesByKeys(List<String> storyKeys) throws JiraApiException {
        logger.info("Fetching {} stories", storyKeys.size());
        
        // Build JQL query for multiple keys
        String jql = String.format("key in (%s)", String.join(", ", storyKeys));
        return searchStories(jql);
    }
    
    /**
     * Search for stories using JQL (Jira Query Language)
     */
    public List<JiraStory> searchStories(String jql) throws JiraApiException {
        logger.info("Searching stories with JQL: {}", jql);
        
        String url = String.format("%s/rest/api/3/search?jql=%s&maxResults=1000", 
                config.getJiraUrl(), 
                java.net.URLEncoder.encode(jql, StandardCharsets.UTF_8));
        
        try {
            String response = makeHttpRequest(url);
            SearchResult searchResult = objectMapper.readValue(response, SearchResult.class);
            
            logger.info("Found {} stories", searchResult.getIssues().size());
            return searchResult.getIssues();
            
        } catch (IOException e) {
            logger.error("Failed to search stories with JQL {}: {}", jql, e.getMessage());
            throw new JiraApiException("Failed to search stories", e);
        }
    }
    
    /**
     * Get all stories from a specific project
     */
    public List<JiraStory> getStoriesByProject(String projectKey) throws JiraApiException {
        String jql = String.format("project = %s AND issuetype = Story", projectKey);
        return searchStories(jql);
    }
    
    /**
     * Get stories assigned to a specific user
     */
    public List<JiraStory> getStoriesByAssignee(String assignee) throws JiraApiException {
        String jql = String.format("assignee = %s AND issuetype = Story", assignee);
        return searchStories(jql);
    }
    
    /**
     * Get stories in a specific sprint
     */
    public List<JiraStory> getStoriesBySprint(String sprintName) throws JiraApiException {
        String jql = String.format("Sprint = \"%s\" AND issuetype = Story", sprintName);
        return searchStories(jql);
    }
    
    /**
     * Get stories with specific status
     */
    public List<JiraStory> getStoriesByStatus(String status) throws JiraApiException {
        String jql = String.format("status = \"%s\" AND issuetype = Story", status);
        return searchStories(jql);
    }
    
    /**
     * Extract acceptance criteria from a story
     */
    public String getAcceptanceCriteria(JiraStory story) {
        if (story == null || story.getFields() == null) {
            return null;
        }
        
        JiraStory.Fields fields = story.getFields();
        String acceptanceCriteria = fields.getFirstAvailableAcceptanceCriteria();
        
        if (acceptanceCriteria == null || acceptanceCriteria.trim().isEmpty()) {
            logger.warn("No acceptance criteria found for story: {}", story.getKey());
            return null;
        }
        
        return acceptanceCriteria.trim();
    }
    
    /**
     * Extract description from a story
     */
    public String getDescription(JiraStory story) {
        if (story == null || story.getFields() == null) {
            return null;
        }
        
        String description = story.getFields().getDescription();
        
        if (description == null || description.trim().isEmpty()) {
            logger.warn("No description found for story: {}", story.getKey());
            return null;
        }
        
        return description.trim();
    }
    
    /**
     * Get story summary
     */
    public String getSummary(JiraStory story) {
        if (story == null || story.getFields() == null) {
            return null;
        }
        
        return story.getFields().getSummary();
    }
    
    /**
     * Print story details in a formatted way
     */
    public void printStoryDetails(JiraStory story) {
        if (story == null) {
            System.out.println("Story is null");
            return;
        }
        
        System.out.println("=".repeat(80));
        System.out.println("STORY: " + story.getKey());
        System.out.println("=".repeat(80));
        
        JiraStory.Fields fields = story.getFields();
        if (fields != null) {
            System.out.println("Summary: " + (fields.getSummary() != null ? fields.getSummary() : "N/A"));
            System.out.println("Status: " + (fields.getStatus() != null ? fields.getStatus().getName() : "N/A"));
            System.out.println("Priority: " + (fields.getPriority() != null ? fields.getPriority().getName() : "N/A"));
            System.out.println("Assignee: " + (fields.getAssignee() != null ? fields.getAssignee().getDisplayName() : "Unassigned"));
            System.out.println("Reporter: " + (fields.getReporter() != null ? fields.getReporter().getDisplayName() : "N/A"));
            System.out.println("Story Points: " + (fields.getStoryPoints() != null ? fields.getStoryPoints() : "N/A"));
            
            System.out.println("\nDescription:");
            System.out.println("-".repeat(40));
            String description = getDescription(story);
            System.out.println(description != null ? description : "No description available");
            
            System.out.println("\nAcceptance Criteria:");
            System.out.println("-".repeat(40));
            String acceptanceCriteria = getAcceptanceCriteria(story);
            System.out.println(acceptanceCriteria != null ? acceptanceCriteria : "No acceptance criteria available");
            
            if (fields.getLabels() != null && !fields.getLabels().isEmpty()) {
                System.out.println("\nLabels: " + String.join(", ", fields.getLabels()));
            }
            
            if (fields.getComponents() != null && !fields.getComponents().isEmpty()) {
                System.out.println("Components: " + fields.getComponents().stream()
                        .map(JiraStory.Component::getName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse(""));
            }
        }
        System.out.println("=".repeat(80));
    }
    
    private String makeHttpRequest(String url) throws JiraApiException {
        HttpGet request = new HttpGet(url);
        
        // Add headers
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        
        // Add Basic Auth header
        String auth = config.getUsername() + ":" + (config.isUseApiToken() ? config.getApiToken() : config.getPassword());
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        request.setHeader("Authorization", "Basic " + encodedAuth);
        
        try {
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } else if (statusCode == 401) {
                throw new JiraApiException("Authentication failed. Please check your credentials.");
            } else if (statusCode == 403) {
                throw new JiraApiException("Access forbidden. Please check your permissions.");
            } else if (statusCode == 404) {
                throw new JiraApiException("Resource not found. Please check the story key or URL.");
            } else {
                throw new JiraApiException("HTTP error: " + statusCode + " - " + response.getStatusLine().getReasonPhrase());
            }
            
        } catch (IOException e) {
            throw new JiraApiException("Failed to make HTTP request: " + e.getMessage(), e);
        } finally {
            request.releaseConnection();
        }
    }
    
    /**
     * Inner class for search results
     */
    public static class SearchResult {
        private List<JiraStory> issues;
        
        public List<JiraStory> getIssues() {
            return issues;
        }
        
        public void setIssues(List<JiraStory> issues) {
            this.issues = issues;
        }
    }
    
    /**
     * Custom exception for Jira API errors
     */
    public static class JiraApiException extends Exception {
        public JiraApiException(String message) {
            super(message);
        }
        
        public JiraApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
