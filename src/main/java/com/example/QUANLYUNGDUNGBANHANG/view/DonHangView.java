package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import com.example.QUANLYUNGDUNGBANHANG.controller.HoaDonController;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * DonHangView — Trang "Đơn Hàng" phía người dùng.
 * Hiển thị 5 tab theo giai đoạn đơn hàng:
 *   1. Chờ thanh toán
 *   2. Chờ vận chuyển
 *   3. Chờ nhận
 *   4. Đã nhận thành công
 *   5. Cần đánh giá
 */
public class DonHangView extends VBox {

    // ───────── Giai đoạn ─────────
    private static final String[] STAGE_LABELS = {
        "⏳ Chờ Thanh Toán",
        "🚚 Chờ Vận Chuyển",
        "📦 Chờ Nhận",
        "✅ Đã Nhận",
        "⭐ Cần Đánh Giá"
    };

    private static final String[] STAGE_ICONS = { "⏳", "🚚", "📦", "✅", "⭐" };

    private static final String[] STAGE_COLORS = {
        "#F59E0B",  // vàng – chờ thanh toán
        "#3B82F6",  // xanh dương – chờ vận chuyển
        "#8B5CF6",  // tím – chờ nhận
        "#10B981",  // xanh lá – đã nhận
        "#EF4444"   // đỏ – cần đánh giá
    };

    private static final String[] STAGE_BG = {
        "#FFFBEB", "#EFF6FF", "#F5F3FF", "#ECFDF5", "#FEF2F2"
    };

    private int currentTab = 0;
    private final List<Button> tabBtns = new ArrayList<>();
    private final StackPane contentArea = new StackPane();

    // Dữ liệu mẫu (giai đoạn được gán giả lập từ HoaDon lịch sử)
    private final HoaDonController controller = new HoaDonController();
    private final List<ObservableList<HoaDon>> stagedData = new ArrayList<>();

    public DonHangView() {
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #F3F4F6;");

        for (int i = 0; i < 5; i++) {
            stagedData.add(FXCollections.observableArrayList());
        }

        this.getChildren().addAll(buildTopBar(), buildTabBar(), buildContent());
        VBox.setVgrow(buildContent(), Priority.ALWAYS);

        loadData();

        FadeTransition ft = new FadeTransition(Duration.millis(300), this);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ─── TOP BAR ───────────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(20, 24, 14, 24));
        bar.setStyle("-fx-background-color: #F3F4F6;");

