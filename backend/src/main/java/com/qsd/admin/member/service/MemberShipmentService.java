package com.qsd.admin.member.service;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.member.dto.MemberShipmentCreateRequest;
import com.qsd.admin.member.dto.MemberShipmentResponse;
import com.qsd.admin.member.entity.MemberAddress;
import com.qsd.admin.member.entity.MemberFinanceRecord;
import com.qsd.admin.member.entity.MemberOrder;
import com.qsd.admin.member.entity.MemberPackage;
import com.qsd.admin.member.entity.MemberShipment;
import com.qsd.admin.member.entity.MemberShipmentPackage;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberAddressMapper;
import com.qsd.admin.member.mapper.MemberFinanceRecordMapper;
import com.qsd.admin.member.mapper.MemberOrderMapper;
import com.qsd.admin.member.mapper.MemberPackageMapper;
import com.qsd.admin.member.mapper.MemberShipmentMapper;
import com.qsd.admin.member.mapper.MemberShipmentPackageMapper;
import com.qsd.admin.member.mapper.MemberUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class MemberShipmentService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MemberUserMapper memberUserMapper;
    private final MemberAddressMapper memberAddressMapper;
    private final MemberPackageMapper memberPackageMapper;
    private final MemberShipmentMapper memberShipmentMapper;
    private final MemberShipmentPackageMapper memberShipmentPackageMapper;
    private final MemberOrderMapper memberOrderMapper;
    private final MemberFinanceRecordMapper memberFinanceRecordMapper;
    private final MemberNumberService memberNumberService;
    private final MemberWaybillSyncService memberWaybillSyncService;

    public MemberShipmentService(
        MemberUserMapper memberUserMapper,
        MemberAddressMapper memberAddressMapper,
        MemberPackageMapper memberPackageMapper,
        MemberShipmentMapper memberShipmentMapper,
        MemberShipmentPackageMapper memberShipmentPackageMapper,
        MemberOrderMapper memberOrderMapper,
        MemberFinanceRecordMapper memberFinanceRecordMapper,
        MemberNumberService memberNumberService,
        MemberWaybillSyncService memberWaybillSyncService
    ) {
        this.memberUserMapper = memberUserMapper;
        this.memberAddressMapper = memberAddressMapper;
        this.memberPackageMapper = memberPackageMapper;
        this.memberShipmentMapper = memberShipmentMapper;
        this.memberShipmentPackageMapper = memberShipmentPackageMapper;
        this.memberOrderMapper = memberOrderMapper;
        this.memberFinanceRecordMapper = memberFinanceRecordMapper;
        this.memberNumberService = memberNumberService;
        this.memberWaybillSyncService = memberWaybillSyncService;
    }

    public List<MemberShipmentResponse> list(String username) {
        MemberUser member = requireMember(username);
        return memberShipmentMapper.selectByMemberId(member.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public MemberShipmentResponse create(String username, MemberShipmentCreateRequest request) {
        MemberUser member = requireMember(username);
        MemberAddress address = requireMemberAddress(member.getId(), request.addressId());
        List<Long> packageIds = normalizePackageIds(request.packageIds());
        List<MemberPackage> packages = memberPackageMapper.selectByMemberIdAndIds(member.getId(), packageIds);
        validateSelectedPackages(packageIds, packages);

        LocalDateTime now = LocalDateTime.now();
        MemberShipment shipment = new MemberShipment();
        shipment.setMemberId(member.getId());
        shipment.setShipmentNo(memberNumberService.nextShipmentNo());
        shipment.setAddressId(request.addressId());
        shipment.setShipmentStatus("submitted");
        shipment.setPackageCount(packages.stream().mapToInt(item -> item.getPackageCount() == null ? 0 : item.getPackageCount()).sum());
        shipment.setTotalWeight(sumWeight(packages));
        applyAddressSnapshot(shipment, address);
        shipment.setRemark(blankToEmpty(request.remark()));
        shipment.setCreatedAt(now);
        shipment.setUpdatedAt(now);
        memberShipmentMapper.insert(shipment);

        int lockedCount = memberPackageMapper.lockForShipment(member.getId(), packageIds);
        if (lockedCount != packageIds.size()) {
            throw new IllegalArgumentException("部分包裹已被提交、存在异常或当前不可集运，请刷新库存后重试");
        }

        for (Long packageId : packageIds) {
            MemberShipmentPackage relation = new MemberShipmentPackage();
            relation.setShipmentId(shipment.getId());
            relation.setPackageId(packageId);
            memberShipmentPackageMapper.insert(relation);
        }

        MemberOrder order = new MemberOrder();
        order.setMemberId(member.getId());
        order.setShipmentId(shipment.getId());
        order.setOrderNo(memberNumberService.nextOrderNo());
        order.setOrderStatus("pending_payment");
        order.setPaymentStatus("unpaid");
        order.setAmount(BigDecimal.ZERO);
        order.setCurrencyCode("CNY");
        order.setRemark("集运申请自动生成订单，待后台核价");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        memberOrderMapper.insert(order);

        MemberFinanceRecord financeRecord = new MemberFinanceRecord();
        financeRecord.setMemberId(member.getId());
        financeRecord.setOrderId(order.getId());
        financeRecord.setRecordNo(memberNumberService.nextFinanceRecordNo());
        financeRecord.setRecordType("charge");
        financeRecord.setAmount(BigDecimal.ZERO);
        financeRecord.setCurrencyCode("CNY");
        financeRecord.setRecordStatus("pending");
        financeRecord.setNote("集运申请待核价");
        financeRecord.setCreatedAt(now);
        memberFinanceRecordMapper.insert(financeRecord);

        memberWaybillSyncService.ensureWaybill(shipment, member, packages, now);

        return toResponse(shipment);
    }

    private MemberAddress requireMemberAddress(Long memberId, Long addressId) {
        MemberAddress address = memberAddressMapper.selectById(addressId);
        if (address == null || !memberId.equals(address.getMemberId())) {
            throw new NotFoundException("收货地址不存在");
        }
        return address;
    }

    private void applyAddressSnapshot(MemberShipment shipment, MemberAddress address) {
        shipment.setRecipientName(address.getContactName());
        shipment.setRecipientPhone(address.getContactPhone());
        shipment.setDestinationCountry(address.getCountry());
        shipment.setDestinationProvince(address.getProvince());
        shipment.setDestinationCity(address.getCity());
        shipment.setDestinationDistrict(address.getDistrict());
        shipment.setDestinationAddress(address.getDetailAddress());
        shipment.setPostalCode(address.getPostalCode());
    }

    private List<Long> normalizePackageIds(List<Long> packageIds) {
        return new LinkedHashSet<>(packageIds).stream().toList();
    }

    private void validateSelectedPackages(List<Long> packageIds, List<MemberPackage> packages) {
        if (packages.size() != packageIds.size()) {
            throw new NotFoundException("部分包裹不存在或不属于当前会员");
        }
        for (MemberPackage item : packages) {
            String status = item.getPackageStatus();
            if (!"pending_claim".equals(status) && !"in_stock".equals(status)) {
                throw new IllegalArgumentException("包裹 " + item.getPackageNo() + " 当前状态不可集运");
            }
            if (item.getIssueFlag() != null && item.getIssueFlag() == 1) {
                throw new IllegalArgumentException("包裹 " + item.getPackageNo() + " 存在异常，需处理后才能集运");
            }
        }
    }

    private BigDecimal sumWeight(List<MemberPackage> packages) {
        return packages.stream()
            .map(MemberPackage::getWeightKg)
            .filter(value -> value != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private MemberShipmentResponse toResponse(MemberShipment shipment) {
        return new MemberShipmentResponse(
            shipment.getId(),
            shipment.getShipmentNo(),
            shipment.getShipmentStatus(),
            shipment.getPackageCount(),
            shipment.getTotalWeight(),
            shipment.getRemark(),
            shipment.getCreatedAt() == null ? null : DATE_TIME_FORMATTER.format(shipment.getCreatedAt()),
            memberShipmentPackageMapper.selectPackageIdsByShipmentId(shipment.getId())
        );
    }

    private MemberUser requireMember(String username) {
        MemberUser member = memberUserMapper.selectByUsernameOrMobile(username);
        if (member == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        return member;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
