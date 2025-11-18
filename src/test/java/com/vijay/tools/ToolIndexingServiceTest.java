package com.vijay.tools;

import com.vijay.manager.AiToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class ToolIndexingServiceTest {

    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
    }

    @Test
    @DisplayName("run should index @Tool methods from AiToolProviders into VectorStore")
    void run_indexesTools() throws Exception {
        // Simple inline AiToolProvider with a single @Tool method
        AiToolProvider provider = new AiToolProvider() {
            @org.springframework.ai.tool.annotation.Tool(description = "Test tool")
            public String testTool() {
                return "ok";
            }
        };

        List<AiToolProvider> providers = List.of(provider);
        ToolIndexingService service = new ToolIndexingService(vectorStore, providers);

        ApplicationArguments args = mock(ApplicationArguments.class);

        // Capture documents passed to vectorStore.add
        final List<Document> captured = new ArrayList<>();
        doAnswer(invocation -> {
            List<Document> docs = invocation.getArgument(0);
            captured.addAll(docs);
            return null;
        }).when(vectorStore).add(anyList());

        service.run(args);

        verify(vectorStore, times(1)).add(anyList());
        assertThat(captured).isNotEmpty();
        assertThat(captured.get(0).getMetadata()).containsKey("toolName");
    }
}
