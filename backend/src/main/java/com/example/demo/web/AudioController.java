package com.example.demo.web;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.DocumentStore;
import com.example.demo.service.GroqTranscriptionService;

@RestController
@RequestMapping("/audio")
public class AudioController {

	private final GroqTranscriptionService transcriptionService;
	private final DocumentStore documentStore;

	public AudioController(GroqTranscriptionService transcriptionService, DocumentStore documentStore) {
		this.transcriptionService = transcriptionService;
		this.documentStore = documentStore;
	}

	@PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> transcribe(@RequestPart("file") MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Upload an audio file."));
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Could not read file."));
		}

		try {
			String transcript = transcriptionService.transcribe(bytes, file.getOriginalFilename());
			String documentId = documentStore.store(transcript);
			return ResponseEntity.ok(Map.of(
					"documentId", documentId,
					"transcript", transcript,
					"textLength", transcript.length()));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}
}

