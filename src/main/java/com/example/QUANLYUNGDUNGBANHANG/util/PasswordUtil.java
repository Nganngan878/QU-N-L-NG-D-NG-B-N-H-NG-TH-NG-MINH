package com.example.QUANLYUNGDUNGBANHANG.util;

import java.security.MessageDigest;

/**
 * PasswordUtil — Băm mật khẩu bằng SHA-256 (một chiều).
 * Mật khẩu gốc KHÔNG bao giờ được lưu trong DB — chỉ lưu chuỗi hash.
 */
public class PasswordUtil {

    /**
     * Băm mật khẩu dạng plain-text bằng SHA-256.
     * @param password Mật khẩu gốc
     * @return Chuỗi hash hex 64 ký tự (không thể đảo ngược)
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // VD: "1234" → "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
