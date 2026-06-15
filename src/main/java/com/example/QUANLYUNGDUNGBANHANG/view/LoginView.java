package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.UserController;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.UserDTO;
import com.example.QUANLYUNGDUNGBANHANG.util.AnimationUtil;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView extends HBox {
    private final UserController userController = new UserController();
    private int failCount = 0;
    private static final int MAX_FAIL = 5;
    private static final int LOCK_SECONDS = 30;

    public LoginView(Stage stage) {
        this.setStyle("-fx-background-color: #FFFFFF;");
        this.setPrefSize(900, 550);

        VBox leftPane  = buildLeftPane();
        VBox rightPane = buildRightPane(stage);
        this.getChildren().addAll(leftPane, rightPane);

        FadeTransition ft = new FadeTransition(Duration.millis(500), this);
        ft.setFromValue(0.0); ft.setToValue(1.0); ft.play();
    }

    // ===== CỘT TRÁI: Banner xanh =====
    private VBox buildLeftPane() {
        VBox pane = new VBox(18);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefWidth(380);
        pane.setMinWidth(380);
        pane.setPadding(new Insets(48));
        pane.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #1D4ED8, #2563EB, #3B82F6);"
        );

        Label icon = new Label("🛒");
        icon.setFont(Font.font(68));

        Label title = new Label("QUẢN LÝ");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");

        Label title2 = new Label("BÁN HÀNG");
        title2.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.8);");

        Region sep = new Region();
        sep.setPrefHeight(2); sep.setPrefWidth(50);
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.5); -fx-background-radius: 2;");

        Label sub = new Label("Hệ thống quản lý bán hàng\nchuyên nghiệp & hiện đại");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8); -fx-text-alignment: center;");
        sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        sub.setWrapText(true);

        // Feature badges
        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER);
        for (String t : new String[]{"⚡ Nhanh", "🔒 An toàn", "📊 Thống kê"}) {
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

        // Bounce animation cho icon
        icon.setTranslateY(-8); icon.setOpacity(0);
        javafx.animation.TranslateTransition iconTt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(500), icon);
        iconTt.setFromY(-8); iconTt.setToY(0);
        iconTt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        javafx.animation.FadeTransition iconFt = new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), icon);
        iconFt.setFromValue(0); iconFt.setToValue(1);
        iconTt.play(); iconFt.play();

        // Stagger slide-in cho badges
        int bi = 0;
        for (javafx.scene.Node b : badges.getChildren()) {
            AnimationUtil.fadeSlideIn(b, 350 + bi * 80);
            bi++;
        }

        pane.getChildren().addAll(icon, title, title2, sep, sub, badges);
        return pane;
    }

    // ===== CỘT PHẢI: Form đăng nhập =====
    private VBox buildRightPane(Stage stage) {
        VBox pane = new VBox(0);
        pane.setAlignment(Pos.CENTER);
        pane.setPrefWidth(520);
        pane.setStyle("-fx-background-color: #FFFFFF;");
        pane.setPadding(new Insets(52, 60, 52, 60));

        Label lblTitle = new Label("Chào mừng trở lại 👋");
        lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblHint = new Label("Đăng nhập để tiếp tục quản lý hệ thống");
        lblHint.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        VBox.setMargin(lblHint, new Insets(6, 0, 32, 0));

        // Username
        HBox userBox = new HBox(16);
        userBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblUser = new Label("TÀI KHOẢN");
        lblUser.setPrefWidth(90);
        lblUser.setAlignment(Pos.CENTER_RIGHT);
        lblUser.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Nhập tên đăng nhập...");
        txtUser.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 10 13; -fx-font-size: 13px;"
        );
        txtUser.setPrefWidth(240); // Bóp ô nhập lại
        userBox.getChildren().addAll(lblUser, txtUser);
        VBox.setMargin(userBox, new Insets(16, 0, 16, 0));

        // Password
        HBox passBox = new HBox(16);
        passBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblPass = new Label("MẬT KHẨU");
        lblPass.setPrefWidth(90);
        lblPass.setAlignment(Pos.CENTER_RIGHT);
        lblPass.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Nhập mật khẩu...");
        txtPass.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 10 13; -fx-font-size: 13px;"
        );
        txtPass.setPrefWidth(240); // Bóp ô nhập lại
        passBox.getChildren().addAll(lblPass, txtPass);
        VBox.setMargin(passBox, new Insets(0, 0, 12, 0));

        // Forgot row
        HBox forgotRow = new HBox();
        forgotRow.setAlignment(Pos.CENTER_RIGHT);
        Label btnForgot = new Label("Quên mật khẩu?");
        btnForgot.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 12px; -fx-cursor: hand;");
        forgotRow.getChildren().add(btnForgot);
        VBox.setMargin(forgotRow, new Insets(0, 0, 24, 60)); // Add left padding to align with input

        Label lblStatus = new Label();
        lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        lblStatus.setWrapText(true);
        VBox.setMargin(lblStatus, new Insets(0, 0, 10, 0));

        // Login button
        Button btnLogin = new Button("Đăng nhập");
        btnLogin.setStyle(
            "-fx-background-color: #2563EB; -fx-background-radius: 6; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-cursor: hand;" +
            "-fx-border-color: transparent;"
        );
        btnLogin.setPrefWidth(346); // 90 + 16 + 240
        VBox.setMargin(btnLogin, new Insets(10, 0, 0, 0));

        txtUser.setOnAction(e -> txtPass.requestFocus());
        txtPass.setOnAction(e -> doLogin(txtUser, txtPass, lblStatus, stage));
        btnLogin.setOnAction(e -> doLogin(txtUser, txtPass, lblStatus, stage));

        // Hover + press effect on button
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(
            "-fx-background-color: #1D4ED8; -fx-background-radius: 6; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-cursor: hand;" +
            "-fx-border-color: transparent; -fx-effect: dropshadow(gaussian, rgba(37,99,235,0.35), 12, 0, 0, 3);"
        ));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(
            "-fx-background-color: #2563EB; -fx-background-radius: 6; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-cursor: hand;" +
            "-fx-border-color: transparent;"
        ));
        btnLogin.setOnMousePressed(e -> AnimationUtil.scaleUp(btnLogin, 0.96));
        btnLogin.setOnMouseReleased(e -> AnimationUtil.scaleDown(btnLogin));

        // Sign up row
        HBox signUpRow = new HBox(8);
        signUpRow.setAlignment(Pos.CENTER);
        Label lblNoAccount = new Label("Chưa có tài khoản?");
        lblNoAccount.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        Label btnSignUp = new Label("Đăng ký ngay");
        btnSignUp.setStyle("-fx-text-fill: #2563EB; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSignUp.setOnMouseClicked(ev -> {
            Scene registerScene = new Scene(new RegisterView(stage), 900, 550);
            registerScene.getStylesheets().add("file:///" +
                java.nio.file.Paths.get("style.css").toAbsolutePath().toString().replace("\\", "/"));
            stage.setScene(registerScene);
        });
        signUpRow.getChildren().addAll(lblNoAccount, btnSignUp);
        VBox.setMargin(signUpRow, new Insets(16, 0, 0, 0));

        // Đăng nhập nhanh với Admin
        HBox adminRow = new HBox(8);
        adminRow.setAlignment(Pos.CENTER);
        Label lblOr = new Label("─────  hoặc  ─────");
        lblOr.setStyle("-fx-text-fill: #D1D5DB; -fx-font-size: 11px;");
        VBox.setMargin(adminRow, new Insets(8, 0, 0, 0));
        adminRow.getChildren().add(lblOr);

        Button btnAdminLogin = new Button("🔑  Đăng nhập Admin");
        btnAdminLogin.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #E5E7EB; -fx-border-radius: 6;" +
            "-fx-background-radius: 6; -fx-text-fill: #374151; -fx-font-size: 12px;" +
            "-fx-padding: 7 18; -fx-cursor: hand;"
        );
        btnAdminLogin.setOnMouseEntered(e -> btnAdminLogin.setStyle(
            "-fx-background-color: #F3F4F6; -fx-border-color: #D1D5DB; -fx-border-radius: 6;" +
            "-fx-background-radius: 6; -fx-text-fill: #111827; -fx-font-size: 12px;" +
            "-fx-padding: 7 18; -fx-cursor: hand;"
        ));
        btnAdminLogin.setOnMouseExited(e -> btnAdminLogin.setStyle(
            "-fx-background-color: transparent; -fx-border-color: #E5E7EB; -fx-border-radius: 6;" +
            "-fx-background-radius: 6; -fx-text-fill: #374151; -fx-font-size: 12px;" +
            "-fx-padding: 7 18; -fx-cursor: hand;"
        ));
        btnAdminLogin.setOnAction(e -> showAdminLoginDialog(stage, lblStatus));
        VBox.setMargin(btnAdminLogin, new Insets(4, 0, 0, 0));

        pane.getChildren().addAll(
            lblTitle, lblHint,
            userBox,
            passBox,
            forgotRow, lblStatus,
            btnLogin,
            signUpRow,
            adminRow,
            btnAdminLogin
        );

        // Slide-in + fade animation
        TranslateTransition tt = new TranslateTransition(Duration.millis(450), pane);
        tt.setFromX(40); tt.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(450), pane);
        ft.setFromValue(0); ft.setToValue(1);
        tt.play(); ft.play();

        return pane;
    }

    // ===== LOGIC ĐĂNG NHẬP (có đếm sai + khoá tài khoản) =====
    private void doLogin(TextField txtUser, PasswordField txtPass, Label lblStatus, Stage stage) {
        String username = txtUser.getText().trim();
        String password = txtPass.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("⚠ Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            shakeNode(txtUser.getParent());
            return;
        }

        UserDTO user = userController.login(username, password);
        if (user != null) {
            failCount = 0; // reset bộ đếm
            stage.setResizable(true);
            Scene mainScene = new Scene(new MainView(stage, user.getUsername(), user.getRole()), 1280, 760);
            mainScene.getStylesheets().add("file:///" +
                java.nio.file.Paths.get("style.css").toAbsolutePath().toString().replace("\\", "/"));
            stage.setTitle("🛒 Quản Lý Bán Hàng");
            stage.setScene(mainScene);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.centerOnScreen();
        } else {
            failCount++;
            int remaining = MAX_FAIL - failCount;
            txtPass.clear();
            if (failCount >= MAX_FAIL) {
                // Khoá nút đăng nhập 30 giây
                lockLoginButton(txtUser, txtPass, lblStatus);
            } else {
                lblStatus.setText("❌ Sai tài khoản hoặc mật khẩu! (Còn " + remaining + " lần thử)");
                shakeNode(txtPass.getParent());
            }
        }
    }

    /** Khoá form đăng nhập 30 giây sau khi nhập sai 5 lần */
    private void lockLoginButton(TextField txtUser, PasswordField txtPass, Label lblStatus) {
        // Tìm Button đăng nhập qua scene — dùng lookup theo ID hoặc pass tham chiếu
        // Vì không có tham chiếu trực tiếp, ta disable cả 2 ô nhập
        txtUser.setDisable(true);
        txtPass.setDisable(true);

        final int[] countdown = { LOCK_SECONDS };
        lblStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblStatus.setText("🔒 Quá nhiều lần sai! Thử lại sau " + countdown[0] + " giây...");

        Timeline timer = new Timeline();
        timer.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            countdown[0]--;
            if (countdown[0] > 0) {
                lblStatus.setText("🔒 Quá nhiều lần sai! Thử lại sau " + countdown[0] + " giây...");
            } else {
                // Mở khoá
                txtUser.setDisable(false);
                txtPass.setDisable(false);
                failCount = 0;
                lblStatus.setStyle("-fx-text-fill: #059669; -fx-font-size: 12px; -fx-font-weight: normal;");
                lblStatus.setText("✅ Đã mở khoá. Bạn có thể đăng nhập lại.");
                timer.stop();
            }
        }));
        timer.setCycleCount(LOCK_SECONDS);
        timer.play();
    }

    private void shakeNode(javafx.scene.Node node) {
        AnimationUtil.shake(node);
    }

    private void showAdminLoginDialog(Stage parentStage, Label lblStatus) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);

        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(17, 24, 39, 0.65);"); // Nền tối 65% để làm nổi bật hộp thoại

        VBox root = new VBox(20);
        root.setPadding(new Insets(36, 40, 36, 40));
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 60, 0, 0, 20);" +
            "-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 16; -fx-border-width: 1;"
        );
        root.setMaxSize(380, Region.USE_PREF_SIZE);
        root.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Đăng nhập Admin");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label hint = new Label("Khu vực quản trị chỉ dành cho Admin hệ thống.");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        VBox.setMargin(hint, new Insets(-8, 0, 8, 0));

        TextField dUser = new TextField();
        dUser.setPromptText("Tên đăng nhập Admin...");
        dUser.setStyle(
            "-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB; -fx-border-radius: 8;" +
            "-fx-background-radius: 8; -fx-padding: 12 14; -fx-font-size: 14px;"
        );
        dUser.setMaxWidth(Double.MAX_VALUE);

        PasswordField dPass = new PasswordField();
        dPass.setPromptText("Mật khẩu...");
        dPass.setStyle(
            "-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB; -fx-border-radius: 8;" +
            "-fx-background-radius: 8; -fx-padding: 12 14; -fx-font-size: 14px;"
        );
        dPass.setMaxWidth(Double.MAX_VALUE);

        Label dStatus = new Label();
        dStatus.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");
        dStatus.setWrapText(true);

        HBox btns = new HBox(12);
        btns.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(btns, new Insets(10, 0, 0, 0));

        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;"
        );
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle(
            "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px;" +
            "-fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;"
        ));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;"
        ));
        btnCancel.setOnAction(e -> dialog.close());

        Button btnOk = new Button("Đăng nhập");
        btnOk.setStyle(
            "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.4), 10, 0, 0, 4);"
        );
        btnOk.setOnMouseEntered(e -> btnOk.setStyle(
            "-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(29,78,216,0.5), 14, 0, 0, 5);"
        ));
        btnOk.setOnMouseExited(e -> btnOk.setStyle(
            "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;" +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 24; -fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.4), 10, 0, 0, 4);"
        ));
        btnOk.setOnMousePressed(e -> AnimationUtil.scaleUp(btnOk, 0.95));
        btnOk.setOnMouseReleased(e -> AnimationUtil.scaleDown(btnOk));
        btnOk.setDefaultButton(true);

        Runnable doAdminLogin = () -> {
            String u = dUser.getText().trim();
            String p = dPass.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                dStatus.setText("⚠ Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            UserDTO user = userController.login(u, p);
            if (user == null) {
                dStatus.setText("❌ Sai tài khoản hoặc mật khẩu!");
                dPass.clear();
                return;
            }
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                dStatus.setText("❌ Tài khoản này không có quyền Admin!");
                dPass.clear();
                return;
            }
            dialog.close();
            Scene mainScene = new Scene(new MainView(parentStage, user.getUsername(), user.getRole()), 1280, 760);
            mainScene.getStylesheets().add("file:///" +
                java.nio.file.Paths.get("style.css").toAbsolutePath().toString().replace("\\", "/"));
            parentStage.setTitle("🛒 Quản Lý Bán Hàng");
            parentStage.setScene(mainScene);
            parentStage.setMinWidth(1000);
            parentStage.setMinHeight(650);
            parentStage.centerOnScreen();
        };

        btnOk.setOnAction(e -> doAdminLogin.run());
        dUser.setOnAction(e -> dPass.requestFocus());
        dPass.setOnAction(e -> doAdminLogin.run());

        btns.getChildren().addAll(btnCancel, btnOk);
        root.getChildren().addAll(title, hint, dUser, dPass, dStatus, btns);
        
        backdrop.getChildren().add(root);

        Scene sc = new Scene(backdrop);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);

        // Căn chỉnh dialog phủ kín toàn bộ màn hình cha
        dialog.setX(parentStage.getX());
        dialog.setY(parentStage.getY());
        dialog.setWidth(parentStage.getWidth());
        dialog.setHeight(parentStage.getHeight());

        parentStage.xProperty().addListener((obs, oldV, newV) -> dialog.setX(newV.doubleValue()));
        parentStage.yProperty().addListener((obs, oldV, newV) -> dialog.setY(newV.doubleValue()));
        parentStage.widthProperty().addListener((obs, oldV, newV) -> dialog.setWidth(newV.doubleValue()));
        parentStage.heightProperty().addListener((obs, oldV, newV) -> dialog.setHeight(newV.doubleValue()));

        // Animation: Phóng to & Mờ dần
        root.setScaleX(0.8);
        root.setScaleY(0.8);
        root.setOpacity(0);
        
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(300), root);
        st.setToX(1); st.setToY(1);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(300), root);
        ft.setToValue(1);

        javafx.animation.FadeTransition bgFt = new javafx.animation.FadeTransition(Duration.millis(300), backdrop);
        bgFt.setFromValue(0);
        bgFt.setToValue(1);

        st.play(); ft.play(); bgFt.play();

        dialog.showAndWait();
    }
}
