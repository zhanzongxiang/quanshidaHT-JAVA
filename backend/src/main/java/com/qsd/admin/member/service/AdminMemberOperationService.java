package com.qsd.admin.member.service;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.member.dto.AdminMemberOrderResponse;
import com.qsd.admin.member.dto.AdminMemberPackageResponse;
import com.qsd.admin.member.dto.AdminMemberPrealertResponse;
import com.qsd.admin.member.dto.AdminMemberShipmentResponse;
import com.qsd.admin.member.dto.AdminPackageInboundRequest;
import com.qsd.admin.member.dto.AdminShipmentQuoteRequest;
import com.qsd.admin.member.entity.MemberFinanceRecord;
import com.qsd.admin.member.entity.MemberOrder;
import com.qsd.admin.member.entity.MemberPackage;
import com.qsd.admin.member.entity.MemberShipment;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.entity.PackagePrealert;
import com.qsd.admin.member.mapper.MemberFinanceRecordMapper;
import com.qsd.admin.member.mapper.MemberOrderMapper;
import com.qsd.admin.member.mapper.MemberPackageMapper;
import com.qsd.admin.member.mapper.MemberShipmentMapper;
import com.qsd.admin.member.mapper.MemberShipmentPackageMapper;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.member.mapper.PackagePrealertMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminMemberOperationService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PackagePrealertMapper packagePrealertMapper;
    private final MemberPackageMapper memberPackageMapper;
    private final MemberShipmentMapper memberShipmentMapper;
    private final MemberShipmentPackageMapper memberShipmentPackageMapper;
    private final MemberOrderMapper memberOrderMapper;
    private final MemberFinanceRecordMapper memberFinanceRecordMapper;
    private final MemberUserMapper memberUserMapper;
    private final MemberNumberService memberNumberService;
    private final MemberWaybillSyncService memberWaybillSyncService;

    public AdminMemberOperationService(
        PackagePrealertMapper packagePrealertMapper,
        MemberPackageMapper memberPackageMapper,
        MemberShipmentMapper memberShipmentMapper,
        MemberShipmentPackageMapper memberShipmentPackageMapper,
        MemberOrderMapper memberOrderMapper,
        MemberFinanceRecordMapper memberFinanceRecordMapper,
        MemberUserMapper memberUserMapper,
        MemberNumberService memberNumberService,
        MemberWaybillSyncService memberWaybillSyncService
    ) {
        this.packagePrealertMapper = packagePrealertMapper;
        this.memberPackageMapper = memberPackageMapper;
        this.memberShipmentMapper = memberShipmentMapper;
        this.memberShipmentPackageMapper = memberShipmentPackageMapper;
        this.memberOrderMapper = memberOrderMapper;
        this.memberFinanceRecordMapper = memberFinanceRecordMapper;
        this.memberUserMapper = memberUserMapper;
        this.memberNumberService = memberNumberService;
        this.memberWaybillSyncService = memberWaybillSyncService;
    }

    public List<AdminMemberPrealertResponse> listPrealerts(String keyword, String status) {
        Map<Long, MemberUser> members = new HashMap<>();
        return packagePrealertMapper.selectAdminList(trimToNull(keyword), trimToNull(status)).stream()
            .map(item -> toPrealertResponse(item, memberOf(item.getMemberId(), members)))
            .toList();
    }

    public List<AdminMemberPackageResponse> listPackages(String keyword, String status) {
        Map<Long, MemberUser> members = new HashMap<>();
        return memberPackageMapper.selectAdminList(trimToNull(keyword), trimToNull(status)).stream()
            .map(item -> toPackageResponse(item, memberOf(item.getMemberId(), members)))
            .toList();
    }

    public List<AdminMemberShipmentResponse> listShipments(String keyword, String status) {
        Map<Long, MemberUser> members = new HashMap<>();
        return memberShipmentMapper.selectAdminList(trimToNull(keyword), trimToNull(status)).stream()
            .map(item -> toShipmentResponse(item, memberOf(item.getMemberId(), members)))
            .toList();
    }

    public List<AdminMemberOrderResponse> listOrders(String keyword, String status) {
        Map<Long, MemberUser> members = new HashMap<>();
        return memberOrderMapper.selectAdminList(trimToNull(keyword), trimToNull(status)).stream()
            .map(item -> toOrderResponse(item, memberOf(item.getMemberId(), members)))
            .toList();
    }

    @Transactional
    public AdminMemberPackageResponse inboundPrealert(Long prealertId, AdminPackageInboundRequest request) {
        PackagePrealert prealert = packagePrealertMapper.selectById(prealertId);
        if (prealert == null) {
            throw new NotFoundException("预报不存在");
        }
        if ("matched".equals(prealert.getStatus())) {
            throw new IllegalArgumentException("预报已入库");
        }

        LocalDateTime now = LocalDateTime.now();
        MemberPackage item = new MemberPackage();
        item.setMemberId(prealert.getMemberId());
        item.setPackageNo(memberNumberService.nextPackageNo());
        item.setPrealertId(prealert.getId());
        item.setTrackingNo(prealert.getTrackingNo());
        item.setGoodsName(prealert.getGoodsName());
        item.setWarehouseCode(defaultValue(request.warehouseCode(), prealert.getWarehouseCode()));
        item.setPackageCount(request.packageCount() == null ? prealert.getPackageCount() : request.packageCount());
        item.setWeightKg(request.weightKg() == null ? prealert.getEstimatedWeight() : request.weightKg());
        item.setPackageStatus("in_stock");
        item.setIssueFlag(0);
        item.setIssueType(null);
        item.setIssueNote(blankToEmpty(request.remark()));
        item.setWarehouseInAt(now);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        memberPackageMapper.insert(item);

        prealert.setStatus("matched");
        prealert.setUpdatedAt(now);
        packagePrealertMapper.updateById(prealert);
        return toPackageResponse(item, memberUserMapper.selectById(item.getMemberId()));
    }

    @Transactional
    public AdminMemberShipmentResponse quoteShipment(Long shipmentId, AdminShipmentQuoteRequest request) {
        MemberShipment shipment = requireShipment(shipmentId);
        requireShipmentStatus(shipment, "submitted", "quoted");
        MemberOrder order = memberOrderMapper.selectByShipmentId(shipmentId);
        if (order == null) {
            throw new NotFoundException("集运订单不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        MemberUser member = memberUserMapper.selectById(shipment.getMemberId());
        memberWaybillSyncService.ensureWaybill(shipment, member, packagesOfShipment(shipment), now);

        shipment.setShipmentStatus("quoted");
        shipment.setUpdatedAt(now);
        if (request.remark() != null) {
            shipment.setRemark(request.remark().trim());
        }
        memberShipmentMapper.updateById(shipment);

        order.setAmount(request.amount());
        order.setOrderStatus("pending_payment");
        order.setPaymentStatus("unpaid");
        order.setRemark(defaultValue(request.remark(), "后台已核价"));
        order.setUpdatedAt(now);
        memberOrderMapper.updateById(order);

        MemberFinanceRecord financeRecord = memberFinanceRecordMapper.selectLatestByOrderId(order.getId());
        if (financeRecord == null) {
            financeRecord = new MemberFinanceRecord();
            financeRecord.setMemberId(order.getMemberId());
            financeRecord.setOrderId(order.getId());
            financeRecord.setRecordNo(memberNumberService.nextFinanceRecordNo());
            financeRecord.setRecordType("charge");
            financeRecord.setCreatedAt(now);
        }
        financeRecord.setAmount(request.amount());
        financeRecord.setCurrencyCode(order.getCurrencyCode());
        financeRecord.setRecordStatus("pending");
        financeRecord.setNote(defaultValue(request.remark(), "后台已核价"));
        if (financeRecord.getId() == null) {
            memberFinanceRecordMapper.insert(financeRecord);
        } else {
            memberFinanceRecordMapper.updateById(financeRecord);
        }

        memberWaybillSyncService.appendEvent(shipment, "quoted", now);
        return toShipmentResponse(shipment, memberUserMapper.selectById(shipment.getMemberId()));
    }

    @Transactional
    public AdminMemberShipmentResponse updateShipmentStatus(Long shipmentId, String status) {
        String normalizedStatus = status.trim();
        MemberShipment shipment = requireShipment(shipmentId);
        if (normalizedStatus.equals(shipment.getShipmentStatus())) {
            return toShipmentResponse(shipment, memberUserMapper.selectById(shipment.getMemberId()));
        }
        validateManualShipmentTransition(shipment, normalizedStatus);
        MemberOrder order = memberOrderMapper.selectByShipmentId(shipmentId);
        LocalDateTime now = LocalDateTime.now();
        MemberUser member = memberUserMapper.selectById(shipment.getMemberId());
        List<MemberPackage> packages = packagesOfShipment(shipment);
        memberWaybillSyncService.ensureWaybill(shipment, member, packages, now);

        shipment.setShipmentStatus(normalizedStatus);
        shipment.setUpdatedAt(now);
        memberShipmentMapper.updateById(shipment);

        List<Long> packageIds = memberShipmentPackageMapper.selectPackageIdsByShipmentId(shipmentId);
        if (!packageIds.isEmpty() && ("outbound".equals(normalizedStatus) || "completed".equals(normalizedStatus) || "cancelled".equals(normalizedStatus))) {
            memberPackageMapper.updateStatusByMemberIdAndIds(
                shipment.getMemberId(),
                packageIds,
                toPackageStatus(normalizedStatus)
            );
        }
        if ("cancelled".equals(normalizedStatus) && order != null) {
            order.setOrderStatus("cancelled");
            order.setUpdatedAt(now);
            memberOrderMapper.updateById(order);

            MemberFinanceRecord financeRecord = memberFinanceRecordMapper.selectLatestByOrderId(order.getId());
            if (financeRecord != null) {
                financeRecord.setRecordStatus("cancelled");
                financeRecord.setNote("后台取消集运单");
                memberFinanceRecordMapper.updateById(financeRecord);
            }
        }

        memberWaybillSyncService.appendEvent(shipment, normalizedStatus, now);
        return toShipmentResponse(shipment, memberUserMapper.selectById(shipment.getMemberId()));
    }

    @Transactional
    public AdminMemberOrderResponse markOrderPaid(Long orderId) {
        MemberOrder order = memberOrderMapper.selectById(orderId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }
        if (order.getAmount() == null || order.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("订单金额未核价，不能确认收款");
        }
        if ("paid".equals(order.getPaymentStatus())) {
            return toOrderResponse(order, memberUserMapper.selectById(order.getMemberId()));
        }
        MemberShipment shipment = requireShipment(order.getShipmentId());
        requireShipmentStatus(shipment, "quoted");
        LocalDateTime now = LocalDateTime.now();
        MemberUser member = memberUserMapper.selectById(order.getMemberId());
        memberWaybillSyncService.ensureWaybill(shipment, member, packagesOfShipment(shipment), now);

        order.setPaymentStatus("paid");
        order.setOrderStatus("paid");
        order.setUpdatedAt(now);
        memberOrderMapper.updateById(order);

        shipment.setShipmentStatus("paid");
        shipment.setUpdatedAt(now);
        memberShipmentMapper.updateById(shipment);

        MemberFinanceRecord financeRecord = memberFinanceRecordMapper.selectLatestByOrderId(orderId);
        if (financeRecord != null) {
            financeRecord.setRecordStatus("confirmed");
            financeRecord.setNote("后台确认收款");
            memberFinanceRecordMapper.updateById(financeRecord);
        }
        memberWaybillSyncService.appendEvent(shipment, "paid", now);
        return toOrderResponse(order, memberUserMapper.selectById(order.getMemberId()));
    }

    private MemberShipment requireShipment(Long shipmentId) {
        MemberShipment shipment = memberShipmentMapper.selectById(shipmentId);
        if (shipment == null) {
            throw new NotFoundException("集运单不存在");
        }
        return shipment;
    }

    private void validateManualShipmentTransition(MemberShipment shipment, String nextStatus) {
        String currentStatus = shipment.getShipmentStatus();
        if ("paid".equals(nextStatus)) {
            throw new IllegalArgumentException("请通过订单确认收款推进到已支付状态");
        }
        if ("outbound".equals(nextStatus)) {
            requireShipmentStatus(shipment, "paid");
            MemberOrder order = memberOrderMapper.selectByShipmentId(shipment.getId());
            if (order == null || !"paid".equals(order.getPaymentStatus())) {
                throw new IllegalArgumentException("订单未支付，不能出库");
            }
            return;
        }
        if ("completed".equals(nextStatus)) {
            requireShipmentStatus(shipment, "outbound");
            return;
        }
        if ("cancelled".equals(nextStatus)) {
            if (!"submitted".equals(currentStatus) && !"quoted".equals(currentStatus)) {
                throw new IllegalArgumentException("只有未支付且未出库的集运单可以取消");
            }
            return;
        }
        throw new IllegalArgumentException("不允许的集运状态流转：" + currentStatus + " -> " + nextStatus);
    }

    private void requireShipmentStatus(MemberShipment shipment, String... allowedStatuses) {
        for (String status : allowedStatuses) {
            if (status.equals(shipment.getShipmentStatus())) {
                return;
            }
        }
        throw new IllegalArgumentException("集运单当前状态不可执行该操作");
    }

    private String toPackageStatus(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "completed" -> "completed";
            case "cancelled" -> "in_stock";
            default -> "outbound";
        };
    }

    private List<MemberPackage> packagesOfShipment(MemberShipment shipment) {
        List<Long> packageIds = memberShipmentPackageMapper.selectPackageIdsByShipmentId(shipment.getId());
        if (packageIds.isEmpty()) {
            return List.of();
        }
        return memberPackageMapper.selectByMemberIdAndIds(shipment.getMemberId(), packageIds);
    }

    private AdminMemberPrealertResponse toPrealertResponse(PackagePrealert item, MemberUser member) {
        return new AdminMemberPrealertResponse(
            item.getId(),
            item.getMemberId(),
            member == null ? "" : member.getMemberNo(),
            member == null ? "" : member.getUsername(),
            item.getPrealertNo(),
            item.getTrackingNo(),
            item.getCourierCode(),
            item.getWarehouseCode(),
            item.getGoodsName(),
            item.getPackageCount(),
            item.getEstimatedWeight(),
            item.getRemark(),
            item.getStatus(),
            formatDateTime(item.getCreatedAt())
        );
    }

    private AdminMemberPackageResponse toPackageResponse(MemberPackage item, MemberUser member) {
        return new AdminMemberPackageResponse(
            item.getId(),
            item.getMemberId(),
            member == null ? "" : member.getMemberNo(),
            member == null ? "" : member.getUsername(),
            item.getPackageNo(),
            item.getTrackingNo(),
            item.getGoodsName(),
            item.getWarehouseCode(),
            item.getPackageCount(),
            item.getWeightKg(),
            item.getPackageStatus(),
            item.getIssueFlag() != null && item.getIssueFlag() == 1,
            item.getIssueType(),
            item.getIssueNote(),
            formatDateTime(item.getWarehouseInAt())
        );
    }

    private AdminMemberShipmentResponse toShipmentResponse(MemberShipment item, MemberUser member) {
        return new AdminMemberShipmentResponse(
            item.getId(),
            item.getMemberId(),
            member == null ? "" : member.getMemberNo(),
            member == null ? "" : member.getUsername(),
            item.getShipmentNo(),
            item.getShipmentStatus(),
            item.getPackageCount(),
            item.getTotalWeight(),
            item.getRemark(),
            formatDateTime(item.getCreatedAt()),
            memberShipmentPackageMapper.selectPackageIdsByShipmentId(item.getId())
        );
    }

    private AdminMemberOrderResponse toOrderResponse(MemberOrder item, MemberUser member) {
        return new AdminMemberOrderResponse(
            item.getId(),
            item.getMemberId(),
            member == null ? "" : member.getMemberNo(),
            member == null ? "" : member.getUsername(),
            item.getShipmentId(),
            item.getOrderNo(),
            item.getOrderStatus(),
            item.getPaymentStatus(),
            item.getAmount(),
            item.getCurrencyCode(),
            item.getRemark(),
            formatDateTime(item.getCreatedAt())
        );
    }

    private MemberUser memberOf(Long memberId, Map<Long, MemberUser> members) {
        if (memberId == null) {
            return null;
        }
        return members.computeIfAbsent(memberId, memberUserMapper::selectById);
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

    private String defaultValue(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
