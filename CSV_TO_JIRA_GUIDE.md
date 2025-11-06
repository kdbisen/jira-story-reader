# CSV to Jira Subtask Upload Guide

This guide explains how to upload test cases from a CSV file to Jira as subtasks.

## Overview

The solution provides a generic approach to:
1. Read test cases from CSV files with flexible column structures
2. Upload them to Jira as subtask issue types
3. Handle all Jira field types (standard and custom fields)

## Components

### 1. **CsvTestReaderService**
   - Reads test cases from CSV files
   - Supports any CSV column structure
   - Validates CSV file format

### 2. **JiraIssueCreatorService**
   - Creates Jira issues (including subtasks)
   - Supports all Jira field types
   - Handles batch creation with error handling

### 3. **JiraTestUploadService**
   - Main orchestration service
   - Combines CSV reading and Jira creation
   - Provides preview functionality

### 4. **Models**
   - `TestCase`: Flexible model for CSV test case data
   - `IssueCreationRequest`: Generic model for creating Jira issues with all field support

## CSV File Format

### Required Columns
- **summary** (or **title**): The test case title/summary
- **parent** (or **parentKey**): The parent issue key (e.g., "PROJ-123")

### Optional Standard Columns
- **description** (or **desc**): Test case description
- **priority**: Priority level (e.g., "High", "Medium", "Low")
- **assignee**: Assignee account ID or username
- **reporter**: Reporter account ID or username
- **labels**: Comma-separated labels
- **components**: Comma-separated component names
- **fixVersions**: Comma-separated version names
- **project** (or **projectKey**): Project key (if different from default)

### Custom Fields
You can include any custom field in your CSV. For custom fields:
- Use the field ID (e.g., `customfield_10014`)
- Or use a descriptive name that will be mapped

### Example CSV

```csv
summary,description,priority,assignee,parent,labels,components,customfield_10014
Test Case 1: Login,Verify login functionality,High,user@example.com,PROJ-123,testing,authentication,Acceptance Criteria 1
Test Case 2: Logout,Verify logout functionality,Medium,user@example.com,PROJ-123,testing,authentication,Acceptance Criteria 2
```

## Usage Examples

### Basic Usage

```java
import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.service.JiraTestUploadService;
import com.adyanta.jira.service.JiraIssueCreatorService;

// Initialize configuration
JiraConfig config = new JiraConfig();

// Create upload service
JiraTestUploadService uploadService = new JiraTestUploadService(config);

// Upload test cases
String csvFilePath = "test-cases.csv";
String projectKey = "PROJ";
String parentKey = "PROJ-123";

JiraIssueCreatorService.CreationResult result = 
    uploadService.uploadTestCasesFromCsv(csvFilePath, projectKey, parentKey);

// Print results
result.printSummary();
```

### Preview Before Upload

```java
// Preview test cases without uploading
List<TestCase> testCases = uploadService.previewTestCases("test-cases.csv");
uploadService.printTestCasesPreview(testCases);
```

### Advanced: Using Different Parent Keys per Test Case

```java
// Use parent key from CSV for each test case
JiraIssueCreatorService.CreationResult result = 
    uploadService.uploadTestCasesFromCsv(csvFilePath, projectKey, null, true);
```

### Direct Service Usage

```java
import com.adyanta.jira.service.CsvTestReaderService;
import com.adyanta.jira.service.JiraIssueCreatorService;

// Read CSV
CsvTestReaderService csvReader = new CsvTestReaderService();
List<TestCase> testCases = csvReader.readTestCases("test-cases.csv");

// Create issues
JiraIssueCreatorService issueCreator = new JiraIssueCreatorService(config);
List<String> createdKeys = issueCreator.createSubtasks(testCases, "PROJ", "PROJ-123");
```

### Creating Custom Issue Requests

```java
import com.adyanta.jira.model.IssueCreationRequest;

IssueCreationRequest request = new IssueCreationRequest()
    .setProject("PROJ")
    .setIssueType("Subtask")
    .setParent("PROJ-123")
    .setSummary("Test Case Summary")
    .setDescription("Test Case Description")
    .setPriority("High")
    .setAssignee("user@example.com")
    .setLabels("testing,automation")
    .setComponents("backend,frontend")
    .setCustomField("customfield_10014", "Custom Value")
    .setCustomFieldOption("customfield_10015", "Option Value")
    .setCustomFieldUser("customfield_10016", "account-id");

String issueKey = issueCreator.createIssue(request);
```

## Field Type Support

