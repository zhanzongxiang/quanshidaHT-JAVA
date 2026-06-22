package com.qsd.admin.member;

import com.qsd.admin.member.controller.MemberPortalController;
import com.qsd.admin.member.dto.MemberProfileResponse;
import com.qsd.admin.member.service.MemberService;
import com.qsd.admin.payment.dto.MemberPaymentPrepareResponse;
import com.qsd.admin.payment.dto.MemberPayOrderSummaryResponse;
import com.qsd.admin.payment.service.PaymentService;
import com.qsd.admin.security.AuthenticatedUser;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.tenant.service.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberPortalController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private TenantService tenantService;

    @Test
    void shouldReturnMemberProfileForAuthenticatedMember() throws Exception {
        when(memberService.getMemberProfile(7L)).thenReturn(new MemberProfileResponse(
            7L,
            "13800138000",
            "openid-001",
            "unionid-001",
            "2026-06-16 09:30:00",
            "wx-user",
            "Wechat User",
            "https://cdn.example/avatar.png",
            "active",
            "miniapp_wechat",
            "2026-06-16 09:31:00",
            "10.0.0.7",
            "2026-06-15 18:00:00",
            "2026-06-01 08:00:00"
        ));

        mockMvc.perform(get("/api/member/profile")
                .principal(authenticatedMember()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value(7))
            .andExpect(jsonPath("$.data.phone").value("13800138000"))
            .andExpect(jsonPath("$.data.wechatOpenid").value("openid-001"))
            .andExpect(jsonPath("$.data.nickname").value("wx-user"));
    }

    @Test
    void shouldListMemberPaymentsForAuthenticatedMember() throws Exception {
        when(paymentService.listMemberPayOrders(7L)).thenReturn(List.of(
            new MemberPayOrderSummaryResponse(
                101L,
                "PO202606160001",
                9L,
                "Acme Merchant",
                15L,
                "WB202606160001",
                "wechat_pay",
                new BigDecimal("88.50"),
                BigDecimal.ZERO,
                "paying",
                "Waybill payment",
                null,
                "2026-06-16 09:45:00"
            )
        ));

        mockMvc.perform(get("/api/member/payments")
                .principal(authenticatedMember()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].id").value(101))
            .andExpect(jsonPath("$.data[0].orderNo").value("PO202606160001"))
            .andExpect(jsonPath("$.data[0].merchantName").value("Acme Merchant"))
            .andExpect(jsonPath("$.data[0].status").value("paying"));
    }

    @Test
    void shouldPrepareMemberPaymentForAuthenticatedMember() throws Exception {
        when(paymentService.prepareMemberPayment(any(), any())).thenReturn(new MemberPaymentPrepareResponse(
            101L,
            "PO202606160001",
            "paying",
            9L,
            "Acme Merchant",
            "wx-app-001",
            "1710000000",
            "nonce-123",
            "prepay_id=wx-prepay-001",
            "RSA",
            "sign-123"
        ));

        mockMvc.perform(put("/api/member/payments/prepare")
                .principal(authenticatedMember())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "waybillId": 15,
                      "amountTotal": 88.50,
                      "description": "Waybill payment",
                      "channel": "wechat_pay"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.payOrderId").value(101))
            .andExpect(jsonPath("$.data.orderNo").value("PO202606160001"))
            .andExpect(jsonPath("$.data.appId").value("wx-app-001"))
            .andExpect(jsonPath("$.data.paySign").value("sign-123"));

        verify(paymentService).prepareMemberPayment(any(), any());
    }

    @Test
    void shouldRejectPaymentPrepareWhenRequestIsInvalid() throws Exception {
        mockMvc.perform(put("/api/member/payments/prepare")
                .principal(authenticatedMember())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "waybillId": 15,
                      "amountTotal": 0,
                      "description": "Waybill payment",
                      "channel": ""
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(40001));

        verify(paymentService, never()).prepareMemberPayment(any(), any());
    }

    @Test
    void shouldRejectProfileRequestWhenMemberSessionIsMissing() throws Exception {
        mockMvc.perform(get("/api/member/profile"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(40102))
            .andExpect(jsonPath("$.message").value("Member session is invalid, please sign in again"));
    }

    private TestingAuthenticationToken authenticatedMember() {
        return new TestingAuthenticationToken(
            new AuthenticatedUser(7L, "member-13800138000", "member", 1L, "default", null, null),
            null
        );
    }
}
