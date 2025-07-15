package com.seeat.server.security.jwt;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.entity.UserRole;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.util.JwtConstants;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

import static com.seeat.server.domain.user.domain.entity.UserSocial.KAKAO;

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

    @Value("${jwt.access-token-expiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidTime;

    @Value("${jwt.dev-token-expiration}")
    private long devTokenExpiration;

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
                .claim(JwtConstants.AUTHORITIES_KEY, authoritiesStr)
                .claim(JwtConstants.USER_ID_KEY, userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateDevTokenWithMockUser(Long userId, String username, UserRole role) {

        /// 개발용 유저 실제 DB에 저장
        User user = userRepository.findBySocialAndSocialId(KAKAO, "dev-" + userId)
                .orElse(userRepository.save(createMockUser(userId, username, role)));

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role.getRole())
        );

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(
                user, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);

        return generateToken(mockAuthentication, devTokenExpiration);
    }

    /// 기존 구조의 of는 USER ROLE만 생성되는 구조라서, 충돌이 발생합니다
    /// of 대신 바꾸는것이 좋아보입니다!
    private User createMockUser(Long userId, String username, UserRole role) {

        return User.builder()
                .email("dev@test.com")
                .socialId("dev-" + userId)
                .social(KAKAO)
                .username(username)
                .nickname("Dev User")
                .imageUrl("https://example.com/profile.jpg")
                .role(role)
                .grade(UserGrade.BRONZE)
                .genres(List.of())
                .build();
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
                Arrays.stream(claims.get(JwtConstants.AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        Long userId = claims.get(JwtConstants.USER_ID_KEY, Long.class);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new JwtAuthenticationException(ErrorCode.NOT_USER.getMessage()));

        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

}
