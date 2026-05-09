package com.qsd.admin.member;

import com.qsd.admin.auth.dto.LoginResponse;
import com.qsd.admin.member.controller.MemberAuthController;
import com.qsd.admin.member.service.MemberService;
import com.qsd.admin.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void shouldSupportWechatLogin() throws Exception {
        when(memberService.wechatLogin(any())).thenReturn(new LoginResponse("mock-token", "Bearer"));

        mockMvc.perform(post("/api/member/auth/wechat-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "demo-code",
                      "phone": "13800138000",
                      "nickname": "wx-user",
                      "fullName": "Wechat User"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.accessToken").value("mock-token"))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }
}
