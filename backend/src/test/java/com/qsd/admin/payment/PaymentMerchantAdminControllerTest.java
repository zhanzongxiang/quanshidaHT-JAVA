package com.qsd.admin.payment;

import com.qsd.admin.payment.controller.PaymentMerchantAdminController;
import com.qsd.admin.payment.dto.PayMerchantConfigSummaryResponse;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentMerchantAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentMerchantAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentMerchantService paymentMerchantService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void shouldReturnMerchantList() throws Exception {
        when(paymentMerchantService.listMerchantConfigs()).thenReturn(List.of(
            new PayMerchantConfigSummaryResponse(
                1L,
                "Default Merchant",
                "default_merchant",
                "demo-mch",
                "wx-demo-miniapp",
                true,
                "http://localhost:8080/api/payment/callback/wechat",
                true,
                true,
                true,
                true,
                true,
                "ready",
                List.of(),
                true,
                true,
                "",
                "2026-05-08 10:00:00",
                "2026-05-08 10:00:00"
            )
        ));

        mockMvc.perform(get("/api/admin/payment-merchants"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].merchantCode").value("default_merchant"))
            .andExpect(jsonPath("$.data[0].active").value(true));
    }
}
