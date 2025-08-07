package com.webscraper.app.dto;

import javax.swing.*;

public class ImageResult {
    final ImageIcon icon;
    final String url;
    final boolean success;

    public ImageResult(ImageIcon icon, String url, boolean success) {
        this.icon = icon;
        this.url = url;
        this.success = success;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSuccess() {
        return success;
    }
}
