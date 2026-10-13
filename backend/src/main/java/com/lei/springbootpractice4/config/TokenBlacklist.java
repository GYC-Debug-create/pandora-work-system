package com.lei.springbootpractice4.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 黑名单（内存版）。
 * 退出登录时把 token 放进来，记录它本身的过期时间；
 * 过期后 token 自然失效，黑名单里的记录也随之清理，不引入 Redis。
 * 注意：服务重启后黑名单清空。
 */
@Component
public class TokenBlacklist {

    /** token -> 该 token 的过期时间（毫秒时间戳） */
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void add(String token, long expiresAtMillis) {
        if (token == null || token.isEmpty()) {
            return;
        }
        // 已经过期的 token 本来就不可用，无需记录
        if (expiresAtMillis <= System.currentTimeMillis()) {
            return;
        }
        blacklist.put(token, expiresAtMillis);
    }

    public boolean contains(String token) {
        if (token == null) {
            return false;
        }
        Long expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt <= System.currentTimeMillis()) {
            // 惰性清理：已过期的记录顺手删掉
            blacklist.remove(token, expiresAt);
            return false;
        }
        return true;
    }

    /** 每 10 分钟清理一次已过期的记录，防止内存只增不减 */
    @Scheduled(fixedDelay = 10 * 60 * 1000L)
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> e.getValue() <= now);
    }

    public int size() {
        return blacklist.size();
    }
}
