package com.qsd.admin.member.service;

import com.qsd.admin.auth.dto.LoginResponse;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.member.dto.MemberAdminDetailResponse;
import com.qsd.admin.member.dto.MemberAdminSaveRequest;
import com.qsd.admin.member.dto.MemberAdminSummaryResponse;
import com.qsd.admin.member.dto.MemberLoginRequest;
import com.qsd.admin.member.dto.MemberProfileResponse;
import com.qsd.admin.member.dto.MemberProfileUpdateRequest;
import com.qsd.admin.member.dto.MemberRegisterRequest;
import com.qsd.admin.member.dto.MemberWechatBindRequest;
import com.qsd.admin.member.dto.MemberWechatLoginRequest;
import com.qsd.admin.member.dto.MemberWaybillDetailResponse;
import com.qsd.admin.member.dto.MemberWaybillSummaryResponse;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.entity.MemberWaybillRelation;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.member.mapper.MemberWaybillRelationMapper;
import com.qsd.admin.payment.dto.WechatCodeSessionResponse;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.WechatPayGateway;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.waybill.dto.WaybillEventPayload;
import com.qsd.admin.waybill.dto.WaybillLegPayload;
import com.qsd.admin.waybill.entity.WaybillLeg;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.entity.WaybillTrackEvent;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import com.qsd.admin.waybill.service.WaybillService;
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
import java.util.stream.Collectors;

@Service
public class MemberService {
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_DISABLED = "disabled";
    private static final String STATUS_PENDING = "pending";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MemberUserMapper memberUserMapper;
    private final MemberWaybillRelationMapper memberWaybillRelationMapper;
    private final WaybillOrderMapper waybillOrderMapper;
    private final WaybillService waybillService;
    private final JwtTokenService jwtTokenService;
    private final WechatPayGateway wechatPayGateway;
    private final PaymentMerchantService paymentMerchantService;

    public MemberService(
        MemberUserMapper memberUserMapper,
        MemberWaybillRelationMapper memberWaybillRelationMapper,
        WaybillOrderMapper waybillOrderMapper,
        WaybillService waybillService,
        JwtTokenService jwtTokenService,
        WechatPayGateway wechatPayGateway,
        PaymentMerchantService paymentMerchantService
    ) {
        this.memberUserMapper = memberUserMapper;
        this.memberWaybillRelationMapper = memberWaybillRelationMapper;
        this.waybillOrderMapper = waybillOrderMapper;
        this.waybillService = waybillService;
        this.jwtTokenService = jwtTokenService;
        this.wechatPayGateway = wechatPayGateway;
        this.paymentMerchantService = paymentMerchantService;
    }

    public List<MemberAdminSummaryResponse> listAdminMembers(String keyword, String status) {
        return memberUserMapper.selectAdminList(trimToNull(keyword), trimToNull(status)).stream()
            .map(member -> new MemberAdminSummaryResponse(
                member.getId(),
                member.getPhone(),
                safe(member.getWechatOpenid()),
                safe(member.getWechatUnionid()),
                formatDateTime(member.getWechatBindTime()),
                safe(member.getNickname()),
                safe(member.getFullName()),
                member.getStatus(),
                waybillOrderMapper.countAccessibleByMember(member.getId(), member.getPhone()),
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
        MemberUser existing = memberUserMapper.selectByPhone(request.phone().trim());
        if (existing != null) {
            throw new IllegalArgumentException("member phone already exists");
        }
        if (trimToNull(request.password()) == null) {
            throw new IllegalArgumentException("password is required when creating a member");
        }

        LocalDateTime now = LocalDateTime.now();
        MemberUser member = new MemberUser();
        member.setPhone(request.phone().trim());
        member.setPasswordHash(request.password().trim());
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl(trimToEmpty(request.avatarUrl()));
        member.setStatus(normalizeStatus(request.status()));
        member.setRemark(trimToEmpty(request.remark()));
        member.setDeleted(0);
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        memberUserMapper.insert(member);

        replaceManualWaybillRelations(member.getId(), request.waybillIds());
        return toAdminDetail(requireMember(member.getId()));
    }

    @Transactional
    public MemberAdminDetailResponse updateAdminMember(Long id, MemberAdminSaveRequest request) {
        MemberUser member = requireMember(id);
        MemberUser existing = memberUserMapper.selectByPhone(request.phone().trim());
        if (existing != null && !existing.getId().equals(id)) {
            throw new IllegalArgumentException("member phone already exists");
        }

        member.setPhone(request.phone().trim());
        if (trimToNull(request.password()) != null) {
            member.setPasswordHash(request.password().trim());
        }
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl(trimToEmpty(request.avatarUrl()));
        member.setStatus(normalizeStatus(request.status()));
        member.setRemark(trimToEmpty(request.remark()));
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);

        replaceManualWaybillRelations(id, request.waybillIds());
        return toAdminDetail(requireMember(id));
    }

    @Transactional
    public MemberAdminDetailResponse updateAdminMemberStatus(Long id, String status) {
        MemberUser member = requireMember(id);
        member.setStatus(normalizeStatus(status));
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);
        return toAdminDetail(requireMember(id));
    }

