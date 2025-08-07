package com.webscraper.app;

import com.webscraper.app.dto.ArticleContent;
import com.webscraper.app.dto.DetailedArticle;
import com.webscraper.app.dto.ImageResult;
import com.webscraper.app.dto.LinkItem;
import com.webscraper.app.service.SentimentAnalysis;
import com.webscraper.app.service.WebScraperService;
import org.springframework.boot.CommandLineRunner;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

@org.springframework.stereotype.Component
class WebScraperGUI implements CommandLineRunner {

    private JFrame frame;
    private JTabbedPane tabbedPane;

    // Tab 1: Link Scraper
    private JTextField urlField;
    private JList<LinkItem> linkList;
    private DefaultListModel<LinkItem> listModel;
    private JTextArea contentArea;
    private JScrollPane imagePanel;
    private JPanel imagesContainer;

    // Tab 2: Article Analyzer
    private JTextField articleUrlField;
    private JLabel headlineLabel;
    private JLabel authorLabel;
    private JLabel dateLabel;
    private JLabel sentimentLabel;
    private JTextArea articleContentArea;
    private JPanel articleImagesContainer;
    private JScrollPane articleImagePanel;

    private WebScraperService scraperService;

    public WebScraperGUI() {
        this.scraperService = new WebScraperService();
    }

