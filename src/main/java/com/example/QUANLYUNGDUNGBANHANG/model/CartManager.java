package com.example.QUANLYUNGDUNGBANHANG.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartManager {
    private static CartManager instance;
    private final ObservableList<CartItem> cartItems;

    private CartManager() {
        cartItems = FXCollections.observableArrayList();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    public void addProduct(SanPham sp) {
        // Check if already in cart
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getSanPham().getMa().equals(sp.getMa())) {
                if (item.getSoLuong() < sp.getSoLuongTon()) {
                    item.setSoLuong(item.getSoLuong() + 1);
                    // Fire observable update so UI refreshes
                    cartItems.set(i, item);
                }
                return;
            }
        }
        // If not in cart, add new
        if (sp.getSoLuongTon() > 0) {
            cartItems.add(new CartItem(sp, 1));
        }
    }

    public void removeProduct(CartItem item) {
        cartItems.remove(item);
    }

    public void clearCart() {
        cartItems.clear();
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTongTien();
        }
        return total;
    }
}
