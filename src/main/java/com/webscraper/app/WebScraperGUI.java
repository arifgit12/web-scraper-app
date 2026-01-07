package com.webscraper.app;

import com.webscraper.app.dto.ArticleContent;
import com.webscraper.app.dto.DetailedArticle;
import com.webscraper.app.dto.ImageResult;
import com.webscraper.app.dto.LinkItem;
import com.webscraper.app.service.ExportService;
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
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.border.TitledBorder.LEFT;
import static javax.swing.border.TitledBorder.TOP;

@org.springframework.stereotype.Component
class WebScraperGUI implements CommandLineRunner {

    // UI Color Constants
    private static final Color PRIMARY_BLUE = new Color(52, 152, 219);
    private static final Color PRIMARY_GREEN = new Color(46, 204, 113);
    private static final Color BACKGROUND_LIGHT = new Color(236, 240, 241);
    private static final Color BACKGROUND_VERY_LIGHT = new Color(250, 250, 252);
    private static final Color BORDER_COLOR = new Color(189, 195, 199);
    private static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private static final Color TEXT_SECONDARY = new Color(149, 165, 166);
    private static final Color STATUS_BAR_BG = new Color(52, 73, 94);
    private static final Color BUTTON_DISABLED = new Color(127, 140, 141);
    private static final Color SENTIMENT_POSITIVE = new Color(39, 174, 96);
    private static final Color SENTIMENT_NEGATIVE = new Color(231, 76, 60);
    private static final Color SENTIMENT_NEUTRAL = new Color(52, 152, 219);
    
    // UI Font Constants
    private static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    private static final Font FONT_LABEL = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final Font FONT_TEXT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    private static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    private static final Font FONT_ITALIC_SMALL = new Font(Font.SANS_SERIF, Font.ITALIC, 12);
    
    // Text Length Constants
    private static final int MAX_HEADLINE_LENGTH = 60;
    private static final int HEADLINE_TRUNCATE_LENGTH = 57;
    
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
    private ExportService exportService;
    
    // Storage for current article and batch analysis
    private DetailedArticle currentArticle;
    private List<DetailedArticle> batchArticles;

    public WebScraperGUI() {
        this.scraperService = new WebScraperService();
        this.exportService = new ExportService();
        this.batchArticles = new ArrayList<>();
    }

