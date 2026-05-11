package com.example.demo.web;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.DocumentStore;
import com.example.demo.service.GroqService;
import com.example.demo.service.PdfExtractionService;

@RestController
@RequestMapping("/")
public class PdfChatController {

	private final PdfExtractionService pdfExtractionService;
	private final DocumentStore documentStore;
	private final GroqService groqService;

	public PdfChatController(PdfExtractionService pdfExtractionService, DocumentStore documentStore,
			GroqService groqService) {
		this.pdfExtractionService = pdfExtractionService;
		this.documentStore = documentStore;
		this.groqService = groqService;
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Upload a PDF file."));
		}
		String original = file.getOriginalFilename();
		if (original == null || !original.toLowerCase().endsWith(".pdf")) {
			return ResponseEntity.badRequest().body(Map.of("error", "Only PDF files are accepted."));
		}
		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Could not read file."));
		}
		String text;
		try {
			text = pdfExtractionService.extractText(bytes);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Could not read PDF: " + e.getMessage()));
		}
		if (text == null || text.isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "No text could be extracted from this PDF."));
		}
		String documentId = documentStore.store(text.trim());
		return ResponseEntity.ok(Map.of(
				"documentId", documentId,
				"textLength", text.length()));
	}

	@PostMapping("/ask")
	public ResponseEntity<?> ask(@RequestBody AskRequest req) {
		if (req.documentId() == null || req.documentId().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "documentId is required."));
		}
		if (req.question() == null || req.question().isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "question is required."));
		}
		String docText = documentStore.get(req.documentId());
		if (docText == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "Unknown or expired document. Upload again."));
		}
		try {
			String answer = groqService.answerQuestion(docText, req.question());
			return ResponseEntity.ok(Map.of("answer", answer));
		} catch (IllegalStateException e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}

	public record AskRequest(String documentId, String question) {
	}

}
