package com.adyanta.jira.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a test case from CSV file.
 * Uses a flexible Map structure to handle any CSV columns dynamically.
 */
public class TestCase {
    
    private Map<String, String> fields;
    
    public TestCase() {
        this.fields = new HashMap<>();
    }
    
    public TestCase(Map<String, String> fields) {
        this.fields = fields != null ? new HashMap<>(fields) : new HashMap<>();
    }
    
    /**
     * Get a field value by name (case-insensitive)
     */
    public String getField(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        return fields.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(fieldName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Set a field value
     */
    public void setField(String fieldName, String value) {
        if (fieldName != null) {
            fields.put(fieldName, value);
        }
    }
    
    /**
     * Get all fields as a map
     */
    public Map<String, String> getFields() {
        return new HashMap<>(fields);
    }
    
    /**
     * Set all fields from a map
     */
    public void setFields(Map<String, String> fields) {
        this.fields = fields != null ? new HashMap<>(fields) : new HashMap<>();
    }
    
    /**
     * Convenience methods for common fields
     */
    public String getSummary() {
        return getField("summary") != null ? getField("summary") : getField("title");
    }
    
    public String getDescription() {
        return getField("description") != null ? getField("description") : getField("desc");
    }
    
    public String getPriority() {
        return getField("priority");
    }
    
    public String getAssignee() {
        return getField("assignee");
    }
    
    public String getLabels() {
        return getField("labels");
    }
    
    public String getComponents() {
        return getField("components");
    }
    
    public String getParentKey() {
        return getField("parent") != null ? getField("parent") : getField("parentKey");
    }
    
    public String getProjectKey() {
        return getField("project") != null ? getField("project") : getField("projectKey");
    }
    
    @Override
    public String toString() {
        return "TestCase{" +
                "fields=" + fields +
                '}';
    }
}

