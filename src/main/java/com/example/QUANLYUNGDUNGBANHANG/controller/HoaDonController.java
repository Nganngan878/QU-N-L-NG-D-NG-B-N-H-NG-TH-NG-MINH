package com.example.QUANLYUNGDUNGBANHANG.controller;

import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import com.example.QUANLYUNGDUNGBANHANG.network.Request;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.network.SocketClient;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.HoaDonDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * HoaDonController — gọi Server qua SocketClient.
 */
public class HoaDonController {

    @SuppressWarnings("unchecked")
    public List<HoaDon> getHoaDonHistory() {
        Response res = SocketClient.getInstance().sendRequest(new Request("GET_HOADON"));
        List<HoaDon> result = new ArrayList<>();
        if (res.isSuccess() && res.getData() instanceof List) {
            for (HoaDonDTO dto : (List<HoaDonDTO>) res.getData()) {
                HoaDon hd = new HoaDon(dto.getMaHD(), dto.getNgay(), dto.getKhachHang(),
                        dto.getSanPham(), dto.getTenSanPham(), dto.getSoLuong(),
                        dto.getDonGia(), dto.getTongTien(), dto.getSdt(), dto.getLoaiKH());
                hd.setGiamGia(dto.getGiamGia());
                hd.setTenKhachHang(dto.getTenKhachHang() != null ? dto.getTenKhachHang() : dto.getKhachHang());
                result.add(hd);
            }
        }
        return result;
    }

    public boolean saveHoaDonList(List<HoaDon> danhSachHD) {
        List<HoaDonDTO> dtos = new ArrayList<>();
        for (HoaDon hd : danhSachHD) {
            dtos.add(new HoaDonDTO(hd.getMaHD(), hd.getNgay(), hd.getKhachHang(),
                    hd.getSanPham(), hd.getTenSanPham(), hd.getSoLuong(),
                    hd.getDonGia(), hd.getTongTien()));
        }
        Request req = new Request("SAVE_HOADON");
        req.setPayload(dtos);
        return SocketClient.getInstance().sendRequest(req).isSuccess();
    }
}