        VBox titleBox = new VBox(3);
        Label title = new Label("🛍️  Đơn Hàng Của Tôi");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label sub = new Label("Theo dõi trạng thái và lịch sử đơn hàng của bạn");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = new Button("↻  Làm Mới");
        btnRefresh.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-text-fill: #374151;" +
            "-fx-font-size: 13px; -fx-padding: 9 18; -fx-cursor: hand;" +
            "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;"
        );
        btnRefresh.setOnMouseEntered(e -> btnRefresh.setStyle(
            "-fx-background-color: #EFF6FF; -fx-background-radius: 8; -fx-text-fill: #2563EB;" +
            "-fx-font-size: 13px; -fx-padding: 9 18; -fx-cursor: hand;" +
            "-fx-border-color: #BFDBFE; -fx-border-radius: 8; -fx-border-width: 1;"));
        btnRefresh.setOnMouseExited(e -> btnRefresh.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-text-fill: #374151;" +
            "-fx-font-size: 13px; -fx-padding: 9 18; -fx-cursor: hand;" +
            "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;"));
        btnRefresh.setOnAction(e -> loadData());

        bar.getChildren().addAll(titleBox, spacer, btnRefresh);
        return bar;
    }

    // ─── TAB BAR ───────────────────────────────────────────────────────────────
    private ScrollPane buildTabBar() {
        HBox tabs = new HBox(8);
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.setPadding(new Insets(0, 24, 14, 24));

        for (int i = 0; i < STAGE_LABELS.length; i++) {
            final int idx = i;
            Button btn = buildTabButton(STAGE_LABELS[i], i);
            btn.setOnAction(e -> switchTab(idx));
            tabBtns.add(btn);
            tabs.getChildren().add(btn);
        }
        setTabActive(0);

        ScrollPane sp = new ScrollPane(tabs);
        sp.setFitToHeight(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        sp.setMinHeight(Region.USE_PREF_SIZE);
        return sp;
    }

    private Button buildTabButton(String label, int idx) {
        Button btn = new Button(label);
        btn.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20;" +
            "-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-padding: 9 18; -fx-cursor: hand;" +
            "-fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-border-width: 1.5;"
        );
        return btn;
    }

    private void setTabActive(int idx) {
        for (int i = 0; i < tabBtns.size(); i++) {
            if (i == idx) {
                tabBtns.get(i).setStyle(
                    "-fx-background-color: " + STAGE_COLORS[i] + "; -fx-background-radius: 20;" +
                    "-fx-text-fill: #FFFFFF; -fx-font-size: 13px; -fx-font-weight: bold;" +
                    "-fx-padding: 9 18; -fx-cursor: hand;" +
                    "-fx-border-color: " + STAGE_COLORS[i] + "; -fx-border-radius: 20; -fx-border-width: 1.5;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
                );
            } else {
                tabBtns.get(i).setStyle(
                    "-fx-background-color: #FFFFFF; -fx-background-radius: 20;" +
                    "-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-weight: bold;" +
                    "-fx-padding: 9 18; -fx-cursor: hand;" +
                    "-fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-border-width: 1.5;"
                );
            }
        }
    }

    // ─── CONTENT AREA ──────────────────────────────────────────────────────────
    private StackPane buildContent() {
        contentArea.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        renderTab(0);
        return contentArea;
    }

    private void switchTab(int idx) {
        if (idx == currentTab) return;
        currentTab = idx;
        setTabActive(idx);
        renderTab(idx);
    }

    private void renderTab(int idx) {
        Node pane = buildOrderList(idx);
        pane.setOpacity(0);
        contentArea.getChildren().setAll(pane);
        FadeTransition ft = new FadeTransition(Duration.millis(220), pane);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ─── ORDER LIST PER TAB ────────────────────────────────────────────────────
    private ScrollPane buildOrderList(int tabIdx) {
        VBox list = new VBox(14);
        list.setPadding(new Insets(0, 24, 24, 24));

        ObservableList<HoaDon> data = stagedData.get(tabIdx);

        if (data.isEmpty()) {
            list.getChildren().add(buildEmptyState(tabIdx));
        } else {
            for (HoaDon hd : data) {
                list.getChildren().add(buildOrderCard(hd, tabIdx));
            }
        }

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle(
            "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;"
        );
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    // ─── EMPTY STATE ───────────────────────────────────────────────────────────
    private VBox buildEmptyState(int tabIdx) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60, 0, 40, 0));

        Label icon = new Label(STAGE_ICONS[tabIdx]);
        icon.setStyle(
            "-fx-font-size: 52px; -fx-background-color: " + STAGE_BG[tabIdx] + ";" +
            "-fx-background-radius: 50; -fx-min-width: 100; -fx-min-height: 100; -fx-alignment: center;"
        );

        Label msg = new Label("Không có đơn hàng nào");
        msg.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        Label sub = new Label("Các đơn ở giai đoạn \"" + STAGE_LABELS[tabIdx] + "\" sẽ hiển thị tại đây.");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #9CA3AF; -fx-text-alignment: center;");
        sub.setWrapText(true);
        sub.setMaxWidth(300);

        box.getChildren().addAll(icon, msg, sub);
        return box;
    }

    // ─── ORDER CARD ────────────────────────────────────────────────────────────
    private VBox buildOrderCard(HoaDon hd, int tabIdx) {
        VBox card = new VBox(0);
        card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 14;" +
            "-fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        );

        // ── Card Header ──────────────────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 10, 16));
        header.setStyle(
            "-fx-background-color: " + STAGE_BG[tabIdx] + ";" +
            "-fx-background-radius: 14 14 0 0;"
        );

        Label stageIcon = new Label(STAGE_ICONS[tabIdx]);
        stageIcon.setStyle("-fx-font-size: 18px;");

        Label stageLbl = new Label(STAGE_LABELS[tabIdx].replaceFirst("^. ", ""));
        stageLbl.setStyle(
            "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + STAGE_COLORS[tabIdx] + ";" +
            "-fx-background-color: " + STAGE_BG[tabIdx] + ";" +
            "-fx-background-radius: 12; -fx-padding: 3 10;"
        );

        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Label maHD = new Label("Mã: " + (hd.getMaHD() != null ? hd.getMaHD() : "---"));
        maHD.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        header.getChildren().addAll(stageIcon, stageLbl, hSpacer, maHD);

        // ── Card Body ────────────────────────────────────────────────
        VBox body = new VBox(10);
        body.setPadding(new Insets(14, 16, 14, 16));

        // Product row
        HBox productRow = new HBox(12);
        productRow.setAlignment(Pos.CENTER_LEFT);

        Label pIcon = new Label("📦");
        pIcon.setStyle(
            "-fx-font-size: 22px; -fx-background-color: #F9FAFB; -fx-background-radius: 8;" +
            "-fx-min-width: 44; -fx-min-height: 44; -fx-alignment: center;"
        );

        VBox pInfo = new VBox(4);
        Label pName = new Label(hd.getTenSanPham() != null ? hd.getTenSanPham() : "Sản phẩm");
        pName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-wrap-text: true;");
        pName.setMaxWidth(200);
        Label pDate = new Label("📅  " + (hd.getNgay() != null ? hd.getNgay() : "---"));
        pDate.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");
        pInfo.getChildren().addAll(pName, pDate);

        Region pSpacer = new Region(); HBox.setHgrow(pSpacer, Priority.ALWAYS);

        VBox pPrice = new VBox(4);
        pPrice.setAlignment(Pos.CENTER_RIGHT);
        Label pTotal = new Label(formatMoney(hd.thanhTien()));
        pTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1D4ED8;");
        Label pQty = new Label("x" + hd.getSoLuong());
        pQty.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");
        pPrice.getChildren().addAll(pTotal, pQty);

        productRow.getChildren().addAll(pIcon, pInfo, pSpacer, pPrice);

        // Divider
        Region div = new Region();
        div.setPrefHeight(1); div.setMaxHeight(1);
        div.setStyle("-fx-background-color: #F3F4F6;");

        // Progress track (5-step timeline)
        HBox progressBar = buildProgressTrack(tabIdx);

        // Action buttons
        HBox actions = buildActionButtons(hd, tabIdx);

        body.getChildren().addAll(productRow, div, progressBar, actions);

        card.getChildren().addAll(header, body);

        // Hover elevation
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 14;" +
            "-fx-border-color: " + STAGE_COLORS[tabIdx] + "; -fx-border-radius: 14; -fx-border-width: 1.5;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0, 0, 6);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 14;" +
            "-fx-border-color: #E5E7EB; -fx-border-radius: 14; -fx-border-width: 1;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);"
        ));

        // Double-click → detail
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) showOrderDetail(hd, tabIdx);
        });

        return card;
    }

    // ─── PROGRESS TRACK ────────────────────────────────────────────────────────
    private HBox buildProgressTrack(int currentStage) {
        HBox track = new HBox(0);
        track.setAlignment(Pos.CENTER);
        track.setPadding(new Insets(10, 0, 6, 0));

        String[] shortLabels = { "Thanh toán", "Vận chuyển", "Chờ nhận", "Đã nhận", "Đánh giá" };

        for (int i = 0; i < 5; i++) {
            boolean done   = i < currentStage;
            boolean active = i == currentStage;

            // Step circle
            Label circle = new Label(done ? "✓" : String.valueOf(i + 1));
            circle.setStyle(
                "-fx-font-size: " + (done ? "12px" : "11px") + ";" +
                "-fx-font-weight: bold;" +
                "-fx-min-width: 26; -fx-min-height: 26;" +
                "-fx-background-radius: 50; -fx-alignment: center;" +
                (done   ? "-fx-background-color: " + STAGE_COLORS[i] + "; -fx-text-fill: white;" :
                 active ? "-fx-background-color: " + STAGE_COLORS[currentStage] + "; -fx-text-fill: white;" +
                           "-fx-effect: dropshadow(gaussian, " + STAGE_COLORS[currentStage] + ", 8, 0, 0, 0);" :
                           "-fx-background-color: #E5E7EB; -fx-text-fill: #9CA3AF;")
            );

            VBox step = new VBox(4);
            step.setAlignment(Pos.CENTER);
            Label stepLbl = new Label(shortLabels[i]);
            stepLbl.setStyle(
                "-fx-font-size: 10px;" +
                "-fx-text-fill: " + (done || active ? "#374151" : "#D1D5DB") + ";" +
                (active ? "-fx-font-weight: bold;" : "")
            );
            step.getChildren().addAll(circle, stepLbl);
            step.setMinWidth(60);

            track.getChildren().add(step);

            // Connector line (skip after last)
            if (i < 4) {
                Region line = new Region();
                line.setPrefHeight(2); line.setMaxHeight(2);
                line.setPrefWidth(20);
                line.setStyle("-fx-background-color: " + (done ? STAGE_COLORS[i] : "#E5E7EB") + ";");
                HBox.setHgrow(line, Priority.ALWAYS);
                track.getChildren().add(line);
            }
        }
        return track;
    }

    // ─── ACTION BUTTONS ────────────────────────────────────────────────────────
    private HBox buildActionButtons(HoaDon hd, int tabIdx) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(8, 0, 0, 0));

        // Common: Xem chi tiết
        Button btnDetail = ghostBtn("Xem chi tiết", "#2563EB");
        btnDetail.setOnAction(e -> showOrderDetail(hd, tabIdx));
        box.getChildren().add(btnDetail);

        switch (tabIdx) {
            case 0: // Chờ thanh toán
                Button btnPay = solidBtn("Thanh toán ngay", STAGE_COLORS[0]);
                btnPay.setOnAction(e -> showActionConfirm("Thanh toán",
                    "Xác nhận thanh toán đơn hàng " + hd.getMaHD() + "?", () -> {
                        moveToNextStage(hd, tabIdx);
                    }));
                Button btnCancel = ghostBtn("Hủy đơn", "#EF4444");
                btnCancel.setOnAction(e -> showActionConfirm("Hủy đơn",
                    "Bạn có chắc muốn hủy đơn hàng " + hd.getMaHD() + "?", () -> {
                        stagedData.get(tabIdx).remove(hd);
                        renderTab(tabIdx);
                        showToast("Đã hủy đơn hàng thành công!");
                    }));
                box.getChildren().addAll(btnCancel, btnPay);
                break;

            case 1: // Chờ vận chuyển
                Button btnShip = solidBtn("Giao hàng", STAGE_COLORS[1]);
                btnShip.setOnAction(e -> showActionConfirm("Xác nhận giao hàng",
                    "Xác nhận bắt đầu vận chuyển đơn hàng " + hd.getMaHD() + "?", () -> {
                        moveToNextStage(hd, tabIdx);
                    }));
                box.getChildren().add(btnShip);
                break;

            case 2: // Chờ nhận
                Button btnReceived = solidBtn("Đã nhận hàng", STAGE_COLORS[2]);
                btnReceived.setOnAction(e -> showActionConfirm("Xác nhận nhận hàng",
                    "Xác nhận bạn đã nhận được đơn hàng " + hd.getMaHD() + "?", () -> {
                        moveToNextStage(hd, tabIdx);
                    }));
                box.getChildren().add(btnReceived);
                break;

            case 3: // Đã nhận
                Button btnReview3 = solidBtn("Đánh giá", STAGE_COLORS[3]);
                btnReview3.setOnAction(e -> showReviewDialog(hd, tabIdx));
                box.getChildren().add(btnReview3);
                break;

            case 4: // Cần đánh giá
                Button btnReview4 = solidBtn("⭐ Đánh giá ngay", STAGE_COLORS[4]);
                btnReview4.setOnAction(e -> showReviewDialog(hd, tabIdx));
                box.getChildren().add(btnReview4);
                break;
        }

        return box;
    }

    private Button solidBtn(String label, String color) {
        Button btn = new Button(label);
        btn.setStyle(
            "-fx-background-color: " + color + "; -fx-background-radius: 8;" +
            "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-padding: 8 18; -fx-cursor: hand;"
        );
        return btn;
    }

    private Button ghostBtn(String label, String color) {
        Button btn = new Button(label);
        btn.setStyle(
            "-fx-background-color: transparent; -fx-background-radius: 8;" +
            "-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-padding: 8 18; -fx-cursor: hand;" +
            "-fx-border-color: " + color + "; -fx-border-radius: 8; -fx-border-width: 1.2;"
        );
        return btn;
    }

    // ─── ORDER DETAIL DIALOG ───────────────────────────────────────────────────
    private void showOrderDetail(HoaDon hd, int tabIdx) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 16;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 40, 0, 0, 10);"
        );
        root.setPrefWidth(460);

        // Header banner
        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(20, 20, 16, 20));
        banner.setStyle("-fx-background-color: " + STAGE_BG[tabIdx] + "; -fx-background-radius: 16 16 0 0;");
        Label bannerIcon = new Label(STAGE_ICONS[tabIdx]);
        bannerIcon.setStyle("-fx-font-size: 28px;");
        VBox bannerText = new VBox(3);
        Label bannerTitle = new Label("Chi Tiết Đơn Hàng");
        bannerTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label bannerSub = new Label(STAGE_LABELS[tabIdx].replaceFirst("^. ", ""));
        bannerSub.setStyle("-fx-font-size: 12px; -fx-text-fill: " + STAGE_COLORS[tabIdx] + "; -fx-font-weight: bold;");
        bannerText.getChildren().addAll(bannerTitle, bannerSub);
        Region bSpacer = new Region(); HBox.setHgrow(bSpacer, Priority.ALWAYS);
        Button btnX = new Button("✕");
        btnX.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 16; -fx-cursor: hand;");
        btnX.setOnAction(e -> dialog.close());
        banner.getChildren().addAll(bannerIcon, bannerText, bSpacer, btnX);

        // Body
        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 24, 24, 24));

        // Amount
        Label amtLabel = new Label(formatMoney(hd.thanhTien()));
        amtLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        amtLabel.setMaxWidth(Double.MAX_VALUE);
        amtLabel.setAlignment(Pos.CENTER);

        // Info grid
        VBox infoBox = new VBox(10);
        infoBox.setStyle(
            "-fx-background-color: #F9FAFB; -fx-background-radius: 10; -fx-padding: 16;" +
            "-fx-border-color: #F3F4F6; -fx-border-radius: 10; -fx-border-width: 1;"
        );
        infoBox.getChildren().addAll(
            detailRow("Mã đơn hàng",   hd.getMaHD()         != null ? hd.getMaHD()        : "---"),
            detailRow("Ngày đặt",       hd.getNgay()          != null ? hd.getNgay()         : "---"),
            detailRow("Khách hàng",     hd.getKhachHang()    != null ? hd.getKhachHang()   : "---"),
            detailRow("Sản phẩm",       hd.getTenSanPham()   != null ? hd.getTenSanPham()  : "---"),
            detailRow("Số lượng",       String.valueOf(hd.getSoLuong())),
            detailRow("Đơn giá",        formatMoney(hd.getDonGia())),
            detailRow("Tổng tiền",      formatMoney(hd.thanhTien()))
        );

        // Progress track large
        HBox prog = buildProgressTrack(tabIdx);
        prog.setPadding(new Insets(8, 0, 8, 0));

        // Close button
        Button btnClose = new Button("Đóng");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle(
            "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnClose.setOnAction(e -> dialog.close());
        btnClose.setOnMouseEntered(e -> btnClose.setStyle(
            "-fx-background-color: #E5E7EB; -fx-text-fill: #111827; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle(
            "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"));

        body.getChildren().addAll(amtLabel, infoBox, prog, btnClose);
        root.getChildren().addAll(banner, body);

        // Animate in
        root.setScaleX(0.88); root.setScaleY(0.88); root.setOpacity(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(240), root);
        st.setToX(1); st.setToY(1);
        st.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        FadeTransition fdt = new FadeTransition(Duration.millis(200), root);
        fdt.setFromValue(0); fdt.setToValue(1);
        root.sceneProperty().addListener((obs, o, sc2) -> {
            if (sc2 != null) new javafx.animation.ParallelTransition(st, fdt).play();
        });

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ─── ACTION CONFIRM DIALOG ─────────────────────────────────────────────────
    private void showActionConfirm(String title, String message, Runnable onConfirm) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0, 0, 8);"
        );
        root.setPrefWidth(360);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label msgLbl = new Label(message);
        msgLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-text-alignment: center;");
        msgLbl.setWrapText(true);

        HBox btns = new HBox(12);
        btns.setAlignment(Pos.CENTER);
        Button btnNo = new Button("Hủy");
        btnNo.setStyle(
            "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnNo.setOnAction(e -> dialog.close());

        Button btnYes = new Button("Xác nhận");
        btnYes.setStyle(
            "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 14px;" +
            "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnYes.setOnAction(e -> { dialog.close(); onConfirm.run(); });

        btns.getChildren().addAll(btnNo, btnYes);
        root.getChildren().addAll(titleLbl, msgLbl, btns);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ─── REVIEW DIALOG ─────────────────────────────────────────────────────────
    private void showReviewDialog(HoaDon hd, int tabIdx) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(28));
        root.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0, 0, 8);"
        );
        root.setPrefWidth(400);

        // Header
        Label titleLbl = new Label("⭐ Đánh Giá Sản Phẩm");
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label pName = new Label(hd.getTenSanPham() != null ? hd.getTenSanPham() : "Sản phẩm");
        pName.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");

        // Star rating
        HBox stars = new HBox(8);
        stars.setAlignment(Pos.CENTER);
        final int[] rating = {0};
        Label[] starBtns = new Label[5];
        for (int i = 0; i < 5; i++) {
            final int starIdx = i + 1;
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 34px; -fx-text-fill: #D1D5DB; -fx-cursor: hand;");
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setText(j < starIdx ? "★" : "☆");
                    starBtns[j].setStyle("-fx-font-size: 34px; -fx-text-fill: " +
                        (j < starIdx ? "#F59E0B" : "#D1D5DB") + "; -fx-cursor: hand;");
                }
            });
            star.setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setText(j < rating[0] ? "★" : "☆");
                    starBtns[j].setStyle("-fx-font-size: 34px; -fx-text-fill: " +
                        (j < rating[0] ? "#F59E0B" : "#D1D5DB") + "; -fx-cursor: hand;");
                }
            });
            star.setOnMouseClicked(e -> {
                rating[0] = starIdx;
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setText(j < starIdx ? "★" : "☆");
                    starBtns[j].setStyle("-fx-font-size: 34px; -fx-text-fill: " +
                        (j < starIdx ? "#F59E0B" : "#D1D5DB") + "; -fx-cursor: hand;");
                }
            });
            starBtns[i] = star;
            stars.getChildren().add(star);
        }

        // Comment
        TextArea comment = new TextArea();
        comment.setPromptText("Nhận xét của bạn về sản phẩm...");
        comment.setPrefRowCount(3);
        comment.setWrapText(true);
        comment.setStyle(
            "-fx-background-color: #F9FAFB; -fx-background-radius: 8; -fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8; -fx-border-width: 1; -fx-font-size: 13px; -fx-padding: 10;"
        );

        // Buttons
        HBox btns = new HBox(12);
        btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = ghostBtn("Hủy", "#6B7280");
        btnCancel.setOnAction(e -> dialog.close());
        Button btnSubmit = solidBtn("Gửi đánh giá", "#F59E0B");
        btnSubmit.setOnAction(e -> {
            if (rating[0] == 0) {
                // nếu chưa chọn sao
                titleLbl.setText("⚠ Vui lòng chọn số sao!");
                titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #EF4444;");
                return;
            }
            dialog.close();
            showToast("Đánh giá của bạn đã được gửi! Cảm ơn 🎉");
            moveToNextStage(hd, tabIdx);
        });
        btns.getChildren().addAll(btnCancel, btnSubmit);

        root.getChildren().addAll(titleLbl, pName, stars, comment, btns);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
    }

    // ─── MOVE TO NEXT STAGE (local demo) ──────────────────────────────────────
    private void moveToNextStage(HoaDon hd, int fromTab) {
        stagedData.get(fromTab).remove(hd);
        int nextTab = fromTab + 1;
        if (nextTab < 5) {
            stagedData.get(nextTab).add(hd);
        }
        renderTab(fromTab);
        showToast("Đơn hàng đã chuyển sang giai đoạn tiếp theo!");
    }

    // ─── TOAST ────────────────────────────────────────────────────────────────
    private void showToast(String msg) {
        Stage toast = new Stage();
        toast.initStyle(StageStyle.TRANSPARENT);
        toast.setAlwaysOnTop(true);

        Label lbl = new Label(msg);
        lbl.setStyle(
            "-fx-background-color: rgba(17,24,39,0.92); -fx-text-fill: white;" +
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-radius: 10;" +
            "-fx-padding: 12 24;"
        );

        StackPane sp = new StackPane(lbl);
        sp.setStyle("-fx-background-color: transparent;");
        Scene sc = new Scene(sp);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        toast.setScene(sc);
        toast.show();

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), lbl);
        fadeOut.setDelay(Duration.millis(1800));
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> toast.close());
        fadeOut.play();
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
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
        return NumberFormat.getInstance(new Locale("vi", "VN")).format((long) amount) + " ₫";
    }

    // ─── LOAD DATA ────────────────────────────────────────────────────────────
    /**
     * Phân bổ đơn hàng vào các giai đoạn giả lập.
     * Thực tế: server nên trả về trường "trangThai" để phân tab chính xác.
     * Tạm thời: lấy HoaDon lịch sử rồi dùng modulo gán giai đoạn demo.
     */
    private void loadData() {
        Thread t = new Thread(() -> {
            java.util.List<HoaDon> result;
            try { result = controller.getHoaDonHistory(); }
            catch (Exception e) { result = java.util.Collections.emptyList(); }
            final java.util.List<HoaDon> data = result;

            javafx.application.Platform.runLater(() -> {
                for (int i = 0; i < 5; i++) stagedData.get(i).clear();

                // Demo: phân phối đơn theo modulo chỉ số
                for (int i = 0; i < data.size(); i++) {
                    stagedData.get(i % 5).add(data.get(i));
                }

                // Re-render tab hiện tại
                renderTab(currentTab);
            });
        });
        t.setDaemon(true);
        t.start();
    }
}
