package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolParameterValidatorTest {

    private ToolParameterValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ToolParameterValidator();
    }

    @Test
    @DisplayName("validateAndFixParameters should fill missing weather city from context and default when missing")
    void validateAndFixParameters_weatherCityExtractionAndDefault() {
        Map<String, Object> params = new HashMap<>();
        String context = "What is the weather in London today?";

        Map<String, Object> fixed = validator.validateAndFixParameters("getWeather", params, context);

        assertThat(fixed.get("city")).isEqualTo("london");

        Map<String, Object> paramsNoContext = new HashMap<>();
        Map<String, Object> fixedNoContext = validator.validateAndFixParameters("weatherTool", paramsNoContext, "");

        assertThat(fixedNoContext.get("city")).isEqualTo("New York");
    }

    @Test
    @DisplayName("validateAndFixParameters should extract calendar title and date from context")
    void validateAndFixParameters_calendarExtraction() {
        Map<String, Object> params = new HashMap<>();
        String context = "Create a meeting event tomorrow about project review.";

        Map<String, Object> fixed = validator.validateAndFixParameters("createCalendarEvent", params, context);

        assertThat(fixed.get("title")).isEqualTo("Meeting");
        assertThat(fixed.get("date")).isEqualTo(LocalDate.now().plusDays(1).toString());
    }

    @Test
    @DisplayName("validateAndFixParameters should remove nulls and warn on empty strings via generic validation")
    void validateAndFixParameters_genericValidation() {
        Map<String, Object> params = new HashMap<>();
        params.put("a", null);
        params.put("b", "");
        params.put("c", "value");

        Map<String, Object> fixed = validator.validateAndFixParameters("someTool", params, "");

        assertThat(fixed).doesNotContainKey("a");
        assertThat(fixed.get("b")).isEqualTo("");
        assertThat(fixed.get("c")).isEqualTo("value");
    }

    @Test
    @DisplayName("isParameterValid should detect null and empty string values")
    void isParameterValid_basic() {
        assertThat(validator.isParameterValid("p1", null)).isFalse();
        assertThat(validator.isParameterValid("p2", "")).isFalse();
        assertThat(validator.isParameterValid("p3", "ok")).isTrue();
        assertThat(validator.isParameterValid("p4", 42)).isTrue();
    }

    @Test
    @DisplayName("getInvalidParameters should return keys of invalid params")
    void getInvalidParameters_basic() {
        Map<String, Object> params = new HashMap<>();
        params.put("a", null);
        params.put("b", "");
        params.put("c", "value");

        List<String> invalid = validator.getInvalidParameters(params);

        assertThat(invalid).contains("a", "b");
        assertThat(invalid).doesNotContain("c");
    }
}
