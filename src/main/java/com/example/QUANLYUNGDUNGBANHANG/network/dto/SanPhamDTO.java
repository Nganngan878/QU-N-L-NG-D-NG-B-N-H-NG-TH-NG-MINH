package com.example.QUANLYUNGDUNGBANHANG.network.dto;

import java.io.Serializable;

/** DTO trung gian — truyền dữ liệu SanPham qua Socket (không dùng JavaFX Properties). */
public class SanPhamDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ma;
    private String ten;
    private String loai;
    private String giaNhap;
    private int soLuongTon;
    private String hinhAnh;

    public SanPhamDTO() {}

    public SanPhamDTO(String ma, String ten, String loai, String giaNhap, int soLuongTon, String hinhAnh) {
        this.ma = ma;
        this.ten = ten;
        this.loai = loai;
        this.giaNhap = giaNhap;
        this.soLuongTon = soLuongTon;
        this.hinhAnh = hinhAnh;
    }

    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public String getGiaNhap() { return giaNhap; }
    public void setGiaNhap(String giaNhap) { this.giaNhap = giaNhap; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
}
