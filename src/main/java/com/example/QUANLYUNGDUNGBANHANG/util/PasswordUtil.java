package com.example.QUANLYUNGDUNGBANHANG.util;

import java.security.MessageDigest;

/**
 * PasswordUtil — Hỗ trợ băm mật khẩu người dùng bằng thuật toán SHA-256.
 */
public class PasswordUtil {

    /**
     * Băm mật khẩu dạng plain-text bằng SHA-256.
     * @param password Mật khẩu gốc cần băm
     * @return Chuỗi băm hex-string
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
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
