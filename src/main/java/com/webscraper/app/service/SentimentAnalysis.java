package com.webscraper.app.service;

import java.util.List;

public class SentimentAnalysis {
    private final String label; // Positive, Negative, Neutral
    private final double score; // -1.0 to 1.0
    private final List<String> keywords;
    private final List<String> positiveWords;
    private final List<String> negativeWords;

    public SentimentAnalysis(String label, double score, List<String> keywords,
                             List<String> positiveWords, List<String> negativeWords) {
        this.label = label;
        this.score = score;
        this.keywords = keywords;
        this.positiveWords = positiveWords;
        this.negativeWords = negativeWords;
    }

    public String getLabel() { return label; }
    public double getScore() { return score; }
    public List<String> getKeywords() { return keywords; }
    public List<String> getPositiveWords() { return positiveWords; }
    public List<String> getNegativeWords() { return negativeWords; }
}
