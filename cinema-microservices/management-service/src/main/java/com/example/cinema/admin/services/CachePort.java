package com.example.cinema.admin.services;

import java.time.Duration;

public interface CachePort {
    void set(String key, String value, Duration timeout);
    void set(String key, String value);
    Boolean setIfAbsent(String key, String value, Duration timeout);
    void delete(String key);
    void delete(java.util.Collection<String> keys);
    String get(String key);
    java.util.Set<String> keys(String pattern);
}
