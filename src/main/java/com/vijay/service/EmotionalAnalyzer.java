package com.vijay.service;

import com.vijay.dto.EmotionalContext;
import com.vijay.dto.EmotionalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 🧠 Emotional Analyzer Service
 * 
 * Detects emotional state from user messages using:
 * - Keyword analysis
 * - Sentiment detection
 * - Punctuation patterns
 * - Message characteristics
 */
@Service
public class EmotionalAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(EmotionalAnalyzer.class);
    
    // Emotional keywords
    private static final Map<String, EmotionalState> POSITIVE_KEYWORDS = new HashMap<>();
    private static final Map<String, EmotionalState> NEGATIVE_KEYWORDS = new HashMap<>();
    private static final Map<String, EmotionalState> URGENT_KEYWORDS = new HashMap<>();
    
    static {
        // Positive keywords
        POSITIVE_KEYWORDS.put("great", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("excellent", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("amazing", EmotionalState.EXCITED);
        POSITIVE_KEYWORDS.put("awesome", EmotionalState.EXCITED);
        POSITIVE_KEYWORDS.put("wonderful", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("fantastic", EmotionalState.EXCITED);
        POSITIVE_KEYWORDS.put("love", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("thanks", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("thank you", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("appreciate", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("happy", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("glad", EmotionalState.POSITIVE);
        POSITIVE_KEYWORDS.put("perfect", EmotionalState.EXCITED);
        
        // Negative keywords
        NEGATIVE_KEYWORDS.put("bad", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("terrible", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("horrible", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("awful", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("hate", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("dislike", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("sad", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("unhappy", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("disappointed", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("fail", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("error", EmotionalState.NEGATIVE);
        NEGATIVE_KEYWORDS.put("problem", EmotionalState.NEGATIVE);
        
        // Urgent keywords
        URGENT_KEYWORDS.put("urgent", EmotionalState.URGENT);
        URGENT_KEYWORDS.put("asap", EmotionalState.URGENT);
        URGENT_KEYWORDS.put("immediately", EmotionalState.URGENT);
        URGENT_KEYWORDS.put("critical", EmotionalState.URGENT);
        URGENT_KEYWORDS.put("emergency", EmotionalState.URGENT);
        URGENT_KEYWORDS.put("help", EmotionalState.URGENT);
        URGENT_KEYWORDS.put("stuck", EmotionalState.FRUSTRATED);
        URGENT_KEYWORDS.put("blocked", EmotionalState.FRUSTRATED);
        URGENT_KEYWORDS.put("frustrated", EmotionalState.FRUSTRATED);
        URGENT_KEYWORDS.put("annoyed", EmotionalState.FRUSTRATED);
        URGENT_KEYWORDS.put("angry", EmotionalState.FRUSTRATED);
        URGENT_KEYWORDS.put("confused", EmotionalState.CONFUSED);
        URGENT_KEYWORDS.put("lost", EmotionalState.CONFUSED);
        URGENT_KEYWORDS.put("unclear", EmotionalState.CONFUSED);
        URGENT_KEYWORDS.put("don't understand", EmotionalState.CONFUSED);
    }
    
    /**
     * Analyze emotional state from user message
     */
    public EmotionalContext analyzeEmotion(String message) {
        logger.debug("🔍 Analyzing emotion from message: {}", message);
        
        if (message == null || message.isEmpty()) {
            logger.debug("⚠️ Empty message, returning neutral");
            return new EmotionalContext();
        }
        
        String lowerMessage = message.toLowerCase();
        
        // Detect emotional state
        EmotionalState detectedState = detectEmotionalState(lowerMessage);
        
        // Calculate intensity
        int intensity = calculateIntensity(message, lowerMessage);
        
        // Detect trigger keywords
        String triggerKeywords = detectTriggerKeywords(lowerMessage);
        
        // Determine recommended tone
        String recommendedTone = determineRecommendedTone(detectedState);
        
        // Calculate confidence
        double confidence = calculateConfidence(lowerMessage, detectedState);
        
        EmotionalContext context = new EmotionalContext(
            detectedState,
            intensity,
            triggerKeywords,
            recommendedTone,
            confidence
        );
        
        logger.debug("😊 Emotion detected: {}", context);
        return context;
    }
    
    /**
     * Detect emotional state from keywords and patterns
     */
    private EmotionalState detectEmotionalState(String lowerMessage) {
        // Check urgent keywords first (highest priority)
        for (Map.Entry<String, EmotionalState> entry : URGENT_KEYWORDS.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                logger.debug("🚨 Urgent emotion detected: {}", entry.getValue());
                return entry.getValue();
            }
        }
        
        // Check positive keywords
        for (Map.Entry<String, EmotionalState> entry : POSITIVE_KEYWORDS.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                logger.debug("😊 Positive emotion detected: {}", entry.getValue());
                return entry.getValue();
            }
        }
        
        // Check negative keywords
        for (Map.Entry<String, EmotionalState> entry : NEGATIVE_KEYWORDS.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                logger.debug("😞 Negative emotion detected: {}", entry.getValue());
                return entry.getValue();
            }
        }
        
        // Check punctuation patterns
        EmotionalState punctuationState = detectFromPunctuation(lowerMessage);
        if (punctuationState != EmotionalState.NEUTRAL) {
            logger.debug("📝 Emotion from punctuation: {}", punctuationState);
            return punctuationState;
        }
        
        logger.debug("😐 Neutral emotion detected");
        return EmotionalState.NEUTRAL;
    }
    
    /**
     * Detect emotion from punctuation patterns
     */
    private EmotionalState detectFromPunctuation(String message) {
        // Multiple exclamation marks = excited
        if (Pattern.compile("!{2,}").matcher(message).find()) {
            return EmotionalState.EXCITED;
        }
        
        // Multiple question marks = confused
        if (Pattern.compile("\\?{2,}").matcher(message).find()) {
            return EmotionalState.CONFUSED;
        }
        
        // ALL CAPS = urgent/frustrated
        if (message.length() > 3 && message.equals(message.toUpperCase())) {
            return EmotionalState.FRUSTRATED;
        }
        
        return EmotionalState.NEUTRAL;
    }
    
    /**
     * Calculate emotional intensity (0-100)
     */
    private int calculateIntensity(String originalMessage, String lowerMessage) {
        int intensity = 0;
        
        // Count emotional keywords
        int emotionalKeywordCount = 0;
        for (String keyword : POSITIVE_KEYWORDS.keySet()) {
            if (lowerMessage.contains(keyword)) emotionalKeywordCount++;
        }
        for (String keyword : NEGATIVE_KEYWORDS.keySet()) {
            if (lowerMessage.contains(keyword)) emotionalKeywordCount++;
        }
        for (String keyword : URGENT_KEYWORDS.keySet()) {
            if (lowerMessage.contains(keyword)) emotionalKeywordCount++;
        }
        
        intensity += Math.min(emotionalKeywordCount * 15, 50);
        
        // Punctuation intensity
        long exclamationCount = originalMessage.chars().filter(ch -> ch == '!').count();
        long questionCount = originalMessage.chars().filter(ch -> ch == '?').count();
        intensity += Math.min((exclamationCount + questionCount) * 10, 30);
        
        // ALL CAPS intensity
        if (originalMessage.length() > 3 && originalMessage.equals(originalMessage.toUpperCase())) {
            intensity += 20;
        }
        
        // Message length (very short = more intense)
        if (originalMessage.length() < 10) {
            intensity += 10;
        }
        
        return Math.min(intensity, 100);
    }
    
    /**
     * Detect trigger keywords that caused the emotion
     */
    private String detectTriggerKeywords(String lowerMessage) {
        StringBuilder triggers = new StringBuilder();
        
        for (String keyword : POSITIVE_KEYWORDS.keySet()) {
            if (lowerMessage.contains(keyword)) {
                if (triggers.length() > 0) triggers.append(", ");
                triggers.append(keyword);
            }
        }
        
        for (String keyword : NEGATIVE_KEYWORDS.keySet()) {
            if (lowerMessage.contains(keyword)) {
                if (triggers.length() > 0) triggers.append(", ");
                triggers.append(keyword);
            }
        }
        
        for (String keyword : URGENT_KEYWORDS.keySet()) {
            if (lowerMessage.contains(keyword)) {
                if (triggers.length() > 0) triggers.append(", ");
                triggers.append(keyword);
            }
        }
        
        return triggers.toString();
    }
    
    /**
     * Determine recommended tone for response
     */
    private String determineRecommendedTone(EmotionalState state) {
        switch (state) {
            case POSITIVE:
            case EXCITED:
                return "enthusiastic";
            case FRUSTRATED:
            case URGENT:
                return "empathetic_and_urgent";
            case CONFUSED:
                return "clear_and_detailed";
            case NEGATIVE:
                return "supportive";
            case CALM:
                return "professional";
            default:
                return "neutral";
        }
    }
    
    /**
     * Calculate confidence in emotion detection (0-1)
     */
    private double calculateConfidence(String lowerMessage, EmotionalState state) {
        if (state == EmotionalState.NEUTRAL) {
            return 0.5; // Lower confidence for neutral
        }
        
        // Count matching keywords
        int matchCount = 0;
        Map<String, EmotionalState> relevantKeywords = new HashMap<>();
        
        if (state.isPositive()) {
            relevantKeywords = POSITIVE_KEYWORDS;
        } else if (state.isNegative()) {
            relevantKeywords = NEGATIVE_KEYWORDS;
        } else if (state.isUrgent()) {
            relevantKeywords = URGENT_KEYWORDS;
        }
        
        for (String keyword : relevantKeywords.keySet()) {
            if (lowerMessage.contains(keyword)) {
                matchCount++;
            }
        }
        
        // Base confidence on keyword matches
        double confidence = Math.min(0.5 + (matchCount * 0.15), 0.95);
        
        logger.debug("📊 Confidence: {:.2f} (matches: {})", confidence, matchCount);
        return confidence;
    }
}
