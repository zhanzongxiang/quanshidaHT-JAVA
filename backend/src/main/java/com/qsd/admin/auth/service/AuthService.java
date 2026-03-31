package com.qsd.admin.auth.service;

import com.qsd.admin.auth.dto.LoginResponse;
import com.qsd.admin.auth.dto.MeResponse;
import com.qsd.admin.auth.entity.AdminMenu;
import com.qsd.admin.auth.entity.AdminUser;
import com.qsd.admin.auth.mapper.AdminMenuMapper;
import com.qsd.admin.auth.mapper.AdminUserMapper;
import com.qsd.admin.security.JwtTokenService;
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

    public AuthService(AdminUserMapper adminUserMapper, AdminMenuMapper adminMenuMapper, JwtTokenService jwtTokenService) {
        this.adminUserMapper = adminUserMapper;
        this.adminMenuMapper = adminMenuMapper;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(String username, String password) {
        AdminUser user = adminUserMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已禁用");
        }
        if (!user.getPasswordHash().equals(password)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        List<String> permissions = adminUserMapper.selectPermissionCodes(user.getId());
        String token = jwtTokenService.createToken(user.getId(), user.getUsername(), permissions);
        return new LoginResponse(token, "Bearer");
    }

    public MeResponse me(String username) {
        AdminUser user = adminUserMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<String> permissions = adminUserMapper.selectPermissionCodes(user.getId());
        List<AdminMenu> menus = adminMenuMapper.selectMenusByUserId(user.getId());
        return new MeResponse(user.getId(), user.getUsername(), permissions, buildMenuTree(menus));
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
