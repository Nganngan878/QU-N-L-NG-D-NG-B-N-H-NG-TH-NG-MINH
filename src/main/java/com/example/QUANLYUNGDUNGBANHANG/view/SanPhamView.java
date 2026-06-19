package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.SanPhamController;
import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import com.example.QUANLYUNGDUNGBANHANG.network.Request;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.network.SocketClient;
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
    private final String username; // Username của người dùng hiện tại

    /** Constructor mặc định — tương thích với code cũ (mặc định ADMIN) */
    public SanPhamView() {
        this("ADMIN", "ADMIN");
    }

    /** Constructor có role — kiểm soát hiển thị nút CRUD theo quyền */
    public SanPhamView(String role) {
        this("ANONYMOUS", role);
    }

    /** Constructor có username và role — kiểm soát hiển thị nút CRUD theo quyền */
    public SanPhamView(String username, String role) {
        this.username = username != null ? username : "ANONYMOUS";
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
        boolean outOfStock = sp.getSoLuongTon() <= 0;
        Label slBadge = new Label(outOfStock ? "Hết hàng" : "Kho: " + sp.getSoLuongTon());
        slBadge.setStyle(sp.getSoLuongTon() > 10
            ? "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 10px;"
            : (outOfStock
                ? "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 10px;"
                : "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 10; -fx-padding: 2 8; -fx-font-size: 10px;")
        );

        HBox bottomRow = new HBox(6, giaLbl, new Region(), slBadge);
        HBox.setHgrow(bottomRow.getChildren().get(1), Priority.ALWAYS);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        // === Nút mua hàng trực tiếp (chỉ hiện với USER) ===
        boolean isUser = !"ADMIN".equalsIgnoreCase(role);
        VBox card;
        if (isUser) {
            Button btnBuyCard = new Button(outOfStock ? "Hết hàng" : "🛒  Mua ngay");
            btnBuyCard.setMaxWidth(Double.MAX_VALUE);
            btnBuyCard.setDisable(outOfStock);
            btnBuyCard.setStyle(
                outOfStock
                ? "-fx-background-color: #E5E7EB; -fx-text-fill: #9CA3AF; -fx-font-size: 12px;" +
                  "-fx-font-weight: bold; -fx-padding: 7 0; -fx-background-radius: 8;"
                : "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 12px;" +
                  "-fx-font-weight: bold; -fx-padding: 7 0; -fx-background-radius: 8; -fx-cursor: hand;"
            );
            if (!outOfStock) {
                btnBuyCard.setOnMouseEntered(ev -> btnBuyCard.setStyle(
                    "-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-size: 12px;" +
                    "-fx-font-weight: bold; -fx-padding: 7 0; -fx-background-radius: 8; -fx-cursor: hand;"
                ));
                btnBuyCard.setOnMouseExited(ev -> btnBuyCard.setStyle(
                    "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 12px;" +
                    "-fx-font-weight: bold; -fx-padding: 7 0; -fx-background-radius: 8; -fx-cursor: hand;"
                ));
                btnBuyCard.setOnAction(ev -> {
                    ev.consume(); // ngăn click lan ra card
                    showPurchaseDialog(sp);
                });
            }
            card = new VBox(8, imageNode, tenLbl, maLbl, bottomRow, btnBuyCard);
        } else {
            card = new VBox(8, imageNode, tenLbl, maLbl, bottomRow);
        }

        card.setPadding(new Insets(12));
        card.setPrefWidth(164);
        card.setMaxWidth(164);
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 10;" +
            "-fx-border-color: #E5E7EB; -fx-border-radius: 10; -fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );

        // Hover effect
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
        // Click card → mở chi tiết
        card.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY && e.getClickCount() == 1) {
                showProductDetailsDialog(sp);
            }
        });

        // Context menu (chỉ Admin thấy Sửa/Xóa)
        ContextMenu cm = new ContextMenu();
        MenuItem miDetail = new MenuItem("🔍  Xem chi tiết");
        miDetail.setOnAction(e -> showProductDetailsDialog(sp));
        cm.getItems().add(miDetail);
        if ("ADMIN".equalsIgnoreCase(role)) {
            MenuItem miEdit   = new MenuItem("✏  Sửa");
            MenuItem miDelete = new MenuItem("🗑  Xóa");
            miEdit.setOnAction(e -> showFormDialog(sp));
            miDelete.setOnAction(e -> {
                if (showConfirm("Xóa sản phẩm '" + sp.getTen() + "'?")) {
                    if (controller.deleteSanPham(sp.getMa())) loadData();
                }
            });
            cm.getItems().addAll(miEdit, miDelete);
        } else if (!outOfStock) {
            MenuItem miBuy = new MenuItem("🛒  Mua ngay");
            miBuy.setOnAction(e -> showPurchaseDialog(sp));
            cm.getItems().add(miBuy);
        }
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

        Label codeLabel = new Label("Mã SP: " + sp.getMa());
        codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        Label typeLabel = new Label("Loại: " + sp.getLoai());
        typeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        Label stock = new Label("Kho: " + sp.getSoLuongTon() + " sản phẩm");
        boolean outOfStock = sp.getSoLuongTon() <= 0;
        stock.setStyle(outOfStock
            ? "-fx-font-size: 14px; -fx-text-fill: #DC2626; -fx-font-weight: bold;"
            : "-fx-font-size: 14px; -fx-text-fill: #059669; -fx-font-weight: bold;");
        if (outOfStock) stock.setText("Hết hàng");

        // Info box
        VBox infoBox = new VBox(6, codeLabel, typeLabel, stock);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 8; -fx-padding: 12;");
        infoBox.setMaxWidth(Double.MAX_VALUE);

        // Close button
        Button btnClose = new Button("Đóng");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle(
            "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnClose.setOnAction(e -> dialog.close());
        btnClose.setOnMouseEntered(e -> btnClose.setStyle(
            "-fx-background-color: #E5E7EB; -fx-text-fill: #111827; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle(
            "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (!isAdmin) {
            Button btnBuy = new Button("🛒 Mua ngay");
            btnBuy.setMaxWidth(Double.MAX_VALUE);
            if (outOfStock) {
                btnBuy.setDisable(true);
                btnBuy.setText("Hết hàng");
                btnBuy.setStyle(
                    "-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                    "-fx-padding: 12; -fx-background-radius: 8;"
                );
            } else {
                btnBuy.setStyle(
                    "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                    "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"
                );
                btnBuy.setOnMouseEntered(ev -> btnBuy.setStyle(
                    "-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                    "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));
                btnBuy.setOnMouseExited(ev -> btnBuy.setStyle(
                    "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;" +
                    "-fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));
                btnBuy.setOnAction(ev -> {
                    dialog.close();
                    showPurchaseDialog(sp);
                });
            }
            root.getChildren().addAll(topRow, imageNode, name, price, infoBox, btnBuy, btnClose);
        } else {
            root.getChildren().addAll(topRow, imageNode, name, price, infoBox, btnClose);
        }

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    private void showPurchaseDialog(SanPham sp) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        // === Root container ===
        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 50, 0, 0, 12);"
        );
        root.setPrefWidth(400);

        // Bounce-in animation
        root.setScaleX(0.85); root.setScaleY(0.85); root.setOpacity(0);
        root.sceneProperty().addListener((obs, o, sc2) -> {
            if (sc2 != null) {
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(280), root);
                st.setToX(1.0); st.setToY(1.0);
                st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                javafx.animation.FadeTransition ft2 = new javafx.animation.FadeTransition(javafx.util.Duration.millis(220), root);
                ft2.setFromValue(0); ft2.setToValue(1);
                new javafx.animation.ParallelTransition(st, ft2).play();
            }
        });

        // === HEADER (gradient xanh) ===
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 16, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #1D4ED8, #3B82F6); -fx-background-radius: 20 20 0 0;");

        HBox hdrRow = new HBox();
        hdrRow.setAlignment(Pos.CENTER_LEFT);
        Label hdrTitle = new Label("🛒  Đặt Mua Sản Phẩm");
        hdrTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Region hdrSpacer = new Region(); HBox.setHgrow(hdrSpacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 50; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4 8;");
        closeBtn.setOnAction(e -> dialog.close());
        hdrRow.getChildren().addAll(hdrTitle, hdrSpacer, closeBtn);

        Label hdrSub = new Label("Vui lòng kiểm tra thông tin trước khi thanh toán");
        hdrSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        header.getChildren().addAll(hdrRow, hdrSub);

        // === BODY ===
        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 20, 6, 20));

        // --- Thông tin sản phẩm ---
        HBox productRow = new HBox(14);
        productRow.setAlignment(Pos.CENTER_LEFT);
        productRow.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 12; -fx-padding: 12;");

        // Mini image/icon
        StackPane miniImg = new StackPane();
        miniImg.setPrefSize(56, 56);
        miniImg.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 10;");
        Label miniIcon = new Label(getIcon(sp.getLoai()));
        miniIcon.setStyle("-fx-font-size: 26px;");
        if (sp.getHinhAnh() != null && !sp.getHinhAnh().trim().isEmpty()) {
            try {
                javafx.scene.image.Image img = ImageCache.get(sp.getHinhAnh());
                if (img != null && !img.isError() && img.getWidth() > 0) {
                    javafx.scene.shape.Rectangle rr = new javafx.scene.shape.Rectangle(56, 56);
                    rr.setArcWidth(10); rr.setArcHeight(10);
                    rr.setFill(new javafx.scene.paint.ImagePattern(img));
                    miniImg.getChildren().addAll(miniIcon, rr);
                } else {
                    miniImg.getChildren().add(miniIcon);
                }
            } catch (Exception ex) { miniImg.getChildren().add(miniIcon); }
        } else {
            miniImg.getChildren().add(miniIcon);
        }

        VBox productInfo = new VBox(3);
        HBox.setHgrow(productInfo, Priority.ALWAYS);
        Label pName = new Label(sp.getTen());
        pName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-wrap-text: true;");
        pName.setWrapText(true);
        Label pCode = new Label("Mã: " + sp.getMa() + "  •  " + sp.getLoai());
        pCode.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        Label pStock = new Label("Còn lại: " + sp.getSoLuongTon() + " sản phẩm");
        pStock.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669;");
        productInfo.getChildren().addAll(pName, pCode, pStock);
        productRow.getChildren().addAll(miniImg, productInfo);

        // --- Chọn số lượng ---
        VBox qtySection = new VBox(8);
        Label qtyTitle = new Label("SỐ LƯỢNG");
        qtyTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        HBox qtyRow = new HBox(10);
        qtyRow.setAlignment(Pos.CENTER_LEFT);

        Button btnMinus = new Button("－");
        btnMinus.setPrefSize(36, 36);
        btnMinus.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: #374151;");

        javafx.scene.control.TextField qtyField = new javafx.scene.control.TextField("1");
        qtyField.setPrefWidth(70);
        qtyField.setAlignment(Pos.CENTER);
        qtyField.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Button btnPlus = new Button("＋");
        btnPlus.setPrefSize(36, 36);
        btnPlus.setStyle("-fx-background-color: #2563EB; -fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: white;");

        Label maxLbl = new Label("(tối đa " + sp.getSoLuongTon() + ")");
        maxLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        qtyRow.getChildren().addAll(btnMinus, qtyField, btnPlus, maxLbl);
        qtySection.getChildren().addAll(qtyTitle, qtyRow);

        // Parse giá
        double priceVal;
        try {
            priceVal = Double.parseDouble(sp.getGiaNhap().replaceAll("[^\\d.]", ""));
        } catch (Exception e) { priceVal = 0; }
        final double finalPrice = priceVal;
        final int[] currentQty = {1};

        // --- Mã giảm giá ---
        VBox discountSection = new VBox(8);
        Label discountTitle = new Label("MÃ GIẢM GIÁ (nếu có)");
        discountTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        HBox discountRow = new HBox(8);
        discountRow.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.control.TextField couponField = new javafx.scene.control.TextField();
        couponField.setPromptText("Nhập mã giảm giá...");
        couponField.setPrefHeight(36);
        HBox.setHgrow(couponField, Priority.ALWAYS);
        couponField.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-font-size: 13px; -fx-text-fill: #111827; -fx-padding: 6 10;");
        Button btnApply = new Button("Áp dụng");
        btnApply.setPrefHeight(36);
        btnApply.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
        Label couponStatus = new Label();
        couponStatus.setStyle("-fx-font-size: 11px;");
        discountRow.getChildren().addAll(couponField, btnApply);
        discountSection.getChildren().addAll(discountTitle, discountRow, couponStatus);

        // --- Tóm tắt đơn hàng ---
        VBox summaryBox = new VBox(8);
        summaryBox.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 12; -fx-padding: 14;");

        Label summaryTitle = new Label("TÓM TẮT ĐƠN HÀNG");
        summaryTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        HBox row1 = makeSummaryRow("Đơn giá:", formatMoney(sp.getGiaNhap()), false);
        Label qtyValLbl = new Label("1 sản phẩm");
        qtyValLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        HBox row2 = new HBox();
        Label r2l = new Label("Số lượng:");
        r2l.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        Region r2s = new Region(); HBox.setHgrow(r2s, Priority.ALWAYS);
        row2.getChildren().addAll(r2l, r2s, qtyValLbl);

        Label discountValLbl = new Label("0 ₫");
        discountValLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #059669;");
        HBox row3 = new HBox();
        Label r3l = new Label("Giảm giá:");
        r3l.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        Region r3s = new Region(); HBox.setHgrow(r3s, Priority.ALWAYS);
        row3.getChildren().addAll(r3l, r3s, discountValLbl);

        javafx.scene.control.Separator sep2 = new javafx.scene.control.Separator();
        Label totalTitle = new Label("TỔNG THANH TOÁN");
        totalTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        Label totalAmtLbl = new Label(formatMoney(sp.getGiaNhap()));
        totalAmtLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");

        summaryBox.getChildren().addAll(summaryTitle, row1, row2, row3, sep2, totalTitle, totalAmtLbl);

        // Discount state
        final double[] discountPct = {0.0};

        // Hàm cập nhật tóm tắt
        Runnable updateSummary = () -> {
            int qty = currentQty[0];
            double sub = qty * finalPrice;
            double disc = sub * discountPct[0];
            double total = sub - disc;
            qtyValLbl.setText(qty + " sản phẩm");
            discountValLbl.setText("-" + formatMoney(String.valueOf((long) disc)));
            totalAmtLbl.setText(formatMoney(String.valueOf((long) total)));
        };

        // +/- buttons logic
        btnMinus.setOnAction(ev -> {
            int val = currentQty[0];
            if (val > 1) { currentQty[0] = val - 1; qtyField.setText(String.valueOf(currentQty[0])); updateSummary.run(); }
        });
        btnPlus.setOnAction(ev -> {
            int val = currentQty[0];
            if (val < sp.getSoLuongTon()) { currentQty[0] = val + 1; qtyField.setText(String.valueOf(currentQty[0])); updateSummary.run(); }
        });
        qtyField.textProperty().addListener((obs, ov, nv) -> {
            try {
                int v = Integer.parseInt(nv.trim());
                if (v >= 1 && v <= sp.getSoLuongTon()) { currentQty[0] = v; updateSummary.run(); }
            } catch (Exception ignored) {}
        });

        // Áp dụng mã giảm giá
        btnApply.setOnAction(ev -> {
            String code = couponField.getText().trim().toUpperCase();
            if (code.equals("GIAM10")) {
                discountPct[0] = 0.10;
                couponStatus.setText("✅ Giảm 10%!");
                couponStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669;");
            } else if (code.equals("GIAM20")) {
                discountPct[0] = 0.20;
                couponStatus.setText("✅ Giảm 20%!");
                couponStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669;");
            } else if (code.isEmpty()) {
                discountPct[0] = 0.0;
                couponStatus.setText("");
            } else {
                discountPct[0] = 0.0;
                couponStatus.setText("❌ Mã không hợp lệ!");
                couponStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #DC2626;");
            }
            updateSummary.run();
        });

        // Error label
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px;");

        body.getChildren().addAll(productRow, qtySection, discountSection, summaryBox, lblError);

        // === FOOTER BUTTONS ===
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(14, 20, 20, 20));
        footer.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("Hủy");
        btnCancel.setPrefHeight(44);
        HBox.setHgrow(btnCancel, Priority.ALWAYS);
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());
        btnCancel.setOnMouseEntered(e -> btnCancel.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #111827; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnCancel.setOnMouseExited(e -> btnCancel.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));

        Button btnPay = new Button("💳  Xác nhận thanh toán");
        btnPay.setPrefHeight(44);
        HBox.setHgrow(btnPay, Priority.ALWAYS);
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setStyle("-fx-background-color: linear-gradient(to right, #1D4ED8, #3B82F6); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnPay.setOnMouseEntered(e -> btnPay.setStyle("-fx-background-color: linear-gradient(to right, #1E40AF, #2563EB); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));
        btnPay.setOnMouseExited(e -> btnPay.setStyle("-fx-background-color: linear-gradient(to right, #1D4ED8, #3B82F6); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));

        btnPay.setOnAction(e -> {
            int qty = currentQty[0];
            if (qty <= 0 || qty > sp.getSoLuongTon()) {
                lblError.setText("⚠ Số lượng không hợp lệ! (1 – " + sp.getSoLuongTon() + ")");
                return;
            }
            double sub   = qty * finalPrice;
            double disc  = sub * discountPct[0];
            double total = sub - disc;
            String maHD  = "HD" + System.currentTimeMillis();
            String ngay  = java.time.LocalDate.now().toString();

            com.example.QUANLYUNGDUNGBANHANG.network.dto.HoaDonDTO dto =
                new com.example.QUANLYUNGDUNGBANHANG.network.dto.HoaDonDTO(
                    maHD, ngay, username, sp.getMa(), sp.getTen(), qty, finalPrice, total
                );
            dto.setGiamGia(disc);
            List<com.example.QUANLYUNGDUNGBANHANG.network.dto.HoaDonDTO> payload = List.of(dto);

            Request checkoutReq = new Request("CHECKOUT");
            checkoutReq.setPayload(payload);

            Stage ownerStage = (Stage) dialog.getScene().getWindow();
            LoadingDialog loading = new LoadingDialog(ownerStage, "Đang xử lý thanh toán...");
            dialog.close();
            loading.show();

            Thread t = new Thread(() -> {
                Response res = SocketClient.getInstance().sendRequest(checkoutReq);
                javafx.application.Platform.runLater(() -> {
                    loading.close();
                    if (res.isSuccess()) {
                        showPaymentSuccess(sp.getTen(), qty, total);
                        loadData();
                    } else {
                        showError("❌ Thanh toán thất bại: " + res.getMessage());
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        footer.getChildren().addAll(btnCancel, btnPay);

        root.getChildren().addAll(header, body, footer);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    /** Tạo 1 hàng tóm tắt đơn hàng (label: value) */
    private HBox makeSummaryRow(String label, String value, boolean bold) {
        HBox row = new HBox();
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;" + (bold ? " -fx-font-weight: bold;" : ""));
        row.getChildren().addAll(l, s, v);
        return row;
    }

    /** Dialog thành công sau khi thanh toán */
    private void showPaymentSuccess(String tenSP, int qty, double total) {
        Stage s = new Stage();
        s.initModality(Modality.APPLICATION_MODAL);
        s.initStyle(StageStyle.UNDECORATED);

        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(32, 28, 28, 28));
        box.setPrefWidth(320);
        box.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 40, 0, 0, 10);");

        // Bounce-in
        box.setScaleX(0.8); box.setScaleY(0.8); box.setOpacity(0);
        box.sceneProperty().addListener((obs, o, sc2) -> {
            if (sc2 != null) {
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), box);
                st.setToX(1.0); st.setToY(1.0); st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                javafx.animation.FadeTransition ft2 = new javafx.animation.FadeTransition(javafx.util.Duration.millis(220), box);
                ft2.setFromValue(0); ft2.setToValue(1);
                new javafx.animation.ParallelTransition(st, ft2).play();
            }
        });

        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Thanh toán thành công!");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #059669;");

        VBox infoBox = new VBox(6);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: #F0FDF4; -fx-background-radius: 10; -fx-padding: 14;");
        Label l1 = new Label("Sản phẩm: " + tenSP);
        l1.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-wrap-text: true;");
        l1.setWrapText(true); l1.setMaxWidth(240);
        Label l2 = new Label("Số lượng: " + qty);
        l2.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");
        Label l3 = new Label("Tổng tiền: " + formatMoney(String.valueOf((long) total)));
        l3.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");
        infoBox.getChildren().addAll(l1, l2, l3);

        Label sub = new Label("Cảm ơn bạn đã mua hàng! 🎉");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        Button btnOk = new Button("Hoàn tất");
        btnOk.setMaxWidth(Double.MAX_VALUE);
        btnOk.setPrefHeight(42);
        btnOk.setStyle("-fx-background-color: linear-gradient(to right, #059669, #10B981); -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        btnOk.setOnAction(e -> s.close());

        box.getChildren().addAll(icon, title, infoBox, sub, btnOk);

        Scene sc = new Scene(box);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        s.setScene(sc);
        s.showAndWait();
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