    @Transactional
    public LoginResponse register(MemberRegisterRequest request) {
        if (memberUserMapper.selectByPhone(request.phone().trim()) != null) {
            throw new IllegalArgumentException("phone already registered");
        }

        LocalDateTime now = LocalDateTime.now();
        MemberUser member = new MemberUser();
        member.setPhone(request.phone().trim());
        member.setPasswordHash(request.password().trim());
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl("");
        member.setStatus(STATUS_ACTIVE);
        member.setRemark("");
        member.setDeleted(0);
        member.setCreatedAt(now);
        member.setUpdatedAt(now);
        member.setLastLoginAt(now);
        memberUserMapper.insert(member);

        String token = jwtTokenService.createMemberToken(member.getId(), member.getPhone());
        return new LoginResponse(token, "Bearer");
    }

    @Transactional
    public LoginResponse login(MemberLoginRequest request) {
        MemberUser member = memberUserMapper.selectByPhone(request.phone().trim());
        if (member == null) {
            throw new IllegalArgumentException("member does not exist");
        }
        if (STATUS_DISABLED.equals(member.getStatus())) {
            throw new IllegalArgumentException("member is disabled");
        }
        if (STATUS_PENDING.equals(member.getStatus())) {
            throw new IllegalArgumentException("member is pending approval");
        }
        if (!member.getPasswordHash().equals(request.password().trim())) {
            throw new IllegalArgumentException("invalid phone or password");
        }

        member.setLastLoginAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);

