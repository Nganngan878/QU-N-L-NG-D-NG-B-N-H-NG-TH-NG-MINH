package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.KhachHangController;
import com.example.QUANLYUNGDUNGBANHANG.model.KhachHang;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class KhachHangView extends VBox {
    private final KhachHangController controller = new KhachHangController();
    private final TableView<KhachHang> table = new TableView<>();
    private final ObservableList<KhachHang> dataList = FXCollections.observableArrayList();
    private FilteredList<KhachHang> filteredList;
    private final String role;

    /** Constructor mặc định — tương thích với code cũ */
    public KhachHangView() {
        this("ADMIN");
    }

    /** Constructor có role */
    public KhachHangView(String role) {
        this.role = role != null ? role : "USER";
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #EFF6FF;");
        this.getChildren().addAll(buildTopBar(), buildTableCard());
        VBox.setVgrow(this.getChildren().get(1), Priority.ALWAYS);
        loadData();

        FadeTransition ft = new FadeTransition(Duration.millis(280), this);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ===== TOP BAR =====
    private VBox buildTopBar() {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        // ---- Hàng 1: Tiêu đề ----
        VBox titleBox = new VBox(2);
        Label title = new Label("👥  Khách Hàng");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label sub = new Label("Quản lý thông tin toàn bộ khách hàng");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        titleBox.getChildren().addAll(title, sub);

        HBox row1 = new HBox(titleBox);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.setPadding(new Insets(20, 20, 10, 20));
        row1.setStyle("-fx-background-color: #EFF6FF;");

        // ---- Hàng 2: Search + Các nút action ----
        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm tên, SĐT, email...");
        searchField.setPrefWidth(240);
        searchField.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-radius: 20;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 7 14; -fx-font-size: 13px;"
        );

        Button btnRefresh = buildBtn("↻  Làm Mới", false);
        btnRefresh.setOnAction(e -> loadData());

        Button btnExport = buildBtn("📤 Xuất XML", false);
        btnExport.setOnAction(e -> handleExportXml());

        Button btnImport = buildBtn("📥 Nhập XML", false);
        btnImport.setOnAction(e -> handleImportXml());
        btnImport.setVisible(isAdmin);
        btnImport.setManaged(isAdmin);

        Button btnAdd = buildBtn("＋  Thêm KH", true);
        btnAdd.setOnAction(e -> showFormDialog(null));
        btnAdd.setVisible(isAdmin);
        btnAdd.setManaged(isAdmin);

        Region rowSpacer = new Region();
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);

        HBox row2 = new HBox(8, searchField, rowSpacer, btnRefresh, btnExport, btnImport, btnAdd);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.setPadding(new Insets(0, 20, 14, 20));
        row2.setStyle("-fx-background-color: #EFF6FF;");

        searchField.textProperty().addListener((obs, old, nv) -> {
            if (filteredList == null) return;
            filteredList.setPredicate(kh -> {
                if (nv == null || nv.isEmpty()) return true;
                String lower = nv.toLowerCase();
                return kh.getMaKH().toLowerCase().contains(lower)
                    || kh.getTenKH().toLowerCase().contains(lower)
                    || kh.getSoDienThoai().toLowerCase().contains(lower)
                    || kh.getEmail().toLowerCase().contains(lower);
            });
        });

        VBox bar = new VBox(0, row1, row2);
        bar.setStyle("-fx-background-color: #EFF6FF;");
        return bar;
    }

    // ===== EXPORT XML =====
    private void handleExportXml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Xuất danh sách khách hàng ra XML");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        fc.setInitialFileName("khachhang.xml");
        File file = fc.showSaveDialog(this.getScene() != null ? this.getScene().getWindow() : null);
        if (file == null) return;

        Stage ownerStage = (Stage) (this.getScene() != null ? this.getScene().getWindow() : null);
        LoadingDialog loading = new LoadingDialog(ownerStage, "Đang thực hiện xuất XML phía Server...");
        loading.show();

        Thread t = new Thread(() -> {
            boolean success = controller.exportToXml(file.getAbsolutePath());
            javafx.application.Platform.runLater(() -> {
                loading.close();
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "✅ Xuất XML thành công!\nFile lưu tại: " + file.getAbsolutePath()) {{ setHeaderText(null); show(); }};
                } else {
                    new Alert(Alert.AlertType.ERROR, "❌ Xuất XML thất bại phía Server.") {{ setHeaderText(null); show(); }};
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ===== IMPORT XML =====
    private void handleImportXml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Nhập danh sách khách hàng từ XML");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fc.showOpenDialog(this.getScene() != null ? this.getScene().getWindow() : null);
        if (file == null) return;

        try {
            String xmlContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Stage ownerStage = (Stage) (this.getScene() != null ? this.getScene().getWindow() : null);
            LoadingDialog loading = new LoadingDialog(ownerStage, "Đang gửi dữ liệu và xử lý import trên Server...");
            loading.show();

            Thread t = new Thread(() -> {
                Response res = controller.importFromXml(xmlContent);
                javafx.application.Platform.runLater(() -> {
                    loading.close();
                    if (res.isSuccess()) {
                        loadData();
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Integer> resMap = (java.util.Map<String, Integer>) res.getData();
                        int ok = resMap != null ? resMap.getOrDefault("ok", 0) : 0;
                        int skip = resMap != null ? resMap.getOrDefault("skip", 0) : 0;
                        new Alert(Alert.AlertType.INFORMATION, "✅ Nhập XML hoàn tất!\nThêm mới thành công: " + ok + " | Bỏ qua (mã trùng): " + skip) {{ setHeaderText(null); show(); }};
                    } else {
                        new Alert(Alert.AlertType.ERROR, "❌ Nhập XML thất bại: " + res.getMessage()) {{ setHeaderText(null); show(); }};
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "❌ Lỗi khi đọc file XML: " + ex.getMessage()) {{ setHeaderText(null); show(); }};
        }
    }

    // ===== TABLE CARD =====
    private VBox buildTableCard() {
        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 12;" +
            "-fx-border-color: #E5E7EB; -fx-border-radius: 12; -fx-border-width: 1;"
        );
        VBox.setMargin(card, new Insets(0, 20, 20, 20));
        VBox.setVgrow(card, Priority.ALWAYS);

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        card.getChildren().add(table);

        filteredList = new FilteredList<>(dataList, p -> true);
        table.setItems(filteredList);

        return card;
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Chưa có khách hàng nào"));

        TableColumn<KhachHang, String> colMa     = col("MÃ KH",          "maKH",        110);
        TableColumn<KhachHang, String> colTen    = col("TÊN KHÁCH HÀNG", "tenKH",       200);
        TableColumn<KhachHang, String> colSDT    = col("SĐT",            "soDienThoai", 130);
        TableColumn<KhachHang, String> colEmail  = col("EMAIL",           "email",       180);
        TableColumn<KhachHang, String> colDiaChi = col("ĐỊA CHỈ",        "diaChi",      160);

        // VIP badge
        TableColumn<KhachHang, String> colLoai = new TableColumn<>("LOẠI KH");
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loaiKH"));
        colLoai.setPrefWidth(100);
        colLoai.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                boolean vip = "VIP".equalsIgnoreCase(item);
                badge.setStyle(vip
                    ? "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #FCD34D; -fx-border-radius: 20; -fx-border-width: 1;"
                    : "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; -fx-background-radius: 20; -fx-padding: 3 10; -fx-font-size: 11px; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-border-width: 1;"
                );
                setGraphic(badge); setText(null);
            }
        });

        // Actions (chỉ hiển thị khi ADMIN)
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        TableColumn<KhachHang, Void> colActions = new TableColumn<>("THAO TÁC");
        colActions.setPrefWidth(140);
        colActions.setVisible(isAdmin);
        colActions.setCellFactory(c -> new TableCell<>() {
            final Button btnEdit   = makeRowBtn("✏ Sửa",  true);
            final Button btnDelete = makeRowBtn("🗑 Xóa", false);
            final HBox box = new HBox(6, btnEdit, btnDelete);
            {
                box.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> showFormDialog(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> {
                    KhachHang kh = getTableView().getItems().get(getIndex());
                    if (showConfirm("Xóa khách hàng '" + kh.getTenKH() + "'?"))
                        if (controller.deleteKhachHang(kh.getMaKH())) loadData();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(colMa, colTen, colSDT, colEmail, colDiaChi, colLoai, colActions);
    }

    private <T> TableColumn<KhachHang, T> col(String header, String prop, double width) {
        TableColumn<KhachHang, T> col = new TableColumn<>(header);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private Button makeRowBtn(String text, boolean isEdit) {
        Button btn = new Button(text);
        btn.setStyle(isEdit
            ? "-fx-background-color: #EFF6FF; -fx-background-radius: 5; -fx-text-fill: #2563EB; -fx-font-size: 11px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-color: #BFDBFE; -fx-border-radius: 5; -fx-border-width: 1;"
            : "-fx-background-color: #FEF2F2; -fx-background-radius: 5; -fx-text-fill: #DC2626; -fx-font-size: 11px; -fx-padding: 4 10; -fx-cursor: hand; -fx-border-color: #FECACA; -fx-border-radius: 5; -fx-border-width: 1;"
        );
        return btn;
    }

    // ===== DIALOG (giữ nguyên logic) =====
    private void showFormDialog(KhachHang existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        boolean isEdit = existing != null;

        VBox root = new VBox(14);
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 8);"
        );
        root.setPadding(new Insets(24));
        root.setPrefWidth(430);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label dlgTitle = new Label(isEdit ? "✏  Sửa Khách Hàng" : "＋  Thêm Khách Hàng");
        dlgTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 16; -fx-cursor: hand; -fx-border-color: transparent;");
        closeBtn.setOnAction(e -> dialog.close());
        titleRow.getChildren().addAll(dlgTitle, sp, closeBtn);

        TextField txtMa      = buildField("Mã KH",              isEdit ? existing.getMaKH() : "");
        TextField txtTen     = buildField("Tên khách hàng",     isEdit ? existing.getTenKH() : "");
        TextField txtSDT     = buildField("Số điện thoại",      isEdit ? existing.getSoDienThoai() : "");
        TextField txtEmail   = buildField("Email",               isEdit ? existing.getEmail() : "");
        TextField txtDiaChi  = buildField("Địa chỉ",            isEdit ? existing.getDiaChi() : "");
        TextField txtNgay    = buildField("Ngày sinh dd/MM/yyyy", isEdit ? existing.getNgaySinh() : "");

        ComboBox<String> cmbLoai = new ComboBox<>();
        cmbLoai.getItems().addAll("Thường", "VIP");
        cmbLoai.setValue(isEdit ? existing.getLoaiKH() : "Thường");
        cmbLoai.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-padding: 4 8;"
        );
        cmbLoai.setMaxWidth(Double.MAX_VALUE);

        if (isEdit) txtMa.setDisable(true);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        Button btnSave   = buildBtn(isEdit ? "💾  Lưu" : "✅  Thêm Mới", true);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        Button btnCancel = buildBtn("Hủy", false);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            // === Validation ===
            String maKH   = txtMa.getText().trim();
            String tenKH  = txtTen.getText().trim();
            String sdt    = txtSDT.getText().trim();
            String email  = txtEmail.getText().trim();
            String ngay   = txtNgay.getText().trim();

            if (maKH.isEmpty())  { lblError.setText("⚠ Mã Khách Hàng không được để trống!"); return; }
            if (tenKH.isEmpty()) { lblError.setText("⚠ Tên Khách Hàng không được để trống!"); return; }

            // Kiểm tra định dạng SĐT: 10-11 chữ số
            if (!sdt.isEmpty() && !sdt.matches("\\d{10,11}")) {
                lblError.setText("⚠ Số điện thoại phải gồm 10 hoặc 11 chữ số!");
                return;
            }

            // Kiểm tra định dạng Email
            if (!email.isEmpty() && !email.matches("^[\\w.+\\-]+@[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}$")) {
                lblError.setText("⚠ Email không đúng định dạng (VD: test@gmail.com)!");
                return;
            }

            // Kiểm tra định dạng Ngày sinh dd/MM/yyyy
            if (!ngay.isEmpty()) {
                try {
                    java.time.format.DateTimeFormatter fmt =
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    java.time.LocalDate.parse(ngay, fmt);
                } catch (Exception ex) {
                    lblError.setText("⚠ Ngày sinh không hợp lệ (phải đúng định dạng dd/MM/yyyy)!");
                    return;
                }
            }

            // Kiểm tra trùng mã KH khi thêm mới
            if (!isEdit) {
                boolean trung = dataList.stream().anyMatch(k -> k.getMaKH().equalsIgnoreCase(maKH));
                if (trung) {
                    lblError.setText("⚠ Mã Khách Hàng " + maKH + " đã tồn tại!");
                    return;
                }
            }

            // Chạy lưu trên Thread nền với LoadingDialog
            KhachHang kh = new KhachHang(
                maKH, tenKH,
                ngay,
                sdt,
                email,
                txtDiaChi.getText().trim(),
                cmbLoai.getValue()
            );
            Stage ownerStage = (Stage) dialog.getScene().getWindow();
            LoadingDialog loading = new LoadingDialog(ownerStage, isEdit ? "Đang cập nhật..." : "Đang thêm mới...");
            dialog.close();
            loading.show();

            Thread t = new Thread(() -> {
                boolean ok = isEdit ? controller.updateKhachHang(kh) : controller.addKhachHang(kh);
                javafx.application.Platform.runLater(() -> {
                    loading.close();
                    if (ok) loadData();
                    else {
                        Alert a = new Alert(Alert.AlertType.ERROR,
                            isEdit ? "Cập nhật thất bại!" : "Thêm mới thất bại!");
                        a.setHeaderText(null); a.show();
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        root.getChildren().addAll(
            titleRow, new Separator(),
            formRow("Mã KH", txtMa),
            formRow("Tên KH", txtTen),
            formRow("SĐT", txtSDT),
            formRow("Email", txtEmail),
            formRow("Địa Chỉ", txtDiaChi),
            formRow("Ngày Sinh", txtNgay),
            formRowCombo("Loại KH", cmbLoai),
            lblError,
            new HBox(10, btnCancel, btnSave)
        );

        Scene sc = new Scene(root);
        sc.getStylesheets().add("file:///" +
            java.nio.file.Paths.get("style.css").toAbsolutePath().toString().replace("\\", "/"));
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    private VBox formRow(String lbl, TextField field) {
        Label l = new Label(lbl.toUpperCase());
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        field.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 9 12; -fx-font-size: 13px;"
        );
        field.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, l, field);
    }

    private VBox formRowCombo(String lbl, ComboBox<String> combo) {
        Label l = new Label(lbl.toUpperCase());
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        return new VBox(4, l, combo);
    }

    private TextField buildField(String prompt, String value) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        return tf;
    }

    private Button buildBtn(String text, boolean primary) {
        Button btn = new Button(text);
        btn.setMinWidth(Region.USE_PREF_SIZE); // Khóa không cho co cụm làm mất chữ
        btn.setStyle(primary
            ? "-fx-background-color: #2563EB; -fx-background-radius: 6; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 9 18; -fx-cursor: hand; -fx-border-color: transparent;"
            : "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-text-fill: #374151; -fx-font-size: 13px; -fx-padding: 9 18; -fx-cursor: hand; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-border-width: 1;"
        );
        return btn;
    }

    private boolean showConfirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận"); alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            java.util.List<KhachHang> result;
            try { result = controller.getAllKhachHang(); }
            catch (Exception e) { result = java.util.Collections.emptyList(); }
            final java.util.List<KhachHang> data = result;
            javafx.application.Platform.runLater(() -> {
                dataList.clear();
                dataList.addAll(data);
                if (filteredList != null) filteredList.setPredicate(p -> true);
            });
        });
        t.setDaemon(true);
        t.start();
    }
}
