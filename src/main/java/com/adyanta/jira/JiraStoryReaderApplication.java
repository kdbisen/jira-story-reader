package com.adyanta.jira;

import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.model.JiraStory;
import com.adyanta.jira.service.JiraStoryReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class demonstrating how to use the Jira Story Reader.
 * This class provides examples of various ways to fetch and display Jira story information.
 */
public class JiraStoryReaderApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraStoryReaderApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting Jira Story Reader Application");
        
        try {
            // Initialize configuration and service
            JiraConfig config = new JiraConfig();
            JiraStoryReaderService service = new JiraStoryReaderService(config);
            
            // Run interactive menu
            runInteractiveMenu(service);
            
        } catch (Exception e) {
            logger.error("Application failed to start: {}", e.getMessage(), e);
            System.err.println("Failed to start application: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void runInteractiveMenu(JiraStoryReaderService service) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            printMenu();
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        fetchSingleStory(service, scanner);
                        break;
                    case 2:
                        fetchMultipleStories(service, scanner);
                        break;
                    case 3:
                        searchStoriesByJQL(service, scanner);
                        break;
                    case 4:
                        fetchStoriesByProject(service, scanner);
                        break;
                    case 5:
                        fetchStoriesByAssignee(service, scanner);
                        break;
                    case 6:
                        fetchStoriesBySprint(service, scanner);
                        break;
                    case 7:
                        fetchStoriesByStatus(service, scanner);
                        break;
                    case 8:
                        runExampleScenarios(service);
                        break;
                    case 9:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                logger.error("Error in menu: {}", e.getMessage(), e);
            }
        }
    }
    
    private static void printMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           JIRA STORY READER APPLICATION");
        System.out.println("=".repeat(60));
        System.out.println("1. Fetch Single Story by Key");
        System.out.println("2. Fetch Multiple Stories by Keys");
        System.out.println("3. Search Stories by JQL");
        System.out.println("4. Fetch Stories by Project");
        System.out.println("5. Fetch Stories by Assignee");
        System.out.println("6. Fetch Stories by Sprint");
        System.out.println("7. Fetch Stories by Status");
        System.out.println("8. Run Example Scenarios");
        System.out.println("9. Exit");
        System.out.println("=".repeat(60));
    }
    
    private static void fetchSingleStory(JiraStoryReaderService service, Scanner scanner) {
        System.out.print("Enter story key (e.g., PROJ-123): ");
        String storyKey = scanner.nextLine().trim();
        
        if (storyKey.isEmpty()) {
            System.out.println("Story key cannot be empty.");
            return;
        }
        
        try {
            JiraStory story = service.getStoryByKey(storyKey);
            service.printStoryDetails(story);
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error fetching story: " + e.getMessage());
        }
    }
    
    private static void fetchMultipleStories(JiraStoryReaderService service, Scanner scanner) {
        System.out.print("Enter story keys separated by commas (e.g., PROJ-123,PROJ-124,PROJ-125): ");
        String input = scanner.nextLine().trim();
        
        if (input.isEmpty()) {
            System.out.println("Story keys cannot be empty.");
            return;
        }
        
        List<String> storyKeys = Arrays.asList(input.split(","));
        storyKeys.replaceAll(String::trim);
        
        try {
            List<JiraStory> stories = service.getStoriesByKeys(storyKeys);
            
            System.out.println("\nFound " + stories.size() + " stories:");
            for (JiraStory story : stories) {
                service.printStoryDetails(story);
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error fetching stories: " + e.getMessage());
        }
    }
    
    private static void searchStoriesByJQL(JiraStoryReaderService service, Scanner scanner) {
        System.out.println("Enter JQL query (e.g., project = PROJ AND issuetype = Story AND status = 'To Do'):");
        System.out.print("JQL: ");
        String jql = scanner.nextLine().trim();
        
        if (jql.isEmpty()) {
            System.out.println("JQL query cannot be empty.");
            return;
        }
        
        try {
            List<JiraStory> stories = service.searchStories(jql);
            
            System.out.println("\nFound " + stories.size() + " stories:");
            for (JiraStory story : stories) {
                service.printStoryDetails(story);
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error searching stories: " + e.getMessage());
        }
    }
    
    private static void fetchStoriesByProject(JiraStoryReaderService service, Scanner scanner) {
        System.out.print("Enter project key (e.g., PROJ): ");
        String projectKey = scanner.nextLine().trim();
        
        if (projectKey.isEmpty()) {
            System.out.println("Project key cannot be empty.");
            return;
        }
        
        try {
            List<JiraStory> stories = service.getStoriesByProject(projectKey);
            
            System.out.println("\nFound " + stories.size() + " stories in project " + projectKey + ":");
            for (JiraStory story : stories) {
                service.printStoryDetails(story);
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error fetching stories: " + e.getMessage());
        }
    }
    
    private static void fetchStoriesByAssignee(JiraStoryReaderService service, Scanner scanner) {
        System.out.print("Enter assignee (email or username): ");
        String assignee = scanner.nextLine().trim();
        
        if (assignee.isEmpty()) {
            System.out.println("Assignee cannot be empty.");
            return;
        }
        
        try {
            List<JiraStory> stories = service.getStoriesByAssignee(assignee);
            
            System.out.println("\nFound " + stories.size() + " stories assigned to " + assignee + ":");
            for (JiraStory story : stories) {
                service.printStoryDetails(story);
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error fetching stories: " + e.getMessage());
        }
    }
    
    private static void fetchStoriesBySprint(JiraStoryReaderService service, Scanner scanner) {
        System.out.print("Enter sprint name: ");
        String sprintName = scanner.nextLine().trim();
        
        if (sprintName.isEmpty()) {
            System.out.println("Sprint name cannot be empty.");
            return;
        }
        
        try {
            List<JiraStory> stories = service.getStoriesBySprint(sprintName);
            
            System.out.println("\nFound " + stories.size() + " stories in sprint " + sprintName + ":");
            for (JiraStory story : stories) {
                service.printStoryDetails(story);
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error fetching stories: " + e.getMessage());
        }
    }
    
    private static void fetchStoriesByStatus(JiraStoryReaderService service, Scanner scanner) {
        System.out.print("Enter status (e.g., 'To Do', 'In Progress', 'Done'): ");
        String status = scanner.nextLine().trim();
        
        if (status.isEmpty()) {
            System.out.println("Status cannot be empty.");
            return;
        }
        
        try {
            List<JiraStory> stories = service.getStoriesByStatus(status);
            
            System.out.println("\nFound " + stories.size() + " stories with status " + status + ":");
            for (JiraStory story : stories) {
                service.printStoryDetails(story);
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error fetching stories: " + e.getMessage());
        }
    }
    
    private static void runExampleScenarios(JiraStoryReaderService service) {
        System.out.println("\nRunning Example Scenarios...");
        System.out.println("=".repeat(50));
        
        // Example 1: Fetch a single story
        System.out.println("\nExample 1: Fetching a single story");
        System.out.println("Note: Replace 'PROJ-123' with an actual story key from your Jira instance");
        try {
            // This will likely fail unless you have a real story key, but shows the usage
            JiraStory story = service.getStoryByKey("PROJ-123");
            service.printStoryDetails(story);
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Expected error (no real story key): " + e.getMessage());
        }
        
        // Example 2: Search with JQL
        System.out.println("\nExample 2: Searching stories with JQL");
        System.out.println("Searching for stories with 'Story' issue type...");
        try {
            List<JiraStory> stories = service.searchStories("issuetype = Story ORDER BY created DESC");
            System.out.println("Found " + stories.size() + " stories");
            
            if (!stories.isEmpty()) {
                System.out.println("First story details:");
                service.printStoryDetails(stories.get(0));
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error in JQL search: " + e.getMessage());
        }
        
        // Example 3: Extract specific information
        System.out.println("\nExample 3: Extracting specific information");
        try {
            List<JiraStory> stories = service.searchStories("issuetype = Story ORDER BY created DESC");
            
            if (!stories.isEmpty()) {
                JiraStory firstStory = stories.get(0);
                
                System.out.println("Story Key: " + firstStory.getKey());
                System.out.println("Summary: " + service.getSummary(firstStory));
                System.out.println("Description: " + service.getDescription(firstStory));
                System.out.println("Acceptance Criteria: " + service.getAcceptanceCriteria(firstStory));
            }
        } catch (JiraStoryReaderService.JiraApiException e) {
            System.out.println("Error extracting information: " + e.getMessage());
        }
        
        System.out.println("\nExample scenarios completed!");
    }
}
