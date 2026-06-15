package com.example.QUANLYUNGDUNGBANHANG.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class CartItem {
    private final SanPham sanPham;
    private final SimpleIntegerProperty soLuong;
    private final SimpleDoubleProperty tongTien;

    public CartItem(SanPham sanPham, int soLuong) {
        this.sanPham = sanPham;
        this.soLuong = new SimpleIntegerProperty(soLuong);
        this.tongTien = new SimpleDoubleProperty(calculateTotal());

        // Update tongTien automatically when soLuong changes
        this.soLuong.addListener((obs, old, nv) -> this.tongTien.set(calculateTotal()));
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public int getSoLuong() {
        return soLuong.get();
    }

    public void setSoLuong(int value) {
        this.soLuong.set(value);
    }

    public SimpleIntegerProperty soLuongProperty() {
        return soLuong;
    }

    public double getTongTien() {
        return tongTien.get();
    }

    public SimpleDoubleProperty tongTienProperty() {
        return tongTien;
    }

    private double calculateTotal() {
        try {
            double price = Double.parseDouble(sanPham.getGiaNhap().replaceAll("[^\\d.]", ""));
            return price * soLuong.get();
        } catch (Exception e) {
            return 0;
        }
    }
}
