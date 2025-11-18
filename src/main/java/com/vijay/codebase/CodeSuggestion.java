package com.vijay.codebase;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class CodeSuggestion {
    private String title;
    private String description;
    private String targetPath;
}

@Data
@EqualsAndHashCode(callSuper = false)
class RefactorSuggestion extends CodeSuggestion {
    public RefactorSuggestion(String title, String description, String path) {
        setTitle(title);
        setDescription(description);
        setTargetPath(path);
    }
}

@Data
@EqualsAndHashCode(callSuper = false)
class TestSuggestion extends CodeSuggestion {
    public TestSuggestion(String title, String description, String path) {
        setTitle(title);
        setDescription(description);
        setTargetPath(path);
    }
}

@Data
@EqualsAndHashCode(callSuper = false)
class ArchitectureSuggestion extends CodeSuggestion {
    public ArchitectureSuggestion(String title, String description, String path) {
        setTitle(title);
        setDescription(description);
        setTargetPath(path);
    }
}

@Data
@EqualsAndHashCode(callSuper = false)
class PerformanceSuggestion extends CodeSuggestion {
    public PerformanceSuggestion(String title, String description, String path) {
        setTitle(title);
        setDescription(description);
        setTargetPath(path);
    }
}
