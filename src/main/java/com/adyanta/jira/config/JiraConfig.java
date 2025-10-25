package com.adyanta.jira.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

/**
 * Configuration class for Jira API connection settings.
 * Loads configuration from properties file or environment variables.
 */
public class JiraConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(JiraConfig.class);
    
    private static final String CONFIG_FILE = "jira-config.properties";
    private static final String DEFAULT_CONFIG_FILE = "src/main/resources/jira-config.properties";
    
    private String jiraUrl;
    private String username;
    private String password;
    private String apiToken;
    private int connectionTimeout;
    private int readTimeout;
    private int maxRetries;
    private boolean useApiToken;
    
    public JiraConfig() {
        loadConfiguration();
    }
    
    public JiraConfig(String jiraUrl, String username, String password) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.password = password;
        this.useApiToken = false;
        this.connectionTimeout = 30000;
        this.readTimeout = 60000;
        this.maxRetries = 3;
    }
    
    public JiraConfig(String jiraUrl, String username, String apiToken, boolean useApiToken) {
        this.jiraUrl = jiraUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.useApiToken = useApiToken;
        this.connectionTimeout = 30000;
        this.readTimeout = 60000;
        this.maxRetries = 3;
    }
    
    private void loadConfiguration() {
        try {
            Configurations configs = new Configurations();
            Configuration config = null;
            
            // Try to load from current directory first
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                config = configs.properties(configFile);
                logger.info("Loaded configuration from: {}", configFile.getAbsolutePath());
            } else {
                // Try default location
                configFile = new File(DEFAULT_CONFIG_FILE);
                if (configFile.exists()) {
                    config = configs.properties(configFile);
                    logger.info("Loaded configuration from: {}", configFile.getAbsolutePath());
                }
            }
            
            if (config != null) {
                loadFromConfig(config);
            } else {
                loadFromEnvironment();
                logger.info("No configuration file found, using environment variables");
            }
            
        } catch (ConfigurationException e) {
            logger.warn("Failed to load configuration file, falling back to environment variables: {}", e.getMessage());
            loadFromEnvironment();
        }
    }
    
    private void loadFromConfig(Configuration config) {
        this.jiraUrl = config.getString("jira.url", getEnvVar("JIRA_URL"));
        this.username = config.getString("jira.username", getEnvVar("JIRA_USERNAME"));
        this.password = config.getString("jira.password", getEnvVar("JIRA_PASSWORD"));
        this.apiToken = config.getString("jira.api.token", getEnvVar("JIRA_API_TOKEN"));
        this.useApiToken = config.getBoolean("jira.use.api.token", false);
        this.connectionTimeout = config.getInt("jira.connection.timeout", 30000);
        this.readTimeout = config.getInt("jira.read.timeout", 60000);
        this.maxRetries = config.getInt("jira.max.retries", 3);
    }
    
    private void loadFromEnvironment() {
        this.jiraUrl = getEnvVar("JIRA_URL");
        this.username = getEnvVar("JIRA_USERNAME");
        this.password = getEnvVar("JIRA_PASSWORD");
        this.apiToken = getEnvVar("JIRA_API_TOKEN");
        this.useApiToken = Boolean.parseBoolean(getEnvVar("JIRA_USE_API_TOKEN", "false"));
        this.connectionTimeout = Integer.parseInt(getEnvVar("JIRA_CONNECTION_TIMEOUT", "30000"));
        this.readTimeout = Integer.parseInt(getEnvVar("JIRA_READ_TIMEOUT", "60000"));
        this.maxRetries = Integer.parseInt(getEnvVar("JIRA_MAX_RETRIES", "3"));
    }
    
    private String getEnvVar(String key) {
        return getEnvVar(key, null);
    }
    
    private String getEnvVar(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
    
    public void validate() {
        if (jiraUrl == null || jiraUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Jira URL is required");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        if (useApiToken) {
            if (apiToken == null || apiToken.trim().isEmpty()) {
                throw new IllegalArgumentException("API Token is required when using API token authentication");
            }
        } else {
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required when using password authentication");
            }
        }
        
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("Connection timeout must be positive");
        }
        
        if (readTimeout <= 0) {
            throw new IllegalArgumentException("Read timeout must be positive");
        }
        
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
    }
    
    // Getters and Setters
    public String getJiraUrl() {
        return jiraUrl;
    }
    
    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getApiToken() {
        return apiToken;
    }
    
    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
    
    public boolean isUseApiToken() {
        return useApiToken;
    }
    
    public void setUseApiToken(boolean useApiToken) {
        this.useApiToken = useApiToken;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    /**
     * Get the authentication credentials based on the configured authentication method
     */
    public String getAuthCredentials() {
        if (useApiToken) {
            return username + ":" + apiToken;
        } else {
            return username + ":" + password;
        }
    }
    
    @Override
    public String toString() {
        return "JiraConfig{" +
                "jiraUrl='" + jiraUrl + '\'' +
                ", username='" + username + '\'' +
                ", useApiToken=" + useApiToken +
                ", connectionTimeout=" + connectionTimeout +
                ", readTimeout=" + readTimeout +
                ", maxRetries=" + maxRetries +
                '}';
    }
}
