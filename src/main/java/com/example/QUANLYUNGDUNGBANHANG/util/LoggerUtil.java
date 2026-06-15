package com.example.QUANLYUNGDUNGBANHANG.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LoggerUtil — Ghi log hệ thống.
 * Ghi đồng thời ra console (stdout/stderr) và file system.log.
 * Định dạng: yyyy-MM-dd HH:mm:ss | [USERNAME] | [ACTION] | [STATUS] | [DETAIL]
 */
public class LoggerUtil {

    private static final String LOG_FILE = "system.log";
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===== API công khai =====

    /**
     * Ghi log thông tin (INFO).
     */
    public static void info(String username, String action, String detail) {
        log("INFO", username, action, detail);
    }

    /**
     * Ghi log cảnh báo (WARN).
     */
    public static void warn(String username, String action, String detail) {
        log("WARN", username, action, detail);
    }

    /**
     * Ghi log lỗi (ERROR).
     */
    public static void error(String username, String action, String detail) {
        log("ERROR", username, action, detail);
    }

    /**
     * Ghi log đăng nhập thành công.
     */
    public static void loginSuccess(String username, String role, String ipAddress) {
        log("INFO", username, "LOGIN_SUCCESS",
            "Role=" + role + " | IP=" + ipAddress);
    }

    /**
     * Ghi log đăng nhập thất bại.
     */
    public static void loginFailed(String username, String ipAddress) {
        log("WARN", username, "LOGIN_FAILED",
            "Sai tài khoản/mật khẩu | IP=" + ipAddress);
    }

    // ===== Nội bộ =====

    private static synchronized void log(String level, String username,
                                         String action, String detail) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String line = String.format("%s | %-5s | %-20s | %-30s | %s",
            timestamp,
            level,
            username == null ? "SYSTEM" : username,
            action,
            detail == null ? "" : detail
        );

        // In ra console
        if ("ERROR".equals(level) || "WARN".equals(level)) {
            System.err.println("[LOG] " + line);
        } else {
            System.out.println("[LOG] " + line);
        }

        // Ghi vào file (append)
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(LOG_FILE, true)))) {
            pw.println(line);
        } catch (Exception e) {
            System.err.println("[LoggerUtil] Không thể ghi file log: " + e.getMessage());
        }
    }
}
