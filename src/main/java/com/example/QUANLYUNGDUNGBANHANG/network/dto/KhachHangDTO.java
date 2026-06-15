package com.example.QUANLYUNGDUNGBANHANG.network.dto;

import java.io.Serializable;

/** DTO trung gian — truyền dữ liệu KhachHang qua Socket. */
public class KhachHangDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maKH;
    private String tenKH;
    private String ngaySinh;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String loaiKH;

    public KhachHangDTO() {}

    public KhachHangDTO(String maKH, String tenKH, String ngaySinh, String soDienThoai,
                        String email, String diaChi, String loaiKH) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.loaiKH = loaiKH;
    }

    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }

    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }

    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getLoaiKH() { return loaiKH; }
    public void setLoaiKH(String loaiKH) { this.loaiKH = loaiKH; }
}