    @Override
    public void run(String... args) {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        frame = new JFrame("Web Scraper - Spring Boot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        // Create tabbed pane
        tabbedPane = new JTabbedPane();

        // Tab 1: Link Scraper
        JPanel linkScraperPanel = createLinkScraperPanel();
        tabbedPane.addTab("Website Link Scraper", linkScraperPanel);

        // Tab 2: Article Analyzer
        JPanel articleAnalyzerPanel = createArticleAnalyzerPanel();
        tabbedPane.addTab("Article Analyzer", articleAnalyzerPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JLabel statusBar = new JLabel("Tab 1: Scrape links from news websites | Tab 2: Analyze individual articles with sentiment analysis");
        frame.add(statusBar, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createLinkScraperPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top panel for URL input
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Enter Website URL"));

        urlField = new JTextField("https://www.bbc.com/");
        JButton scrapeButton = new JButton("Get Links");

        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(scrapeButton, BorderLayout.EAST);

        // Left panel for links list
        listModel = new DefaultListModel<>();
        linkList = new JList<>(listModel);
        linkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        linkList.setCellRenderer(new LinkCellRenderer());

        JScrollPane linkScrollPane = new JScrollPane(linkList);
        linkScrollPane.setPreferredSize(new Dimension(400, 600));
        linkScrollPane.setBorder(BorderFactory.createTitledBorder("Latest News Articles"));

        // Right panel for content display
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Text content area
        contentArea = new JTextArea();
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setEditable(false);
        contentArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JScrollPane textScrollPane = new JScrollPane(contentArea);
        textScrollPane.setPreferredSize(new Dimension(600, 300));
        textScrollPane.setBorder(BorderFactory.createTitledBorder("Article Content"));

        // Images panel
        imagesContainer = new JPanel();
        imagesContainer.setLayout(new BoxLayout(imagesContainer, BoxLayout.Y_AXIS));
        imagePanel = new JScrollPane(imagesContainer);
        imagePanel.setPreferredSize(new Dimension(600, 300));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Images"));

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScrollPane, imagePanel);
        rightSplitPane.setDividerLocation(400);

        contentPanel.add(rightSplitPane, BorderLayout.CENTER);

        // Main split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, linkScrollPane, contentPanel);
        mainSplitPane.setDividerLocation(400);

        // Event listeners
        scrapeButton.addActionListener(new ScrapeButtonListener());
        linkList.addListSelectionListener(new LinkSelectionListener());

        // Add components to panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(mainSplitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createArticleAnalyzerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Top panel for article URL input
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Enter Article URL for Detailed Analysis (works with most news sites)"));

        articleUrlField = new JTextField("https://www.bbc.com/news/world-asia-india-12345678");
        JButton analyzeButton = new JButton("Analyze Article");

        topPanel.add(articleUrlField, BorderLayout.CENTER);
        topPanel.add(analyzeButton, BorderLayout.EAST);

        // Article details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Article Details"));

        // Create labels for article metadata
        headlineLabel = new JLabel("Headline: Not analyzed yet");
        headlineLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        authorLabel = new JLabel("Author: Not analyzed yet");
        authorLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        dateLabel = new JLabel("Date: Not analyzed yet");
        dateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        sentimentLabel = new JLabel("Sentiment: Not analyzed yet");
        sentimentLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        detailsPanel.add(headlineLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(authorLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(dateLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(sentimentLabel);
        detailsPanel.add(Box.createVerticalStrut(10));

        // Add helper text
        JLabel helpLabel = new JLabel("<html><small>Supported sites: BBC, CNN, Reuters, Guardian, etc.<br>" +
                "Telegraph India may block automated requests.</small></html>");
        helpLabel.setForeground(Color.GRAY);
        detailsPanel.add(helpLabel);
        detailsPanel.add(Box.createVerticalStrut(10));

        // Article content area
        articleContentArea = new JTextArea();
        articleContentArea.setWrapStyleWord(true);
        articleContentArea.setLineWrap(true);
        articleContentArea.setEditable(false);
        articleContentArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JScrollPane articleTextScrollPane = new JScrollPane(articleContentArea);
        articleTextScrollPane.setPreferredSize(new Dimension(700, 400));
        articleTextScrollPane.setBorder(BorderFactory.createTitledBorder("Article Content"));

        // Article images panel
        articleImagesContainer = new JPanel();
        articleImagesContainer.setLayout(new BoxLayout(articleImagesContainer, BoxLayout.Y_AXIS));
        articleImagePanel = new JScrollPane(articleImagesContainer);
        articleImagePanel.setPreferredSize(new Dimension(700, 200));
        articleImagePanel.setBorder(BorderFactory.createTitledBorder("Article Images"));

        // Create main content panel
        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, articleTextScrollPane, articleImagePanel);
        contentSplitPane.setDividerLocation(400);

        // Left panel for details, right panel for content
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailsPanel, contentSplitPane);
        mainSplitPane.setDividerLocation(350);

        // Event listener
        analyzeButton.addActionListener(new AnalyzeArticleListener());

        // Add components to panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(mainSplitPane, BorderLayout.CENTER);

        return panel;
    }

    private class ScrapeButtonListener implements ActionListener {
        private SwingWorker<List<LinkItem>, String> currentWorker;

        @Override
        public void actionPerformed(ActionEvent e) {
            String url = urlField.getText().trim();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid URL", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Cancel previous operation if running
            if (currentWorker != null && !currentWorker.isDone()) {
                currentWorker.cancel(true);
            }

            // Clear previous results
            listModel.clear();
            contentArea.setText("");
            imagesContainer.removeAll();
            imagesContainer.revalidate();
            imagesContainer.repaint();

            // Show loading message
            contentArea.setText("Connecting to " + url +
                    "...\nSearching for latest news articles and stories...\nThis may take a few seconds.");

            // Disable button during operation
            JButton button = (JButton) e.getSource();
            button.setEnabled(false);
            button.setText("Loading...");

            // Run scraping in background thread
            currentWorker = new SwingWorker<>() {
                @Override
                protected List<LinkItem> doInBackground() throws Exception {
                    publish("Connecting to website...");
                    List<LinkItem> links = scraperService.extractLinks(url);
                    publish("Processing " + links.size() + " links...");
                    return links;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String message : chunks) {
                        contentArea.setText(message);
                    }
                }

                @Override
                protected void done() {
                    // Re-enable button
                    button.setEnabled(true);
                    button.setText("Get Links");

                    try {
                        if (isCancelled()) {
                            contentArea.setText("Operation cancelled.");
                            return;
                        }

                        List<LinkItem> links = get();
                        if (links.isEmpty()) {
                            contentArea.setText("No links found on this page. Please try a different URL.");
                        } else {
                            for (LinkItem link : links) {
                                listModel.addElement(link);
                            }
                            contentArea.setText("Found " + links.size() + " links. Select a link to view its content.");
                        }
                    } catch (Exception ex) {
                        String errorMsg = "Error loading links: " + ex.getMessage();
                        contentArea.setText(errorMsg);
                        System.err.println("Scraping error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            };
            currentWorker.execute();
        }
    }

    private class AnalyzeArticleListener implements ActionListener {
        private SwingWorker<DetailedArticle, String> currentWorker;

        @Override
        public void actionPerformed(ActionEvent e) {
            String url = articleUrlField.getText().trim();
            if (url.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid article URL",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Cancel previous operation if running
            if (currentWorker != null && !currentWorker.isDone()) {
                currentWorker.cancel(true);
            }

            // Clear previous results
            headlineLabel.setText("Headline: Analyzing...");
            authorLabel.setText("Author: Analyzing...");
            dateLabel.setText("Date: Analyzing...");
            sentimentLabel.setText("Sentiment: Analyzing...");
            articleContentArea.setText("");
            articleImagesContainer.removeAll();
            articleImagesContainer.revalidate();
            articleImagesContainer.repaint();

            // Disable button during operation
            JButton button = (JButton) e.getSource();
            button.setEnabled(false);
            button.setText("Analyzing...");

            // Run analysis in background thread
            currentWorker = new SwingWorker<DetailedArticle, String>() {
                @Override
                protected DetailedArticle doInBackground() throws Exception {
                    publish("Connecting to article...");
                    DetailedArticle article = scraperService.extractDetailedArticle(url);
                    publish("Analyzing sentiment...");
                    return article;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String message : chunks) {
                        articleContentArea.setText(message);
                    }
                }

                @Override
                protected void done() {
                    // Re-enable button
                    button.setEnabled(true);
                    button.setText("Analyze Article");

                    try {
                        if (isCancelled()) {
                            articleContentArea.setText("Analysis cancelled.");
                            return;
                        }

                        DetailedArticle article = get();
                        displayDetailedArticle(article);

                    } catch (Exception ex) {
                        String errorMsg = "Error analyzing article: " + ex.getMessage();
                        articleContentArea.setText(errorMsg);
                        headlineLabel.setText("Headline: Analysis failed");
                        authorLabel.setText("Author: Analysis failed");
                        dateLabel.setText("Date: Analysis failed");
                        sentimentLabel.setText("Sentiment: Analysis failed");
                        System.err.println("Article analysis error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            };
            currentWorker.execute();
        }
    }


    private void displayDetailedArticle(DetailedArticle article) {
        // Display article metadata
        headlineLabel.setText("Headline: " + (article.getHeadline().length() > 80 ?
                article.getHeadline().substring(0, 77) + "..." : article.getHeadline()));
        headlineLabel.setToolTipText(article.getHeadline()); // Full headline on hover

        authorLabel.setText("Author: " + article.getAuthor());
        dateLabel.setText("Date: " + article.getPublishDate());

        // Display sentiment with color coding
        SentimentAnalysis sentiment = article.getSentiment();
        String sentimentText = "Sentiment: " + sentiment.getLabel() + " (Score: " +
                String.format("%.2f", sentiment.getScore()) + ")";
        sentimentLabel.setText(sentimentText);

        // Color code sentiment
        if (sentiment.getLabel().equals("Positive")) {
            sentimentLabel.setForeground(Color.GREEN.darker());
        } else if (sentiment.getLabel().equals("Negative")) {
            sentimentLabel.setForeground(Color.RED.darker());
        } else {
            sentimentLabel.setForeground(Color.BLUE);
        }

        // Display article content
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("=== ARTICLE ANALYSIS ===\n\n");
        contentBuilder.append("HEADLINE: ").append(article.getHeadline()).append("\n\n");
        contentBuilder.append("AUTHOR: ").append(article.getAuthor()).append("\n");
        contentBuilder.append("PUBLISHED: ").append(article.getPublishDate()).append("\n");
        contentBuilder.append("SENTIMENT: ").append(sentiment.getLabel())
                .append(" (").append(String.format("%.2f", sentiment.getScore())).append(")\n");
        contentBuilder.append("WORD COUNT: ").append(article.getWordCount()).append(" words\n\n");
        contentBuilder.append("=== CONTENT ===\n\n");
        contentBuilder.append(article.getContent());

        if (!article.getSentiment().getKeywords().isEmpty()) {
            contentBuilder.append("\n\n=== SENTIMENT KEYWORDS ===\n");
            contentBuilder.append("Positive: ").append(String.join(", ", sentiment.getPositiveWords())).append("\n");
            contentBuilder.append("Negative: ").append(String.join(", ", sentiment.getNegativeWords())).append("\n");
        }

        articleContentArea.setText(contentBuilder.toString());
        articleContentArea.setCaretPosition(0);

        // Display images
        displayArticleImages(article.getImageUrls());
    }

    private void displayArticleImages(List<String> imageUrls) {
        articleImagesContainer.removeAll();

        if (imageUrls.isEmpty()) {
            JLabel noImagesLabel = new JLabel("No images found in this article.");
            noImagesLabel.setForeground(Color.GRAY);
            articleImagesContainer.add(noImagesLabel);
        } else {
            JLabel loadingLabel = new JLabel("Loading " + imageUrls.size() + " images from article...");
            loadingLabel.setForeground(Color.BLUE);
            articleImagesContainer.add(loadingLabel);

            // Load images in background
            SwingWorker<Void, ImageResult> imageWorker = new SwingWorker<Void, ImageResult>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (String imageUrl : imageUrls) {
                        if (isCancelled()) break;

                        try {
                            ImageIcon icon = loadImageIcon(imageUrl);
                            if (icon != null) {
                                publish(new ImageResult(icon, imageUrl, true));
                            } else {
                                publish(new ImageResult(null, imageUrl, false));
                            }
                        } catch (Exception e) {
                            publish(new ImageResult(null, imageUrl, false));
                        }

                        Thread.sleep(300); // Small delay between images
                    }
                    return null;
                }

                @Override
                protected void process(List<ImageResult> chunks) {
                    // Remove loading label on first image
                    if (articleImagesContainer.getComponentCount() > 0 &&
                            articleImagesContainer.getComponent(0) == loadingLabel) {
                        articleImagesContainer.remove(loadingLabel);
                    }

                    for (ImageResult result : chunks) {
                        if (result.isSuccess() && result.getIcon() != null) {
                            JPanel imagePanel = new JPanel(new BorderLayout());
                            imagePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                            JLabel imageLabel = new JLabel(result.getIcon());
                            imagePanel.add(imageLabel, BorderLayout.CENTER);

                            articleImagesContainer.add(imagePanel);
                            articleImagesContainer.add(Box.createVerticalStrut(10));
                        }
                    }
                    articleImagesContainer.revalidate();
                    articleImagesContainer.repaint();
                }

                @Override
                protected void done() {
                    if (articleImagesContainer.getComponentCount() == 0) {
                        JLabel noImagesLabel = new JLabel("Could not load any images from this article.");
                        noImagesLabel.setForeground(Color.GRAY);
                        articleImagesContainer.add(noImagesLabel);
                    }
                    articleImagesContainer.revalidate();
                    articleImagesContainer.repaint();
                }
            };
            imageWorker.execute();
        }

        articleImagesContainer.revalidate();
        articleImagesContainer.repaint();
    }
    private class LinkSelectionListener implements ListSelectionListener {
        private SwingWorker<ArticleContent, String> currentContentWorker;

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) return;

            LinkItem selectedLink = linkList.getSelectedValue();
            if (selectedLink != null) {
                // Cancel previous content loading if running
                if (currentContentWorker != null && !currentContentWorker.isDone()) {
                    currentContentWorker.cancel(true);
                }

                contentArea.setText("Loading content from: " + selectedLink.getTitle() + "...");
                imagesContainer.removeAll();
                imagesContainer.revalidate();
                imagesContainer.repaint();

                // Load content in background
                currentContentWorker = new SwingWorker<ArticleContent, String>() {
                    @Override
                    protected ArticleContent doInBackground() throws Exception {
                        publish("Fetching content...");
                        return scraperService.extractContent(selectedLink.getUrl());
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        for (String message : chunks) {
                            contentArea.setText(message);
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            if (isCancelled()) {
                                contentArea.setText("Content loading cancelled.");
                                return;
                            }

                            ArticleContent content = get();
                            displayContent(content);
                        } catch (Exception ex) {
                            contentArea.setText("Error loading content from " + selectedLink.getTitle() + ": " + ex.getMessage());
                            System.err.println("Content loading error: " + ex.getMessage());
                        }
                    }
                };
                currentContentWorker.execute();
            }
        }
    }

    private void displayContent(ArticleContent content) {
        // Display text content
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("Title: ").append(content.getTitle()).append("\n\n");
        textBuilder.append("Content:\n").append(content.getText());
        contentArea.setText(textBuilder.toString());
        contentArea.setCaretPosition(0);

        // Display images asynchronously
        imagesContainer.removeAll();

        if (content.getImageUrls().isEmpty()) {
            JLabel noImagesLabel = new JLabel("No images found on this page.");
            noImagesLabel.setForeground(Color.GRAY);
            imagesContainer.add(noImagesLabel);
        } else {
            JLabel loadingLabel = new JLabel("Loading " + content.getImageUrls().size() + " images...");
            imagesContainer.add(loadingLabel);

            // Load images in background to prevent hanging
            SwingWorker<Void, ImageIcon> imageWorker = new SwingWorker<Void, ImageIcon>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (String imageUrl : content.getImageUrls()) {
                        if (isCancelled()) break;

                        try {
                            ImageIcon icon = loadImageIcon(imageUrl);
                            if (icon != null) {
                                publish(icon);
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to load image: " + imageUrl + " - " + e.getMessage());
                        }
                    }
                    return null;
                }

                @Override
                protected void process(List<ImageIcon> chunks) {
                    // Remove loading label on first image
                    if (imagesContainer.getComponentCount() > 0 &&
                            imagesContainer.getComponent(0) == loadingLabel) {
                        imagesContainer.remove(loadingLabel);
                    }

                    for (ImageIcon icon : chunks) {
                        JLabel imageLabel = new JLabel(icon);
                        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                        imagesContainer.add(imageLabel);
                        imagesContainer.add(Box.createVerticalStrut(5));
                    }
                    imagesContainer.revalidate();
                    imagesContainer.repaint();
                }

                @Override
                protected void done() {
                    // Remove loading label if it's still there
                    for (int i = 0; i < imagesContainer.getComponentCount(); i++) {
                        if (imagesContainer.getComponent(i) == loadingLabel) {
                            imagesContainer.remove(loadingLabel);
                            break;
                        }
                    }

                    if (imagesContainer.getComponentCount() == 0) {
                        JLabel noImagesLabel = new JLabel("Could not load any images.");
                        noImagesLabel.setForeground(Color.GRAY);
                        imagesContainer.add(noImagesLabel);
                    }

                    imagesContainer.revalidate();
                    imagesContainer.repaint();
                }
            };
            imageWorker.execute();
        }

        imagesContainer.revalidate();
        imagesContainer.repaint();
    }

    private ImageIcon loadImageIcon(String imageUrl) {
        try {
            System.out.println("Loading image: " + imageUrl);
            URL url = new URL(imageUrl);

            // Create connection with proper headers and timeouts
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000); // 5 seconds
            connection.setReadTimeout(8000); // 8 seconds
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", "image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            connection.setRequestProperty("Referer", imageUrl.substring(0, imageUrl.indexOf('/', 8))); // Use domain as referer
            connection.setInstanceFollowRedirects(true);

            // Handle HTTPS certificates more leniently
            if (connection instanceof javax.net.ssl.HttpsURLConnection) {
                javax.net.ssl.HttpsURLConnection httpsConnection = (javax.net.ssl.HttpsURLConnection) connection;

                // Create a trust manager that accepts all certificates (for scraping purposes)
                javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[] {
                        new javax.net.ssl.X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() { return null; }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                        }
                };

                try {
                    javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    httpsConnection.setSSLSocketFactory(sc.getSocketFactory());
                    httpsConnection.setHostnameVerifier((hostname, session) -> true);
                } catch (Exception e) {
                    System.err.println("SSL setup failed for " + imageUrl + ": " + e.getMessage());
                }
            }

            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("HTTP " + responseCode + " for image: " + imageUrl);
                return null;
            }

            // Get content type
            String contentType = connection.getContentType();
            if (contentType != null && !contentType.toLowerCase().startsWith("image/")) {
                System.err.println("Invalid content type for " + imageUrl + ": " + contentType);
                return null;
            }

            // Read image using ImageIO
            Image image = ImageIO.read(connection.getInputStream());
            connection.disconnect();

            if (image != null && image.getWidth(null) > 0 && image.getHeight(null) > 0) {
                // Scale image to fit - make them smaller for better performance
                int maxWidth = 300;
                int maxHeight = 200;

                int originalWidth = image.getWidth(null);
                int originalHeight = image.getHeight(null);

                // Skip very small images (likely icons or ads)
                if (originalWidth < 100 || originalHeight < 100) {
                    System.out.println("Skipping small image: " + originalWidth + "x" + originalHeight);
                    return null;
                }

                // Calculate scaling
                double scaleX = (double) maxWidth / originalWidth;
                double scaleY = (double) maxHeight / originalHeight;
                double scale = Math.min(scaleX, scaleY);

                if (scale < 1.0) {
                    int scaledWidth = (int) (originalWidth * scale);
                    int scaledHeight = (int) (originalHeight * scale);
                    Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    System.out.println("Loaded and scaled image: " + imageUrl + " (" + scaledWidth + "x" + scaledHeight + ")");
                    return new ImageIcon(scaledImage);
                } else {
                    System.out.println("Loaded image: " + imageUrl + " (" + originalWidth + "x" + originalHeight + ")");
                    return new ImageIcon(image);
                }
            } else {
                System.err.println("Failed to decode image: " + imageUrl);
            }
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Timeout loading image: " + imageUrl);
        } catch (java.net.UnknownHostException e) {
            System.err.println("Unknown host for image: " + imageUrl);
        } catch (javax.net.ssl.SSLException e) {
            System.err.println("SSL error loading image: " + imageUrl + " - " + e.getMessage());
        } catch (java.io.IOException e) {
            System.err.println("IO error loading image: " + imageUrl + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error loading image: " + imageUrl + " - " + e.getMessage());
        }
        return null;
    }
}

// Custom cell renderer for links
class LinkCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof LinkItem) {
            LinkItem link = (LinkItem) value;
            setText(link.getTitle());
            setToolTipText(link.getUrl());
        }
        return this;
    }
}
