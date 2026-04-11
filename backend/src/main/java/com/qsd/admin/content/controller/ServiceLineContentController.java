package com.qsd.admin.content.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.content.dto.HomeContentSaveRequest;
import com.qsd.admin.content.dto.ServiceLineContentResponse;
import com.qsd.admin.content.dto.ServiceLineSummaryResponse;
import com.qsd.admin.content.service.ServiceLineContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content/service-lines")
public class ServiceLineContentController {
    private final ServiceLineContentService serviceLineContentService;

    public ServiceLineContentController(ServiceLineContentService serviceLineContentService) {
        this.serviceLineContentService = serviceLineContentService;
    }

    @GetMapping
    public ApiResponse<List<ServiceLineSummaryResponse>> listServiceLines() {
        return ApiResponse.ok(serviceLineContentService.listServiceLines());
    }

    @GetMapping("/{lineCode}")
    public ApiResponse<ServiceLineContentResponse> getServiceLine(@PathVariable String lineCode) {
        return ApiResponse.ok(serviceLineContentService.getServiceLine(lineCode));
    }

    @PutMapping("/{lineCode}/draft")
    public ApiResponse<ServiceLineContentResponse> saveDraft(
        @PathVariable String lineCode,
        @Valid @RequestBody HomeContentSaveRequest request
    ) {
        return ApiResponse.ok(serviceLineContentService.saveDraft(lineCode, request.form()));
    }

    @PutMapping("/{lineCode}/publish")
    public ApiResponse<ServiceLineContentResponse> publish(
        @PathVariable String lineCode,
        @Valid @RequestBody HomeContentSaveRequest request
    ) {
        return ApiResponse.ok(serviceLineContentService.publish(lineCode, request.form()));
    }
}
