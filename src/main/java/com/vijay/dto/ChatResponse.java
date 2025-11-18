package com.vijay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatResponse {
    
    @JsonProperty("response")
    private String response;
    
    @JsonProperty("provider")
    private String provider;
    
    @JsonProperty("toolsUsed")
    private String[] toolsUsed;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("formattedResponse")
    private String formattedResponse;
    
    public ChatResponse() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    public ChatResponse(String response, String provider) {
        this();
        this.response = response;
        this.provider = provider;
        this.formattedResponse = formatResponseForUI(response);
    }
    
    public ChatResponse(String response, String provider, String[] toolsUsed) {
        this();
        this.response = response;
        this.provider = provider;
        this.toolsUsed = toolsUsed;
        this.formattedResponse = formatResponseForUI(response);
    }
    
    private String formatResponseForUI(String rawResponse) {
        if (rawResponse == null) return "";
        
        // Convert newlines to HTML breaks for better UI display
        return rawResponse
                .replace("\n", "<br/>")
                .replace("- ", "• ")  // Convert dashes to bullet points
                .trim();
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public String[] getToolsUsed() {
        return toolsUsed;
    }
    
    public void setToolsUsed(String[] toolsUsed) {
        this.toolsUsed = toolsUsed;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getFormattedResponse() {
        return formattedResponse;
    }
    
    public void setFormattedResponse(String formattedResponse) {
        this.formattedResponse = formattedResponse;
    }
}
