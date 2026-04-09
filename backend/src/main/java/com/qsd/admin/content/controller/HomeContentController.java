package com.qsd.admin.content.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.content.dto.HomeContentResponse;
import com.qsd.admin.content.dto.HomeContentSaveRequest;
import com.qsd.admin.content.service.HomeContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/content")
public class HomeContentController {
    private final HomeContentService homeContentService;

    public HomeContentController(HomeContentService homeContentService) {
        this.homeContentService = homeContentService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeContentResponse> getHomeContent() {
        return ApiResponse.ok(homeContentService.getHomeContent());
    }

    @PutMapping("/home/draft")
    public ApiResponse<HomeContentResponse> saveDraft(@Valid @RequestBody HomeContentSaveRequest request) {
        return ApiResponse.ok(homeContentService.saveDraft(request.form()));
    }

    @PutMapping("/home/publish")
    public ApiResponse<HomeContentResponse> publish(@Valid @RequestBody HomeContentSaveRequest request) {
        return ApiResponse.ok(homeContentService.publish(request.form()));
    }
}
