package com.qsd.admin.member.service;

import com.qsd.admin.member.entity.MemberPackage;
import com.qsd.admin.member.entity.MemberShipment;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberShipmentMapper;
import com.qsd.admin.tenant.TenantContextHolder;
import com.qsd.admin.waybill.entity.WaybillLeg;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.entity.WaybillTrackEvent;
import com.qsd.admin.waybill.mapper.WaybillLegMapper;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import com.qsd.admin.waybill.mapper.WaybillTrackEventMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberWaybillSyncService {
    private final MemberShipmentMapper memberShipmentMapper;
    private final WaybillOrderMapper waybillOrderMapper;
    private final WaybillLegMapper waybillLegMapper;
    private final WaybillTrackEventMapper waybillTrackEventMapper;

    public MemberWaybillSyncService(
        MemberShipmentMapper memberShipmentMapper,
        WaybillOrderMapper waybillOrderMapper,
        WaybillLegMapper waybillLegMapper,
        WaybillTrackEventMapper waybillTrackEventMapper
    ) {
        this.memberShipmentMapper = memberShipmentMapper;
        this.waybillOrderMapper = waybillOrderMapper;
        this.waybillLegMapper = waybillLegMapper;
        this.waybillTrackEventMapper = waybillTrackEventMapper;
    }

    public void ensureWaybill(MemberShipment shipment, MemberUser member, List<MemberPackage> packages, LocalDateTime now) {
        if (shipment.getWaybillId() != null) {
            return;
        }

        Long tenantId = TenantContextHolder.requireTenantId();
        WaybillOrder existing = waybillOrderMapper.selectActiveByMainTrackingNo(tenantId, shipment.getShipmentNo());
        if (existing != null) {
            shipment.setWaybillId(existing.getId());
            memberShipmentMapper.updateById(shipment);
            return;
        }

        WaybillOrder order = new WaybillOrder();
        order.setTenantId(tenantId);
        order.setMemberId(member == null ? null : member.getId());
        order.setMainTrackingNo(shipment.getShipmentNo());
        order.setReferenceNo(member == null ? "" : member.getMemberNo());
        order.setCustomerName(defaultValue(shipment.getRecipientName(), member == null ? "会员客户" : member.getUsername()));
        order.setCustomerPhone(defaultText(shipment.getRecipientPhone()));
        order.setOriginWarehouse(resolveOriginWarehouse(packages));
        order.setDestinationCountry(defaultValue(shipment.getDestinationCountry(), "中国"));
        order.setDestinationCity(defaultText(shipment.getDestinationCity()));
        order.setRouteType("direct");
        order.setCurrentStatus(toWaybillStatus(shipment.getShipmentStatus()));
        order.setCurrentNode(toCurrentNode(shipment.getShipmentStatus()));
        order.setCargoDescription(resolveGoodsDescription(packages));
        order.setPackageCount(shipment.getPackageCount());
        order.setWeightKg(shipment.getTotalWeight());
        order.setRemark("会员集运单自动生成，集运单号：" + shipment.getShipmentNo());
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        waybillOrderMapper.insert(order);

        WaybillLeg leg = new WaybillLeg();
        leg.setTenantId(tenantId);
        leg.setWaybillId(order.getId());
        leg.setLegNo(1);
        leg.setLegType("member_consolidation");
        leg.setCarrierName("QSD");
        leg.setTrackingNo(shipment.getShipmentNo());
        leg.setFromNode(defaultText(order.getOriginWarehouse()));
        leg.setToNode(buildDestination(shipment));
        leg.setLegStatus(toLegStatus(shipment.getShipmentStatus()));
        leg.setTransferFlag(0);
        leg.setRemark("会员集运主分段");
        leg.setCreatedAt(now);
        leg.setUpdatedAt(now);
        waybillLegMapper.insert(leg);

        shipment.setWaybillId(order.getId());
        memberShipmentMapper.updateById(shipment);
        appendEvent(shipment, shipment.getShipmentStatus(), now);
    }

    public void appendEvent(MemberShipment shipment, String shipmentStatus, LocalDateTime now) {
        if (shipment.getWaybillId() == null) {
            return;
        }

        Long tenantId = TenantContextHolder.requireTenantId();
        WaybillOrder order = waybillOrderMapper.selectActiveById(tenantId, shipment.getWaybillId());
        if (order == null) {
            return;
        }

        order.setCurrentStatus(toWaybillStatus(shipmentStatus));
        order.setCurrentNode(toCurrentNode(shipmentStatus));
        order.setUpdatedAt(now);
        waybillOrderMapper.updateById(order);

        List<WaybillLeg> legs = waybillLegMapper.selectByWaybillId(tenantId, order.getId());
        if (!legs.isEmpty()) {
            WaybillLeg leg = legs.get(0);
            leg.setLegStatus(toLegStatus(shipmentStatus));
            if ("outbound".equals(shipmentStatus) && leg.getDepartureTime() == null) {
                leg.setDepartureTime(now);
            }
            if ("completed".equals(shipmentStatus) && leg.getArrivalTime() == null) {
                leg.setArrivalTime(now);
            }
            leg.setUpdatedAt(now);
            waybillLegMapper.updateById(leg);
        }

        WaybillTrackEvent event = new WaybillTrackEvent();
        event.setTenantId(tenantId);
        event.setWaybillId(order.getId());
        event.setLegId(legs.isEmpty() ? null : legs.get(0).getId());
        event.setEventTime(now);
        event.setEventStatus(toWaybillStatus(shipmentStatus));
        event.setEventDescription(toEventDescription(shipmentStatus));
        event.setEventLocation(toCurrentNode(shipmentStatus));
        event.setVisibleToCustomer(1);
        event.setSortNo(waybillTrackEventMapper.selectByWaybillId(tenantId, order.getId()).size() + 1);
        event.setCreatedAt(now);
        waybillTrackEventMapper.insert(event);
    }

    private String toWaybillStatus(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "quoted", "paid" -> "processing";
            case "outbound" -> "in_transit";
            case "completed" -> "signed";
            case "cancelled" -> "cancelled";
            default -> "created";
        };
    }

    private String toLegStatus(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "outbound" -> "in_transit";
            case "completed" -> "arrived";
            case "cancelled" -> "cancelled";
            default -> "pending";
        };
    }

    private String toCurrentNode(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "quoted" -> "运营核价";
            case "paid" -> "仓库待出库";
            case "outbound" -> "运输途中";
            case "completed" -> "目的地签收";
            case "cancelled" -> "订单取消";
            default -> "会员集运";
        };
    }

    private String toEventDescription(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "quoted" -> "运费已核价，请完成支付";
            case "paid" -> "运费已确认，等待仓库出库";
            case "outbound" -> "包裹已出库，进入运输流程";
            case "completed" -> "包裹已签收，集运完成";
            case "cancelled" -> "集运单已取消";
            default -> "集运申请已提交";
        };
    }

    private String resolveOriginWarehouse(List<MemberPackage> packages) {
        String warehouse = packages.stream()
            .map(MemberPackage::getWarehouseCode)
            .filter(value -> value != null && !value.isBlank())
            .distinct()
            .collect(Collectors.joining("/"));
        return warehouse.isBlank() ? "QSD仓库" : limit(warehouse, 128);
    }

    private String resolveGoodsDescription(List<MemberPackage> packages) {
        String goods = packages.stream()
            .map(MemberPackage::getGoodsName)
            .filter(value -> value != null && !value.isBlank())
            .distinct()
            .collect(Collectors.joining("，"));
        return goods.isBlank() ? "会员集运包裹" : limit(goods, 255);
    }

    private String buildDestination(MemberShipment shipment) {
        return limit(String.join(" ",
            defaultText(shipment.getDestinationCountry()),
            defaultText(shipment.getDestinationProvince()),
            defaultText(shipment.getDestinationCity()),
            defaultText(shipment.getDestinationDistrict())
        ).trim(), 128);
    }

    private String defaultValue(String value, String fallback) {
        String text = defaultText(value);
        return text.isBlank() ? fallback : text;
    }

    private String defaultText(String value) {
        return value == null ? "" : value.trim();
    }

    private String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
