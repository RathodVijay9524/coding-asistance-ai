package com.vijay.manager;

import com.vijay.dto.JudgeAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelfRefineEvaluationAdvisor implements CallAdvisor {


    private final ChatClient judgeClient;

    @Autowired
    public SelfRefineEvaluationAdvisor(OpenAiChatModel judgeModel) {
        // दूसरा मॉडल जो केवल "judge" का काम करेगा
        this.judgeClient = ChatClient.builder(judgeModel).build();
    }

    // Secondary constructor for tests, allows injecting a mocked ChatClient
    public SelfRefineEvaluationAdvisor(ChatClient judgeClient) {
        this.judgeClient = judgeClient;
    }



    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {

        // Step 1️⃣: बेसिक मॉडल से जवाब लेना
        ChatClientResponse response = chain.nextCall(request);
        String answer = response.chatResponse().getResult().getOutput().getText();

        // Step 2️⃣: Judge मॉडल से evaluate करवाना
        String evaluationPrompt = """
            Please rate the following answer from 1 (bad) to 5 (excellent):
            ---
            %s
            ---
            Only return a number between 1 and 5.
        """.formatted(answer);

        ChatResponse judgeResponse = judgeClient
                .prompt(evaluationPrompt)
                .call()
                .chatResponse();

        String ratingStr = judgeResponse.getResult().getOutput().getText().trim();

        int rating = JudgeAdvisor.evaluate(answer);

        System.out.println("🧾 Judge rating: " + rating);

        // Step 3️⃣: अगर रेटिंग 4 से कम है तो recursion
        if (rating < 4) {
            System.out.println("🔁 Re-evaluating... (rating too low)");

            String newPrompt = """
                The previous answer was rated %d/5.
                Please improve your response to make it clearer and more accurate.
                Original answer:
                %s
            """.formatted(rating, answer);

            ChatClientRequest improvedRequest = request.mutate()
                    .prompt(request.prompt().augmentUserMessage(newPrompt))
                    .build();

            // Recursive call - only continue from this point
            return chain.copy(this).nextCall(improvedRequest);
        }

        // Step 4️⃣: Final result
        return response;
    }

    @Override
    public String getName() {
        return "selfRefineEvaluationAdvisor";
    }

    @Override
    public int getOrder() {
        // आप चाहो तो order control कर सकते हो
        return 1000;
    }


}


