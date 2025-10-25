# Jira Story Reader

A Java application that reads Jira story acceptance criteria and descriptions using the Jira REST API. This tool provides a comprehensive way to fetch, display, and analyze Jira stories programmatically.

## Features

- **Single Story Fetching**: Get detailed information for a specific story by key
- **Multiple Story Fetching**: Fetch multiple stories at once
- **JQL Search**: Search stories using Jira Query Language (JQL)
- **Project-based Search**: Get all stories from a specific project
- **Assignee-based Search**: Find stories assigned to specific users
- **Sprint-based Search**: Get stories from specific sprints
- **Status-based Search**: Find stories with specific statuses
- **Acceptance Criteria Extraction**: Automatically extract acceptance criteria from various custom fields
- **Interactive Menu**: User-friendly command-line interface
- **Flexible Configuration**: Support for both password and API token authentication

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Access to a Jira instance (Cloud or Server)
- Valid Jira credentials (username/password or username/API token)

## Installation

1. **Clone or download the project**:
   ```bash
   git clone <repository-url>
   cd jira-story-reader
   ```

2. **Build the project**:
   ```bash
   mvn clean compile
   ```

3. **Run tests** (optional):
   ```bash
   mvn test
   ```

## Configuration

### Method 1: Configuration File (Recommended)

1. Copy the example configuration file:
   ```bash
   cp src/main/resources/jira-config.properties jira-config.properties
   ```

2. Edit `jira-config.properties` with your Jira details:
   ```properties
   # Jira Server URL
   jira.url=https://your-domain.atlassian.net
   
   # Authentication Method 1: Username and Password
   jira.username=your-email@example.com
   jira.password=your-password
   
   # Authentication Method 2: Username and API Token (recommended)
   # jira.username=your-email@example.com
   # jira.api.token=your-api-token
   # jira.use.api.token=true
   
   # Connection Settings
   jira.connection.timeout=30000
   jira.read.timeout=60000
   jira.max.retries=3
   ```

### Method 2: Environment Variables

Set the following environment variables:

```bash
export JIRA_URL="https://your-domain.atlassian.net"
export JIRA_USERNAME="your-email@example.com"
export JIRA_PASSWORD="your-password"
# OR for API token:
export JIRA_API_TOKEN="your-api-token"
export JIRA_USE_API_TOKEN="true"
```

### API Token Setup (Recommended)

For better security, use API tokens instead of passwords:

1. Go to [Atlassian Account Settings](https://id.atlassian.com/manage-profile/security/api-tokens)
2. Click "Create API token"
3. Give it a label (e.g., "Jira Story Reader")
4. Copy the generated token
5. Use the token in your configuration

## Usage

### Running the Application

```bash
mvn exec:java
```

Or compile and run manually:
```bash
mvn clean package
java -cp target/classes:target/dependency/* com.adyanta.jira.JiraStoryReaderApplication
```

### Interactive Menu

The application provides an interactive menu with the following options:

1. **Fetch Single Story by Key**: Enter a story key (e.g., PROJ-123)
2. **Fetch Multiple Stories by Keys**: Enter comma-separated story keys
3. **Search Stories by JQL**: Enter a JQL query
4. **Fetch Stories by Project**: Enter a project key
5. **Fetch Stories by Assignee**: Enter an assignee email/username
6. **Fetch Stories by Sprint**: Enter a sprint name
7. **Fetch Stories by Status**: Enter a status name
8. **Run Example Scenarios**: See example usage patterns
9. **Exit**: Close the application

### Programmatic Usage

You can also use the service programmatically in your own code:

```java
// Initialize configuration and service
JiraConfig config = new JiraConfig("https://your-domain.atlassian.net", 
                                  "your-email@example.com", 
                                  "your-api-token", 
                                  true);
JiraStoryReaderService service = new JiraStoryReaderService(config);

// Fetch a single story
JiraStory story = service.getStoryByKey("PROJ-123");

// Get specific information
String summary = service.getSummary(story);
String description = service.getDescription(story);
String acceptanceCriteria = service.getAcceptanceCriteria(story);

// Search stories
List<JiraStory> stories = service.searchStories("project = PROJ AND issuetype = Story");

// Print story details
service.printStoryDetails(story);
```

## JQL Examples

Here are some useful JQL queries you can use:

```jql
# All stories in a project
project = PROJ AND issuetype = Story

# Stories assigned to a user
assignee = user@example.com AND issuetype = Story

# Stories in a specific sprint
Sprint = "Sprint 1" AND issuetype = Story

# Stories with specific status
status = "To Do" AND issuetype = Story

# Stories created in the last 7 days
created >= -7d AND issuetype = Story

# Stories with story points
issuetype = Story AND "Story Points" is not EMPTY

# Stories with acceptance criteria
issuetype = Story AND "Acceptance Criteria" is not EMPTY
```

## Custom Fields

The application automatically looks for acceptance criteria in common custom field IDs:
- `customfield_10014`
- `customfield_10015`
- `customfield_10016`
- `customfield_10017`
- `customfield_10018`

If your Jira instance uses different field IDs, you can modify the `JiraStory.java` model to include your specific field IDs.

## Error Handling

The application includes comprehensive error handling for common scenarios:

- **Authentication Errors**: Invalid credentials or API tokens
- **Permission Errors**: Insufficient permissions to access stories
- **Network Errors**: Connection timeouts or network issues
- **Not Found Errors**: Invalid story keys or non-existent resources
- **Rate Limiting**: Automatic retry with exponential backoff

## Logging

The application uses SLF4J with Logback for logging. Log levels can be configured in `logback.xml`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify your username and password/API token
   - Check if your account has the necessary permissions
   - Ensure the Jira URL is correct

2. **Permission Denied**
   - Make sure your account has access to the projects/stories you're trying to read
   - Check if the stories are in projects you have access to

3. **Story Not Found**
   - Verify the story key exists
   - Check if the story is in a project you have access to

4. **Connection Timeout**
   - Increase the timeout values in configuration
   - Check your network connection
   - Verify the Jira server is accessible

### Debug Mode

Enable debug logging by setting the log level to DEBUG in your configuration:

```xml
<root level="DEBUG">
    <appender-ref ref="STDOUT" />
</root>
```

## API Rate Limits

Jira Cloud has rate limits:
- **Atlassian Cloud**: 300 requests per minute per user
- **Jira Server**: Varies by configuration

The application includes retry logic with exponential backoff to handle rate limiting gracefully.

## Security Considerations

- **Never commit credentials**: Keep your `jira-config.properties` file out of version control
- **Use API tokens**: Prefer API tokens over passwords for better security
- **Environment variables**: Use environment variables for production deployments
- **Network security**: Ensure your network connection to Jira is secure

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review the Jira REST API documentation
3. Create an issue in the project repository

## Changelog

### Version 1.0.0
- Initial release
- Support for fetching single and multiple stories
- JQL search functionality
- Interactive command-line interface
- Support for both password and API token authentication
- Comprehensive error handling and logging
