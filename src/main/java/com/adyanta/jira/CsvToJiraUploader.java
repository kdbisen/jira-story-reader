package com.adyanta.jira;

import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.model.TestCase;
import com.adyanta.jira.service.JiraIssueCreatorService;
import com.adyanta.jira.service.JiraTestUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Scanner;

/**
 * Example application for uploading test cases from CSV to Jira as subtasks.
 * This demonstrates the complete workflow of reading CSV and creating Jira issues.
 */
public class CsvToJiraUploader {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvToJiraUploader.class);
    
    public static void main(String[] args) {
        logger.info("Starting CSV to Jira Uploader");
        
        try {
            // Initialize configuration and service
            JiraConfig config = new JiraConfig();
            JiraTestUploadService uploadService = new JiraTestUploadService(config);
            
            Scanner scanner = new Scanner(System.in);
            
            // Get CSV file path
            System.out.print("Enter CSV file path (or press Enter for 'test-cases-template.csv'): ");
            String csvFilePath = scanner.nextLine().trim();
            if (csvFilePath.isEmpty()) {
                csvFilePath = "test-cases-template.csv";
            }
            
            // Get project key
            System.out.print("Enter Jira project key (e.g., PROJ): ");
            String projectKey = scanner.nextLine().trim();
            if (projectKey.isEmpty()) {
                System.out.println("Project key is required!");
                return;
            }
            
            // Get parent key
            System.out.print("Enter parent issue key (e.g., PROJ-123): ");
            String parentKey = scanner.nextLine().trim();
            if (parentKey.isEmpty()) {
                System.out.println("Parent issue key is required for subtasks!");
                return;
            }
            
            // Preview test cases
            System.out.println("\n" + "=".repeat(80));
            System.out.println("PREVIEWING TEST CASES");
            System.out.println("=".repeat(80));
            List<TestCase> testCases = uploadService.previewTestCases(csvFilePath);
            uploadService.printTestCasesPreview(testCases);
            
            // Confirm upload
            System.out.print("\nDo you want to upload these test cases to Jira? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (!confirm.equals("yes") && !confirm.equals("y")) {
                System.out.println("Upload cancelled.");
                return;
            }
            
            // Upload test cases
            System.out.println("\n" + "=".repeat(80));
            System.out.println("UPLOADING TEST CASES TO JIRA");
            System.out.println("=".repeat(80));
            JiraIssueCreatorService.CreationResult result = 
                    uploadService.uploadTestCasesFromCsv(csvFilePath, projectKey, parentKey);
            
            // Print results
            result.printSummary();
            
            System.out.println("\nUpload process completed!");
            
        } catch (Exception e) {
            logger.error("Application failed: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

