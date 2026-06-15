package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.SanPhamController;
import com.example.QUANLYUNGDUNGBANHANG.model.CartManager;
import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.util.AnimationUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.ImageCache;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SanPhamView extends VBox {
    private final SanPhamController controller = new SanPhamController();
    private final ObservableList<SanPham> dataList = FXCollections.observableArrayList();
    private FilteredList<SanPham> filteredList;
    private final FlowPane productGrid = new FlowPane();
    private String activeFilter = "Tất cả";
    private final String role; // Role của người dùng hiện tại

    /** Constructor mặc định — tương thích với code cũ (mặc định ADMIN) */
    public SanPhamView() {
        this("ADMIN");
    }

    /** Constructor có role — kiểm soát hiển thị nút CRUD theo quyền */
    public SanPhamView(String role) {
        this.role = role != null ? role : "USER";
        this.setStyle("-fx-background-color: #EFF6FF;");
        this.setSpacing(0);

        // Top bar (title + buttons + search)
        this.getChildren().addAll(buildTopBar(), buildFilterBar(), buildGridArea());
        VBox.setVgrow(this.getChildren().get(2), Priority.ALWAYS);
        loadData();

        FadeTransition ft = new FadeTransition(Duration.millis(280), this);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ===== TOP BAR =====
    private VBox buildTopBar() {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        // ---- Hàng 1: Tiêu đề ----
        VBox titleBox = new VBox(2);
        Label title = new Label("📦  Sản Phẩm");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label sub = new Label("Quản lý danh mục sản phẩm");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        titleBox.getChildren().addAll(title, sub);

        HBox row1 = new HBox(titleBox);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.setPadding(new Insets(20, 20, 10, 20));
        row1.setStyle("-fx-background-color: #EFF6FF;");

        // ---- Hàng 2: Search + Các nút action ----
        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm tên, mã, loại...");
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

        Button btnEdit = buildBtn("✏  Sửa SP", false);
        btnEdit.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Sửa Sản Phẩm");
            dialog.setHeaderText("Nhập Mã Sản Phẩm cần sửa:");
            dialog.setContentText("Mã SP:");
            dialog.showAndWait().ifPresent(ma -> {
                SanPham spToEdit = dataList.stream().filter(s -> s.getMa().equalsIgnoreCase(ma)).findFirst().orElse(null);
                if (spToEdit != null) {
                    showFormDialog(spToEdit);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Không tìm thấy sản phẩm với mã: " + ma);
                    alert.setHeaderText(null);
                    alert.show();
                }
            });
        });
        btnEdit.setVisible(isAdmin);
        btnEdit.setManaged(isAdmin);

        Button btnAdd = buildBtn("＋  Thêm SP", true);
        btnAdd.setOnAction(e -> showFormDialog(null));
        btnAdd.setVisible(isAdmin);
        btnAdd.setManaged(isAdmin);

        Region rowSpacer = new Region();
        HBox.setHgrow(rowSpacer, Priority.ALWAYS);

        HBox row2 = new HBox(8, searchField, rowSpacer, btnRefresh, btnExport, btnImport, btnEdit, btnAdd);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.setPadding(new Insets(0, 20, 14, 20));
        row2.setStyle("-fx-background-color: #EFF6FF;");

        // Search filter logic
        searchField.textProperty().addListener((obs, old, nv) -> {
            if (filteredList == null) return;
            filteredList.setPredicate(sp -> {
                if ((nv == null || nv.isEmpty()) && "Tất cả".equals(activeFilter)) return true;
                String lower = nv == null ? "" : nv.toLowerCase();
                boolean matchSearch = nv == null || nv.isEmpty()
                    || sp.getMa().toLowerCase().contains(lower)
                    || sp.getTen().toLowerCase().contains(lower)
                    || sp.getLoai().toLowerCase().contains(lower);
                boolean matchFilter = "Tất cả".equals(activeFilter)
                    || sp.getLoai().equalsIgnoreCase(activeFilter);
                return matchSearch && matchFilter;
            });
            refreshGrid();
        });

        VBox bar = new VBox(0, row1, row2);
        bar.setStyle("-fx-background-color: #EFF6FF;");
        return bar;
    }

    // ===== EXPORT XML =====
    private void handleExportXml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Xuất danh sách sản phẩm ra XML");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        fc.setInitialFileName("sanpham.xml");
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
                    showInfo("✅ Xuất XML thành công!\nFile lưu tại: " + file.getAbsolutePath());
                } else {
                    showError("❌ Xuất XML thất bại phía Server.");
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ===== IMPORT XML =====
    private void handleImportXml() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Nhập danh sách sản phẩm từ XML");
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
                        showInfo("✅ Nhập XML hoàn tất!\nThêm mới thành công: " + ok + " | Bỏ qua (mã trùng): " + skip);
                    } else {
                        showError("❌ Nhập XML thất bại: " + res.getMessage());
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        } catch (Exception ex) {
            showError("❌ Lỗi khi đọc file XML: " + ex.getMessage());
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setHeaderText(null); alert.setTitle("Thông báo"); alert.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText(null); alert.setTitle("Lỗi"); alert.show();
    }

    // ===== FILTER BAR =====
    private HBox buildFilterBar() {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 20, 12, 20));
        bar.setStyle("-fx-background-color: #EFF6FF;");

        String[] filters = {"Tất cả", "Đồ uống", "Đồ ăn", "Trái cây", "Quần áo", "Giày dép", "Dụng cụ", "Mỹ phẩm", "Khác"};
        Button[] btns = new Button[filters.length];

        for (int i = 0; i < filters.length; i++) {
            final String f = filters[i];
            Button btn = new Button(f);
            btns[i] = btn;
            btn.setStyle(i == 0 ? activeFilterStyle() : inactiveFilterStyle());
            btn.setOnAction(e -> {
                activeFilter = f;
                for (Button b : btns) b.setStyle(inactiveFilterStyle());
                btn.setStyle(activeFilterStyle());
                applyFilter();
            });
            bar.getChildren().add(btn);
        }
        return bar;
    }

    private void applyFilter() {
        if (filteredList == null) return;
        filteredList.setPredicate(sp ->
            "Tất cả".equals(activeFilter) || sp.getLoai().equalsIgnoreCase(activeFilter)
        );
        refreshGrid();
    }

    // ===== PRODUCT GRID =====
    private ScrollPane buildGridArea() {
        productGrid.setHgap(14);
        productGrid.setVgap(14);
        productGrid.setPadding(new Insets(4, 20, 20, 20));
        productGrid.setStyle("-fx-background-color: #EFF6FF;");

        ScrollPane scroll = new ScrollPane(productGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background-color: #EFF6FF; -fx-background: #EFF6FF;" +
            "-fx-border-color: transparent;"
        );
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    private void refreshGrid() {
        productGrid.getChildren().clear();
        List<SanPham> items = filteredList != null ? filteredList : dataList;
        int i = 0;
        // Giới hạn stagger tối đa 15ms/card và cap ở 300ms tổng
        for (SanPham sp : items) {
            VBox card = buildProductCard(sp);
            productGrid.getChildren().add(card);
            double delay = Math.min(i * 15, 300);
            AnimationUtil.fadeSlideIn(card, delay);
            i++;
        }
    }

    private VBox buildProductCard(SanPham sp) {
        // Icon or Image
        Node imageNode;
        String hinhAnhUrl = sp.getHinhAnh();
        if (hinhAnhUrl != null && !hinhAnhUrl.trim().isEmpty()) {
            try {
                Image img = ImageCache.get(hinhAnhUrl); // dùng cache

                StackPane imgContainer = new StackPane();
                imgContainer.setPrefSize(136, 110);
                imgContainer.setMaxSize(136, 110);

                Label fallbackIcon = new Label(getIcon(sp.getLoai()));
                fallbackIcon.setStyle("-fx-font-size: 34px;");
                fallbackIcon.setAlignment(Pos.CENTER);
                imgContainer.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12;");
                imgContainer.getChildren().add(fallbackIcon);

                Rectangle rect = new Rectangle(136, 110);
                rect.setArcWidth(20); rect.setArcHeight(20);
                rect.setVisible(false);

                if (img != null) {
                    // Nếu ảnh đã cached và sẵn sàng
                    if (!img.isError() && img.getWidth() > 0) {
                        rect.setFill(new ImagePattern(img));
                        rect.setVisible(true);
                        fallbackIcon.setVisible(false);
                    } else {
                        // Ảnh đang tải nền
                        img.progressProperty().addListener((obs, oldV, newV) -> {
                            if (newV.doubleValue() >= 1.0 && !img.isError()) {
                                rect.setFill(new ImagePattern(img));
                                rect.setVisible(true);
                                fallbackIcon.setVisible(false);
                            }
                        });
                        img.errorProperty().addListener((obs, old, isErr) -> {
                            if (isErr) { rect.setVisible(false); fallbackIcon.setVisible(true); }
                        });
                    }
                }

                imgContainer.getChildren().add(rect);
                imageNode = imgContainer;
            } catch (Exception e) {
                imageNode = makeFallbackPane(sp.getLoai(), 136, 110, 34);
            }
        } else {
            imageNode = makeFallbackPane(sp.getLoai(), 136, 110, 34);
        }

        Label tenLbl = new Label(sp.getTen());
        tenLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-wrap-text: true;");
        tenLbl.setWrapText(true);
        tenLbl.setMaxWidth(140);

        Label maLbl = new Label(sp.getMa());
        maLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");

        Label giaLbl = new Label(formatMoney(sp.getGiaNhap()));
        giaLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");

        // Stock badge
        Label slBadge = new Label("Kho: " + sp.getSoLuongTon());
        slBadge.setStyle(sp.getSoLuongTon() > 10
            ? "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 10px;"
            : "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 10px;"
        );

        HBox bottomRow = new HBox(6, giaLbl, new Region(), slBadge);
        HBox.setHgrow(bottomRow.getChildren().get(1), Priority.ALWAYS);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, imageNode, tenLbl, maLbl, bottomRow);
        card.setPadding(new Insets(12));
        card.setPrefWidth(160);
        card.setMaxWidth(160);
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
            "-fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );

        // Hover effect — dùng ScaleTransition để mượt hơn CSS scale
        card.setOnMouseEntered(e -> {
            AnimationUtil.scaleUp(card, 1.04);
            card.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
                "-fx-border-color: #2563EB; -fx-border-radius: 10; -fx-border-width: 1.5;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.18), 14, 0, 0, 4);"
            );
        });
        card.setOnMouseExited(e -> {
            AnimationUtil.scaleDown(card);
            card.setStyle(
                "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
                "-fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-border-width: 1;" +
                "-fx-cursor: hand;"
            );
        });
        // Chỉ mở chi tiết khi click đơn (click count == 1), không bắn khi click đúp
        card.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && e.getClickCount() == 1) {
                showProductDetailsDialog(sp);
            }
        });

        // Context menu
        ContextMenu cm = new ContextMenu();
        MenuItem miEdit   = new MenuItem("✏  Sửa");
        MenuItem miDelete = new MenuItem("🗑  Xóa");
        miEdit.setOnAction(e -> showFormDialog(sp));
        miDelete.setOnAction(e -> {
            if (showConfirm("Xóa sản phẩm '" + sp.getTen() + "'?")) {
                if (controller.deleteSanPham(sp.getMa())) loadData();
            }
        });
        cm.getItems().addAll(miEdit, miDelete);
        card.setOnContextMenuRequested(e -> cm.show(card, e.getScreenX(), e.getScreenY()));

        return card;
    }

    /** Tạo StackPane fallback khi không có ảnh hoặc ảnh lỗi */
    private StackPane makeFallbackPane(String loai, double w, double h, double fontSize) {
        StackPane pane = new StackPane();
        pane.setPrefSize(w, h);
        pane.setMaxSize(w, h);
        pane.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12;");
        Label icon = new Label(getIcon(loai));
        icon.setStyle("-fx-font-size: " + (int) fontSize + "px;");
        icon.setAlignment(Pos.CENTER);
        pane.getChildren().add(icon);
        return pane;
    }

    private String getIcon(String loai) {
        if (loai == null) return "📦";
        return switch (loai.toLowerCase()) {
            case "đồ uống", "do uong", "nuoc" -> "🥤";
            case "trái cây", "trai cay"       -> "🍎";
            case "đồ ăn", "do an", "thuc an"  -> "🍱";
            case "bánh kẹo", "banh keo"       -> "🍬";
            case "quần áo", "quan ao", "thời trang" -> "👕";
            case "giày dép", "giay dep", "giày" -> "👟";
            case "dụng cụ", "dung cu", "cong cu" -> "🛠️";
            case "mỹ phẩm", "my pham", "làm đẹp" -> "💄";
            default -> "📦";
        };
    }

    private String formatMoney(String gia) {
        try {
            double d = Double.parseDouble(gia.replaceAll("[^\\d.]", ""));
            return NumberFormat.getInstance(new Locale("vi", "VN")).format((long) d) + " ₫";
        } catch (Exception e) { return gia + " ₫"; }
    }

    // ===== DIALOG THÊM/SỬA (giữ nguyên logic) =====
    private void showFormDialog(SanPham existing) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        boolean isEdit = existing != null;

        VBox root = new VBox(12);
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 8);"
        );
        root.setPadding(new Insets(20));
        root.setPrefWidth(420);
        // Bounce-in animation cho dialog
        root.setScaleX(0.88); root.setScaleY(0.88); root.setOpacity(0);
        root.sceneProperty().addListener((obs, o, sc) -> {
            if (sc != null) {
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(260), root);
                st.setToX(1.0); st.setToY(1.0);
                st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), root);
                ft.setFromValue(0); ft.setToValue(1);
                new javafx.animation.ParallelTransition(st, ft).play();
            }
        });

        // Title row
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label dlgTitle = new Label(isEdit ? "✏  Sửa Sản Phẩm" : "＋  Thêm Sản Phẩm");
        dlgTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 16; -fx-cursor: hand; -fx-border-color: transparent;");
        closeBtn.setOnAction(e -> dialog.close());
        titleRow.getChildren().addAll(dlgTitle, sp, closeBtn);

        // Fields (logic giữ nguyên)
        TextField txtMa   = buildField("Mã Sản Phẩm",    isEdit ? existing.getMa() : "");
        TextField txtTen  = buildField("Tên Sản Phẩm",   isEdit ? existing.getTen() : "");
        ComboBox<String> cbLoai = buildCombo("Chọn loại sản phẩm", isEdit ? existing.getLoai() : null);
        TextField txtGia  = buildField("Giá Nhập (VD: 50000)",  isEdit ? existing.getGiaNhap() : "");
        TextField txtSL   = buildField("Số Lượng Tồn",   isEdit ? String.valueOf(existing.getSoLuongTon()) : "");
        TextField txtHinhAnh = buildField("URL Hình Ảnh (http://...)", isEdit ? existing.getHinhAnh() : "");

        if (isEdit) txtMa.setDisable(true);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        Button btnSave   = buildBtn(isEdit ? "💾  Lưu Thay Đổi" : "✅  Thêm Mới", true);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        Button btnCancel = buildBtn("Hủy", false);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setOnAction(e -> dialog.close());

        HBox btnRow = new HBox(10, btnCancel, btnSave);
        btnSave.setOnAction(e -> {
            // === Validation ===
            String ma   = txtMa.getText().trim();
            String ten  = txtTen.getText().trim();
            String gia  = txtGia.getText().trim();
            String sl   = txtSL.getText().trim();
            String loai = cbLoai.getValue() != null ? cbLoai.getValue() : "Khác";

            if (ma.isEmpty()) { lblError.setText("⚠ Mã sản phẩm không được để trống!"); return; }
            if (!ma.matches("[A-Za-z0-9_-]+")) {
                lblError.setText("⚠ Mã SP chỉ được chứa chữ, số, '_' hoặc '-'!");
                return;
            }
            if (ten.isEmpty()) { lblError.setText("⚠ Tên sản phẩm không được để trống!"); return; }

            double giaVal;
            try {
                giaVal = Double.parseDouble(gia.replaceAll("[^\\d.]", ""));
                if (giaVal <= 0) {
                    lblError.setText("⚠ Giá nhập phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException ex) {
                lblError.setText("⚠ Giá nhập phải là số hợp lệ (VD: 50000)!");
                return;
            }

            int slVal;
            try {
                slVal = Integer.parseInt(sl);
                if (slVal < 0) {
                    lblError.setText("⚠ Số lượng tồn không được âm!");
                    return;
                }
            } catch (NumberFormatException ex) {
                lblError.setText("⚠ Số lượng phải là số nguyên!");
                return;
            }

            // Kiểm tra trùng mã khi thêm mới
            if (!isEdit) {
                boolean trung = dataList.stream().anyMatch(s -> s.getMa().equalsIgnoreCase(ma));
                if (trung) {
                    lblError.setText("⚠ Mã sản phẩm " + ma + " đã tồn tại!");
                    return;
                }
            }

            // Chạy lưu trên thread nền + hiện LoadingDialog
            SanPham s = new SanPham(ma, ten, loai, String.valueOf((long)giaVal), slVal,
                txtHinhAnh.getText().trim());
            Stage ownerStage = (Stage) dialog.getScene().getWindow();
            LoadingDialog loading = new LoadingDialog(ownerStage, isEdit ? "Đang cập nhật..." : "Đang thêm mới...");
            dialog.close();
            loading.show();

            Thread t = new Thread(() -> {
                boolean ok = isEdit ? controller.updateSanPham(s) : controller.addSanPham(s);
                javafx.application.Platform.runLater(() -> {
                    loading.close();
                    if (ok) loadData();
                    else {
                        Alert a = new Alert(Alert.AlertType.ERROR,
                            isEdit ? "Cập nhật thất bại!" : "Thêm mới thất bại (mã đã tồn tại?)!");
                        a.setHeaderText(null); a.show();
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        root.getChildren().addAll(
            titleRow, new Separator(),
            formRow("Mã SP", txtMa),
            formRow("Tên SP", txtTen),
            formRow("Loại", cbLoai),
            formRow("Giá Nhập", txtGia),
            formRow("Số Lượng", txtSL),
            formRow("Hình Ảnh", txtHinhAnh),
            lblError, btnRow
        );

        Scene sc = new Scene(root);
        sc.getStylesheets().add("file:///" +
            java.nio.file.Paths.get("style.css").toAbsolutePath().toString().replace("\\", "/"));
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    private void showProductDetailsDialog(SanPham sp) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 40, 0, 0, 10);"
        );
        root.setPadding(new Insets(24));
        root.setPrefWidth(320);

        // Close button at top right
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 18; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        topRow.getChildren().add(closeBtn);
        VBox.setMargin(topRow, new Insets(-10, -10, 0, 0));

        // Big Image
        Node imageNode;
        String hinhAnhUrl = sp.getHinhAnh();
        if (hinhAnhUrl != null && !hinhAnhUrl.trim().isEmpty()) {
            Image img = new Image(hinhAnhUrl, true);

            StackPane imgContainer = new StackPane();
            imgContainer.setPrefSize(200, 200);
            imgContainer.setMaxSize(200, 200);
            imgContainer.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 16;");

            Label fallbackIcon = new Label(getIcon(sp.getLoai()));
            fallbackIcon.setStyle("-fx-font-size: 72px;");
            fallbackIcon.setAlignment(Pos.CENTER);
            imgContainer.getChildren().add(fallbackIcon);

            Rectangle rect = new Rectangle(200, 200);
            rect.setArcWidth(24); rect.setArcHeight(24);
            rect.setVisible(false);

            img.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() >= 1.0 && !img.isError()) {
                    rect.setFill(new ImagePattern(img));
                    rect.setVisible(true);
                    fallbackIcon.setVisible(false);
                }
            });
            if (!img.isError() && img.getWidth() > 0) {
                rect.setFill(new ImagePattern(img));
                rect.setVisible(true);
                fallbackIcon.setVisible(false);
            }
            img.errorProperty().addListener((obs, old, isErr) -> {
                if (isErr) {
                    rect.setVisible(false);
                    fallbackIcon.setVisible(true);
                }
            });
            imgContainer.getChildren().add(rect);
            imageNode = imgContainer;
        } else {
            imageNode = makeFallbackPane(sp.getLoai(), 200, 200, 72);
        }

        // Info
        Label name = new Label(sp.getTen());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-wrap-text: true; -fx-text-alignment: center;");
        name.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label price = new Label(formatMoney(sp.getGiaNhap()));
        price.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");

        Label stock = new Label("Kho: " + sp.getSoLuongTon());
        stock.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");

        // Buttons
        Button btnAddCart = new Button("🛒 Thêm vào giỏ");
        btnAddCart.setMaxWidth(Double.MAX_VALUE);
        btnAddCart.setStyle(
            "-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #BFDBFE; -fx-border-radius: 8;"
        );
        
        Button btnBuyNow = new Button("⚡ Mua ngay");
        btnBuyNow.setMaxWidth(Double.MAX_VALUE);
        btnBuyNow.setStyle(
            "-fx-background-color: #2563EB; -fx-text-fill: #FFFFFF; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"
        );

        if (sp.getSoLuongTon() <= 0) {
            btnAddCart.setDisable(true);
            btnBuyNow.setDisable(true);
            stock.setText("Kho: Hết hàng");
            stock.setStyle("-fx-font-size: 14px; -fx-text-fill: #DC2626; -fx-font-weight: bold;");
        }

        btnAddCart.setOnAction(e -> {
            CartManager.getInstance().addProduct(sp);
            dialog.close();
        });

        btnBuyNow.setOnAction(e -> {
            CartManager.getInstance().addProduct(sp);
            dialog.close();
        });

        VBox btnBox = new VBox(10, btnAddCart, btnBuyNow);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(topRow, imageNode, name, price, stock, btnBox);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    private VBox formRow(String labelText, Control field) {
        Label lbl = new Label(labelText.toUpperCase());
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        
        String padding = (field instanceof TextField) ? "-fx-padding: 9 12;" : "-fx-padding: 4 8;";
        field.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-border-radius: 6;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-font-size: 13px; " + padding
        );
        if (field instanceof Region) {
            ((Region) field).setMaxWidth(Double.MAX_VALUE);
        }
        return new VBox(5, lbl, field);
    }

    private ComboBox<String> buildCombo(String prompt, String value) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("Đồ uống", "Đồ ăn", "Trái cây", "Quần áo", "Giày dép", "Dụng cụ", "Mỹ phẩm", "Khác");
        cb.setPromptText(prompt);
        if (value != null && !value.isEmpty()) {
            // Find match ignoring case to handle old data
            String matched = "Khác";
            for (String item : cb.getItems()) {
                if (item.equalsIgnoreCase(value)) {
                    matched = item;
                    break;
                }
            }
            cb.setValue(matched);
        }
        return cb;
    }

    private TextField buildField(String prompt, String value) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        return tf;
    }

    private Button buildBtn(String text, boolean primary) {
        Button btn = new Button(text);
        btn.setMinWidth(Region.USE_PREF_SIZE); // Khóa không cho co cụm làm mất chữ
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

    private String activeFilterStyle() {
        return "-fx-background-color: #2563EB; -fx-background-radius: 20; -fx-border-color: #2563EB;" +
               "-fx-border-radius: 20; -fx-border-width: 1; -fx-text-fill: #FFFFFF;" +
               "-fx-font-size: 12px; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-weight: bold;";
    }

    private String inactiveFilterStyle() {
        return "-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-color: #D1D5DB;" +
               "-fx-border-radius: 20; -fx-border-width: 1; -fx-text-fill: #374151;" +
               "-fx-font-size: 12px; -fx-padding: 6 14; -fx-cursor: hand;";
    }

    private boolean showConfirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận"); alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void loadData() {
        Thread t = new Thread(() -> {
            java.util.List<SanPham> result;
            try { result = controller.getAllSanPham(); }
            catch (Exception e) { result = java.util.Collections.emptyList(); }
            final java.util.List<SanPham> data = result;
            javafx.application.Platform.runLater(() -> {
                dataList.clear();
                dataList.addAll(data);
                filteredList = new FilteredList<>(dataList, p -> true);
                applyFilter();
                refreshGrid();
            });
        });
        t.setDaemon(true);
        t.start();
    }
}
