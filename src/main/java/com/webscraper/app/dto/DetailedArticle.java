package com.webscraper.app.dto;

import com.webscraper.app.service.SentimentAnalysis;

import java.util.List;

public class DetailedArticle {
    private final String headline;
    private final String author;
    private final String publishDate;
    private final String content;
    private final List<String> imageUrls;
    private final SentimentAnalysis sentiment;
    private final int wordCount;

    public DetailedArticle(String headline, String author, String publishDate, String content,
                           List<String> imageUrls, SentimentAnalysis sentiment, int wordCount) {
        this.headline = headline;
        this.author = author;
        this.publishDate = publishDate;
        this.content = content;
        this.imageUrls = imageUrls;
        this.sentiment = sentiment;
        this.wordCount = wordCount;
    }

    public String getHeadline() { return headline; }
    public String getAuthor() { return author; }
    public String getPublishDate() { return publishDate; }
    public String getContent() { return content; }
    public List<String> getImageUrls() { return imageUrls; }
    public SentimentAnalysis getSentiment() { return sentiment; }
    public int getWordCount() { return wordCount; }
}
