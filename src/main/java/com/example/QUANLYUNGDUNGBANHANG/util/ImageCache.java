package com.example.QUANLYUNGDUNGBANHANG.util;

import javafx.scene.image.Image;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ImageCache — cache ảnh URL để tránh tải lại mỗi lần refreshGrid().
 * Dùng LRU cache tối đa 80 ảnh.
 */
public class ImageCache {
    private static final int MAX = 80;
    private static final Map<String, Image> cache = new LinkedHashMap<>(MAX, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
            return size() > MAX;
        }
    };

    private ImageCache() {}

    /**
     * Lấy ảnh từ cache hoặc tải mới (background loading).
     * @param url URL của ảnh
     * @return Image object (có thể chưa load xong nếu mới tải)
     */
    public static Image get(String url) {
        if (url == null || url.isBlank()) return null;
        return cache.computeIfAbsent(url, u -> {
            try {
                return new Image(u, true); // background load
            } catch (Exception e) {
                return null;
            }
        });
    }

    /** Xóa toàn bộ cache (gọi khi cần giải phóng bộ nhớ) */
    public static void clear() {
        cache.clear();
    }
}
