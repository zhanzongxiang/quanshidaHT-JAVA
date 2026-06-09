package com.qsd.admin.tenant.service;

import com.qsd.admin.auth.mapper.AdminUserMapper;
import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.content.service.HomeContentService;
import com.qsd.admin.content.service.ServiceLineContentService;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.TenantContextHolder;
import com.qsd.admin.tenant.dto.TenantBootstrapAdminResponse;
import com.qsd.admin.tenant.dto.TenantDomainResponse;
import com.qsd.admin.tenant.dto.TenantResponse;
import com.qsd.admin.tenant.dto.TenantSaveRequest;
import com.qsd.admin.tenant.dto.TenantDomainSaveRequest;
import com.qsd.admin.tenant.entity.Tenant;
import com.qsd.admin.tenant.entity.TenantDomain;
import com.qsd.admin.tenant.mapper.TenantDomainMapper;
import com.qsd.admin.tenant.mapper.TenantMapper;
import com.qsd.admin.auth.entity.AdminUser;
import com.qsd.admin.payment.service.PaymentMerchantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TenantService {
    public static final String DEFAULT_TENANT_CODE = "default";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DISABLED = "DISABLED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_ACTIVE, STATUS_DISABLED);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TenantMapper tenantMapper;
    private final TenantDomainMapper tenantDomainMapper;
    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final HomeContentService homeContentService;
    private final ServiceLineContentService serviceLineContentService;
    private final PaymentMerchantService paymentMerchantService;

    public TenantService(
        TenantMapper tenantMapper,
        TenantDomainMapper tenantDomainMapper,
        AdminUserMapper adminUserMapper,
        PasswordEncoder passwordEncoder,
        HomeContentService homeContentService,
        ServiceLineContentService serviceLineContentService,
        PaymentMerchantService paymentMerchantService
    ) {
        this.tenantMapper = tenantMapper;
        this.tenantDomainMapper = tenantDomainMapper;
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.homeContentService = homeContentService;
        this.serviceLineContentService = serviceLineContentService;
        this.paymentMerchantService = paymentMerchantService;
    }

    public List<TenantResponse> listTenants() {
        return tenantMapper.selectActiveList().stream()
            .map(this::toResponse)
            .toList();
    }

    public TenantResponse getCurrentTenant() {
        return toResponse(requireCurrentTenant());
    }

    public Tenant requireCurrentTenant() {
        TenantContext context = TenantContextHolder.get();
        if (context == null) {
            return requireDefaultTenant();
        }
        if (context.tenantId() != null) {
            return requireTenantById(context.tenantId());
        }
        if (context.tenantCode() != null && !context.tenantCode().isBlank()) {
            return requireTenantByCode(context.tenantCode());
        }
        return requireDefaultTenant();
    }

    public Tenant requireDefaultTenant() {
        Tenant tenant = tenantMapper.selectDefaultTenant();
        if (tenant == null) {
            throw new IllegalStateException("default tenant is missing");
        }
        ensureActive(tenant);
        return tenant;
    }

    public Tenant requireTenantById(Long tenantId) {
        Tenant tenant = tenantMapper.selectActiveById(tenantId);
        if (tenant == null) {
            throw new NotFoundException("tenant not found");
        }
        ensureActive(tenant);
        return tenant;
    }

    public Tenant requireTenantByCode(String tenantCode) {
        Tenant tenant = tenantMapper.selectByTenantCode(tenantCode);
        if (tenant == null) {
            throw new NotFoundException("tenant not found");
        }
        ensureActive(tenant);
        return tenant;
    }

    public Tenant findActiveTenantById(Long tenantId) {
        Tenant tenant = tenantMapper.selectActiveById(tenantId);
        return isActive(tenant) ? tenant : null;
    }

    public Tenant findActiveTenantByCode(String tenantCode) {
        Tenant tenant = tenantMapper.selectByTenantCode(trimToNull(tenantCode));
        return isActive(tenant) ? tenant : null;
    }

    public Tenant findActiveTenantByDomain(String domain) {
        String normalized = trimToNull(domain);
        if (normalized == null) {
            return null;
        }

        TenantDomain tenantDomain = tenantDomainMapper.selectEnabledByDomain(normalized);
        if (tenantDomain == null) {
            return null;
        }
        return findActiveTenantById(tenantDomain.getTenantId());
    }

    @Transactional
    public TenantResponse createTenant(TenantSaveRequest request) {
        String tenantCode = normalizeTenantCode(request.tenantCode());
        if (tenantMapper.selectByTenantCode(tenantCode) != null) {
            throw new BusinessException("tenant code already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = new Tenant();
        tenant.setTenantCode(tenantCode);
        tenant.setTenantName(request.tenantName().trim());
        tenant.setStatus(normalizeStatus(request.status()));
        tenant.setTimezone(request.timezone().trim());
        tenant.setLocale(request.locale().trim());
        tenant.setRemark(trimToEmpty(request.remark()));
        tenant.setDeleted(0);
        tenant.setCreatedAt(now);
        tenant.setUpdatedAt(now);
        tenantMapper.insert(tenant);
        replaceTenantDomains(tenant.getId(), request.domains());
        bootstrapTenantData(tenant);
        return toResponse(tenant);
    }

    @Transactional
    public TenantResponse updateTenant(Long id, TenantSaveRequest request) {
        Tenant tenant = requireTenantById(id);
        String tenantCode = normalizeTenantCode(request.tenantCode());
        Tenant existing = tenantMapper.selectByTenantCode(tenantCode);
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("tenant code already exists");
        }

        ensureStatusTransitionAllowed(tenant, request);
        tenant.setTenantCode(tenantCode);
        tenant.setTenantName(request.tenantName().trim());
        tenant.setStatus(normalizeStatus(request.status()));
        tenant.setTimezone(request.timezone().trim());
        tenant.setLocale(request.locale().trim());
        tenant.setRemark(trimToEmpty(request.remark()));
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantMapper.updateById(tenant);
        replaceTenantDomains(id, request.domains());
        return toResponse(requireTenantById(id));
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getTenantCode(),
            tenant.getTenantName(),
            tenant.getStatus(),
            tenant.getTimezone(),
            tenant.getLocale(),
            tenant.getRemark(),
            tenantDomainMapper.selectByTenantId(tenant.getId()).stream().map(this::toDomainResponse).toList(),
            buildBootstrapAdminResponse(tenant),
            formatDateTime(tenant.getCreatedAt()),
            formatDateTime(tenant.getUpdatedAt())
        );
    }

    private TenantBootstrapAdminResponse buildBootstrapAdminResponse(Tenant tenant) {
        String username = buildBootstrapAdminUsername(tenant.getTenantCode());
        AdminUser adminUser = adminUserMapper.selectActiveByUsername(username);
        if (adminUser == null || !tenant.getId().equals(adminUser.getTenantId())) {
            return null;
        }
        return new TenantBootstrapAdminResponse(username, buildBootstrapAdminInitialPassword(tenant.getTenantCode()));
    }

    private TenantDomainResponse toDomainResponse(TenantDomain domain) {
        return new TenantDomainResponse(
            domain.getId(),
            domain.getDomain(),
            domain.getDomainType(),
            domain.getEnabled() != null && domain.getEnabled() == 1,
            formatDateTime(domain.getCreatedAt())
        );
    }

    private void replaceTenantDomains(Long tenantId, List<TenantDomainSaveRequest> requests) {
        tenantDomainMapper.deleteByTenantId(tenantId);
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<String> seenDomains = new ArrayList<>();
        for (TenantDomainSaveRequest request : requests) {
            String domain = normalizeDomain(request.domain());
            if (seenDomains.contains(domain)) {
                throw new BusinessException("duplicate tenant domain in request");
            }
            seenDomains.add(domain);

            TenantDomain existing = tenantDomainMapper.selectByDomain(domain);
            if (existing != null && !existing.getTenantId().equals(tenantId)) {
                throw new BusinessException("tenant domain already exists");
            }

            TenantDomain domainEntity = new TenantDomain();
            domainEntity.setTenantId(tenantId);
            domainEntity.setDomain(domain);
            domainEntity.setDomainType(normalizeDomainType(request.domainType()));
            domainEntity.setEnabled(request.enabled() == null || request.enabled() ? 1 : 0);
            domainEntity.setCreatedAt(LocalDateTime.now());
            tenantDomainMapper.insert(domainEntity);
        }
    }

    private void ensureStatusTransitionAllowed(Tenant tenant, TenantSaveRequest request) {
        String nextStatus = normalizeStatus(request.status());
        if (!STATUS_DISABLED.equals(nextStatus)) {
            return;
        }
        if (STATUS_DISABLED.equals(tenant.getStatus())) {
            return;
        }
        if (DEFAULT_TENANT_CODE.equals(tenant.getTenantCode())) {
            throw new BusinessException("default tenant cannot be disabled");
        }
        if (adminUserMapper.countEnabledByTenantId(tenant.getId()) > 0) {
            throw new BusinessException("tenant still has enabled admin users");
        }
        if (tenantDomainMapper.countEnabledByTenantId(tenant.getId()) > 0) {
            throw new BusinessException("tenant still has enabled domains");
        }
    }

    private void bootstrapTenantData(Tenant tenant) {
        TenantContext previous = TenantContextHolder.get();
        try {
            TenantContextHolder.set(new TenantContext(tenant.getId(), tenant.getTenantCode(), tenant.getTenantName()));
            homeContentService.getHomeContent();
            serviceLineContentService.listServiceLines();
            paymentMerchantService.listMerchantConfigs();
            ensureBootstrapAdminExists(tenant);
        } finally {
            if (previous == null) {
                TenantContextHolder.clear();
            } else {
                TenantContextHolder.set(previous);
            }
        }
    }

    private void ensureBootstrapAdminExists(Tenant tenant) {
        String username = buildBootstrapAdminUsername(tenant.getTenantCode());
        AdminUser existing = adminUserMapper.selectActiveByUsername(username);
        if (existing != null) {
            if (!tenant.getId().equals(existing.getTenantId())) {
                throw new BusinessException("bootstrap admin username already exists");
            }
            ensureSuperAdminRole(existing.getId());
            return;
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setTenantId(tenant.getId());
        adminUser.setUsername(username);
        adminUser.setPasswordHash(passwordEncoder.encode(buildBootstrapAdminInitialPassword(tenant.getTenantCode())));
        adminUser.setStatus("ENABLED");
        adminUserMapper.insert(adminUser);
        ensureSuperAdminRole(adminUser.getId());
    }

    private void ensureSuperAdminRole(Long userId) {
        Long roleId = adminUserMapper.selectRoleIdByRoleCode("super_admin");
        if (roleId == null) {
            throw new IllegalStateException("super_admin role is missing");
        }
        if (adminUserMapper.countUserRole(userId, roleId) == 0) {
            adminUserMapper.insertUserRole(userId, roleId);
        }
    }

    private String buildBootstrapAdminUsername(String tenantCode) {
        return "admin_" + tenantCode.toLowerCase();
    }

    private String buildBootstrapAdminInitialPassword(String tenantCode) {
        return "Init@" + tenantCode.toUpperCase() + "123";
    }

    private void ensureActive(Tenant tenant) {
        if (!isActive(tenant)) {
            throw new BusinessException("tenant is disabled");
        }
    }

    private boolean isActive(Tenant tenant) {
        return tenant != null && STATUS_ACTIVE.equals(tenant.getStatus());
    }

    private String normalizeTenantCode(String tenantCode) {
        String normalized = trimToNull(tenantCode);
        if (normalized == null) {
            throw new BusinessException("tenant code must not be blank");
        }
        return normalized.toLowerCase();
    }

    private String normalizeDomain(String domain) {
        String normalized = trimToNull(domain);
        if (normalized == null) {
            throw new BusinessException("tenant domain must not be blank");
        }
        return normalized.toLowerCase();
    }

    private String normalizeDomainType(String domainType) {
        String normalized = trimToNull(domainType);
        if (normalized == null) {
            throw new BusinessException("tenant domain type must not be blank");
        }
        return normalized.toLowerCase();
    }

    private String normalizeStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized == null) {
            throw new BusinessException("tenant status must not be blank");
        }
        normalized = normalized.toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new BusinessException("invalid tenant status");
        }
        return normalized;
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
}
