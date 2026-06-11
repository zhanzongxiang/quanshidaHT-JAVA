package com.qsd.admin.content.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.content.dto.HomeContentResponse;
import com.qsd.admin.content.entity.SiteContentPage;
import com.qsd.admin.content.mapper.SiteContentPageMapper;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.TenantContextHolder;
import com.qsd.admin.website.service.PublicWebsiteService;
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
    private final PublicWebsiteService publicWebsiteService;

    public HomeContentService(
        SiteContentPageMapper siteContentPageMapper,
        ObjectMapper objectMapper,
        PublicWebsiteService publicWebsiteService
    ) {
        this.siteContentPageMapper = siteContentPageMapper;
        this.objectMapper = objectMapper;
        this.publicWebsiteService = publicWebsiteService;
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
        if (STATUS_PUBLISHED.equals(status)) {
            publicWebsiteService.evictPublishedPageCache(HOME_PAGE_CODE);
        }
        return page;
    }

    private SiteContentPage ensureHomeContent() {
        Long tenantId = TenantContextHolder.requireTenantId();
        SiteContentPage page = siteContentPageMapper.selectByPageCode(tenantId, HOME_PAGE_CODE);
        if (page != null) {
            page.setFormJson(writeFormJson(readFormJson(page.getFormJson())));
            return page;
        }

        LocalDateTime now = LocalDateTime.now();
        SiteContentPage initial = new SiteContentPage();
        initial.setTenantId(tenantId);
        initial.setPageCode(HOME_PAGE_CODE);
        initial.setStatus(STATUS_PUBLISHED);
        initial.setFormJson(writeFormJson(createDefaultForm()));
        initial.setCreatedAt(now);
        initial.setUpdatedAt(now);
        initial.setPublishedAt(now);
        siteContentPageMapper.insert(initial);
        return initial;
    }

    private void validateForm(JsonNode form) {
        if (form == null || form.isNull() || !form.isObject()) {
            throw new BusinessException("home content form is invalid");
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
            throw new BusinessException("failed to serialize home content");
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
                    slide.put("title", source.path("hero").path("title").asText(slide.path("title").asText("")));
                    slide.put("subtitle", source.path("hero").path("subtitle").asText(slide.path("subtitle").asText("")));
                    slide.set("primaryButton", createButtonNode(
                        source.path("hero").path("primaryButtonText").asText(slide.path("primaryButton").path("text").asText("")),
                        source.path("hero").path("primaryButtonLink").asText(slide.path("primaryButton").path("value").asText(""))
                    ));
                    slide.set("secondaryButton", createButtonNode(
                        source.path("hero").path("secondaryButtonText").asText(slide.path("secondaryButton").path("text").asText("")),
                        source.path("hero").path("secondaryButtonLink").asText(slide.path("secondaryButton").path("value").asText(""))
                    ));
                }
                slides.add(slide);
            }
        }

        ObjectNode trackingSection = normalized.with("trackingSection");
        JsonNode currentTracking = source.has("trackingSection") ? source.path("trackingSection") : source.path("tracking");
        trackingSection.put("title", currentTracking.path("title").asText(trackingSection.path("title").asText("")));
        trackingSection.put(
            "inputPlaceholder",
            currentTracking.path("inputPlaceholder").asText(currentTracking.path("placeholder").asText(trackingSection.path("inputPlaceholder").asText("")))
        );
        trackingSection.put(
            "searchButtonText",
            currentTracking.path("searchButtonText").asText(currentTracking.path("buttonText").asText(trackingSection.path("searchButtonText").asText("")))
        );
        trackingSection.put("emptyText", currentTracking.path("emptyText").asText(trackingSection.path("emptyText").asText("")));
        trackingSection.put("notFoundText", currentTracking.path("notFoundText").asText(trackingSection.path("notFoundText").asText("")));
        trackingSection.put("loadingText", currentTracking.path("loadingText").asText(trackingSection.path("loadingText").asText("")));

        ObjectNode businessSection = normalized.with("businessSection");
        JsonNode currentBusiness = source.has("businessSection") ? source.path("businessSection") : source.path("servicesSection");
        businessSection.put("title", currentBusiness.path("title").asText(businessSection.path("title").asText("")));
        businessSection.put(
            "subtitle",
            currentBusiness.path("subtitle").asText(currentBusiness.path("description").asText(businessSection.path("subtitle").asText("")))
        );
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
        processSection.put("title", currentProcess.path("title").asText(processSection.path("title").asText("")));
        processSection.put("subtitle", currentProcess.path("subtitle").asText(processSection.path("subtitle").asText("")));
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
        promiseSection.put("title", currentPromise.path("title").asText(promiseSection.path("title").asText("")));
        promiseSection.put("subtitle", currentPromise.path("subtitle").asText(promiseSection.path("subtitle").asText("")));
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
        newsPreviewSection.put("title", currentNewsPreview.path("title").asText(newsPreviewSection.path("title").asText("")));
        newsPreviewSection.put("subtitle", currentNewsPreview.path("subtitle").asText(newsPreviewSection.path("subtitle").asText("")));
        newsPreviewSection.put("viewAllText", currentNewsPreview.path("viewAllText").asText(newsPreviewSection.path("viewAllText").asText("")));
        newsPreviewSection.put("viewAllUrl", currentNewsPreview.path("viewAllUrl").asText(newsPreviewSection.path("viewAllUrl").asText("")));
        ArrayNode newsItems = newsPreviewSection.putArray("items");
        if (currentNewsPreview.path("items").isArray()) {
            for (JsonNode item : currentNewsPreview.path("items")) {
                newsItems.add(item.deepCopy());
            }
        }

        ObjectNode seo = normalized.with("seo");
        JsonNode currentSeo = source.path("seo");
        seo.put("title", currentSeo.path("title").asText(seo.path("title").asText("")));
        seo.put("description", currentSeo.path("description").asText(seo.path("description").asText("")));
        seo.put("keywords", currentSeo.path("keywords").asText(seo.path("keywords").asText("")));

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
        String tenantName = currentTenantName();
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode hero = root.putObject("hero");
        ArrayNode slides = hero.putArray("slides");
        slides.add(createSlide(
            "https://images.unsplash.com/photo-1494412651409-8963ce7935a7?auto=format&fit=crop&w=1600&q=80",
            tenantName + " logistics overview",
            tenantName + " logistics solutions",
            "A reusable tenant homepage template for waybill, delivery, and cross-border operations.",
            "Track Shipment",
            "/",
            "Contact Team",
            "/contact"
        ));
        slides.add(createSlide(
            "https://images.unsplash.com/photo-1570710891163-6d3b5c47248b?auto=format&fit=crop&w=1600&q=80",
            tenantName + " service network",
            "Operational visibility from booking to delivery",
            "Replace this default copy with tenant-specific route, fulfillment, and service-positioning content.",
            "View Services",
            "/taiwan",
            "Latest News",
            "/news"
        ));

        ObjectNode trackingSection = root.putObject("trackingSection");
        trackingSection.put("title", "Shipment Tracking");
        trackingSection.put("inputPlaceholder", "Enter tracking number or reference number");
        trackingSection.put("searchButtonText", "Track Now");
        trackingSection.put("emptyText", "Enter a tracking number to see the latest shipment status.");
        trackingSection.put("notFoundText", "No shipment was found for the current number.");
        trackingSection.put("loadingText", "Loading shipment details...");

        ObjectNode businessSection = root.putObject("businessSection");
        businessSection.put("title", "Core Services");
        businessSection.put("subtitle", "A reusable service layout for freight, route delivery, and fulfillment scenarios.");
        ArrayNode businessItems = businessSection.putArray("items");
        businessItems.add(createBusinessItem("Regional Line-Haul", "Stable route-based delivery for recurring business flows.", "mdi-truck-fast-outline", "/taiwan"));
        businessItems.add(createBusinessItem("Cross-Border Delivery", "Template content for cross-border routing, customs, and last-mile execution.", "mdi-earth", "/international"));
        businessItems.add(createBusinessItem("Warehousing Support", "Use this block to describe storage, sorting, consolidation, or value-added services.", "mdi-warehouse", "/contact"));

        ObjectNode processSection = root.putObject("processSection");
        processSection.put("title", "Delivery Workflow");
        processSection.put("subtitle", "Default steps that can be customized for each tenant.");
        ArrayNode processSteps = processSection.putArray("steps");
        processSteps.add(createStep("Requirement Review", "Confirm shipment profile, destination, and service expectation."));
        processSteps.add(createStep("Route Planning", "Choose line-haul, customs, and last-mile handling strategy."));
        processSteps.add(createStep("Shipment Execution", "Create waybill, collect cargo, and start operational handoff."));
        processSteps.add(createStep("Tracking And Delivery", "Monitor events and close the shipment with delivery confirmation."));

        ObjectNode promiseSection = root.putObject("promiseSection");
        promiseSection.put("title", "Service Commitments");
        promiseSection.put("subtitle", "Default value propositions ready to be replaced by tenant-specific copy.");
        promiseSection.set("items", createDefaultPromiseItems());

        ObjectNode newsPreviewSection = root.putObject("newsPreviewSection");
        newsPreviewSection.put("title", "Latest Updates");
        newsPreviewSection.put("subtitle", "Use this section for operating announcements, line changes, or service news.");
        newsPreviewSection.put("viewAllText", "View All News");
        newsPreviewSection.put("viewAllUrl", "/news");
        newsPreviewSection.set("items", objectMapper.createArrayNode());

        ObjectNode seo = root.putObject("seo");
        seo.put("title", tenantName + " | Logistics And Waybill Platform");
        seo.put("description", "Default SEO description for the tenant homepage. Replace with tenant-specific brand positioning.");
        seo.put("keywords", tenantName + ", logistics, waybill, tracking, delivery");
        return root;
    }

    private ArrayNode createDefaultPromiseItems() {
        return objectMapper.createArrayNode()
            .add(createPromiseItem("Stable Delivery", "Use this slot for the tenant's lead-time commitment.", "mdi-timer-outline"))
            .add(createPromiseItem("Transparent Tracking", "Describe operational milestones and customer visibility.", "mdi-map-marker-path"))
            .add(createPromiseItem("Flexible Handling", "Explain special cargo, route, or packaging capability.", "mdi-package-variant-closed"))
            .add(createPromiseItem("Response Coverage", "Introduce support hours, escalation path, or account service.", "mdi-headset"))
            .add(createPromiseItem("Operational Quality", "Highlight process control, exception handling, or compliance.", "mdi-shield-check-outline"))
            .add(createPromiseItem("Scalable Collaboration", "Reserve this area for multi-warehouse or multi-country scale-up messaging.", "mdi-domain"));
    }

    private ObjectNode createPromiseItem(String title, String description, String icon) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("title", title);
        item.put("description", description);
        item.put("icon", icon);
        item.put("imageUrl", "");
        return item;
    }

    private ObjectNode createSlide(
        String image,
        String alt,
        String title,
        String subtitle,
        String primaryText,
        String primaryValue,
        String secondaryText,
        String secondaryValue
    ) {
        ObjectNode slide = objectMapper.createObjectNode();
        slide.put("image", image);
        slide.put("alt", alt);
        slide.put("title", title);
        slide.put("subtitle", subtitle);
        slide.set("primaryButton", createButtonNode(primaryText, primaryValue));
        slide.set("secondaryButton", createButtonNode(secondaryText, secondaryValue));
        return slide;
    }

    private ObjectNode createBusinessItem(String title, String description, String icon, String url) {
        ObjectNode item = objectMapper.createObjectNode();
        item.put("title", title);
        item.put("description", description);
        item.put("icon", icon);
        item.put("url", url);
        return item;
    }

    private ObjectNode createStep(String title, String description) {
        ObjectNode step = objectMapper.createObjectNode();
        step.put("title", title);
        step.put("description", description);
        return step;
    }

    private String currentTenantName() {
        TenantContext context = TenantContextHolder.get();
        if (context != null && context.tenantName() != null && !context.tenantName().isBlank()) {
            return context.tenantName().trim();
        }
        return "Tenant";
    }
}
