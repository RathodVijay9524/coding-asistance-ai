package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HallucinationDetectorTest {

    private HallucinationDetector detector;

    @BeforeEach
    void setUp() {
        detector = new HallucinationDetector();
    }

    @Test
    @DisplayName("detectHallucinations should flag impossible claims with HIGH severity and high score")
    void detectHallucinations_impossibleClaim() {
        String response = "This AI can read minds and predict the future with 100% accuracy.";

        HallucinationDetector.HallucinationReport report = detector.detectHallucinations(response);

        assertThat(report.getHallucinationCount()).isGreaterThan(0);
        assertThat(report.hasHighSeverityHallucinations()).isTrue();
        assertThat(report.getHallucinationScore()).isGreaterThan(0.4);
        assertThat(report.isTrusted()).isFalse();
    }

    @Test
    @DisplayName("detectHallucinations should detect overconfident statements as LOW severity and keep response trusted")
    void detectHallucinations_overconfidentButTrusted() {
        String response = "This approach definitely works and is guaranteed in most cases.";

        HallucinationDetector.HallucinationReport report = detector.detectHallucinations(response);

        assertThat(report.getHallucinationCount()).isGreaterThan(0);
        assertThat(report.getHighestSeverity()).isEqualTo(HallucinationDetector.HallucinationSeverity.LOW);
        assertThat(report.getHallucinationScore()).isLessThan(0.3);
        assertThat(report.isTrusted()).isTrue();
    }

    @Test
    @DisplayName("addKnownFact and validateClaim should mark aligned claims as valid")
    void addKnownFact_and_validateClaim() {
        detector.addKnownFact("Cascade is an AI coding assistant");

        boolean valid = detector.validateClaim("In our system, Cascade is an AI coding assistant that helps with code.");

        assertThat(valid).isTrue();
    }
}
