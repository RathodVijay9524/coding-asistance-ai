package com.vijay.service;

import com.vijay.model.EditHistory;
import com.vijay.model.UserPattern;
import com.vijay.repository.EditHistoryRepository;
import com.vijay.repository.UserPatternRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class InlineSuggestionEngineServiceTest {

    @Mock
    private EditHistoryRepository editHistoryRepository;

    @Mock
    private UserPatternRepository userPatternRepository;

    @InjectMocks
    private InlineSuggestionEngineService inlineSuggestionEngineService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("generateInlineSuggestions should honor user patterns and produce suggestions")
    void generateInlineSuggestions_shouldProduceSuggestionsBasedOnPatterns() {
        // Arrange
        String userId = "user123";
        String code = "public void longMethod() {\n" +
                "  int a = 1;\n" +
                "  int b = 2;\n" +
                "  int c = 3;\n" +
                "  int d = 4;\n" +
                "  int e = 5;\n" +
                "  int f = 6;\n" +
                "  int g = 7;\n" +
                "  int h = 8;\n" +
                "  int i = 9;\n" +
                "  int j = 10;\n" +
                "}\n" +
                "int x = 0; x = 1;";

        // User likes extract_method and rename_variable
        UserPattern extractPattern = UserPattern.builder()
                .userId(userId)
                .patternType("extract_method")
                .acceptanceRate(0.9)
                .build();
        UserPattern renamePattern = UserPattern.builder()
                .userId(userId)
                .patternType("rename_variable")
                .acceptanceRate(0.8)
                .build();

        when(userPatternRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of(extractPattern, renamePattern));

        // Act
        List<InlineSuggestionEngineService.InlineSuggestion> suggestions =
                inlineSuggestionEngineService.generateInlineSuggestions(
                        userId,
                        code,
                        "java",
                        0,
                        "method"
                );

        // Assert
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions)
                .extracting(InlineSuggestionEngineService.InlineSuggestion::getType)
                .contains("extract_method", "rename_variable");
    }

    @Test
    @DisplayName("getContextAwareSuggestions should use recent edits to suggest extract_method")
    void getContextAwareSuggestions_shouldUseRecentEdits() {
        // Arrange
        String userId = "user123";

        EditHistory e1 = EditHistory.builder()
                .userId(userId)
                .editType("extract_method")
                .createdAt(LocalDateTime.now())
                .build();

        // More than 3 extract_method edits
        when(editHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(e1, e1, e1, e1));

        // Act
        List<InlineSuggestionEngineService.InlineSuggestion> suggestions =
                inlineSuggestionEngineService.getContextAwareSuggestions(
                        userId,
                        "public void m() {}",
                        "m",
                        "TestClass"
                );

        // Assert
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions)
                .extracting(InlineSuggestionEngineService.InlineSuggestion::getType)
                .contains("extract_method");
        verify(editHistoryRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("getPersonalizedSuggestions should use effective patterns")
    void getPersonalizedSuggestions_shouldUseEffectivePatterns() {
        // Arrange
        String userId = "user123";

        UserPattern pattern = UserPattern.builder()
                .userId(userId)
                .patternType("extract_method")
                .acceptanceRate(0.8)
                .description("You often extract long methods")
                .build();

        when(userPatternRepository.findByUserIdAndAcceptanceRateGreaterThan(userId, 0.75))
                .thenReturn(List.of(pattern));

        // Act
        List<InlineSuggestionEngineService.InlineSuggestion> suggestions =
                inlineSuggestionEngineService.getPersonalizedSuggestions(
                        userId,
                        "public void m() {}"
                );

        // Assert
        assertThat(suggestions).hasSize(1);
        InlineSuggestionEngineService.InlineSuggestion s = suggestions.get(0);
        assertThat(s.getType()).isEqualTo("extract_method");
        assertThat(s.getSuggestion()).isEqualTo(pattern.getDescription());
        verify(userPatternRepository, times(1))
                .findByUserIdAndAcceptanceRateGreaterThan(userId, 0.75);
    }

    @Test
    @DisplayName("getQuickFixSuggestions should suggest null and bounds checks for common errors")
    void getQuickFixSuggestions_shouldReturnQuickFixSuggestions() {
        // Arrange
        String userId = "user123";
        String code = "int[] arr = new int[10]; int x = arr[20];";
        String errorMessage = "NullPointerException and ArrayIndexOutOfBoundsException";

        // Act
        List<InlineSuggestionEngineService.InlineSuggestion> suggestions =
                inlineSuggestionEngineService.getQuickFixSuggestions(userId, code, errorMessage);

        // Assert
        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions)
                .extracting(InlineSuggestionEngineService.InlineSuggestion::getType)
                .contains("null_check", "bounds_check");
    }

    @Test
    @DisplayName("generateInlineSuggestions should return empty list when no active patterns are found")
    void generateInlineSuggestions_noPatterns_returnsEmptyList() {
        String userId = "user123";
        String code = "public void m() { int x = 1; }";

        when(userPatternRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of());

        List<InlineSuggestionEngineService.InlineSuggestion> suggestions =
                inlineSuggestionEngineService.generateInlineSuggestions(
                        userId,
                        code,
                        "java",
                        0,
                        "method"
                );

        assertThat(suggestions).isEmpty();
    }

    @Test
    @DisplayName("generateInlineSuggestions should include all suggestion types and rank by priority and confidence")
    void generateInlineSuggestions_allTypes_ranksSuggestions() {
        String userId = "user123";
        String code = """
                public class Sample {
                    public static Sample getInstance() { return new Sample(); }
                    public void longMethod() {
                        int a = 1;
                        int b = 2;
                        int c = 3;
                        int d = 4;
                        int e = 5;
                        int f = 6;
                        int g = 7;
                        int h = 8;
                        int i = 9;
                        int j = 10;
                        if (a > 0) {}
                        if (b > 0) {}
                        if (c > 0) {}
                    }
                    public void loop() {
                        for (int i = 0; i < 10; i++) {
                        }
                    }
                }
                a = 1;
                b = 2;
                """;

        UserPattern extractPattern = UserPattern.builder()
                .userId(userId)
                .patternType("extract_method")
                .acceptanceRate(0.9)
                .build();
        UserPattern renamePattern = UserPattern.builder()
                .userId(userId)
                .patternType("rename_variable")
                .acceptanceRate(0.9)
                .build();
        UserPattern simplifyPattern = UserPattern.builder()
                .userId(userId)
                .patternType("simplify_logic")
                .acceptanceRate(0.9)
                .build();
        UserPattern commentPattern = UserPattern.builder()
                .userId(userId)
                .patternType("add_comments")
                .acceptanceRate(0.9)
                .build();

        when(userPatternRepository.findByUserIdAndActiveTrue(userId))
                .thenReturn(List.of(extractPattern, renamePattern, simplifyPattern, commentPattern));

        List<InlineSuggestionEngineService.InlineSuggestion> suggestions =
                inlineSuggestionEngineService.generateInlineSuggestions(
                        userId,
                        code,
                        "java",
                        0,
                        "method"
                );

        assertThat(suggestions).isNotEmpty();
        // Expect all types: extract_method, rename_variable, simplify_logic, add_comments, apply_pattern
        assertThat(suggestions)
                .extracting(InlineSuggestionEngineService.InlineSuggestion::getType)
                .contains("extract_method", "rename_variable", "simplify_logic", "add_comments", "apply_pattern");

        // Verify ranking: priority asc, then confidence desc
        List<String> orderedTypes = suggestions.stream()
                .map(InlineSuggestionEngineService.InlineSuggestion::getType)
                .toList();

        assertThat(orderedTypes.get(0)).isEqualTo("extract_method"); // priority 1, highest
        assertThat(orderedTypes).containsSubsequence("extract_method", "rename_variable", "simplify_logic");
    }

    @Test
    @DisplayName("getContextAwareSuggestions should return empty list when there are no recent edits")
    void getContextAwareSuggestions_noRecentEdits_returnsEmptyList() {
        String userId = "user123";

        when(editHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<InlineSuggestionEngineService.InlineSuggestion> suggestions =
                inlineSuggestionEngineService.getContextAwareSuggestions(
                        userId,
                        "public void m() {}",
                        "m",
                        "TestClass"
                );

        assertThat(suggestions).isEmpty();
    }

    @Test
    @DisplayName("getSuggestionById and getSuggestionHistory should return default values")
    void getSuggestionById_andHistory_defaults() {
        InlineSuggestionEngineService.InlineSuggestion suggestion =
                inlineSuggestionEngineService.getSuggestionById("any-id");
        assertThat(suggestion).isNull();

        List<InlineSuggestionEngineService.InlineSuggestion> history =
                inlineSuggestionEngineService.getSuggestionHistory("user123", 10);
        assertThat(history).isEmpty();
    }
}
