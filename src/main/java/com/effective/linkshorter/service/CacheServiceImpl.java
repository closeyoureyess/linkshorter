package com.effective.linkshorter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;

    @Autowired
    public CacheServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Optional<Object> getFromCache(String cacheName, Object key) {

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper cacheValueWrapper = cache.get(key);
            if (cacheValueWrapper != null) {
                return Optional.of(cacheValueWrapper.get());
            }
        }
        return Optional.empty();
    }
}
