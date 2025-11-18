package com.vijay.dto;

import java.util.Random;

public class JudgeAdvisor {

    public static int evaluate(String answer) {
        if (answer == null || answer.isBlank()) return 1;

        // Mock rating logic — could call another LLM here
        int rating = new Random().nextInt(5) + 1;
        return rating;
    }
}
