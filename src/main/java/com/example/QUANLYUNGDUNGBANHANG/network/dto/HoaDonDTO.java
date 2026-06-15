package com.example.QUANLYUNGDUNGBANHANG.network.dto;

import java.io.Serializable;

/** DTO trung gian — truyền dữ liệu HoaDon qua Socket. */
public class HoaDonDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maHD;
    private String ngay;
    private String khachHang;
    private String sanPham;
    private String tenSanPham;
    private int soLuong;
    private double donGia;
    private double giamGia;
    private double tongTien;
    private String sdt;
    private String loaiKH;
    private String tenKhachHang;

    public HoaDonDTO() {}

    public HoaDonDTO(String maHD, String ngay, String khachHang, String sanPham,
                     String tenSanPham, int soLuong, double donGia, double tongTien) {
        this.maHD = maHD;
        this.ngay = ngay;
        this.khachHang = khachHang;
        this.sanPham = sanPham;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tongTien = tongTien;
    }

    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }

    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }

    public String getKhachHang() { return khachHang; }
    public void setKhachHang(String khachHang) { this.khachHang = khachHang; }

    public String getSanPham() { return sanPham; }
    public void setSanPham(String sanPham) { this.sanPham = sanPham; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getLoaiKH() { return loaiKH; }
    public void setLoaiKH(String loaiKH) { this.loaiKH = loaiKH; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
}
