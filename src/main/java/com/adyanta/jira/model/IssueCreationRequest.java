package com.adyanta.jira.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Generic model for creating Jira issues with support for all field types.
 * This class provides a flexible way to create issues with any Jira fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueCreationRequest {
    
    @JsonProperty("fields")
    private ObjectNode fields;
    
    private final ObjectMapper objectMapper;
    
    public IssueCreationRequest() {
        this.objectMapper = new ObjectMapper();
        this.fields = objectMapper.createObjectNode();
    }
    
    /**
     * Set project key
     */
    public IssueCreationRequest setProject(String projectKey) {
        ObjectNode project = objectMapper.createObjectNode();
        project.put("key", projectKey);
        fields.set("project", project);
        return this;
    }
    
    /**
     * Set issue type (e.g., "Subtask", "Story", "Bug")
     */
    public IssueCreationRequest setIssueType(String issueTypeName) {
        ObjectNode issueType = objectMapper.createObjectNode();
        issueType.put("name", issueTypeName);
        fields.set("issuetype", issueType);
        return this;
    }
    
    /**
     * Set summary/title
     */
    public IssueCreationRequest setSummary(String summary) {
        fields.put("summary", summary);
        return this;
    }
    
    /**
     * Set description (supports Jira markup)
     */
    public IssueCreationRequest setDescription(String description) {
        if (description != null && !description.trim().isEmpty()) {
            // Jira expects description in ADF (Atlassian Document Format) or plain text
            // For simplicity, we'll use plain text. For rich text, you'd need ADF format
            fields.put("description", description);
        }
        return this;
    }
    
    /**
     * Set priority by name (e.g., "Highest", "High", "Medium", "Low", "Lowest")
     */
    public IssueCreationRequest setPriority(String priorityName) {
        if (priorityName != null && !priorityName.trim().isEmpty()) {
            ObjectNode priority = objectMapper.createObjectNode();
            priority.put("name", priorityName);
            fields.set("priority", priority);
        }
        return this;
    }
    
    /**
     * Set assignee by account ID or username
     */
    public IssueCreationRequest setAssignee(String accountId) {
        if (accountId != null && !accountId.trim().isEmpty()) {
            ObjectNode assignee = objectMapper.createObjectNode();
            assignee.put("accountId", accountId);
            fields.set("assignee", assignee);
        }
        return this;
    }
    
    /**
     * Set reporter by account ID or username
     */
    public IssueCreationRequest setReporter(String accountId) {
        if (accountId != null && !accountId.trim().isEmpty()) {
            ObjectNode reporter = objectMapper.createObjectNode();
            reporter.put("accountId", accountId);
            fields.set("reporter", reporter);
        }
        return this;
    }
    
    /**
     * Set parent issue key (required for subtasks)
     */
    public IssueCreationRequest setParent(String parentKey) {
        if (parentKey != null && !parentKey.trim().isEmpty()) {
            ObjectNode parent = objectMapper.createObjectNode();
            parent.put("key", parentKey);
            fields.set("parent", parent);
        }
        return this;
    }
    
    /**
     * Set labels (comma-separated string or list)
     */
    public IssueCreationRequest setLabels(String labels) {
        if (labels != null && !labels.trim().isEmpty()) {
            List<String> labelList = Arrays.asList(labels.split(","));
            labelList.replaceAll(String::trim);
            fields.set("labels", objectMapper.valueToTree(labelList));
        }
        return this;
    }
    
    /**
     * Set labels from a list
     */
    public IssueCreationRequest setLabels(List<String> labels) {
        if (labels != null && !labels.isEmpty()) {
            fields.set("labels", objectMapper.valueToTree(labels));
        }
        return this;
    }
    
    /**
     * Set components by name (comma-separated string)
     */
    public IssueCreationRequest setComponents(String componentNames) {
        if (componentNames != null && !componentNames.trim().isEmpty()) {
            String[] names = componentNames.split(",");
            List<ObjectNode> components = new ArrayList<>();
            for (String name : names) {
                ObjectNode component = objectMapper.createObjectNode();
                component.put("name", name.trim());
                components.add(component);
            }
            fields.set("components", objectMapper.valueToTree(components));
        }
        return this;
    }
    
    /**
     * Set fix versions by name (comma-separated string)
     */
    public IssueCreationRequest setFixVersions(String versionNames) {
        if (versionNames != null && !versionNames.trim().isEmpty()) {
            String[] names = versionNames.split(",");
            List<ObjectNode> versions = new ArrayList<>();
            for (String name : names) {
                ObjectNode version = objectMapper.createObjectNode();
                version.put("name", name.trim());
                versions.add(version);
            }
            fields.set("fixVersions", objectMapper.valueToTree(versions));
        }
        return this;
    }
    
    /**
     * Set custom field by field ID (e.g., "customfield_10014")
     */
    public IssueCreationRequest setCustomField(String fieldId, Object value) {
        if (fieldId != null && value != null) {
            fields.set(fieldId, objectMapper.valueToTree(value));
        }
        return this;
    }
    
    /**
     * Set custom field as string
     */
    public IssueCreationRequest setCustomField(String fieldId, String value) {
        if (fieldId != null && value != null && !value.trim().isEmpty()) {
            fields.put(fieldId, value);
        }
        return this;
    }
    
    /**
     * Set custom field as number
     */
    public IssueCreationRequest setCustomField(String fieldId, Number value) {
        if (fieldId != null && value != null) {
            fields.putPOJO(fieldId, value);
        }
        return this;
    }
    
    /**
     * Set custom field as user (by account ID)
     */
    public IssueCreationRequest setCustomFieldUser(String fieldId, String accountId) {
        if (fieldId != null && accountId != null && !accountId.trim().isEmpty()) {
            ObjectNode user = objectMapper.createObjectNode();
            user.put("accountId", accountId);
            fields.set(fieldId, user);
        }
        return this;
    }
    
    /**
     * Set custom field as select list option
     */
    public IssueCreationRequest setCustomFieldOption(String fieldId, String optionValue) {
        if (fieldId != null && optionValue != null && !optionValue.trim().isEmpty()) {
            ObjectNode option = objectMapper.createObjectNode();
            option.put("value", optionValue);
            fields.set(fieldId, option);
        }
        return this;
    }
    
    /**
     * Set custom field as multi-select (comma-separated)
     */
    public IssueCreationRequest setCustomFieldMultiSelect(String fieldId, String values) {
        if (fieldId != null && values != null && !values.trim().isEmpty()) {
            String[] valueArray = values.split(",");
            List<ObjectNode> options = new ArrayList<>();
            for (String value : valueArray) {
                ObjectNode option = objectMapper.createObjectNode();
                option.put("value", value.trim());
                options.add(option);
            }
            fields.set(fieldId, objectMapper.valueToTree(options));
        }
        return this;
    }
    
    /**
     * Generic method to set any field by name
     * This allows setting fields dynamically from CSV columns
     */
    public IssueCreationRequest setField(String fieldName, String value) {
        if (fieldName == null || value == null || value.trim().isEmpty()) {
            return this;
        }
        
        // Handle standard fields
        switch (fieldName.toLowerCase()) {
            case "project":
            case "projectkey":
                setProject(value);
                break;
            case "issuetype":
            case "type":
                setIssueType(value);
                break;
            case "summary":
            case "title":
                setSummary(value);
                break;
            case "description":
            case "desc":
                setDescription(value);
                break;
            case "priority":
                setPriority(value);
                break;
            case "assignee":
                setAssignee(value);
                break;
            case "reporter":
                setReporter(value);
                break;
            case "parent":
            case "parentkey":
                setParent(value);
                break;
            case "labels":
                setLabels(value);
                break;
            case "components":
                setComponents(value);
                break;
            case "fixversions":
            case "fix_versions":
                setFixVersions(value);
                break;
            default:
                // Assume it's a custom field if it starts with "customfield_"
                if (fieldName.toLowerCase().startsWith("customfield_")) {
                    setCustomField(fieldName, value);
                } else {
                    // Try to set as custom field with the field name
                    setCustomField(fieldName, value);
                }
                break;
        }
        
        return this;
    }
    
    /**
     * Build the request from a TestCase object
     */
    public static IssueCreationRequest fromTestCase(TestCase testCase, String projectKey, String parentKey) {
        IssueCreationRequest request = new IssueCreationRequest();
        
        // Set required fields
        request.setProject(projectKey != null ? projectKey : testCase.getProjectKey());
        request.setIssueType("Subtask");
        if (parentKey != null) {
            request.setParent(parentKey);
        } else if (testCase.getParentKey() != null) {
            request.setParent(testCase.getParentKey());
        }
        
        // Set standard fields from test case
        if (testCase.getSummary() != null) {
            request.setSummary(testCase.getSummary());
        }
        if (testCase.getDescription() != null) {
            request.setDescription(testCase.getDescription());
        }
        if (testCase.getPriority() != null) {
            request.setPriority(testCase.getPriority());
        }
        if (testCase.getAssignee() != null) {
            request.setAssignee(testCase.getAssignee());
        }
        if (testCase.getLabels() != null) {
            request.setLabels(testCase.getLabels());
        }
        if (testCase.getComponents() != null) {
            request.setComponents(testCase.getComponents());
        }
        
        // Set all other fields from test case (including custom fields)
        Map<String, String> allFields = testCase.getFields();
        for (Map.Entry<String, String> entry : allFields.entrySet()) {
            String fieldName = entry.getKey();
            String value = entry.getValue();
            
            // Skip already processed fields
            if (!isStandardField(fieldName)) {
                request.setField(fieldName, value);
            }
        }
        
        return request;
    }
    
    private static boolean isStandardField(String fieldName) {
        String lower = fieldName.toLowerCase();
        return lower.equals("project") || lower.equals("projectkey") ||
               lower.equals("issuetype") || lower.equals("type") ||
               lower.equals("summary") || lower.equals("title") ||
               lower.equals("description") || lower.equals("desc") ||
               lower.equals("priority") ||
               lower.equals("assignee") ||
               lower.equals("reporter") ||
               lower.equals("parent") || lower.equals("parentkey") ||
               lower.equals("labels") ||
               lower.equals("components") ||
               lower.equals("fixversions") || lower.equals("fix_versions");
    }
    
    /**
     * Get the JSON representation of the request
     */
    public ObjectNode getFields() {
        return fields;
    }
    
    /**
     * Convert to JSON string
     */
    public String toJsonString() {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.set("fields", fields);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to JSON", e);
        }
    }
}

