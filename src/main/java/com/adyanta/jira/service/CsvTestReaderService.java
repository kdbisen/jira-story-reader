package com.adyanta.jira.service;

import com.adyanta.jira.model.TestCase;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class for reading test cases from CSV files.
 * Supports flexible CSV structures with any column names.
 */
public class CsvTestReaderService {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvTestReaderService.class);
    
    /**
     * Read test cases from a CSV file
     * 
     * @param csvFilePath Path to the CSV file
     * @return List of TestCase objects
     * @throws IOException If file cannot be read
     * @throws CsvException If CSV parsing fails
     */
    public List<TestCase> readTestCases(String csvFilePath) throws IOException, CsvException {
        logger.info("Reading test cases from CSV file: {}", csvFilePath);
        
        Path path = Paths.get(csvFilePath);
        if (!Files.exists(path)) {
            throw new IOException("CSV file not found: " + csvFilePath);
        }
        
        List<TestCase> testCases = new ArrayList<>();
        
        try (Reader reader = new FileReader(csvFilePath);
             CSVReader csvReader = new CSVReader(reader)) {
            
            // Read all lines
            List<String[]> allLines = csvReader.readAll();
            
            if (allLines.isEmpty()) {
                logger.warn("CSV file is empty: {}", csvFilePath);
                return testCases;
            }
            
            // First line is header
            String[] headers = allLines.get(0);
            logger.info("Found {} columns in CSV: {}", headers.length, String.join(", ", headers));
            
            // Process data rows
            for (int i = 1; i < allLines.size(); i++) {
                String[] row = allLines.get(i);
                
                // Skip empty rows
                if (isEmptyRow(row)) {
                    continue;
                }
                
                // Create test case from row
                TestCase testCase = createTestCaseFromRow(headers, row);
                testCases.add(testCase);
            }
            
            logger.info("Successfully read {} test cases from CSV", testCases.size());
        }
        
        return testCases;
    }
    
    /**
     * Read test cases from a CSV file with custom separator
     */
    public List<TestCase> readTestCases(String csvFilePath, char separator) throws IOException, CsvException {
        logger.info("Reading test cases from CSV file: {} with separator: {}", csvFilePath, separator);
        
        Path path = Paths.get(csvFilePath);
        if (!Files.exists(path)) {
            throw new IOException("CSV file not found: " + csvFilePath);
        }
        
        List<TestCase> testCases = new ArrayList<>();
        
        try (Reader reader = new FileReader(csvFilePath);
             CSVReader csvReader = CSVReaderBuilder.create(reader)
                     .withSeparator(separator)
                     .build()) {
            
            // Read all lines
            List<String[]> allLines = csvReader.readAll();
            
            if (allLines.isEmpty()) {
                logger.warn("CSV file is empty: {}", csvFilePath);
                return testCases;
            }
            
            // First line is header
            String[] headers = allLines.get(0);
            logger.info("Found {} columns in CSV: {}", headers.length, String.join(", ", headers));
            
            // Process data rows
            for (int i = 1; i < allLines.size(); i++) {
                String[] row = allLines.get(i);
                
                // Skip empty rows
                if (isEmptyRow(row)) {
                    continue;
                }
                
                // Create test case from row
                TestCase testCase = createTestCaseFromRow(headers, row);
                testCases.add(testCase);
            }
            
            logger.info("Successfully read {} test cases from CSV", testCases.size());
        }
        
        return testCases;
    }
    
    /**
     * Create a TestCase object from a CSV row
     */
    private TestCase createTestCaseFromRow(String[] headers, String[] row) {
        Map<String, String> fields = new HashMap<>();
        
        // Map headers to row values
        for (int i = 0; i < headers.length && i < row.length; i++) {
            String header = headers[i].trim();
            String value = row[i] != null ? row[i].trim() : "";
            
            // Only add non-empty values
            if (!header.isEmpty() && !value.isEmpty()) {
                fields.put(header, value);
            }
        }
        
        return new TestCase(fields);
    }
    
    /**
     * Check if a row is empty
     */
    private boolean isEmptyRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }
        
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validate CSV file structure
     */
    public void validateCsvFile(String csvFilePath) throws IOException, CsvException {
        logger.info("Validating CSV file: {}", csvFilePath);
        
        Path path = Paths.get(csvFilePath);
        if (!Files.exists(path)) {
            throw new IOException("CSV file not found: " + csvFilePath);
        }
        
        try (Reader reader = new FileReader(csvFilePath);
             CSVReader csvReader = new CSVReader(reader)) {
            
            List<String[]> allLines = csvReader.readAll();
            
            if (allLines.isEmpty()) {
                throw new IOException("CSV file is empty: " + csvFilePath);
            }
            
            String[] headers = allLines.get(0);
            if (headers.length == 0) {
                throw new IOException("CSV file has no headers: " + csvFilePath);
            }
            
            // Check for required columns (at minimum, summary or title)
            boolean hasSummary = false;
            for (String header : headers) {
                if (header != null && (header.trim().equalsIgnoreCase("summary") || 
                    header.trim().equalsIgnoreCase("title"))) {
                    hasSummary = true;
                    break;
                }
            }
            
            if (!hasSummary) {
                logger.warn("CSV file does not contain 'summary' or 'title' column. Issues may not be created properly.");
            }
            
            logger.info("CSV file validation passed. Found {} columns and {} data rows", 
                    headers.length, allLines.size() - 1);
        }
    }
}

