package com.example.demo.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class DocumentStore {

	private final Map<String, String> idToText = new ConcurrentHashMap<>();

	public String store(String extractedText) {
		String id = UUID.randomUUID().toString();
		idToText.put(id, extractedText);
		return id;
	}

	public String get(String documentId) {
		return idToText.get(documentId);
	}

}
