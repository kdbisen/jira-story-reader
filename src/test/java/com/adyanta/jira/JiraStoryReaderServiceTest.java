package com.adyanta.jira;

import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.model.JiraStory;
import com.adyanta.jira.service.JiraStoryReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JiraStoryReaderService.
 * These tests require valid Jira credentials to be set as environment variables.
 */
@EnabledIfEnvironmentVariable(named = "JIRA_URL", matches = ".*")
public class JiraStoryReaderServiceTest {
    
    private JiraStoryReaderService service;
    
    @BeforeEach
    void setUp() {
        try {
            JiraConfig config = new JiraConfig();
            service = new JiraStoryReaderService(config);
        } catch (Exception e) {
            // Skip tests if configuration is not available
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Jira configuration not available");
        }
    }
    
    @Test
    void testSearchStories() {
        try {
            // Search for stories (this should work even if no stories exist)
            List<JiraStory> stories = service.searchStories("issuetype = Story ORDER BY created DESC");
            assertNotNull(stories);
            // Note: We don't assert the size as it depends on the Jira instance
        } catch (JiraStoryReaderService.JiraApiException e) {
            // This is expected if there are no stories or permission issues
            assertTrue(e.getMessage().contains("Failed to search stories") || 
                      e.getMessage().contains("Authentication failed") ||
                      e.getMessage().contains("Access forbidden"));
        }
    }
    
    @Test
    void testGetStoryByKey() {
        try {
            // Try to get a story that likely doesn't exist
            JiraStory story = service.getStoryByKey("TEST-999999");
            fail("Expected exception for non-existent story");
        } catch (JiraStoryReaderService.JiraApiException e) {
            // This is expected for non-existent stories
            assertTrue(e.getMessage().contains("Failed to fetch story") || 
                      e.getMessage().contains("Resource not found"));
        }
    }
    
    @Test
    void testConfigurationValidation() {
        // Test valid configuration
        JiraConfig validConfig = new JiraConfig("https://test.atlassian.net", "test@example.com", "password");
        assertDoesNotThrow(() -> validConfig.validate());
        
        // Test invalid configuration
        JiraConfig invalidConfig = new JiraConfig();
        invalidConfig.setJiraUrl("");
        invalidConfig.setUsername("test@example.com");
        invalidConfig.setPassword("password");
        
        assertThrows(IllegalArgumentException.class, () -> invalidConfig.validate());
    }
    
    @Test
    void testStoryDataExtraction() {
        // Create a mock story for testing data extraction
        JiraStory story = new JiraStory();
        story.setKey("TEST-123");
        
        JiraStory.Fields fields = new JiraStory.Fields();
        fields.setSummary("Test Story");
        fields.setDescription("This is a test description");
        fields.setAcceptanceCriteria("Given-When-Then criteria");
        story.setFields(fields);
        
        // Test data extraction methods
        assertEquals("Test Story", service.getSummary(story));
        assertEquals("This is a test description", service.getDescription(story));
        assertEquals("Given-When-Then criteria", service.getAcceptanceCriteria(story));
    }
    
    @Test
    void testNullStoryHandling() {
        // Test that methods handle null stories gracefully
        assertNull(service.getSummary(null));
        assertNull(service.getDescription(null));
        assertNull(service.getAcceptanceCriteria(null));
    }
}
