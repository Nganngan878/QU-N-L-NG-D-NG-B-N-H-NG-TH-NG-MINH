package com.example.QUANLYUNGDUNGBANHANG.util;

import java.sql.Connection;
import java.sql.Statement;

/**
 * ClearData — Lớp tiện ích dọn dẹp hóa đơn trên database phục vụ kiểm thử.
 * Đã chuyển vào package util để đảm bảo cấu trúc thư mục dự án sạch sẽ.
 */
public class ClearData {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Xóa toàn bộ hóa đơn phục vụ reset dữ liệu test
            stmt.executeUpdate("DELETE FROM hoadon");
            
            System.out.println("✅ Đã xóa sạch toàn bộ Hóa Đơn trên Database!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
