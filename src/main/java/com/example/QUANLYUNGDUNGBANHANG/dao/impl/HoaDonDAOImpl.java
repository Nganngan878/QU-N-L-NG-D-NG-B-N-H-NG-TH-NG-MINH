package com.example.QUANLYUNGDUNGBANHANG.dao.impl;

import com.example.QUANLYUNGDUNGBANHANG.dao.HoaDonDAO;
import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import com.example.QUANLYUNGDUNGBANHANG.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAOImpl implements HoaDonDAO {

    @Override
    public List<HoaDon> findAll() {
        return findByCondition("");
    }

    @Override
    public boolean insert(HoaDon hd) {
        String sql = "INSERT INTO hoadon (mahd, ngay, makh, masp, soluong, dongia, giamgia, tongtien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, hd.getMaHD());
            ps.setString(2, hd.getNgay());
            ps.setString(3, hd.getKhachHang());
            ps.setString(4, hd.getSanPham());
            ps.setInt(5, hd.getSoLuong());
            ps.setDouble(6, hd.getDonGia());
            ps.setDouble(7, hd.getGiamGia());
            ps.setDouble(8, hd.getTongTien());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(HoaDon hd) {
        // Hóa đơn thường không sửa, nếu có có thể update theo ID
        return false;
    }

    @Override
    public boolean delete(String maHD) {
        String sql = "DELETE FROM hoadon WHERE mahd = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, maHD);
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertAll(List<HoaDon> listHD) {
        String sql = "INSERT INTO hoadon (mahd, ngay, makh, masp, soluong, dongia, giamgia, tongtien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            conn.setAutoCommit(false); // Bắt đầu transaction
            
            for (HoaDon hd : listHD) {
                ps.setString(1, hd.getMaHD());
                ps.setString(2, hd.getNgay());
                ps.setString(3, hd.getKhachHang());
                ps.setString(4, hd.getSanPham());
                ps.setInt(5, hd.getSoLuong());
                ps.setDouble(6, hd.getDonGia());
                ps.setDouble(7, hd.getGiamGia());
                ps.setDouble(8, hd.getTongTien());
                ps.addBatch();
            }
            
            ps.executeBatch();
            conn.commit(); // Hoàn thành transaction
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<HoaDon> findByCondition(String condition) {
        List<HoaDon> list = new ArrayList<>();
        // Kết nối bảng hoadon với bảng sanpham để lấy tên sản phẩm, và khachhang để lấy sdt, loaikh
        String sql = "SELECT h.*, s.tensp, k.sdt, k.loaikh, k.tenkh " +
                     "FROM hoadon h " +
                     "LEFT JOIN sanpham s ON h.masp = s.masp " +
                     "LEFT JOIN khachhang k ON h.makh = k.makh ";
                     
        if (condition != null && !condition.isEmpty()) {
            sql += condition;
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                HoaDon hd = new HoaDon(
                        rs.getString("mahd"),
                        rs.getString("ngay"),
                        rs.getString("makh"),
                        rs.getString("masp"),
                        rs.getString("tensp") != null ? rs.getString("tensp") : "Không rõ",
                        rs.getInt("soluong"),
                        rs.getDouble("dongia"),
                        rs.getDouble("tongtien"),
                        rs.getString("sdt"),
                        rs.getString("loaikh")
                );
                hd.setGiamGia(rs.getDouble("giamgia"));
                String tenkh = rs.getString("tenkh");
                if (tenkh != null) hd.setTenKhachHang(tenkh);
                list.add(hd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<HoaDon> findByCreatedBy(String username) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT h.*, s.tensp, k.sdt, k.loaikh, k.tenkh " +
                     "FROM hoadon h " +
                     "LEFT JOIN sanpham s ON h.masp = s.masp " +
                     "LEFT JOIN khachhang k ON h.makh = k.makh " +
                     "WHERE h.created_by = ?";
                     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDon hd = new HoaDon(
                            rs.getString("mahd"),
                            rs.getString("ngay"),
                            rs.getString("makh"),
                            rs.getString("masp"),
                            rs.getString("tensp") != null ? rs.getString("tensp") : "Không rõ",
                            rs.getInt("soluong"),
                            rs.getDouble("dongia"),
                            rs.getDouble("tongtien"),
                            rs.getString("sdt"),
                            rs.getString("loaikh")
                    );
                    hd.setGiamGia(rs.getDouble("giamgia"));
                    String tenkh = rs.getString("tenkh");
                    if (tenkh != null) hd.setTenKhachHang(tenkh);
                    list.add(hd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
