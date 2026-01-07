# 🕷️ Web Scraper App - Spring Boot

A powerful Spring Boot application with professional Swing GUI for web scraping news websites and analyzing articles with AI-powered sentiment analysis.

## 🚀 Features

### Tab 1: Website Link Scraper (📰)
- **Smart Link Extraction**: Scrapes and lists latest news articles from websites
- **Content Preview**: Click any link to view full article content
- **Image Display**: Shows article images with proper loading and scaling
- **News Focus**: Filters out navigation/footer links, shows only articles
- **Performance Optimized**: Background loading prevents UI freezing
- **Professional UI**: Modern blue color scheme with numbered article list

### Tab 2: Article Analyzer (📊)
- **Detailed Article Parsing**: Extract headline, author, publish date, and content
- **Sentiment Analysis**: AI-powered emotion detection (😊 Positive/😟 Negative/😐 Neutral)
- **Word Count**: Automatic article statistics
- **Image Extraction**: Finds and displays article images
- **Keyword Analysis**: Shows positive/negative sentiment keywords
- **Professional UI**: Modern green color scheme with structured report format

### 📤 Export & Batch Features (NEW!)
- **CSV Export**: Export individual article analysis to CSV format
- **PDF Export**: Generate professional PDF reports with complete analysis
- **Batch Analysis**: Analyze multiple articles and store them in memory
- **Batch Export**: Export all analyzed articles at once to CSV or PDF
- **Flexible Workflow**: Add articles to batch, export when ready, or clear batch

## 🎨 User Interface

The application features a **professional, industry-standard UI** with:
- **Modern Color Scheme**: Blue and green themes with professional grays
- **Intuitive Icons**: Emoji icons for better visual recognition
- **Clear Feedback**: Loading states, success/error messages with helpful suggestions
- **Responsive Layout**: Split panes with adjustable dividers
- **Enhanced Typography**: Clear fonts and proper spacing throughout
- **Hover Tooltips**: Full information on hover for truncated text
- **Numbered Lists**: Easy-to-follow article enumeration
- **Status Bar**: Real-time application status with tips

## 🛠️ Technology Stack

- **Spring Boot 3.4.8** - Application framework
- **Java Swing** - Desktop GUI with custom styling
- **JSoup 1.18.1** - HTML parsing and web scraping
- **Apache HTTP Client** - HTTP connections
- **Apache Commons CSV 1.10.0** - CSV export functionality
- **iText7 7.2.5** - PDF generation and export
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

### Website Link Scraper (Tab 1 - 📰)
1. Enter a news website URL (e.g., `https://www.bbc.com/`)
2. Click **"🔍 Get Latest Articles"** button to scrape
3. Browse the numbered list of articles on the left
4. Select any article to view its content and images
5. Images load automatically in the background
6. Clear success/error messages guide you throughout

### Article Analyzer (Tab 2 - 📊)
1. Paste a specific article URL in the input field
2. Click **"🧠 Analyze with AI"** button
3. View the comprehensive analysis in the left panel:
   - **Headline** with hover for full text
   - **Author** information
   - **Publication Date**
   - **Sentiment Analysis** with emoji indicator and color coding
4. Read the structured report in the main area:
   - Formatted headline and metadata
   - Sentiment analysis with score
   - Sentiment keywords (positive/negative)
   - Full article content
5. View extracted images below the content
6. All processing happens in the background for smooth experience

### Export & Batch Analysis (NEW! 📤)

**Exporting Single Articles:**
1. After analyzing an article, use the export buttons in the left panel
2. Click **"💾 CSV"** to export to CSV format
3. Click **"📄 PDF"** to export to PDF format
4. Choose the save location in the file dialog
5. Get confirmation when export is successful

**Batch Analysis Workflow:**
1. Analyze an article as usual
2. Click **"➕ Add to Batch"** to store it for batch processing
3. Repeat steps 1-2 for multiple articles
4. Click **"📦 Export Batch"** when ready
5. Choose CSV or PDF format
6. All articles are exported to a single file
7. Use **"🗑️ Clear Batch"** to start fresh

**Benefits of Batch Analysis:**
- Compare multiple articles at once
- Generate consolidated reports
- Save time with bulk exports
- Perfect for research and analysis tasks

## 🧠 Sentiment Analysis

The built-in sentiment analyzer provides:
- **Analyzes emotional tone** of articles using word-based analysis
- **Scores from -1.0 to +1.0** (negative to positive)
- **Color coding**: 🟢 Positive (Green), 🔴 Negative (Red), 🔵 Neutral (Blue)
- **Emoji indicators**: 😊 for positive, 😟 for negative, 😐 for neutral
- **Keyword detection** shows sentiment-bearing words found in the article
- **Statistical analysis** with word count and sentiment score metrics
- **Structured report format** with clear sections and formatting

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

### Article Analysis Report Format:
```
═══════════════════════════════════════════════════════════
                    ARTICLE ANALYSIS REPORT
═══════════════════════════════════════════════════════════

📰 HEADLINE
─────────────────────────────────────────────────────────────
Breaking: Major Economic Policy Changes Announced

✍️  METADATA
─────────────────────────────────────────────────────────────
Author:    John Smith
Published: 2024-08-07 10:30:00
Words:     847 words

💭 SENTIMENT ANALYSIS
─────────────────────────────────────────────────────────────
Overall Sentiment: 😟 Negative (Score: -0.23)

🔑 SENTIMENT KEYWORDS
─────────────────────────────────────────────────────────────
✅ Positive: progress, improve, success
❌ Negative: crisis, problem, decline, concern

📄 ARTICLE CONTENT
═══════════════════════════════════════════════════════════
[Full article text here...]
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

- [x] **Export analysis results to PDF/CSV** ✅ COMPLETED
- [x] **Batch article analysis** ✅ COMPLETED
- [ ] Advanced sentiment analysis with machine learning
- [ ] Support for RSS feeds
- [ ] Custom keyword tracking
- [ ] Article comparison features

---

**Built with ❤️ using Spring Boot and Java Swing**

For questions or issues, please open a GitHub issue or contact the maintainer.
