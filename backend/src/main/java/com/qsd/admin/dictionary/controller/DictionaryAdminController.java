package com.qsd.admin.dictionary.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.dictionary.dto.DictionaryGroupResponse;
import com.qsd.admin.dictionary.dto.DictionaryItemResponse;
import com.qsd.admin.dictionary.dto.DictionaryItemSaveRequest;
import com.qsd.admin.dictionary.dto.DictionaryOptionResponse;
import com.qsd.admin.dictionary.service.DictionaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dictionaries")
public class DictionaryAdminController {
    private final DictionaryService dictionaryService;

    public DictionaryAdminController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping
    public ApiResponse<List<DictionaryGroupResponse>> listGroups() {
        return ApiResponse.ok(dictionaryService.listGroups());
    }

    @GetMapping("/options")
    public ApiResponse<Map<String, List<DictionaryOptionResponse>>> listOptions(
        @RequestParam List<String> types,
        @RequestParam(defaultValue = "true") boolean enabledOnly
    ) {
        return ApiResponse.ok(dictionaryService.listOptionsByTypes(types, enabledOnly));
    }

    @PostMapping
    public ApiResponse<DictionaryItemResponse> create(@Valid @RequestBody DictionaryItemSaveRequest request) {
        return ApiResponse.ok(dictionaryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DictionaryItemResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody DictionaryItemSaveRequest request
    ) {
        return ApiResponse.ok(dictionaryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dictionaryService.delete(id);
        return ApiResponse.ok();
    }
}
