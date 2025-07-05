package com.seeat.server.security.jwt;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
/**
 * JWT 토큰 생성 및 검증
 *
 * Authentication 객체를 기반으로 액세스 토큰과 리프레시 토큰을 생성,
 * JWT 토큰의 서명 검증 및 유효성 검사를 수행,
 * 토큰에서 권한 정보를 추출하여 Spring Security Authentication 객체로 변환
 */
@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    private final String AUTHORITIES_KEY = "auth";
    private final String USER_ID_KEY = "userId";

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidTime;

    private final UserRepository userRepository;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성 시 User 엔티티에서 id, 권한 등을 넣음
    public String generateToken(Authentication authentication, long tokenValidTime) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String authoritiesStr = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidTime);

        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authoritiesStr)
                .claim(USER_ID_KEY, userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, accessTokenValidTime);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, refreshTokenValidTime);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        Long userId = claims.get(USER_ID_KEY, Long.class);
        User user = userRepository.findById(userId)
                // 공통에러처리
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }
}
