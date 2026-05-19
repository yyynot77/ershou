package com.campus.ershou.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaptchaStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public String save(String code) {
        String key = UUID.randomUUID().toString();
        store.put(key, code.toLowerCase());
        return key;
    }

    public boolean verify(String key, String code) {
        if (key == null || code == null) return false;
        String expected = store.remove(key);
        return expected != null && expected.equalsIgnoreCase(code.trim());
    }
}
