package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.HoaDonController;
import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.Locale;

public class HoaDonView extends VBox {
    private final HoaDonController controller = new HoaDonController();
    private final TableView<HoaDon> table = new TableView<>();
    private final ObservableList<HoaDon> dataList = FXCollections.observableArrayList();
    private FilteredList<HoaDon> filteredList;
    private final Label totalLabel = new Label("Tổng doanh thu: 0 ₫");

    public HoaDonView() {
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #EFF6FF;");

        this.getChildren().addAll(buildTopBar(), buildSummaryBar(), buildTableCard());
        VBox.setVgrow(this.getChildren().get(2), Priority.ALWAYS);
        loadData();

        FadeTransition ft = new FadeTransition(Duration.millis(280), this);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ===== TOP BAR =====
    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(20, 20, 12, 20));
        bar.setStyle("-fx-background-color: #EFF6FF;");

        VBox titleBox = new VBox(2);
        Label title = new Label("🧾  Hóa Đơn");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label sub = new Label("Lịch sử giao dịch và doanh thu cửa hàng");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        titleBox.getChildren().addAll(title, sub);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Tìm mã HĐ, khách hàng...");
        searchField.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-radius: 20;" +
            "-fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF; -fx-padding: 8 16; -fx-font-size: 13px; -fx-pref-width: 240;"
        );

        Button btnRefresh = new Button("↻  Làm Mới");
        btnRefresh.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 6; -fx-text-fill: #374151;" +
            "-fx-font-size: 13px; -fx-padding: 9 18; -fx-cursor: hand;" +
            "-fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-border-width: 1;"
        );
        btnRefresh.setOnAction(e -> loadData());

        bar.getChildren().addAll(titleBox, spacer, searchField, btnRefresh);

        searchField.textProperty().addListener((obs, old, nv) -> {
            if (filteredList == null) return;
            filteredList.setPredicate(hd -> {
                if (nv == null || nv.isEmpty()) return true;
                String lower = nv.toLowerCase();
                return (hd.getMaHD() != null && hd.getMaHD().toLowerCase().contains(lower))
                    || (hd.getKhachHang() != null && hd.getKhachHang().toLowerCase().contains(lower))
                    || (hd.getTenKhachHang() != null && hd.getTenKhachHang().toLowerCase().contains(lower))
                    || (hd.getTenSanPham() != null && hd.getTenSanPham().toLowerCase().contains(lower));
            });
        });

        return bar;
    }

    // ===== SUMMARY BAR =====
    private HBox buildSummaryBar() {
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1D4ED8;");

        Label icon = new Label("💰");
        icon.setStyle("-fx-font-size: 18px;");

        HBox bar = new HBox(10, icon, totalLabel);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle(
            "-fx-background-color: #EFF6FF; -fx-background-radius: 10;" +
            "-fx-border-color: #BFDBFE; -fx-border-radius: 10; -fx-border-width: 1;" +
            "-fx-padding: 12 20;"
        );
        VBox.setMargin(bar, new Insets(0, 20, 12, 20));

        return bar;
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
        table.setPlaceholder(new Label("Chưa có hóa đơn nào trong hệ thống"));

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                showHoaDonDetails(table.getSelectionModel().getSelectedItem());
            }
        });

        TableColumn<HoaDon, String>  colMaHD  = col("MÃ HĐ",           "maHD",          120);
        TableColumn<HoaDon, String>  colNgay  = col("NGÀY",             "ngay",          120);
        TableColumn<HoaDon, String>  colKH    = col("MÃ KH",            "khachHang",     110);
        TableColumn<HoaDon, String>  colTenKH = col("TÊN KHÁCH HÀNG",   "tenKhachHang",  170);
        TableColumn<HoaDon, String>  colSP    = col("SẢN PHẨM",         "tenSanPham",    180);

        TableColumn<HoaDon, Integer> colSL = new TableColumn<>("SL");
        colSL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colSL.setPrefWidth(60);
        colSL.setStyle("-fx-alignment: CENTER;");

        TableColumn<HoaDon, Double> colDonGia = new TableColumn<>("ĐƠN GIÁ");
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colDonGia.setPrefWidth(120);
        colDonGia.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatMoney(item));
                setStyle("-fx-text-fill: #374151;");
            }
        });

        TableColumn<HoaDon, Double> colTotal = new TableColumn<>("TỔNG TIỀN");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("tongTien"));
        colTotal.setPrefWidth(130);
        colTotal.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); return; }
                Label lbl = new Label(formatMoney(item));
                lbl.setStyle("-fx-text-fill: #1D4ED8; -fx-font-weight: bold;");
                setGraphic(lbl); setText(null);
            }
        });

        table.getColumns().addAll(colMaHD, colNgay, colKH, colTenKH, colSP, colSL, colDonGia, colTotal);
    }

    private <T> TableColumn<HoaDon, T> col(String header, String prop, double w) {
        TableColumn<HoaDon, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        return c;
    }

    private void showHoaDonDetails(HoaDon hd) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32));
        root.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 40, 0, 0, 10);");
        root.setPrefWidth(420);

        // Icon
        Label checkIcon = new Label("🧾");
        checkIcon.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-font-size: 36px; -fx-background-radius: 50; -fx-min-width: 70; -fx-min-height: 70; -fx-alignment: center;");
        
        Label lblTitle = new Label("Chi Tiết Hóa Đơn");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // Amount & Status
        VBox topBox = new VBox(6);
        topBox.setAlignment(Pos.CENTER);
        Label lblAmount = new Label(formatMoney(hd.getTongTien()));
        lblAmount.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label lblStatus = new Label("Đã thanh toán thành công");
        lblStatus.setStyle("-fx-font-size: 14px; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-background-color: #D1FAE5; -fx-padding: 4 12; -fx-background-radius: 20;");
        topBox.getChildren().addAll(lblAmount, lblStatus);

        // Details box
        VBox detailsBox = new VBox(12);
        detailsBox.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: #F3F4F6; -fx-border-radius: 12; -fx-border-width: 1;");
        detailsBox.setMaxWidth(Double.MAX_VALUE);
        
        detailsBox.getChildren().addAll(
            detailRow("Mã hóa đơn", hd.getMaHD()),
            detailRow("Thời gian", hd.getNgay()),
            detailRow("Khách hàng", hd.getKhachHang())
        );
        
        // Products summary
        VBox prodBox = new VBox(8);
        Label prodTitle = new Label("Chi tiết sản phẩm");
        prodTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        
        HBox pRow = new HBox();
        VBox pInfo = new VBox(4);
        Label pName = new Label(hd.getTenSanPham());
        pName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-wrap-text: true;");
        pName.setMaxWidth(200);
        Label pCode = new Label("Mã SP: " + hd.getSanPham());
        pCode.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        pInfo.getChildren().addAll(pName, pCode);
        
        Region pSpacer = new Region(); HBox.setHgrow(pSpacer, Priority.ALWAYS);
        
        VBox pPrice = new VBox(4);
        pPrice.setAlignment(Pos.CENTER_RIGHT);
        Label pTotal = new Label(formatMoney(hd.getDonGia() * hd.getSoLuong()));
        pTotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");
        Label pQty = new Label(formatMoney(hd.getDonGia()) + " x " + hd.getSoLuong());
        pQty.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        pPrice.getChildren().addAll(pTotal, pQty);
        
        pRow.getChildren().addAll(pInfo, pSpacer, pPrice);
        prodBox.getChildren().addAll(prodTitle, pRow);
        
        detailsBox.getChildren().addAll(new Separator(), prodBox);

        // Close button
        Button btnClose = new Button("Đóng");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;");
        btnClose.setOnAction(e -> dialog.close());
        btnClose.setOnMouseEntered(e -> btnClose.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #111827; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnClose.setOnMouseExited(e -> btnClose.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;"));

        root.getChildren().addAll(checkIcon, lblTitle, topBox, detailsBox, btnClose);

        Scene sc = new Scene(root);
        sc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.setScene(sc);
        dialog.showAndWait();
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

    // ===== LOGIC =====
    private void loadData() {
        Thread t = new Thread(() -> {
            java.util.List<HoaDon> result;
            try { result = controller.getHoaDonHistory(); }
            catch (Exception e) { result = java.util.Collections.emptyList(); }
            final java.util.List<HoaDon> data = result;
            javafx.application.Platform.runLater(() -> {
                dataList.clear();
                dataList.addAll(data);
                if (filteredList != null) filteredList.setPredicate(p -> true);
                updateTotal();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void updateTotal() {
        double total = dataList.stream().mapToDouble(HoaDon::thanhTien).sum();
        totalLabel.setText("Tổng doanh thu: " + formatMoney(total)
            + "   |   " + dataList.size() + " hóa đơn");
    }

    private String formatMoney(double amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format((long) amount) + " ₫";
    }
}
