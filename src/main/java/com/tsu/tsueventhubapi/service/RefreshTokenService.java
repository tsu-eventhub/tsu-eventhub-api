package com.tsu.tsueventhubapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public void storeRefreshToken(String token, String email, long expirationMs) {
        redisTemplate.opsForValue().set(token, email, expirationMs, TimeUnit.MILLISECONDS);
    }

    public boolean validateRefreshToken(String token) {
        return redisTemplate.hasKey(token);
    }

    public String getEmailFromToken(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    public void deleteRefreshToken(String token) {
        redisTemplate.delete(token);
    }
}
