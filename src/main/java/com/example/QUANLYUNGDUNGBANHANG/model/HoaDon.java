package com.example.QUANLYUNGDUNGBANHANG.model;

public class HoaDon {
    private String maHD;
    private String ngay;
    private String khachHang;
    private String sanPham; 
    private String tenSanPham; 
    private int soLuong;
    private double donGia;
    private double tongTien;
    private String soDienThoai; 
    private String loaiKH; 
    private double giamGia; 
    private String tenKhachHang;

    public HoaDon() {}

    public HoaDon(String khachHang, String maSP, String tenSP, int soLuong, double donGia) {
        this.khachHang = khachHang;
        this.sanPham = maSP;
        this.tenSanPham = tenSP;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public HoaDon(String khachHang, String maSP, int soLuong, double donGia) {
        this.khachHang = khachHang;
        this.sanPham = maSP;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tenSanPham = "Sản phẩm";
    }

    public HoaDon(String maHD, String ngay, String khachHang, String sanPham, int soLuong, double donGia,
                  double tongTien) {
        this.maHD = maHD;
        this.ngay = ngay;
        this.khachHang = khachHang;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tongTien = tongTien;
        this.tenSanPham = "Hóa đơn hệ thống";
    }

    public HoaDon(String maHD, String ngay, String khachHang, String sanPham, String tenSanPham, int soLuong,
                  double donGia, double tongTien) {
        this.maHD = maHD;
        this.ngay = ngay;
        this.khachHang = khachHang;
        this.sanPham = sanPham;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tongTien = tongTien;
    }

    public HoaDon(String maHD, String ngay, String khachHang, String sanPham, String tenSanPham, int soLuong,
                  double donGia, double tongTien, String soDienThoai, String loaiKH) {
        this.maHD = maHD;
        this.ngay = ngay;
        this.khachHang = khachHang;
        this.sanPham = sanPham;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.tongTien = tongTien;
        this.soDienThoai = soDienThoai;
        this.loaiKH = loaiKH;
    }

    public double getGiamGia() { return giamGia; }
    public void setGiamGia(double giamGia) { this.giamGia = giamGia; }

    public String getMaHD() { return maHD; }
    public String getNgay() { return ngay; }
    public String getKhachHang() { return khachHang; }
    public String getSanPham() { return sanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public int getSoLuong() { return soLuong; }
    public double getDonGia() { return donGia; }
    public double getTongTien() { return tongTien; }
    public String getSoDienThoai() { return soDienThoai != null ? soDienThoai : ""; }
    public String getLoaiKH() { return loaiKH != null ? loaiKH : "Thường"; }
    public String getTenKhachHang() { return tenKhachHang != null ? tenKhachHang : "Khách Lẻ"; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public double thanhTien() {
        return (tongTien > 0) ? tongTien : soLuong * donGia;
    }
}
