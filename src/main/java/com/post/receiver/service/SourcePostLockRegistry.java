package com.post.receiver.service;

import com.post.receiver.domain.SourceSite;
import com.post.receiver.exception.WordPressSyncException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class SourcePostLockRegistry {

    private static final long LOCK_TIMEOUT_SECONDS = 180;

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T withLock(SourceSite sourceSite, Long sourcePostId, Supplier<T> action) {
        String key = sourceSite.code() + ":" + sourcePostId;
        ReentrantLock lock = locks.computeIfAbsent(key, ignored -> new ReentrantLock());
        boolean acquired;
        try {
            acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WordPressSyncException("Sync interrompido enquanto aguardava o post " + key, e);
        }
        if (!acquired) {
            throw new WordPressSyncException(
                    "Timeout aguardando sync simultâneo do post " + key,
                    503
            );
        }
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }
}
