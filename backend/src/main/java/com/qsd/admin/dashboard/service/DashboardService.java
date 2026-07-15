package com.qsd.admin.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qsd.admin.content.entity.SiteContentPage;
import com.qsd.admin.content.mapper.SiteContentPageMapper;
import com.qsd.admin.dashboard.dto.DashboardSummaryResponse;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.news.mapper.NewsArticleMapper;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.RefundOrder;
import com.qsd.admin.payment.mapper.PayMerchantConfigMapper;
import com.qsd.admin.payment.mapper.PayOrderMapper;
import com.qsd.admin.payment.mapper.RefundOrderMapper;
import com.qsd.admin.tenant.TenantContextHolder;
import com.qsd.admin.tenant.entity.Tenant;
import com.qsd.admin.tenant.mapper.TenantDomainMapper;
import com.qsd.admin.tenant.service.TenantService;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class DashboardService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TenantService tenantService;
    private final TenantDomainMapper tenantDomainMapper;
    private final PayMerchantConfigMapper payMerchantConfigMapper;
    private final MemberUserMapper memberUserMapper;
    private final WaybillOrderMapper waybillOrderMapper;
    private final PayOrderMapper payOrderMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final NewsArticleMapper newsArticleMapper;
    private final SiteContentPageMapper siteContentPageMapper;

    public DashboardService(
        TenantService tenantService,
        TenantDomainMapper tenantDomainMapper,
        PayMerchantConfigMapper payMerchantConfigMapper,
        MemberUserMapper memberUserMapper,
        WaybillOrderMapper waybillOrderMapper,
        PayOrderMapper payOrderMapper,
        RefundOrderMapper refundOrderMapper,
        NewsArticleMapper newsArticleMapper,
        SiteContentPageMapper siteContentPageMapper
    ) {
        this.tenantService = tenantService;
        this.tenantDomainMapper = tenantDomainMapper;
        this.payMerchantConfigMapper = payMerchantConfigMapper;
        this.memberUserMapper = memberUserMapper;
        this.waybillOrderMapper = waybillOrderMapper;
        this.payOrderMapper = payOrderMapper;
        this.refundOrderMapper = refundOrderMapper;
        this.newsArticleMapper = newsArticleMapper;
        this.siteContentPageMapper = siteContentPageMapper;
    }

    public DashboardSummaryResponse getSummary() {
        Tenant tenant = tenantService.requireCurrentTenant();
        Long tenantId = TenantContextHolder.requireTenantId();
        long enabledDomainCount = tenantDomainMapper.countEnabledByTenantId(tenantId);
        long enabledMerchantCount = payMerchantConfigMapper.selectCount(new QueryWrapper<PayMerchantConfig>()
            .eq("tenant_id", tenantId)
            .eq("deleted", 0)
            .eq("enabled", 1));
        long memberTotal = memberUserMapper.selectCount(new QueryWrapper<MemberUser>()
            .eq("tenant_id", tenantId)
            .eq("deleted", 0));
        long memberEnabled = memberUserMapper.selectCount(new QueryWrapper<MemberUser>()
            .eq("tenant_id", tenantId)
            .eq("deleted", 0)
            .eq("status", "ENABLED"));
        long memberWechatBound = memberUserMapper.selectCount(new QueryWrapper<MemberUser>()
            .eq("tenant_id", tenantId)
            .eq("deleted", 0)
            .isNotNull("wechat_openid")
            .ne("wechat_openid", ""));
        long waybillTotal = waybillOrderMapper.countActive(tenantId);
        long waybillInTransit = waybillOrderMapper.countByCurrentStatus(tenantId, "in_transit");
        long payOrderTotal = payOrderMapper.selectCount(new QueryWrapper<PayOrder>()
            .eq("tenant_id", tenantId)
            .eq("deleted", 0));
        long payOrderPaying = payOrderMapper.selectCount(new QueryWrapper<PayOrder>()
            .eq("tenant_id", tenantId)
            .eq("deleted", 0)
            .in("status", "pending", "paying"));
        long payOrderPaid = payOrderMapper.selectCount(new QueryWrapper<PayOrder>()
            .eq("tenant_id", tenantId)
            .eq("deleted", 0)
            .eq("status", "paid"));
        long refundOrderTotal = refundOrderMapper.selectCount(new QueryWrapper<RefundOrder>()
            .eq("tenant_id", tenantId));
        long refundProcessing = refundOrderMapper.selectCount(new QueryWrapper<RefundOrder>()
            .eq("tenant_id", tenantId)
            .eq("status", "processing"));
        long newsTotal = newsArticleMapper.countActive(tenantId);
        long newsPublished = newsArticleMapper.countPublished(tenantId, null, null);
        int serviceLineTotal = siteContentPageMapper.countServiceLinePages(tenantId);
        SiteContentPage homeContent = siteContentPageMapper.selectByPageCode(tenantId, "home");

        return new DashboardSummaryResponse(
            tenant.getId(),
            tenant.getTenantCode(),
            tenant.getTenantName(),
            tenant.getStatus(),
            tenant.getTimezone(),
            tenant.getLocale(),
            enabledDomainCount,
            enabledMerchantCount,
            memberTotal,
            memberEnabled,
            memberWechatBound,
            waybillTotal,
            waybillInTransit,
            payOrderTotal,
            payOrderPaying,
            payOrderPaid,
            refundOrderTotal,
            refundProcessing,
            newsTotal,
            newsPublished,
            serviceLineTotal,
            homeContent == null ? null : homeContent.getStatus(),
            homeContent == null || homeContent.getUpdatedAt() == null ? null : DATE_TIME_FORMATTER.format(homeContent.getUpdatedAt())
        );
    }
}
