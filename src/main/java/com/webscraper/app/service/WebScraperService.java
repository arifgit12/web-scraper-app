package com.webscraper.app.service;

import com.webscraper.app.dto.ArticleContent;
import com.webscraper.app.dto.DetailedArticle;
import com.webscraper.app.dto.LinkItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebScraperService {
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

    public DetailedArticle extractDetailedArticle(String url) throws IOException {
        try {
            System.out.println("Extracting detailed article from: " + url);

            // Add a small delay to be respectful
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Cache-Control", "no-cache")
                    .header("Referer", "https://www.google.com/")
                    .timeout(15000)
                    .followRedirects(true)
                    .maxBodySize(3 * 1024 * 1024) // 3MB max for detailed articles
                    .ignoreHttpErrors(true)
                    .get();

            // Check response status
            int statusCode = doc.connection().response().statusCode();
            if (statusCode >= 400) {
                throw new IOException("Failed to load article (HTTP " + statusCode + ")");
            }

            // Extract headline
            String headline = extractHeadline(doc);

            // Extract author
            String author = extractAuthor(doc);

            // Extract publish date
            String publishDate = extractPublishDate(doc);

            // Extract content
            String content = extractArticleContent(doc);

            // Extract images
            List<String> imageUrls = extractArticleImages(doc);

            // Perform sentiment analysis
            SentimentAnalysis sentiment = analyzeSentiment(content);

            // Calculate word count
            int wordCount = content.split("\\s+").length;

            System.out.println("Extracted detailed article: " + headline + " by " + author);
            System.out.println("Word count: " + wordCount + ", Images: " + imageUrls.size() +
                    ", Sentiment: " + sentiment.getLabel());

            return new DetailedArticle(headline, author, publishDate, content, imageUrls, sentiment, wordCount);

        } catch (IOException e) {
            throw new IOException("Failed to analyze article: " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Error processing article: " + e.getMessage());
        }
    }

    private String extractHeadline(Document doc) {
        String headline = "";

        // Try multiple selectors for headline
        String[] headlineSelectors = {
                "h1", ".headline", ".title", ".article-title", ".story-title",
                ".post-title", ".entry-title", ".main-title", "[itemprop=headline]",
                "meta[property='og:title']", "meta[name='twitter:title']"
        };

        for (String selector : headlineSelectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                if (selector.startsWith("meta")) {
                    headline = elements.first().attr("content");
                } else {
                    headline = elements.first().text();
                }
                if (!headline.isEmpty()) {
                    break;
                }
            }
        }

        // Fallback to document title
        if (headline.isEmpty()) {
            headline = doc.title();
        }

        return headline.isEmpty() ? "No headline found" : headline.trim();
    }

    private String extractAuthor(Document doc) {
        String author = "";

        // Try multiple selectors for author
        String[] authorSelectors = {
                ".author", ".byline", ".writer", ".journalist", "[itemprop=author]",
                ".article-author", ".post-author", ".story-author", ".by-author",
                "meta[name='author']", "meta[property='article:author']"
        };

        for (String selector : authorSelectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                if (selector.startsWith("meta")) {
                    author = elements.first().attr("content");
                } else {
                    author = elements.first().text();
                    // Clean up author text
                    author = author.replaceAll("(?i)^(by|author|written by):?\\s*", "").trim();
                }
                if (!author.isEmpty()) {
                    break;
                }
            }
        }

        return author.isEmpty() ? "Unknown Author" : author.trim();
    }

    private String extractPublishDate(Document doc) {
        String date = "";

        // Try multiple selectors for publish date
        String[] dateSelectors = {
                ".date", ".publish-date", ".published", ".timestamp", ".article-date",
                ".post-date", ".story-date", "[itemprop=datePublished]", "[datetime]",
                "meta[property='article:published_time']", "meta[name='publish_date']",
                "time"
        };

        for (String selector : dateSelectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                Element element = elements.first();
                if (selector.startsWith("meta")) {
                    date = element.attr("content");
                } else if (element.hasAttr("datetime")) {
                    date = element.attr("datetime");
                } else {
                    date = element.text();
                }
                if (!date.isEmpty()) {
                    break;
                }
            }
        }

        return date.isEmpty() ? "Unknown Date" : date.trim();
    }

    private String extractArticleContent(Document doc) {
        StringBuilder contentBuilder = new StringBuilder();

        // Try multiple content selectors
        String[] contentSelectors = {
                ".article-content", ".article-body", ".story-content", ".post-content",
                ".entry-content", ".content", "article", ".main-content", "[itemprop=articleBody]",
                ".text", ".body", ".article-text", ".story-text"
        };

        Elements contentElements = new Elements();
        for (String selector : contentSelectors) {
            contentElements = doc.select(selector);
            if (!contentElements.isEmpty()) {
                System.out.println("Found content using selector: " + selector);
                break;
            }
        }

        // If no main content found, try paragraphs
        if (contentElements.isEmpty()) {
            contentElements = doc.select("p");
        }

        // Extract text from all content elements
        for (Element element : contentElements) {
            String text = element.text().trim();
            if (!text.isEmpty() && text.length() > 30) {
                contentBuilder.append(text).append("\n\n");
            }
        }

        String content = contentBuilder.toString().trim();
        return content.isEmpty() ? "No content could be extracted from this article." : content;
    }

    private List<String> extractArticleImages(Document doc) {
        List<String> imageUrls = new ArrayList<>();

        // Try to find images in article content areas
        String[] imageSelectors = {
                ".article-content img[src]", ".article-body img[src]", ".story-content img[src]",
                ".post-content img[src]", ".entry-content img[src]", "article img[src]",
                ".content img[src]", ".main-content img[src]", ".text img[src]"
        };

        Elements images = new Elements();
        for (String selector : imageSelectors) {
            images = doc.select(selector);
            if (!images.isEmpty()) {
                break;
            }
        }

        // Fallback to all images if none found in content
        if (images.isEmpty()) {
            images = doc.select("img[src]");
        }

        for (Element img : images) {
            String src = img.attr("abs:src");
            if (!src.isEmpty() && isValidImageUrl(src) && isNewsImage(img, src)) {
                imageUrls.add(src);
                if (imageUrls.size() >= 5) break;
            }
        }

        return imageUrls;
    }

    private SentimentAnalysis analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new SentimentAnalysis("Neutral", 0.0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        // Simple word-based sentiment analysis
        String[] positiveWords = {
                "good", "great", "excellent", "amazing", "wonderful", "fantastic", "outstanding",
                "positive", "success", "achieve", "progress", "improve", "benefit", "advantage",
                "hope", "optimistic", "bright", "happy", "joy", "celebrate", "victory", "win",
                "strong", "confident", "effective", "efficient", "valuable", "important"
        };

        String[] negativeWords = {
                "bad", "terrible", "awful", "horrible", "disgusting", "hate", "anger", "sad",
                "negative", "fail", "failure", "problem", "issue", "crisis", "disaster", "tragedy",
                "wrong", "mistake", "error", "corrupt", "violence", "war", "conflict", "threat",
                "dangerous", "risk", "concern", "worry", "fear", "anxiety", "depression", "decline"
        };

        // Convert to lowercase and split into words
        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("\\W+");

        int positiveScore = 0;
        int negativeScore = 0;
        List<String> foundPositive = new ArrayList<>();
        List<String> foundNegative = new ArrayList<>();

        // Count sentiment words
        for (String word : words) {
            for (String posWord : positiveWords) {
                if (word.equals(posWord)) {
                    positiveScore++;
                    if (!foundPositive.contains(posWord)) {
                        foundPositive.add(posWord);
                    }
                }
            }
            for (String negWord : negativeWords) {
                if (word.equals(negWord)) {
                    negativeScore++;
                    if (!foundNegative.contains(negWord)) {
                        foundNegative.add(negWord);
                    }
                }
            }
        }

        // Calculate sentiment score
        int totalWords = words.length;
        double score = 0.0;
        String label = "Neutral";

        if (totalWords > 0) {
            score = (double) (positiveScore - negativeScore) / totalWords * 10; // Scale to reasonable range
            score = Math.max(-1.0, Math.min(1.0, score)); // Clamp to [-1, 1]

            if (score > 0.1) {
                label = "Positive";
            } else if (score < -0.1) {
                label = "Negative";
            }
        }

        List<String> allKeywords = new ArrayList<>();
        allKeywords.addAll(foundPositive);
        allKeywords.addAll(foundNegative);

        return new SentimentAnalysis(label, score, allKeywords, foundPositive, foundNegative);
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
