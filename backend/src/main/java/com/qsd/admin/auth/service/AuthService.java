package com.qsd.admin.auth.service;

import com.qsd.admin.auth.dto.MeResponse;
import com.qsd.admin.auth.entity.AdminMenu;
import com.qsd.admin.auth.entity.AdminUser;
import com.qsd.admin.auth.mapper.AdminMenuMapper;
import com.qsd.admin.auth.mapper.AdminUserMapper;
import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.ErrorCode;
import com.qsd.admin.common.service.RateLimiterService;
import com.qsd.admin.security.AuthenticatedUser;
import com.qsd.admin.security.JwtTokenService;
import com.qsd.admin.tenant.entity.Tenant;
import com.qsd.admin.tenant.service.TenantService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {
    private final AdminUserMapper adminUserMapper;
    private final AdminMenuMapper adminMenuMapper;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiterService;
    private final TenantService tenantService;

    public AuthService(
        AdminUserMapper adminUserMapper,
        AdminMenuMapper adminMenuMapper,
        JwtTokenService jwtTokenService,
        PasswordEncoder passwordEncoder,
        RateLimiterService rateLimiterService,
        TenantService tenantService
    ) {
        this.adminUserMapper = adminUserMapper;
        this.adminMenuMapper = adminMenuMapper;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiterService = rateLimiterService;
        this.tenantService = tenantService;
    }

    public String login(String username, String password, String clientIp) {
        Tenant tenant = tenantService.requireCurrentTenant();
        String rateLimitKey = "admin:" + tenant.getTenantCode() + ":" + (clientIp != null ? clientIp : "unknown") + ":" + username;
        if (!rateLimiterService.isAllowed(rateLimitKey)) {
            long remaining = rateLimiterService.getRemainingLockoutSeconds(rateLimitKey);
            throw new BusinessException(ErrorCode.RATE_LIMITED, "Login attempts are too frequent, retry after " + remaining + " seconds");
        }

        AdminUser user = adminUserMapper.selectByUsernameAndTenantId(username, tenant.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid username or password");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.STATE_INVALID, "Account is disabled");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            rateLimiterService.recordFailure(rateLimitKey);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED, "Invalid username or password");
        }

        rateLimiterService.recordSuccess(rateLimitKey);
        List<String> permissions = adminUserMapper.selectPermissionCodes(user.getId());
        return jwtTokenService.createAdminToken(
            user.getId(),
            user.getUsername(),
            tenant.getId(),
            tenant.getTenantCode(),
            tenant.getId(),
            tenant.getTenantCode(),
            permissions
        );
    }

    public MeResponse me(AuthenticatedUser authenticatedUser) {
        Long tenantId = authenticatedUser.tenantId() == null
            ? tenantService.requireCurrentTenant().getId()
            : authenticatedUser.tenantId();
        Long sourceTenantId = authenticatedUser.sourceTenantId() == null ? tenantId : authenticatedUser.sourceTenantId();
        AdminUser user = adminUserMapper.selectActiveByIdAndTenantId(authenticatedUser.userId(), sourceTenantId);
        if (user == null) {
            throw new BusinessException(ErrorCode.SESSION_INVALID, "Session is invalid, please sign in again");
        }

        Tenant tenant = tenantService.requireTenantById(tenantId);
        Tenant loginTenant = sourceTenantId.equals(tenantId) ? tenant : tenantService.requireTenantById(sourceTenantId);
        List<String> permissions = adminUserMapper.selectPermissionCodes(user.getId());
        List<AdminMenu> menus = adminMenuMapper.selectMenusByUserId(user.getId());
        return new MeResponse(
            user.getId(),
            user.getUsername(),
            tenant.getId(),
            tenant.getTenantCode(),
            tenant.getTenantName(),
            loginTenant.getId(),
            loginTenant.getTenantCode(),
            loginTenant.getTenantName(),
            !loginTenant.getId().equals(tenant.getId()),
            permissions,
            buildMenuTree(menus)
        );
    }

    public String switchTenant(AuthenticatedUser authenticatedUser, Long targetTenantId) {
        Long sourceTenantId = authenticatedUser.sourceTenantId() == null
            ? authenticatedUser.tenantId()
            : authenticatedUser.sourceTenantId();
        if (sourceTenantId == null) {
            throw new BusinessException(ErrorCode.STATE_INVALID, "Source tenant is missing");
        }

        Tenant sourceTenant = tenantService.requireTenantById(sourceTenantId);
        Tenant targetTenant = tenantService.requireTenantById(targetTenantId);
        AdminUser user = adminUserMapper.selectActiveByIdAndTenantId(authenticatedUser.userId(), sourceTenant.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.SESSION_INVALID, "Session is invalid, please sign in again");
        }

        List<String> permissions = adminUserMapper.selectPermissionCodes(user.getId());
        if (!permissions.contains("tenant:edit")) {
            throw new BusinessException(ErrorCode.AUTHORIZATION_DENIED, "Permission denied");
        }

        return jwtTokenService.createAdminToken(
            user.getId(),
            user.getUsername(),
            sourceTenant.getId(),
            sourceTenant.getTenantCode(),
            targetTenant.getId(),
            targetTenant.getTenantCode(),
            permissions
        );
    }

    private List<MeResponse.MenuNode> buildMenuTree(List<AdminMenu> menus) {
        Map<Long, MeResponse.MenuNode> nodeMap = new HashMap<>();
        for (AdminMenu menu : menus) {
            nodeMap.put(menu.getId(), new MeResponse.MenuNode(
                menu.getId(),
                menu.getName(),
                menu.getPath(),
                menu.getComponent(),
                menu.getIcon(),
                new ArrayList<>()
            ));
        }

        List<MeResponse.MenuNode> roots = new ArrayList<>();
        for (AdminMenu menu : menus) {
            MeResponse.MenuNode node = nodeMap.get(menu.getId());
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(node);
                continue;
            }

            MeResponse.MenuNode parent = nodeMap.get(menu.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.children().add(node);
            }
        }
        return roots;
    }
}
