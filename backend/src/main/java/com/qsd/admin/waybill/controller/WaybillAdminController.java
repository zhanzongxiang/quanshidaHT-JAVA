package com.qsd.admin.waybill.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.waybill.dto.WaybillDetailResponse;
import com.qsd.admin.waybill.dto.WaybillSaveRequest;
import com.qsd.admin.waybill.dto.WaybillSummaryResponse;
import com.qsd.admin.waybill.service.WaybillService;
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

@RestController
@RequestMapping("/api/waybills")
public class WaybillAdminController {
    private final WaybillService waybillService;

    public WaybillAdminController(WaybillService waybillService) {
        this.waybillService = waybillService;
    }

    @GetMapping
    public ApiResponse<List<WaybillSummaryResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return ApiResponse.ok(waybillService.list(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<WaybillDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(waybillService.getById(id));
    }

    @PostMapping
    public ApiResponse<WaybillDetailResponse> create(@Valid @RequestBody WaybillSaveRequest request) {
        return ApiResponse.ok(waybillService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WaybillDetailResponse> update(@PathVariable Long id, @Valid @RequestBody WaybillSaveRequest request) {
        return ApiResponse.ok(waybillService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        waybillService.delete(id);
        return ApiResponse.ok();
    }
}
