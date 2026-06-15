package com.example.QUANLYUNGDUNGBANHANG.dao.impl;

import com.example.QUANLYUNGDUNGBANHANG.dao.KhachHangDAO;
import com.example.QUANLYUNGDUNGBANHANG.model.KhachHang;
import com.example.QUANLYUNGDUNGBANHANG.util.AESUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * KhachHangDAOImpl — Tự động mã hóa AES cho SĐT và Email khi ghi vào DB.
 * Và tự động giải mã khi đọc ra để hiển thị đầy đủ (dành cho Server).
 * Việc masking thông tin của USER được thực hiện ở tầng ClientHandler.
 */
public class KhachHangDAOImpl implements KhachHangDAO {

    /** Đọc từ DB và giải mã SĐT, Email tự động */
    private KhachHang mapRow(ResultSet rs) throws Exception {
        return new KhachHang(
            rs.getString("makh"),
            rs.getString("tenkh"),
            rs.getString("ngaysinh"),
            AESUtil.decrypt(rs.getString("sdt")),
            AESUtil.decrypt(rs.getString("email")),
            rs.getString("diachi"),
            rs.getString("loaikh")
        );
    }

    @Override
    public List<KhachHang> findAll() {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT * FROM khachhang";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(KhachHang kh) {
        String sql = "INSERT INTO khachhang(makh, tenkh, ngaysinh, sdt, email, diachi, loaikh) " +
                     "VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKH());
            ps.setString(2, kh.getTenKH());
            ps.setString(3, kh.getNgaySinh());
            // Mã hóa AES trước khi lưu
            ps.setString(4, AESUtil.encrypt(kh.getSoDienThoai()));
            ps.setString(5, AESUtil.encrypt(kh.getEmail()));
            ps.setString(6, kh.getDiaChi());
            ps.setString(7, kh.getLoaiKH());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(KhachHang kh) {
        String sql = "UPDATE khachhang SET tenkh=?, ngaysinh=?, sdt=?, email=?, diachi=?, loaikh=? WHERE makh=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getTenKH());
            ps.setString(2, kh.getNgaySinh());
            // Mã hóa AES trước khi lưu
            ps.setString(3, AESUtil.encrypt(kh.getSoDienThoai()));
            ps.setString(4, AESUtil.encrypt(kh.getEmail()));
            ps.setString(5, kh.getDiaChi());
            ps.setString(6, kh.getLoaiKH());
            ps.setString(7, kh.getMaKH());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String maKH) {
        String sql = "DELETE FROM khachhang WHERE makh = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKH);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public KhachHang findById(String maKH) {
        String sql = "SELECT * FROM khachhang WHERE makh = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKH);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
