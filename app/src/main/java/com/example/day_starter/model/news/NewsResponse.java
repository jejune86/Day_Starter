package com.example.day_starter.model.news;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("totalResults")
    private int totalResults;
    
    @SerializedName("articles")
    private List<Article> articles;
    
    public static class Article {
        @SerializedName("source")
        private Source source;
        
        @SerializedName("author")
        private String author;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("description") 
        private String description;
        
        @SerializedName("url")
        private String url;
        
        @SerializedName("urlToImage")
        private String urlToImage;
        
        @SerializedName("publishedAt")
        private String publishedAt;
        
        @SerializedName("content")
        private String content;
        
        public static class Source {
            @SerializedName("id")
            private String id;
            
            @SerializedName("name")
            private String name;
            
            // Getters
            public String getId() { return id; }
            public String getName() { return name; }
        }
        
        // Getters
        public Source getSource() { return source; }
        public String getAuthor() { return author; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }
        public String getUrlToImage() { return urlToImage; }
        public String getPublishedAt() { return publishedAt; }
        public String getContent() { return content; }
    }
    
    // Getters
    public String getStatus() { return status; }
    public int getTotalResults() { return totalResults; }
    public List<Article> getArticles() { return articles; }
} 