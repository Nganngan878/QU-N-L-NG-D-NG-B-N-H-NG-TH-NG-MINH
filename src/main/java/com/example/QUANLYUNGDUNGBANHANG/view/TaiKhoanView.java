package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.UserController;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.UserDTO;
import com.example.QUANLYUNGDUNGBANHANG.util.AnimationUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.ImageCache;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class TaiKhoanView extends VBox {

    private final String username;
    private final UserController userController = new UserController();
    private UserDTO profileData;

    private Circle avatarCircle;
    private Label lblName, lblBio, lblGender, lblDob, lblPhone, lblEmail;

    public TaiKhoanView(String username) {
        this.username = username;
        this.setStyle("-fx-background-color: #F3F4F6;"); // Nền xám nhạt giống iOS settings
        this.setSpacing(20);
        this.setPadding(new Insets(30, 40, 30, 40));

        // Tải dữ liệu
        profileData = userController.getProfile(username);
        if (profileData == null) {
            profileData = new UserDTO();
            profileData.setUsername(username);
        }

        // Header Title
        Label title = new Label("Tài khoản của tôi");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // --- Avatar Section ---
        VBox avatarSection = buildAvatarSection();

        // --- Info Cards ---
        VBox card1 = buildInfoCard(
                buildRow("Tên", profileData.getFullname() != null ? profileData.getFullname() : "Chưa cập nhật", true, () -> showEditDialog("Tên", profileData.getFullname(), "fullname")),
                buildRow("Tiểu sử", profileData.getBio() != null ? profileData.getBio() : "Chưa cập nhật", true, () -> showEditDialog("Tiểu sử", profileData.getBio(), "bio"))
        );

        VBox card2 = buildInfoCard(
                buildRow("Giới tính", profileData.getGender() != null ? profileData.getGender() : "Chưa cập nhật", true, () -> showComboDialog("Giới tính", profileData.getGender(), "gender", "Nam", "Nữ", "Khác")),
                buildRow("Ngày sinh", profileData.getDob() != null ? profileData.getDob() : "Chưa cập nhật", true, () -> showEditDialog("Ngày sinh (dd/MM/yyyy)", profileData.getDob(), "dob")),
                buildRow("Thông tin cá nhân", "", true, null) // Nút ảo
        );

        VBox card3 = buildInfoCard(
                buildRow("Số điện thoại", profileData.getPhone() != null ? profileData.getPhone() : "Chưa cập nhật", true, () -> showEditDialog("Số điện thoại", profileData.getPhone(), "phone")),
                buildRow("Email", profileData.getEmail() != null ? profileData.getEmail() : "Chưa cập nhật", true, () -> showEditDialog("Email", profileData.getEmail(), "email"))
        );

        ScrollPane scroll = new ScrollPane();
        VBox content = new VBox(24, title, avatarSection, card1, card2, card3);
        content.setPadding(new Insets(0, 20, 40, 0));
        content.setStyle("-fx-background-color: transparent;");
        scroll.setContent(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #F3F4F6; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox.setVgrow(scroll, Priority.ALWAYS);
        this.getChildren().add(scroll);

        AnimationUtil.fadeSlideIn(this, 0);
    }

    private VBox buildAvatarSection() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);

        avatarCircle = new Circle(50);
        avatarCircle.setStyle("-fx-fill: #E5E7EB; -fx-stroke: #D1D5DB; -fx-stroke-width: 2;");
        loadAvatarImage();

        Button btnChangeAvatar = new Button("Thay đổi ảnh đại diện");
        btnChangeAvatar.setStyle("-fx-background-color: transparent; -fx-text-fill: #2563EB; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        btnChangeAvatar.setOnAction(e -> handleAvatarChange());

        box.getChildren().addAll(avatarCircle, btnChangeAvatar);
        return box;
    }

    private void loadAvatarImage() {
        if (profileData.getAvatarUrl() != null && !profileData.getAvatarUrl().isBlank()) {
            try {
                String url = profileData.getAvatarUrl();
                Image img;
                if (url.startsWith("http")) {
                    img = ImageCache.get(url);
                } else {
                    img = new Image(new File(url).toURI().toString(), true);
                }
                if (img != null) {
                    img.progressProperty().addListener((obs, old, newv) -> {
                        if (newv.doubleValue() >= 1.0 && !img.isError()) avatarCircle.setFill(new ImagePattern(img));
                    });
                    if (!img.isError() && img.getWidth() > 0) {
                        avatarCircle.setFill(new ImagePattern(img));
                    }
                }
            } catch (Exception ignored) {}
        } else {
            avatarCircle.setFill(javafx.scene.paint.Color.valueOf("#E5E7EB"));
        }
    }

    private void handleAvatarChange() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh đại diện");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(this.getScene().getWindow());
        if (file != null) {
            profileData.setAvatarUrl(file.getAbsolutePath());
            if (userController.updateProfile(profileData)) {
                loadAvatarImage();
                showAlert("Thành công", "Đã cập nhật ảnh đại diện!");
            } else {
                showAlert("Lỗi", "Không thể lưu ảnh đại diện.");
            }
        }
    }

    private VBox buildInfoCard(Node... rows) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        
        for (int i = 0; i < rows.length; i++) {
            card.getChildren().add(rows[i]);
            if (i < rows.length - 1) {
                Region div = new Region();
                div.setStyle("-fx-background-color: #F3F4F6;");
                div.setPrefHeight(1);
                VBox.setMargin(div, new Insets(0, 16, 0, 16));
                card.getChildren().add(div);
            }
        }
        return card;
    }

    private HBox buildRow(String label, String valueText, boolean hasArrow, Runnable onClickAction) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(16));
        row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #374151;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label val = new Label(valueText);
        val.setStyle("-fx-font-size: 15px; -fx-text-fill: #6B7280;");
        
        // Gán Label vào biến để update sau này
        switch (label) {
            case "Tên": lblName = val; break;
            case "Tiểu sử": lblBio = val; break;
            case "Giới tính": lblGender = val; break;
            case "Ngày sinh": lblDob = val; break;
            case "Số điện thoại": lblPhone = val; break;
            case "Email": lblEmail = val; break;
        }

        row.getChildren().addAll(lbl, spacer, val);

        if (hasArrow) {
            Label arrow = new Label("›");
            arrow.setStyle("-fx-font-size: 20px; -fx-text-fill: #D1D5DB; -fx-padding: 0 0 0 8;");
            row.getChildren().add(arrow);
        }

        if (onClickAction != null) {
            row.setOnMouseClicked(e -> onClickAction.run());
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F9FAFB; -fx-cursor: hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));
        }

        return row;
    }

    private void showEditDialog(String title, String currentValue, String fieldName) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);");
        root.setPrefWidth(320);

        Label lblTitle = new Label("Chỉnh sửa " + title);
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField txt = new TextField(currentValue != null ? currentValue : "");
        txt.setStyle("-fx-font-size: 14px; -fx-padding: 8; -fx-background-radius: 6; -fx-border-color: #D1D5DB; -fx-border-radius: 6;");

        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnSave = new Button("Lưu");
        btnSave.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            updateProfileField(fieldName, txt.getText().trim());
            dialog.close();
        });

        btns.getChildren().addAll(btnCancel, btnSave);
        root.getChildren().addAll(lblTitle, txt, btns);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    private void showComboDialog(String title, String currentValue, String fieldName, String... options) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);");
        root.setPrefWidth(320);

        Label lblTitle = new Label("Chỉnh sửa " + title);
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(options);
        combo.setValue(currentValue != null ? currentValue : options[0]);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setStyle("-fx-font-size: 14px; -fx-padding: 4; -fx-background-radius: 6;");

        HBox btns = new HBox(8);
        btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnSave = new Button("Lưu");
        btnSave.setStyle("-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            updateProfileField(fieldName, combo.getValue());
            dialog.close();
        });

        btns.getChildren().addAll(btnCancel, btnSave);
        root.getChildren().addAll(lblTitle, combo, btns);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    private void updateProfileField(String field, String value) {
        switch (field) {
            case "fullname": profileData.setFullname(value); if (lblName != null) lblName.setText(value); break;
            case "bio": profileData.setBio(value); if (lblBio != null) lblBio.setText(value); break;
            case "gender": profileData.setGender(value); if (lblGender != null) lblGender.setText(value); break;
            case "dob": profileData.setDob(value); if (lblDob != null) lblDob.setText(value); break;
            case "phone": profileData.setPhone(value); if (lblPhone != null) lblPhone.setText(value); break;
            case "email": profileData.setEmail(value); if (lblEmail != null) lblEmail.setText(value); break;
        }
        boolean ok = userController.updateProfile(profileData);
        if (!ok) {
            showAlert("Lỗi", "Không thể cập nhật hồ sơ trên Server!");
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
