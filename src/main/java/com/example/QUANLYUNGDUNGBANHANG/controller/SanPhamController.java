package com.example.QUANLYUNGDUNGBANHANG.controller;

import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import com.example.QUANLYUNGDUNGBANHANG.network.Request;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.network.SocketClient;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.SanPhamDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * SanPhamController — gọi Server qua SocketClient thay vì DAO trực tiếp.
 */
public class SanPhamController {

    @SuppressWarnings("unchecked")
    public List<SanPham> getAllSanPham() {
        Response res = SocketClient.getInstance().sendRequest(new Request("GET_SANPHAM"));
        List<SanPham> result = new ArrayList<>();
        if (res.isSuccess() && res.getData() instanceof List) {
            for (SanPhamDTO dto : (List<SanPhamDTO>) res.getData()) {
                result.add(new SanPham(dto.getMa(), dto.getTen(), dto.getLoai(),
                        dto.getGiaNhap(), dto.getSoLuongTon(), dto.getHinhAnh()));
            }
        }
        return result;
    }

    public boolean addSanPham(SanPham sp) {
        Request req = new Request("ADD_SANPHAM");
        req.setPayload(new SanPhamDTO(sp.getMa(), sp.getTen(), sp.getLoai(),
                sp.getGiaNhap(), sp.getSoLuongTon(), sp.getHinhAnh()));
        return SocketClient.getInstance().sendRequest(req).isSuccess();
    }

    public boolean updateSanPham(SanPham sp) {
        Request req = new Request("UPDATE_SANPHAM");
        req.setPayload(new SanPhamDTO(sp.getMa(), sp.getTen(), sp.getLoai(),
                sp.getGiaNhap(), sp.getSoLuongTon(), sp.getHinhAnh()));
        return SocketClient.getInstance().sendRequest(req).isSuccess();
    }

    public boolean deleteSanPham(String maSP) {
        Request req = new Request("DELETE_SANPHAM");
        req.addParam("ma", maSP);
        return SocketClient.getInstance().sendRequest(req).isSuccess();
    }

    public SanPham getSanPhamById(String maSP) {
        // Lấy toàn bộ danh sách rồi lọc (đơn giản, đủ cho dự án quy mô nhỏ)
        return getAllSanPham().stream()
                .filter(sp -> sp.getMa().equalsIgnoreCase(maSP))
                .findFirst().orElse(null);
    }

    public boolean exportToXml(String filePath) {
        Request req = new Request("EXPORT_SANPHAM_XML");
        req.addParam("filePath", filePath);
        Response res = SocketClient.getInstance().sendRequest(req);
        return res.isSuccess();
    }

    public Response importFromXml(String xmlContent) {
        Request req = new Request("IMPORT_SANPHAM_XML");
        req.setPayload(xmlContent);
        return SocketClient.getInstance().sendRequest(req);
    }
}
