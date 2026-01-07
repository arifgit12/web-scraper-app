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
                article.getHeadline(),
                article.getAuthor(),
                article.getPublishDate(),
                article.getWordCount(),
                article.getSentiment().getLabel(),
                String.format("%.2f", article.getSentiment().getScore()),
                String.join(", ", article.getSentiment().getPositiveWords()),
                String.join(", ", article.getSentiment().getNegativeWords())
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
                    article.getHeadline(),
                    article.getAuthor(),
                    article.getPublishDate(),
                    article.getWordCount(),
                    article.getSentiment().getLabel(),
                    String.format("%.2f", article.getSentiment().getScore()),
                    String.join(", ", article.getSentiment().getPositiveWords()),
                    String.join(", ", article.getSentiment().getNegativeWords())
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
                    .add(article.getHeadline()));
            
            // Metadata
            document.add(new Paragraph(new Text("Author: ").setBold())
                    .add(article.getAuthor()));
            document.add(new Paragraph(new Text("Published: ").setBold())
                    .add(article.getPublishDate()));
            document.add(new Paragraph(new Text("Word Count: ").setBold())
                    .add(String.valueOf(article.getWordCount())));
            
            // Sentiment Analysis
            document.add(new Paragraph("\nSentiment Analysis")
                    .setFontSize(16)
                    .setBold());
            document.add(new Paragraph(new Text("Overall Sentiment: ").setBold())
                    .add(article.getSentiment().getLabel() + " (" + 
                         String.format("%.2f", article.getSentiment().getScore()) + ")"));
            
            if (!article.getSentiment().getPositiveWords().isEmpty()) {
                document.add(new Paragraph(new Text("Positive Keywords: ").setBold())
                        .add(String.join(", ", article.getSentiment().getPositiveWords())));
            }
            
            if (!article.getSentiment().getNegativeWords().isEmpty()) {
                document.add(new Paragraph(new Text("Negative Keywords: ").setBold())
                        .add(String.join(", ", article.getSentiment().getNegativeWords())));
            }
            
            // Content
            document.add(new Paragraph("\nArticle Content")
                    .setFontSize(16)
                    .setBold());
            document.add(new Paragraph(article.getContent()));
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
                        .add(article.getHeadline()));
                
                // Metadata
                document.add(new Paragraph(new Text("Author: ").setBold())
                        .add(article.getAuthor()));
                document.add(new Paragraph(new Text("Published: ").setBold())
                        .add(article.getPublishDate()));
                document.add(new Paragraph(new Text("Sentiment: ").setBold())
                        .add(article.getSentiment().getLabel() + " (" + 
                             String.format("%.2f", article.getSentiment().getScore()) + ")"));
                
                // Keywords
                if (!article.getSentiment().getPositiveWords().isEmpty() || 
                    !article.getSentiment().getNegativeWords().isEmpty()) {
                    document.add(new Paragraph(new Text("Keywords: ").setBold())
                            .add("Positive: " + String.join(", ", article.getSentiment().getPositiveWords()) +
                                 " | Negative: " + String.join(", ", article.getSentiment().getNegativeWords())));
                }
                
                document.add(new Paragraph("\n"));
            }
        }
    }
}
