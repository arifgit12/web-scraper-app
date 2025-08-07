package com.webscraper.app.dto;

import java.util.List;

public class ArticleContent {
    private final String title;
    private final String text;
    private final List<String> imageUrls;

    public ArticleContent(String title, String text, List<String> imageUrls) {
        this.title = title;
        this.text = text;
        this.imageUrls = imageUrls;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}
