package com.qsd.admin.member;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.member.dto.MemberPasswordChangeRequest;
import com.qsd.admin.member.dto.MemberProfileResponse;
import com.qsd.admin.member.dto.MemberWechatBindRequest;
import com.qsd.admin.member.dto.MemberWechatCompleteRequest;
import com.qsd.admin.member.dto.MemberWechatLoginRequest;
import com.qsd.admin.member.dto.MemberWechatLoginResponse;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberAuditLogMapper;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.member.mapper.MemberWaybillRelationMapper;
import com.qsd.admin.member.service.MemberService;
import com.qsd.admin.payment.dto.WechatCodeSessionResponse;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.WechatPayGateway;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.TenantContextHolder;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import com.qsd.admin.waybill.service.WaybillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    private static final long TENANT_ID = 1L;

    @Mock
    private MemberAuditLogMapper memberAuditLogMapper;

    @Mock
    private MemberUserMapper memberUserMapper;

    @Mock
    private MemberWaybillRelationMapper memberWaybillRelationMapper;

    @Mock
    private WaybillOrderMapper waybillOrderMapper;

    @Mock
    private WaybillService waybillService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private WechatPayGateway wechatPayGateway;

    @Mock
    private PaymentMerchantService paymentMerchantService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private com.qsd.admin.common.service.RateLimiterService rateLimiterService;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(TENANT_ID, "default", "Default Tenant"));
        memberService = new MemberService(
            memberAuditLogMapper,
            memberUserMapper,
            memberWaybillRelationMapper,
            waybillOrderMapper,
            waybillService,
            jwtTokenService,
            wechatPayGateway,
            paymentMerchantService,
            passwordEncoder,
            rateLimiterService
        );
    }

    @Test
    void shouldRequirePhoneCompletionOnFirstWechatLoginWithoutPhone() {
        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.exchangeCode("demo-code", merchant)).thenReturn(
            new WechatCodeSessionResponse("openid-001", "unionid-001", "session-001")
        );
        when(memberUserMapper.selectByWechatOpenid(TENANT_ID, "openid-001")).thenReturn(null);
        when(jwtTokenService.createMemberWechatBindTicket(TENANT_ID, "default", "openid-001", "unionid-001"))
            .thenReturn("bind-ticket-001");

        MemberWechatLoginResponse response = memberService.wechatLogin(
            new MemberWechatLoginRequest("demo-code", null, "wx-user", "Wechat User", null),
            "10.0.0.1"
        );

        assertTrue(response.phoneCompletionRequired());
        assertEquals("bind-ticket-001", response.bindTicket());
        assertNull(response.accessToken());
        assertNull(response.tokenType());
        verify(memberUserMapper, never()).insert(any(MemberUser.class));
        verify(memberUserMapper, never()).updateById(any(MemberUser.class));
    }

    @Test
    void shouldCreateMemberWhenCompletingWechatLogin() {
        when(jwtTokenService.parseMemberWechatBindTicket("bind-ticket-001")).thenReturn(
            new JwtTokenService.MemberWechatBindTicket(TENANT_ID, "default", "openid-001", "unionid-001")
        );
        when(memberUserMapper.selectByPhone(TENANT_ID, "13800138000")).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(jwtTokenService.createMemberToken(101L, "13800138000", TENANT_ID, "default")).thenReturn("member-token-001");

        doAnswer(invocation -> {
            MemberUser member = invocation.getArgument(0);
            member.setId(101L);
            return 1;
        }).when(memberUserMapper).insert(any(MemberUser.class));

        MemberWechatLoginResponse response = memberService.completeWechatLogin(
            new MemberWechatCompleteRequest("bind-ticket-001", "13800138000", "wx-user", "Wechat User", null),
            "10.0.0.2"
        );

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).insert(memberCaptor.capture());
        MemberUser insertedMember = memberCaptor.getValue();
        assertEquals(TENANT_ID, insertedMember.getTenantId());
        assertEquals("13800138000", insertedMember.getPhone());
        assertEquals("openid-001", insertedMember.getWechatOpenid());
        assertEquals("unionid-001", insertedMember.getWechatUnionid());
        assertEquals("wx-user", insertedMember.getNickname());
        assertEquals("Wechat User", insertedMember.getFullName());
        assertEquals("Created by WeChat login", insertedMember.getRemark());
        assertEquals("miniapp_wechat", insertedMember.getRegisterSource());
        assertEquals("10.0.0.2", insertedMember.getRegisterIp());
        assertNotNull(insertedMember.getWechatBindTime());

        assertFalse(response.phoneCompletionRequired());
        assertEquals("member-token-001", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void shouldBindWechatToExistingMemberOnWechatLogin() {
        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);

        MemberUser existingMember = new MemberUser();
        existingMember.setId(11L);
        existingMember.setPhone("13800138000");
        existingMember.setStatus("active");
        existingMember.setNickname("old-name");

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.exchangeCode("demo-code", merchant)).thenReturn(
            new WechatCodeSessionResponse("openid-002", "unionid-002", "session-002")
        );
        when(memberUserMapper.selectByWechatOpenid(TENANT_ID, "openid-002")).thenReturn(null);
        when(memberUserMapper.selectByPhone(TENANT_ID, "13800138000")).thenReturn(existingMember);
        when(jwtTokenService.createMemberToken(11L, "13800138000", TENANT_ID, "default")).thenReturn("member-token-002");

        MemberWechatLoginResponse response = memberService.wechatLogin(
            new MemberWechatLoginRequest("demo-code", "13800138000", "wx-user", "Wechat User", null),
            "10.0.0.3"
        );

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).updateById(memberCaptor.capture());
        MemberUser updatedMember = memberCaptor.getValue();
        assertEquals(11L, updatedMember.getId());
        assertEquals("openid-002", updatedMember.getWechatOpenid());
        assertEquals("unionid-002", updatedMember.getWechatUnionid());
        assertEquals("10.0.0.3", updatedMember.getLastLoginIp());
        assertNotNull(updatedMember.getWechatBindTime());

        assertEquals("member-token-002", response.accessToken());
    }

    @Test
    void shouldAllowReplacingExistingWechatBindingOnWechatLogin() {
        when(jwtTokenService.parseMemberWechatBindTicket("bind-ticket-002")).thenReturn(
            new JwtTokenService.MemberWechatBindTicket(TENANT_ID, "default", "openid-new", "unionid-new")
        );

        MemberUser existingMember = new MemberUser();
        existingMember.setId(12L);
        existingMember.setPhone("13800138001");
        existingMember.setStatus("active");
        existingMember.setWechatOpenid("openid-old");
        existingMember.setWechatUnionid("unionid-old");

        when(memberUserMapper.selectByPhone(TENANT_ID, "13800138001")).thenReturn(existingMember);
        when(jwtTokenService.createMemberToken(12L, "13800138001", TENANT_ID, "default")).thenReturn("member-token-003");

        MemberWechatLoginResponse response = memberService.completeWechatLogin(
            new MemberWechatCompleteRequest("bind-ticket-002", "13800138001", null, null, true),
            "10.0.0.4"
        );

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).updateById(memberCaptor.capture());
        MemberUser updatedMember = memberCaptor.getValue();
        assertEquals("openid-new", updatedMember.getWechatOpenid());
        assertEquals("unionid-new", updatedMember.getWechatUnionid());
        assertEquals("member-token-003", response.accessToken());
    }

    @Test
    void shouldRejectWechatLoginWhenExistingMemberAlreadyBoundWithoutReplaceFlag() {
        when(jwtTokenService.parseMemberWechatBindTicket("bind-ticket-003")).thenReturn(
            new JwtTokenService.MemberWechatBindTicket(TENANT_ID, "default", "openid-new", "unionid-new")
        );

        MemberUser existingMember = new MemberUser();
        existingMember.setId(13L);
        existingMember.setPhone("13800138002");
        existingMember.setStatus("active");
        existingMember.setWechatOpenid("openid-old");

        when(memberUserMapper.selectByPhone(TENANT_ID, "13800138002")).thenReturn(existingMember);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> memberService.completeWechatLogin(
                new MemberWechatCompleteRequest("bind-ticket-003", "13800138002", null, null, false),
                "10.0.0.5"
            )
        );

        assertEquals("Current member is already bound to another WeChat account", ex.getMessage());
        verify(memberUserMapper, never()).updateById(any(MemberUser.class));
    }

    @Test
    void shouldBindWechatIdentityForCurrentMember() {
        MemberUser member = new MemberUser();
        member.setId(21L);
        member.setPhone("13800138000");
        member.setStatus("active");
        member.setNickname("member-a");
        member.setFullName("Member A");
        member.setAvatarUrl("avatar.png");

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(2L);
        when(memberUserMapper.selectActiveById(TENANT_ID, 21L)).thenReturn(member);
        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.exchangeCode("test-code", merchant)).thenReturn(
            new WechatCodeSessionResponse("openid-003", "unionid-003", "session-003")
        );
        when(memberUserMapper.selectByWechatOpenid(TENANT_ID, "openid-003")).thenReturn(null);

        MemberProfileResponse response = memberService.bindWechatIdentity(
            21L,
            new MemberWechatBindRequest("test-code", null)
        );

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).updateById(memberCaptor.capture());
        MemberUser updatedMember = memberCaptor.getValue();
        assertEquals("openid-003", updatedMember.getWechatOpenid());
        assertEquals("unionid-003", updatedMember.getWechatUnionid());
        assertNotNull(updatedMember.getWechatBindTime());

        assertEquals(21L, response.id());
        assertEquals("openid-003", response.wechatOpenid());
        assertEquals("unionid-003", response.wechatUnionid());
    }

    @Test
    void shouldRejectBindingWechatIdentityWhenOpenidAlreadyBelongsToAnotherMember() {
        MemberUser currentMember = new MemberUser();
        currentMember.setId(21L);
        currentMember.setPhone("13800138000");
        currentMember.setStatus("active");

        MemberUser existingMember = new MemberUser();
        existingMember.setId(99L);
        existingMember.setPhone("13900139000");

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);
        when(memberUserMapper.selectActiveById(TENANT_ID, 21L)).thenReturn(currentMember);
        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.exchangeCode("code-occupied", merchant)).thenReturn(
            new WechatCodeSessionResponse("openid-occupied", "", "")
        );
        when(memberUserMapper.selectByWechatOpenid(TENANT_ID, "openid-occupied")).thenReturn(existingMember);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> memberService.bindWechatIdentity(21L, new MemberWechatBindRequest("code-occupied", null))
        );

        assertEquals("This WeChat account is already bound to another member", ex.getMessage());
        verify(memberUserMapper, never()).updateById(any(MemberUser.class));
    }

    @Test
    void shouldRejectBindingWechatIdentityWhenCurrentMemberAlreadyBoundWithoutReplaceFlag() {
        MemberUser currentMember = new MemberUser();
        currentMember.setId(22L);
        currentMember.setPhone("13800138003");
        currentMember.setStatus("active");
        currentMember.setWechatOpenid("openid-old");

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);
        when(memberUserMapper.selectActiveById(TENANT_ID, 22L)).thenReturn(currentMember);
        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.exchangeCode("code-switch", merchant)).thenReturn(
            new WechatCodeSessionResponse("openid-new", "unionid-new", "")
        );
        when(memberUserMapper.selectByWechatOpenid(TENANT_ID, "openid-new")).thenReturn(null);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> memberService.bindWechatIdentity(22L, new MemberWechatBindRequest("code-switch", false))
        );

        assertEquals("Current member is already bound to another WeChat account", ex.getMessage());
    }

    @Test
    void shouldUnbindWechatIdentity() {
        MemberUser member = new MemberUser();
        member.setId(23L);
        member.setPhone("13800138004");
        member.setStatus("active");
        member.setWechatOpenid("openid-bound");
        member.setWechatUnionid("unionid-bound");
        member.setNickname("member");
        member.setFullName("Member");
        member.setAvatarUrl("");

        when(memberUserMapper.selectActiveById(TENANT_ID, 23L)).thenReturn(member);

        MemberProfileResponse response = memberService.unbindWechatIdentity(23L);

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).updateById(memberCaptor.capture());
        MemberUser updatedMember = memberCaptor.getValue();
        assertEquals("", updatedMember.getWechatOpenid());
        assertEquals("", updatedMember.getWechatUnionid());
        assertNull(updatedMember.getWechatBindTime());
        assertEquals("", response.wechatOpenid());
        assertEquals("", response.wechatUnionid());
    }

    @Test
    void shouldRejectWechatLoginForDisabledMember() {
        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);

        MemberUser existingMember = new MemberUser();
        existingMember.setId(12L);
        existingMember.setPhone("13800138001");
        existingMember.setStatus("disabled");

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.exchangeCode("demo-code", merchant)).thenReturn(
            new WechatCodeSessionResponse("openid-disabled", "unionid-disabled", "session-disabled")
        );
        when(memberUserMapper.selectByWechatOpenid(TENANT_ID, "openid-disabled")).thenReturn(null);
        when(memberUserMapper.selectByPhone(TENANT_ID, "13800138001")).thenReturn(existingMember);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> memberService.wechatLogin(new MemberWechatLoginRequest("demo-code", "13800138001", null, null, null), "10.0.0.6")
        );

        assertEquals("Member is disabled", ex.getMessage());
        verify(memberUserMapper, never()).updateById(any(MemberUser.class));
        verify(memberUserMapper, never()).insert(any(MemberUser.class));
    }

    @Test
    void shouldChangePasswordForMember() {
        MemberUser member = new MemberUser();
        member.setId(30L);
        member.setPhone("13800138009");
        member.setStatus("active");
        member.setPasswordHash("old-hash");

        when(memberUserMapper.selectActiveById(TENANT_ID, 30L)).thenReturn(member);
        when(passwordEncoder.matches("old-pass", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

        memberService.changeMemberPassword(30L, new MemberPasswordChangeRequest("old-pass", "new-pass"));

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).updateById(memberCaptor.capture());
        assertEquals("new-hash", memberCaptor.getValue().getPasswordHash());
        assertNotNull(memberCaptor.getValue().getPasswordUpdatedAt());
    }
}
