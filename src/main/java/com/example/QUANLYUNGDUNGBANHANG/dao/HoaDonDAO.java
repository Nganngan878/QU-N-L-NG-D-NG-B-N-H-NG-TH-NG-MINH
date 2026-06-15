package com.example.QUANLYUNGDUNGBANHANG.dao;

import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import java.util.List;

public interface HoaDonDAO {
    List<HoaDon> findAll();
    boolean insert(HoaDon hd);
    boolean update(HoaDon hd);
    boolean delete(String maHD);
    boolean insertAll(List<HoaDon> listHD);
    List<HoaDon> findByCondition(String condition);
    List<HoaDon> findByCreatedBy(String username);
}
