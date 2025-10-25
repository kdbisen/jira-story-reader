package com.adyanta.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Represents a Jira Story with all relevant information including
 * acceptance criteria and description.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraStory {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("self")
    private String self;
    
    @JsonProperty("fields")
    private Fields fields;
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getSelf() {
        return self;
    }
    
    public void setSelf(String self) {
        this.self = self;
    }
    
    public Fields getFields() {
        return fields;
    }
    
    public void setFields(Fields fields) {
        this.fields = fields;
    }
    
    @Override
    public String toString() {
        return "JiraStory{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", fields=" + fields +
                '}';
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        
        @JsonProperty("summary")
        private String summary;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("issuetype")
        private IssueType issueType;
        
        @JsonProperty("status")
        private Status status;
        
        @JsonProperty("priority")
        private Priority priority;
        
        @JsonProperty("assignee")
        private User assignee;
        
        @JsonProperty("reporter")
        private User reporter;
        
        @JsonProperty("created")
        private String created;
        
        @JsonProperty("updated")
        private String updated;
        
        @JsonProperty("customfield_10014") // Common field for acceptance criteria
        private String acceptanceCriteria;
        
        @JsonProperty("customfield_10015") // Alternative field for acceptance criteria
        private String acceptanceCriteriaAlt;
        
        @JsonProperty("customfield_10016") // Another common field
        private String acceptanceCriteriaAlt2;
        
        // Additional custom fields that might contain acceptance criteria
        @JsonProperty("customfield_10017")
        private String customField1;
        
        @JsonProperty("customfield_10018")
        private String customField2;
        
        // Epic link
        @JsonProperty("customfield_10020")
        private String epicLink;
        
        // Story points
        @JsonProperty("customfield_10021")
        private Double storyPoints;
        
        // Sprint information
        @JsonProperty("customfield_10022")
        private List<String> sprint;
        
        // Labels
        @JsonProperty("labels")
        private List<String> labels;
        
        // Components
        @JsonProperty("components")
        private List<Component> components;
        
        // Fix versions
        @JsonProperty("fixVersions")
        private List<Version> fixVersions;
        
        // Getters and Setters
        public String getSummary() {
            return summary;
        }
        
        public void setSummary(String summary) {
            this.summary = summary;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public IssueType getIssueType() {
            return issueType;
        }
        
        public void setIssueType(IssueType issueType) {
            this.issueType = issueType;
        }
        
        public Status getStatus() {
            return status;
        }
        
        public void setStatus(Status status) {
            this.status = status;
        }
        
        public Priority getPriority() {
            return priority;
        }
        
        public void setPriority(Priority priority) {
            this.priority = priority;
        }
        
        public User getAssignee() {
            return assignee;
        }
        
        public void setAssignee(User assignee) {
            this.assignee = assignee;
        }
        
        public User getReporter() {
            return reporter;
        }
        
        public void setReporter(User reporter) {
            this.reporter = reporter;
        }
        
        public String getCreated() {
            return created;
        }
        
        public void setCreated(String created) {
            this.created = created;
        }
        
        public String getUpdated() {
            return updated;
        }
        
        public void setUpdated(String updated) {
            this.updated = updated;
        }
        
        public String getAcceptanceCriteria() {
            return acceptanceCriteria;
        }
        
        public void setAcceptanceCriteria(String acceptanceCriteria) {
            this.acceptanceCriteria = acceptanceCriteria;
        }
        
        public String getAcceptanceCriteriaAlt() {
            return acceptanceCriteriaAlt;
        }
        
        public void setAcceptanceCriteriaAlt(String acceptanceCriteriaAlt) {
            this.acceptanceCriteriaAlt = acceptanceCriteriaAlt;
        }
        
        public String getAcceptanceCriteriaAlt2() {
            return acceptanceCriteriaAlt2;
        }
        
        public void setAcceptanceCriteriaAlt2(String acceptanceCriteriaAlt2) {
            this.acceptanceCriteriaAlt2 = acceptanceCriteriaAlt2;
        }
        
        public String getCustomField1() {
            return customField1;
        }
        
        public void setCustomField1(String customField1) {
            this.customField1 = customField1;
        }
        
        public String getCustomField2() {
            return customField2;
        }
        
        public void setCustomField2(String customField2) {
            this.customField2 = customField2;
        }
        
        public String getEpicLink() {
            return epicLink;
        }
        
        public void setEpicLink(String epicLink) {
            this.epicLink = epicLink;
        }
        
        public Double getStoryPoints() {
            return storyPoints;
        }
        
        public void setStoryPoints(Double storyPoints) {
            this.storyPoints = storyPoints;
        }
        
        public List<String> getSprint() {
            return sprint;
        }
        
        public void setSprint(List<String> sprint) {
            this.sprint = sprint;
        }
        
        public List<String> getLabels() {
            return labels;
        }
        
        public void setLabels(List<String> labels) {
            this.labels = labels;
        }
        
        public List<Component> getComponents() {
            return components;
        }
        
        public void setComponents(List<Component> components) {
            this.components = components;
        }
        
        public List<Version> getFixVersions() {
            return fixVersions;
        }
        
        public void setFixVersions(List<Version> fixVersions) {
            this.fixVersions = fixVersions;
        }
        
        /**
         * Get the first available acceptance criteria from any custom field
         */
        public String getFirstAvailableAcceptanceCriteria() {
            if (acceptanceCriteria != null && !acceptanceCriteria.trim().isEmpty()) {
                return acceptanceCriteria;
            }
            if (acceptanceCriteriaAlt != null && !acceptanceCriteriaAlt.trim().isEmpty()) {
                return acceptanceCriteriaAlt;
            }
            if (acceptanceCriteriaAlt2 != null && !acceptanceCriteriaAlt2.trim().isEmpty()) {
                return acceptanceCriteriaAlt2;
            }
            if (customField1 != null && !customField1.trim().isEmpty()) {
                return customField1;
            }
            if (customField2 != null && !customField2.trim().isEmpty()) {
                return customField2;
            }
            return null;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueType {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Priority {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("accountId")
        private String accountId;
        
        @JsonProperty("displayName")
        private String displayName;
        
        @JsonProperty("emailAddress")
        private String emailAddress;
        
        public String getAccountId() {
            return accountId;
        }
        
        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getEmailAddress() {
            return emailAddress;
        }
        
        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("description")
        private String description;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
