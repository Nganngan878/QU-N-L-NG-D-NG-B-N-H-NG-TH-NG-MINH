package com.example.QUANLYUNGDUNGBANHANG.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AESUtil — Mã hóa và giải mã dữ liệu bằng AES-128.
 * Dùng để bảo mật thông tin nhạy cảm của khách hàng (SĐT, Email) khi lưu vào Database.
 *
 * Cách dùng:
 *   - Khi INSERT/UPDATE:  lưu AESUtil.encrypt(plainText)  vào DB
 *   - Khi SELECT (Admin): hiển thị AESUtil.decrypt(cipherText)
 *   - Khi SELECT (User):  hiển thị AESUtil.mask(AESUtil.decrypt(cipherText))
 */
public class AESUtil {

    private static final String ALGORITHM = "AES";

    /** Lấy khóa từ ConfigUtil (phải đúng 16 ký tự) */
    private static SecretKeySpec getKey() {
        String rawKey = ConfigUtil.aesKey();
        // Đảm bảo đúng 16 bytes
        byte[] keyBytes = new byte[16];
        byte[] src = rawKey.getBytes();
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 16));
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Mã hóa chuỗi văn bản thường thành chuỗi Base64.
     * @param plainText  Văn bản gốc (ví dụ: SĐT, Email)
     * @return           Chuỗi đã mã hóa (Base64) hoặc chuỗi rỗng nếu đầu vào null/rỗng
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return plainText;
        // Nếu đã mã hóa rồi thì bỏ qua (tránh mã hóa 2 lần)
        if (isEncrypted(plainText)) return plainText;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("[AESUtil] Lỗi mã hóa: " + e.getMessage());
            return plainText; // Trả về bản gốc nếu thất bại
        }
    }

    /**
     * Giải mã chuỗi Base64 về văn bản gốc.
     * @param cipherText  Chuỗi đã mã hóa (Base64)
     * @return            Văn bản gốc hoặc chuỗi gốc nếu giải mã thất bại
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) return cipherText;
        // Nếu không phải Base64 hợp lệ thì trả về nguyên (dữ liệu cũ chưa mã hóa)
        if (!isEncrypted(cipherText)) return cipherText;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), "UTF-8");
        } catch (Exception e) {
            // Có thể dữ liệu chưa mã hóa, trả về nguyên bản
            return cipherText;
        }
    }

    /**
     * Che giấu thông tin nhạy cảm (dành cho vai trò USER).
     * SĐT: 0912345678 → 091***678
     * Email: test@gmail.com → te***@gmail.com
     * @param text  Văn bản đã giải mã
     * @return      Văn bản đã che giấu
     */
    public static String mask(String text) {
        if (text == null || text.isEmpty()) return text;

        // Kiểm tra nếu là email
        if (text.contains("@")) {
            int atIdx = text.indexOf('@');
            String local = text.substring(0, atIdx);
            String domain = text.substring(atIdx);
            if (local.length() <= 2) {
                return local + "***" + domain;
            }
            return local.substring(0, 2) + "***" + domain;
        }

        // Giả sử là SĐT hoặc chuỗi số
        if (text.length() <= 6) return text.substring(0, 1) + "***";
        return text.substring(0, 3) + "***" + text.substring(text.length() - 3);
    }

    /**
     * Kiểm tra chuỗi có phải đã được mã hóa AES (Base64) hay chưa.
     * Dùng heuristic: Base64 hợp lệ và độ dài chia hết cho 4.
     */
    private static boolean isEncrypted(String text) {
        if (text == null || text.length() % 4 != 0) return false;
        try {
            Base64.getDecoder().decode(text);
            // Thêm điều kiện: Base64 thuần ký tự [A-Za-z0-9+/=]
            return text.matches("^[A-Za-z0-9+/]+=*$");
        } catch (Exception e) {
            return false;
        }
    }
}
