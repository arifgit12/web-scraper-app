# 🕷️ Web Scraper App - Spring Boot

A powerful Spring Boot application with Swing GUI for web scraping news websites and analyzing articles with sentiment analysis.

## 🚀 Features

### Tab 1: Website Link Scraper
- **Smart Link Extraction**: Scrapes and lists latest news articles from websites
- **Content Preview**: Click any link to view full article content
- **Image Display**: Shows article images with proper loading and scaling
- **News Focus**: Filters out navigation/footer links, shows only articles
- **Performance Optimized**: Background loading prevents UI freezing

### Tab 2: Article Analyzer
- **Detailed Article Parsing**: Extract headline, author, publish date, and content
- **Sentiment Analysis**: AI-powered emotion detection (Positive/Negative/Neutral)
- **Word Count**: Automatic article statistics
- **Image Extraction**: Finds and displays article images
- **Keyword Analysis**: Shows positive/negative sentiment keywords

## 🛠️ Technology Stack

- **Spring Boot 3.2.0** - Application framework
- **Java Swing** - Desktop GUI
- **JSoup 1.16.2** - HTML parsing and web scraping
- **Apache HTTP Client** - HTTP connections
- **Java 17** - Runtime environment

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Internet connection for web scraping

## 🏃‍♂️ Running the Application

### Option 1: Maven
```bash
mvn spring-boot:run
```

### Option 2: JAR
```bash
mvn clean package
java -jar target/web-scraper-app-1.0.0.jar
```

### Option 3: IDE
Run the `WebScraperApplication.java` main class

## 🌐 Supported Websites

### ✅ **Confirmed Working:**
- BBC News (`https://www.bbc.com/`)
- CNN (`https://www.cnn.com/`)
- Reuters (`https://www.reuters.com/`)
- NBC News (`https://www.nbcnews.com/`)
- The Guardian (`https://www.theguardian.com/`)

### ⚠️ **May Block Automated Requests:**
- Telegraph India
- Many paywalled news sites
- Sites with heavy JavaScript content loading

## 📖 How to Use

### Website Link Scraper (Tab 1)
1. Enter a news website URL (e.g., `https://www.bbc.com/`)
2. Click **"Get Links"** to scrape latest articles
3. Select any article from the list to view content and images
4. Images load automatically in the background

### Article Analyzer (Tab 2)
1. Paste a specific article URL
2. Click **"Analyze Article"**
3. View extracted details:
    - **Headline** and **Author**
    - **Publication Date**
    - **Sentiment Analysis** with color coding
    - **Full Content** with word count
    - **Article Images**
    - **Sentiment Keywords** (positive/negative words found)

## 🧠 Sentiment Analysis

The built-in sentiment analyzer:
- **Analyzes emotional tone** of articles
- **Scores from -1.0 to +1.0** (negative to positive)
- **Color coding**: 🟢 Positive, 🔴 Negative, 🔵 Neutral
- **Keyword detection** shows sentiment-bearing words
- **Statistical analysis** with word count metrics

## ⚙️ Configuration

### Timeouts and Limits
- **Connection timeout**: 5-8 seconds
- **Read timeout**: 8-10 seconds
- **Max article links**: 25 (for performance)
- **Max images per article**: 3-5
- **Max image size**: 300x200px (scaled automatically)

### Request Headers
The application uses proper browser headers to avoid blocking:
- Modern Chrome User-Agent
- Accept headers for HTML/images
- Referer headers for legitimacy

## 🔧 Dependencies

Add to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.16.2</version>
    </dependency>
    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
    </dependency>
</dependencies>
```

## 🚨 Error Handling

The application handles common issues:
- **403 Forbidden**: Website blocks automated requests
- **Connection timeouts**: Network or server issues
- **SSL errors**: Certificate problems with HTTPS sites
- **Image loading failures**: Graceful fallbacks with error messages
- **Content extraction failures**: Clear user feedback

## 📊 Example Output

```
=== ARTICLE ANALYSIS ===

HEADLINE: Breaking: Major Economic Policy Changes Announced
AUTHOR: John Smith
PUBLISHED: 2024-08-07 10:30:00
SENTIMENT: Negative (-0.23)
WORD COUNT: 847 words

=== SENTIMENT KEYWORDS ===
Positive: progress, improve, success
Negative: crisis, problem, decline, concern
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🐛 Known Issues

- **JavaScript-heavy sites**: JSoup cannot execute JavaScript, so dynamic content may not be captured
- **Anti-bot protection**: Some sites actively block automated requests
- **Image loading**: Some images may fail due to CORS or authentication requirements

## 💡 Tips for Best Results

1. **Use major news sites**: BBC, CNN, Reuters work best
2. **Check robots.txt**: Respect website scraping policies
3. **Don't overwhelm servers**: Built-in delays prevent server overload
4. **Try different URLs**: If one site blocks, try alternatives

## 🔮 Future Enhancements

- [ ] Export analysis results to PDF/CSV
- [ ] Advanced sentiment analysis with machine learning
- [ ] Support for RSS feeds
- [ ] Batch article analysis
- [ ] Custom keyword tracking
- [ ] Article comparison features

---

**Built with ❤️ using Spring Boot and Java Swing**

For questions or issues, please open a GitHub issue or contact the maintainer.
