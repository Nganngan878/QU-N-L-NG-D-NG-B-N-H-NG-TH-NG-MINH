package com.example.QUANLYUNGDUNGBANHANG.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * LoadingDialog — Màn hình chờ hiển thị khi đang thực hiện tác vụ nặng.
 * Dùng để ngăn người dùng thao tác trong khi CRUD / Import XML đang chạy nền.
 *
 * Cách dùng:
 *   LoadingDialog loading = new LoadingDialog(stage, "Đang lưu dữ liệu...");
 *   loading.show();
 *   // ... thực hiện tác vụ trong background thread ...
 *   Platform.runLater(() -> loading.close());
 */
public class LoadingDialog {

    private final Stage dialog;

    public LoadingDialog(Stage owner, String message) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        if (owner != null) dialog.initOwner(owner);

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(52, 52);

        Label msgLabel = new Label(message != null ? message : "Đang xử lý...");
        msgLabel.setStyle(
            "-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: bold;"
        );

        VBox root = new VBox(16, spinner, msgLabel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32, 48, 32, 48));
        root.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0, 0, 8);"
        );

        Scene sc = new Scene(root);
        sc.setFill(Color.TRANSPARENT);
        dialog.setScene(sc);
    }

    /** Hiển thị màn hình chờ */
    public void show() {
        dialog.show();
    }

    /** Đóng màn hình chờ (gọi từ Platform.runLater) */
    public void close() {
        dialog.close();
    }
}
