module com.example.QUANLYUNGDUNGBANHANG {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;                // Để dùng SQL
    requires java.xml;                // Để dùng DOM Parser cho XML Export/Import
    requires org.postgresql.jdbc;     // Để dùng thư viện PostgreSQL

    opens com.example.QUANLYUNGDUNGBANHANG to javafx.fxml;
    exports com.example.QUANLYUNGDUNGBANHANG;

    // Xuất các package cho JavaFX
    exports com.example.QUANLYUNGDUNGBANHANG.model;
    exports com.example.QUANLYUNGDUNGBANHANG.controller;
    exports com.example.QUANLYUNGDUNGBANHANG.view;
    exports com.example.QUANLYUNGDUNGBANHANG.util;
    exports com.example.QUANLYUNGDUNGBANHANG.dao;
    exports com.example.QUANLYUNGDUNGBANHANG.dao.impl;

    // Package mới: Network (Socket Client/Server)
    exports com.example.QUANLYUNGDUNGBANHANG.network;
    exports com.example.QUANLYUNGDUNGBANHANG.network.dto;
    exports com.example.QUANLYUNGDUNGBANHANG.server;
}