package com.vijay.service;

import com.vijay.model.SuggestionFeedback;
import com.vijay.repository.SuggestionFeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SuggestionFeedbackServiceTest {

    @Mock
    private SuggestionFeedbackRepository feedbackRepository;

    @InjectMocks
    private SuggestionFeedbackService suggestionFeedbackService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("recordFeedback should save SuggestionFeedback with all fields")
    void recordFeedback_shouldSaveSuggestionFeedbackWithAllFields() {
        // Arrange
        String userId = "user123";
        Long suggestionId = 10L;
        String suggestionType = "extract_method";
        String suggestionContent = "public void helper() {}";
        Integer rating = 5;
        String action = "accepted";
        String feedback = "Very helpful";
        String userModification = "Added logging";
        String reason = "Improves readability";
        Boolean helpful = true;
        Boolean relevant = true;
        Boolean accurate = true;
        String sentiment = "positive";

        SuggestionFeedback saved = SuggestionFeedback.builder()
                .id(1L)
                .userId(userId)
                .suggestionId(suggestionId)
                .suggestionType(suggestionType)
                .suggestionContent(suggestionContent)
                .rating(rating)
                .action(action)
                .feedback(feedback)
                .userModification(userModification)
                .reason(reason)
                .helpful(helpful)
                .relevant(relevant)
                .accurate(accurate)
                .sentiment(sentiment)
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.save(any(SuggestionFeedback.class))).thenReturn(saved);

        // Act
        SuggestionFeedback result = suggestionFeedbackService.recordFeedback(
                userId,
                suggestionId,
                suggestionType,
                suggestionContent,
                rating,
                action,
                feedback,
                userModification,
                reason,
                helpful,
                relevant,
                accurate,
                sentiment
        );

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSuggestionId()).isEqualTo(suggestionId);
        assertThat(result.getRating()).isEqualTo(rating);
        assertThat(result.getAction()).isEqualTo(action);
        assertThat(result.getHelpful()).isTrue();
        verify(feedbackRepository, times(1)).save(any(SuggestionFeedback.class));
    }

    @Test
    @DisplayName("getUserFeedback should return paged results for user")
    void getUserFeedback_shouldReturnPage() {
        // Arrange
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);

        SuggestionFeedback f1 = SuggestionFeedback.builder().id(1L).userId(userId).rating(5).build();
        SuggestionFeedback f2 = SuggestionFeedback.builder().id(2L).userId(userId).rating(4).build();
        Page<SuggestionFeedback> page = new PageImpl<>(List.of(f1, f2), pageable, 2);

        when(feedbackRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        // Act
        Page<SuggestionFeedback> result = suggestionFeedbackService.getUserFeedback(userId, 0, 10);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        verify(feedbackRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("getFeedbackByRating should filter by user and rating")
    void getFeedbackByRating_shouldFilterCorrectly() {
        // Arrange
        String userId = "user123";
        int rating = 5;

        List<SuggestionFeedback> list = List.of(
                SuggestionFeedback.builder().id(1L).userId(userId).rating(5).build(),
                SuggestionFeedback.builder().id(2L).userId(userId).rating(5).build()
        );

        when(feedbackRepository.findByUserIdAndRating(userId, rating)).thenReturn(list);

        // Act
        List<SuggestionFeedback> result = suggestionFeedbackService.getFeedbackByRating(userId, rating);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(f -> f.getRating() == 5);
        verify(feedbackRepository, times(1)).findByUserIdAndRating(userId, rating);
    }

    @Test
    @DisplayName("getHelpfulFeedback should only return helpful entries")
    void getHelpfulFeedback_shouldReturnOnlyHelpful() {
        // Arrange
        String userId = "user123";
        List<SuggestionFeedback> list = List.of(
                SuggestionFeedback.builder().id(1L).userId(userId).helpful(true).build(),
                SuggestionFeedback.builder().id(2L).userId(userId).helpful(true).build()
        );

        when(feedbackRepository.findByUserIdAndHelpfulTrue(userId)).thenReturn(list);

        // Act
        List<SuggestionFeedback> result = suggestionFeedbackService.getHelpfulFeedback(userId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(f -> Boolean.TRUE.equals(f.getHelpful()));
        verify(feedbackRepository, times(1)).findByUserIdAndHelpfulTrue(userId);
    }

    @Test
    @DisplayName("getUserFeedbackStatistics should handle empty feedback list")
    void getUserFeedbackStatistics_shouldHandleEmpty() {
        // Arrange
        String userId = "user-empty";
        when(feedbackRepository.findByUserId(userId)).thenReturn(List.of());

        // Act
        Map<String, Object> stats = suggestionFeedbackService.getUserFeedbackStatistics(userId);

        // Assert
        assertThat(stats.get("userId")).isEqualTo(userId);
        assertThat(stats.get("totalFeedback")).isEqualTo(0);
        assertThat(stats.get("averageRating")).isEqualTo(0.0);
        assertThat(stats.get("acceptanceRate")).isEqualTo("0%");
        assertThat(stats.get("rejectionRate")).isEqualTo("0%");
    }

    @Test
    @DisplayName("getUserFeedbackStatistics should compute averages and counts")
    void getUserFeedbackStatistics_shouldComputeValues() {
        // Arrange
        String userId = "user123";

        SuggestionFeedback f1 = SuggestionFeedback.builder()
                .userId(userId)
                .rating(5)
                .action("accepted")
                .helpful(true)
                .sentiment("positive")
                .suggestionType("extract_method")
                .build();

        SuggestionFeedback f2 = SuggestionFeedback.builder()
                .userId(userId)
                .rating(3)
                .action("rejected")
                .helpful(false)
                .sentiment("negative")
                .suggestionType("rename_variable")
                .build();

        when(feedbackRepository.findByUserId(userId)).thenReturn(List.of(f1, f2));

        // Act
        Map<String, Object> stats = suggestionFeedbackService.getUserFeedbackStatistics(userId);

        // Assert
        assertThat(stats.get("userId")).isEqualTo(userId);
        assertThat(stats.get("totalFeedback")).isEqualTo(2);
        assertThat(stats.get("averageRating")).isEqualTo("4.00");
        assertThat(stats.get("helpfulCount")).isEqualTo(1L);
        assertThat(stats.get("notHelpfulCount")).isEqualTo(1L);
        assertThat(stats.get("acceptedCount")).isEqualTo(1L);
        assertThat(stats.get("rejectedCount")).isEqualTo(1L);
        assertThat(stats.get("acceptanceRate")).isEqualTo("50.00%");
        assertThat(stats.get("rejectionRate")).isEqualTo("50.00%");
    }
}
