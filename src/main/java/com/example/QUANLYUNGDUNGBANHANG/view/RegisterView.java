package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.UserController;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class RegisterView extends HBox {
    private final UserController userController = new UserController();

    public RegisterView(Stage stage) {
        this.setStyle("-fx-background-color: #FFFFFF;");
        this.setPrefSize(900, 550);

        VBox leftPane  = buildLeftPane();
        VBox rightPane = buildRightPane(stage);
        this.getChildren().addAll(leftPane, rightPane);

        FadeTransition ft = new FadeTransition(Duration.millis(500), this);
        ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
    }

    // ===== CỘT TRÁI: Banner xanh (giữ nguyên phong cách của LoginView) =====
    private VBox buildLeftPane() {
        VBox pane = new VBox(18);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefWidth(380);
        pane.setMinWidth(380);
        pane.setPadding(new Insets(48));
        pane.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1D4ED8, #2563EB, #3B82F6);"
        );

        Label icon = new Label("🚀");
        icon.setFont(Font.font(68));

        Label title = new Label("QUẢN LÝ");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label title2 = new Label("BÁN HÀNG");
        title2.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.8);");

        Region sep = new Region();
        sep.setPrefHeight(2); sep.setPrefWidth(50);
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 2;");

        Label sub = new Label("Đăng ký tài khoản mới\nTrở thành thành viên hệ thống");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8); -fx-text-alignment: center;");
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        sub.setWrapText(true);

        // Feature badges
        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER);
        for (String t : new String[]{"⚡ Nhanh chóng", "🔒 Bảo mật", "🤝 Thân thiện"}) {
            Label b = new Label(t);
            b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 5 12;" +
                "-fx-font-size: 11px;"
            );
            badges.getChildren().add(b);
        }

        pane.getChildren().addAll(icon, title, title2, sep, sub, badges);
        return pane;
    }

    // ===== CỘT PHẢI: Form đăng ký =====
    private VBox buildRightPane(Stage stage) {
        VBox pane = new VBox(0);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefWidth(520);
        pane.setStyle("-fx-background-color: #FFFFFF;");
        pane.setPadding(new Insets(30, 60, 30, 60));

        Label lblTitle = new Label("Đăng ký tài khoản ✨");
        lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblHint = new Label("Nhập thông tin bên dưới để tạo tài khoản mới");
        lblHint.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        VBox.setMargin(lblHint, new Insets(6, 0, 24, 0));

        // Username field row
        HBox userBox = new HBox(16);
        userBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblUser = new Label("TÀI KHOẢN");
        lblUser.setPrefWidth(100);
        lblUser.setAlignment(Pos.CENTER_RIGHT);
        lblUser.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Nhập tên đăng nhập mới...");
        txtUser.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 8 13; -fx-font-size: 13px;"
        );
        txtUser.setPrefWidth(240);
        userBox.getChildren().addAll(lblUser, txtUser);
        VBox.setMargin(userBox, new Insets(10, 0, 12, 0));

        // Password field row
        HBox passBox = new HBox(16);
        passBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblPass = new Label("MẬT KHẨU");
        lblPass.setPrefWidth(100);
        lblPass.setAlignment(Pos.CENTER_RIGHT);
        lblPass.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Nhập mật khẩu...");
        txtPass.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 8 13; -fx-font-size: 13px;"
        );
        txtPass.setPrefWidth(240);
        passBox.getChildren().addAll(lblPass, txtPass);
        VBox.setMargin(passBox, new Insets(0, 0, 12, 0));

        // Confirm Password field row
        HBox confirmBox = new HBox(16);
        confirmBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblConfirm = new Label("XÁC NHẬN");
        lblConfirm.setPrefWidth(100);
        lblConfirm.setAlignment(Pos.CENTER_RIGHT);
        lblConfirm.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        PasswordField txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Nhập lại mật khẩu...");
        txtConfirm.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 8 13; -fx-font-size: 13px;"
        );
        txtConfirm.setPrefWidth(240);
        confirmBox.getChildren().addAll(lblConfirm, txtConfirm);
        VBox.setMargin(confirmBox, new Insets(0, 0, 16, 0));

        // Status Label
        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        lblStatus.setWrapText(true);
        VBox.setMargin(lblStatus, new Insets(0, 0, 12, 0));

        // Register Button
        Button btnRegister = new Button("Đăng ký");
        btnRegister.setStyle(
            "-fx-background-color: #2563EB; -fx-background-radius: 6; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-cursor: hand;" +
            "-fx-border-color: transparent;"
        );
        btnRegister.setPrefWidth(356); // 100 + 16 + 240
        VBox.setMargin(btnRegister, new Insets(6, 0, 0, 0));

        // Hover effect for Register Button
        btnRegister.setOnMouseEntered(e -> btnRegister.setStyle(
            "-fx-background-color: #1D4ED8; -fx-background-radius: 6; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-cursor: hand;" +
            "-fx-border-color: transparent; -fx-effect: dropshadow(gaussian, rgba(37,99,235,0.35), 12, 0, 0, 3);"
        ));
        btnRegister.setOnMouseExited(e -> btnRegister.setStyle(
            "-fx-background-color: #2563EB; -fx-background-radius: 6; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-cursor: hand;" +
            "-fx-border-color: transparent;"
        ));

        // Event handler for Enter keys
        txtUser.setOnAction(e -> txtPass.requestFocus());
        txtPass.setOnAction(e -> txtConfirm.requestFocus());
        txtConfirm.setOnAction(e -> doRegister(txtUser, txtPass, txtConfirm, lblStatus, stage));
        btnRegister.setOnAction(e -> doRegister(txtUser, txtPass, txtConfirm, lblStatus, stage));

        // Back to Login Link
        HBox loginRow = new HBox(8);
        loginRow.setAlignment(Pos.CENTER);
        Label lblHasAccount = new Label("Đã có tài khoản?");
        lblHasAccount.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        Label btnBack = new Label("Đăng nhập ngay");
        btnBack.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnBack.setOnMouseClicked(e -> navigateToLogin(stage));
        loginRow.getChildren().addAll(lblHasAccount, btnBack);
        VBox.setMargin(loginRow, new Insets(16, 0, 0, 0));

        pane.getChildren().addAll(
            lblTitle, lblHint,
            userBox,
            passBox,
            confirmBox,
            lblStatus,
            btnRegister,
            loginRow
        );

        // Slide-in Animation
        TranslateTransition tt = new TranslateTransition(Duration.millis(450), pane);
        tt.setFromX(40); tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(450), pane);
        ft.setFromValue(0); ft.setToValue(1);
        tt.play(); ft.play();

        return pane;
    }

    private void doRegister(TextField txtUser, PasswordField txtPass, PasswordField txtConfirm, Label lblStatus, Stage stage) {
        String username = txtUser.getText().trim();
        String password = txtPass.getText().trim();
        String confirm = txtConfirm.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            lblStatus.setText("⚠ Vui lòng nhập đầy đủ tất cả các trường!");
            shakeNode(txtUser.getParent());
            return;
        }

        if (!password.equals(confirm)) {
            lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            lblStatus.setText("⚠ Mật khẩu xác nhận không trùng khớp!");
            shakeNode(txtConfirm.getParent());
            return;
        }

        if (password.length() < 4) {
            lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            lblStatus.setText("⚠ Mật khẩu phải có độ dài từ 4 ký tự trở lên!");
            shakeNode(txtPass.getParent());
            return;
        }

        // Gửi yêu cầu qua UserController
        Response res = userController.register(username, password);
        if (res != null && res.isSuccess()) {
            lblStatus.setStyle("-fx-text-fill: #059669; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblStatus.setText("🎉 " + res.getMessage());
            
            // Chuyển hướng sau 1.5 giây
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> navigateToLogin(stage));
            pause.play();
        } else {
            lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
            lblStatus.setText("❌ " + (res != null ? res.getMessage() : "Lỗi kết nối Server!"));
            shakeNode(txtUser.getParent());
        }
    }

    private void navigateToLogin(Stage stage) {
        Scene loginScene = new Scene(new LoginView(stage), 900, 550);
        String cssPath = java.nio.file.Paths.get("style.css").toAbsolutePath().toString().replace("\\", "/");
        loginScene.getStylesheets().add("file:///" + cssPath);
        stage.setScene(loginScene);
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0); tt.setByX(8);
        tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.play();
    }
}
