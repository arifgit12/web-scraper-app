package com.webscraper.app.dto;

public class LinkItem {
    private final String title;
    private final String url;

    public LinkItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return title;
    }
}
