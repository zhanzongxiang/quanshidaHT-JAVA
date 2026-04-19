package com.qsd.admin.waybill.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qsd.admin.dictionary.service.DictionaryService;
import com.qsd.admin.waybill.entity.WaybillLeg;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.entity.WaybillTrackEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WaybillPublicService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WaybillService waybillService;
    private final ObjectMapper objectMapper;
    private final DictionaryService dictionaryService;

    public WaybillPublicService(
        WaybillService waybillService,
        ObjectMapper objectMapper,
        DictionaryService dictionaryService
    ) {
        this.waybillService = waybillService;
        this.objectMapper = objectMapper;
        this.dictionaryService = dictionaryService;
    }

    public JsonNode findTrackingResult(String trackingNo) {
        WaybillOrder order = waybillService.findByTrackingNo(trackingNo);
        if (order == null) {
            return null;
        }

        List<WaybillLeg> legs = waybillService.listLegs(order.getId());
        List<WaybillTrackEvent> events = waybillService.listVisibleEvents(order.getId());

        ObjectNode root = objectMapper.createObjectNode();
        root.put("trackingId", order.getMainTrackingNo());
        root.put("status", dictionaryService.labelOf("waybill_status", order.getCurrentStatus()));
        root.put("origin", defaultText(order.getOriginWarehouse()));
        root.put("destination", buildDestination(order));
        root.put("isException", isExceptionStatus(order.getCurrentStatus()));
        root.put("currentNode", defaultText(order.getCurrentNode()));

        ArrayNode steps = objectMapper.createArrayNode();
        for (WaybillTrackEvent event : events) {
            ObjectNode step = objectMapper.createObjectNode();
            step.put("time", formatDateTime(event.getEventTime()));
            step.put("status", dictionaryService.labelOf("waybill_status", event.getEventStatus()));
            step.put("description", defaultText(event.getEventDescription()));
            step.put("location", defaultText(event.getEventLocation()));
            step.put("isException", isExceptionStatus(event.getEventStatus()));
            steps.add(step);
        }
        root.set("steps", steps);

        ArrayNode legNodes = objectMapper.createArrayNode();
        for (WaybillLeg leg : legs) {
            ObjectNode legNode = objectMapper.createObjectNode();
            legNode.put("legNo", leg.getLegNo());
            legNode.put("legType", defaultText(leg.getLegType()));
            legNode.put("carrierName", defaultText(leg.getCarrierName()));
            legNode.put("trackingNo", defaultText(leg.getTrackingNo()));
            legNode.put("fromNode", defaultText(leg.getFromNode()));
            legNode.put("toNode", defaultText(leg.getToNode()));
            legNode.put("status", dictionaryService.labelOf("waybill_leg_status", leg.getLegStatus()));
            legNode.put("isTransfer", leg.getTransferFlag() != null && leg.getTransferFlag() == 1);
            legNodes.add(legNode);
        }
        root.set("legs", legNodes);
        return root;
    }

    private String buildDestination(WaybillOrder order) {
        String country = defaultText(order.getDestinationCountry());
        String city = defaultText(order.getDestinationCity());
        if (!country.isBlank() && !city.isBlank()) {
            return country + " / " + city;
        }
        return country + city;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMATTER.format(value);
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private boolean isExceptionStatus(String value) {
        return "exception".equalsIgnoreCase(value) || "运输异常".equals(value);
    }
}
