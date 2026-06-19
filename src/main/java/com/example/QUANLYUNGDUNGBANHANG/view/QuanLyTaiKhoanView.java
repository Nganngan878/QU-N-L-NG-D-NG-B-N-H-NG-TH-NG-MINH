package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.UserController;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.UserDTO;
import com.example.QUANLYUNGDUNGBANHANG.util.AnimationUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.List;

/**
 * QuanLyTaiKhoanView — Trang quản lý tài khoản người dùng (Admin only).
 * Hiển thị danh sách toàn bộ tài khoản với chức năng:
 *  - Xem thông tin (username, họ tên, email, role)
 *  - Thay đổi role (USER ↔ ADMIN)
 *  - Đặt lại mật khẩu
 *  - Xóa tài khoản
 */
public class QuanLyTaiKhoanView extends VBox {

    private final UserController userController = new UserController();
    private final ObservableList<UserDTO> dataList = FXCollections.observableArrayList();
    private FilteredList<UserDTO> filteredList;

    // Table
    private TableView<UserDTO> table;

    public QuanLyTaiKhoanView() {
        this.setStyle("-fx-background-color: #EFF6FF;");
        this.setSpacing(0);

        this.getChildren().addAll(buildTopBar(), buildTableArea());
        VBox.setVgrow(this.getChildren().get(1), Priority.ALWAYS);

        loadData();

        FadeTransition ft = new FadeTransition(Duration.millis(280), this);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ===== TOP BAR =====
    private VBox buildTopBar() {
        // Title row
        Label title = new Label("👤  Quản Lý Tài Khoản");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label sub = new Label("Quản lý tài khoản người dùng hệ thống");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        VBox titleBox = new VBox(2, title, sub);

        HBox row1 = new HBox(titleBox);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.setPadding(new Insets(20, 20, 10, 20));
        row1.setStyle("-fx-background-color: #EFF6FF;");

        // Search + action buttons
        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm theo username, họ tên, email...");
        searchField.setPrefWidth(280);
        searchField.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-radius: 20;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 7 14; -fx-font-size: 13px;"
        );

        Button btnRefresh = buildBtn("↻  Làm Mới", false);
        btnRefresh.setOnAction(e -> loadData());

        Button btnAdd = buildBtn("＋  Thêm TK", true);
        btnAdd.setOnAction(e -> showAddUserDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row2 = new HBox(8, searchField, spacer, btnRefresh, btnAdd);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.setPadding(new Insets(0, 20, 14, 20));
        row2.setStyle("-fx-background-color: #EFF6FF;");

        // Search logic
        searchField.textProperty().addListener((obs, old, nv) -> {
            if (filteredList == null) return;
            String lower = nv == null ? "" : nv.toLowerCase().trim();
            filteredList.setPredicate(u -> {
                if (lower.isEmpty()) return true;
                return (u.getUsername() != null && u.getUsername().toLowerCase().contains(lower))
                    || (u.getFullname() != null && u.getFullname().toLowerCase().contains(lower))
                    || (u.getEmail() != null && u.getEmail().toLowerCase().contains(lower))
                    || (u.getRole() != null && u.getRole().toLowerCase().contains(lower));
            });
        });

        VBox bar = new VBox(0, row1, row2);
        bar.setStyle("-fx-background-color: #EFF6FF;");
        return bar;
    }

    // ===== TABLE =====
    @SuppressWarnings("unchecked")
    private ScrollPane buildTableArea() {
        table = new TableView<>();
        table.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
            "-fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-border-width: 1;"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Chưa có dữ liệu tài khoản"));
        table.setFixedCellSize(52);

        // --- Cột STT ---
        TableColumn<UserDTO, Number> colSTT = new TableColumn<>("#");
        colSTT.setMinWidth(50); colSTT.setMaxWidth(60);
        colSTT.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleIntegerProperty(
                table.getItems().indexOf(cd.getValue()) + 1));
        colSTT.setCellFactory(col -> {
            TableCell<UserDTO, Number> cell = new TableCell<>() {
                @Override protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); }
                    else {
                        setText(item.toString());
                        setStyle("-fx-alignment: center; -fx-text-fill: #6B7280; -fx-font-size: 13px;");
                    }
                }
            };
            return cell;
        });

        // --- Cột Username ---
        TableColumn<UserDTO, String> colUsername = new TableColumn<>("Username");
        colUsername.setMinWidth(140);
        colUsername.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(cd.getValue().getUsername()));
        colUsername.setCellFactory(col -> styledCell(true));

        // --- Cột Họ tên ---
        TableColumn<UserDTO, String> colFullname = new TableColumn<>("Họ & Tên");
        colFullname.setMinWidth(160);
        colFullname.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
            cd.getValue().getFullname() != null && !cd.getValue().getFullname().isEmpty()
                ? cd.getValue().getFullname() : "—"));
        colFullname.setCellFactory(col -> styledCell(false));

        // --- Cột Email ---
        TableColumn<UserDTO, String> colEmail = new TableColumn<>("Email");
        colEmail.setMinWidth(180);
        colEmail.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
            cd.getValue().getEmail() != null && !cd.getValue().getEmail().isEmpty()
                ? cd.getValue().getEmail() : "—"));
        colEmail.setCellFactory(col -> styledCell(false));

        // --- Cột Role ---
        TableColumn<UserDTO, String> colRole = new TableColumn<>("Vai trò");
        colRole.setMinWidth(100); colRole.setMaxWidth(130);
        colRole.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(cd.getValue().getRole()));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setText(null); setGraphic(null); }
                else {
                    boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
                    Label badge = new Label(isAdmin ? "🔑 ADMIN" : "👤 USER");
                    badge.setStyle(isAdmin
                        ? "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;"
                        : "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setText(null);
                    setStyle("-fx-alignment: center;");
                }
            }
        });

        // --- Cột Hành động ---
        TableColumn<UserDTO, Void> colAction = new TableColumn<>("Thao tác");
        colAction.setMinWidth(230); colAction.setMaxWidth(260);
        colAction.setCellFactory(col -> new TableCell<>() {
            final Button btnRole  = buildSmallBtn("🔄 Đổi Role", "#6366F1", "#FFFFFF");
            final Button btnPwd   = buildSmallBtn("🔑 Đặt lại MK", "#059669", "#FFFFFF");
            final Button btnDel   = buildSmallBtn("🗑 Xóa", "#DC2626", "#FFFFFF");

            {
                btnRole.setOnAction(e -> {
                    UserDTO u = getTableView().getItems().get(getIndex());
                    if (u != null) showChangeRoleDialog(u);
                });
                btnPwd.setOnAction(e -> {
                    UserDTO u = getTableView().getItems().get(getIndex());
                    if (u != null) showResetPasswordDialog(u);
                });
                btnDel.setOnAction(e -> {
                    UserDTO u = getTableView().getItems().get(getIndex());
                    if (u != null) handleDelete(u);
                });
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); }
                else {
                    HBox box = new HBox(6, btnRole, btnPwd, btnDel);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.setPadding(new Insets(0, 8, 0, 8));
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(colSTT, colUsername, colFullname, colEmail, colRole, colAction);

        // Striped rows
        table.setRowFactory(tv -> {
            TableRow<UserDTO> row = new TableRow<>();
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #EFF6FF;"));
            row.setOnMouseExited(e -> row.setStyle(""));
            return row;
        });

        ScrollPane scroll = new ScrollPane(table);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: #EFF6FF; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setPadding(new Insets(0, 20, 20, 20));
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private <T> TableCell<UserDTO, T> styledCell(boolean bold) {
        return new TableCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else {
                    setText(item.toString());
                    setStyle("-fx-font-size: 13px; -fx-text-fill: #111827;" +
                             (bold ? " -fx-font-weight: bold;" : "") +
                             " -fx-padding: 0 8;");
                }
            }
        };
    }

    // ===== LOAD DATA =====
    private void loadData() {
        Thread t = new Thread(() -> {
            List<UserDTO> users;
            try { users = userController.getAllUsers(); }
            catch (Exception e) { users = java.util.Collections.emptyList(); }
            final List<UserDTO> data = users;
            Platform.runLater(() -> {
                dataList.clear();
                dataList.addAll(data);
                filteredList = new FilteredList<>(dataList, p -> true);
                table.setItems(filteredList);
                // Stagger animation
                int i = 0;
                for (javafx.scene.Node node : table.lookupAll(".table-row-cell")) {
                    AnimationUtil.fadeSlideIn(node, i * 30.0);
                    i++;
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ===== THÊM TÀI KHOẢN =====
    private void showAddUserDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0, 0, 8);"
        );
        root.setPrefWidth(380);
        applyBounceIn(root);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label dlgTitle = new Label("＋  Thêm tài khoản mới");
        dlgTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Region hsp = new Region(); HBox.setHgrow(hsp, Priority.ALWAYS);
        Button closeBtn = buildCloseBtn(dialog);
        header.getChildren().addAll(dlgTitle, hsp, closeBtn);

        TextField txtUsername = buildField("Tên đăng nhập *", "");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Mật khẩu *");
        txtPassword.setStyle(fieldStyle());
        txtPassword.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbRole = new ComboBox<>();
        cbRole.getItems().addAll("USER", "ADMIN");
        cbRole.setValue("USER");
        cbRole.setMaxWidth(Double.MAX_VALUE);
        cbRole.setStyle("-fx-font-size: 13px; -fx-background-radius: 6; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-padding: 4 8;");

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        Button btnSave = buildBtn("✅  Tạo tài khoản", true);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        Button btnCancel = buildBtn("Hủy", false);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            String role = cbRole.getValue();

            if (username.isEmpty()) { lblError.setText("⚠ Username không được để trống!"); return; }
            if (!username.matches("[A-Za-z0-9_.@-]+")) { lblError.setText("⚠ Username chứa ký tự không hợp lệ!"); return; }
            if (password.length() < 4) { lblError.setText("⚠ Mật khẩu tối thiểu 4 ký tự!"); return; }

            LoadingDialog loading = new LoadingDialog((Stage) dialog.getScene().getWindow(), "Đang tạo tài khoản...");
            dialog.close();
            loading.show();

            Thread t = new Thread(() -> {
                // Dùng REGISTER rồi update role nếu cần
                com.example.QUANLYUNGDUNGBANHANG.network.Response res =
                    userController.register(username, password);
                boolean ok = res.isSuccess();
                if (ok && "ADMIN".equals(role)) {
                    userController.updateUserRole(username, "ADMIN");
                }
                Platform.runLater(() -> {
                    loading.close();
                    if (ok) { loadData(); showInfo("✅ Tạo tài khoản '" + username + "' thành công!"); }
                    else showError("❌ " + res.getMessage());
                });
            });
            t.setDaemon(true); t.start();
        });

        root.getChildren().addAll(
            header, new Separator(),
            formRow("USERNAME", txtUsername),
            formRow("MẬT KHẨU", txtPassword),
            formRow("VAI TRÒ", cbRole),
            lblError,
            new HBox(10, btnCancel, btnSave)
        );

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ===== ĐỔI ROLE =====
    private void showChangeRoleDialog(UserDTO user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0, 0, 8);"
        );
        root.setPrefWidth(340);
        applyBounceIn(root);

        Label titleLbl = new Label("🔄  Đổi vai trò — @" + user.getUsername());
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label curRoleLbl = new Label("Vai trò hiện tại: " + ("ADMIN".equalsIgnoreCase(user.getRole()) ? "🔑 ADMIN" : "👤 USER"));
        curRoleLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        ComboBox<String> cbNewRole = new ComboBox<>();
        cbNewRole.getItems().addAll("USER", "ADMIN");
        cbNewRole.setValue("ADMIN".equalsIgnoreCase(user.getRole()) ? "USER" : "ADMIN");
        cbNewRole.setMaxWidth(Double.MAX_VALUE);
        cbNewRole.setStyle("-fx-font-size: 13px; -fx-background-radius: 6; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-padding: 4 8;");

        Button btnConfirm = buildBtn("✅  Xác nhận đổi", true);
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        Button btnCancel = buildBtn("Hủy", false);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setOnAction(e -> dialog.close());

        btnConfirm.setOnAction(e -> {
            String newRole = cbNewRole.getValue();
            LoadingDialog loading = new LoadingDialog((Stage) dialog.getScene().getWindow(), "Đang cập nhật...");
            dialog.close();
            loading.show();
            Thread t = new Thread(() -> {
                Response res = userController.updateUserRole(user.getUsername(), newRole);
                Platform.runLater(() -> {
                    loading.close();
                    if (res.isSuccess()) { loadData(); showInfo("✅ Đã đổi vai trò thành " + newRole + "!"); }
                    else showError("❌ " + res.getMessage());
                });
            });
            t.setDaemon(true); t.start();
        });

        root.getChildren().addAll(titleLbl, curRoleLbl, new Separator(),
            formRow("VAI TRÒ MỚI", cbNewRole),
            new HBox(10, btnCancel, btnConfirm));

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ===== ĐẶT LẠI MẬT KHẨU =====
    private void showResetPasswordDialog(UserDTO user) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0, 0, 8);"
        );
        root.setPrefWidth(340);
        applyBounceIn(root);

        Label titleLbl = new Label("🔑  Đặt lại mật khẩu — @" + user.getUsername());
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-wrap-text: true;");
        titleLbl.setWrapText(true);

        PasswordField txtNewPwd = new PasswordField();
        txtNewPwd.setPromptText("Mật khẩu mới (tối thiểu 4 ký tự)");
        txtNewPwd.setStyle(fieldStyle());
        txtNewPwd.setMaxWidth(Double.MAX_VALUE);

        PasswordField txtConfirmPwd = new PasswordField();
        txtConfirmPwd.setPromptText("Xác nhận mật khẩu mới");
        txtConfirmPwd.setStyle(fieldStyle());
        txtConfirmPwd.setMaxWidth(Double.MAX_VALUE);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        Button btnConfirm = buildBtn("✅  Đặt lại", true);
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        Button btnCancel = buildBtn("Hủy", false);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setOnAction(e -> dialog.close());

        btnConfirm.setOnAction(e -> {
            String pwd1 = txtNewPwd.getText().trim();
            String pwd2 = txtConfirmPwd.getText().trim();
            if (pwd1.length() < 4) { lblError.setText("⚠ Mật khẩu tối thiểu 4 ký tự!"); return; }
            if (!pwd1.equals(pwd2))  { lblError.setText("⚠ Mật khẩu xác nhận không khớp!"); return; }

            LoadingDialog loading = new LoadingDialog((Stage) dialog.getScene().getWindow(), "Đang đặt lại mật khẩu...");
            dialog.close();
            loading.show();
            Thread t = new Thread(() -> {
                Response res = userController.resetPassword(user.getUsername(), pwd1);
                Platform.runLater(() -> {
                    loading.close();
                    if (res.isSuccess()) showInfo("✅ Đặt lại mật khẩu thành công!");
                    else showError("❌ " + res.getMessage());
                });
            });
            t.setDaemon(true); t.start();
        });

        root.getChildren().addAll(titleLbl, new Separator(),
            formRow("MẬT KHẨU MỚI", txtNewPwd),
            formRow("XÁC NHẬN MK", txtConfirmPwd),
            lblError,
            new HBox(10, btnCancel, btnConfirm));

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ===== XÓA TÀI KHOẢN =====
    private void handleDelete(UserDTO user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Xóa tài khoản '@" + user.getUsername() + "'?\nHành động này không thể hoàn tác!",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa tài khoản");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                Thread t = new Thread(() -> {
                    Response res = userController.deleteUser(user.getUsername());
                    Platform.runLater(() -> {
                        if (res.isSuccess()) { loadData(); showInfo("✅ Đã xóa tài khoản '@" + user.getUsername() + "'!"); }
                        else showError("❌ " + res.getMessage());
                    });
                });
                t.setDaemon(true); t.start();
            }
        });
    }

    // ===== HELPER UI =====
    private Button buildBtn(String text, boolean primary) {
        Button btn = new Button(text);
        btn.setMinWidth(Region.USE_PREF_SIZE);
        if (primary) {
            btn.setStyle(
                "-fx-background-color: #2563EB; -fx-background-radius: 6; -fx-text-fill: white;" +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 9 18;" +
                "-fx-cursor: hand; -fx-border-color: transparent;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-text-fill: #374151;" +
                "-fx-font-size: 13px; -fx-padding: 9 18; -fx-cursor: hand;" +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-border-width: 1;"
            );
        }
        return btn;
    }

    private Button buildSmallBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-font-size: 11px;" +
            "-fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand;" +
            "-fx-border-color: transparent;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    private Button buildCloseBtn(Stage dialog) {
        Button btn = new Button("✕");
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 16; -fx-cursor: hand; -fx-border-color: transparent;");
        btn.setOnAction(e -> dialog.close());
        return btn;
    }

    private TextField buildField(String prompt, String value) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setStyle(fieldStyle());
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private String fieldStyle() {
        return "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
               "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
               "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 9 12; -fx-font-size: 13px;";
    }

    private VBox formRow(String labelText, javafx.scene.control.Control field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        if (field instanceof Region) ((Region) field).setMaxWidth(Double.MAX_VALUE);
        return new VBox(5, lbl, field);
    }

    private void applyBounceIn(VBox root) {
        root.setScaleX(0.88); root.setScaleY(0.88); root.setOpacity(0);
        root.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) {
                javafx.animation.ScaleTransition st =
                    new javafx.animation.ScaleTransition(Duration.millis(260), root);
                st.setToX(1.0); st.setToY(1.0);
                st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                javafx.animation.FadeTransition ft =
                    new javafx.animation.FadeTransition(Duration.millis(200), root);
                ft.setFromValue(0); ft.setToValue(1);
                new javafx.animation.ParallelTransition(st, ft).play();
            }
        });
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null); a.setTitle("Thông báo"); a.show();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(null); a.setTitle("Lỗi"); a.show();
    }
}
