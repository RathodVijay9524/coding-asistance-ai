package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 🧠 HALLUCINATION DETECTOR - Phase 7
 * 
 * Purpose: Detects false claims, validates against known facts,
 * flags suspicious statements, and calculates hallucination score.
 * 
 * Responsibilities:
 * - Detect potential hallucinations
 * - Validate claims against known facts
 * - Flag suspicious statements
 * - Calculate hallucination score
 * - Provide hallucination report
 */
@Service
public class HallucinationDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(HallucinationDetector.class);
    
    // Known facts database (simplified)
    private final Set<String> knownFacts = new HashSet<>();
    private final Set<String> suspiciousPatterns = new HashSet<>();
    
    public HallucinationDetector() {
        initializeKnownFacts();
        initializeSuspiciousPatterns();
    }
    
    /**
     * Initialize known facts
     */
    private void initializeKnownFacts() {
        // Java facts
        knownFacts.add("java is a programming language");
        knownFacts.add("java runs on the jvm");
        knownFacts.add("java is object-oriented");
        knownFacts.add("java is compiled to bytecode");
        knownFacts.add("java is platform-independent");
        knownFacts.add("spring boot is a framework");
        knownFacts.add("spring boot simplifies spring development");
        knownFacts.add("maven is a build tool");
        knownFacts.add("gradle is a build tool");
        knownFacts.add("junit is a testing framework");
        
        // General facts
        knownFacts.add("earth orbits the sun");
        knownFacts.add("water boils at 100 degrees celsius");
        knownFacts.add("python is a programming language");
        knownFacts.add("javascript runs in browsers");
        knownFacts.add("sql is a query language");
        knownFacts.add("databases store data");
        knownFacts.add("apis allow communication between systems");
        knownFacts.add("git is a version control system");
        knownFacts.add("docker is a containerization platform");
        knownFacts.add("kubernetes orchestrates containers");
    }
    
    /**
     * Initialize suspicious patterns
     */
    private void initializeSuspiciousPatterns() {
        suspiciousPatterns.add("definitely");
        suspiciousPatterns.add("absolutely");
        suspiciousPatterns.add("100% sure");
        suspiciousPatterns.add("always works");
        suspiciousPatterns.add("never fails");
        suspiciousPatterns.add("guaranteed");
        suspiciousPatterns.add("impossible");
        suspiciousPatterns.add("everyone knows");
        suspiciousPatterns.add("obviously");
        suspiciousPatterns.add("clearly");
        suspiciousPatterns.add("without a doubt");
        suspiciousPatterns.add("no question");
    }
    
    /**
     * Detect hallucinations in response
     */
    public HallucinationReport detectHallucinations(String response) {
        HallucinationReport report = new HallucinationReport();
        
        if (response == null || response.isEmpty()) {
            return report;
        }
        
        // Run all detection methods
        detectFalseClaimsPatterns(response, report);
        detectOverconfidentStatements(response, report);
        detectLogicalInconsistencies(response, report);
        detectMissingEvidence(response, report);
        detectFactualErrors(response, report);
        
        // Calculate hallucination score
        double score = calculateHallucinationScore(report);
        report.setHallucinationScore(score);
        
        logger.info("🧠 Hallucination Detection: {} potential hallucinations detected (score: {:.2f})", 
            report.getHallucinationCount(), score);
        
        return report;
    }
    
    /**
     * Detect false claims patterns
     */
    private void detectFalseClaimsPatterns(String response, HallucinationReport report) {
        String lower = response.toLowerCase();
        
        // Check for impossible claims
        if (lower.contains("can read minds") || lower.contains("predict the future")) {
            report.addHallucination("Impossible claim detected", HallucinationSeverity.HIGH);
        }
        
        // Check for exaggerated claims
        if (lower.contains("the only way") && !lower.contains("one way")) {
            report.addHallucination("Potentially exaggerated claim: 'the only way'", HallucinationSeverity.MEDIUM);
        }
        
        // Check for false universals
        if (lower.contains("all ") && lower.contains("never")) {
            report.addHallucination("Contradictory universal claim", HallucinationSeverity.MEDIUM);
        }
    }
    
    /**
     * Detect overconfident statements
     */
    private void detectOverconfidentStatements(String response, HallucinationReport report) {
        String lower = response.toLowerCase();
        
        for (String pattern : suspiciousPatterns) {
            if (lower.contains(pattern)) {
                report.addHallucination("Overconfident statement: '" + pattern + "'", HallucinationSeverity.LOW);
            }
        }
    }
    
    /**
     * Detect logical inconsistencies
     */
    private void detectLogicalInconsistencies(String response, HallucinationReport report) {
        String lower = response.toLowerCase();
        
        // Check for self-contradictions
        if (lower.contains("is required") && lower.contains("is optional")) {
            report.addHallucination("Logical inconsistency: same thing marked as both required and optional", 
                HallucinationSeverity.HIGH);
        }
        
        if (lower.contains("must be") && lower.contains("must not be")) {
            report.addHallucination("Logical inconsistency: contradictory requirements", HallucinationSeverity.HIGH);
        }
        
        // Check for temporal inconsistencies
        if (lower.contains("before") && lower.contains("after")) {
            int beforeIndex = lower.indexOf("before");
            int afterIndex = lower.indexOf("after");
            if (Math.abs(beforeIndex - afterIndex) < 200) {
                report.addHallucination("Potential temporal inconsistency", HallucinationSeverity.MEDIUM);
            }
        }
    }
    
    /**
     * Detect missing evidence
     */
    private void detectMissingEvidence(String response, HallucinationReport report) {
        String lower = response.toLowerCase();
        
        // Check for claims without support
        if (lower.contains("studies show") && !lower.contains("study")) {
            report.addHallucination("Claim of studies without specific study mentioned", HallucinationSeverity.MEDIUM);
        }
        
        if (lower.contains("research proves") && !lower.contains("research")) {
            report.addHallucination("Claim of research without specific research mentioned", HallucinationSeverity.MEDIUM);
        }
        
        if (lower.contains("experts agree") && !lower.contains("expert")) {
            report.addHallucination("Claim of expert agreement without naming experts", HallucinationSeverity.MEDIUM);
        }
    }
    
    /**
     * Detect factual errors
     */
    private void detectFactualErrors(String response, HallucinationReport report) {
        String lower = response.toLowerCase();
        
        // Check against known facts
        for (String fact : knownFacts) {
            if (lower.contains(fact)) {
                // This is good - fact is mentioned correctly
                continue;
            }
            
            // Check for contradictions of known facts
            String[] parts = fact.split(" is ");
            if (parts.length == 2) {
                String subject = parts[0];
                String predicate = parts[1];
                
                // Check for negation of known facts
                if (lower.contains(subject) && lower.contains("is not " + predicate)) {
                    report.addHallucination("Contradiction of known fact: " + fact, HallucinationSeverity.HIGH);
                }
            }
        }
    }
    
    /**
     * Calculate hallucination score
     */
    private double calculateHallucinationScore(HallucinationReport report) {
        double score = 0.0;
        
        // Weight by severity
        long highCount = report.getHallucinations().stream()
            .filter(h -> h.severity == HallucinationSeverity.HIGH)
            .count();
        
        long mediumCount = report.getHallucinations().stream()
            .filter(h -> h.severity == HallucinationSeverity.MEDIUM)
            .count();
        
        long lowCount = report.getHallucinations().stream()
            .filter(h -> h.severity == HallucinationSeverity.LOW)
            .count();
        
        score = (highCount * 0.5) + (mediumCount * 0.2) + (lowCount * 0.05);
        
        // Cap at 1.0
        return Math.min(1.0, score);
    }
    
    /**
     * Add custom known fact
     */
    public void addKnownFact(String fact) {
        knownFacts.add(fact.toLowerCase());
        logger.debug("🧠 Hallucination Detector: Added known fact: {}", fact);
    }
    
    /**
     * Add custom suspicious pattern
     */
    public void addSuspiciousPattern(String pattern) {
        suspiciousPatterns.add(pattern.toLowerCase());
        logger.debug("🧠 Hallucination Detector: Added suspicious pattern: {}", pattern);
    }
    
    /**
     * Validate claim against known facts
     */
    public boolean validateClaim(String claim) {
        String lower = claim.toLowerCase();
        
        for (String fact : knownFacts) {
            if (lower.contains(fact)) {
                return true; // Claim aligns with known fact
            }
        }
        
        return false; // Claim not validated
    }
    
    // ============ Inner Classes ============
    
    /**
     * Hallucination severity levels
     */
    public enum HallucinationSeverity {
        LOW, MEDIUM, HIGH
    }
    
    /**
     * Single hallucination
     */
    public static class Hallucination {
        public final String message;
        public final HallucinationSeverity severity;
        
        public Hallucination(String message, HallucinationSeverity severity) {
            this.message = message;
            this.severity = severity;
        }
    }
    
    /**
     * Hallucination report
     */
    public static class HallucinationReport {
        private final List<Hallucination> hallucinations = new ArrayList<>();
        private double hallucinationScore = 0.0;
        
        public void addHallucination(String message, HallucinationSeverity severity) {
            hallucinations.add(new Hallucination(message, severity));
        }
        
        public List<Hallucination> getHallucinations() {
            return new ArrayList<>(hallucinations);
        }
        
        public int getHallucinationCount() {
            return hallucinations.size();
        }
        
        public void setHallucinationScore(double score) {
            this.hallucinationScore = Math.max(0.0, Math.min(1.0, score));
        }
        
        public double getHallucinationScore() {
            return hallucinationScore;
        }
        
        public boolean hasHighSeverityHallucinations() {
            return hallucinations.stream()
                .anyMatch(h -> h.severity == HallucinationSeverity.HIGH);
        }
        
        public boolean isTrusted() {
            return hallucinationScore < 0.3 && !hasHighSeverityHallucinations();
        }
        
        public HallucinationSeverity getHighestSeverity() {
            return hallucinations.stream()
                .map(h -> h.severity)
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElse(HallucinationSeverity.LOW);
        }
    }
}
