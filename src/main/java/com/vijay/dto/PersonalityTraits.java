package com.vijay.dto;

/**
 * 🧠 Personality Traits
 * 
 * Defines the personality characteristics of the AI assistant.
 * These traits remain consistent across interactions to create a cohesive personality.
 */
public class PersonalityTraits {
    
    private int helpfulness;           // 1-10 scale (how eager to help)
    private int humor;                 // 1-10 scale (how funny/witty)
    private int formality;             // 1-10 scale (1=casual, 10=formal)
    private int verbosity;             // 1-10 scale (1=concise, 10=detailed)
    private int patience;              // 1-10 scale (how patient with repeated questions)
    private int empathy;               // 1-10 scale (emotional sensitivity)
    private int enthusiasm;            // 1-10 scale (energy level)
    private int directness;            // 1-10 scale (1=indirect, 10=direct)
    
    // Constructor with defaults
    public PersonalityTraits() {
        this.helpfulness = 9;          // Very helpful
        this.humor = 6;                // Moderately funny
        this.formality = 5;            // Balanced (professional but friendly)
        this.verbosity = 6;            // Balanced (detailed but concise)
        this.patience = 9;             // Very patient
        this.empathy = 8;              // Highly empathetic
        this.enthusiasm = 7;           // Enthusiastic
        this.directness = 7;           // Fairly direct
    }
    
    // Constructor with custom values
    public PersonalityTraits(int helpfulness, int humor, int formality, int verbosity,
                            int patience, int empathy, int enthusiasm, int directness) {
        this.helpfulness = clamp(helpfulness);
        this.humor = clamp(humor);
        this.formality = clamp(formality);
        this.verbosity = clamp(verbosity);
        this.patience = clamp(patience);
        this.empathy = clamp(empathy);
        this.enthusiasm = clamp(enthusiasm);
        this.directness = clamp(directness);
    }
    
    // Clamp value between 1-10
    private int clamp(int value) {
        return Math.max(1, Math.min(10, value));
    }
    
    // Getters and Setters
    public int getHelpfulness() {
        return helpfulness;
    }
    
    public void setHelpfulness(int helpfulness) {
        this.helpfulness = clamp(helpfulness);
    }
    
    public int getHumor() {
        return humor;
    }
    
    public void setHumor(int humor) {
        this.humor = clamp(humor);
    }
    
    public int getFormality() {
        return formality;
    }
    
    public void setFormality(int formality) {
        this.formality = clamp(formality);
    }
    
    public int getVerbosity() {
        return verbosity;
    }
    
    public void setVerbosity(int verbosity) {
        this.verbosity = clamp(verbosity);
    }
    
    public int getPatience() {
        return patience;
    }
    
    public void setPatience(int patience) {
        this.patience = clamp(patience);
    }
    
    public int getEmpathy() {
        return empathy;
    }
    
    public void setEmpathy(int empathy) {
        this.empathy = clamp(empathy);
    }
    
    public int getEnthusiasm() {
        return enthusiasm;
    }
    
    public void setEnthusiasm(int enthusiasm) {
        this.enthusiasm = clamp(enthusiasm);
    }
    
    public int getDirectness() {
        return directness;
    }
    
    public void setDirectness(int directness) {
        this.directness = clamp(directness);
    }
    
    /**
     * Get personality summary
     */
    public String getPersonalitySummary() {
        return String.format(
            "Helpfulness: %d/10 | Humor: %d/10 | Formality: %d/10 | Verbosity: %d/10 | " +
            "Patience: %d/10 | Empathy: %d/10 | Enthusiasm: %d/10 | Directness: %d/10",
            helpfulness, humor, formality, verbosity, patience, empathy, enthusiasm, directness
        );
    }
    
    /**
     * Get personality archetype
     */
    public String getArchetype() {
        if (helpfulness >= 8 && empathy >= 8 && patience >= 8) {
            return "MENTOR";
        } else if (humor >= 7 && enthusiasm >= 7) {
            return "ENTHUSIAST";
        } else if (directness >= 8 && formality >= 7) {
            return "PROFESSIONAL";
        } else if (empathy >= 8 && formality <= 4) {
            return "FRIEND";
        } else {
            return "BALANCED";
        }
    }
    
    @Override
    public String toString() {
        return "PersonalityTraits{" +
                "archetype=" + getArchetype() +
                ", helpfulness=" + helpfulness +
                ", humor=" + humor +
                ", formality=" + formality +
                ", verbosity=" + verbosity +
                ", patience=" + patience +
                ", empathy=" + empathy +
                ", enthusiasm=" + enthusiasm +
                ", directness=" + directness +
                '}';
    }
}