The solution supports all Jira field types:

### Standard Fields
- Text fields (summary, description)
- Select lists (priority, issue type)
- User fields (assignee, reporter)
- Multi-select (labels, components, fix versions)
- Parent field (for subtasks)

### Custom Fields
- Text custom fields
- Number custom fields
- User custom fields
- Select list custom fields
- Multi-select custom fields
- Date fields
- And more...

## Error Handling

The service includes comprehensive error handling:

1. **CSV Validation**: Validates CSV file structure before processing
2. **Batch Processing**: Continues processing even if some issues fail
3. **Detailed Results**: Returns success and failure information
4. **Logging**: Comprehensive logging for debugging

## Configuration

Ensure your `jira-config.properties` is properly configured:

```properties
jira.url=https://your-domain.atlassian.net
jira.username=your-email@example.com
jira.api.token=your-api-token
jira.use.api.token=true
```

## Best Practices

1. **Preview First**: Always preview test cases before uploading
2. **Validate CSV**: Ensure CSV has required columns (summary, parent)
3. **Test with Small Batch**: Test with a small CSV file first
4. **Check Permissions**: Ensure your Jira user has permission to create subtasks
5. **Parent Issue**: Ensure parent issue exists and is not a subtask itself
6. **Rate Limiting**: The service includes delays to avoid rate limiting

## Troubleshooting

### Common Issues

1. **"Parent issue not found"**
   - Verify parent issue key exists
   - Ensure parent issue is not a subtask

2. **"Issue type 'Subtask' not found"**
   - Verify subtask issue type exists in your Jira instance
   - Check project configuration

3. **"Authentication failed"**
   - Verify credentials in config file
   - Check API token is valid

4. **"Access forbidden"**
   - Ensure user has permission to create issues
   - Check project permissions

5. **"Custom field not found"**
   - Verify custom field ID is correct
   - Check field is available for subtask issue type

## API Reference

### JiraTestUploadService Methods

- `uploadTestCasesFromCsv(csvFilePath, projectKey, parentKey)`: Upload test cases
- `uploadTestCasesFromCsv(csvFilePath, projectKey, parentKey, useCsvParentKey)`: Upload with flexible parent key
- `previewTestCases(csvFilePath)`: Preview test cases without uploading
- `printTestCasesPreview(testCases)`: Print formatted preview

### JiraIssueCreatorService Methods

- `createIssue(request)`: Create a single issue
- `createSubtask(testCase, projectKey, parentKey)`: Create a single subtask
- `createSubtasks(testCases, projectKey, parentKey)`: Create multiple subtasks
- `createSubtasksWithErrorHandling(...)`: Create with error handling
- `getIssueTypeId(projectKey, issueTypeName)`: Get issue type ID

### IssueCreationRequest Methods

- `setProject(key)`: Set project
- `setIssueType(name)`: Set issue type
- `setSummary(text)`: Set summary
- `setDescription(text)`: Set description
- `setPriority(name)`: Set priority
- `setAssignee(accountId)`: Set assignee
- `setParent(key)`: Set parent (for subtasks)
- `setLabels(labels)`: Set labels
- `setComponents(names)`: Set components
- `setCustomField(fieldId, value)`: Set custom field
- `setField(name, value)`: Generic field setter

## Example Complete Program

```java
package com.adyanta.jira;

import com.adyanta.jira.config.JiraConfig;
import com.adyanta.jira.service.JiraTestUploadService;
import com.adyanta.jira.service.JiraIssueCreatorService;

public class CsvToJiraUploader {
    public static void main(String[] args) {
        try {
            // Initialize
            JiraConfig config = new JiraConfig();
            JiraTestUploadService uploadService = new JiraTestUploadService(config);
            
            // Configuration
            String csvFilePath = "test-cases.csv";
            String projectKey = "PROJ";
            String parentKey = "PROJ-123";
            
            // Preview first
            System.out.println("Previewing test cases...");
            uploadService.printTestCasesPreview(
                uploadService.previewTestCases(csvFilePath)
            );
            
            // Upload
            System.out.println("\nUploading test cases to Jira...");
            JiraIssueCreatorService.CreationResult result = 
                uploadService.uploadTestCasesFromCsv(csvFilePath, projectKey, parentKey);
            
            // Print results
            result.printSummary();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

## Notes

- The solution is generic and handles any CSV column structure
- All Jira field types are supported
- Error handling ensures partial success scenarios are handled gracefully
- The service includes rate limiting protection
- Comprehensive logging helps with debugging

