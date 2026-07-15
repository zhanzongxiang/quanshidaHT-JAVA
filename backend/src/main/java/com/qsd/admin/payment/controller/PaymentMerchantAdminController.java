package com.qsd.admin.payment.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.payment.dto.PayMerchantActivateRequest;
import com.qsd.admin.payment.dto.PayMerchantConfigCreateRequest;
import com.qsd.admin.payment.dto.PayMerchantConfigSummaryResponse;
import com.qsd.admin.payment.dto.PayMerchantConfigUpdateRequest;
import com.qsd.admin.payment.service.PaymentMerchantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payment-merchants")
public class PaymentMerchantAdminController {
    private final PaymentMerchantService paymentMerchantService;

    public PaymentMerchantAdminController(PaymentMerchantService paymentMerchantService) {
        this.paymentMerchantService = paymentMerchantService;
    }

    @GetMapping
    public ApiResponse<List<PayMerchantConfigSummaryResponse>> list() {
        return ApiResponse.ok(paymentMerchantService.listMerchantConfigs());
    }

    @PostMapping
    public ApiResponse<PayMerchantConfigSummaryResponse> create(@Valid @RequestBody PayMerchantConfigCreateRequest request) {
        return ApiResponse.ok(paymentMerchantService.createMerchantConfig(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PayMerchantConfigSummaryResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody PayMerchantConfigUpdateRequest request
    ) {
        return ApiResponse.ok(paymentMerchantService.updateMerchantConfig(id, request));
    }

    @PutMapping("/activate")
    public ApiResponse<PayMerchantConfigSummaryResponse> activate(@Valid @RequestBody PayMerchantActivateRequest request) {
        return ApiResponse.ok(paymentMerchantService.activateMerchantConfig(request.merchantConfigId()));
    }
}
