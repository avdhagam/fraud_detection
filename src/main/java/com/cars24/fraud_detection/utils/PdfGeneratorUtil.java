package com.cars24.fraud_detection.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PdfGeneratorUtil {

    public static byte[] generateUserPdf(JsonNode userData) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Extract Top-Level Data
        document.add(new Paragraph("User Report").setBold().setFontSize(14));
        document.add(new Paragraph("User ID: " + userData.get("id").asText()));
        document.add(new Paragraph("Name: " + userData.get("name").asText()));
        document.add(new Paragraph("Phone: " + userData.get("phone").asText()));

        // Process Audio Calls
        if (userData.has("audioCalls")) {
            document.add(new Paragraph("\nAudio Calls:").setBold());
            for (JsonNode call : userData.get("audioCalls")) {
                document.add(new Paragraph("Call ID: " + call.get("id").asText()));

                if (call.has("transcript") && call.get("transcript").isArray()) {
                    ArrayNode transcriptArray = (ArrayNode) call.get("transcript");
                    String transcriptText = String.join("\n",
                            StreamSupport.stream(transcriptArray.spliterator(), false)
                                    .map(JsonNode::asText)
                                    .collect(Collectors.toList()));

                    document.add(new Paragraph("Transcripts:"));
                    document.add(new Paragraph(transcriptText));
                } else {
                    document.add(new Paragraph("Transcripts: Not Available"));
                }

                document.add(new Paragraph("\nExtracted Details: " ));
                document.add(new Paragraph("Reference Name: " + call.get("referenceName").asText()));
                document.add(new Paragraph("Subject Name: " + call.get("subjectName").asText()));
                document.add(new Paragraph("Subject Address: " + call.get("subjectAddress").asText()));
                document.add(new Paragraph("Relation to Subject: " + call.get("relationToSubject").asText()));
                document.add(new Paragraph("Subject Occupation: " + call.get("subjectOccupation").asText()));
                document.add(new Paragraph("\nOverall Score: " + call.get("overallScore").asText()));

                // Explanation and Field Scores
                if (call.has("explanation")) {
                    document.add(new Paragraph("Explanation: " + call.get("explanation").asText()));
                }
                if (call.has("fieldByFieldScores")) {
                    document.add(new Paragraph("Field-by-Field Scores: " + call.get("fieldByFieldScores").toString()));
                }

                // Audio Analysis
                if (call.has("audioAnalysis")) {
                    document.add(new Paragraph("Audio Analysis: " + call.get("audioAnalysis").get("output").asText()));
                }

                document.add(new Paragraph("Status: " + call.get("status").asText()));
            }
        }

        // Process Documents
        if (userData.has("documents")) {
            document.add(new Paragraph("\nDocuments:").setBold());
            for (JsonNode doc : userData.get("documents")) {
                document.add(new Paragraph("Document Entry ID: " + doc.get("id").asText()));
                document.add(new Paragraph("Document ID: " + doc.get("documentId").asText()));
                document.add(new Paragraph("File Name: " + doc.get("fileName").asText()));
                document.add(new Paragraph("File Path: " + doc.get("filePath").asText()));
                document.add(new Paragraph("Status: " + doc.get("status").asText()));
                document.add(new Paragraph("Remarks: " + doc.get("remarks").asText()));
                document.add(new Paragraph("Final Risk Score: " + doc.get("finalRiskScore").asText()));
                document.add(new Paragraph("Risk Level: " + doc.get("riskLevel").asText()));
                document.add(new Paragraph("Decision: " + doc.get("decision").asText()));
                document.add(new Paragraph("Next Steps: " + doc.get("nextSteps").asText()));

                // OCR, Forgery, and Validation Results
                if (doc.has("ocrResults")) {
                    document.add(new Paragraph("OCR Results: " + doc.get("ocrResults").get("output").asText()));
                }
                if (doc.has("forgeryResults")) {
                    document.add(new Paragraph("Forgery Results: " + doc.get("forgeryResults").get("output").asText()));
                }
                if (doc.has("qualityResults")) {
                    document.add(new Paragraph("Quality Results: " + doc.get("qualityResults").get("output").asText()));
                }
                if (doc.has("validationResults")) {
                    document.add(new Paragraph("Validation Results: " + doc.get("validationResults").get("output").asText()));
                }
            }
        }

        document.close();
        return out.toByteArray();
    }
}
