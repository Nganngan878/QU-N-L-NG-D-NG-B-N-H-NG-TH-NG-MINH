package com.example.QUANLYUNGDUNGBANHANG.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SanPham {
    private final SimpleStringProperty ma;
    private final SimpleStringProperty ten;
    private final SimpleStringProperty loai;
    private final SimpleStringProperty giaNhap;
    private final SimpleIntegerProperty soLuongTon;
    private final SimpleStringProperty hinhAnh;

    public SanPham(String ma, String ten, String loai, String giaNhap, int soLuongTon) {
        this(ma, ten, loai, giaNhap, soLuongTon, null);
    }

    public SanPham(String ma, String ten, String loai, String giaNhap, int soLuongTon, String hinhAnh) {
        this.ma = new SimpleStringProperty(ma);
        this.ten = new SimpleStringProperty(ten);
        this.loai = new SimpleStringProperty(loai);
        this.giaNhap = new SimpleStringProperty(giaNhap);
        this.soLuongTon = new SimpleIntegerProperty(soLuongTon);
        this.hinhAnh = new SimpleStringProperty(hinhAnh == null ? "" : hinhAnh);
    }

    public String getMa() { return ma.get(); }
    public void setMa(String value) { ma.set(value); }

    public String getTen() { return ten.get(); }
    public void setTen(String value) { ten.set(value); }

    public String getLoai() { return loai.get(); }
    public void setLoai(String value) { loai.set(value); }

    public String getGiaNhap() { return giaNhap.get(); }
    public void setGiaNhap(String value) { giaNhap.set(value); }

    public int getSoLuongTon() { return soLuongTon.get(); }
    public void setSoLuongTon(int value) { soLuongTon.set(value); }

    public String getHinhAnh() { return hinhAnh.get(); }
    public void setHinhAnh(String value) { hinhAnh.set(value); }

    public SimpleStringProperty maProperty() { return ma; }
    public SimpleStringProperty tenProperty() { return ten; }
    public SimpleStringProperty loaiProperty() { return loai; }
    public SimpleStringProperty giaNhapProperty() { return giaNhap; }
    public SimpleIntegerProperty soLuongTonProperty() { return soLuongTon; }
    public SimpleStringProperty hinhAnhProperty() { return hinhAnh; }
}
