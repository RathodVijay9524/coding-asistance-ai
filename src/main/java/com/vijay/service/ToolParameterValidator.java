package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 🔧 Tool Parameter Validator
 * 
 * Validates and fixes tool parameters before execution:
 * - Detects null parameters
 * - Extracts missing values from context
 * - Validates parameter types
 * - Prevents tool execution failures
 */
@Component
public class ToolParameterValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ToolParameterValidator.class);
    
    /**
     * Validate and fix tool parameters
     */
    public Map<String, Object> validateAndFixParameters(String toolName, Map<String, Object> parameters, String queryContext) {
        logger.debug("🔧 Validating parameters for tool: {}", toolName);
        
        Map<String, Object> fixedParams = new HashMap<>(parameters);
        
        // Weather tool validation
        if (toolName.contains("Weather") || toolName.contains("weather")) {
            fixedParams = validateWeatherToolParameters(fixedParams, queryContext);
        }
        
        // DateTime tool validation
        if (toolName.contains("DateTime") || toolName.contains("datetime") || toolName.contains("Time")) {
            fixedParams = validateDateTimeToolParameters(fixedParams, queryContext);
        }
        
        // Calendar tool validation
        if (toolName.contains("Calendar") || toolName.contains("calendar")) {
            fixedParams = validateCalendarToolParameters(fixedParams, queryContext);
        }
        
        // Generic validation
        fixedParams = validateGenericParameters(fixedParams);
        
        logger.debug("✅ Parameters validated for {}: {}", toolName, fixedParams);
        return fixedParams;
    }
    
    /**
     * Validate weather tool parameters
     */
    private Map<String, Object> validateWeatherToolParameters(Map<String, Object> params, String queryContext) {
        // Check if city is null or empty
        Object city = params.get("city");
        if (city == null || city.toString().isEmpty()) {
            logger.warn("⚠️ Weather tool: city parameter is null - extracting from context");
            String extractedCity = extractCityFromContext(queryContext);
            if (extractedCity != null && !extractedCity.isEmpty()) {
                params.put("city", extractedCity);
                logger.info("✅ Weather tool: city extracted from context: {}", extractedCity);
            } else {
                // Default to a common city if extraction fails
                params.put("city", "New York");
                logger.warn("⚠️ Weather tool: using default city: New York");
            }
        }
        
        return params;
    }
    
    /**
     * Validate DateTime tool parameters
     */
    private Map<String, Object> validateDateTimeToolParameters(Map<String, Object> params, String queryContext) {
        // DateTime tool usually doesn't need parameters, but validate if present
        if (params.isEmpty()) {
            logger.debug("✅ DateTime tool: no parameters needed");
        }
        return params;
    }
    
    /**
     * Validate Calendar tool parameters
     */
    private Map<String, Object> validateCalendarToolParameters(Map<String, Object> params, String queryContext) {
        // Check for required fields
        if (!params.containsKey("title") || params.get("title") == null) {
            logger.warn("⚠️ Calendar tool: title parameter is missing");
            String extractedTitle = extractEventTitleFromContext(queryContext);
            if (extractedTitle != null) {
                params.put("title", extractedTitle);
                logger.info("✅ Calendar tool: title extracted: {}", extractedTitle);
            }
        }
        
        if (!params.containsKey("date") || params.get("date") == null) {
            logger.warn("⚠️ Calendar tool: date parameter is missing");
            String extractedDate = extractDateFromContext(queryContext);
            if (extractedDate != null) {
                params.put("date", extractedDate);
                logger.info("✅ Calendar tool: date extracted: {}", extractedDate);
            }
        }
        
        return params;
    }
    
    /**
     * Generic parameter validation
     */
    private Map<String, Object> validateGenericParameters(Map<String, Object> params) {
        // Remove null values
        params.entrySet().removeIf(e -> e.getValue() == null);
        
        // Validate string parameters
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof String) {
                String strValue = (String) entry.getValue();
                if (strValue.isEmpty()) {
                    logger.warn("⚠️ Parameter '{}' is empty string", entry.getKey());
                }
            }
        }
        
        return params;
    }
    
    /**
     * Extract city from query context
     */
    private String extractCityFromContext(String queryContext) {
        if (queryContext == null || queryContext.isEmpty()) {
            return null;
        }
        
        String lowerContext = queryContext.toLowerCase();
        
        // Common city patterns
        String[] cities = {
            "new york", "london", "paris", "tokyo", "sydney", "toronto",
            "berlin", "moscow", "dubai", "singapore", "hong kong",
            "los angeles", "chicago", "san francisco", "seattle", "boston"
        };
        
        for (String city : cities) {
            if (lowerContext.contains(city)) {
                logger.info("🌍 Extracted city from context: {}", city);
                return city;
            }
        }
        
        // Try to extract city after "in" or "for"
        if (lowerContext.contains(" in ")) {
            String[] parts = lowerContext.split(" in ");
            if (parts.length > 1) {
                String city = parts[1].split(" ")[0].trim();
                if (!city.isEmpty()) {
                    logger.info("🌍 Extracted city after 'in': {}", city);
                    return city;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Extract event title from query context
     */
    private String extractEventTitleFromContext(String queryContext) {
        if (queryContext == null || queryContext.isEmpty()) {
            return null;
        }
        
        String lowerContext = queryContext.toLowerCase();
        
        // Look for patterns like "create event", "schedule", "add event"
        if (lowerContext.contains("meeting")) {
            return "Meeting";
        }
        if (lowerContext.contains("appointment")) {
            return "Appointment";
        }
        if (lowerContext.contains("reminder")) {
            return "Reminder";
        }
        
        // Extract after "event" or "schedule"
        if (lowerContext.contains("event ")) {
            String[] parts = lowerContext.split("event ");
            if (parts.length > 1) {
                String title = parts[1].split("[,.]")[0].trim();
                if (!title.isEmpty() && title.length() < 50) {
                    logger.info("📅 Extracted event title: {}", title);
                    return title;
                }
            }
        }
        
        return "Event";
    }
    
    /**
     * Extract date from query context
     */
    private String extractDateFromContext(String queryContext) {
        if (queryContext == null || queryContext.isEmpty()) {
            return null;
        }
        
        String lowerContext = queryContext.toLowerCase();
        
        // Common date patterns
        if (lowerContext.contains("today")) {
            return java.time.LocalDate.now().toString();
        }
        if (lowerContext.contains("tomorrow")) {
            return java.time.LocalDate.now().plusDays(1).toString();
        }
        if (lowerContext.contains("next week")) {
            return java.time.LocalDate.now().plusWeeks(1).toString();
        }
        if (lowerContext.contains("next month")) {
            return java.time.LocalDate.now().plusMonths(1).toString();
        }
        
        return null;
    }
    
    /**
     * Check if parameter is valid
     */
    public boolean isParameterValid(String paramName, Object paramValue) {
        if (paramValue == null) {
            logger.warn("⚠️ Parameter '{}' is null", paramName);
            return false;
        }
        
        if (paramValue instanceof String) {
            String strValue = (String) paramValue;
            if (strValue.isEmpty()) {
                logger.warn("⚠️ Parameter '{}' is empty string", paramName);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get all invalid parameters
     */
    public List<String> getInvalidParameters(Map<String, Object> parameters) {
        List<String> invalid = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (!isParameterValid(entry.getKey(), entry.getValue())) {
                invalid.add(entry.getKey());
            }
        }
        
        return invalid;
    }
}
