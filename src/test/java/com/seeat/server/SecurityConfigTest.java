package com.seeat.server;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    private SecretKey getTestSecretKey() {
        String testSecret = "testSecretKeyForJwtWhichIsLongEnough1234567890abcdef";
        return Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String generateTestJwt() {
        SecretKey secretKey = getTestSecretKey();
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject("test-user")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 3600_000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

//    열려있는 get controller 없어서 주석처리
//    @Test
//    void should_allow_access_to_permitted_endpoint_without_authentication() throws Exception {
//        // Given
//        String url = "/api/v1/users";
//
//        // When // Then
//        mockMvc.perform(get(url))
//                .andExpect(status().isOk());
//    }

    @Test
    void should_redirect_when_accessing_protected_url_without_authentication() throws Exception {
        // Given
        String url = "/test";

        // When // Then
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection());
    }

//    열려있는 get controller없어서 주석처리
//    @Test
//    void should_allow_access_to_protected_url_with_valid_jwt() throws Exception {
//        // Given
//        String url = "/test";
//        String jwt = generateTestJwt();
//
//        // When // Then
//        mockMvc.perform(get(url)
//                        .header("Authorization", "Bearer " + jwt))
//                .andExpect(status().isOk());
//    }
}
