package com.vijay.service;

import com.vijay.dto.CognitiveBias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 Cognitive Bias Engine
 * 
 * Simulates human-like cognitive biases in decision-making and response generation.
 * Makes the AI think more naturally with inherent biases like humans have.
 */
@Service
public class CognitiveBiasEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(CognitiveBiasEngine.class);
    
    private final Map<String, List<String>> recentInteractions = new ConcurrentHashMap<>();
    private final Map<String, CognitiveBias> activeBiases = new ConcurrentHashMap<>();
    
    private static final int MAX_RECENT_ITEMS = 10;
    
    public CognitiveBiasEngine() {
        logger.info("🧠 Cognitive Bias Engine initialized - Simulating human-like thinking patterns");
    }
    
    /**
     * Apply cognitive biases to decision-making
     */
    public String applyBiases(String userId, String query, String baseResponse) {
        logger.debug("🎯 Applying cognitive biases for user: {}", userId);
        
        CognitiveBias activeBias = getActiveBias(userId);
        String biasedResponse = baseResponse;
        
        switch (activeBias) {
            case RECENCY_BIAS:
                biasedResponse = applyRecencyBias(userId, biasedResponse);
                break;
            case AVAILABILITY_HEURISTIC:
                biasedResponse = applyAvailabilityHeuristic(userId, biasedResponse);
                break;
            case CONFIRMATION_BIAS:
                biasedResponse = applyConfirmationBias(userId, biasedResponse);
                break;
            case ANCHORING_BIAS:
                biasedResponse = applyAnchoringBias(userId, biasedResponse);
                break;
            case POSITIVITY_BIAS:
                biasedResponse = applyPositivityBias(biasedResponse);
                break;
            case STATUS_QUO_BIAS:
                biasedResponse = applyStatusQuoBias(biasedResponse);
                break;
            case NEGATIVITY_BIAS:
                biasedResponse = applyNegativityBias(biasedResponse);
                break;
            case NONE:
            default:
                // No bias applied
                break;
        }
        
        logger.debug("✅ Biases applied - Active bias: {}", activeBias.getDisplayName());
        return biasedResponse;
    }
    
    /**
     * Apply recency bias - recent information weighted more
     */
    private String applyRecencyBias(String userId, String response) {
        List<String> recent = recentInteractions.getOrDefault(userId, new ArrayList<>());
        
        if (!recent.isEmpty()) {
            // Emphasize recent information
            response = response.replaceAll("(?i)recently", "**recently** (as we just discussed)");
            response = response.replaceAll("(?i)last time", "**last time** (our most recent interaction)");
            logger.debug("📊 Recency bias applied - {} recent interactions", recent.size());
        }
        
        return response;
    }
    
    /**
     * Apply availability heuristic - easy-to-recall info prioritized
     */
    private String applyAvailabilityHeuristic(String userId, String response) {
        // Prioritize common/familiar concepts
        response = response.replaceAll("(?i)common", "**very common** (easy to think of)");
        response = response.replaceAll("(?i)popular", "**widely popular** (comes to mind easily)");
        response = response.replaceAll("(?i)well-known", "**well-known** (immediately familiar)");
        
        logger.debug("🧠 Availability heuristic applied - prioritizing familiar concepts");
        return response;
    }
    
    /**
     * Apply confirmation bias - seek confirming information
     */
    private String applyConfirmationBias(String userId, String response) {
        // Emphasize supporting evidence
        response = response.replaceAll("(?i)supports", "**strongly supports**");
        response = response.replaceAll("(?i)evidence", "**clear evidence**");
        response = response.replaceAll("(?i)shows", "**clearly shows**");
        
        logger.debug("✓ Confirmation bias applied - emphasizing supporting evidence");
        return response;
    }
    
    /**
     * Apply anchoring bias - first info influences decisions
     */
    private String applyAnchoringBias(String userId, String response) {
        // Emphasize first/initial information
        if (response.length() > 0) {
            String[] sentences = response.split("\\. ");
            if (sentences.length > 0) {
                sentences[0] = "**[Key point]** " + sentences[0];
                response = String.join(". ", sentences);
            }
        }
        
        logger.debug("⚓ Anchoring bias applied - emphasizing initial information");
        return response;
    }
    
    /**
     * Apply positivity bias - see positive aspects
     */
    private String applyPositivityBias(String response) {
        response = response.replaceAll("(?i)problem", "challenge");
        response = response.replaceAll("(?i)issue", "opportunity");
        response = response.replaceAll("(?i)difficult", "interesting");
        response = response.replaceAll("(?i)bad", "suboptimal");
        
        logger.debug("😊 Positivity bias applied - emphasizing positive aspects");
        return response;
    }
    
    /**
     * Apply status quo bias - preference for current state
     */
    private String applyStatusQuoBias(String response) {
        response = response.replaceAll("(?i)change", "significant change");
        response = response.replaceAll("(?i)new", "relatively new");
        response = response.replaceAll("(?i)different", "quite different");
        
        logger.debug("🔄 Status quo bias applied - emphasizing current state");
        return response;
    }
    
    /**
     * Apply negativity bias - negative info weighted more
     */
    private String applyNegativityBias(String response) {
        response = response.replaceAll("(?i)risk", "**significant risk**");
        response = response.replaceAll("(?i)danger", "**serious danger**");
        response = response.replaceAll("(?i)problem", "**major problem**");
        
        logger.debug("⚠️ Negativity bias applied - emphasizing negative aspects");
        return response;
    }
    
    /**
     * Record user interaction for bias tracking
     */
    public void recordInteraction(String userId, String interaction) {
        List<String> interactions = recentInteractions.computeIfAbsent(userId, k -> new ArrayList<>());
        interactions.add(interaction);
        
        // Keep only recent interactions
        if (interactions.size() > MAX_RECENT_ITEMS) {
            interactions.remove(0);
        }
        
        logger.debug("📝 Interaction recorded for user: {}", userId);
    }
    
    /**
     * Get active bias for user
     */
    public CognitiveBias getActiveBias(String userId) {
        return activeBiases.getOrDefault(userId, CognitiveBias.RECENCY_BIAS);
    }
    
    /**
     * Set active bias for user
     */
    public void setActiveBias(String userId, CognitiveBias bias) {
        activeBiases.put(userId, bias);
        logger.info("🎯 Active bias set for user {} - {}", userId, bias.getDisplayName());
    }
    
    /**
     * Rotate bias for user (simulate changing thinking patterns)
     */
    public void rotateBias(String userId) {
        CognitiveBias currentBias = getActiveBias(userId);
        CognitiveBias[] biases = CognitiveBias.values();
        
        int currentIndex = Arrays.asList(biases).indexOf(currentBias);
        int nextIndex = (currentIndex + 1) % biases.length;
        
        CognitiveBias nextBias = biases[nextIndex];
        setActiveBias(userId, nextBias);
        
        logger.info("🔄 Bias rotated for user {} - {} → {}", userId, currentBias.getDisplayName(), nextBias.getDisplayName());
    }
    
    /**
     * Get bias strength (how much it influences decisions)
     */
    public float getBiasStrength(String userId) {
        CognitiveBias bias = getActiveBias(userId);
        return bias.getWeight();
    }
    
    /**
     * Get all available biases
     */
    public List<CognitiveBias> getAvailableBiases() {
        return Arrays.asList(CognitiveBias.values());
    }
    
    /**
     * Get bias summary
     */
    public String getBiasSummary(String userId) {
        CognitiveBias bias = getActiveBias(userId);
        return String.format(
            "🧠 Active Bias: %s (Strength: %.1f) - %s",
            bias.getDisplayName(),
            bias.getWeight() * 100,
            bias.getDescription()
        );
    }
}
