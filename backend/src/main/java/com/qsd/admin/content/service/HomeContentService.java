package com.qsd.admin.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qsd.admin.content.dto.HomeContentResponse;
import com.qsd.admin.content.entity.SiteContentPage;
import com.qsd.admin.content.mapper.SiteContentPageMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class HomeContentService {
    private static final String HOME_PAGE_CODE = "home";
    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_PUBLISHED = "published";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SiteContentPageMapper siteContentPageMapper;
    private final ObjectMapper objectMapper;

    public HomeContentService(SiteContentPageMapper siteContentPageMapper, ObjectMapper objectMapper) {
        this.siteContentPageMapper = siteContentPageMapper;
        this.objectMapper = objectMapper;
    }

    public HomeContentResponse getHomeContent() {
        return toResponse(ensureHomeContent());
    }

    public HomeContentResponse saveDraft(JsonNode form) {
        return toResponse(save(form, STATUS_DRAFT));
    }

    public HomeContentResponse publish(JsonNode form) {
        return toResponse(save(form, STATUS_PUBLISHED));
    }

    private SiteContentPage save(JsonNode form, String status) {
        validateForm(form);

        SiteContentPage page = ensureHomeContent();
        LocalDateTime now = LocalDateTime.now();
        page.setStatus(status);
        page.setFormJson(writeFormJson(form));
        page.setUpdatedAt(now);
        if (STATUS_PUBLISHED.equals(status)) {
            page.setPublishedAt(now);
        }
        siteContentPageMapper.updateById(page);
        return page;
    }

    private SiteContentPage ensureHomeContent() {
        SiteContentPage page = siteContentPageMapper.selectByPageCode(HOME_PAGE_CODE);
        if (page != null) {
            return page;
        }

        LocalDateTime now = LocalDateTime.now();
        SiteContentPage initial = new SiteContentPage();
        initial.setPageCode(HOME_PAGE_CODE);
        initial.setStatus(STATUS_DRAFT);
        initial.setFormJson(writeFormJson(createDefaultForm()));
        initial.setCreatedAt(now);
        initial.setUpdatedAt(now);
        initial.setPublishedAt(null);
        siteContentPageMapper.insert(initial);
        return initial;
    }

    private void validateForm(JsonNode form) {
        if (form == null || form.isNull() || !form.isObject()) {
            throw new IllegalArgumentException("form must be an object");
        }
    }

    private HomeContentResponse toResponse(SiteContentPage page) {
        return new HomeContentResponse(
            page.getPageCode(),
            page.getStatus(),
            formatDateTime(page.getUpdatedAt()),
            formatDateTime(page.getPublishedAt()),
            readFormJson(page.getFormJson())
        );
    }

    private String writeFormJson(JsonNode form) {
        try {
            return objectMapper.writeValueAsString(form);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("failed to serialize home content");
        }
    }

    private JsonNode readFormJson(String formJson) {
        if (formJson == null || formJson.isBlank()) {
            return createDefaultForm();
        }

        try {
            JsonNode node = objectMapper.readTree(formJson);
            return node != null && node.isObject() ? node : createDefaultForm();
        } catch (JsonProcessingException ex) {
            return createDefaultForm();
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private ObjectNode createDefaultForm() {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode hero = root.putObject("hero");
        hero.put("enabled", true);
        hero.set("images", objectMapper.createArrayNode());
        hero.put("title", "");
        hero.put("subtitle", "");
        hero.put("primaryButtonText", "");
        hero.put("primaryButtonLink", "");
        hero.put("secondaryButtonText", "");
        hero.put("secondaryButtonLink", "");

        ObjectNode tracking = root.putObject("tracking");
        tracking.put("enabled", true);
        tracking.put("title", "");
        tracking.put("placeholder", "");
        tracking.put("buttonText", "");

        ObjectNode servicesSection = root.putObject("servicesSection");
        servicesSection.put("enabled", true);
        servicesSection.put("title", "");
        servicesSection.put("description", "");
        servicesSection.set("items", objectMapper.createArrayNode());

        ObjectNode processSection = root.putObject("processSection");
        processSection.put("enabled", true);
        processSection.put("title", "");
        processSection.put("subtitle", "");
        processSection.set("steps", objectMapper.createArrayNode());

        ObjectNode promiseSection = root.putObject("promiseSection");
        promiseSection.put("title", "");
        promiseSection.put("subtitle", "");
        promiseSection.set("items", createDefaultPromiseItems());

        ObjectNode seo = root.putObject("seo");
        seo.put("title", "");
        seo.put("description", "");
        seo.put("keywords", "");
        return root;
    }

    private JsonNode createDefaultPromiseItems() {
        return objectMapper.createArrayNode()
            .add(createPromiseItem("promise-1"))
            .add(createPromiseItem("promise-2"))
            .add(createPromiseItem("promise-3"))
            .add(createPromiseItem("promise-4"))
            .add(createPromiseItem("promise-5"))
            .add(createPromiseItem("promise-6"));
    }

    private ObjectNode createPromiseItem(String id) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("id", id);
        item.put("iconUrl", "");
        item.put("title", "");
        item.put("subtitle", "");
        return item;
    }
}
