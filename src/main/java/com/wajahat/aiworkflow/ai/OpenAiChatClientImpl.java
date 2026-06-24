package com.wajahat.aiworkflow.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiChatClientImpl implements OpenAiChatClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api-key}")
    private String apiKey;

    @Override
    public String chat(String model, String systemPrompt, String context, String userQuestion) {
        String finalSystemPrompt = systemPrompt + """



                Use only the context below to answer.
                If the answer is not present in the context, say you do not know.

                Context:
                """ + context;

        OpenAiChatRequest request = new OpenAiChatRequest(
                model,
                List.of(
                        new OpenAiChatMessage("system", finalSystemPrompt),
                        new OpenAiChatMessage("user", userQuestion)
                ),
                0.2
        );

        OpenAiChatResponse response = webClientBuilder.build()
                .post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .block();

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI chat response is empty");
        }

        return response.choices().getFirst().message().content();
    }
}