package com.qsd.admin.website.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.content.entity.SiteContentPage;
import com.qsd.admin.content.mapper.SiteContentPageMapper;
import com.qsd.admin.news.entity.NewsArticle;
import com.qsd.admin.news.mapper.NewsArticleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class PublicWebsiteService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SiteContentPageMapper siteContentPageMapper;
    private final NewsArticleMapper newsArticleMapper;
    private final ObjectMapper objectMapper;

    public PublicWebsiteService(
        SiteContentPageMapper siteContentPageMapper,
        NewsArticleMapper newsArticleMapper,
        ObjectMapper objectMapper
    ) {
        this.siteContentPageMapper = siteContentPageMapper;
        this.newsArticleMapper = newsArticleMapper;
        this.objectMapper = objectMapper;
    }

    public JsonNode getSiteConfig() {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode logo = objectMapper.createObjectNode();
        logo.put("image", "");
        logo.put("alt", "TEX Express");
        logo.put("href", "/");
        root.set("logo", logo);

        ArrayNode navItems = objectMapper.createArrayNode();
        navItems.add(createNamePath("首页", "/"));
        navItems.add(createNamePath("台湾专线", "/taiwan"));
        navItems.add(createNamePath("非洲专线", "/feizhou"));
        navItems.add(createNamePath("国际快递", "/international"));
        navItems.add(createNamePath("关于我们", "/about"));
        navItems.add(createNamePath("新闻资讯", "/news"));
        navItems.add(createNamePath("联系我们", "/contact"));
        root.set("navItems", navItems);

        ArrayNode languages = objectMapper.createArrayNode();
        languages.add(createLanguage("中文", "zh-CN"));
        languages.add(createLanguage("English", "en"));
        root.set("languages", languages);

        ObjectNode footer = objectMapper.createObjectNode();
        footer.put("brandTitle", "TEX Express");
        footer.put("brandDescription", "聚焦跨境专线、国际快递和仓配协同的一体化物流服务。");
        footer.put("contactTitle", "联系我们");
        footer.put("phone", "400-123-4567");
        footer.put("email", "info@texexpress.com");
        footer.put("linksTitle", "快捷链接");
        ArrayNode footerLinks = objectMapper.createArrayNode();
        footerLinks.add(createTitleHref("关于我们", "/about"));
        footerLinks.add(createTitleHref("新闻资讯", "/news"));
        footerLinks.add(createTitleHref("联系我们", "/contact"));
        footer.set("links", footerLinks);
        footer.put("socialTitle", "关注我们");
        footer.put("socialText", "微信公众号：TEX Express");
        footer.put("copyright", "© 2026 TEX Express. All rights reserved.");
        root.set("footer", footer);

        return root;
    }

    public JsonNode getHomePage() {
        ObjectNode root = readPublishedPage("home");
        root.remove("seo");
        return root;
    }

    public JsonNode getAboutPage() {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode hero = objectMapper.createObjectNode();
        hero.put("tag", "About TEX Express");
        hero.put("title", "让跨境物流从能发走向更稳、更清晰、更可复用");
        hero.put("description", "我们围绕专线运输、国际快递和仓配协同持续优化履约流程，帮助企业客户减少沟通损耗和交付波动。");
        hero.set("primaryButton", createTextLink("联系我们", "/contact"));
        hero.set("secondaryButton", createTextLink("查看线路", "/taiwan"));
        ArrayNode stats = objectMapper.createArrayNode();
        stats.add(createValueLabel("10+", "重点服务国家和地区"));
        stats.add(createValueLabel("30min", "紧急问题响应"));
        stats.add(createValueLabel("7x12", "工作时段支持"));
        hero.set("stats", stats);
        ObjectNode images = objectMapper.createObjectNode();
        images.put("main", "https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=1400&q=80");
        images.put("sideTop", "https://images.unsplash.com/photo-1556740749-887f6717d7e4?auto=format&fit=crop&w=900&q=80");
        images.put("sideBottom", "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=900&q=80");
        hero.set("images", images);
        ObjectNode floatingNote = objectMapper.createObjectNode();
        floatingNote.put("title", "更关注长期协作");
        floatingNote.put("text", "从首次询价到稳定月发运，我们更重视方案复用和关键节点可追踪。");
        hero.set("floatingNote", floatingNote);
        root.set("hero", hero);

        ObjectNode intro = objectMapper.createObjectNode();
        ObjectNode left = objectMapper.createObjectNode();
        left.put("tag", "Who We Are");
        left.put("title", "不是单次发货撮合，而是长期交付协作");
        left.put("description", "我们把线路判断、资料准备、清关协同、末端交付和异常反馈串成一套更稳定的交付流程。");
        intro.set("left", left);
        ObjectNode right = objectMapper.createObjectNode();
        right.put("tag", "What We Focus On");
        ArrayNode focusItems = objectMapper.createArrayNode();
        focusItems.add("先判断货型、时效和目的地要求，再给出线路建议");
        focusItems.add("把异常同步做得比发出去了更重要");
        focusItems.add("让高频线路沉淀成可复用的协作模板");
        right.set("items", focusItems);
        intro.set("right", right);
        root.set("intro", intro);

        ArrayNode milestones = objectMapper.createArrayNode();
        milestones.add(createYearMilestone("2019", "团队搭建", "从跨境专线执行起步，积累高频线路履约经验。"));
        milestones.add(createYearMilestone("2022", "重点线路扩展", "服务范围逐步扩展到台湾、非洲和国际快递方向。"));
        milestones.add(createYearMilestone("2025", "交付模型重构", "把后台管理、线路模板和前台公开数据结构统一起来。"));
        root.set("milestones", milestones);

        ArrayNode principles = objectMapper.createArrayNode();
        principles.add(createTitleDescriptionIcon("方案先判断", "先看货型、时效和目的地要求，再决定渠道。", "mdi-compass-outline"));
        principles.add(createTitleDescriptionIcon("节点要透明", "关键节点可追踪、可同步、可复盘。", "mdi-map-marker-path"));
        principles.add(createTitleDescriptionIcon("异常要前置", "异常不是结果，而是要尽早识别和处理。", "mdi-alert-outline"));
        root.set("principles", principles);

        ArrayNode promises = objectMapper.createArrayNode();
        promises.add("报价透明，不隐藏关键条件");
        promises.add("高频线路持续复盘");
        promises.add("异常件主动同步");
        promises.add("企业客户支持长期协同");
        root.set("promises", promises);

        ObjectNode cta = objectMapper.createObjectNode();
        cta.put("tag", "Ready to Talk");
        cta.put("title", "如果你也在找更稳的跨境物流协作方式");
        cta.put("description", "把当前发运场景告诉我们，我们会先判断链路，再讨论价格和执行节奏。");
        cta.set("primaryButton", createTextLink("联系顾问", "/contact"));
        cta.set("secondaryButton", createTextLink("查看新闻", "/news"));
        root.set("cta", cta);

        return root;
    }

    public JsonNode getContactPage() {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode hero = objectMapper.createObjectNode();
        hero.put("badge", "7 x 12 快速响应");
        hero.put("title", "联系我们，获取更稳的跨境物流方案");
        hero.put("description", "支持专线运输、国际快递、仓配协同与定制报价。把目的地、货量和时效发给我们，顾问会尽快响应。");
        ArrayNode heroTags = objectMapper.createArrayNode();
        heroTags.add("专线运输");
        heroTags.add("国际快递");
        heroTags.add("仓配协同");
        heroTags.add("企业报价");
        hero.set("tags", heroTags);
        hero.set("primaryButton", createHrefButton("立即咨询顾问", "/contact/form"));
        hero.set("secondaryButton", createRouteButton("查看服务线路", "/taiwan"));
        ArrayNode quickPanel = objectMapper.createArrayNode();
        quickPanel.add(createLabelValue("业务热线", "400-123-4567"));
        quickPanel.add(createLabelValue("商务邮箱", "bd@texexpress.com"));
        quickPanel.add(createLabelValue("服务时间", "周一至周日 09:00 - 21:00"));
        hero.set("quickPanel", quickPanel);
        hero.put("note", "如果你已经有 SKU、装箱清单或目的国要求，建议直接发给顾问，能明显减少来回确认时间。");
        root.set("hero", hero);

        ArrayNode contactCards = objectMapper.createArrayNode();
        contactCards.add(createContactCard("业务热线", "400-123-4567", "工作日 09:00 - 21:00", "提供专线咨询、报价和异常跟进。", "mdi-phone-outline", "拨打电话", "tel:4001234567"));
        contactCards.add(createContactCard("商务邮箱", "bd@texexpress.com", "合作与投标", "适合发送合作需求、招投标资料和批量询价清单。", "mdi-email-outline", "发送邮件", "mailto:bd@texexpress.com"));
        contactCards.add(createContactCard("客户顾问微信", "TEX-Express", "工作时段在线", "适合补充品类图片、清关要求和阶段性问题沟通。", "mdi-wechat", "复制微信号", "weixin://dl/chat"));
        contactCards.add(createContactCard("深圳运营中心", "深圳市龙岗区坂田街道跨境物流产业园 A2 栋 5 层", "支持预约到访", "建议提前 1 个工作日提交到访信息。", "mdi-map-marker-outline", "查看地图", "/contact#map"));
        root.set("contactCards", contactCards);

        ArrayNode serviceWindows = objectMapper.createArrayNode();
        serviceWindows.add(createLabelValue("工作日", "周一至周五 09:00 - 21:00"));
        serviceWindows.add(createLabelValue("周末", "周六至周日 10:00 - 18:00"));
        serviceWindows.add(createLabelValue("响应承诺", "紧急问题 30 分钟内响应，常规询价 2 小时内给出初步方案。"));
        root.set("serviceWindows", serviceWindows);

        ArrayNode visitChecklist = objectMapper.createArrayNode();
        visitChecklist.add("准备货物类型和目的地信息");
        visitChecklist.add("提供预计重量、体积和件数");
        visitChecklist.add("补充对时效、清关和派送的要求");
        root.set("visitChecklist", visitChecklist);

        ObjectNode mapInfo = objectMapper.createObjectNode();
        mapInfo.put("title", "地图与到访提示");
        mapInfo.put("description", "地图区域建议展示深圳运营中心定位点，并补充停车和来访登记说明。");
        ArrayNode markers = objectMapper.createArrayNode();
        ObjectNode marker = objectMapper.createObjectNode();
        marker.put("title", "深圳运营中心");
        marker.put("type", "office");
        markers.add(marker);
        mapInfo.set("markers", markers);
        root.set("mapInfo", mapInfo);

        ArrayNode promises = objectMapper.createArrayNode();
        promises.add("报价透明，无隐藏附加费");
        promises.add("方案先评估时效和风险，再给报价");
        promises.add("异常件全程跟进并同步节点");
        promises.add("支持企业客户月结与定制对账");
        root.set("promises", promises);

        ObjectNode cta = objectMapper.createObjectNode();
        cta.put("tag", "先发需求，我们再给方案");
        cta.put("title", "把需求发过来，我们先判断线路再报价");
        cta.put("description", "如果你已经有装箱清单、SKU 或目的国要求，建议直接发给顾问，减少沟通损耗。");
        cta.set("primaryButton", createHrefButton("立即咨询顾问", "/contact/form"));
        cta.set("secondaryButton", createRouteButton("查看服务线路", "/taiwan"));
        root.set("cta", cta);

        return root;
    }

    public JsonNode getServiceLinePage(String key) {
        return readPublishedPage(resolveServiceLinePageCode(key));
    }

    public JsonNode getNewsListPage(String year, Integer page, Integer pageSize) {
        List<NewsArticle> allArticles = newsArticleMapper.selectPublishedList();
        List<NewsArticle> filteredArticles = allArticles.stream()
            .filter(article -> year == null || year.isBlank() || formatDate(article.getPublishedAt()).startsWith(year))
            .toList();

        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int fromIndex = Math.min((safePage - 1) * safePageSize, filteredArticles.size());
        int toIndex = Math.min(fromIndex + safePageSize, filteredArticles.size());
        List<NewsArticle> pageItems = filteredArticles.subList(fromIndex, toIndex);

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode hero = objectMapper.createObjectNode();
        hero.put("tag", "News Center");
        hero.put("title", "新闻资讯");
        hero.put("description", "集中展示公司动态、线路更新和交付能力进展。");
        root.set("hero", hero);

        ArrayNode years = objectMapper.createArrayNode();
        allArticles.stream()
            .map(NewsArticle::getPublishedAt)
            .filter(Objects::nonNull)
            .map(date -> String.valueOf(date.getYear()))
            .distinct()
            .sorted(Comparator.reverseOrder())
            .forEach(years::add);
        ObjectNode filters = objectMapper.createObjectNode();
        filters.set("years", years);
        root.set("filters", filters);

        ArrayNode list = objectMapper.createArrayNode();
        for (NewsArticle article : pageItems) {
            ObjectNode item = objectMapper.createObjectNode();
            item.put("id", String.valueOf(article.getId()));
            item.put("title", defaultText(article.getTitle()));
            item.put("summary", defaultText(article.getSummary()));
            item.put("coverImage", defaultText(article.getCoverImageUrl()));
            item.put("date", formatDate(article.getPublishedAt()));
            item.put("publishAt", formatDateTime(article.getPublishedAt()));
            item.put("category", "公司动态");
            item.put("author", defaultText(article.getAuthor()));
            item.put("url", "/news/" + article.getId());
            list.add(item);
        }
        root.set("list", list);

        ObjectNode pagination = objectMapper.createObjectNode();
        pagination.put("page", safePage);
        pagination.put("pageSize", safePageSize);
        pagination.put("total", filteredArticles.size());
        pagination.put("hasMore", toIndex < filteredArticles.size());
        root.set("pagination", pagination);

        return root;
    }

    public JsonNode getNewsDetailPage(Long id) {
        NewsArticle article = newsArticleMapper.selectPublishedById(id);
        if (article == null) {
            throw new NotFoundException("published news article not found");
        }

        ObjectNode root = objectMapper.createObjectNode();
        root.put("id", String.valueOf(article.getId()));
        root.put("category", "公司动态");
        root.put("title", defaultText(article.getTitle()));
        root.put("publishDate", formatDateTime(article.getPublishedAt()));
        root.put("coverImage", defaultText(article.getCoverImageUrl()));
        root.put("summary", defaultText(article.getSummary()));
        root.put("author", defaultText(article.getAuthor()));
        root.put("contentHtml", toContentHtml(article.getContent()));
        ArrayNode tags = objectMapper.createArrayNode();
        tags.add("公司动态");
        tags.add("服务升级");
        tags.add("物流资讯");
        root.set("tags", tags);
        root.put("backUrl", "/news");
        return root;
    }

    public JsonNode getTrackingResult(String trackingNo) {
        String normalizedNo = trackingNo == null ? "" : trackingNo.trim().toUpperCase(Locale.ROOT);
        if (normalizedNo.isBlank()) {
            throw new NotFoundException("tracking number is required");
        }

        boolean exception = normalizedNo.contains("ERR") || normalizedNo.endsWith("9");
        ObjectNode root = objectMapper.createObjectNode();
        root.put("trackingId", normalizedNo);
        root.put("status", exception ? "运输异常" : "运输中");
        root.put("origin", "深圳");
        root.put("destination", resolveTrackingDestination(normalizedNo));
        root.put("isException", exception);

        ArrayNode steps = objectMapper.createArrayNode();
        steps.add(createTrackingStep("2026-04-13 09:15:00", "订单创建", "已接收运单信息，等待分配处理。", "深圳"));
        steps.add(createTrackingStep("2026-04-13 12:40:00", "货物入仓", "货物已入仓并完成基础核验。", "深圳仓"));
        steps.add(createTrackingStep(
            "2026-04-13 18:25:00",
            exception ? "运输异常" : "国际运输中",
            exception ? "目的港资料待补充，请联系顾问确认。" : "货物已发出，正在运往下一节点。",
            exception ? "转运中心" : "国际干线"
        ));
        steps.add(createTrackingStep(
            "2026-04-14 10:10:00",
            exception ? "待处理" : "预计派送",
            exception ? "等待资料补充后继续推进。" : "清关完成后将安排末端派送。",
            "目的港"
        ));
        root.set("steps", steps);

        return root;
    }

    private ObjectNode readPublishedPage(String pageCode) {
        SiteContentPage page = siteContentPageMapper.selectPublishedByPageCode(pageCode);
        if (page == null) {
            throw new NotFoundException("published page not found");
        }

        try {
            JsonNode node = objectMapper.readTree(page.getFormJson());
            if (!node.isObject()) {
                throw new NotFoundException("page data is invalid");
            }
            return (ObjectNode) node;
        } catch (JsonProcessingException ex) {
            throw new NotFoundException("page data is invalid");
        }
    }

    private String resolveServiceLinePageCode(String key) {
        return switch (key) {
            case "taiwan", "feizhou", "international" -> "service-line:" + key;
            default -> throw new NotFoundException("service line page not found");
        };
    }

    private String resolveTrackingDestination(String trackingNo) {
        if (trackingNo.startsWith("TW")) {
            return "台北";
        }
        if (trackingNo.startsWith("AF")) {
            return "拉各斯";
        }
        if (trackingNo.startsWith("INT")) {
            return "国际目的港";
        }
        return "海外目的地";
    }

    private String toContentHtml(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return "";
        }

        try {
            JsonNode root = objectMapper.readTree(rawContent);
            JsonNode blocks = root.path("blocks");
            if (!blocks.isArray()) {
                return wrapParagraph(escapeHtml(rawContent));
            }

            StringBuilder html = new StringBuilder();
            for (JsonNode block : blocks) {
                String type = block.path("type").asText("");
                String content = block.path("content").asText("");
                String imageUrl = block.path("url").asText(content);
                if (content.isBlank() && imageUrl.isBlank()) {
                    continue;
                }

                switch (type) {
                    case "heading" -> html.append("<h2>").append(escapeHtml(content)).append("</h2>");
                    case "image" -> html.append("<p><img src=\"").append(escapeHtml(imageUrl)).append("\" alt=\"news image\" /></p>");
                    case "image_caption" -> html.append("<p><em>").append(escapeHtml(content)).append("</em></p>");
                    default -> html.append(wrapParagraph(escapeHtml(content)));
                }
            }
            return html.toString();
        } catch (JsonProcessingException ex) {
            return wrapParagraph(escapeHtml(rawContent));
        }
    }

    private String wrapParagraph(String content) {
        return "<p>" + content.replace("\n", "<br/>") + "</p>";
    }

    private String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "" : DATE_FORMATTER.format(value);
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMATTER.format(value);
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private ObjectNode createNamePath(String name, String path) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("name", name);
        node.put("path", path);
        return node;
    }

    private ObjectNode createLanguage(String title, String lang) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", title);
        node.put("lang", lang);
        return node;
    }

    private ObjectNode createTitleHref(String title, String href) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", title);
        node.put("href", href);
        return node;
    }

    private ObjectNode createValueLabel(String value, String label) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("value", value);
        node.put("label", label);
        return node;
    }

    private ObjectNode createTitleDescriptionIcon(String title, String description, String icon) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", title);
        node.put("description", description);
        node.put("icon", icon);
        return node;
    }

    private ObjectNode createYearMilestone(String year, String title, String description) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("year", year);
        node.put("title", title);
        node.put("description", description);
        return node;
    }

    private ObjectNode createLabelValue(String label, String value) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("label", label);
        node.put("value", value);
        return node;
    }

    private ObjectNode createTextLink(String text, String route) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("text", text);
        node.put("route", route);
        return node;
    }

    private ObjectNode createHrefButton(String text, String href) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("text", text);
        node.put("href", href);
        return node;
    }

    private ObjectNode createRouteButton(String text, String route) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("text", text);
        node.put("route", route);
        return node;
    }

    private ObjectNode createContactCard(
        String title,
        String value,
        String meta,
        String description,
        String icon,
        String actionLabel,
        String href
    ) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("title", title);
        node.put("value", value);
        node.put("meta", meta);
        node.put("description", description);
        node.put("icon", icon);
        node.put("actionLabel", actionLabel);
        node.put("href", href);
        return node;
    }

    private ObjectNode createTrackingStep(String time, String status, String description, String location) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("time", time);
        node.put("status", status);
        node.put("description", description);
        node.put("location", location);
        node.put("isException", "运输异常".equals(status));
        return node;
    }
}
