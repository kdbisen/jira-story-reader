#!/bin/bash

# Jira Story Reader - Run Script
# This script helps you run the Jira Story Reader application

echo "Jira Story Reader - Starting Application"
echo "========================================"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java version: $JAVA_VERSION"

# Check if configuration file exists
if [ ! -f "jira-config.properties" ]; then
    echo "Warning: jira-config.properties not found in current directory"
    echo "The application will try to use environment variables instead"
    echo ""
fi

# Compile the project
echo "Compiling the project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "Error: Failed to compile the project"
    exit 1
fi

echo "Compilation successful!"
echo ""

# Run the application
echo "Starting the application..."
echo "=========================="
mvn exec:java -Dexec.mainClass="com.adyanta.jira.JiraStoryReaderApplication"
