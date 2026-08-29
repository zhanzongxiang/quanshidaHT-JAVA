package com.qsd.admin.member.service;

import com.qsd.admin.auth.dto.LoginResponse;
import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.ErrorCode;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.common.service.RateLimiterService;
import com.qsd.admin.member.dto.MemberAdminDetailResponse;
import com.qsd.admin.member.dto.MemberAdminSaveRequest;
import com.qsd.admin.member.dto.MemberAdminSummaryResponse;
import com.qsd.admin.member.dto.MemberAuditLogResponse;
import com.qsd.admin.member.dto.MemberLoginRequest;
import com.qsd.admin.member.dto.MemberPasswordChangeRequest;
import com.qsd.admin.member.dto.MemberProfileResponse;
import com.qsd.admin.member.dto.MemberProfileUpdateRequest;
import com.qsd.admin.member.dto.MemberRegisterRequest;
import com.qsd.admin.member.dto.MemberWaybillDetailResponse;
import com.qsd.admin.member.dto.MemberWaybillSummaryResponse;
import com.qsd.admin.member.dto.MemberWechatBindRequest;
import com.qsd.admin.member.dto.MemberWechatCompleteRequest;
import com.qsd.admin.member.dto.MemberWechatLoginRequest;
import com.qsd.admin.member.dto.MemberWechatLoginResponse;
import com.qsd.admin.member.entity.MemberAuditLog;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.entity.MemberWaybillRelation;
import com.qsd.admin.member.mapper.MemberAuditLogMapper;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.member.mapper.MemberWaybillRelationMapper;
import com.qsd.admin.payment.dto.WechatCodeSessionResponse;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.WechatPayGateway;
import com.qsd.admin.security.AuthenticatedUser;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.TenantContextHolder;
import com.qsd.admin.waybill.dto.WaybillEventPayload;
import com.qsd.admin.waybill.dto.WaybillLegPayload;
import com.qsd.admin.waybill.entity.WaybillLeg;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.entity.WaybillTrackEvent;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import com.qsd.admin.waybill.service.WaybillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_DISABLED = "disabled";
    private static final String STATUS_PENDING = "pending";
    private static final String REGISTER_SOURCE_MINIAPP_PHONE = "miniapp_phone";
    private static final String REGISTER_SOURCE_MINIAPP_WECHAT = "miniapp_wechat";
    private static final String REGISTER_SOURCE_ADMIN_CREATED = "admin_created";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_ACTIVE, STATUS_DISABLED, STATUS_PENDING);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MemberAuditLogMapper memberAuditLogMapper;
    private final MemberUserMapper memberUserMapper;
    private final MemberWaybillRelationMapper memberWaybillRelationMapper;
    private final WaybillOrderMapper waybillOrderMapper;
    private final WaybillService waybillService;
    private final JwtTokenService jwtTokenService;
    private final WechatPayGateway wechatPayGateway;
    private final PaymentMerchantService paymentMerchantService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiterService;

    public MemberService(
        MemberAuditLogMapper memberAuditLogMapper,
        MemberUserMapper memberUserMapper,
        MemberWaybillRelationMapper memberWaybillRelationMapper,
        WaybillOrderMapper waybillOrderMapper,
        WaybillService waybillService,
        JwtTokenService jwtTokenService,
        WechatPayGateway wechatPayGateway,
        PaymentMerchantService paymentMerchantService,
        PasswordEncoder passwordEncoder,
        RateLimiterService rateLimiterService
    ) {
        this.memberAuditLogMapper = memberAuditLogMapper;
        this.memberUserMapper = memberUserMapper;
        this.memberWaybillRelationMapper = memberWaybillRelationMapper;
        this.waybillOrderMapper = waybillOrderMapper;
        this.waybillService = waybillService;
        this.jwtTokenService = jwtTokenService;
        this.wechatPayGateway = wechatPayGateway;
        this.paymentMerchantService = paymentMerchantService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiterService = rateLimiterService;
    }

    public List<MemberAdminSummaryResponse> listAdminMembers(String keyword, String status) {
        Long tenantId = TenantContextHolder.requireTenantId();
        List<MemberUser> members = memberUserMapper.selectAdminList(tenantId, trimToNull(keyword), trimToNull(status));
        if (members.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = members.stream()
            .map(MemberUser::getId)
            .toList();
        Map<Long, Integer> waybillCountMap = new HashMap<>();
        try {
            List<Map<String, Object>> counts = waybillOrderMapper.countAccessibleByMemberIds(tenantId, memberIds);
            for (Map<String, Object> row : counts) {
                waybillCountMap.put(
                    ((Number) row.get("memberId")).longValue(),
                    ((Number) row.get("cnt")).intValue()
                );
            }
        } catch (Exception ex) {
            log.warn("Failed to batch query waybill counts for {} members", members.size(), ex);
        }

        Map<Long, Integer> finalCountMap = waybillCountMap;
        return members.stream()
            .map(member -> new MemberAdminSummaryResponse(
                member.getId(),
                member.getPhone(),
                safe(member.getWechatOpenid()),
                safe(member.getWechatUnionid()),
                formatDateTime(member.getWechatBindTime()),
                safe(member.getNickname()),
                safe(member.getFullName()),
                member.getStatus(),
                finalCountMap.getOrDefault(member.getId(), 0),
                formatDateTime(member.getLastLoginAt()),
                formatDateTime(member.getCreatedAt())
            ))
            .toList();
    }

    public MemberAdminDetailResponse getAdminMemberDetail(Long id) {
        return toAdminDetail(requireMember(id));
    }

    @Transactional
    public MemberAdminDetailResponse createAdminMember(MemberAdminSaveRequest request) {
        Long tenantId = TenantContextHolder.requireTenantId();
        validateProfileFields(request.nickname(), request.fullName(), request.avatarUrl());
        MemberUser existing = memberUserMapper.selectByPhone(tenantId, request.phone().trim());
        if (existing != null) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Phone already exists");
        }
        if (trimToNull(request.password()) == null) {
            throw new BusinessException("Password is required when creating a member");
        }

        LocalDateTime now = LocalDateTime.now();
        MemberUser member = new MemberUser();
        member.setTenantId(tenantId);
        member.setPhone(request.phone().trim());
        member.setPasswordHash(passwordEncoder.encode(request.password().trim()));
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl(trimToEmpty(request.avatarUrl()));
        member.setStatus(normalizeStatus(request.status()));
        member.setRemark(trimToEmpty(request.remark()));
        member.setRegisterSource(REGISTER_SOURCE_ADMIN_CREATED);
        member.setRegisterIp("");
        member.setLastLoginIp("");
        member.setPasswordUpdatedAt(now);
        member.setDeleted(0);
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        memberUserMapper.insert(member);

        replaceManualWaybillRelations(member.getId(), request.waybillIds());
        recordMemberAudit(member.getId(), "admin_created", "Member created by admin", "source=admin");
        return toAdminDetail(requireMember(member.getId()));
    }

    @Transactional
    public MemberAdminDetailResponse updateAdminMember(Long id, MemberAdminSaveRequest request) {
        Long tenantId = TenantContextHolder.requireTenantId();
        validateProfileFields(request.nickname(), request.fullName(), request.avatarUrl());
        MemberUser member = requireMember(id);
        MemberUser existing = memberUserMapper.selectByPhone(tenantId, request.phone().trim());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Phone already exists");
        }

        member.setPhone(request.phone().trim());
        if (trimToNull(request.password()) != null) {
            member.setPasswordHash(passwordEncoder.encode(request.password().trim()));
            member.setPasswordUpdatedAt(LocalDateTime.now());
        }
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl(trimToEmpty(request.avatarUrl()));
        member.setStatus(normalizeStatus(request.status()));
        member.setRemark(trimToEmpty(request.remark()));
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);

        replaceManualWaybillRelations(id, request.waybillIds());
        recordMemberAudit(id, "admin_updated", "Member profile updated by admin", "fields=profile");
        return toAdminDetail(requireMember(id));
    }

    @Transactional
    public MemberAdminDetailResponse updateAdminMemberStatus(Long id, String status) {
        MemberUser member = requireMember(id);
        member.setStatus(normalizeStatus(status));
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);
        recordMemberAudit(id, "admin_status_updated", "Member status updated to " + member.getStatus(), "status=" + member.getStatus());
        return toAdminDetail(requireMember(id));
    }

    @Transactional
    public LoginResponse register(MemberRegisterRequest request, String clientIp) {
        Long tenantId = TenantContextHolder.requireTenantId();
        validateProfileFields(request.nickname(), request.fullName(), "");
        if (memberUserMapper.selectByPhone(tenantId, request.phone().trim()) != null) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Phone already registered");
        }

        LocalDateTime now = LocalDateTime.now();
        MemberUser member = new MemberUser();
        member.setTenantId(tenantId);
        member.setPhone(request.phone().trim());
        member.setPasswordHash(passwordEncoder.encode(request.password().trim()));
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl("");
        member.setStatus(STATUS_ACTIVE);
        member.setRemark("");
        member.setRegisterSource(REGISTER_SOURCE_MINIAPP_PHONE);
        member.setRegisterIp(trimToEmpty(clientIp));
        member.setDeleted(0);
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        member.setPasswordUpdatedAt(now);
        updateLastLogin(member, clientIp);
        memberUserMapper.insert(member);
        recordMemberAudit(member.getId(), new AuditActor("guest", null, "guest"), "registered", "Member registered", "source=" + member.getRegisterSource());

        String token = createMemberToken(member);
        return new LoginResponse(token, "Bearer");
    }

    @Transactional
    public LoginResponse login(MemberLoginRequest request, String clientIp) {
        Long tenantId = TenantContextHolder.requireTenantId();
        String rateLimitKey = "member:" + (clientIp != null ? clientIp : "unknown") + ":" + request.account();
        if (!rateLimiterService.isAllowed(rateLimitKey)) {
            long remaining = rateLimiterService.getRemainingLockoutSeconds(rateLimitKey);
            throw new BusinessException(ErrorCode.RATE_LIMITED, "Too many login attempts. Retry after " + remaining + " seconds");
        }

        MemberUser member = memberUserMapper.selectByPhone(tenantId, request.account().trim());
        if (member == null) {
            rateLimiterService.recordFailure(rateLimitKey);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Phone or password is incorrect");
        }
        String rawPassword = request.password().trim();
        if (!passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
            rateLimiterService.recordFailure(rateLimitKey);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Phone or password is incorrect");
        }

        ensureMemberUsable(member);
        rateLimiterService.recordSuccess(rateLimitKey);
        updateLastLogin(member, clientIp);
        memberUserMapper.updateById(member);
        recordMemberAudit(member.getId(), new AuditActor("guest", null, "guest"), "password_login", "Member signed in with password", trimToEmpty(clientIp));

        String token = createMemberToken(member);
        return new LoginResponse(token, "Bearer");
    }

    @Transactional
    public MemberWechatLoginResponse wechatLogin(MemberWechatLoginRequest request, String clientIp) {
        Long tenantId = TenantContextHolder.requireTenantId();
        WechatIdentity identity = resolveWechatIdentityByCode(request.code());
        MemberUser member = memberUserMapper.selectByWechatOpenid(tenantId, identity.openid());
        if (member != null) {
            return authenticateBoundWechatMember(member, clientIp);
        }

        String phone = trimToNull(request.phone());
        if (phone == null) {
            return MemberWechatLoginResponse.phoneCompletionRequired(createWechatBindTicket(identity));
        }

        return bindWechatToPhone(
            identity,
            phone,
            request.nickname(),
            request.fullName(),
            Boolean.TRUE.equals(request.replaceBinding()),
            clientIp
        );
    }

    @Transactional
    public MemberWechatLoginResponse completeWechatLogin(MemberWechatCompleteRequest request, String clientIp) {
        WechatIdentity identity = resolveWechatIdentityFromTicket(request.bindTicket());
        return bindWechatToPhone(
            identity,
            request.phone(),
            request.nickname(),
            request.fullName(),
            Boolean.TRUE.equals(request.replaceBinding()),
            clientIp
        );
    }

    public MemberProfileResponse getMemberProfile(Long memberId) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);
        return toProfile(member);
    }

    @Transactional
    public MemberProfileResponse updateMemberProfile(Long memberId, MemberProfileUpdateRequest request) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);
        validateProfileFields(request.nickname(), request.fullName(), request.avatarUrl());
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl(trimToEmpty(request.avatarUrl()));
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);
        recordMemberAudit(memberId, "profile_updated", "Member profile updated", "fields=profile");
        return toProfile(member);
    }

    @Transactional
    public void changeMemberPassword(Long memberId, MemberPasswordChangeRequest request) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);

        String currentPassword = request.currentPassword().trim();
        String newPassword = request.newPassword().trim();
        if (!passwordEncoder.matches(currentPassword, member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Current password is incorrect");
        }
        if (currentPassword.equals(newPassword)) {
            throw new BusinessException("New password must be different from the current password");
        }

        LocalDateTime now = LocalDateTime.now();
        member.setPasswordHash(passwordEncoder.encode(newPassword));
        member.setPasswordUpdatedAt(now);
        member.setUpdatedAt(now);
        memberUserMapper.updateById(member);
        recordMemberAudit(memberId, "password_changed", "Member password changed", "");
    }

    @Transactional
    public MemberProfileResponse bindWechatIdentity(Long memberId, MemberWechatBindRequest request) {
        Long tenantId = TenantContextHolder.requireTenantId();
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);

        WechatIdentity identity = resolveWechatIdentityByCode(request.code());
        MemberUser existing = memberUserMapper.selectByWechatOpenid(tenantId, identity.openid());
        if (existing != null && !existing.getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "This WeChat account is already bound to another member");
        }

        String currentOpenid = trimToNull(member.getWechatOpenid());
        if (currentOpenid != null && !currentOpenid.equals(identity.openid()) && !Boolean.TRUE.equals(request.replaceBinding())) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Current member is already bound to another WeChat account");
        }

        LocalDateTime now = LocalDateTime.now();
        bindWechat(member, identity, now);
        member.setUpdatedAt(now);
        memberUserMapper.updateById(member);
        recordMemberAudit(memberId, "wechat_bound", "WeChat account bound", "openid=" + identity.openid());
        return toProfile(member);
    }

    @Transactional
    public MemberProfileResponse unbindWechatIdentity(Long memberId) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);
        LocalDateTime now = LocalDateTime.now();
        member.setWechatOpenid("");
        member.setWechatUnionid("");
        member.setWechatBindTime(null);
        member.setUpdatedAt(now);
        memberUserMapper.updateById(member);
        recordMemberAudit(memberId, "wechat_unbound", "WeChat account unbound", "");
        return toProfile(member);
    }

    public List<MemberWaybillSummaryResponse> listMemberWaybills(Long memberId) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);
        return listAccessibleWaybills(member);
    }

    public MemberWaybillDetailResponse getMemberWaybillDetail(Long memberId, Long waybillId) {
        Long tenantId = TenantContextHolder.requireTenantId();
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);
        WaybillOrder order = waybillOrderMapper.selectAccessibleDetailByMember(tenantId, waybillId, memberId, member.getPhone());
        if (order == null) {
            throw new NotFoundException("Member waybill not found");
        }
        List<WaybillLeg> legEntities = waybillService.listLegs(order.getId());
        Map<Long, Integer> legNoMap = new HashMap<>();
        for (WaybillLeg legEntity : legEntities) {
            legNoMap.put(legEntity.getId(), legEntity.getLegNo());
        }

        List<WaybillLegPayload> legs = legEntities.stream()
            .map(this::toLegPayload)
            .toList();
        List<WaybillEventPayload> events = waybillService.listVisibleEvents(order.getId()).stream()
            .map(event -> toEventPayload(event, legNoMap))
            .toList();
        return new MemberWaybillDetailResponse(
            order.getId(),
            order.getMainTrackingNo(),
            safe(order.getReferenceNo()),
            order.getCustomerName(),
            order.getDestinationCountry(),
            safe(order.getDestinationCity()),
            order.getCurrentStatus(),
            safe(order.getCurrentNode()),
            safe(order.getOriginWarehouse()),
            safe(order.getCargoDescription()),
            order.getPackageCount(),
            order.getWeightKg(),
            formatDateTime(order.getUpdatedAt()),
            legs,
            events
        );
    }

    private MemberAdminDetailResponse toAdminDetail(MemberUser member) {
        List<Long> boundWaybillIds = memberWaybillRelationMapper.selectWaybillIdsByMemberId(
            TenantContextHolder.requireTenantId(),
            member.getId()
        );
        return new MemberAdminDetailResponse(
            member.getId(),
            member.getPhone(),
            safe(member.getWechatOpenid()),
            safe(member.getWechatUnionid()),
            formatDateTime(member.getWechatBindTime()),
            safe(member.getNickname()),
            safe(member.getFullName()),
            safe(member.getAvatarUrl()),
            member.getStatus(),
            safe(member.getRemark()),
            safe(member.getRegisterSource()),
            safe(member.getRegisterIp()),
            formatDateTime(member.getLastLoginAt()),
            safe(member.getLastLoginIp()),
            formatDateTime(member.getPasswordUpdatedAt()),
            formatDateTime(member.getCreatedAt()),
            formatDateTime(member.getUpdatedAt()),
            boundWaybillIds,
            listAccessibleWaybills(member),
            toAuditResponses(member.getId())
        );
    }

    private MemberProfileResponse toProfile(MemberUser member) {
        return new MemberProfileResponse(
            member.getId(),
            member.getPhone(),
            safe(member.getWechatOpenid()),
            safe(member.getWechatUnionid()),
            formatDateTime(member.getWechatBindTime()),
            safe(member.getNickname()),
            safe(member.getFullName()),
            safe(member.getAvatarUrl()),
            member.getStatus(),
            safe(member.getRegisterSource()),
            formatDateTime(member.getLastLoginAt()),
            safe(member.getLastLoginIp()),
            formatDateTime(member.getPasswordUpdatedAt()),
            formatDateTime(member.getCreatedAt())
        );
    }

    private List<MemberAuditLogResponse> toAuditResponses(Long memberId) {
        return memberAuditLogMapper.selectRecentByMemberId(TenantContextHolder.requireTenantId(), memberId, 20).stream()
            .map(this::toAuditResponse)
            .toList();
    }

    private MemberAuditLogResponse toAuditResponse(MemberAuditLog log) {
        return new MemberAuditLogResponse(
            log.getId(),
            log.getActionType(),
            log.getOperatorType(),
            log.getOperatorLabel(),
            log.getSummary(),
            formatDateTime(log.getCreatedAt())
        );
    }

    private List<MemberWaybillSummaryResponse> listAccessibleWaybills(MemberUser member) {
        return waybillOrderMapper.selectAccessibleByMember(TenantContextHolder.requireTenantId(), member.getId(), member.getPhone()).stream()
            .map(this::toWaybillSummary)
            .toList();
    }

    private MemberWaybillSummaryResponse toWaybillSummary(WaybillOrder order) {
        return new MemberWaybillSummaryResponse(
            order.getId(),
            order.getMainTrackingNo(),
            safe(order.getReferenceNo()),
            order.getCustomerName(),
            order.getDestinationCountry(),
            safe(order.getDestinationCity()),
            order.getCurrentStatus(),
            safe(order.getCurrentNode()),
            formatDateTime(order.getUpdatedAt())
        );
    }

    private WaybillLegPayload toLegPayload(WaybillLeg leg) {
        return new WaybillLegPayload(
            leg.getLegNo(),
            leg.getLegType(),
            leg.getCarrierName(),
            leg.getTrackingNo(),
            leg.getFromNode(),
            leg.getToNode(),
            leg.getLegStatus(),
            leg.getTransferFlag() != null && leg.getTransferFlag() == 1,
            formatDateTime(leg.getDepartureTime()),
            formatDateTime(leg.getArrivalTime()),
            leg.getRemark()
        );
    }

    private WaybillEventPayload toEventPayload(WaybillTrackEvent event, Map<Long, Integer> legNoMap) {
        Integer legNo = event.getLegId() == null ? null : legNoMap.get(event.getLegId());
        return new WaybillEventPayload(
            legNo == null ? null : Long.valueOf(legNo),
            event.getSortNo(),
            formatDateTime(event.getEventTime()),
            event.getEventStatus(),
            event.getEventDescription(),
            event.getEventLocation(),
            event.getVisibleToCustomer() != null && event.getVisibleToCustomer() == 1
        );
    }

    private void replaceManualWaybillRelations(Long memberId, List<Long> waybillIds) {
        Long tenantId = TenantContextHolder.requireTenantId();
        memberWaybillRelationMapper.deleteByMemberId(tenantId, memberId);
        Set<Long> uniqueIds = normalizeWaybillIds(waybillIds);
        if (uniqueIds.isEmpty()) {
            return;
        }

        List<WaybillOrder> waybills = waybillOrderMapper.selectActiveByIds(tenantId, new ArrayList<>(uniqueIds));
        if (waybills.size() != uniqueIds.size()) {
            throw new BusinessException("Some waybills are invalid");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long waybillId : uniqueIds) {
            MemberWaybillRelation relation = new MemberWaybillRelation();
            relation.setTenantId(tenantId);
            relation.setMemberId(memberId);
            relation.setWaybillId(waybillId);
            relation.setCreatedAt(now);
            memberWaybillRelationMapper.insert(relation);
        }
    }

    private Set<Long> normalizeWaybillIds(List<Long> waybillIds) {
        if (waybillIds == null) {
            return Set.of();
        }
        return waybillIds.stream()
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private MemberWechatLoginResponse bindWechatToPhone(
        WechatIdentity identity,
        String phone,
        String nickname,
        String fullName,
        boolean replaceBinding,
        String clientIp
    ) {
        Long tenantId = TenantContextHolder.requireTenantId();
        validateProfileFields(nickname, fullName, "");
        MemberUser member = memberUserMapper.selectByPhone(tenantId, phone);
        LocalDateTime now = LocalDateTime.now();
        if (member == null) {
            member = new MemberUser();
            member.setTenantId(tenantId);
            member.setPhone(phone);
            member.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            member.setNickname(trimToEmpty(nickname));
            member.setFullName(trimToEmpty(fullName));
            member.setAvatarUrl("");
            member.setStatus(STATUS_ACTIVE);
            member.setRemark("Created by WeChat login");
            member.setRegisterSource(REGISTER_SOURCE_MINIAPP_WECHAT);
            member.setRegisterIp(trimToEmpty(clientIp));
            member.setDeleted(0);
            member.setCreatedAt(now);
            member.setPasswordUpdatedAt(now);
        } else {
            ensureMemberUsable(member);
            ensureWechatReplaceAllowed(member, identity.openid(), replaceBinding);
        }

        bindWechat(member, identity, now);
        updateLastLogin(member, clientIp);
        member.setUpdatedAt(now);
        if (member.getId() == null) {
            memberUserMapper.insert(member);
            recordMemberAudit(member.getId(), new AuditActor("guest", null, "guest"), "registered", "Member registered by WeChat login", "source=" + REGISTER_SOURCE_MINIAPP_WECHAT);
        } else {
            memberUserMapper.updateById(member);
        }
        recordMemberAudit(member.getId(), new AuditActor("guest", null, "guest"), "wechat_login", "Member signed in with WeChat", trimToEmpty(clientIp));
        return MemberWechatLoginResponse.authenticated(createMemberToken(member), "Bearer");
    }

    private MemberWechatLoginResponse authenticateBoundWechatMember(MemberUser member, String clientIp) {
        ensureMemberUsable(member);
        updateLastLogin(member, clientIp);
        memberUserMapper.updateById(member);
        recordMemberAudit(member.getId(), new AuditActor("guest", null, "guest"), "wechat_login", "Member signed in with WeChat", trimToEmpty(clientIp));
        return MemberWechatLoginResponse.authenticated(createMemberToken(member), "Bearer");
    }

    private void ensureWechatReplaceAllowed(MemberUser member, String openid, boolean replaceBinding) {
        String currentOpenid = trimToNull(member.getWechatOpenid());
        if (currentOpenid == null || currentOpenid.equals(openid)) {
            return;
        }
        if (!replaceBinding) {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "Current member is already bound to another WeChat account");
        }
    }

    private void bindWechat(MemberUser member, WechatIdentity identity, LocalDateTime now) {
        member.setWechatOpenid(identity.openid());
        member.setWechatUnionid(identity.unionid());
        member.setWechatBindTime(now);
    }

    private WechatIdentity resolveWechatIdentityByCode(String code) {
        String normalizedCode = trimToNull(code);
        if (normalizedCode == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "WeChat auth code is required");
        }
        PayMerchantConfig merchantConfig = paymentMerchantService.requireCurrentMerchant();
        WechatCodeSessionResponse session = wechatPayGateway.exchangeCode(normalizedCode, merchantConfig);
        String openid = trimToNull(session.openid());
        if (openid == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Unable to resolve WeChat identity");
        }
        return new WechatIdentity(openid, trimToEmpty(session.unionid()));
    }

    private WechatIdentity resolveWechatIdentityFromTicket(String bindTicket) {
        String normalizedTicket = trimToNull(bindTicket);
        if (normalizedTicket == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "WeChat bind ticket is required");
        }
        try {
            JwtTokenService.MemberWechatBindTicket ticket = jwtTokenService.parseMemberWechatBindTicket(normalizedTicket);
            Long tenantId = TenantContextHolder.requireTenantId();
            if (ticket.tenantId() != null && !ticket.tenantId().equals(tenantId)) {
                throw new BusinessException(ErrorCode.AUTHORIZATION_DENIED, "WeChat bind ticket does not belong to the current tenant");
            }
            String openid = trimToNull(ticket.openid());
            if (openid == null) {
                throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "WeChat bind ticket is invalid");
            }
            return new WechatIdentity(openid, trimToEmpty(ticket.unionid()));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "WeChat bind ticket is invalid or expired");
        }
    }

    private String createWechatBindTicket(WechatIdentity identity) {
        TenantContext tenantContext = TenantContextHolder.get();
        Long tenantId = TenantContextHolder.requireTenantId();
        String tenantCode = tenantContext == null ? null : tenantContext.tenantCode();
        return jwtTokenService.createMemberWechatBindTicket(tenantId, tenantCode, identity.openid(), identity.unionid());
    }

    private MemberUser requireMember(Long id) {
        MemberUser member = memberUserMapper.selectActiveById(TenantContextHolder.requireTenantId(), id);
        if (member == null) {
            throw new NotFoundException("Member not found");
        }
        return member;
    }

    private String createMemberToken(MemberUser member) {
        TenantContext tenantContext = TenantContextHolder.get();
        Long tenantId = TenantContextHolder.requireTenantId();
        String tenantCode = tenantContext == null ? null : tenantContext.tenantCode();
        return jwtTokenService.createMemberToken(member.getId(), member.getPhone(), tenantId, tenantCode);
    }

    private void ensureMemberUsable(MemberUser member) {
        if (STATUS_DISABLED.equals(member.getStatus())) {
            throw new BusinessException(ErrorCode.STATE_INVALID, "Member is disabled");
        }
        if (STATUS_PENDING.equals(member.getStatus())) {
            throw new BusinessException(ErrorCode.STATE_INVALID, "Member is pending review");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized == null) {
            throw new BusinessException("Status is required");
        }
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new BusinessException("Invalid member status");
        }
        return normalized;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMATTER.format(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void validateProfileFields(String nickname, String fullName, String avatarUrl) {
        String normalizedNickname = trimToNull(nickname);
        String normalizedFullName = trimToNull(fullName);
        String normalizedAvatarUrl = trimToNull(avatarUrl);
        if (normalizedNickname != null && normalizedNickname.length() > 64) {
            throw new BusinessException("Nickname must be at most 64 characters");
        }
        if (normalizedFullName != null && normalizedFullName.length() > 64) {
            throw new BusinessException("Full name must be at most 64 characters");
        }
        if (normalizedAvatarUrl != null) {
            if (normalizedAvatarUrl.length() > 500) {
                throw new BusinessException("Avatar URL must be at most 500 characters");
            }
            if (normalizedAvatarUrl.contains(" ")) {
                throw new BusinessException("Avatar URL must not contain spaces");
            }
        }
    }

    private void updateLastLogin(MemberUser member, String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        member.setLastLoginAt(now);
        member.setLastLoginIp(trimToEmpty(clientIp));
        member.setUpdatedAt(now);
    }

    private void recordMemberAudit(Long memberId, String actionType, String summary, String detail) {
        recordMemberAudit(memberId, resolveAuditActor(), actionType, summary, detail);
    }

    private void recordMemberAudit(Long memberId, AuditActor actor, String actionType, String summary, String detail) {
        MemberAuditLog logEntry = new MemberAuditLog();
        logEntry.setTenantId(TenantContextHolder.requireTenantId());
        logEntry.setMemberId(memberId);
        logEntry.setActionType(actionType);
        logEntry.setOperatorType(actor.operatorType());
        logEntry.setOperatorId(actor.operatorId());
        logEntry.setOperatorLabel(actor.operatorLabel());
        logEntry.setSummary(summary);
        logEntry.setDetailJson(trimToNull(detail));
        logEntry.setCreatedAt(LocalDateTime.now());
        memberAuditLogMapper.insert(logEntry);
    }

    private AuditActor resolveAuditActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            if (JwtTokenService.TOKEN_TYPE_MEMBER.equals(user.tokenType())) {
                return new AuditActor("member", user.userId(), "member:" + user.userId());
            }
            return new AuditActor("admin", user.userId(), user.username());
        }
        return new AuditActor("system", null, "system");
    }

    private record WechatIdentity(String openid, String unionid) {
    }

    private record AuditActor(String operatorType, Long operatorId, String operatorLabel) {
    }
}
