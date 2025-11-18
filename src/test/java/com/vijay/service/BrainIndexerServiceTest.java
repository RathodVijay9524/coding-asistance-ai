package com.vijay.service;

import com.vijay.manager.IAgentBrain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class BrainIndexerServiceTest {

    private VectorStore brainVectorStore;
    private IAgentBrain brain1;
    private IAgentBrain brain2;
    private BrainIndexerService service;

    @BeforeEach
    void setUp() {
        brainVectorStore = mock(VectorStore.class);
        brain1 = mock(IAgentBrain.class);
        brain2 = mock(IAgentBrain.class);

        when(brain1.getBrainName()).thenReturn("brainOne");
        when(brain1.getBrainDescription()).thenReturn("First brain description");
        when(brain1.getOrder()).thenReturn(1);

        when(brain2.getBrainName()).thenReturn("brainTwo");
        when(brain2.getBrainDescription()).thenReturn("Second brain description");
        when(brain2.getOrder()).thenReturn(2);

        service = new BrainIndexerService(brainVectorStore, List.of(brain1, brain2));
    }

    @Test
    @DisplayName("run should index all brains into brainVectorStore with correct metadata")
    void run_indexesBrains() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);

        service.run(args);

        verify(brainVectorStore, times(1)).add(anyList());
    }
}
