package com.seeat.server.global.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;
/**
 * Redis와의 상호작용을 담당하는 서비스 클래스
 *
 * Redis에 일반 값 저장, 조회, 삭제 기능을 제공,
 * 토큰 관리 등 임시 저장소로 Redis를 사용한다.
 */
@Component
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";

    public void setValues(String key, Object data) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValues(String key, Object data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    @Transactional(readOnly = true)
    public <T> T getValues(String key, Class<T> clazz) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        Object value = values.get(key);
        if (value == null) {
            return null;
        }
        return objectMapper.convertValue(value, clazz);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    public void setRefreshToken(Long userId, String refreshToken, Duration duration) {
        setValues(REFRESH_TOKEN_PREFIX + userId, refreshToken, duration);
    }

    public String getRefreshToken(Long userId) {
        return getValues(REFRESH_TOKEN_PREFIX + userId, String.class);
    }

    public void deleteRefreshToken(Long userId) {
        deleteValues(REFRESH_TOKEN_PREFIX + userId);
    }
}
