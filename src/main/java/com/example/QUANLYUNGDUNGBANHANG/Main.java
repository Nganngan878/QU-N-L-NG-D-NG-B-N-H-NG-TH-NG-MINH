
package com.example.QUANLYUNGDUNGBANHANG;

import com.example.QUANLYUNGDUNGBANHANG.view.LoginView;
import com.example.QUANLYUNGDUNGBANHANG.util.DBConnection;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Paths;

public class Main extends Application {
    // Trigger IDE rebuild

    @Override
    public void start(Stage stage) {
        // Khởi tạo database
        DBConnection.setupDatabase();

        // Màn hình đăng nhập
        LoginView loginRoot = new LoginView(stage);
        Scene scene = new Scene(loginRoot, 900, 550);

        // Nạp CSS từ file bên cạnh jar / project root
        try {
            java.net.URI cssUri = Paths.get("style.css").toAbsolutePath().toUri();
            scene.getStylesheets().add(cssUri.toString());
        } catch (Exception ignore) {}

        // Cấu hình Stage
        stage.setTitle("Đăng nhập - Quản Lý Bán Hàng");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
