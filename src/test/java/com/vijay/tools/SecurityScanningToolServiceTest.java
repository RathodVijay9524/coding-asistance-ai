package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityScanningToolServiceTest {

    private ObjectMapper objectMapper;
    private SecurityScanningToolService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new SecurityScanningToolService(objectMapper);
    }

    @Test
    @DisplayName("scanSecurity should include sections and securityScore for simple code")
    void scanSecurity_basic() throws Exception {
        String code = "public void run(){ String password = \"123\"; }";

        String json = service.scanSecurity(code, "Java", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("injectionVulnerabilities")).isTrue();
        assertThat(root.has("hardcodedSecrets")).isTrue();
        assertThat(root.has("authenticationIssues")).isTrue();
        assertThat(root.has("cryptographyIssues")).isTrue();
        assertThat(root.has("inputValidationIssues")).isTrue();
        assertThat(root.has("aiSecurityAnalysis")).isTrue();
        assertThat(root.has("securityScore")).isTrue();
        assertThat(root.has("summary")).isTrue();
    }
}
