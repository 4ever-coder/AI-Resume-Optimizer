package com.qianyi.resumeoptimizer.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiChatClient implements ChatModelClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiChatClient(ObjectMapper objectMapper) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(), objectMapper);
    }

    OpenAiChatClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String complete(ModelConnectionSettings settings, AnalysisPrompt prompt) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", settings.model(),
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", prompt.systemPrompt()),
                            Map.of("role", "user", "content", prompt.userPrompt())
                    )
            ));
            HttpRequest request = HttpRequest.newBuilder(chatCompletionsUri(settings.baseUrl()))
                    .timeout(Duration.ofSeconds(90))
                    .header("Authorization", "Bearer " + settings.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AnalysisException("模型接口调用失败，请检查 API Key、Base URL 和模型名。", "MODEL_REQUEST_FAILED");
            }
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new AnalysisException("模型没有返回可解析内容，请重新生成。", "EMPTY_MODEL_RESPONSE");
            }
            return content.asText();
        } catch (IOException exception) {
            throw new AnalysisException("模型接口请求失败，请检查网络或 Base URL。", "MODEL_NETWORK_ERROR");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AnalysisException("模型接口请求被中断，请重试。", "MODEL_REQUEST_INTERRUPTED");
        }
    }

    private URI chatCompletionsUri(String baseUrl) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return URI.create(normalized + "/chat/completions");
    }
}

