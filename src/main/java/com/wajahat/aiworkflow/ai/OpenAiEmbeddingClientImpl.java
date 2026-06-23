package com.wajahat.aiworkflow.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingClientImpl implements OpenAiEmbeddingClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.embedding-model}")
    private String model;

    @Override
    public List<Double> embed(String input) {
        OpenAiEmbeddingRequest request =
                new OpenAiEmbeddingRequest(model, input, "float");

        OpenAiEmbeddingResponse response = webClientBuilder.build()
                .post()
                .uri("https://api.openai.com/v1/embeddings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiEmbeddingResponse.class)
                .block();

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("OpenAI embedding response is empty");
        }

        return response.data().getFirst().embedding();
    }
}