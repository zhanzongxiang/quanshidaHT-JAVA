package com.qsd.admin.news.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.news.dto.NewsArticleResponse;
import com.qsd.admin.news.dto.NewsArticleSaveRequest;
import com.qsd.admin.news.service.NewsArticleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('news:view')")
    public ApiResponse<List<NewsArticleResponse>> list() {
        return ApiResponse.ok(newsArticleService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('news:view')")
    public ApiResponse<NewsArticleResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(newsArticleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('news:edit')")
    public ApiResponse<NewsArticleResponse> create(@Valid @RequestBody NewsArticleSaveRequest request) {
        return ApiResponse.ok(newsArticleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('news:edit')")
    public ApiResponse<NewsArticleResponse> update(@PathVariable Long id, @Valid @RequestBody NewsArticleSaveRequest request) {
        return ApiResponse.ok(newsArticleService.update(id, request));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('news:edit')")
    public ApiResponse<NewsArticleResponse> publish(@PathVariable Long id) {
        return ApiResponse.ok(newsArticleService.publish(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_TYPE_ADMIN') and hasAuthority('news:edit')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        newsArticleService.delete(id);
        return ApiResponse.ok();
    }
}
