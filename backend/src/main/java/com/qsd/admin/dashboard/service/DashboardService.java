package com.qsd.admin.dashboard.service;

import com.qsd.admin.content.entity.SiteContentPage;
import com.qsd.admin.content.mapper.SiteContentPageMapper;
import com.qsd.admin.dashboard.dto.DashboardSummaryResponse;
import com.qsd.admin.news.mapper.NewsArticleMapper;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class DashboardService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WaybillOrderMapper waybillOrderMapper;
    private final NewsArticleMapper newsArticleMapper;
    private final SiteContentPageMapper siteContentPageMapper;

    public DashboardService(
        WaybillOrderMapper waybillOrderMapper,
        NewsArticleMapper newsArticleMapper,
        SiteContentPageMapper siteContentPageMapper
    ) {
        this.waybillOrderMapper = waybillOrderMapper;
        this.newsArticleMapper = newsArticleMapper;
        this.siteContentPageMapper = siteContentPageMapper;
    }

    public DashboardSummaryResponse getSummary() {
        long waybillTotal = waybillOrderMapper.countActive();
        long waybillInTransit = waybillOrderMapper.countByCurrentStatus("in_transit");
        long newsTotal = newsArticleMapper.countActive();
        long newsPublished = newsArticleMapper.countPublished(null, null);
        int serviceLineTotal = siteContentPageMapper.countServiceLinePages();
        SiteContentPage homeContent = siteContentPageMapper.selectByPageCode("home");

        return new DashboardSummaryResponse(
            waybillTotal,
            waybillInTransit,
            newsTotal,
            newsPublished,
            serviceLineTotal,
            homeContent == null ? null : homeContent.getStatus(),
            homeContent == null || homeContent.getUpdatedAt() == null ? null : DATE_TIME_FORMATTER.format(homeContent.getUpdatedAt())
        );
    }
}
