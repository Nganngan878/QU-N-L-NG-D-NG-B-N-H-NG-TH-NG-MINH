package com.example.QUANLYUNGDUNGBANHANG.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(
            ConfigUtil.dbUrl(),
            ConfigUtil.dbUser(),
            ConfigUtil.dbPassword()
        );
    }

    public static void setupDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("🔧 Đang cấu hình Database trên Supabase (PostgreSQL)...");

            // 1. Tạo bảng Users (Dùng cho Security & Authentication)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(100) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "role VARCHAR(50) DEFAULT 'USER')");

            // Thêm các cột cho hồ sơ người dùng nếu chưa có (PostgreSQL 15+ hỗ trợ IF NOT EXISTS)
            try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS fullname VARCHAR(255)"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS bio VARCHAR(500)"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(20)"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS dob VARCHAR(50)"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20)"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100)"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url TEXT"); } catch(Exception ignored) {}

            // Tạo sẵn một tài khoản Admin mặc định nếu chưa có
            String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = 'ngan@'";
            java.sql.ResultSet rs = stmt.executeQuery(checkUserSql);
            rs.next();
            if (rs.getInt(1) == 0) {
                String insertAdminSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertAdminSql)) {
                    ps.setString(1, "ngan@");
                    ps.setString(2, PasswordUtil.hashPassword("1234")); // Hash password
                    ps.setString(3, "ADMIN");
                    ps.executeUpdate();
                    System.out.println("✅ Đã tạo tài khoản admin mặc định.");
                }
            }

            // Tạo sẵn tài khoản 'ngân ngân' nếu chưa có
            String checkNganNganSql = "SELECT COUNT(*) FROM users WHERE username = 'ngân ngân'";
            java.sql.ResultSet rsNganNgan = stmt.executeQuery(checkNganNganSql);
            rsNganNgan.next();
            if (rsNganNgan.getInt(1) == 0) {
                String insertNganNganSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertNganNganSql)) {
                    ps.setString(1, "ngân ngân");
                    ps.setString(2, PasswordUtil.hashPassword("1234"));
                    ps.setString(3, "USER");
                    ps.executeUpdate();
                    System.out.println("✅ Đã tạo tài khoản 'ngân ngân'.");
                }
            }

            // Tạo sẵn tài khoản 'nganngan' (không dấu) nếu chưa có
            String checkNganNganKdSql = "SELECT COUNT(*) FROM users WHERE username = 'nganngan'";
            java.sql.ResultSet rsNganNganKd = stmt.executeQuery(checkNganNganKdSql);
            rsNganNganKd.next();
            if (rsNganNganKd.getInt(1) == 0) {
                String insertNganNganKdSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertNganNganKdSql)) {
                    ps.setString(1, "nganngan");
                    ps.setString(2, PasswordUtil.hashPassword("1234"));
                    ps.setString(3, "USER");
                    ps.executeUpdate();
                    System.out.println("✅ Đã tạo tài khoản 'nganngan'.");
                }
            }

            // 2. Tạo bảng Sản phẩm
            stmt.execute("CREATE TABLE IF NOT EXISTS sanpham (" +
                    "masp VARCHAR(50) PRIMARY KEY, " +
                    "tensp VARCHAR(255), " +
                    "loai VARCHAR(50), " +
                    "gianhap VARCHAR(50), " +
                    "soluongton INT DEFAULT 0)");
                    
            try {
                stmt.execute("ALTER TABLE sanpham ADD COLUMN IF NOT EXISTS hinhanh VARCHAR(1000)");
            } catch (Exception e) {
                System.out.println("Cột hinhanh đã tồn tại hoặc không thể thêm: " + e.getMessage());
            }

            seedProducts(conn);

            // 3. Tạo bảng Khách hàng
            stmt.execute("CREATE TABLE IF NOT EXISTS khachhang (" +
                    "makh VARCHAR(50) PRIMARY KEY, " +
                    "tenkh VARCHAR(255), " +
                    "ngaysinh VARCHAR(20), " +
                    "sdt VARCHAR(255), " +
                    "email VARCHAR(255), " +
                    "diachi VARCHAR(255), " +
                    "loaikh VARCHAR(50) DEFAULT 'Thường')");

            try { stmt.execute("ALTER TABLE khachhang ALTER COLUMN sdt TYPE VARCHAR(255)"); } catch(Exception ignored) {}
            try { stmt.execute("ALTER TABLE khachhang ALTER COLUMN email TYPE VARCHAR(255)"); } catch(Exception ignored) {}

            // 4. Tạo bảng Hóa đơn
            stmt.execute("CREATE TABLE IF NOT EXISTS hoadon (" +
                    "id SERIAL PRIMARY KEY, " +
                    "mahd VARCHAR(50), " +
                    "ngay VARCHAR(50), " +
                    "makh VARCHAR(50), " +
                    "masp VARCHAR(50), " +
                    "soluong INT, " +
                    "dongia DECIMAL(15,2), " +
                    "giamgia DECIMAL(15,2) DEFAULT 0, " +
                    "tongtien DECIMAL(15,2), " +
                    "created_by VARCHAR(50) DEFAULT 'admin')");

            try {
                stmt.execute("ALTER TABLE hoadon ADD COLUMN IF NOT EXISTS created_by VARCHAR(50) DEFAULT 'admin'");
            } catch (Exception e) {
                System.out.println("Cột created_by đã tồn tại hoặc không thể thêm: " + e.getMessage());
            }

            System.out.println("✅ Kết nối và khởi tạo Database thành công!");

        } catch (Exception e) {
            System.err.println("❌ Lỗi kết nối Database: " + e.getMessage());
            System.err.println("Lưu ý: Hãy chắc chắn bạn đã điền đúng URL và Password của Supabase trong util/DBConnection.java");
        }
    }

    private static void seedProducts(Connection conn) throws Exception {
        String checkSql = "SELECT COUNT(*) FROM sanpham";
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(checkSql)) {
            rs.next();
            int currentCount = rs.getInt(1);
            if (currentCount >= 5) {
                System.out.println("ℹ️ Đã có " + currentCount + " sản phẩm, bỏ qua seed data.");
                return;
            }
        }

        System.out.println("🌱 Đang seed 35 sản phẩm mẫu...");

        String sql = "INSERT INTO sanpham(masp, tensp, loai, gianhap, soluongton, hinhanh) " +
                     "VALUES(?,?,?,?,?,?) ON CONFLICT (masp) DO NOTHING";

        Object[][] products = {
            // --- Trái cây ---
            {"SP001", "Táo Đỏ Mỹ", "Trái cây", "45000", 99,
             "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=400&q=80"},
            {"SP002", "Cam Vàng Úc", "Trái cây", "38000", 99,
             "https://images.unsplash.com/photo-1547514701-42782101795e?w=400&q=80"},
            {"SP003", "Chuối Tiêu Việt Nam", "Trái cây", "20000", 99,
             "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=400&q=80"},
            {"SP004", "Nho Đen Hàn Quốc", "Trái cây", "120000", 99,
             "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400&q=80"},
            {"SP005", "Dâu Tây Đà Lạt", "Trái cây", "65000", 99,
             "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400&q=80"},
            {"SP006", "Xoài Cát Hòa Lộc", "Trái cây", "55000", 99,
             "https://images.unsplash.com/photo-1553279768-865429fa0078?w=400&q=80"},
            {"SP007", "Bơ Booth Đắk Lắk", "Trái cây", "40000", 99,
             "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=400&q=80"},

            // --- Đồ uống ---
            {"SP008", "Nước Cam Vắt Tươi", "Đồ uống", "25000", 99,
             "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400&q=80"},
            {"SP009", "Trà Sữa Trân Châu", "Đồ uống", "35000", 99,
             "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&q=80"},
            {"SP010", "Cà Phê Đen Nguyên Chất", "Đồ uống", "30000", 99,
             "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&q=80"},
            {"SP011", "Nước Dừa Tươi", "Đồ uống", "20000", 99,
             "https://images.unsplash.com/photo-1548173228-bf06d8800bc3?w=400&q=80"},
            {"SP012", "Sinh Tố Bơ Mật Ong", "Đồ uống", "45000", 99,
             "https://images.unsplash.com/photo-1638176066666-ffb2f013c7dd?w=400&q=80"},

            // --- Đồ ăn ---
            {"SP013", "Bánh Mì Pate Sài Gòn", "Đồ ăn", "20000", 99,
             "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&q=80"},
            {"SP014", "Phở Bò Hà Nội", "Đồ ăn", "55000", 99,
             "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400&q=80"},
            {"SP015", "Cơm Tấm Sườn Nướng", "Đồ ăn", "50000", 99,
             "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400&q=80"},
            {"SP016", "Bánh Cuốn Hà Nội", "Đồ ăn", "35000", 99,
             "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&q=80"},
            {"SP017", "Pizza Hải Sản Ý", "Đồ ăn", "150000", 99,
             "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&q=80"},
            {"SP018", "Burger Bò Phô Mai", "Đồ ăn", "75000", 99,
             "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80"},

            // --- Quần áo ---
            {"SP019", "Áo Thun Nam Basic Cotton", "Quần áo", "120000", 99,
             "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&q=80"},
            {"SP020", "Quần Jeans Skinny Nữ", "Quần áo", "350000", 99,
             "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=400&q=80"},
            {"SP021", "Áo Khoác Bomber Unisex", "Quần áo", "450000", 99,
             "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&q=80"},
            {"SP022", "Váy Hoa Nữ Mùa Hè", "Quần áo", "280000", 99,
             "https://images.unsplash.com/photo-1515372039744-b8f02a3ae446?w=400&q=80"},
            {"SP023", "Áo Sơ Mi Trắng Nam", "Quần áo", "220000", 99,
             "https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=400&q=80"},

            // --- Giày dép ---
            {"SP024", "Giày Sneaker Trắng", "Giày dép", "850000", 99,
             "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&q=80"},
            {"SP025", "Dép Lê Đi Biển Thời Trang", "Giày dép", "180000", 99,
             "https://images.unsplash.com/photo-1603487742131-4160ec999306?w=400&q=80"},
            {"SP026", "Giày Cao Gót Nữ 7cm", "Giày dép", "420000", 99,
             "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=400&q=80"},
            {"SP027", "Giày Boot Nam Da Thật", "Giày dép", "950000", 99,
             "https://images.unsplash.com/photo-1608256246200-d8d462da0b32?w=400&q=80"},

            // --- Dụng cụ ---
            {"SP028", "Nồi Cơm Điện 1.8L", "Dụng cụ", "750000", 99,
             "https://images.unsplash.com/photo-1585515320310-259814833e62?w=400&q=80"},
            {"SP029", "Máy Xay Sinh Tố Công Suất Lớn", "Dụng cụ", "1200000", 99,
             "https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=400&q=80"},
            {"SP030", "Chảo Chống Dính Ceramic", "Dụng cụ", "480000", 99,
             "https://images.unsplash.com/photo-1575318634028-6a0cfcb60c59?w=400&q=80"},
            {"SP031", "Bộ Dao Nhà Bếp Inox 5 Món", "Dụng cụ", "320000", 99,
             "https://images.unsplash.com/photo-1593618998160-e34014e67546?w=400&q=80"},
            {"SP032", "Ấm Đun Siêu Tốc 1.7L", "Dụng cụ", "380000", 99,
             "https://images.unsplash.com/photo-1544441893-675973e31985?w=400&q=80"},

            // --- Mỹ phẩm ---
            {"SP033", "Kem Dưỡng Da Ban Đêm", "Mỹ phẩm", "320000", 99,
             "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=400&q=80"},
            {"SP034", "Son Môi Lì Cao Cấp", "Mỹ phẩm", "450000", 99,
             "https://images.unsplash.com/photo-1586495777744-4e6232bf2f8d?w=400&q=80"},
            {"SP035", "Serum Vitamin C Sáng Da", "Mỹ phẩm", "580000", 99,
             "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=400&q=80"},
        };

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] p : products) {
                ps.setString(1, (String) p[0]);
                ps.setString(2, (String) p[1]);
                ps.setString(3, (String) p[2]);
                ps.setString(4, (String) p[3]);
                ps.setInt(5, (Integer) p[4]);
                ps.setString(6, (String) p[5]);
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            int inserted = 0;
            for (int r : results) if (r > 0) inserted++;
            System.out.println("✅ Đã seed " + inserted + " sản phẩm mẫu vào database!");
        }
    }
}
