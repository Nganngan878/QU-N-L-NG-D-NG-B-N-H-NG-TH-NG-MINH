package com.example.QUANLYUNGDUNGBANHANG.controller;

import com.example.QUANLYUNGDUNGBANHANG.model.KhachHang;
import com.example.QUANLYUNGDUNGBANHANG.network.Request;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.network.SocketClient;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.KhachHangDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * KhachHangController — gọi Server qua SocketClient.
 */
public class KhachHangController {

    @SuppressWarnings("unchecked")
    public List<KhachHang> getAllKhachHang() {
        Response res = SocketClient.getInstance().sendRequest(new Request("GET_KHACHHANG"));
        List<KhachHang> result = new ArrayList<>();
        if (res.isSuccess() && res.getData() instanceof List) {
            for (KhachHangDTO dto : (List<KhachHangDTO>) res.getData()) {
                result.add(new KhachHang(dto.getMaKH(), dto.getTenKH(), dto.getNgaySinh(),
                        dto.getSoDienThoai(), dto.getEmail(), dto.getDiaChi(), dto.getLoaiKH()));
            }
        }
        return result;
    }

    public boolean addKhachHang(KhachHang kh) {
        Request req = new Request("ADD_KHACHHANG");
        req.setPayload(new KhachHangDTO(kh.getMaKH(), kh.getTenKH(), kh.getNgaySinh(),
                kh.getSoDienThoai(), kh.getEmail(), kh.getDiaChi(), kh.getLoaiKH()));
        return SocketClient.getInstance().sendRequest(req).isSuccess();
    }

    public boolean updateKhachHang(KhachHang kh) {
        Request req = new Request("UPDATE_KHACHHANG");
        req.setPayload(new KhachHangDTO(kh.getMaKH(), kh.getTenKH(), kh.getNgaySinh(),
                kh.getSoDienThoai(), kh.getEmail(), kh.getDiaChi(), kh.getLoaiKH()));
        return SocketClient.getInstance().sendRequest(req).isSuccess();
    }

    public boolean deleteKhachHang(String maKH) {
        Request req = new Request("DELETE_KHACHHANG");
        req.addParam("ma", maKH);
        return SocketClient.getInstance().sendRequest(req).isSuccess();
    }

    public boolean exportToXml(String filePath) {
        Request req = new Request("EXPORT_KHACHHANG_XML");
        req.addParam("filePath", filePath);
        Response res = SocketClient.getInstance().sendRequest(req);
        return res.isSuccess();
    }

    public Response importFromXml(String xmlContent) {
        Request req = new Request("IMPORT_KHACHHANG_XML");
        req.setPayload(xmlContent);
        return SocketClient.getInstance().sendRequest(req);
    }
}
