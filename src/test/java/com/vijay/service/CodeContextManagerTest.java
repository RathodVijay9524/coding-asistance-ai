package com.vijay.service;

import com.vijay.dto.CodeCursor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CodeContextManagerTest {

    private CodeContextManager manager;

    @BeforeEach
    void setUp() {
        manager = new CodeContextManager();
    }

    @Test
    @DisplayName("createCursor should build context, scope, imports and dependencies from Java file")
    void createCursor_basic() throws Exception {
        Path tempFile = Files.createTempFile("TestClass", ".java");
        String code = "package com.example;\n" +
                "import java.util.List;\n" +
                "public class TestClass {\n" +
                "    public void doSomething() {\n" +
                "        System.out.println(\"hi\");\n" +
                "    }\n" +
                "}\n";
        Files.writeString(tempFile, code);

        CodeCursor cursor = manager.createCursor(tempFile.toString(), 5, 3);

        assertThat(cursor.getFilePath()).isEqualTo(tempFile.toString());
        assertThat(cursor.getLanguage()).isEqualTo("java");
        assertThat(cursor.getContextCode()).contains("5:");
        assertThat(cursor.getScope()).isEqualTo("TestClass.doSomething");
        assertThat(cursor.getClassName()).isEqualTo("TestClass");
        assertThat(cursor.getMethodName()).isEqualTo("doSomething");
        assertThat(cursor.getImports()).contains("java.util.List");
        assertThat(cursor.getDependencies()).contains("java");
    }

    @Test
    @DisplayName("createCursor should return default cursor for empty file path")
    void createCursor_emptyPath() {
        CodeCursor cursor = manager.createCursor("", 10);

        assertThat(cursor.getFilePath()).isEqualTo("unknown");
        assertThat(cursor.getLanguage()).isEqualTo("unknown");
        assertThat(cursor.getScope()).isEqualTo("unknown");
    }
}
