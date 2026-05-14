package com.qsd.admin.waybill.service;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.waybill.dto.WaybillDetailResponse;
import com.qsd.admin.waybill.dto.WaybillEventPayload;
import com.qsd.admin.waybill.dto.WaybillLegPayload;
import com.qsd.admin.waybill.dto.WaybillSaveRequest;
import com.qsd.admin.waybill.dto.WaybillSummaryResponse;
import com.qsd.admin.waybill.entity.WaybillLeg;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.entity.WaybillTrackEvent;
import com.qsd.admin.waybill.mapper.WaybillLegMapper;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import com.qsd.admin.waybill.mapper.WaybillTrackEventMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WaybillService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WaybillOrderMapper waybillOrderMapper;
    private final WaybillLegMapper waybillLegMapper;
    private final WaybillTrackEventMapper waybillTrackEventMapper;

    public WaybillService(
        WaybillOrderMapper waybillOrderMapper,
        WaybillLegMapper waybillLegMapper,
        WaybillTrackEventMapper waybillTrackEventMapper
    ) {
        this.waybillOrderMapper = waybillOrderMapper;
        this.waybillLegMapper = waybillLegMapper;
        this.waybillTrackEventMapper = waybillTrackEventMapper;
    }

    public List<WaybillSummaryResponse> list(String keyword, String status) {
        return waybillOrderMapper.selectActiveList(trimToNull(keyword), trimToNull(status)).stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    public WaybillDetailResponse getById(Long id) {
        return toDetailResponse(requireWaybill(id));
    }

    @Transactional
    public WaybillDetailResponse create(WaybillSaveRequest request) {
        ensureMainTrackingNoAvailable(request.mainTrackingNo(), null);

        LocalDateTime now = LocalDateTime.now();
        WaybillOrder order = new WaybillOrder();
        applyRequest(order, request, now);
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        waybillOrderMapper.insert(order);

        replaceChildren(order.getId(), request, now);
        return toDetailResponse(requireWaybill(order.getId()));
    }

    @Transactional
    public WaybillDetailResponse update(Long id, WaybillSaveRequest request) {
        WaybillOrder order = requireWaybill(id);
        ensureMainTrackingNoAvailable(request.mainTrackingNo(), id);

        LocalDateTime now = LocalDateTime.now();
        applyRequest(order, request, now);
        waybillOrderMapper.updateById(order);

        replaceChildren(id, request, now);
        return toDetailResponse(requireWaybill(id));
    }

    @Transactional
    public void delete(Long id) {
        WaybillOrder order = requireWaybill(id);
        order.setDeleted(1);
        order.setUpdatedAt(LocalDateTime.now());
        waybillOrderMapper.updateById(order);
        waybillLegMapper.deleteByWaybillId(id);
        waybillTrackEventMapper.deleteByWaybillId(id);
    }

    public WaybillOrder findByTrackingNo(String trackingNo) {
        String normalized = trimToNull(trackingNo);
        if (normalized == null) {
            return null;
        }

        WaybillOrder order = waybillOrderMapper.selectActiveByMainTrackingNo(normalized);
        if (order != null) {
            return order;
        }

        WaybillLeg leg = waybillLegMapper.selectByTrackingNo(normalized);
        if (leg == null) {
            return null;
        }
        return waybillOrderMapper.selectActiveById(leg.getWaybillId());
    }

    public List<WaybillLeg> listLegs(Long waybillId) {
        return waybillLegMapper.selectByWaybillId(waybillId);
    }

    public List<WaybillTrackEvent> listVisibleEvents(Long waybillId) {
        return waybillTrackEventMapper.selectVisibleByWaybillId(waybillId);
    }

    private WaybillSummaryResponse toSummaryResponse(WaybillOrder order) {
        return new WaybillSummaryResponse(
            order.getId(),
            order.getMainTrackingNo(),
            order.getCustomerName(),
            order.getDestinationCountry(),
            order.getDestinationCity(),
            order.getRouteType(),
            order.getCurrentStatus(),
            order.getCurrentNode(),
            formatDateTime(order.getUpdatedAt())
        );
    }

    private WaybillDetailResponse toDetailResponse(WaybillOrder order) {
        List<WaybillLeg> legEntities = waybillLegMapper.selectByWaybillId(order.getId());
        Map<Long, Integer> legNoMap = new HashMap<>();
        for (WaybillLeg legEntity : legEntities) {
            legNoMap.put(legEntity.getId(), legEntity.getLegNo());
        }

        List<WaybillLegPayload> legs = legEntities.stream()
            .map(this::toLegPayload)
            .toList();
        List<WaybillEventPayload> events = waybillTrackEventMapper.selectByWaybillId(order.getId()).stream()
            .map(event -> toEventPayload(event, legNoMap))
            .toList();

        return new WaybillDetailResponse(
            order.getId(),
            order.getMainTrackingNo(),
            order.getReferenceNo(),
            order.getCustomerName(),
            order.getCustomerPhone(),
            order.getOriginWarehouse(),
            order.getDestinationCountry(),
            order.getDestinationCity(),
            order.getMemberId(),
            order.getRouteType(),
            order.getCurrentStatus(),
            order.getCurrentNode(),
            order.getCargoDescription(),
            order.getPackageCount(),
            order.getWeightKg(),
            order.getRemark(),
            formatDateTime(order.getCreatedAt()),
            formatDateTime(order.getUpdatedAt()),
            legs,
            events
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

    private WaybillOrder requireWaybill(Long id) {
        WaybillOrder order = waybillOrderMapper.selectActiveById(id);
        if (order == null) {
            throw new NotFoundException("运单不存在");
        }
        return order;
    }

    private void ensureMainTrackingNoAvailable(String mainTrackingNo, Long currentId) {
        String normalized = trimToNull(mainTrackingNo);
        if (normalized == null) {
            throw new BusinessException("主运单号不能为空");
        }

        WaybillOrder existing = waybillOrderMapper.selectActiveByMainTrackingNo(normalized);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException("主运单号已存在");
        }
    }

    private void applyRequest(WaybillOrder order, WaybillSaveRequest request, LocalDateTime now) {
        validateRequest(request);
        order.setMainTrackingNo(request.mainTrackingNo().trim());
        order.setReferenceNo(trimToEmpty(request.referenceNo()));
        order.setCustomerName(request.customerName().trim());
        order.setCustomerPhone(trimToEmpty(request.customerPhone()));
        order.setOriginWarehouse(trimToEmpty(request.originWarehouse()));
        order.setDestinationCountry(request.destinationCountry().trim());
        order.setDestinationCity(trimToEmpty(request.destinationCity()));
        order.setRouteType(request.routeType().trim());
        order.setCurrentStatus(request.currentStatus().trim());
        order.setCurrentNode(trimToEmpty(request.currentNode()));
        order.setCargoDescription(trimToEmpty(request.cargoDescription()));
        order.setPackageCount(request.packageCount());
        order.setWeightKg(request.weightKg());
        order.setRemark(trimToEmpty(request.remark()));
        order.setUpdatedAt(now);
    }

    private void validateRequest(WaybillSaveRequest request) {
        if (request.packageCount() == null || request.packageCount() < 1) {
            throw new BusinessException("包裹数量必须大于 0");
        }
        if (request.legs() == null || request.legs().isEmpty()) {
            throw new BusinessException("至少需要填写一条线路分段");
        }
        if (request.events() == null || request.events().isEmpty()) {
            throw new BusinessException("至少需要填写一条轨迹事件");
        }

        validateLegs(request.legs());
        validateEvents(request.events(), request.legs().size());
    }

    private void validateLegs(List<WaybillLegPayload> legs) {
        for (int index = 0; index < legs.size(); index++) {
            WaybillLegPayload leg = legs.get(index);
            int rowNo = index + 1;

            if (trimToNull(leg.legType()) == null) {
                throw new BusinessException("第 " + rowNo + " 条分段未填写分段类型");
            }
            if (trimToNull(leg.trackingNo()) == null) {
                throw new BusinessException("第 " + rowNo + " 条分段未填写分段运单号");
            }
            if (trimToNull(leg.departureTime()) != null && trimToNull(leg.arrivalTime()) != null) {
                LocalDateTime departureTime = parseDateTime(leg.departureTime());
                LocalDateTime arrivalTime = parseDateTime(leg.arrivalTime());
                if (arrivalTime.isBefore(departureTime)) {
                    throw new BusinessException("第 " + rowNo + " 条分段到达时间不能早于发出时间");
                }
            }
        }
    }

    private void validateEvents(List<WaybillEventPayload> events, int legCount) {
        for (int index = 0; index < events.size(); index++) {
            WaybillEventPayload event = events.get(index);
            int rowNo = index + 1;

            if (trimToNull(event.eventTime()) == null) {
                throw new BusinessException("第 " + rowNo + " 条事件未填写事件时间");
            }
            if (trimToNull(event.eventStatus()) == null) {
                throw new BusinessException("第 " + rowNo + " 条事件未填写事件状态");
            }
            if (trimToNull(event.eventDescription()) == null) {
                throw new BusinessException("第 " + rowNo + " 条事件未填写事件描述");
            }
            parseDateTime(event.eventTime());

            if (event.legId() != null && (event.legId() < 1 || event.legId() > legCount)) {
                throw new BusinessException("第 " + rowNo + " 条事件关联的分段序号无效");
            }
        }
    }

    private void replaceChildren(Long waybillId, WaybillSaveRequest request, LocalDateTime now) {
        waybillLegMapper.deleteByWaybillId(waybillId);
        waybillTrackEventMapper.deleteByWaybillId(waybillId);

        Map<Integer, Long> legIdMap = new HashMap<>();
        for (int index = 0; index < request.legs().size(); index++) {
            WaybillLegPayload legPayload = request.legs().get(index);
            WaybillLeg leg = new WaybillLeg();
            Integer legNo = legPayload.legNo() == null ? index + 1 : legPayload.legNo();
            leg.setWaybillId(waybillId);
            leg.setLegNo(legNo);
            leg.setLegType(legPayload.legType().trim());
            leg.setCarrierName(trimToEmpty(legPayload.carrierName()));
            leg.setTrackingNo(legPayload.trackingNo().trim());
            leg.setFromNode(trimToEmpty(legPayload.fromNode()));
            leg.setToNode(trimToEmpty(legPayload.toNode()));
            leg.setLegStatus(defaultValue(legPayload.legStatus(), "pending"));
            leg.setTransferFlag(Boolean.TRUE.equals(legPayload.transferFlag()) ? 1 : 0);
            leg.setDepartureTime(parseDateTime(legPayload.departureTime()));
            leg.setArrivalTime(parseDateTime(legPayload.arrivalTime()));
            leg.setRemark(trimToEmpty(legPayload.remark()));
            leg.setCreatedAt(now);
            leg.setUpdatedAt(now);
            waybillLegMapper.insert(leg);
            legIdMap.put(legNo, leg.getId());
        }

        for (int index = 0; index < request.events().size(); index++) {
            WaybillEventPayload eventPayload = request.events().get(index);
            WaybillTrackEvent event = new WaybillTrackEvent();
            event.setWaybillId(waybillId);
            event.setLegId(eventPayload.legId() == null ? null : legIdMap.get(eventPayload.legId().intValue()));
            event.setEventTime(parseDateTime(eventPayload.eventTime()));
            event.setEventStatus(eventPayload.eventStatus().trim());
            event.setEventDescription(eventPayload.eventDescription().trim());
            event.setEventLocation(trimToEmpty(eventPayload.eventLocation()));
            event.setVisibleToCustomer(Boolean.FALSE.equals(eventPayload.visibleToCustomer()) ? 0 : 1);
            event.setSortNo(eventPayload.sortNo() == null ? index + 1 : eventPayload.sortNo());
            event.setCreatedAt(now);
            waybillTrackEventMapper.insert(event);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(trimmed, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("时间格式必须为 yyyy-MM-dd HH:mm:ss");
        }
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

    private String defaultValue(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }
}
