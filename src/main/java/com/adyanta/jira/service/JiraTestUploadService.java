package com.adyanta.jira.service;

import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.model.TestCase;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Main service class that orchestrates reading test cases from CSV
 * and uploading them to Jira as subtasks.
 * This is the primary entry point for the CSV to Jira upload functionality.
 */
public class JiraTestUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraTestUploadService.class);
    
    private final CsvTestReaderService csvReader;
    private final JiraIssueCreatorService issueCreator;
    
    public JiraTestUploadService(JiraConfig config) {
        this.csvReader = new CsvTestReaderService();
        this.issueCreator = new JiraIssueCreatorService(config);
    }
    
    /**
     * Upload test cases from CSV file to Jira as subtasks
     * 
     * @param csvFilePath Path to the CSV file
     * @param projectKey Jira project key (e.g., "PROJ")
     * @param parentKey Parent issue key (required for subtasks, e.g., "PROJ-123")
     * @return CreationResult with success and failure information
     * @throws IOException If CSV file cannot be read
     * @throws CsvException If CSV parsing fails
     */
    public JiraIssueCreatorService.CreationResult uploadTestCasesFromCsv(
            String csvFilePath, String projectKey, String parentKey) 
            throws IOException, CsvException {
        
        logger.info("Starting upload process: CSV={}, Project={}, Parent={}", 
                csvFilePath, projectKey, parentKey);
        
        // Validate CSV file
        csvReader.validateCsvFile(csvFilePath);
        
        // Read test cases from CSV
        List<TestCase> testCases = csvReader.readTestCases(csvFilePath);
        
        if (testCases.isEmpty()) {
            logger.warn("No test cases found in CSV file: {}", csvFilePath);
            JiraIssueCreatorService.CreationResult emptyResult = 
                    new JiraIssueCreatorService.CreationResult();
            return emptyResult;
        }
        
        logger.info("Read {} test cases from CSV. Starting upload to Jira...", testCases.size());
        
        // Create subtasks in Jira
        JiraIssueCreatorService.CreationResult result = 
                issueCreator.createSubtasksWithErrorHandling(testCases, projectKey, parentKey);
        
        logger.info("Upload process completed. Created: {}, Failed: {}", 
                result.getSuccessfulCount(), result.getFailedCount());
        
        return result;
    }
    
    /**
     * Upload test cases from CSV file to Jira as subtasks
     * Uses parent key from CSV if not provided
     * 
     * @param csvFilePath Path to the CSV file
     * @param projectKey Jira project key
     * @param parentKey Parent issue key (optional, will use CSV value if not provided)
     * @param useCsvParentKey If true, use parent key from CSV; if false, use provided parentKey
     * @return CreationResult with success and failure information
     */
    public JiraIssueCreatorService.CreationResult uploadTestCasesFromCsv(
            String csvFilePath, String projectKey, String parentKey, boolean useCsvParentKey) 
            throws IOException, CsvException {
        
        // Read test cases first
        csvReader.validateCsvFile(csvFilePath);
        List<TestCase> testCases = csvReader.readTestCases(csvFilePath);
        
        if (testCases.isEmpty()) {
            logger.warn("No test cases found in CSV file: {}", csvFilePath);
            return new JiraIssueCreatorService.CreationResult();
        }
        
        // Process each test case
        JiraIssueCreatorService.CreationResult overallResult = 
                new JiraIssueCreatorService.CreationResult();
        
        for (TestCase testCase : testCases) {
            String effectiveParentKey = useCsvParentKey && testCase.getParentKey() != null 
                    ? testCase.getParentKey() 
                    : parentKey;
            
            String effectiveProjectKey = testCase.getProjectKey() != null 
                    ? testCase.getProjectKey() 
                    : projectKey;
            
            try {
                String issueKey = issueCreator.createSubtask(testCase, effectiveProjectKey, effectiveParentKey);
                overallResult.addSuccess(issueKey, testCase.getSummary() != null ? testCase.getSummary() : "Unknown");
            } catch (JiraIssueCreatorService.JiraApiException e) {
                overallResult.addFailure(
                        testCase.getSummary() != null ? testCase.getSummary() : "Unknown", 
                        e.getMessage());
            }
        }
        
        return overallResult;
    }
    
    /**
     * Preview test cases from CSV without uploading
     * 
     * @param csvFilePath Path to the CSV file
     * @return List of test cases
     * @throws IOException If CSV file cannot be read
     * @throws CsvException If CSV parsing fails
     */
    public List<TestCase> previewTestCases(String csvFilePath) throws IOException, CsvException {
        logger.info("Previewing test cases from CSV file: {}", csvFilePath);
        
        csvReader.validateCsvFile(csvFilePath);
        List<TestCase> testCases = csvReader.readTestCases(csvFilePath);
        
        logger.info("Found {} test cases in CSV", testCases.size());
        
        return testCases;
    }
    
    /**
     * Print test cases preview
     */
    public void printTestCasesPreview(List<TestCase> testCases) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST CASES PREVIEW");
        System.out.println("=".repeat(80));
        System.out.println("Total test cases: " + testCases.size());
        System.out.println();
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            System.out.println("Test Case #" + (i + 1));
            System.out.println("-".repeat(40));
            
            if (testCase.getSummary() != null) {
                System.out.println("Summary: " + testCase.getSummary());
            }
            if (testCase.getDescription() != null) {
                System.out.println("Description: " + testCase.getDescription());
            }
            if (testCase.getPriority() != null) {
                System.out.println("Priority: " + testCase.getPriority());
            }
            if (testCase.getAssignee() != null) {
                System.out.println("Assignee: " + testCase.getAssignee());
            }
            if (testCase.getParentKey() != null) {
                System.out.println("Parent: " + testCase.getParentKey());
            }
            
            // Print all other fields
            testCase.getFields().entrySet().stream()
                    .filter(entry -> !isStandardField(entry.getKey()))
                    .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
            
            System.out.println();
        }
        System.out.println("=".repeat(80));
    }
    
    private boolean isStandardField(String fieldName) {
        String lower = fieldName.toLowerCase();
        return lower.equals("summary") || lower.equals("title") ||
               lower.equals("description") || lower.equals("desc") ||
               lower.equals("priority") ||
               lower.equals("assignee") ||
               lower.equals("parent") || lower.equals("parentkey") ||
               lower.equals("project") || lower.equals("projectkey");
    }
}

