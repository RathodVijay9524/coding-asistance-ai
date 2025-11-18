package com.vijay.codebase;

import lombok.Data;

@Data
public class CodeReference {
    private String filePath;
    private int lineNumber;
    private String context;
}
