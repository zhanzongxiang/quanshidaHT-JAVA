package com.qsd.admin.news.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.news.dto.NewsArticleResponse;
import com.qsd.admin.news.dto.NewsArticleSaveRequest;
import com.qsd.admin.news.service.NewsArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsArticleController {
    private final NewsArticleService newsArticleService;

    public NewsArticleController(NewsArticleService newsArticleService) {
        this.newsArticleService = newsArticleService;
    }

    @GetMapping
    public ApiResponse<List<NewsArticleResponse>> list() {
        return ApiResponse.ok(newsArticleService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<NewsArticleResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(newsArticleService.getById(id));
    }

    @PostMapping
    public ApiResponse<NewsArticleResponse> create(@Valid @RequestBody NewsArticleSaveRequest request) {
        return ApiResponse.ok(newsArticleService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<NewsArticleResponse> update(@PathVariable Long id, @Valid @RequestBody NewsArticleSaveRequest request) {
        return ApiResponse.ok(newsArticleService.update(id, request));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<NewsArticleResponse> publish(@PathVariable Long id) {
        return ApiResponse.ok(newsArticleService.publish(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsArticleService.delete(id);
        return ApiResponse.ok();
    }
}
