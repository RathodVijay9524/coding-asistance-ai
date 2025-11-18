package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BrainFinderServiceTest {

    private VectorStore brainVectorStore;
    private BrainFinderService brainFinderService;

    @BeforeEach
    void setUp() {
        brainVectorStore = mock(VectorStore.class);
        brainFinderService = new BrainFinderService(brainVectorStore);
    }

    @Test
    @DisplayName("findBrainsFor should include core brains and add specialist brains without duplicates, in execution order")
    void findBrainsFor_includesCoreAndSpecialistsWithoutDuplicates() {
        Document specialist = new Document("Advanced capabilities", Map.of("brainName", "advancedCapabilitiesAdvisor"));
        Document duplicateCore = new Document("Duplicate core", Map.of("brainName", "conductorAdvisor"));

        when(brainVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(specialist, duplicateCore));

        List<String> brains = brainFinderService.findBrainsFor("what is 10 + 20");

        assertThat(brains).contains("conductorAdvisor", "toolCallAdvisor", "selfRefineV3Advisor", "personalityAdvisor");
        assertThat(brains).contains("advancedCapabilitiesAdvisor");
        assertThat(brains).doesNotHaveDuplicates();
        assertThat(brains).hasSize(5);

        int idxConductor = brains.indexOf("conductorAdvisor");
        int idxToolCall = brains.indexOf("toolCallAdvisor");
        int idxAdvanced = brains.indexOf("advancedCapabilitiesAdvisor");
        int idxPersonality = brains.indexOf("personalityAdvisor");
        int idxSelfRefine = brains.indexOf("selfRefineV3Advisor");

        assertThat(idxConductor).isLessThan(idxToolCall);
        assertThat(idxToolCall).isLessThan(idxAdvanced);
        assertThat(idxAdvanced).isLessThan(idxPersonality);
        assertThat(idxPersonality).isLessThan(idxSelfRefine);

        verify(brainVectorStore, times(1)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    @DisplayName("findBrainsFor should fall back to core brains only when VectorStore throws error")
    void findBrainsFor_onErrorReturnsCoreBrainsOnly() {
        when(brainVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Vector store failure"));

        List<String> brains = brainFinderService.findBrainsFor("any query");

        assertThat(brains).containsExactly(
                "conductorAdvisor",
                "toolCallAdvisor",
                "selfRefineV3Advisor",
                "personalityAdvisor"
        );
    }

    @Test
    @DisplayName("getAllBrains should return brain names from VectorStore metadata")
    void getAllBrains_returnsNamesFromVectorStore() {
        Document doc1 = new Document("Brain 1", Map.of("brainName", "conductorAdvisor"));
        Document doc2 = new Document("Brain 2", Map.of("brainName", "advancedCapabilitiesAdvisor"));

        when(brainVectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1, doc2));

        List<String> brains = brainFinderService.getAllBrains();

        assertThat(brains).containsExactly("conductorAdvisor", "advancedCapabilitiesAdvisor");
        verify(brainVectorStore, times(1)).similaritySearch(any(SearchRequest.class));
    }
}
