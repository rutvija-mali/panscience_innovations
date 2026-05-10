package com.example.demo.service;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class PdfExtractionService {

	public String extractText(byte[] pdfBytes) throws IOException {
		try (PDDocument document = Loader.loadPDF(pdfBytes)) {
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(document);
		}
	}

}
