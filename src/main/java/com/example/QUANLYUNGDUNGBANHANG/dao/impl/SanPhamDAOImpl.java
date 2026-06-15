package com.example.QUANLYUNGDUNGBANHANG.dao.impl;

import com.example.QUANLYUNGDUNGBANHANG.dao.SanPhamDAO;
import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import com.example.QUANLYUNGDUNGBANHANG.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAOImpl implements SanPhamDAO {

    @Override
    public List<SanPham> findAll() {
        List<SanPham> list = new ArrayList<>();
        String sql = "SELECT * FROM sanpham";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new SanPham(
                        rs.getString("masp"),
                        rs.getString("tensp"),
                        rs.getString("loai"),
                        rs.getString("gianhap"),
                        rs.getInt("soluongton"),
                        rs.getString("hinhanh")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean insert(SanPham sp) {
        String sql = "INSERT INTO sanpham(masp, tensp, loai, gianhap, soluongton, hinhanh) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sp.getMa());
            ps.setString(2, sp.getTen());
            ps.setString(3, sp.getLoai());
            ps.setString(4, sp.getGiaNhap());
            ps.setInt(5, sp.getSoLuongTon());
            ps.setString(6, sp.getHinhAnh());
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(SanPham sp) {
        String sql = "UPDATE sanpham SET tensp=?, loai=?, gianhap=?, soluongton=?, hinhanh=? WHERE masp=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, sp.getTen());
            ps.setString(2, sp.getLoai());
            ps.setString(3, sp.getGiaNhap());
            ps.setInt(4, sp.getSoLuongTon());
            ps.setString(5, sp.getHinhAnh());
            ps.setString(6, sp.getMa());
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String maSP) {
        String sql = "DELETE FROM sanpham WHERE masp = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maSP);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public SanPham findById(String maSP) {
        String sql = "SELECT * FROM sanpham WHERE masp = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SanPham(
                            rs.getString("masp"),
                            rs.getString("tensp"),
                            rs.getString("loai"),
                            rs.getString("gianhap"),
                            rs.getInt("soluongton"),
                            rs.getString("hinhanh")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
