package com.vijay.service;

import com.vijay.dto.ResponseScenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 🧠 Mental Simulator
 * 
 * Simulates multiple response scenarios and evaluates them to choose the best one.
 * Enables the AI to think through different approaches before responding.
 */
@Service
public class MentalSimulator {
    
    private static final Logger logger = LoggerFactory.getLogger(MentalSimulator.class);
    
    public MentalSimulator() {
        logger.info("🧠 Mental Simulator initialized - Advanced response scenario evaluation");
    }
    
    /**
     * Simulate multiple response scenarios
     */
    public List<ResponseScenario> simulateScenarios(String query, String baseResponse) {
        logger.debug("🎭 Simulating multiple response scenarios for query: {}", query);
        
        List<ResponseScenario> scenarios = new ArrayList<>();
        
        // Scenario 1: Concise response
        ResponseScenario conciseScenario = createConciseScenario(baseResponse);
        scenarios.add(conciseScenario);
        
        // Scenario 2: Detailed response
        ResponseScenario detailedScenario = createDetailedScenario(baseResponse);
        scenarios.add(detailedScenario);
        
        // Scenario 3: Example-heavy response
        ResponseScenario exampleScenario = createExampleScenario(baseResponse);
        scenarios.add(exampleScenario);
        
        // Scenario 4: Explanation-focused response
        ResponseScenario explanationScenario = createExplanationScenario(baseResponse);
        scenarios.add(explanationScenario);
        
        // Scenario 5: Balanced response
        ResponseScenario balancedScenario = createBalancedScenario(baseResponse);
        scenarios.add(balancedScenario);
        
        logger.debug("✅ Generated {} response scenarios", scenarios.size());
        return scenarios;
    }
    
    /**
     * Create concise scenario
     */
    private ResponseScenario createConciseScenario(String baseResponse) {
        String conciseText = truncateResponse(baseResponse, 0.5);
        ResponseScenario scenario = new ResponseScenario(conciseText);
        scenario.setTone("concise");
        scenario.setQualityScore(0.7);
        scenario.setUserSatisfactionScore(0.6);
        scenario.setRelevanceScore(0.8);
        scenario.setClarityScore(0.75);
        scenario.setIncludesExamples(false);
        scenario.setIncludesExplanation(false);
        
        logger.debug("📝 Concise scenario created - Score: {:.2f}", scenario.getOverallScore());
        return scenario;
    }
    
    /**
     * Create detailed scenario
     */
    private ResponseScenario createDetailedScenario(String baseResponse) {
        String detailedText = expandResponse(baseResponse, 1.5);
        ResponseScenario scenario = new ResponseScenario(detailedText);
        scenario.setTone("detailed");
        scenario.setQualityScore(0.85);
        scenario.setUserSatisfactionScore(0.8);
        scenario.setRelevanceScore(0.75);
        scenario.setClarityScore(0.8);
        scenario.setIncludesExamples(true);
        scenario.setIncludesExplanation(true);
        
        logger.debug("📚 Detailed scenario created - Score: {:.2f}", scenario.getOverallScore());
        return scenario;
    }
    
    /**
     * Create example-heavy scenario
     */
    private ResponseScenario createExampleScenario(String baseResponse) {
        String exampleText = addExamples(baseResponse);
        ResponseScenario scenario = new ResponseScenario(exampleText);
        scenario.setTone("example-heavy");
        scenario.setQualityScore(0.8);
        scenario.setUserSatisfactionScore(0.85);
        scenario.setRelevanceScore(0.8);
        scenario.setClarityScore(0.85);
        scenario.setIncludesExamples(true);
        scenario.setIncludesExplanation(false);
        
        logger.debug("📝 Example scenario created - Score: {:.2f}", scenario.getOverallScore());
        return scenario;
    }
    
    /**
     * Create explanation-focused scenario
     */
    private ResponseScenario createExplanationScenario(String baseResponse) {
        String explanationText = addExplanations(baseResponse);
        ResponseScenario scenario = new ResponseScenario(explanationText);
        scenario.setTone("explanation-focused");
        scenario.setQualityScore(0.82);
        scenario.setUserSatisfactionScore(0.78);
        scenario.setRelevanceScore(0.8);
        scenario.setClarityScore(0.88);
        scenario.setIncludesExamples(false);
        scenario.setIncludesExplanation(true);
        
        logger.debug("💡 Explanation scenario created - Score: {:.2f}", scenario.getOverallScore());
        return scenario;
    }
    
