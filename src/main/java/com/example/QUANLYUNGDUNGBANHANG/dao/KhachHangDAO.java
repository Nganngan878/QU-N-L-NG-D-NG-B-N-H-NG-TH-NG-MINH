package com.example.QUANLYUNGDUNGBANHANG.dao;

import com.example.QUANLYUNGDUNGBANHANG.model.KhachHang;
import java.util.List;

public interface KhachHangDAO {
    List<KhachHang> findAll();
    boolean insert(KhachHang kh);
    boolean update(KhachHang kh);
    boolean update(String oldMaKH, KhachHang kh);
    boolean delete(String maKH);
    KhachHang findById(String maKH);
}
