package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.HoaDonController;
import com.example.QUANLYUNGDUNGBANHANG.model.CartItem;
import com.example.QUANLYUNGDUNGBANHANG.model.CartManager;
import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import com.example.QUANLYUNGDUNGBANHANG.network.Request;
import com.example.QUANLYUNGDUNGBANHANG.network.SocketClient;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.HoaDonDTO;
import com.example.QUANLYUNGDUNGBANHANG.util.AnimationUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.ImageCache;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainView extends BorderPane {

    private Button activeBtn = null;
    private final StackPane contentStack = new StackPane();

    // Right panel — dùng chung cho tất cả view
    private Label lblSubTotal;
    private Label lblTotal;
    private final VBox rightPanel = buildRightPanel();

    private void updateCartTotalsUI() {
        if (lblSubTotal != null && lblTotal != null) {
            updateTotals(lblSubTotal, lblTotal);
        }
    }

    public MainView(Stage stage, String username, String role) {
        // BỐ CỤC: Sidebar hẹp | Nội dung | Right Panel
        VBox sidebar = buildSidebar(stage, username, role);

        contentStack.setStyle("-fx-background-color: #EFF6FF;");
        VBox.setVgrow(contentStack, Priority.ALWAYS);

        this.setLeft(sidebar);
        this.setCenter(contentStack);
        this.setRight(null); // Không hiện panel giỏ hàng ở trang chủ mặc định

        // Mặc định Dashboard
        navigateTo(new DashboardView(), null);
    }

    // ===== SIDEBAR — CHỈ ICON =====
    private VBox buildSidebar(Stage stage, String username, String role) {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        VBox sidebar = new VBox(0);
        sidebar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2563EB, #3B82F6);" +
            "-fx-border-color: transparent;"
        );
        sidebar.setPrefWidth(180);
        sidebar.setMinWidth(180);
        sidebar.setMaxWidth(180);
        sidebar.setAlignment(Pos.TOP_LEFT);

        // Logo icon
        Label logo = new Label("🛒  HỆ THỐNG");
        logo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-padding: 18 16 14 16;");
        logo.setMaxWidth(Double.MAX_VALUE);
        logo.setAlignment(Pos.CENTER_LEFT);

        Region div1 = new Region();
        div1.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        div1.setPrefHeight(1); div1.setMaxHeight(1); div1.setMaxWidth(Double.MAX_VALUE);

        // Nav buttons (icon + text)
        Button btnDash = iconNavBtn("🏠", "Trang Chủ");
        Button btnSP   = iconNavBtn("📦", "Sản Phẩm");
        Button btnDH   = iconNavBtn("📋", "Đơn Hàng");
        Button btnKH   = iconNavBtn("👥", "Khách Hàng");
        Button btnHD   = iconNavBtn("🧾", "Hóa Đơn");
        Button btnTK   = iconNavBtn("👤", "Tài khoản");

        btnDash.setOnAction(e -> { navigateTo(new DashboardView(),             btnDash); setActive(btnDash); showRightPanel(false); });
        btnSP.setOnAction(  e -> { navigateTo(new SanPhamView(role),           btnSP);   setActive(btnSP);   showRightPanel(true);  });
        btnDH.setOnAction(  e -> { navigateTo(new DonHangView(),               btnDH);   setActive(btnDH);   showRightPanel(false); });
        btnKH.setOnAction(  e -> { navigateTo(new KhachHangView(role),         btnKH);   setActive(btnKH);   showRightPanel(false); });
        btnHD.setOnAction(  e -> { navigateTo(new HoaDonView(),                btnHD);   setActive(btnHD);   showRightPanel(false); });
        btnTK.setOnAction(  e -> { navigateTo(new TaiKhoanView(username),      btnTK);   setActive(btnTK);   showRightPanel(false); });

        setActive(btnDash);

        VBox navGroup = new VBox(6);
        navGroup.getChildren().addAll(btnDash, btnSP, btnDH);
        if (isAdmin) {
            navGroup.getChildren().add(btnKH);
        }
        navGroup.getChildren().addAll(btnHD, btnTK);
        navGroup.setPadding(new Insets(12, 8, 0, 8));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Region div2 = new Region();
        div2.setStyle("-fx-background-color: rgba(255,255,255,0.15);");
        div2.setPrefHeight(1); div2.setMaxHeight(1); div2.setMaxWidth(Double.MAX_VALUE);

        // User avatar + logout
        String roleLabel = isAdmin ? "ADMIN" : "USER";
        Label avatarLbl = new Label("👤  " + username);
        avatarLbl.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-alignment: center-left;" +
            "-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;" +
            "-fx-padding: 10 12; -fx-cursor: hand;"
        );
        avatarLbl.setMaxWidth(Double.MAX_VALUE);
        avatarLbl.setAlignment(Pos.CENTER_LEFT);
        avatarLbl.setTooltip(new Tooltip(username + " (" + roleLabel + ")"));

        Button btnLogout = new Button("⏏  Đăng xuất");
        btnLogout.setStyle(
            "-fx-background-color: rgba(239,68,68,0.25); -fx-background-radius: 8;" +
            "-fx-text-fill: #FCA5A5; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 12;" +
            "-fx-cursor: hand; -fx-border-color: transparent; -fx-alignment: center-left;"
        );
        btnLogout.setTooltip(new Tooltip("Đăng xuất"));
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> {
            stage.setResizable(false);
            Scene sc = new Scene(new LoginView(stage), 900, 550);
            sc.getStylesheets().add("file:///" +
                java.nio.file.Paths.get("style.css").toAbsolutePath().toString().replace("\\", "/"));
            stage.setScene(sc);
            stage.setTitle("Đăng nhập - Quản Lý Bán Hàng");
            stage.setMinWidth(0); stage.setMinHeight(0);
            stage.sizeToScene(); stage.centerOnScreen();
        });

        VBox bottomBox = new VBox(8, div2, avatarLbl, btnLogout);
        bottomBox.setPadding(new Insets(0, 8, 14, 8));
        bottomBox.setAlignment(Pos.CENTER_LEFT);

        sidebar.getChildren().addAll(logo, div1, navGroup, spacer, bottomBox);
        return sidebar;
    }

    private void showRightPanel(boolean show) {
        if (show) {
            if (this.getRight() == null) {
                this.setRight(rightPanel);
                // Slide in từ phải
                rightPanel.setTranslateX(rightPanel.getPrefWidth());
                rightPanel.setOpacity(0);
                javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(260), rightPanel);
                tt.setFromX(rightPanel.getPrefWidth()); tt.setToX(0);
                tt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(260), rightPanel);
                ft.setFromValue(0); ft.setToValue(1);
                new javafx.animation.ParallelTransition(tt, ft).play();
            }
        } else {
            if (this.getRight() != null) {
                javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), rightPanel);
                tt.setFromX(0); tt.setToX(rightPanel.getPrefWidth());
                tt.setInterpolator(javafx.animation.Interpolator.EASE_IN);
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), rightPanel);
                ft.setFromValue(1); ft.setToValue(0);
                javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(tt, ft);
                pt.setOnFinished(e -> this.setRight(null));
                pt.play();
            }
        }
    }

    // ===== RIGHT PANEL — Tóm tắt / Cart =====
    private VBox buildRightPanel() {
        VBox panel = new VBox(0);
        panel.setPrefWidth(320);
        panel.setMinWidth(320);
        panel.setMaxWidth(320);
        panel.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: transparent transparent transparent #E5E7EB;" +
            "-fx-border-width: 0 0 0 1;"
        );

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 16, 16, 16));
        header.setStyle(
            "-fx-background-color: #EFF6FF;" +
            "-fx-border-color: transparent transparent #BFDBFE transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );
        Label headerLbl = new Label("Mua hàng");
        headerLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E40AF;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnClear = new Button("Xóa hết");
        btnClear.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-font-size: 13px;");
        btnClear.setOnAction(e -> CartManager.getInstance().clearCart());
        
        header.getChildren().addAll(headerLbl, spacer, btnClear);

        // Cart items area
        VBox cartItemsBox = new VBox(0);
        ScrollPane scrollPane = new ScrollPane(cartItemsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Empty cart hint (initial state)
        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(40, 0, 0, 0));
        Label emptyIcon = new Label("🛍️");
        emptyIcon.setStyle("-fx-font-size: 40px;");
        Label emptyText = new Label("Chưa có sản phẩm nào");
        emptyText.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px; -fx-text-alignment: center;");
        emptyBox.getChildren().addAll(emptyIcon, emptyText);
        cartItemsBox.getChildren().add(emptyBox);

        // Summary area
        VBox summaryBox = new VBox(12);
        summaryBox.setPadding(new Insets(16));
        summaryBox.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: #E5E7EB transparent transparent transparent;" +
            "-fx-border-width: 1 0 0 0;"
        );
        
        // Customer
        HBox customerBox = new HBox(8);
        customerBox.setAlignment(Pos.CENTER_LEFT);
        Label customerIcon = new Label("👤");
        customerIcon.setStyle("-fx-font-size: 16px;");
        Label customerName = new Label("Khách Lẻ (Mặc định)");
        customerName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        customerBox.getChildren().addAll(customerIcon, customerName);

        // Lines
        lblSubTotal = new Label("0 ₫");
        lblTotal = new Label("0 ₫");
        
        summaryBox.getChildren().addAll(
            customerBox,
            new Separator(),
            summaryLine("Tạm tính", lblSubTotal, false),
            summaryLine("Thuế (0%)", new Label("0 ₫"), false),
            summaryLine("Tổng", lblTotal, true)
        );

        // Pay button
        Button btnPay = new Button("THANH TOÁN");
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setStyle(
            "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 16px;" +
            "-fx-font-weight: bold; -fx-padding: 14 0; -fx-background-radius: 0; -fx-cursor: hand;"
        );
        
        panel.getChildren().addAll(header, scrollPane, summaryBox, btnPay);

        // Listen to CartManager changes
        CartManager.getInstance().getCartItems().addListener((ListChangeListener<CartItem>) c -> {
            cartItemsBox.getChildren().clear();
            if (CartManager.getInstance().getCartItems().isEmpty()) {
                cartItemsBox.getChildren().add(emptyBox);
                lblSubTotal.setText("0 ₫");
                lblTotal.setText("0 ₫");
                btnPay.setDisable(true);
            } else {
                for (CartItem item : CartManager.getInstance().getCartItems()) {
                    cartItemsBox.getChildren().add(buildCartRow(item));
                }
                updateTotals(lblSubTotal, lblTotal);
                btnPay.setDisable(false);
            }
        });
        
        // Initial state
        btnPay.setDisable(true);
        btnPay.setOnAction(e -> showCheckoutConfirmDialog());

        return panel;
    }
    
    private void updateTotals(Label sub, Label tot) {
        double total = CartManager.getInstance().getTotalAmount();
        String formatted = formatMoney(total);
        sub.setText(formatted);
        tot.setText(formatted);
    }
    
    private HBox summaryLine(String title, Label valLbl, boolean isTotal) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (isTotal ? "#111827" : "#6B7280") + ";" + 
                   (isTotal ? " -fx-font-weight: bold;" : ""));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        valLbl.setStyle("-fx-font-size: " + (isTotal ? "16px" : "13px") + "; -fx-text-fill: " + 
                        (isTotal ? "#2563EB" : "#111827") + ";" +
                        (isTotal ? " -fx-font-weight: bold;" : ""));
        box.getChildren().addAll(t, sp, valLbl);
        return box;
    }

    private HBox buildCartRow(CartItem item) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 0 0 1 0;");
        
        // Image in cart row — dùng ImageCache
        Node imageNode;
        String hinhAnhUrl = item.getSanPham().getHinhAnh();
        if (hinhAnhUrl != null && !hinhAnhUrl.trim().isEmpty()) {
            try {
                Image img = ImageCache.get(hinhAnhUrl);
                Rectangle rect = new Rectangle(40, 40);
                rect.setArcWidth(8); rect.setArcHeight(8);
                rect.setFill(javafx.scene.paint.Color.valueOf("#EFF6FF"));
                if (img != null) {
                    if (!img.isError() && img.getWidth() > 0) {
                        rect.setFill(new ImagePattern(img));
                    } else {
                        img.progressProperty().addListener((obs, oldV, newV) -> {
                            if (newV.doubleValue() >= 1.0 && !img.isError()) rect.setFill(new ImagePattern(img));
                        });
                        img.errorProperty().addListener((obs, old, isErr) -> {
                            if (isErr) rect.setFill(javafx.scene.paint.Color.valueOf("#EFF6FF"));
                        });
                    }
                }
                imageNode = rect;
            } catch (Exception e) {
                Label fallback = new Label("📦");
                fallback.setStyle("-fx-font-size: 20px; -fx-background-color: #EFF6FF; -fx-background-radius: 4; -fx-padding: 4 8;");
                imageNode = fallback;
            }
        } else {
            Label fallback = new Label("📦");
            fallback.setStyle("-fx-font-size: 20px; -fx-background-color: #EFF6FF; -fx-background-radius: 4; -fx-padding: 4 8;");
            imageNode = fallback;
        }

        // Name
        Label name = new Label(item.getSanPham().getTen());
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        name.setPrefWidth(85); name.setWrapText(true);

        // Quantity controls
        HBox qtyBox = new HBox(4);
        qtyBox.setAlignment(Pos.CENTER);
        
        Button btnMinus = new Button("-");
        btnMinus.setStyle("-fx-background-color: #F3F4F6; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
        
        Label lblQty = new Label(String.valueOf(item.getSoLuong()));
        lblQty.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        lblQty.setMinWidth(22);
        lblQty.setPrefWidth(22);
        lblQty.setAlignment(Pos.CENTER);
        
        Button btnPlus = new Button("+");
        btnPlus.setStyle("-fx-background-color: #F3F4F6; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
        
        // Price
        Label price = new Label(formatMoney(item.getTongTien()));
        price.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #E63946;");
        price.setMinWidth(65);
        price.setAlignment(Pos.CENTER_RIGHT);
        
        btnMinus.setOnAction(e -> {
            if (item.getSoLuong() > 1) {
                item.setSoLuong(item.getSoLuong() - 1);
                lblQty.setText(String.valueOf(item.getSoLuong()));
                price.setText(formatMoney(item.getTongTien()));
                updateCartTotalsUI();
            }
        });
        btnPlus.setOnAction(e -> {
            if (item.getSoLuong() < item.getSanPham().getSoLuongTon()) {
                item.setSoLuong(item.getSoLuong() + 1);
                lblQty.setText(String.valueOf(item.getSoLuong()));
                price.setText(formatMoney(item.getTongTien()));
                updateCartTotalsUI();
            }
        });
        qtyBox.getChildren().addAll(btnMinus, lblQty, btnPlus);
        qtyBox.setMinWidth(Region.USE_PREF_SIZE);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Remove
        Button btnDel = new Button("✕");
        btnDel.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-cursor: hand; -fx-padding: 4; -fx-font-weight: bold;");
        btnDel.setMinWidth(24);
        btnDel.setOnAction(e -> CartManager.getInstance().removeProduct(item));

        // Animate cart row vào từ phải
        row.setOpacity(0);
        row.setTranslateX(20);
        javafx.animation.FadeTransition rowFt = new javafx.animation.FadeTransition(javafx.util.Duration.millis(220), row);
        rowFt.setFromValue(0); rowFt.setToValue(1);
        javafx.animation.TranslateTransition rowTt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(220), row);
        rowTt.setFromX(20); rowTt.setToX(0);
        rowTt.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        new javafx.animation.ParallelTransition(rowFt, rowTt).play();

        row.getChildren().addAll(imageNode, name, spacer, qtyBox, price, btnDel);
        return row;
    }
    
    private void showCheckoutConfirmDialog() {
        if (CartManager.getInstance().getCartItems().isEmpty()) return;
        double totalAmount = CartManager.getInstance().getTotalAmount();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 30, 0, 0, 8);");
        root.setPrefWidth(380);

        // Header
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER);
        Label title = new Label("Xác nhận thanh toán");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 16; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.close());
        topRow.getChildren().addAll(title, spacer, closeBtn);

        // Total amount
        VBox totalBox = new VBox(4);
        totalBox.setAlignment(Pos.CENTER);
        Label lblTotalTitle = new Label("Tổng tiền");
        lblTotalTitle.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        Label lblTotal = new Label(formatMoney(totalAmount));
        lblTotal.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #D97706;");
        totalBox.getChildren().addAll(lblTotalTitle, lblTotal);

        // Products List
        VBox productListBox = new VBox(8);
        Label lblProductTitle = new Label("Danh sách sản phẩm");
        lblProductTitle.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        VBox listContainer = new VBox(8);
        for (CartItem item : CartManager.getInstance().getCartItems()) {
            HBox pRow = new HBox(10);
            pRow.setAlignment(Pos.CENTER_LEFT);
            pRow.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 10 12; -fx-background-radius: 6; -fx-border-color: #F3F4F6; -fx-border-radius: 6; -fx-border-width: 1;");
            
            VBox infoBox = new VBox(4);
            Label pName = new Label(item.getSanPham().getTen());
            pName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
            Label pCode = new Label("Mã SP: " + item.getSanPham().getMa());
            pCode.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            infoBox.getChildren().addAll(pName, pCode);
            
            Region pSpacer = new Region(); HBox.setHgrow(pSpacer, Priority.ALWAYS);
            
            VBox priceBox = new VBox(4);
            priceBox.setAlignment(Pos.CENTER_RIGHT);
            Label pQty = new Label("SL: " + item.getSoLuong());
            pQty.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
            Label pTotal = new Label(formatMoney(item.getTongTien()));
            pTotal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");
            priceBox.getChildren().addAll(pTotal, pQty);
            
            pRow.getChildren().addAll(infoBox, pSpacer, priceBox);
            listContainer.getChildren().add(pRow);
        }
        
        ScrollPane scrollProds = new ScrollPane(listContainer);
        scrollProds.setFitToWidth(true);
        scrollProds.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scrollProds.setMaxHeight(200);
        scrollProds.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollProds.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        productListBox.getChildren().addAll(lblProductTitle, scrollProds);

        // Transaction Details
        VBox transactionBox = new VBox(8);
        transactionBox.setStyle("-fx-background-color: #F9FAFB; -fx-padding: 12; -fx-background-radius: 6; -fx-border-color: #F3F4F6; -fx-border-radius: 6; -fx-border-width: 1;");
        
        Label lblTxTitle = new Label("Chi tiết giao dịch");
        lblTxTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        HBox timeRow = new HBox();
        Label lblTimeLabel = new Label("Thời gian");
        lblTimeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        Region timeSpacer = new Region(); HBox.setHgrow(timeSpacer, Priority.ALWAYS);
        Label lblTimeVal = new Label(currentTime);
        lblTimeVal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        timeRow.getChildren().addAll(lblTimeLabel, timeSpacer, lblTimeVal);
        
        transactionBox.getChildren().addAll(lblTxTitle, timeRow);

        // Confirm Button
        Button btnConfirm = new Button("Xác nhận thanh toán");
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 14 0; -fx-background-radius: 8; -fx-cursor: hand;");
        btnConfirm.setOnAction(e -> {
            btnConfirm.setText("Đang xử lý...");
            btnConfirm.setDisable(true);
            processCheckout(dialog);
        });

        root.getChildren().addAll(topRow, totalBox, new Separator(), productListBox, transactionBox, btnConfirm);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.show();
    }
    
    private void processCheckout(Stage confirmDialog) {
        if (CartManager.getInstance().getCartItems().isEmpty()) return;

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String maHD = "HD" + System.currentTimeMillis();

        // Snapshot the cart to avoid ConcurrentModificationException after clearCart
        List<CartItem> snapshot = new ArrayList<>(CartManager.getInstance().getCartItems());

        // Chuẩn bị danh sách DTO để gửi lên Server (1 action CHECKOUT duy nhất)
        List<HoaDonDTO> dtos = new ArrayList<>();
        for (CartItem item : snapshot) {
            SanPham sp = item.getSanPham();
            try {
                double donGia = Double.parseDouble(sp.getGiaNhap().replaceAll("[^\\d.]", ""));
                HoaDonDTO dto = new HoaDonDTO(maHD, time, "Khách Lẻ",
                    sp.getMa(), sp.getTen(), item.getSoLuong(), donGia, item.getTongTien());
                dtos.add(dto);
            } catch (NumberFormatException ignored) {}
        }

        if (dtos.isEmpty()) {
            if (confirmDialog != null) confirmDialog.close();
            return;
        }

        // Gửi lên Server qua Socket — chạy trong background thread để tránh treo UI
        LoadingDialog loading = new LoadingDialog(
            confirmDialog != null ? confirmDialog : null, "Đang thanh toán...");
        if (confirmDialog != null) confirmDialog.close();
        loading.show();

        Thread bgThread = new Thread(() -> {
            // Gọi action CHECKOUT — Server xử lý transaction hoàn toàn
            Request req = new Request("CHECKOUT");
            req.setPayload(dtos);
            var res = SocketClient.getInstance().sendRequest(req);
            boolean success = res.isSuccess();
            String message  = res.getMessage();

            javafx.application.Platform.runLater(() -> {
                loading.close();
                if (success) {
                    CartManager.getInstance().clearCart();
                    double total = snapshot.stream().mapToDouble(CartItem::getTongTien).sum();
                    showSuccessDialog(snapshot, total, maHD, time);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Thanh toán thất bại!\n" + message);
                    alert.setHeaderText(null);
                    alert.show();
                }
            });
        });
        bgThread.setDaemon(true);
        bgThread.start();
    }

    private void showSuccessDialog(List<CartItem> items, double totalAmount, String maHD, String time) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32));
        root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 40, 0, 0, 10);");
        root.setPrefWidth(360);
        // Animate dialog xuất hiện
        root.setScaleX(0.85); root.setScaleY(0.85); root.setOpacity(0);
        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(280), root);
        scaleIn.setToX(1.0); scaleIn.setToY(1.0);
        scaleIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        javafx.animation.FadeTransition fadeIn2 = new javafx.animation.FadeTransition(javafx.util.Duration.millis(220), root);
        fadeIn2.setFromValue(0); fadeIn2.setToValue(1);
        root.sceneProperty().addListener((obs, o, sc2) -> {
            if (sc2 != null) new javafx.animation.ParallelTransition(scaleIn, fadeIn2).play();
        });

        // Checkmark icon
        Label checkIcon = new Label("✔");
        checkIcon.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 80; -fx-min-height: 80; -fx-alignment: center;");
        
        // Amount & Status
        VBox topBox = new VBox(6);
        topBox.setAlignment(Pos.CENTER);
        Label lblAmount = new Label(formatMoney(totalAmount));
        lblAmount.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label lblStatus = new Label("Thanh toán thành công");
        lblStatus.setStyle("-fx-font-size: 15px; -fx-text-fill: #059669; -fx-font-weight: bold;");
        topBox.getChildren().addAll(lblAmount, lblStatus);

        // Details box
        VBox detailsBox = new VBox(10);
        detailsBox.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 12; -fx-padding: 16; -fx-border-color: #F3F4F6; -fx-border-radius: 12; -fx-border-width: 1;");
        detailsBox.setMaxWidth(Double.MAX_VALUE);
        
        detailsBox.getChildren().addAll(
            detailRow("Mã giao dịch", maHD),
            detailRow("Thời gian", time),
            detailRow("Khách hàng", "Khách Lẻ")
        );
        
        // Products summary
        StringBuilder prodStr = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            prodStr.append("• ").append(item.getSanPham().getTen()).append(" (x").append(item.getSoLuong()).append(")");
            if (i < items.size() - 1) prodStr.append("\n");
        }
        Label prodLabel = new Label(prodStr.toString());
        prodLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-line-spacing: 4px;");
        prodLabel.setWrapText(true);
        VBox prodBox = new VBox(6, new Label("Sản phẩm:") {{ setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;"); }}, prodLabel);
        
        detailsBox.getChildren().addAll(new Separator(), prodBox);

        // Close button
        Button btnClose = new Button("Hoàn tất");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;");
        btnClose.setOnAction(e -> dialog.close());
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #111827; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"));

        root.getChildren().addAll(checkIcon, topBox, detailsBox, btnClose);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.show();
    }

    private HBox detailRow(String label, String value) {
        HBox box = new HBox();
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        box.getChildren().addAll(l, s, v);
        return box;
    }

    private String formatMoney(double amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format((long) amount) + "₫";
    }

    // ===== ICON NAV BUTTON =====
    private Button iconNavBtn(String icon, String label) {
        Button btn = new Button(icon + "   " + label);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: rgba(255,255,255,0.75);" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent;" +
            "-fx-alignment: center-left;"
        );
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setTooltip(new Tooltip(label));

        // Hover effect to make it "nổi lên" (pop out a bit)
        btn.setOnMouseEntered(e -> {
            if (btn != activeBtn) {
                btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.1);" +
                    "-fx-text-fill: #FFFFFF;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 12 16;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: transparent;" +
                    "-fx-alignment: center-left;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"
                );
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeBtn) {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: rgba(255,255,255,0.75);" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 12 16;" +
                    "-fx-background-radius: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: transparent;" +
                    "-fx-alignment: center-left;"
                );
            }
        });

        return btn;
    }

    private void setActive(Button btn) {
        if (activeBtn != null) {
            activeBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: rgba(255,255,255,0.75);" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 12 16;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: transparent;" +
                "-fx-alignment: center-left;"
            );
        }
        activeBtn = btn;
        btn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.22);" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 12 16;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent;" +
            "-fx-alignment: center-left;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        );
    }

    private void navigateTo(Node content, Button btn) {
        // Fade out nhanh
        FadeTransition ftOut = new FadeTransition(javafx.util.Duration.millis(120), contentStack);
        ftOut.setFromValue(1.0); ftOut.setToValue(0.0);
        ftOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        ftOut.setOnFinished(e -> {
            content.setTranslateX(20);
            contentStack.getChildren().setAll(content);
            // Fade + slide in mượt
            FadeTransition ftIn = new FadeTransition(javafx.util.Duration.millis(260), contentStack);
            ftIn.setFromValue(0.0); ftIn.setToValue(1.0);
            ftIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            TranslateTransition ttIn = new TranslateTransition(javafx.util.Duration.millis(260), content);
            ttIn.setFromX(20); ttIn.setToX(0);
            ttIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
            new javafx.animation.ParallelTransition(ftIn, ttIn).play();
        });
        ftOut.play();
    }
}
