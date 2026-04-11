package com.qsd.admin.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
        page.setFormJson(writeFormJson(normalizeForm(form)));
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
            page.setFormJson(writeFormJson(readFormJson(page.getFormJson())));
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
            return normalizeForm(node);
        } catch (JsonProcessingException ex) {
            return createDefaultForm();
        }
    }

    private ObjectNode normalizeForm(JsonNode source) {
        if (source == null || !source.isObject()) {
            return createDefaultForm();
        }

        ObjectNode normalized = createDefaultForm();

        ObjectNode hero = normalized.with("hero");
        ArrayNode slides = hero.putArray("slides");

        if (source.path("hero").path("slides").isArray()) {
            for (JsonNode slide : source.path("hero").path("slides")) {
                slides.add(normalizeSlide(slide));
            }
        } else if (source.path("hero").path("images").isArray()) {
            ArrayNode images = (ArrayNode) source.path("hero").path("images");
            for (int i = 0; i < images.size(); i++) {
                JsonNode image = images.get(i);
                ObjectNode slide = normalizeSlide(image);
                if (i == 0) {
                    slide.put("title", source.path("hero").path("title").asText(""));
                    slide.put("subtitle", source.path("hero").path("subtitle").asText(""));
                    slide.set("primaryButton", createButtonNode(
                        source.path("hero").path("primaryButtonText").asText(""),
                        source.path("hero").path("primaryButtonLink").asText("")
                    ));
                    slide.set("secondaryButton", createButtonNode(
                        source.path("hero").path("secondaryButtonText").asText(""),
                        source.path("hero").path("secondaryButtonLink").asText("")
                    ));
                }
                slides.add(slide);
            }
        }

        ObjectNode trackingSection = normalized.with("trackingSection");
        JsonNode currentTracking = source.has("trackingSection") ? source.path("trackingSection") : source.path("tracking");
        trackingSection.put("title", currentTracking.path("title").asText(""));
        trackingSection.put("inputPlaceholder", currentTracking.path("inputPlaceholder").asText(currentTracking.path("placeholder").asText("")));
        trackingSection.put("searchButtonText", currentTracking.path("searchButtonText").asText(currentTracking.path("buttonText").asText("")));
        trackingSection.put("emptyText", currentTracking.path("emptyText").asText(""));
        trackingSection.put("notFoundText", currentTracking.path("notFoundText").asText(""));
        trackingSection.put("loadingText", currentTracking.path("loadingText").asText(""));

        ObjectNode businessSection = normalized.with("businessSection");
        JsonNode currentBusiness = source.has("businessSection") ? source.path("businessSection") : source.path("servicesSection");
        businessSection.put("title", currentBusiness.path("title").asText(""));
        businessSection.put("subtitle", currentBusiness.path("subtitle").asText(currentBusiness.path("description").asText("")));
        ArrayNode businessItems = businessSection.putArray("items");
        if (currentBusiness.path("items").isArray()) {
            for (JsonNode item : currentBusiness.path("items")) {
                ObjectNode normalizedItem = objectMapper.createObjectNode();
                normalizedItem.put("title", item.path("title").asText(item.path("name").asText("")));
                normalizedItem.put("description", item.path("description").asText(""));
                normalizedItem.put("icon", item.path("icon").asText(item.path("iconUrl").asText("")));
                normalizedItem.put("url", item.path("url").asText(item.path("link").asText("")));
                businessItems.add(normalizedItem);
            }
        }

        ObjectNode processSection = normalized.with("processSection");
        JsonNode currentProcess = source.path("processSection");
        processSection.put("title", currentProcess.path("title").asText(""));
        processSection.put("subtitle", currentProcess.path("subtitle").asText(""));
        ArrayNode processSteps = processSection.putArray("steps");
        if (currentProcess.path("steps").isArray()) {
            for (JsonNode step : currentProcess.path("steps")) {
                ObjectNode normalizedStep = objectMapper.createObjectNode();
                normalizedStep.put("title", step.path("title").asText(""));
                normalizedStep.put("description", step.path("description").asText(""));
                processSteps.add(normalizedStep);
            }
        }

        ObjectNode promiseSection = normalized.with("promiseSection");
        JsonNode currentPromise = source.path("promiseSection");
        promiseSection.put("title", currentPromise.path("title").asText(""));
        promiseSection.put("subtitle", currentPromise.path("subtitle").asText(""));
        ArrayNode promiseItems = promiseSection.putArray("items");
        if (currentPromise.path("items").isArray()) {
            for (JsonNode item : currentPromise.path("items")) {
                ObjectNode normalizedItem = objectMapper.createObjectNode();
                normalizedItem.put("title", item.path("title").asText(""));
                normalizedItem.put("description", item.path("description").asText(item.path("subtitle").asText("")));
                normalizedItem.put("icon", item.path("icon").asText(item.path("iconUrl").asText("")));
                normalizedItem.put("imageUrl", item.path("imageUrl").asText(""));
                promiseItems.add(normalizedItem);
            }
        }
        if (promiseItems.isEmpty()) {
            for (JsonNode item : createDefaultPromiseItems()) {
                promiseItems.add(item);
            }
        }

        ObjectNode newsPreviewSection = normalized.with("newsPreviewSection");
        JsonNode currentNewsPreview = source.path("newsPreviewSection");
        newsPreviewSection.put("title", currentNewsPreview.path("title").asText(""));
        newsPreviewSection.put("subtitle", currentNewsPreview.path("subtitle").asText(""));
        newsPreviewSection.put("viewAllText", currentNewsPreview.path("viewAllText").asText(""));
        newsPreviewSection.put("viewAllUrl", currentNewsPreview.path("viewAllUrl").asText(""));
        ArrayNode newsItems = newsPreviewSection.putArray("items");
        if (currentNewsPreview.path("items").isArray()) {
            for (JsonNode item : currentNewsPreview.path("items")) {
                newsItems.add(item.deepCopy());
            }
        }

        ObjectNode seo = normalized.with("seo");
        JsonNode currentSeo = source.path("seo");
        seo.put("title", currentSeo.path("title").asText(""));
        seo.put("description", currentSeo.path("description").asText(""));
        seo.put("keywords", currentSeo.path("keywords").asText(""));

        return normalized;
    }

    private ObjectNode normalizeSlide(JsonNode source) {
        ObjectNode slide = objectMapper.createObjectNode();
        slide.put("image", source.path("image").asText(source.path("url").asText("")));
        slide.put("alt", source.path("alt").asText(source.path("name").asText("")));
        slide.put("title", source.path("title").asText(""));
        slide.put("subtitle", source.path("subtitle").asText(""));
        slide.set("primaryButton", normalizeButton(source.path("primaryButton")));
        slide.set("secondaryButton", normalizeButton(source.path("secondaryButton")));
        return slide;
    }

    private JsonNode normalizeButton(JsonNode source) {
        return createButtonNode(source.path("text").asText(""), source.path("value").asText(""));
    }

    private ObjectNode createButtonNode(String text, String value) {
        ObjectNode button = objectMapper.createObjectNode();
        button.put("text", text);
        button.put("actionType", value.startsWith("/") || value.isBlank() ? "route" : "url");
        button.put("value", value);
        return button;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMATTER.format(dateTime);
    }

    private ObjectNode createDefaultForm() {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode hero = root.putObject("hero");
        hero.set("slides", objectMapper.createArrayNode());

        ObjectNode trackingSection = root.putObject("trackingSection");
        trackingSection.put("title", "");
        trackingSection.put("inputPlaceholder", "");
        trackingSection.put("searchButtonText", "");
        trackingSection.put("emptyText", "");
        trackingSection.put("notFoundText", "");
        trackingSection.put("loadingText", "");

        ObjectNode businessSection = root.putObject("businessSection");
        businessSection.put("title", "");
        businessSection.put("subtitle", "");
        businessSection.set("items", objectMapper.createArrayNode());

        ObjectNode processSection = root.putObject("processSection");
        processSection.put("title", "");
        processSection.put("subtitle", "");
        processSection.set("steps", objectMapper.createArrayNode());

        ObjectNode promiseSection = root.putObject("promiseSection");
        promiseSection.put("title", "");
        promiseSection.put("subtitle", "");
        promiseSection.set("items", createDefaultPromiseItems());

        ObjectNode newsPreviewSection = root.putObject("newsPreviewSection");
        newsPreviewSection.put("title", "");
        newsPreviewSection.put("subtitle", "");
        newsPreviewSection.put("viewAllText", "");
        newsPreviewSection.put("viewAllUrl", "");
        newsPreviewSection.set("items", objectMapper.createArrayNode());

        ObjectNode seo = root.putObject("seo");
        seo.put("title", "");
        seo.put("description", "");
        seo.put("keywords", "");
        return root;
    }

    private ArrayNode createDefaultPromiseItems() {
        return objectMapper.createArrayNode()
            .add(createPromiseItem())
            .add(createPromiseItem())
            .add(createPromiseItem())
            .add(createPromiseItem())
            .add(createPromiseItem())
            .add(createPromiseItem());
    }

    private ObjectNode createPromiseItem() {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("title", "");
        item.put("description", "");
        item.put("icon", "");
        item.put("imageUrl", "");
        return item;
    }
}
