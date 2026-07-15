package com.qsd.admin.member.service;

import com.qsd.admin.member.dto.MemberLoginResponse;
import com.qsd.admin.member.dto.MemberMeResponse;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberAuthService {
    private final MemberUserMapper memberUserMapper;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public MemberAuthService(
        MemberUserMapper memberUserMapper,
        JwtTokenService jwtTokenService,
        PasswordEncoder passwordEncoder
    ) {
        this.memberUserMapper = memberUserMapper;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public MemberLoginResponse login(String account, String password) {
        MemberUser user = memberUserMapper.selectByUsernameOrMobile(account.trim());
        if (user == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        if (!"ENABLED".equals(user.getStatus())) {
            throw new IllegalArgumentException("会员账号已禁用");
        }
        if (!passwordMatches(user, password)) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        user.setLastLoginAt(LocalDateTime.now());
        memberUserMapper.updateById(user);
        String token = jwtTokenService.createToken(user.getId(), user.getUsername(), List.of("member"), JwtTokenService.USER_TYPE_MEMBER);
        return new MemberLoginResponse(token, "Bearer");
    }

    public MemberMeResponse me(String username) {
        MemberUser user = memberUserMapper.selectByUsernameOrMobile(username);
        if (user == null) {
            throw new IllegalArgumentException("会员不存在");
        }
        return new MemberMeResponse(
            user.getId(),
            user.getMemberNo(),
            user.getUsername(),
            user.getMobile(),
            user.getNickname(),
            user.getRealName(),
            user.getLevelCode(),
            user.getStatus()
        );
    }

    private boolean passwordMatches(MemberUser user, String rawPassword) {
        String storedPassword = user.getPasswordHash();
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        if (!storedPassword.equals(rawPassword)) {
            return false;
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return true;
    }
}
