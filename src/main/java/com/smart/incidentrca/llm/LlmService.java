package com.smart.incidentrca.llm;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class LlmService {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Value("${llm.rca.prompt}")
    private String rcaPrompt;

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String generateRca(String description, String severity, String environment, List<String> symptoms) throws Exception {
        if (!isEnabled()) {
            throw new IllegalStateException("LLM disabled");
        }

        // Format the prompt with incident data
        String prompt = String.format(rcaPrompt,
                description, severity, environment, String.join(", ", symptoms));

        // Build JSON request for OpenAI Chat API
        JSONObject json = new JSONObject();
        json.put("model", "gpt-4");
        json.put("messages", List.of(
                new JSONObject().put("role", "user").put("content", prompt)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse AI response
        JSONObject responseJson = new JSONObject(response.body());
        String aiText = responseJson.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        return aiText;
    }
}
