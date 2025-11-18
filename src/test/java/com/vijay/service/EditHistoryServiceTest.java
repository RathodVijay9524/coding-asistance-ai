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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EditHistoryServiceTest {

    @Mock
    private EditHistoryRepository editHistoryRepository;

    @Mock
    private UserPatternRepository userPatternRepository;

    @InjectMocks
    private EditHistoryService editHistoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("trackEdit should save EditHistory with given properties")
    void trackEdit_shouldSaveEditHistoryWithCorrectFields() {
        // Arrange
        String userId = "user123";
        String filePath = "src/Main.java";
        String originalCode = "int a = 1;";
        String editedCode = "int a = 2;";
        String editType = "modify_variable";
        String suggestionSource = "AI";
        Boolean accepted = true;
        String description = "Changed initial value";

        EditHistory saved = EditHistory.builder()
                .id(1L)
                .userId(userId)
                .filePath(filePath)
                .originalCode(originalCode)
                .editedCode(editedCode)
                .editType(editType)
                .suggestionSource(suggestionSource)
                .accepted(accepted)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        when(editHistoryRepository.save(any(EditHistory.class))).thenReturn(saved);

        // Act
        EditHistory result = editHistoryService.trackEdit(
                userId,
                filePath,
                originalCode,
                editedCode,
                editType,
                suggestionSource,
                accepted,
                description
        );

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getFilePath()).isEqualTo(filePath);
        assertThat(result.getEditType()).isEqualTo(editType);
        assertThat(result.getSuggestionSource()).isEqualTo(suggestionSource);
        assertThat(result.getAccepted()).isTrue();
        verify(editHistoryRepository, times(1)).save(any(EditHistory.class));
    }

    @Test
    @DisplayName("getUserEditHistory should return paged results from repository")
    void getUserEditHistory_shouldReturnPagedResults() {
        // Arrange
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);

        EditHistory e1 = EditHistory.builder().id(1L).userId(userId).filePath("A.java").build();
        EditHistory e2 = EditHistory.builder().id(2L).userId(userId).filePath("B.java").build();
        Page<EditHistory> page = new PageImpl<>(List.of(e1, e2), pageable, 2);

        when(editHistoryRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        // Act
        Page<EditHistory> result = editHistoryService.getUserEditHistory(userId, 0, 10);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getFilePath()).isEqualTo("A.java");
        verify(editHistoryRepository, times(1)).findByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("getRecentEdits should limit results to given size")
    void getRecentEdits_shouldLimitResults() {
        // Arrange
        String userId = "user123";
        int limit = 3;
        List<EditHistory> all = List.of(
                EditHistory.builder().id(1L).userId(userId).build(),
                EditHistory.builder().id(2L).userId(userId).build(),
                EditHistory.builder().id(3L).userId(userId).build(),
                EditHistory.builder().id(4L).userId(userId).build()
        );

        when(editHistoryRepository.findRecentEdits(userId, limit)).thenReturn(all.subList(0, limit));

        // Act
        List<EditHistory> result = editHistoryService.getRecentEdits(userId, limit);

        // Assert
        assertThat(result).hasSize(limit);
        verify(editHistoryRepository, times(1)).findRecentEdits(userId, limit);
    }

    @Test
    @DisplayName("getUserStatistics should handle no edits gracefully")
    void getUserStatistics_shouldHandleNoEditsGracefully() {
        // Arrange
        String userId = "user-empty";

        when(editHistoryRepository.countByUserId(userId)).thenReturn(0L);
        when(editHistoryRepository.countByUserIdAndAccepted(userId, true)).thenReturn(0L);
        when(editHistoryRepository.getMostCommonEditTypes(userId)).thenReturn(List.of());

        Page<EditHistory> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0);
        when(editHistoryRepository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        var stats = editHistoryService.getUserStatistics(userId);

        // Assert
        assertThat(stats.get("userId")).isEqualTo(userId);
        assertThat(stats.get("totalEdits")).isEqualTo(0L);
        assertThat(stats.get("acceptedEdits")).isEqualTo(0L);
        assertThat(stats.get("acceptanceRate")).isEqualTo("0.00%");
    }
}