    @Override
    public void run(String... args) {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        frame = new JFrame("Web Scraper - Professional News Analysis Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);
        frame.setLayout(new BorderLayout());

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Continue with default look and feel
        }

        // Create tabbed pane with custom styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_TITLE);
        tabbedPane.setBackground(new Color(245, 245, 250));

        // Tab 1: Link Scraper
        JPanel linkScraperPanel = createLinkScraperPanel();
        tabbedPane.addTab("📰 Website Link Scraper", linkScraperPanel);

        // Tab 2: Article Analyzer
        JPanel articleAnalyzerPanel = createArticleAnalyzerPanel();
        tabbedPane.addTab("📊 Article Analyzer", articleAnalyzerPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Status bar with better styling
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(STATUS_BAR_BG);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        JLabel statusBar = new JLabel("💡 Tab 1: Scrape latest news articles | Tab 2: Analyze articles with AI-powered sentiment analysis");
        statusBar.setForeground(Color.WHITE);
        statusBar.setFont(FONT_SMALL);
        statusPanel.add(statusBar, BorderLayout.WEST);
        
        frame.add(statusPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createLinkScraperPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Top panel for URL input with modern styling
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND_LIGHT);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_BLUE),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel urlLabel = new JLabel("🌐 Website URL:");
        urlLabel.setFont(FONT_LABEL);
        urlLabel.setForeground(TEXT_PRIMARY);
        
        urlField = new JTextField("https://www.bbc.com/");
        urlField.setFont(FONT_TEXT);
        urlField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JButton scrapeButton = new JButton("🔍 Get Latest Articles");
        scrapeButton.setFont(FONT_LABEL);
        scrapeButton.setBackground(PRIMARY_BLUE);
        scrapeButton.setForeground(Color.WHITE);
        scrapeButton.setOpaque(true);
        scrapeButton.setBorderPainted(true);
        scrapeButton.setContentAreaFilled(true);
        scrapeButton.setFocusPainted(false);
        scrapeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_BLUE.darker(), 2),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        scrapeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_LIGHT);
        inputPanel.add(urlLabel, BorderLayout.WEST);
        inputPanel.add(urlField, BorderLayout.CENTER);
        
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(scrapeButton, BorderLayout.EAST);

        // Left panel for links list
        listModel = new DefaultListModel<>();
        linkList = new JList<>(listModel);
        linkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        linkList.setCellRenderer(new LinkCellRenderer());
        linkList.setFont(FONT_SMALL);
        linkList.setBackground(BACKGROUND_VERY_LIGHT);
        linkList.setSelectionBackground(PRIMARY_BLUE);
        linkList.setSelectionForeground(Color.WHITE);

        JScrollPane linkScrollPane = new JScrollPane(linkList);
        linkScrollPane.setPreferredSize(new Dimension(450, 600));
        linkScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "📑 Latest News Articles",
            LEFT,
            TOP,
            FONT_LABEL,
            TEXT_PRIMARY
        ));

        // Right panel for content display
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // Text content area
        contentArea = new JTextArea();
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);
        contentArea.setEditable(false);
        contentArea.setFont(FONT_TEXT);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane textScrollPane = new JScrollPane(contentArea);
        textScrollPane.setPreferredSize(new Dimension(700, 300));
        textScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "📄 Article Content",
            LEFT,
            TOP,
            FONT_LABEL,
            TEXT_PRIMARY
        ));

        // Images panel
        imagesContainer = new JPanel();
        imagesContainer.setLayout(new BoxLayout(imagesContainer, BoxLayout.Y_AXIS));
        imagesContainer.setBackground(Color.WHITE);
        imagePanel = new JScrollPane(imagesContainer);
        imagePanel.setPreferredSize(new Dimension(700, 300));
        imagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "🖼️ Article Images",
            LEFT,
            TOP,
            FONT_LABEL,
            TEXT_PRIMARY
        ));

        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScrollPane, imagePanel);
        rightSplitPane.setDividerLocation(420);
        rightSplitPane.setDividerSize(6);

        contentPanel.add(rightSplitPane, BorderLayout.CENTER);

        // Main split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, linkScrollPane, contentPanel);
        mainSplitPane.setDividerLocation(450);
        mainSplitPane.setDividerSize(6);

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
        panel.setBackground(Color.WHITE);

        // Top panel for article URL input with modern styling
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND_LIGHT);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_GREEN),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel articleLabel = new JLabel("🔗 Article URL:");
        articleLabel.setFont(FONT_LABEL);
        articleLabel.setForeground(TEXT_PRIMARY);
        
        articleUrlField = new JTextField("https://www.bbc.com/news/world-asia-india-12345678");
        articleUrlField.setFont(FONT_TEXT);
        articleUrlField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JButton analyzeButton = new JButton("🧠 Analyze with AI");
        analyzeButton.setFont(FONT_LABEL);
        analyzeButton.setBackground(PRIMARY_GREEN);
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setOpaque(true);
        analyzeButton.setBorderPainted(true);
        analyzeButton.setContentAreaFilled(true);
        analyzeButton.setFocusPainted(false);
        analyzeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN.darker(), 2),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        analyzeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(BACKGROUND_LIGHT);
        inputPanel.add(articleLabel, BorderLayout.WEST);
        inputPanel.add(articleUrlField, BorderLayout.CENTER);
        
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(analyzeButton, BorderLayout.EAST);

        // Article details panel with better styling
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(BACKGROUND_VERY_LIGHT);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                "📋 Article Metadata",
                LEFT,
                TOP,
                FONT_LABEL,
                TEXT_PRIMARY
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Create labels for article metadata with professional styling
        JLabel metaIcon = new JLabel("📰");
        metaIcon.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        
        headlineLabel = new JLabel("<html><b>Headline:</b> Ready to analyze</html>");
        headlineLabel.setFont(FONT_SMALL);
        headlineLabel.setForeground(TEXT_PRIMARY);

        authorLabel = new JLabel("<html><b>Author:</b> Ready to analyze</html>");
        authorLabel.setFont(FONT_SMALL);
        authorLabel.setForeground(TEXT_PRIMARY);

        dateLabel = new JLabel("<html><b>Published:</b> Ready to analyze</html>");
        dateLabel.setFont(FONT_SMALL);
        dateLabel.setForeground(TEXT_PRIMARY);

        sentimentLabel = new JLabel("<html><b>Sentiment:</b> Ready to analyze</html>");
        sentimentLabel.setFont(FONT_LABEL);
        sentimentLabel.setForeground(new Color(52, 73, 94));

        // Add separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        detailsPanel.add(metaIcon);
        detailsPanel.add(Box.createVerticalStrut(15));
        detailsPanel.add(headlineLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(authorLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(dateLabel);
        detailsPanel.add(Box.createVerticalStrut(15));
        detailsPanel.add(separator);
        detailsPanel.add(Box.createVerticalStrut(15));
        detailsPanel.add(sentimentLabel);
        detailsPanel.add(Box.createVerticalStrut(20));

        // Add separator
        JSeparator separator2 = new JSeparator();
        separator2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        detailsPanel.add(separator2);
        detailsPanel.add(Box.createVerticalStrut(15));

        // Export buttons
        JLabel exportLabel = new JLabel("📤 Export");
        exportLabel.setFont(FONT_LABEL);
        exportLabel.setForeground(TEXT_PRIMARY);
        detailsPanel.add(exportLabel);
        detailsPanel.add(Box.createVerticalStrut(10));

        JButton exportCSVButton = new JButton("💾 CSV");
        exportCSVButton.setFont(FONT_SMALL);
        exportCSVButton.setBackground(new Color(52, 152, 219));
        exportCSVButton.setForeground(Color.WHITE);
        exportCSVButton.setOpaque(true);
        exportCSVButton.setBorderPainted(true);
        exportCSVButton.setContentAreaFilled(true);
        exportCSVButton.setFocusPainted(false);
        exportCSVButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        exportCSVButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportCSVButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportCSVButton.addActionListener(e -> exportCurrentArticleToCSV());

        JButton exportPDFButton = new JButton("📄 PDF");
        exportPDFButton.setFont(FONT_SMALL);
        exportPDFButton.setBackground(new Color(231, 76, 60));
        exportPDFButton.setForeground(Color.WHITE);
        exportPDFButton.setOpaque(true);
        exportPDFButton.setBorderPainted(true);
        exportPDFButton.setContentAreaFilled(true);
        exportPDFButton.setFocusPainted(false);
        exportPDFButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(192, 57, 43), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        exportPDFButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportPDFButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportPDFButton.addActionListener(e -> exportCurrentArticleToPDF());

        detailsPanel.add(exportCSVButton);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(exportPDFButton);
        detailsPanel.add(Box.createVerticalStrut(15));

        // Batch analysis section
        JSeparator separator3 = new JSeparator();
        separator3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        detailsPanel.add(separator3);
        detailsPanel.add(Box.createVerticalStrut(15));

        JLabel batchLabel = new JLabel("📊 Batch Analysis");
        batchLabel.setFont(FONT_LABEL);
        batchLabel.setForeground(TEXT_PRIMARY);
        detailsPanel.add(batchLabel);
        detailsPanel.add(Box.createVerticalStrut(10));

        JButton addToBatchButton = new JButton("➕ Add to Batch");
        addToBatchButton.setFont(FONT_SMALL);
        addToBatchButton.setBackground(PRIMARY_GREEN);
        addToBatchButton.setForeground(Color.WHITE);
        addToBatchButton.setOpaque(true);
        addToBatchButton.setBorderPainted(true);
        addToBatchButton.setContentAreaFilled(true);
        addToBatchButton.setFocusPainted(false);
        addToBatchButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_GREEN.darker(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        addToBatchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToBatchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToBatchButton.addActionListener(e -> addToBatch());

        JButton exportBatchButton = new JButton("📦 Export Batch");
        exportBatchButton.setFont(FONT_SMALL);
        exportBatchButton.setBackground(new Color(155, 89, 182));
        exportBatchButton.setForeground(Color.WHITE);
        exportBatchButton.setOpaque(true);
        exportBatchButton.setBorderPainted(true);
        exportBatchButton.setContentAreaFilled(true);
        exportBatchButton.setFocusPainted(false);
        exportBatchButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(142, 68, 173), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        exportBatchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportBatchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportBatchButton.addActionListener(e -> exportBatch());

        JButton clearBatchButton = new JButton("🗑️ Clear Batch");
        clearBatchButton.setFont(FONT_SMALL);
        clearBatchButton.setBackground(TEXT_SECONDARY);
        clearBatchButton.setForeground(Color.WHITE);
        clearBatchButton.setOpaque(true);
        clearBatchButton.setBorderPainted(true);
        clearBatchButton.setContentAreaFilled(true);
        clearBatchButton.setFocusPainted(false);
        clearBatchButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_SECONDARY.darker(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        clearBatchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBatchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        clearBatchButton.addActionListener(e -> clearBatch());

        detailsPanel.add(addToBatchButton);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(exportBatchButton);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(clearBatchButton);
        detailsPanel.add(Box.createVerticalStrut(10));

        // Add helper text with modern styling
        JLabel helpLabel = new JLabel("<html><small style='color: #7f8c8d;'>" +
                "✓ Supported: BBC, CNN, Reuters, Guardian<br>" +
                "✗ Limited: Paywalled sites, Telegraph India</small></html>");
        detailsPanel.add(helpLabel);
        detailsPanel.add(Box.createVerticalGlue());

        // Article content area with better styling
        articleContentArea = new JTextArea();
        articleContentArea.setWrapStyleWord(true);
        articleContentArea.setLineWrap(true);
        articleContentArea.setEditable(false);
        articleContentArea.setFont(FONT_TEXT);
        articleContentArea.setBackground(Color.WHITE);
        articleContentArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane articleTextScrollPane = new JScrollPane(articleContentArea);
        articleTextScrollPane.setPreferredSize(new Dimension(750, 400));
        articleTextScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "📝 Article Content & Analysis",
            LEFT,
            TOP,
            FONT_LABEL,
            TEXT_PRIMARY
        ));

        // Article images panel with better styling
        articleImagesContainer = new JPanel();
        articleImagesContainer.setLayout(new BoxLayout(articleImagesContainer, BoxLayout.Y_AXIS));
        articleImagesContainer.setBackground(Color.WHITE);
        articleImagePanel = new JScrollPane(articleImagesContainer);
        articleImagePanel.setPreferredSize(new Dimension(750, 200));
        articleImagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "🖼️ Article Images",
            LEFT,
            TOP,
            FONT_LABEL,
            TEXT_PRIMARY
        ));

        // Create main content panel
        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, articleTextScrollPane, articleImagePanel);
        contentSplitPane.setDividerLocation(420);
        contentSplitPane.setDividerSize(6);

        // Left panel for details, right panel for content
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailsPanel, contentSplitPane);
        mainSplitPane.setDividerLocation(380);
        mainSplitPane.setDividerSize(6);

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

            // Disable button during operation with better feedback
            JButton button = (JButton) e.getSource();
            button.setEnabled(false);
            button.setText("⏳ Loading...");
            button.setBackground(BUTTON_DISABLED);

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
                    // Re-enable button with proper styling
                    button.setEnabled(true);
                    button.setText("🔍 Get Latest Articles");
                    button.setBackground(PRIMARY_BLUE);

                    try {
                        if (isCancelled()) {
                            contentArea.setText("Operation cancelled.");
                            return;
                        }

                        List<LinkItem> links = get();
                        if (links.isEmpty()) {
                            contentArea.setText("❌ No links found on this page. Please try a different URL.\n\n" +
                                    "Suggestion: Try well-known news sites like:\n" +
                                    "• https://www.bbc.com/\n" +
                                    "• https://www.cnn.com/\n" +
                                    "• https://www.reuters.com/");
                        } else {
                            for (LinkItem link : links) {
                                listModel.addElement(link);
                            }
                            contentArea.setText("✅ Successfully found " + links.size() + " news articles!\n\n" +
                                    "Select any article from the list to view its content and images.");
                        }
                    } catch (Exception ex) {
                        String errorMsg = "❌ Error loading links:\n\n" + ex.getMessage() + 
                                "\n\nPlease check your internet connection and try again.";
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

            // Disable button during operation with better feedback
            JButton button = (JButton) e.getSource();
            button.setEnabled(false);
            button.setText("⏳ Analyzing...");
            button.setBackground(BUTTON_DISABLED);

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
                    // Re-enable button with proper styling
                    button.setEnabled(true);
                    button.setText("🧠 Analyze with AI");
                    button.setBackground(PRIMARY_GREEN);

                    try {
                        if (isCancelled()) {
                            articleContentArea.setText("Analysis cancelled.");
                            return;
                        }

                        DetailedArticle article = get();
                        displayDetailedArticle(article);

                    } catch (Exception ex) {
                        String errorMsg = "❌ Error analyzing article:\n\n" + ex.getMessage() + 
                                "\n\nPlease verify the URL is correct and accessible.";
                        articleContentArea.setText(errorMsg);
                        headlineLabel.setText("<html><b>Headline:</b> Analysis failed</html>");
                        authorLabel.setText("<html><b>Author:</b> Analysis failed</html>");
                        dateLabel.setText("<html><b>Published:</b> Analysis failed</html>");
                        sentimentLabel.setText("<html><b>Sentiment:</b> Analysis failed</html>");
                        System.err.println("Article analysis error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            };
            currentWorker.execute();
        }
    }


    private void displayDetailedArticle(DetailedArticle article) {
        // Store the current article for export functionality
        this.currentArticle = article;
        
        // Display article metadata with better formatting
        String headlineText = article.getHeadline();
        if (headlineText.length() > MAX_HEADLINE_LENGTH) {
            headlineLabel.setText("<html><b>Headline:</b><br/>" + 
                    headlineText.substring(0, HEADLINE_TRUNCATE_LENGTH) + "...</html>");
        } else {
            headlineLabel.setText("<html><b>Headline:</b><br/>" + headlineText + "</html>");
        }
        headlineLabel.setToolTipText(article.getHeadline()); // Full headline on hover

        authorLabel.setText("<html><b>Author:</b> " + article.getAuthor() + "</html>");
        dateLabel.setText("<html><b>Published:</b> " + article.getPublishDate() + "</html>");

        // Display sentiment with enhanced color coding and icon
        SentimentAnalysis sentiment = article.getSentiment();
        String sentimentIcon;
        Color sentimentColor;
        
        if (sentiment.getLabel().equals("Positive")) {
            sentimentIcon = "😊";
            sentimentColor = SENTIMENT_POSITIVE;
        } else if (sentiment.getLabel().equals("Negative")) {
            sentimentIcon = "😟";
            sentimentColor = SENTIMENT_NEGATIVE;
        } else {
            sentimentIcon = "😐";
            sentimentColor = SENTIMENT_NEUTRAL;
        }
        
        String sentimentText = "<html><b>Sentiment:</b> " + sentimentIcon + " " + 
                sentiment.getLabel() + " <i>(Score: " +
                String.format("%.2f", sentiment.getScore()) + ")</i></html>";
        sentimentLabel.setText(sentimentText);
        sentimentLabel.setForeground(sentimentColor);

        // Display article content with enhanced formatting
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("═══════════════════════════════════════════════════════════\n");
        contentBuilder.append("                    ARTICLE ANALYSIS REPORT\n");
        contentBuilder.append("═══════════════════════════════════════════════════════════\n\n");
        
        contentBuilder.append("📰 HEADLINE\n");
        contentBuilder.append("─────────────────────────────────────────────────────────────\n");
        contentBuilder.append(article.getHeadline()).append("\n\n");
        
        contentBuilder.append("✍️  METADATA\n");
        contentBuilder.append("─────────────────────────────────────────────────────────────\n");
        contentBuilder.append("Author:    ").append(article.getAuthor()).append("\n");
        contentBuilder.append("Published: ").append(article.getPublishDate()).append("\n");
        contentBuilder.append("Words:     ").append(article.getWordCount()).append(" words\n\n");
        
        contentBuilder.append("💭 SENTIMENT ANALYSIS\n");
        contentBuilder.append("─────────────────────────────────────────────────────────────\n");
        contentBuilder.append("Overall Sentiment: ").append(sentimentIcon).append(" ")
                .append(sentiment.getLabel())
                .append(" (Score: ").append(String.format("%.2f", sentiment.getScore())).append(")\n");

        if (!article.getSentiment().getKeywords().isEmpty()) {
            contentBuilder.append("\n🔑 SENTIMENT KEYWORDS\n");
            contentBuilder.append("─────────────────────────────────────────────────────────────\n");
            if (!sentiment.getPositiveWords().isEmpty()) {
                contentBuilder.append("✅ Positive: ").append(String.join(", ", sentiment.getPositiveWords())).append("\n");
            }
            if (!sentiment.getNegativeWords().isEmpty()) {
                contentBuilder.append("❌ Negative: ").append(String.join(", ", sentiment.getNegativeWords())).append("\n");
            }
        }
        
        contentBuilder.append("\n📄 ARTICLE CONTENT\n");
        contentBuilder.append("═══════════════════════════════════════════════════════════\n\n");
        contentBuilder.append(article.getContent());

        articleContentArea.setText(contentBuilder.toString());
        articleContentArea.setCaretPosition(0);

        // Display images
        displayArticleImages(article.getImageUrls());
    }

    private void displayArticleImages(List<String> imageUrls) {
        articleImagesContainer.removeAll();

        if (imageUrls.isEmpty()) {
            JLabel noImagesLabel = new JLabel("📷 No images found in this article.");
            noImagesLabel.setForeground(TEXT_SECONDARY);
            noImagesLabel.setFont(FONT_ITALIC_SMALL);
            noImagesLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            articleImagesContainer.add(noImagesLabel);
        } else {
            JLabel loadingLabel = new JLabel("⏳ Loading " + imageUrls.size() + " images from article...");
            loadingLabel.setForeground(PRIMARY_BLUE);
            loadingLabel.setFont(FONT_SMALL);
            loadingLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
                            imagePanel.setBackground(Color.WHITE);
                            imagePanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                            ));

                            JLabel imageLabel = new JLabel(result.getIcon());
                            imageLabel.setHorizontalAlignment(JLabel.CENTER);
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
                        JLabel noImagesLabel = new JLabel("📷 Could not load any images from this article.");
                        noImagesLabel.setForeground(TEXT_SECONDARY);
                        noImagesLabel.setFont(FONT_ITALIC_SMALL);
                        noImagesLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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
        textBuilder.append("═══════════════════════════════════════════════════════════\n");
        textBuilder.append("📰 ").append(content.getTitle()).append("\n");
        textBuilder.append("═══════════════════════════════════════════════════════════\n\n");
        textBuilder.append(content.getText());
        contentArea.setText(textBuilder.toString());
        contentArea.setCaretPosition(0);

        // Display images asynchronously
        imagesContainer.removeAll();

        if (content.getImageUrls().isEmpty()) {
            JLabel noImagesLabel = new JLabel("📷 No images found on this page.");
            noImagesLabel.setForeground(TEXT_SECONDARY);
            noImagesLabel.setFont(FONT_ITALIC_SMALL);
            noImagesLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            imagesContainer.add(noImagesLabel);
        } else {
            JLabel loadingLabel = new JLabel("⏳ Loading " + content.getImageUrls().size() + " images...");
            loadingLabel.setForeground(PRIMARY_BLUE);
            loadingLabel.setFont(FONT_SMALL);
            loadingLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
                        JPanel imagePanel = new JPanel(new BorderLayout());
                        imagePanel.setBackground(Color.WHITE);
                        imagePanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER_COLOR, 1),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                        ));
                        
                        JLabel imageLabel = new JLabel(icon);
                        imageLabel.setHorizontalAlignment(JLabel.CENTER);
                        imagePanel.add(imageLabel, BorderLayout.CENTER);
                        
                        imagesContainer.add(imagePanel);
                        imagesContainer.add(Box.createVerticalStrut(10));
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
                        JLabel noImagesLabel = new JLabel("📷 Could not load any images.");
                        noImagesLabel.setForeground(TEXT_SECONDARY);
                        noImagesLabel.setFont(FONT_ITALIC_SMALL);
                        noImagesLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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

    // Export and Batch Analysis Methods
    
    private void exportCurrentArticleToCSV() {
        if (currentArticle == null) {
            JOptionPane.showMessageDialog(frame, 
                "No article analyzed yet. Please analyze an article first.",
                "Export Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Article to CSV");
        fileChooser.setSelectedFile(new File("article_analysis.csv"));
        
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                exportService.exportToCSV(currentArticle, fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(frame,
                    "Article exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    "Error exporting article:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void exportCurrentArticleToPDF() {
        if (currentArticle == null) {
            JOptionPane.showMessageDialog(frame,
                "No article analyzed yet. Please analyze an article first.",
                "Export Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Article to PDF");
        fileChooser.setSelectedFile(new File("article_analysis.pdf"));
        
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                exportService.exportToPDF(currentArticle, fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(frame,
                    "Article exported successfully to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    "Error exporting article:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void addToBatch() {
        if (currentArticle == null) {
            JOptionPane.showMessageDialog(frame,
                "No article analyzed yet. Please analyze an article first.",
                "Batch Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check for duplicate based on headline (simple duplicate detection)
        boolean isDuplicate = batchArticles.stream()
            .anyMatch(article -> article.getHeadline() != null && 
                     article.getHeadline().equals(currentArticle.getHeadline()));

        if (isDuplicate) {
            int choice = JOptionPane.showConfirmDialog(frame,
                "An article with this headline is already in the batch.\nAdd anyway?",
                "Possible Duplicate",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        batchArticles.add(currentArticle);
        JOptionPane.showMessageDialog(frame,
            "Article added to batch!\n\nTotal articles in batch: " + batchArticles.size(),
            "Added to Batch",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportBatch() {
        if (batchArticles.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Batch is empty. Add articles to batch first.",
                "Batch Export Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ask user to choose format
        String[] options = {"CSV", "PDF", "Cancel"};
        int choice = JOptionPane.showOptionDialog(frame,
            "Export " + batchArticles.size() + " articles to which format?",
            "Export Batch",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            return; // User cancelled
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Batch Analysis");
        
        if (choice == 0) { // CSV
            fileChooser.setSelectedFile(new File("batch_analysis.csv"));
            int userSelection = fileChooser.showSaveDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    exportService.exportBatchToCSV(batchArticles, fileToSave.getAbsolutePath());
                    JOptionPane.showMessageDialog(frame,
                        batchArticles.size() + " articles exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Batch Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                        "Error exporting batch:\n" + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        } else { // PDF
            fileChooser.setSelectedFile(new File("batch_analysis.pdf"));
            int userSelection = fileChooser.showSaveDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    exportService.exportBatchToPDF(batchArticles, fileToSave.getAbsolutePath());
                    JOptionPane.showMessageDialog(frame,
                        batchArticles.size() + " articles exported successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Batch Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                        "Error exporting batch:\n" + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }
    }

    private void clearBatch() {
        if (batchArticles.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Batch is already empty.",
                "Clear Batch",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame,
            "Clear all " + batchArticles.size() + " articles from batch?",
            "Confirm Clear Batch",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int count = batchArticles.size();
            batchArticles.clear();
            JOptionPane.showMessageDialog(frame,
                "Batch cleared. " + count + " articles removed.",
                "Batch Cleared",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

// Custom cell renderer for links with professional styling
class LinkCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof LinkItem) {
            LinkItem link = (LinkItem) value;
            
            // Create multi-line display with title and subtle index
            String displayText = "<html><div style='padding: 5px;'>" +
                    "<b>" + (index + 1) + ".</b> " + link.getTitle() + 
                    "</div></html>";
            setText(displayText);
            setToolTipText("<html><b>Click to view:</b><br/>" + link.getUrl() + "</html>");
            
            // Enhanced styling
            Color borderColor = new Color(236, 240, 241);
            Color evenBgColor = Color.WHITE;
            Color oddBgColor = new Color(250, 250, 252);
            Color textColor = new Color(44, 62, 80);
            
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            
            if (!isSelected) {
                setBackground(index % 2 == 0 ? evenBgColor : oddBgColor);
                setForeground(textColor);
            }
        }
        return this;
    }
}
