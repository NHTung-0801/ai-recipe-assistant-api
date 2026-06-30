package com.project.recipe_assistant.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.project.recipe_assistant.constant.AppConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cấu hình caching với Caffeine - in-memory cache provider hiệu năng cao.
 * <p>
 * Vì sao cache ở đây có lợi: gọi Gemini API tốn ~2-5 giây mỗi request và mất quota.
 * Người dùng hay tìm những combo nguyên liệu phổ biến (gà + rau, bò + khoai tây...) —
 * cache giảm response time xuống <10ms và tiết kiệm chi phí AI.
 * <p>
 * Tham số cấu hình (tuning cho student project):
 * <ul>
 *   <li><b>TTL 24 giờ:</b> đủ dài để hit cao, đủ ngắn để công thức không bị "stale" lâu.</li>
 *   <li><b>maximumSize 1000:</b> mỗi entry ~vài KB, total RAM ~vài MB - an toàn.
 *       Khi quá 1000, Caffeine tự evict theo thuật toán LFU (least frequently used).</li>
 * </ul>
 */
@Configuration
@EnableCaching // Bật Spring Cache - cho phép dùng annotation @Cacheable/@CacheEvict
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                AppConstants.CACHE_RECIPE_SUGGESTIONS);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats()); // Bật stats để có thể expose qua Actuator sau này nếu cần
        return cacheManager;
    }
}
