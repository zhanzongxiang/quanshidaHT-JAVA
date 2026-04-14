package com.qsd.admin.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qsd.admin.content.dto.ServiceLineContentResponse;
import com.qsd.admin.content.dto.ServiceLineSummaryResponse;
import com.qsd.admin.content.entity.SiteContentPage;
import com.qsd.admin.content.mapper.SiteContentPageMapper;
import com.qsd.admin.website.service.PublicWebsiteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceLineContentService {
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, ServiceLineDefinition> SERVICE_LINE_DEFINITIONS = new LinkedHashMap<>();

    static {
        SERVICE_LINE_DEFINITIONS.put(
            "taiwan",
            new ServiceLineDefinition("taiwan", "台湾专线", "/taiwan", "Taiwan Line", "台湾专线固定模板页面", null)
        );
        SERVICE_LINE_DEFINITIONS.put(
            "feizhou",
            new ServiceLineDefinition("feizhou", "非洲专线", "/feizhou", "Africa Line", "非洲专线固定模板页面", "africa")
        );
        SERVICE_LINE_DEFINITIONS.put(
            "international",
            new ServiceLineDefinition("international", "国际快递", "/international", "International Express", "国际快递固定模板页面", "express")
        );
    }

    private final SiteContentPageMapper siteContentPageMapper;
    private final ObjectMapper objectMapper;
    private final PublicWebsiteService publicWebsiteService;

    public ServiceLineContentService(
        SiteContentPageMapper siteContentPageMapper,
        ObjectMapper objectMapper,
        PublicWebsiteService publicWebsiteService
    ) {
        this.siteContentPageMapper = siteContentPageMapper;
        this.objectMapper = objectMapper;
        this.publicWebsiteService = publicWebsiteService;
    }

    public List<ServiceLineSummaryResponse> listServiceLines() {
        return SERVICE_LINE_DEFINITIONS.values().stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    public ServiceLineContentResponse getServiceLine(String lineCode) {
        ServiceLineDefinition definition = getDefinition(lineCode);
        return toContentResponse(definition, ensureServiceLineContent(definition));
    }

    public ServiceLineContentResponse saveDraft(String lineCode, JsonNode form) {
        ServiceLineDefinition definition = getDefinition(lineCode);
        return toContentResponse(definition, save(definition, form, STATUS_DRAFT));
    }

    public ServiceLineContentResponse publish(String lineCode, JsonNode form) {
        ServiceLineDefinition definition = getDefinition(lineCode);
        return toContentResponse(definition, save(definition, form, STATUS_PUBLISHED));
    }

    private ServiceLineSummaryResponse toSummaryResponse(ServiceLineDefinition definition) {
        SiteContentPage page = ensureServiceLineContent(definition);
        JsonNode form = readFormJson(page.getFormJson(), definition);
        return new ServiceLineSummaryResponse(
            definition.lineCode(),
            definition.lineName(),
            definition.linePath(),
            form.path("description").asText(definition.description()),
            page.getStatus(),
            formatDateTime(page.getUpdatedAt()),
            formatDateTime(page.getPublishedAt())
        );
    }

    private ServiceLineContentResponse toContentResponse(ServiceLineDefinition definition, SiteContentPage page) {
        return new ServiceLineContentResponse(
            page.getPageCode(),
            definition.lineCode(),
            definition.lineName(),
            definition.linePath(),
            page.getStatus(),
            formatDateTime(page.getUpdatedAt()),
            formatDateTime(page.getPublishedAt()),
            readFormJson(page.getFormJson(), definition)
        );
    }

    private SiteContentPage save(ServiceLineDefinition definition, JsonNode form, String status) {
        validateForm(form);

        SiteContentPage page = ensureServiceLineContent(definition);
        LocalDateTime now = LocalDateTime.now();
        page.setStatus(status);
        page.setFormJson(writeFormJson(normalizeForm(form, definition)));
        page.setUpdatedAt(now);
        if (STATUS_PUBLISHED.equals(status)) {
            page.setPublishedAt(now);
        }
        siteContentPageMapper.updateById(page);
        if (STATUS_PUBLISHED.equals(status)) {
            publicWebsiteService.evictPublishedPageCache(page.getPageCode());
        }
        return page;
    }

    private SiteContentPage ensureServiceLineContent(ServiceLineDefinition definition) {
        SiteContentPage page = siteContentPageMapper.selectByPageCode(toPageCode(definition.lineCode()));
        if (page == null && definition.legacyCode() != null) {
            page = siteContentPageMapper.selectByPageCode(toPageCode(definition.legacyCode()));
            if (page != null) {
                page.setPageCode(toPageCode(definition.lineCode()));
                siteContentPageMapper.updateById(page);
            }
        }

        if (page != null) {
            page.setFormJson(writeFormJson(readFormJson(page.getFormJson(), definition)));
            return page;
        }

        LocalDateTime now = LocalDateTime.now();
        SiteContentPage initial = new SiteContentPage();
        initial.setPageCode(toPageCode(definition.lineCode()));
        initial.setStatus(STATUS_DRAFT);
        initial.setFormJson(writeFormJson(createDefaultForm(definition)));
        initial.setCreatedAt(now);
        initial.setUpdatedAt(now);
        siteContentPageMapper.insert(initial);
        return initial;
    }

    private ServiceLineDefinition getDefinition(String lineCode) {
        String resolvedCode = switch (lineCode) {
            case "africa" -> "feizhou";
            case "express" -> "international";
            default -> lineCode;
        };

        ServiceLineDefinition definition = SERVICE_LINE_DEFINITIONS.get(resolvedCode);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported service line code");
        }
        return definition;
    }

    private String toPageCode(String lineCode) {
        return "service-line:" + lineCode;
    }

    private void validateForm(JsonNode form) {
        if (form == null || form.isNull() || !form.isObject()) {
            throw new IllegalArgumentException("form must be an object");
        }
    }

    private String writeFormJson(JsonNode form) {
        try {
            return objectMapper.writeValueAsString(form);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("failed to serialize service line content");
        }
    }

    private JsonNode readFormJson(String formJson, ServiceLineDefinition definition) {
        if (formJson == null || formJson.isBlank()) {
            return createDefaultForm(definition);
        }

        try {
            JsonNode node = objectMapper.readTree(formJson);
            return normalizeForm(node, definition);
        } catch (JsonProcessingException ex) {
            return createDefaultForm(definition);
        }
    }

    private ObjectNode normalizeForm(JsonNode source, ServiceLineDefinition definition) {
        if (source == null || !source.isObject()) {
            return createDefaultForm(definition);
        }

        ObjectNode normalized = createDefaultForm(definition);

        if (source.has("key")) {
            normalized.put("key", definition.lineCode());
            normalized.put("eyebrow", source.path("eyebrow").asText(definition.eyebrow()));
            normalized.put("title", source.path("title").asText(definition.lineName() + "服务"));
            normalized.put("subtitle", source.path("subtitle").asText(""));
            normalized.put("description", source.path("description").asText(definition.description()));
            normalized.put("heroImage", source.path("heroImage").asText(""));
            normalized.set("heroTags", normalizeStringArray(source.path("heroTags")));
            normalized.set("metrics", normalizeMetrics(source.path("metrics")));
            normalized.put("highlightsTitle", source.path("highlightsTitle").asText(""));
            normalized.put("highlightsSubtitle", source.path("highlightsSubtitle").asText(""));
            normalized.set("highlights", normalizeHighlights(source.path("highlights")));
            normalized.put("processTitle", source.path("processTitle").asText(""));
            normalized.put("processSubtitle", source.path("processSubtitle").asText(""));
            normalized.set("processSteps", normalizeStringArray(source.path("processSteps")));
            normalized.put("scopeTitle", source.path("scopeTitle").asText(""));
            normalized.put("scopeSubtitle", source.path("scopeSubtitle").asText(""));
            normalized.put("scopeImage", source.path("scopeImage").asText(""));
            normalized.set("scopeItems", normalizeStringArray(source.path("scopeItems")));
            normalized.put("supportTitle", source.path("supportTitle").asText(""));
            normalized.put("supportDescription", source.path("supportDescription").asText(""));
            normalized.set("supportItems", normalizeStringArray(source.path("supportItems")));
            normalized.put("ctaTitle", source.path("ctaTitle").asText(""));
            normalized.put("ctaSubtitle", source.path("ctaSubtitle").asText(""));
            return normalized;
        }

        normalized.put("key", definition.lineCode());
        normalized.put("eyebrow", definition.eyebrow());
        normalized.put("title", source.path("hero").path("title").asText(source.path("basic").path("pageName").asText(definition.lineName() + "服务")));
        normalized.put("subtitle", source.path("hero").path("subtitle").asText(""));
        normalized.put("description", source.path("basic").path("summary").asText(definition.description()));
        normalized.put("heroImage", source.path("hero").path("backgroundImageUrl").asText(""));
        normalized.set("heroTags", objectMapper.createArrayNode());
        normalized.set("metrics", objectMapper.createArrayNode());
        normalized.put("highlightsTitle", source.path("advantages").path("title").asText(""));
        normalized.put("highlightsSubtitle", source.path("advantages").path("subtitle").asText(""));
        normalized.set("highlights", normalizeLegacyHighlights(source.path("advantages").path("items")));
        normalized.put("processTitle", source.path("process").path("title").asText(""));
        normalized.put("processSubtitle", source.path("process").path("subtitle").asText(""));
        normalized.set("processSteps", normalizeLegacyProcessSteps(source.path("process").path("steps")));
        normalized.put("scopeTitle", source.path("coverage").path("title").asText(""));
        normalized.put("scopeSubtitle", source.path("coverage").path("description").asText(""));
        normalized.put("scopeImage", source.path("coverage").path("imageUrl").asText(""));
        normalized.set("scopeItems", normalizeLegacyCoverageItems(source.path("coverage").path("items")));
        normalized.put("supportTitle", "");
        normalized.put("supportDescription", "");
        normalized.set("supportItems", objectMapper.createArrayNode());
        normalized.put("ctaTitle", source.path("cta").path("title").asText(""));
        normalized.put("ctaSubtitle", source.path("cta").path("description").asText(""));
        return normalized;
    }

    private ArrayNode normalizeMetrics(JsonNode source) {
        ArrayNode metrics = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                ObjectNode metric = objectMapper.createObjectNode();
                metric.put("label", item.path("label").asText(""));
                metric.put("value", item.path("value").asText(""));
                metrics.add(metric);
            }
        }
        return metrics;
    }

    private ArrayNode normalizeHighlights(JsonNode source) {
        ArrayNode highlights = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                ObjectNode highlight = objectMapper.createObjectNode();
                highlight.put("title", item.path("title").asText(""));
                highlight.put("description", item.path("description").asText(""));
                highlight.put("icon", item.path("icon").asText(""));
                highlights.add(highlight);
            }
        }
        return highlights;
    }

    private ArrayNode normalizeLegacyHighlights(JsonNode source) {
        ArrayNode highlights = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                ObjectNode highlight = objectMapper.createObjectNode();
                highlight.put("title", item.path("title").asText(""));
                highlight.put("description", item.path("description").asText(""));
                highlight.put("icon", item.path("iconUrl").asText(""));
                highlights.add(highlight);
            }
        }
        return highlights;
    }

    private ArrayNode normalizeLegacyProcessSteps(JsonNode source) {
        ArrayNode steps = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode step : source) {
                String value = step.path("title").asText("");
                if (value.isBlank()) {
                    value = step.path("description").asText("");
                }
                steps.add(value);
            }
        }
        return steps;
    }

    private ArrayNode normalizeLegacyCoverageItems(JsonNode source) {
        ArrayNode items = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                items.add(item.path("label").asText(""));
            }
        }
        return items;
    }

    private ArrayNode normalizeStringArray(JsonNode source) {
        ArrayNode items = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                items.add(item.asText(""));
            }
        }
        return items;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private ObjectNode createDefaultForm(ServiceLineDefinition definition) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", definition.lineCode());
        root.put("eyebrow", definition.eyebrow());
        root.put("title", definition.lineName() + "服务");
        root.put("subtitle", "");
        root.put("description", definition.description());
        root.put("heroImage", "");
        root.set("heroTags", objectMapper.createArrayNode());
        root.set("metrics", objectMapper.createArrayNode());
        root.put("highlightsTitle", "");
        root.put("highlightsSubtitle", "");
        root.set("highlights", objectMapper.createArrayNode());
        root.put("processTitle", "");
        root.put("processSubtitle", "");
        root.set("processSteps", objectMapper.createArrayNode());
        root.put("scopeTitle", "");
        root.put("scopeSubtitle", "");
        root.put("scopeImage", "");
        root.set("scopeItems", objectMapper.createArrayNode());
        root.put("supportTitle", "");
        root.put("supportDescription", "");
        root.set("supportItems", objectMapper.createArrayNode());
        root.put("ctaTitle", "");
        root.put("ctaSubtitle", "");
        return root;
    }

    private record ServiceLineDefinition(
        String lineCode,
        String lineName,
        String linePath,
        String eyebrow,
        String description,
        String legacyCode
    ) {
    }
}
