package com.effective.linkshorter.service;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public interface CacheService {
    Optional<Object> getFromCache(@NotNull String cacheName, @NotNull Object key);
}
