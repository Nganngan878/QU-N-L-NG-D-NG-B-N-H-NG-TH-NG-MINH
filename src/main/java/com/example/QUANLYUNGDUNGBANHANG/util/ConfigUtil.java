package com.example.QUANLYUNGDUNGBANHANG.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * ConfigUtil — Đọc file cấu hình config.properties.
 * Ưu tiên đọc từ thư mục làm việc (working directory) trước,
 * nếu không tìm thấy thì dùng giá trị mặc định được định nghĩa sẵn.
 */
public class ConfigUtil {

    private static final Properties props = new Properties();

    static {
        // Thử đọc từ file config.properties trong thư mục gốc dự án
        try (InputStream fis = Files.newInputStream(Paths.get("config.properties"))) {
            props.load(fis);
            System.out.println("[Config] Đã đọc cấu hình từ config.properties");
        } catch (Exception e) {
            System.out.println("[Config] Không tìm thấy config.properties, dùng giá trị mặc định.");
        }
    }

    /**
     * Lấy giá trị chuỗi từ file cấu hình.
     * @param key       Tên khóa
     * @param fallback  Giá trị mặc định nếu không tìm thấy
     */
    public static String get(String key, String fallback) {
        return props.getProperty(key, fallback);
    }

    /**
     * Lấy giá trị số nguyên từ file cấu hình.
     * @param key       Tên khóa
     * @param fallback  Giá trị mặc định nếu không tìm thấy hoặc không hợp lệ
     */
    public static int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ===== Các khóa cấu hình được định nghĩa sẵn =====

    /** Host của Server */
    public static String serverHost() {
        return get("server.host", "localhost");
    }

    /** Cổng kết nối Server */
    public static int serverPort() {
        return getInt("server.port", 8888);
    }

    /** URL kết nối Database */
    public static String dbUrl() {
        return get("database.url",
            "jdbc:postgresql://db.exjifugxqrdkkwetbwmm.supabase.co:5432/postgres");
    }

    /** Tên đăng nhập Database */
    public static String dbUser() {
        return get("database.user", "postgres");
    }

    /** Mật khẩu Database */
    public static String dbPassword() {
        return get("database.password", "zefCiz-jazjyw-cogne8");
    }

    /** Khóa bí mật AES (phải đúng 16 ký tự) */
    public static String aesKey() {
        return get("aes.secret.key", "QuanLyBanHang123");
    }
}