        String token = jwtTokenService.createMemberToken(member.getId(), member.getPhone());
        return new LoginResponse(token, "Bearer");
    }

    @Transactional
    public LoginResponse wechatLogin(MemberWechatLoginRequest request) {
        PayMerchantConfig merchantConfig = paymentMerchantService.requireCurrentMerchant();
        WechatCodeSessionResponse session = wechatPayGateway.exchangeCode(request.code(), merchantConfig);
        String openid = trimToNull(session.openid());
        if (openid == null) {
            throw new IllegalArgumentException("wechat openid is missing");
        }

        MemberUser member = memberUserMapper.selectByWechatOpenid(openid);
        LocalDateTime now = LocalDateTime.now();
        if (member == null) {
            String phone = trimToNull(request.phone());
            if (phone != null) {
                member = memberUserMapper.selectByPhone(phone);
            }
            if (member == null) {
                member = new MemberUser();
                member.setPhone(phone == null ? generateVirtualPhone(openid) : phone);
                member.setPasswordHash("wechat-login");
                member.setNickname(trimToEmpty(request.nickname()));
                member.setFullName(trimToEmpty(request.fullName()));
                member.setAvatarUrl("");
                member.setStatus(STATUS_ACTIVE);
                member.setRemark("created by wechat login");
                member.setDeleted(0);
                member.setCreatedAt(now);
            }
        }

        ensureMemberUsable(member);
        member.setWechatOpenid(openid);
        member.setWechatUnionid(trimToEmpty(session.unionid()));
        member.setWechatBindTime(now);
        member.setLastLoginAt(now);
        member.setUpdatedAt(now);

        if (member.getId() == null) {
            memberUserMapper.insert(member);
        } else {
            memberUserMapper.updateById(member);
        }

        String token = jwtTokenService.createMemberToken(member.getId(), member.getPhone());
        return new LoginResponse(token, "Bearer");
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
        member.setNickname(trimToEmpty(request.nickname()));
        member.setFullName(trimToEmpty(request.fullName()));
        member.setAvatarUrl(trimToEmpty(request.avatarUrl()));
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);
        return toProfile(member);
    }

    @Transactional
    public MemberProfileResponse bindWechatIdentity(Long memberId, MemberWechatBindRequest request) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);

        String openid = trimToNull(request.openid());
        if (openid == null) {
            throw new IllegalArgumentException("openid must not be blank");
        }

        MemberUser existing = memberUserMapper.selectByWechatOpenid(openid);
        if (existing != null && !existing.getId().equals(memberId)) {
            throw new IllegalArgumentException("wechat openid is already bound to another member");
        }

        member.setWechatOpenid(openid);
        member.setWechatUnionid(trimToEmpty(request.unionid()));
        member.setWechatBindTime(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        memberUserMapper.updateById(member);
        return toProfile(member);
    }

    public List<MemberWaybillSummaryResponse> listMemberWaybills(Long memberId) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);
        return listAccessibleWaybills(member);
    }

    public MemberWaybillDetailResponse getMemberWaybillDetail(Long memberId, Long waybillId) {
        MemberUser member = requireMember(memberId);
        ensureMemberUsable(member);
        WaybillOrder order = waybillOrderMapper.selectAccessibleDetailByMember(waybillId, memberId, member.getPhone());
        if (order == null) {
            throw new NotFoundException("member waybill not found");
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
        List<Long> boundWaybillIds = memberWaybillRelationMapper.selectWaybillIdsByMemberId(member.getId());
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
            formatDateTime(member.getLastLoginAt()),
            formatDateTime(member.getCreatedAt()),
            formatDateTime(member.getUpdatedAt()),
            boundWaybillIds,
            listAccessibleWaybills(member)
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
            formatDateTime(member.getCreatedAt())
        );
    }

    private List<MemberWaybillSummaryResponse> listAccessibleWaybills(MemberUser member) {
        return waybillOrderMapper.selectAccessibleByMember(member.getId(), member.getPhone()).stream()
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
        memberWaybillRelationMapper.deleteByMemberId(memberId);
        Set<Long> uniqueIds = normalizeWaybillIds(waybillIds);
        if (uniqueIds.isEmpty()) {
            return;
        }

        List<WaybillOrder> waybills = waybillOrderMapper.selectActiveByIds(new ArrayList<>(uniqueIds));
        if (waybills.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("invalid waybill ids for member binding");
        }

        for (Long waybillId : uniqueIds) {
            MemberWaybillRelation relation = new MemberWaybillRelation();
            relation.setMemberId(memberId);
            relation.setWaybillId(waybillId);
            relation.setCreatedAt(LocalDateTime.now());
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

    private MemberUser requireMember(Long id) {
        MemberUser member = memberUserMapper.selectActiveById(id);
        if (member == null) {
            throw new NotFoundException("member not found");
        }
        return member;
    }

    private void ensureMemberUsable(MemberUser member) {
        if (STATUS_DISABLED.equals(member.getStatus())) {
            throw new IllegalArgumentException("member is disabled");
        }
        if (STATUS_PENDING.equals(member.getStatus())) {
            throw new IllegalArgumentException("member is pending approval");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized == null) {
            throw new IllegalArgumentException("member status is required");
        }
        if (!STATUS_ACTIVE.equals(normalized) && !STATUS_DISABLED.equals(normalized) && !STATUS_PENDING.equals(normalized)) {
            throw new IllegalArgumentException("member status is invalid");
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

    private String generateVirtualPhone(String openid) {
        int suffix = Math.abs(openid.hashCode()) % 100000000;
        return "199" + String.format("%08d", suffix);
    }
}
