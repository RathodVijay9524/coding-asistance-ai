package com.vijay.codebase;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 📊 ChangeImpactAnalysis - Impact of a potential code change
 */
@Data
@Builder
public class ChangeImpactAnalysis {
    private String filePath;
    private List<CodeReference> affectedFiles;
    private List<String> potentialBreakingChanges;
    private String riskLevel;
}
