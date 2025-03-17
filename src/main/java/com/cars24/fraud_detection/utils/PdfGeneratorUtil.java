package com.cars24.fraud_detection.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;

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
                document.add(new Paragraph("\nCall ID: " + call.get("id").asText()).setBold());

                if (call.has("transcript") && call.get("transcript").isArray()) {
                    ArrayNode transcriptArray = (ArrayNode) call.get("transcript");
                    String transcriptText = String.join("\n",
                            StreamSupport.stream(transcriptArray.spliterator(), false)
                                    .map(JsonNode::asText)
                                    .collect(Collectors.toList()));

                    // Bold heading
                    document.add(new Paragraph("Transcripts:").setBold());

                    // Normal font for transcript text
                    PdfFont normalFont = PdfFontFactory.createFont();
                    document.add(new Paragraph(transcriptText).setFont(normalFont));
                } else {
                    // Bold heading
                    document.add(new Paragraph("Transcripts:").setBold());

                    // Normal font for "Not Available"
                    PdfFont normalFont = PdfFontFactory.createFont();
                    document.add(new Paragraph("Not Available").setFont(normalFont));
                }

                // Bold heading for Extracted Details
                document.add(new Paragraph("\nExtracted Details:").setBold());

                PdfFont normalFont = PdfFontFactory.createFont();  // Reset to normal font
                document.add(new Paragraph("Reference Name: " + call.get("referenceName").asText()).setFont(normalFont));
                document.add(new Paragraph("Subject Name: " + call.get("subjectName").asText()).setFont(normalFont));
                document.add(new Paragraph("Subject Address: " + call.get("subjectAddress").asText()).setFont(normalFont));
                document.add(new Paragraph("Relation to Subject: " + call.get("relationToSubject").asText()).setFont(normalFont));
                document.add(new Paragraph("Subject Occupation: " + call.get("subjectOccupation").asText()).setFont(normalFont));

                document.add(new Paragraph("\nOverall Score: " + call.get("overallScore").asText()).setBold());

                // Bold heading for Explanation
                if (call.has("explanation")) {
                    document.add(new Paragraph("Explanation:").setBold());
                    document.add(new Paragraph(call.get("explanation").asText()).setFont(normalFont));
                }

                // Bold heading for Field-by-Field Scores
                if (call.has("fieldByFieldScores")) {
                    document.add(new Paragraph("Field-by-Field Scores:").setBold());
                    document.add(new Paragraph(call.get("fieldByFieldScores").toString()).setFont(normalFont));
                }

                // Bold heading for Audio Analysis
                if (call.has("audioAnalysis")) {
                    document.add(new Paragraph("Audio Analysis:").setBold());
                    document.add(new Paragraph(call.get("audioAnalysis").get("output").asText()).setFont(normalFont));
                }

                document.add(new Paragraph("Status: " + call.get("status").asText()).setBold());
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
