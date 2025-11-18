package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsistencyCheckServiceTest {

    private ConsistencyCheckService service;

    @BeforeEach
    void setUp() {
        service = new ConsistencyCheckService();
    }

    @Test
    @DisplayName("checkConsistency should report HIGH issue for empty response")
    void checkConsistency_emptyResponse() {
        ConsistencyCheckService.ConsistencyReport report = service.checkConsistency("");

        assertThat(report.getIssueCount()).isEqualTo(1);
        assertThat(report.getHighestSeverity()).isEqualTo(ConsistencyCheckService.IssueSeverity.HIGH);
        assertThat(report.isConsistent()).isFalse();
    }

    @Test
    @DisplayName("checkConsistency should detect unmatched braces and parentheses in code")
    void checkConsistency_codeBraceMismatch() {
        String response = "public class Test {\n" +
                "  public void method() {\n" +
                "    if (true) {\n" +
                "      System.out.println(\"hi\";\n" + // unmatched parenthesis
                "  }\n";

        ConsistencyCheckService.ConsistencyReport report = service.checkConsistency(response);

        assertThat(report.getIssues())
                .extracting(i -> i.severity)
                .contains(ConsistencyCheckService.IssueSeverity.HIGH);
        assertThat(report.isConsistent()).isFalse();
    }

    @Test
    @DisplayName("validateRequiredFields should report missing fields")
    void validateRequiredFields_reportsMissing() {
        String response = "Name: John Doe. Project: Coding-Assistance.";
        List<String> required = List.of("name", "project", "deadline");

        ConsistencyCheckService.FieldValidationReport report = service.validateRequiredFields(response, required);

        assertThat(report.getMissingFieldCount()).isEqualTo(1);
        assertThat(report.getMissingFields()).containsExactly("deadline");
        assertThat(report.isValid()).isFalse();
    }

    @Test
    @DisplayName("validateRequiredFields should be valid when all fields present")
    void validateRequiredFields_allPresent() {
        String response = "Name: John Doe. Project: Coding-Assistance. Deadline: tomorrow.";
        List<String> required = List.of("name", "project", "deadline");

        ConsistencyCheckService.FieldValidationReport report = service.validateRequiredFields(response, required);

        assertThat(report.getMissingFieldCount()).isZero();
        assertThat(report.isValid()).isTrue();
    }

    @Test
    @DisplayName("validateCodeStructure should detect class without methods and methods without class")
    void validateCodeStructure_classAndMethodIssues() {
        String onlyClass = "public class OnlyClass { }";
        ConsistencyCheckService.CodeStructureReport classReport = service.validateCodeStructure(onlyClass);

        assertThat(classReport.getFoundClasses()).isNotEmpty();
        assertThat(classReport.getFoundMethods()).isEmpty();
        assertThat(classReport.getIssues())
                .anyMatch(issue -> issue.message.contains("Class found but no methods"));

        String onlyMethod = "public void doSomething() {}";
        ConsistencyCheckService.CodeStructureReport methodReport = service.validateCodeStructure(onlyMethod);

        assertThat(methodReport.getFoundMethods()).isEmpty();
        assertThat(methodReport.getFoundClasses()).isEmpty();
        assertThat(methodReport.getIssues()).isEmpty();
    }
}
