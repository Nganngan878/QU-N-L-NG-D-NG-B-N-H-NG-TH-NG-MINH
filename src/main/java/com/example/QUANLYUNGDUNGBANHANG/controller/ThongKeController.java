package com.example.QUANLYUNGDUNGBANHANG.controller;

import com.example.QUANLYUNGDUNGBANHANG.dao.HoaDonDAO;
import com.example.QUANLYUNGDUNGBANHANG.dao.impl.HoaDonDAOImpl;
import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;

import java.util.List;

public class ThongKeController {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();

    public List<HoaDon> getHoaDonList() {
        return hoaDonDAO.findAll();
    }

    public List<HoaDon> locTheoNgay(String ngay) {
        return hoaDonDAO.findByCondition("WHERE ngay = '" + ngay + "'");
    }

    public List<HoaDon> locTheoThangNam(String thang, String nam) {
        // Đây là ví dụ lọc đơn giản (tuỳ định dạng chuỗi lưu trong DB)
        // Nếu bạn lưu "dd/MM/yyyy", dùng LIKE
        return hoaDonDAO.findByCondition("WHERE ngay LIKE '%/" + thang + "/" + nam + "'");
    }

    public List<HoaDon> locTheoKhoangThoiGian(String tuNgay, String denNgay) {
        // Tuỳ vào cách bạn lưu ngày tháng trong Database mà điều chỉnh logic này
        // (Cách tốt nhất là lưu kiểu DATE trong DB, nhưng hiện tại đang lưu chuỗi VARCHAR)
        return hoaDonDAO.findAll(); // Tạm thời trả về tất cả
    }
}
