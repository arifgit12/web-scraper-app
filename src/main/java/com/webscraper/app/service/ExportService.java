package com.webscraper.app.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.webscraper.app.dto.DetailedArticle;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Service for exporting article analysis results to various formats
 */
public class ExportService {

    /**
     * Export a single article to CSV format
     */
    public void exportToCSV(DetailedArticle article, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Headline", "Author", "Publish Date", "Word Count", 
                                "Sentiment", "Sentiment Score", "Positive Keywords", "Negative Keywords"))) {
            
            csvPrinter.printRecord(
                article.getHeadline() != null ? article.getHeadline() : "",
                article.getAuthor() != null ? article.getAuthor() : "Unknown",
                article.getPublishDate() != null ? article.getPublishDate() : "Unknown",
                article.getWordCount(),
                article.getSentiment() != null ? article.getSentiment().getLabel() : "Unknown",
                article.getSentiment() != null ? String.format("%.2f", article.getSentiment().getScore()) : "0.00",
                article.getSentiment() != null && article.getSentiment().getPositiveWords() != null ? 
                    String.join(", ", article.getSentiment().getPositiveWords()) : "",
                article.getSentiment() != null && article.getSentiment().getNegativeWords() != null ?
                    String.join(", ", article.getSentiment().getNegativeWords()) : ""
            );
        }
    }

    /**
     * Export multiple articles to CSV format (batch export)
     */
    public void exportBatchToCSV(List<DetailedArticle> articles, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("Headline", "Author", "Publish Date", "Word Count", 
                                "Sentiment", "Sentiment Score", "Positive Keywords", "Negative Keywords"))) {
            
            for (DetailedArticle article : articles) {
                csvPrinter.printRecord(
                    article.getHeadline() != null ? article.getHeadline() : "",
                    article.getAuthor() != null ? article.getAuthor() : "Unknown",
                    article.getPublishDate() != null ? article.getPublishDate() : "Unknown",
                    article.getWordCount(),
                    article.getSentiment() != null ? article.getSentiment().getLabel() : "Unknown",
                    article.getSentiment() != null ? String.format("%.2f", article.getSentiment().getScore()) : "0.00",
                    article.getSentiment() != null && article.getSentiment().getPositiveWords() != null ?
                        String.join(", ", article.getSentiment().getPositiveWords()) : "",
                    article.getSentiment() != null && article.getSentiment().getNegativeWords() != null ?
                        String.join(", ", article.getSentiment().getNegativeWords()) : ""
                );
            }
        }
    }

    /**
     * Export a single article to PDF format
     */
    public void exportToPDF(DetailedArticle article, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Title
            Text titleText = new Text("Article Analysis Report\n\n")
                    .setFontSize(20)
                    .setBold();
            Paragraph title = new Paragraph(titleText)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            // Headline
            document.add(new Paragraph(new Text("Headline: ").setBold())
                    .add(article.getHeadline() != null ? article.getHeadline() : "No headline"));
            
            // Metadata
            document.add(new Paragraph(new Text("Author: ").setBold())
                    .add(article.getAuthor() != null ? article.getAuthor() : "Unknown"));
            document.add(new Paragraph(new Text("Published: ").setBold())
                    .add(article.getPublishDate() != null ? article.getPublishDate() : "Unknown"));
            document.add(new Paragraph(new Text("Word Count: ").setBold())
                    .add(String.valueOf(article.getWordCount())));
            
            // Sentiment Analysis
            document.add(new Paragraph("\nSentiment Analysis")
                    .setFontSize(16)
                    .setBold());
            
            if (article.getSentiment() != null) {
                document.add(new Paragraph(new Text("Overall Sentiment: ").setBold())
                        .add(article.getSentiment().getLabel() + " (" + 
                             String.format("%.2f", article.getSentiment().getScore()) + ")"));
                
                if (article.getSentiment().getPositiveWords() != null && !article.getSentiment().getPositiveWords().isEmpty()) {
                    document.add(new Paragraph(new Text("Positive Keywords: ").setBold())
                            .add(String.join(", ", article.getSentiment().getPositiveWords())));
                }
                
                if (article.getSentiment().getNegativeWords() != null && !article.getSentiment().getNegativeWords().isEmpty()) {
                    document.add(new Paragraph(new Text("Negative Keywords: ").setBold())
                            .add(String.join(", ", article.getSentiment().getNegativeWords())));
                }
            }
            
            // Content
            document.add(new Paragraph("\nArticle Content")
                    .setFontSize(16)
                    .setBold());
            document.add(new Paragraph(article.getContent() != null ? article.getContent() : "No content available"));
        }
    }

    /**
     * Export multiple articles to PDF format (batch export)
     */
    public void exportBatchToPDF(List<DetailedArticle> articles, String filePath) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // Title
            Text titleText = new Text("Batch Article Analysis Report\n\n")
                    .setFontSize(20)
                    .setBold();
            Paragraph title = new Paragraph(titleText)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            
            document.add(new Paragraph("Total Articles: " + articles.size())
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));
            
            // Add each article
            for (int i = 0; i < articles.size(); i++) {
                DetailedArticle article = articles.get(i);
                
                // Article separator
                document.add(new Paragraph("Article " + (i + 1) + " of " + articles.size())
                        .setFontSize(14)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("─────────────────────────────────────────────")
                        .setTextAlignment(TextAlignment.CENTER));
                
                // Headline
                document.add(new Paragraph(new Text("Headline: ").setBold())
                        .add(article.getHeadline() != null ? article.getHeadline() : "No headline"));
                
                // Metadata
                document.add(new Paragraph(new Text("Author: ").setBold())
                        .add(article.getAuthor() != null ? article.getAuthor() : "Unknown"));
                document.add(new Paragraph(new Text("Published: ").setBold())
                        .add(article.getPublishDate() != null ? article.getPublishDate() : "Unknown"));
                
                if (article.getSentiment() != null) {
                    document.add(new Paragraph(new Text("Sentiment: ").setBold())
                            .add(article.getSentiment().getLabel() + " (" + 
                                 String.format("%.2f", article.getSentiment().getScore()) + ")"));
                    
                    // Keywords
                    boolean hasPositive = article.getSentiment().getPositiveWords() != null && !article.getSentiment().getPositiveWords().isEmpty();
                    boolean hasNegative = article.getSentiment().getNegativeWords() != null && !article.getSentiment().getNegativeWords().isEmpty();
                    
                    if (hasPositive || hasNegative) {
                        String positiveWords = hasPositive ? String.join(", ", article.getSentiment().getPositiveWords()) : "None";
                        String negativeWords = hasNegative ? String.join(", ", article.getSentiment().getNegativeWords()) : "None";
                        document.add(new Paragraph(new Text("Keywords: ").setBold())
                                .add("Positive: " + positiveWords + " | Negative: " + negativeWords));
                    }
                }
                
                document.add(new Paragraph("\n"));
            }
        }
    }
}
