package com.adyanta.jira.service;

import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.model.IssueCreationRequest;
import com.adyanta.jira.model.TestCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service class for creating Jira issues (including subtasks) using the REST API.
 * Provides generic methods to create issues with all field types.
 */
public class JiraIssueCreatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraIssueCreatorService.class);
    
    private final JiraConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public JiraIssueCreatorService(JiraConfig config) {
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
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUsername(), config.getApiToken())
            );
            builder.setDefaultCredentialsProvider(credentialsProvider);
        } else {
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
     * Create a single Jira issue from an IssueCreationRequest
     * 
     * @param request The issue creation request
     * @return The created issue key (e.g., "PROJ-123")
     * @throws JiraApiException If creation fails
     */
    public String createIssue(IssueCreationRequest request) throws JiraApiException {
        logger.info("Creating Jira issue with summary: {}", 
                request.getFields().has("summary") ? request.getFields().get("summary").asText() : "N/A");
        
        String url = config.getJiraUrl() + "/rest/api/3/issue";
        
        try {
            String jsonPayload = request.toJsonString();
            logger.debug("Request payload: {}", jsonPayload);
            
            String response = makeHttpPostRequest(url, jsonPayload);
            JsonNode responseNode = objectMapper.readTree(response);
            
            String issueKey = responseNode.get("key").asText();
            String issueId = responseNode.get("id").asText();
            
            logger.info("Successfully created issue: {} (ID: {})", issueKey, issueId);
            return issueKey;
            
        } catch (IOException e) {
            logger.error("Failed to create issue: {}", e.getMessage());
            throw new JiraApiException("Failed to create issue: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a subtask from a TestCase object
     * 
     * @param testCase The test case data
     * @param projectKey The project key (e.g., "PROJ")
     * @param parentKey The parent issue key (required for subtasks)
     * @return The created subtask key
     * @throws JiraApiException If creation fails
     */
    public String createSubtask(TestCase testCase, String projectKey, String parentKey) throws JiraApiException {
        IssueCreationRequest request = IssueCreationRequest.fromTestCase(testCase, projectKey, parentKey);
        return createIssue(request);
    }
    
    /**
     * Create multiple subtasks from a list of TestCase objects
     * 
     * @param testCases List of test cases
     * @param projectKey The project key
     * @param parentKey The parent issue key (required for subtasks)
     * @return List of created issue keys
     * @throws JiraApiException If any creation fails
     */
    public List<String> createSubtasks(List<TestCase> testCases, String projectKey, String parentKey) 
            throws JiraApiException {
        logger.info("Creating {} subtasks for project {} under parent {}", 
                testCases.size(), projectKey, parentKey);
        
        List<String> createdKeys = new ArrayList<>();
        List<String> failedKeys = new ArrayList<>();
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            try {
                String issueKey = createSubtask(testCase, projectKey, parentKey);
                createdKeys.add(issueKey);
                logger.info("Progress: {}/{} subtasks created", i + 1, testCases.size());
                
                // Add a small delay to avoid rate limiting
                Thread.sleep(100);
                
            } catch (JiraApiException e) {
                String summary = testCase.getSummary() != null ? testCase.getSummary() : "Unknown";
                logger.error("Failed to create subtask {}: {}", summary, e.getMessage());
                failedKeys.add(summary);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JiraApiException("Thread interrupted during subtask creation", e);
            }
        }
        
        logger.info("Created {}/{} subtasks successfully", createdKeys.size(), testCases.size());
        if (!failedKeys.isEmpty()) {
            logger.warn("Failed to create {} subtasks: {}", failedKeys.size(), failedKeys);
        }
        
        return createdKeys;
    }
    
    /**
     * Create multiple subtasks with error handling (continues on errors)
     * 
     * @param testCases List of test cases
     * @param projectKey The project key
     * @param parentKey The parent issue key
     * @return CreationResult containing successful and failed creations
     */
    public CreationResult createSubtasksWithErrorHandling(List<TestCase> testCases, String projectKey, String parentKey) {
        logger.info("Creating {} subtasks for project {} under parent {} (with error handling)", 
                testCases.size(), projectKey, parentKey);
        
        CreationResult result = new CreationResult();
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            String summary = testCase.getSummary() != null ? testCase.getSummary() : "Unknown";
            
            try {
                String issueKey = createSubtask(testCase, projectKey, parentKey);
                result.addSuccess(issueKey, summary);
                logger.info("Progress: {}/{} subtasks processed ({} successful, {} failed)", 
                        i + 1, testCases.size(), result.getSuccessfulCount(), result.getFailedCount());
                
                // Add a small delay to avoid rate limiting
                Thread.sleep(100);
                
            } catch (JiraApiException e) {
                logger.error("Failed to create subtask {}: {}", summary, e.getMessage());
                result.addFailure(summary, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrupted during subtask creation");
                result.addFailure(summary, "Thread interrupted");
            }
        }
        
        logger.info("Completed: {}/{} subtasks created successfully, {} failed", 
                result.getSuccessfulCount(), testCases.size(), result.getFailedCount());
        
        return result;
    }
    
    /**
     * Get issue type ID for a given issue type name
     * This is useful for validation before creating issues
     */
    public String getIssueTypeId(String projectKey, String issueTypeName) throws JiraApiException {
        logger.info("Getting issue type ID for '{}' in project {}", issueTypeName, projectKey);
        
        String url = String.format("%s/rest/api/3/issue/createmeta?projectKeys=%s&issuetypeNames=%s&expand=projects.issuetypes.fields",
                config.getJiraUrl(), projectKey, java.net.URLEncoder.encode(issueTypeName, StandardCharsets.UTF_8));
        
        try {
            String response = makeHttpRequest(url);
            JsonNode responseNode = objectMapper.readTree(response);
            
            // Parse the response to find issue type ID
            JsonNode projects = responseNode.get("projects");
            if (projects != null && projects.isArray() && projects.size() > 0) {
                JsonNode project = projects.get(0);
                JsonNode issueTypes = project.get("issuetypes");
                if (issueTypes != null && issueTypes.isArray()) {
                    for (JsonNode issueType : issueTypes) {
                        if (issueTypeName.equalsIgnoreCase(issueType.get("name").asText())) {
                            String id = issueType.get("id").asText();
                            logger.info("Found issue type ID: {} for '{}'", id, issueTypeName);
                            return id;
                        }
                    }
                }
            }
            
            throw new JiraApiException("Issue type '" + issueTypeName + "' not found in project " + projectKey);
            
        } catch (IOException e) {
            logger.error("Failed to get issue type ID: {}", e.getMessage());
            throw new JiraApiException("Failed to get issue type ID: " + e.getMessage(), e);
        }
    }
    
    private String makeHttpPostRequest(String url, String jsonPayload) throws JiraApiException {
        HttpPost request = new HttpPost(url);
        
        // Add headers
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        
        // Add Basic Auth header
        String auth = config.getUsername() + ":" + (config.isUseApiToken() ? config.getApiToken() : config.getPassword());
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        request.setHeader("Authorization", "Basic " + encodedAuth);
        
        // Set request body
        try {
            request.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            
            if (statusCode == 201) {
                return responseBody;
            } else if (statusCode == 400) {
                logger.error("Bad request. Response: {}", responseBody);
                throw new JiraApiException("Bad request: " + extractErrorMessage(responseBody));
            } else if (statusCode == 401) {
                throw new JiraApiException("Authentication failed. Please check your credentials.");
            } else if (statusCode == 403) {
                throw new JiraApiException("Access forbidden. Please check your permissions.");
            } else if (statusCode == 404) {
                throw new JiraApiException("Resource not found. Please check the project key or parent issue key.");
            } else {
                logger.error("HTTP error: {}. Response: {}", statusCode, responseBody);
                throw new JiraApiException("HTTP error: " + statusCode + " - " + 
                        response.getStatusLine().getReasonPhrase() + ". " + extractErrorMessage(responseBody));
            }
            
        } catch (IOException e) {
            throw new JiraApiException("Failed to make HTTP POST request: " + e.getMessage(), e);
        } finally {
            request.releaseConnection();
        }
    }
    
    private String makeHttpRequest(String url) throws JiraApiException {
        org.apache.http.client.methods.HttpGet request = new org.apache.http.client.methods.HttpGet(url);
        
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
                throw new JiraApiException("Resource not found.");
            } else {
                throw new JiraApiException("HTTP error: " + statusCode + " - " + response.getStatusLine().getReasonPhrase());
            }
            
        } catch (IOException e) {
            throw new JiraApiException("Failed to make HTTP request: " + e.getMessage(), e);
        } finally {
            request.releaseConnection();
        }
    }
    
    private String extractErrorMessage(String responseBody) {
        try {
            JsonNode errorNode = objectMapper.readTree(responseBody);
            if (errorNode.has("errorMessages")) {
                return errorNode.get("errorMessages").toString();
            }
            if (errorNode.has("errors")) {
                return errorNode.get("errors").toString();
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return responseBody;
    }
    
    /**
     * Result class for batch creation operations
     */
    public static class CreationResult {
        private final List<String> successfulKeys = new ArrayList<>();
        private final List<String> successfulSummaries = new ArrayList<>();
        private final List<String> failedSummaries = new ArrayList<>();
        private final List<String> failureMessages = new ArrayList<>();
        
        public void addSuccess(String issueKey, String summary) {
            successfulKeys.add(issueKey);
            successfulSummaries.add(summary);
        }
        
        public void addFailure(String summary, String errorMessage) {
            failedSummaries.add(summary);
            failureMessages.add(errorMessage);
        }
        
        public List<String> getSuccessfulKeys() {
            return new ArrayList<>(successfulKeys);
        }
        
        public List<String> getSuccessfulSummaries() {
            return new ArrayList<>(successfulSummaries);
        }
        
        public List<String> getFailedSummaries() {
            return new ArrayList<>(failedSummaries);
        }
        
        public List<String> getFailureMessages() {
            return new ArrayList<>(failureMessages);
        }
        
        public int getSuccessfulCount() {
            return successfulKeys.size();
        }
        
        public int getFailedCount() {
            return failedSummaries.size();
        }
        
        public void printSummary() {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("CREATION SUMMARY");
            System.out.println("=".repeat(80));
            System.out.println("Successful: " + getSuccessfulCount());
            System.out.println("Failed: " + getFailedCount());
            
            if (!successfulKeys.isEmpty()) {
                System.out.println("\nSuccessfully created issues:");
                for (int i = 0; i < successfulKeys.size(); i++) {
                    System.out.println("  " + successfulKeys.get(i) + " - " + successfulSummaries.get(i));
                }
            }
            
            if (!failedSummaries.isEmpty()) {
                System.out.println("\nFailed to create:");
                for (int i = 0; i < failedSummaries.size(); i++) {
                    System.out.println("  " + failedSummaries.get(i) + " - " + failureMessages.get(i));
                }
            }
            System.out.println("=".repeat(80));
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

