package com.qsd.admin.news.service;

import com.qsd.admin.news.dto.NewsArticleResponse;
import com.qsd.admin.news.dto.NewsArticleSaveRequest;
import com.qsd.admin.news.entity.NewsArticle;
import com.qsd.admin.news.mapper.NewsArticleMapper;
import com.qsd.admin.website.service.PublicWebsiteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NewsArticleService {
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final NewsArticleMapper newsArticleMapper;
    private final PublicWebsiteService publicWebsiteService;

    public NewsArticleService(NewsArticleMapper newsArticleMapper, PublicWebsiteService publicWebsiteService) {
        this.newsArticleMapper = newsArticleMapper;
        this.publicWebsiteService = publicWebsiteService;
    }

    public List<NewsArticleResponse> list() {
        return newsArticleMapper.selectActiveList().stream().map(this::toResponse).toList();
    }

    public NewsArticleResponse getById(Long id) {
        return toResponse(requireArticle(id));
    }

    public NewsArticleResponse create(NewsArticleSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        NewsArticle article = new NewsArticle();
        article.setTitle(request.title().trim());
        article.setSummary(request.summary().trim());
        article.setCoverImageUrl(trimToEmpty(request.coverImageUrl()));
        article.setContent(request.content().trim());
        article.setAuthor(defaultAuthor(request.author()));
        article.setStatus(STATUS_DRAFT);
        article.setPublishedAt(null);
        article.setCreatedAt(now);
        article.setUpdatedAt(now);
        article.setDeleted(0);
        newsArticleMapper.insert(article);
        publicWebsiteService.evictNewsCache();
        return toResponse(article);
    }

    public NewsArticleResponse update(Long id, NewsArticleSaveRequest request) {
        NewsArticle article = requireArticle(id);
        article.setTitle(request.title().trim());
        article.setSummary(request.summary().trim());
        article.setCoverImageUrl(trimToEmpty(request.coverImageUrl()));
        article.setContent(request.content().trim());
        article.setAuthor(defaultAuthor(request.author()));
        article.setUpdatedAt(LocalDateTime.now());
        newsArticleMapper.updateById(article);
        publicWebsiteService.evictNewsCache();
        return toResponse(article);
    }

    public NewsArticleResponse publish(Long id) {
        NewsArticle article = requireArticle(id);
        LocalDateTime now = LocalDateTime.now();
        article.setStatus(STATUS_PUBLISHED);
        article.setPublishedAt(now);
        article.setUpdatedAt(now);
        newsArticleMapper.updateById(article);
        publicWebsiteService.evictNewsCache();
        return toResponse(article);
    }

    public void delete(Long id) {
        NewsArticle article = requireArticle(id);
        article.setDeleted(1);
        article.setUpdatedAt(LocalDateTime.now());
        newsArticleMapper.updateById(article);
        publicWebsiteService.evictNewsCache();
    }

    private NewsArticle requireArticle(Long id) {
        NewsArticle article = newsArticleMapper.selectActiveById(id);
        if (article == null) {
            throw new IllegalArgumentException("news article not found");
        }
        return article;
    }

    private NewsArticleResponse toResponse(NewsArticle article) {
        return new NewsArticleResponse(
            article.getId(),
            article.getTitle(),
            article.getSummary(),
            article.getCoverImageUrl(),
            article.getContent(),
            article.getAuthor(),
            article.getStatus(),
            formatDateTime(article.getPublishedAt()),
            formatDateTime(article.getUpdatedAt())
        );
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMATTER.format(value);
    }

    private String defaultAuthor(String value) {
        String trimmed = trimToEmpty(value);
        return trimmed.isBlank() ? "QSD Admin" : trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
