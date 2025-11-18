package com.vijay.service;

import com.vijay.dto.CommunicationStyle;
import com.vijay.dto.PersonalityTraits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalityEngineTest {

    private PersonalityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PersonalityEngine();
    }

    @Test
    @DisplayName("applyPersonality should apply casual and formal tone contractions appropriately")
    void applyPersonality_toneContractions() {
        PersonalityTraits traits = engine.getTraits();
        String base = "I cannot help if you do not provide details and I will not guess.";

        traits.setFormality(2);
        String casual = engine.applyPersonality(base);
        assertThat(casual).contains("can't").contains("don't").contains("won't");

        traits.setFormality(9);
        String formal = engine.applyPersonality("I can't help if you don't provide details and I won't guess.");
        assertThat(formal).contains("cannot").contains("do not").contains("will not");
    }

    @Test
    @DisplayName("applyPersonality should adjust language level between simple and technical")
    void applyPersonality_languageLevel() {
        CommunicationStyle style = engine.getStyle();

        style.setLanguageLevel("simple");
        String simple = engine.applyPersonality("We will utilize this API and implement helpers to facilitate usage.");
        assertThat(simple.toLowerCase()).contains("use this api").contains("create helpers").contains("help usage");

        style.setLanguageLevel("technical");
        String technical = engine.applyPersonality("We will use this API and create helpers.");
        assertThat(technical.toLowerCase()).contains("utilize this api").contains("implement helpers");
    }

    @Test
    @DisplayName("applyPersonality should honor emoji usage and enthusiasm")
    void applyPersonality_emojiAndEnthusiasm() {
        CommunicationStyle style = engine.getStyle();
        PersonalityTraits traits = engine.getTraits();

        style.setEmojiUsage("none");
        String noEmoji = engine.applyPersonality("Hi there! 😊 success is great!");
        assertThat(noEmoji).doesNotContain("😊");

        style.setEmojiUsage("heavy");
        String withEmoji = engine.applyPersonality("This is a success and a great result.");
        assertThat(withEmoji.toLowerCase()).contains("success").contains("great");

        traits.setEnthusiasm(9);
        String enthusiastic = engine.applyPersonality("This is a great and good result.");
        assertThat(enthusiastic.toLowerCase()).contains("amazing").contains("excellent");
    }

    @Test
    @DisplayName("applyPersonality should add humor markers when humor is high and response mentions error")
    void applyPersonality_humor() {
        PersonalityTraits traits = engine.getTraits();
        traits.setHumor(9);

        String result = engine.applyPersonality("We encountered an error while processing your request.");
        assertThat(result.toLowerCase()).contains("error (oops!");
    }

    @Test
    @DisplayName("getGreeting and getClosing should follow communication style presets")
    void greetingsAndClosings_followStyle() {
        CommunicationStyle style = engine.getStyle();

        assertThat(engine.getGreeting()).contains("Hi there");
        assertThat(engine.getClosing()).contains("Hope this helps");

        style.setGreetingStyle("formal");
        style.setClosingStyle("formal");
        assertThat(engine.getGreeting()).isEqualTo("Good day,");
        assertThat(engine.getClosing()).isEqualTo("Best regards");

        style.setGreetingStyle("casual");
        style.setClosingStyle("casual");
        assertThat(engine.getGreeting()).isEqualTo("Hey,");
        assertThat(engine.getClosing()).isEqualTo("Catch you later!");
    }

    @Test
    @DisplayName("Personality flags and summaries should reflect default traits and style")
    void personalityFlagsAndSummaries() {
        assertThat(engine.isEmpathetic()).isTrue();
        assertThat(engine.isPatient()).isTrue();
        assertThat(engine.isHelpful()).isTrue();

        String personalitySummary = engine.getPersonalitySummary();
        String styleSummary = engine.getStyleSummary();

        assertThat(personalitySummary).contains("Personality:");
        assertThat(styleSummary).contains("Communication:");
    }
}
