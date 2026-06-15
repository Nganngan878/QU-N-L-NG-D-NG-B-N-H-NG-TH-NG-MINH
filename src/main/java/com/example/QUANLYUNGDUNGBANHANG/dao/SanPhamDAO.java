package com.example.QUANLYUNGDUNGBANHANG.dao;

import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import java.util.List;

public interface SanPhamDAO {
    List<SanPham> findAll();
    boolean insert(SanPham sp);
    boolean update(SanPham sp);
    boolean delete(String maSP);
    SanPham findById(String maSP);
}
