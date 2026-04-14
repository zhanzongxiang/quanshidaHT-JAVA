package com.qsd.admin.waybill.service;

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

        replaceChildren(order.getId(), request);
        return toDetailResponse(requireWaybill(order.getId()));
    }

    @Transactional
    public WaybillDetailResponse update(Long id, WaybillSaveRequest request) {
        WaybillOrder order = requireWaybill(id);
        ensureMainTrackingNoAvailable(request.mainTrackingNo(), id);

        applyRequest(order, request, LocalDateTime.now());
        waybillOrderMapper.updateById(order);

        replaceChildren(id, request);
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
            throw new NotFoundException("waybill not found");
        }
        return order;
    }

    private void ensureMainTrackingNoAvailable(String mainTrackingNo, Long currentId) {
        WaybillOrder existing = waybillOrderMapper.selectActiveByMainTrackingNo(mainTrackingNo.trim());
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new IllegalArgumentException("mainTrackingNo already exists");
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
            throw new IllegalArgumentException("packageCount must be greater than 0");
        }
        if (request.legs() == null || request.legs().isEmpty()) {
            throw new IllegalArgumentException("at least one route leg is required");
        }
        if (request.events() == null || request.events().isEmpty()) {
            throw new IllegalArgumentException("at least one tracking event is required");
        }
    }

    private void replaceChildren(Long waybillId, WaybillSaveRequest request) {
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
            leg.setCreatedAt(LocalDateTime.now());
            leg.setUpdatedAt(LocalDateTime.now());
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
            event.setCreatedAt(LocalDateTime.now());
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
            throw new IllegalArgumentException("datetime format must be yyyy-MM-dd HH:mm:ss");
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
