package com.qsd.admin.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtTokenService.parse(token);
        } catch (Exception ex) {
            filterChain.doFilter(request, response);
            return;
        }

        Object uidValue = claims.get("uid");
        Long userId = uidValue instanceof Number number ? number.longValue() : null;
        String username = claims.getSubject();
        String tokenType = claims.get("tokenType", String.class);
        AuthenticatedUser principal = new AuthenticatedUser(userId, username, tokenType);
        Collection<SimpleGrantedAuthority> authorities = extractAuthorities(claims, tokenType);
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Collection<SimpleGrantedAuthority> extractAuthorities(Claims claims, String tokenType) {
        if (JwtTokenService.TOKEN_TYPE_MEMBER.equals(tokenType)) {
            return List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }

        Object permissionObj = claims.get("permissions");
        if (!(permissionObj instanceof List<?> permissions)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        List<SimpleGrantedAuthority> authorities = permissions.stream()
            .map(Object::toString)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return authorities;
    }
}
