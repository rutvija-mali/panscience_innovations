package com.example.demo.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
public class GroqService {

	private static final String CHAT_URL = "https://api.groq.com/openai/v1/chat/completions";
	private static final int MAX_CONTEXT_CHARS = 100_000;

	private final RestClient http = RestClient.create();
	private final ObjectMapper objectMapper;

	@Value("${groq.api.key:}")
	private String apiKey;

	@Value("${groq.model:llama-3.3-70b-versatile}")
	private String model;

	public GroqService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String answerQuestion(String documentText, String question) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(
					"GROQ_API_KEY is not set. Set groq.api.key or the GROQ_API_KEY environment variable.");
		}
		String context = truncate(documentText);
		String userContent = """
				---DOCUMENT---
				%s
				---END---

				Question: %s
				""".formatted(context, question.trim());

		ObjectNode root = objectMapper.createObjectNode();
		root.put("model", model);
		root.put("temperature", 0.3);
		root.put("max_tokens", 2048);
		ArrayNode messages = root.putArray("messages");
		ObjectNode systemMsg = messages.addObject();
		systemMsg.put("role", "system");
		systemMsg.put("content",
				"You answer questions using only the document text in the user message. If the answer is not in the document, say you cannot find it in the document. Be concise.");
		ObjectNode userMsg = messages.addObject();
		userMsg.put("role", "user");
		userMsg.put("content", userContent);

		String body;
		try {
			body = objectMapper.writeValueAsString(root);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to build Groq request", e);
		}

		String responseBody;
		try {
			responseBody = http.post()
					.uri(CHAT_URL)
					.header("Authorization", "Bearer " + apiKey)
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.body(String.class);
		} catch (RestClientResponseException e) {
			throw new IllegalStateException(parseGroqError(e), e);
		}

		if (responseBody == null || responseBody.isBlank()) {
			throw new IllegalStateException("Empty response from Groq");
		}

		try {
			JsonNode tree = objectMapper.readTree(responseBody);
			if (tree.has("error")) {
				String msg = tree.path("error").path("message").asText("Groq API error");
				throw new IllegalStateException(msg);
			}
			JsonNode choices = tree.path("choices");
			if (!choices.isArray() || choices.isEmpty()) {
				throw new IllegalStateException("No answer from model (empty choices).");
			}
			JsonNode content = choices.get(0).path("message").path("content");
			if (content.isMissingNode() || content.asText().isBlank()) {
				String finish = choices.get(0).path("finish_reason").asText("");
				throw new IllegalStateException(
						finish.isBlank() ? "Model returned no text." : "Stopped: " + finish);
			}
			return content.asText().trim();
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to parse Groq response", e);
		}
	}

	private String parseGroqError(RestClientResponseException e) {
		String errBody = e.getResponseBodyAsString(StandardCharsets.UTF_8);
		if (errBody == null || errBody.isBlank()) {
			return e.getMessage();
		}
		try {
			JsonNode errTree = objectMapper.readTree(errBody);
			if (errTree.has("error")) {
				String msg = errTree.path("error").path("message").asText(errBody);
				if (!msg.isBlank()) {
					return msg;
				}
			}
		} catch (Exception ignored) {
			// fall through
		}
		return errBody;
	}

	private static String truncate(String text) {
		if (text.length() <= MAX_CONTEXT_CHARS) {
			return text;
		}
		return text.substring(0, MAX_CONTEXT_CHARS) + "\n\n[Document truncated for length.]";
	}

}
