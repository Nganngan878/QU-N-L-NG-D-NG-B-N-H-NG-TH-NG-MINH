
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
        // Nạp phông chữ Inter
        try {
            javafx.scene.text.Font.loadFont(Paths.get("fonts/Inter-Regular.ttf").toUri().toURL().toExternalForm(), 13);
            javafx.scene.text.Font.loadFont(Paths.get("fonts/Inter-Medium.ttf").toUri().toURL().toExternalForm(), 13);
            javafx.scene.text.Font.loadFont(Paths.get("fonts/Inter-Bold.ttf").toUri().toURL().toExternalForm(), 13);
            javafx.scene.text.Font.loadFont(Paths.get("fonts/Inter-SemiBold.ttf").toUri().toURL().toExternalForm(), 13);
        } catch (Exception e) {
            System.err.println("❌ Không thể nạp phông chữ Inter: " + e.getMessage());
        }

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