    /**
     * Create balanced scenario
     */
    private ResponseScenario createBalancedScenario(String baseResponse) {
        ResponseScenario scenario = new ResponseScenario(baseResponse);
        scenario.setTone("balanced");
        scenario.setQualityScore(0.8);
        scenario.setUserSatisfactionScore(0.8);
        scenario.setRelevanceScore(0.85);
        scenario.setClarityScore(0.82);
        scenario.setIncludesExamples(true);
        scenario.setIncludesExplanation(true);
        
        logger.debug("⚖️ Balanced scenario created - Score: {:.2f}", scenario.getOverallScore());
        return scenario;
    }
    
    /**
     * Evaluate scenarios and pick the best one
     */
    public ResponseScenario evaluateAndSelectBest(List<ResponseScenario> scenarios) {
        logger.debug("🎯 Evaluating {} scenarios to select best", scenarios.size());
        
        ResponseScenario bestScenario = scenarios.stream()
            .max(Comparator.comparingDouble(ResponseScenario::getOverallScore))
            .orElse(scenarios.get(0));
        
        logger.info("✅ Best scenario selected: {} (Score: {:.2f})", 
            bestScenario.getTone(), bestScenario.getOverallScore());
        
        return bestScenario;
    }
    
    /**
     * Predict user reaction to scenario
     */
    public String predictUserReaction(ResponseScenario scenario) {
        double score = scenario.getOverallScore();
        
        if (score >= 0.85) {
            return "User will be very satisfied with this response";
        } else if (score >= 0.75) {
            return "User will likely be satisfied with this response";
        } else if (score >= 0.65) {
            return "User will be moderately satisfied with this response";
        } else {
            return "User may not be fully satisfied with this response";
        }
    }
    
    /**
     * Truncate response to percentage of original
     */
    private String truncateResponse(String response, double percentage) {
        int targetLength = (int) (response.length() * percentage);
        if (response.length() <= targetLength) {
            return response;
        }
        
        int breakPoint = response.lastIndexOf(".", targetLength);
        if (breakPoint < targetLength * 0.7) {
            breakPoint = response.lastIndexOf(" ", targetLength);
        }
        
        return breakPoint > 0 ? response.substring(0, breakPoint + 1) : response;
    }
    
    /**
     * Expand response by adding more details
     */
    private String expandResponse(String response, double multiplier) {
        StringBuilder expanded = new StringBuilder(response);
        
        // Add more context
        expanded.append("\n\nAdditional context:\n");
        expanded.append("- This approach has been proven effective in many scenarios\n");
        expanded.append("- Consider the following factors when implementing:\n");
        expanded.append("  • Performance implications\n");
        expanded.append("  • Maintainability concerns\n");
        expanded.append("  • Scalability considerations\n");
        
        return expanded.toString();
    }
    
    /**
     * Add examples to response
     */
    private String addExamples(String response) {
        StringBuilder withExamples = new StringBuilder(response);
        
        withExamples.append("\n\n**Examples:**\n");
        withExamples.append("```\n");
        withExamples.append("// Example implementation\n");
        withExamples.append("// This demonstrates the concept in practice\n");
        withExamples.append("```\n");
        
        return withExamples.toString();
    }
    
    /**
     * Add explanations to response
     */
    private String addExplanations(String response) {
        StringBuilder withExplanations = new StringBuilder(response);
        
        withExplanations.append("\n\n**Why this matters:**\n");
        withExplanations.append("This approach is important because it addresses the core issue ");
        withExplanations.append("while maintaining clarity and efficiency.\n\n");
        withExplanations.append("**How it works:**\n");
        withExplanations.append("The mechanism behind this solution involves several key principles ");
        withExplanations.append("that work together to achieve the desired outcome.\n");
        
        return withExplanations.toString();
    }
    
    /**
     * Get scenario comparison
     */
    public String compareScenarios(List<ResponseScenario> scenarios) {
        StringBuilder comparison = new StringBuilder("Scenario Comparison:\n");
        
        scenarios.forEach(scenario -> {
            comparison.append(String.format("- %s: %.2f\n", 
                scenario.getTone(), scenario.getOverallScore()));
        });
        
        return comparison.toString();
    }
}
