package com.webscraper.app;

import org.springframework.boot.CommandLineRunner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@org.springframework.stereotype.Component
class WebScraperGUI implements CommandLineRunner {

    private JFrame frame;
    private JTextField urlField;
    private JList<LinkItem> linkList;
    private DefaultListModel<LinkItem> listModel;
    private JTextArea contentArea;
    private JScrollPane imagePanel;
    private JPanel imagesContainer;
    private WebScraperService scraperService;

    public WebScraperGUI() {
        this.scraperService = new WebScraperService();
    }

    @Override
    public void run(String... args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Web Scraper - Spring Boot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        // Top panel for URL input
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Enter Website URL"));

        urlField = new JTextField("https://www.thehindu.com/");
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
        linkScrollPane.setBorder(BorderFactory.createTitledBorder("Available Links"));

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

        // Add components to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(mainSplitPane, BorderLayout.CENTER);

        // Status bar
        JLabel statusBar = new JLabel("Ready to scrape...");
        frame.add(statusBar, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
            contentArea.setText("Connecting to " + url + "...\nThis may take a few seconds.");

            // Disable button during operation
            JButton button = (JButton) e.getSource();
            button.setEnabled(false);
            button.setText("Loading...");

            // Run scraping in background thread
            currentWorker = new SwingWorker<List<LinkItem>, String>() {
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

// Data classes
class LinkItem {
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

class ArticleContent {
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

// Service class for web scraping
class WebScraperService {

    public List<LinkItem> extractLinks(String baseUrl) throws IOException {
        List<LinkItem> links = new ArrayList<>();
        Set<String> seenUrls = new HashSet<>();

        try {
            System.out.println("Connecting to: " + baseUrl);

            Document doc = Jsoup.connect(baseUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Sec-Ch-Ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
                    .header("Sec-Ch-Ua-Mobile", "?0")
                    .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .referrer("https://www.google.com/")
                    .timeout(8000) // Increased timeout for better success rate
                    .followRedirects(true)
                    .maxBodySize(1024 * 1024) // 1MB max
                    .ignoreHttpErrors(true) // Don't throw exception on HTTP errors
                    .get();

            // Check if we got a successful response
            int statusCode = doc.connection().response().statusCode();
            if (statusCode == 403) {
                throw new IOException("Access denied (403). The website is blocking automated requests. Try a different website or check if the URL is correct.");
            } else if (statusCode == 404) {
                throw new IOException("Page not found (404). Please check the URL and try again.");
            } else if (statusCode == 500) {
                throw new IOException("Server error (500). The website is experiencing issues. Please try again later.");
            } else if (statusCode >= 400) {
                throw new IOException("HTTP error " + statusCode + ". The website returned an error response.");
            }

            System.out.println("Successfully connected. Status: " + statusCode + ". Parsing links...");

            // Debug: Print some HTML structure info
            System.out.println("Page title: " + doc.title());
            System.out.println("HTML body length: " + doc.body().html().length() + " characters");

            // Get base URI for resolving relative URLs
            String baseUri = doc.baseUri();
            if (baseUri.isEmpty()) {
                baseUri = baseUrl;
            }

            // Try multiple link selectors, prioritizing news content areas
            Elements linkElements = new Elements();
            String[] newsContentSelectors = {
                    // Priority 1: Main news content areas
                    "main a[href]", ".main-content a[href]", ".content a[href]",
                    "article a[href]", ".article a[href]", ".story a[href]",
                    ".news a[href]", ".headlines a[href]", ".latest a[href]",

                    // Priority 2: News-specific sections
                    ".story-card a", ".article-card a", ".news-item a",
                    ".headline a", ".story-headline a", ".article-title a",
                    ".post-title a", ".entry-title a",

                    // Priority 3: Common news website structures
                    "h1 a", "h2 a", "h3 a", ".title a",
                    "[data-module='story'] a", "[data-component='headline'] a",

                    // Priority 4: Fallback to all links
                    "a[href]"
            };

            for (String selector : newsContentSelectors) {
                linkElements = doc.select(selector);
                System.out.println("Selector '" + selector + "' found " + linkElements.size() + " elements");
                if (linkElements.size() > 5) { // Only use if we find a reasonable number
                    System.out.println("Using selector: " + selector);
                    break;
                }
            }

            // If still no good results, try news-specific areas
            if (linkElements.size() < 5) {
                System.out.println("Trying news-specific area selectors...");
                String[] newsAreaSelectors = {
                        ".top-stories a", ".breaking-news a", ".latest-news a",
                        ".featured a", ".trending a", ".popular a",
                        ".homepage a", ".front-page a"
                };

                for (String selector : newsAreaSelectors) {
                    Elements elements = doc.select(selector);
                    if (elements.size() > 0) {
                        linkElements.addAll(elements);
                        System.out.println("Added " + elements.size() + " from " + selector);
                    }
                }
            }

            // Debug: Print first few elements found
            if (!linkElements.isEmpty()) {
                System.out.println("Sample of found elements:");
                for (int i = 0; i < Math.min(3, linkElements.size()); i++) {
                    Element el = linkElements.get(i);
                    System.out.println("  " + i + ": " + el.tagName() + " - " + el.attr("href") + " - " + el.text().substring(0, Math.min(50, el.text().length())));
                }
            }

            int validLinks = 0;
            for (Element link : linkElements) {
                String href = "";

                // Try different ways to get the URL
                if (link.hasAttr("href")) {
                    href = link.attr("abs:href");
                } else if (link.hasAttr("data-href")) {
                    href = link.attr("data-href");
                } else if (link.hasAttr("data-link")) {
                    href = link.attr("data-link");
                }

                // Handle relative URLs manually if needed
                if (!href.startsWith("http") && href.startsWith("/")) {
                    href = baseUrl + href;
                }

                String text = link.text().trim();

                // Filter valid links
                if (!href.isEmpty() && !seenUrls.contains(href) && isValidLink(href, baseUrl)) {
                    seenUrls.add(href);

                    if (text.isEmpty()) {
                        text = link.attr("title");
                        if (text.isEmpty()) {
                            text = link.attr("alt");
                            if (text.isEmpty()) {
                                String urlPart = href.substring(href.lastIndexOf('/') + 1);
                                text = urlPart.isEmpty() ? ("Link " + (validLinks + 1)) : urlPart;
                            }
                        }
                    }

                    // Limit title length
                    if (text.length() > 100) {
                        text = text.substring(0, 97) + "...";
                    }

                    links.add(new LinkItem(text, href));
                    validLinks++;

                    // Limit number of links to focus on latest articles
                    if (links.size() >= 25) { // Reduced from 50 to focus on recent content
                        System.out.println("Limiting to first 25 latest article links");
                        break;
                    }
                }
            }

            System.out.println("Extracted " + validLinks + " valid links");

            if (links.isEmpty()) {
                // Provide more detailed debugging info
                String debugInfo = "Debug information:\n" +
                        "• Page title: " + doc.title() + "\n" +
                        "• HTML body size: " + doc.body().html().length() + " characters\n" +
                        "• Total elements found: " + linkElements.size() + "\n" +
                        "• Base URL: " + baseUrl + "\n" +
                        "• Response status: " + statusCode;

                System.out.println(debugInfo);

                throw new IOException("No valid links found on this page.\n\n" + debugInfo + "\n\n" +
                        "This could mean:\n" +
                        "1. The website uses JavaScript to load content dynamically\n" +
                        "2. The website structure has changed\n" +
                        "3. The website is serving different content to automated requests\n\n" +
                        "Working alternatives: BBC.com, CNN.com, Reuters.com, NBCNews.com");
            }

            return links;

        } catch (IOException e) {
            System.err.println("IOException while connecting to " + baseUrl + ": " + e.getMessage());

            // Provide more specific error messages
            if (e.getMessage().contains("403")) {
                throw new IOException("Access Forbidden (403): The website '" + getDomainFromUrl(baseUrl) + "' is blocking automated requests.\n\n" +
                        "Suggestions:\n" +
                        "• Try websites like: bbc.com, cnn.com, reuters.com, theguardian.com\n" +
                        "• Some news websites block scrapers to protect their content\n" +
                        "• The Hindu works because it allows automated access");
            } else if (e.getMessage().contains("timeout") || e.getMessage().contains("timed out")) {
                throw new IOException("Connection timeout: The website is taking too long to respond.\n" +
                        "Please check your internet connection and try again.");
            } else {
                throw new IOException("Failed to connect to '" + getDomainFromUrl(baseUrl) + "'.\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "Please check:\n" +
                        "• The URL is correct and accessible\n" +
                        "• Your internet connection\n" +
                        "• Try a different website like BBC.com which is known to work");
            }
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            throw new IOException("Unexpected error occurred while accessing '" + getDomainFromUrl(baseUrl) + "': " + e.getMessage());
        }
    }

    private String getDomainFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    private boolean isValidLink(String href, String baseUrl) {
        try {
            if (href == null || href.trim().isEmpty()) {
                return false;
            }

            // Basic URL validation
            if (!href.startsWith("http")) {
                return false;
            }

            URI uri = URI.create(href);
            URI baseUri = URI.create(baseUrl);

            // Check if it's from the same domain or subdomain
            String linkHost = uri.getHost();
            String baseHost = baseUri.getHost();

            if (linkHost == null || baseHost == null) {
                return false;
            }

            // Allow same domain and subdomains
            boolean sameDomain = linkHost.equals(baseHost) || linkHost.endsWith("." + baseHost);

            // Filter out non-article links
            String path = uri.getPath().toLowerCase();
            String fullUrl = href.toLowerCase();

            // Skip common non-article pages
            String[] skipPatterns = {
                    "/about", "/contact", "/privacy", "/terms", "/policy",
                    "/subscribe", "/newsletter", "/advertise", "/jobs", "/careers",
                    "/help", "/support", "/faq", "/sitemap", "/search",
                    "/login", "/register", "/account", "/profile",
                    "/tag/", "/tags/", "/category/", "/author/", "/page/",
                    "/gallery", "/video", "/photos", "/images",
                    "/rss", "/feed", "/xml", "/api/",
                    "facebook.com", "twitter.com", "instagram.com", "youtube.com",
                    "mailto:", "javascript:", "tel:"
            };

            for (String pattern : skipPatterns) {
                if (fullUrl.contains(pattern)) {
                    return false;
                }
            }

            // Prefer URLs that look like news articles
            String[] articlePatterns = {
                    "/news/", "/article/", "/story/", "/post/", "/politics/",
                    "/world/", "/business/", "/sports/", "/technology/", "/health/",
                    "/entertainment/", "/science/", "/opinion/", "/analysis/",
                    "/breaking", "/latest", "/today", "/live"
            };

            boolean looksLikeArticle = false;
            for (String pattern : articlePatterns) {
                if (fullUrl.contains(pattern)) {
                    looksLikeArticle = true;
                    break;
                }
            }

            // Also check for date patterns in URL (common in news sites)
            boolean hasDatePattern = path.matches(".*/(20\\d{2}|\\d{4})/(\\d{1,2}|\\d{2})/(\\d{1,2}|\\d{2})/.*") ||
                    path.matches(".*/(20\\d{2})/(\\d{1,2})/.*");

            return sameDomain &&
                    !href.contains("#") && // Skip anchors
                    !href.toLowerCase().matches(".*\\.(pdf|doc|docx|xls|xlsx|zip|rar|exe|jpg|jpeg|png|gif|mp4|mp3)$") && // Skip files
                    href.length() < 500 && // Skip very long URLs
                    (looksLikeArticle || hasDatePattern || path.length() > 10); // Prefer article-like URLs

        } catch (Exception e) {
            System.err.println("Error validating link " + href + ": " + e.getMessage());
            return false;
        }
    }

    public ArticleContent extractContent(String url) throws IOException {
        try {
            System.out.println("Extracting content from: " + url);

            // Add a small delay to be more respectful to the server
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Cache-Control", "no-cache")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .referrer(url)
                    .timeout(10000) // 10 seconds for content loading
                    .followRedirects(true)
                    .maxBodySize(2 * 1024 * 1024) // 2MB max for content pages
                    .ignoreHttpErrors(true)
                    .get();

            // Check response status
            int statusCode = doc.connection().response().statusCode();
            if (statusCode >= 400) {
                throw new IOException("Failed to load page content (HTTP " + statusCode + ")");
            }

            // Extract title
            String title = doc.title();
            if (title == null || title.trim().isEmpty()) {
                title = "No Title Available";
            }

            // Extract text content with better selectors
            StringBuilder textBuilder = new StringBuilder();

            // Try multiple content selectors in order of preference
            String[] contentSelectors = {
                    "article", ".article", ".content", ".article-content",
                    ".story-content", ".post-content", "main", ".main-content",
                    "[role=main]", ".entry-content", ".post", ".story",
                    ".news-content", ".article-body"
            };

            Elements contentElements = new Elements();
            for (String selector : contentSelectors) {
                contentElements = doc.select(selector);
                if (!contentElements.isEmpty()) {
                    System.out.println("Found content using selector: " + selector);
                    break;
                }
            }

            // Fallback to paragraphs if no main content found
            if (contentElements.isEmpty()) {
                contentElements = doc.select("p");
                System.out.println("Using paragraph fallback, found " + contentElements.size() + " paragraphs");
            }

            for (Element element : contentElements) {
                String text = element.text().trim();
                if (!text.isEmpty() && text.length() > 20) { // Only meaningful paragraphs
                    textBuilder.append(text).append("\n\n");
                }
            }

            String content = textBuilder.toString().trim();
            if (content.isEmpty()) {
                content = "No readable content found on this page. This might be because:\n" +
                        "• The page uses JavaScript to load content\n" +
                        "• The content is protected or behind a paywall\n" +
                        "• The page structure is not recognized by the scraper";
            }

            // Extract images with better filtering for news content
            List<String> imageUrls = new ArrayList<>();

            // Try to find images in content areas first
            Elements contentImages = new Elements();
            String[] imageSelectors = {
                    "article img[src]", ".article img[src]", ".content img[src]",
                    ".story img[src]", ".news-content img[src]",
                    ".post-content img[src]", ".entry-content img[src]",
                    "main img[src]", ".main-content img[src]",
                    "img[src]" // fallback to all images
            };

            for (String selector : imageSelectors) {
                contentImages = doc.select(selector);
                System.out.println("Image selector '" + selector + "' found " + contentImages.size() + " images");
                if (contentImages.size() > 0) {
                    break;
                }
            }

            for (Element img : contentImages) {
                String src = img.attr("abs:src");
                if (!src.isEmpty() && isValidImageUrl(src) && isNewsImage(img, src)) {
                    imageUrls.add(src);
                    System.out.println("Added image: " + src);

                    // Limit number of images to prevent overload
                    if (imageUrls.size() >= 3) { // Reduced to 3 for better performance
                        break;
                    }
                }
            }

            System.out.println("Extracted content: " + content.length() + " characters, " + imageUrls.size() + " images");
            return new ArticleContent(title, content, imageUrls);

        } catch (IOException e) {
            System.err.println("IOException while extracting content from " + url + ": " + e.getMessage());
            throw new IOException("Failed to load page content: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error extracting content: " + e.getMessage());
            throw new IOException("Error processing page content: " + e.getMessage());
        }
    }

    private boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            URI uri = URI.create(url);
            String lowerUrl = url.toLowerCase();

            // Skip common non-content images
            String[] skipPatterns = {
                    "logo", "header", "footer", "nav", "menu", "icon", "avatar",
                    "advertisement", "banner", "sidebar", "widget", "social",
                    "tracking", "pixel", "analytics", "beacon", "1x1", "spacer"
            };

            for (String pattern : skipPatterns) {
                if (lowerUrl.contains(pattern)) {
                    return false;
                }
            }

            return lowerUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|svg).*") &&
                    url.length() < 500 &&
                    !lowerUrl.contains("data:") && // Skip data URLs
                    !lowerUrl.contains("base64"); // Skip base64 images
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNewsImage(Element img, String src) {
        // Check image attributes and context for news relevance
        String alt = img.attr("alt").toLowerCase();
        String className = img.attr("class").toLowerCase();
        String parentClass = img.parent() != null ? img.parent().attr("class").toLowerCase() : "";

        // Skip obvious non-content images
        String[] skipTerms = {
                "logo", "advertisement", "banner", "social", "icon", "avatar",
                "tracking", "pixel", "widget", "sidebar", "nav", "menu", "footer"
        };

        String combinedContext = alt + " " + className + " " + parentClass;
        for (String term : skipTerms) {
            if (combinedContext.contains(term)) {
                return false;
            }
        }

        // Prefer images with meaningful alt text or in content areas
        return alt.length() > 10 ||
                className.contains("content") ||
                className.contains("article") ||
                className.contains("story") ||
                parentClass.contains("content") ||
                parentClass.contains("article") ||
                parentClass.contains("story");
    }
}