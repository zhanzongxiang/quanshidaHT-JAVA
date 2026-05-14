package com.qsd.admin.member;

import com.qsd.admin.auth.dto.LoginResponse;
import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.member.dto.MemberProfileResponse;
import com.qsd.admin.member.dto.MemberWechatBindRequest;
import com.qsd.admin.member.dto.MemberWechatLoginRequest;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.member.mapper.MemberWaybillRelationMapper;
import com.qsd.admin.member.service.MemberService;
import com.qsd.admin.payment.dto.WechatCodeSessionResponse;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.WechatPayGateway;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import com.qsd.admin.waybill.service.WaybillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

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

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(
            memberUserMapper,
            memberWaybillRelationMapper,
            waybillOrderMapper,
            waybillService,
            jwtTokenService,
            wechatPayGateway,
            paymentMerchantService
        );
    }

    @Test
    void shouldCreateMemberOnFirstWechatLogin() {
        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(1L);

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.exchangeCode("demo-code", merchant)).thenReturn(
            new WechatCodeSessionResponse("openid-001", "unionid-001", "session-001")
        );
        when(memberUserMapper.selectByWechatOpenid("openid-001")).thenReturn(null);
        when(memberUserMapper.selectByPhone("13800138000")).thenReturn(null);
        when(jwtTokenService.createMemberToken(101L, "13800138000")).thenReturn("member-token-001");

        doAnswer(invocation -> {
            MemberUser member = invocation.getArgument(0);
            member.setId(101L);
            return 1;
        }).when(memberUserMapper).insert(any(MemberUser.class));

        LoginResponse response = memberService.wechatLogin(
            new MemberWechatLoginRequest("demo-code", "13800138000", "wx-user", "Wechat User")
        );

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).insert(memberCaptor.capture());
        MemberUser insertedMember = memberCaptor.getValue();
        assertEquals("13800138000", insertedMember.getPhone());
        assertEquals("openid-001", insertedMember.getWechatOpenid());
        assertEquals("unionid-001", insertedMember.getWechatUnionid());
        assertEquals("wx-user", insertedMember.getNickname());
        assertEquals("Wechat User", insertedMember.getFullName());
        assertEquals("微信登录自动创建", insertedMember.getRemark());
        assertNotNull(insertedMember.getWechatBindTime());

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
        when(memberUserMapper.selectByWechatOpenid("openid-002")).thenReturn(null);
        when(memberUserMapper.selectByPhone("13800138000")).thenReturn(existingMember);
        when(jwtTokenService.createMemberToken(11L, "13800138000")).thenReturn("member-token-002");

        LoginResponse response = memberService.wechatLogin(
            new MemberWechatLoginRequest("demo-code", "13800138000", "wx-user", "Wechat User")
        );

        ArgumentCaptor<MemberUser> memberCaptor = ArgumentCaptor.forClass(MemberUser.class);
        verify(memberUserMapper).updateById(memberCaptor.capture());
        MemberUser updatedMember = memberCaptor.getValue();
        assertEquals(11L, updatedMember.getId());
        assertEquals("openid-002", updatedMember.getWechatOpenid());
        assertEquals("unionid-002", updatedMember.getWechatUnionid());
        assertNotNull(updatedMember.getWechatBindTime());

        assertEquals("member-token-002", response.accessToken());
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

        when(memberUserMapper.selectActiveById(21L)).thenReturn(member);
        when(memberUserMapper.selectByWechatOpenid("openid-003")).thenReturn(null);

        MemberProfileResponse response = memberService.bindWechatIdentity(
            21L,
            new MemberWechatBindRequest("openid-003", "unionid-003")
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
        when(memberUserMapper.selectByWechatOpenid("openid-disabled")).thenReturn(null);
        when(memberUserMapper.selectByPhone("13800138001")).thenReturn(existingMember);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> memberService.wechatLogin(new MemberWechatLoginRequest("demo-code", "13800138001", null, null))
        );

        assertEquals("会员已被停用", ex.getMessage());
        verify(memberUserMapper, never()).updateById(any(MemberUser.class));
        verify(memberUserMapper, never()).insert(any(MemberUser.class));
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

        when(memberUserMapper.selectActiveById(21L)).thenReturn(currentMember);
        when(memberUserMapper.selectByWechatOpenid("openid-occupied")).thenReturn(existingMember);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> memberService.bindWechatIdentity(21L, new MemberWechatBindRequest("openid-occupied", "unionid-occupied"))
        );

        assertEquals("该微信身份已绑定其他会员", ex.getMessage());
        verify(memberUserMapper, never()).updateById(any(MemberUser.class));
    }
}
