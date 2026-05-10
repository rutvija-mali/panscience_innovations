package com.example.demo.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class GroqTranscriptionService {

	private static final String TRANSCRIBE_URL = "https://api.groq.com/openai/v1/audio/transcriptions";

	private final RestClient http = RestClient.create();
	private final ObjectMapper objectMapper;

	@Value("${groq.api.key:}")
	private String apiKey;

	@Value("${groq.whisper.model:whisper-large-v3}")
	private String whisperModel;

	public GroqTranscriptionService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String transcribe(byte[] audioBytes, String originalFilename) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(
					"GROQ_API_KEY is not set. Set groq.api.key or the GROQ_API_KEY environment variable.");
		}
		if (audioBytes == null || audioBytes.length == 0) {
			throw new IllegalArgumentException("Empty audio file.");
		}

		ByteArrayResource fileResource = new ByteArrayResource(audioBytes) {
			@Override
			public String getFilename() {
				return (originalFilename == null || originalFilename.isBlank()) ? "audio" : originalFilename;
			}
		};

		MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
		form.add("file", fileResource);
		form.add("model", whisperModel);

		String responseBody;
		try {
			responseBody = http.post()
					.uri(TRANSCRIBE_URL)
					.header("Authorization", "Bearer " + apiKey)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(form)
					.retrieve()
					.body(String.class);
		} catch (RestClientResponseException e) {
			throw new IllegalStateException(parseGroqError(e), e);
		}

		if (responseBody == null || responseBody.isBlank()) {
			throw new IllegalStateException("Empty response from Groq transcription.");
		}

		try {
			JsonNode tree = objectMapper.readTree(responseBody);
			if (tree.has("error")) {
				String msg = tree.path("error").path("message").asText("Groq transcription error");
				throw new IllegalStateException(msg);
			}
			String text = tree.path("text").asText("");
			if (text.isBlank()) {
				throw new IllegalStateException("No transcript returned.");
			}
			return text.trim();
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to parse transcription response.", e);
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
}

