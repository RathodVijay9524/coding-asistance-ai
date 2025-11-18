package com.vijay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.editing.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IntelligentEditorController.class)
class IntelligentEditorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private CodeSelectionAnalyzer codeSelectionAnalyzer;
    @MockBean private EditSuggestionGenerator editSuggestionGenerator;
    @MockBean private LiveCodeEditor liveCodeEditor;
    @MockBean private InlineSuggestionEngine inlineSuggestionEngine;
    @MockBean private SmartCompletionEngine smartCompletionEngine;
    @MockBean private RefactoringAssistant refactoringAssistant;
    @MockBean private CodeTransformationEngine codeTransformationEngine;

    @Test
    @DisplayName("/api/editor/analyze should return analysis on success")
    void analyzeCode_success() throws Exception {
        CodeSelectionAnalyzer.SelectionAnalysis analysis = new CodeSelectionAnalyzer.SelectionAnalysis();
        analysis.setLineCount(3);
        when(codeSelectionAnalyzer.analyzeSelection(any())).thenReturn(analysis);

        IntelligentEditorController.CodeAnalysisRequest req = new IntelligentEditorController.CodeAnalysisRequest();
        req.setSelectedCode("code");

        mockMvc.perform(post("/api/editor/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("/api/editor/suggest-edits should return suggestions array")
    void suggestEdits_success() throws Exception {
        when(editSuggestionGenerator.generateSuggestions(any(), any()))
                .thenReturn(Collections.emptyList());

        IntelligentEditorController.EditSuggestionRequest req = new IntelligentEditorController.EditSuggestionRequest();
        req.setSelectedCode("code");
        req.setInstruction("refactor");

        mockMvc.perform(post("/api/editor/suggest-edits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.suggestions").isArray());
    }
}
