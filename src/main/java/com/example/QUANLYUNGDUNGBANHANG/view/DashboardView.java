package com.example.QUANLYUNGDUNGBANHANG.view;

import com.example.QUANLYUNGDUNGBANHANG.controller.HoaDonController;
import com.example.QUANLYUNGDUNGBANHANG.controller.KhachHangController;
import com.example.QUANLYUNGDUNGBANHANG.controller.SanPhamController;
import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import com.example.QUANLYUNGDUNGBANHANG.util.AnimationUtil;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class DashboardView extends VBox {
    public DashboardView() {
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #EFF6FF; -fx-padding: 24;");

        // Tiêu đề
        Label title = new Label("📊  Tổng Quan Hệ Thống");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        Label sub = new Label("Xem nhanh các chỉ số quan trọng của cửa hàng");
        sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        VBox header = new VBox(3, title, sub);

        // Placeholder labels (will be updated after DB load)
        Label valSP = placeholder(); Label valKH = placeholder();
        Label valHD = placeholder(); Label valDT = placeholder();

        // Stat cards (built with placeholder values)
        GridPane cards = new GridPane();
        cards.setHgap(16); cards.setVgap(16);
        cards.getColumnConstraints().addAll(cc(), cc(), cc(), cc());
        cards.add(statCard("📦", "Sản Phẩm",  valSP, "#EFF6FF", "#1D4ED8", "#BFDBFE"), 0, 0);
        cards.add(statCard("👥", "Khách Hàng", valKH, "#F0FDF4", "#16A34A", "#BBF7D0"), 1, 0);
        cards.add(statCard("🧾", "Hóa Đơn",   valHD, "#FFFBEB", "#D97706", "#FDE68A"), 2, 0);
        cards.add(statCard("💰", "Doanh Thu",  valDT, "#FFF1F2", "#DC2626", "#FECDD3"), 3, 0);

        // Chart panel
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false); // To prevent animation bugs from background thread updates

        Label chartTitle = new Label("📈 Top 10 Sản Phẩm Bán Chạy Nhất");
        chartTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        VBox chartPanel = new VBox(12, chartTitle, barChart);
        chartPanel.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 20;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );
        VBox.setVgrow(chartPanel, Priority.ALWAYS);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        this.getChildren().addAll(header, cards, chartPanel);

        // Stagger animation cho 4 stat cards
        int ci = 0;
        for (javafx.scene.Node child : cards.getChildren()) {
            AnimationUtil.fadeSlideIn(child, ci * 80);
            ci++;
        }
        // Slide-in cho chart panel
        AnimationUtil.fadeSlideIn(chartPanel, 350);

        FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(220), this);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        // Load DB data off the JavaFX thread to avoid blocking the UI
        Thread dbThread = new Thread(() -> {
            int totalSP = 0, totalKH = 0, totalHD = 0;
            double doanhThu = 0;
            List<Map.Entry<String, Integer>> topProducts = null;
            
            try { totalSP = new SanPhamController().getAllSanPham().size(); } catch (Exception ignored) {}
            try { totalKH = new KhachHangController().getAllKhachHang().size(); } catch (Exception ignored) {}
            try {
                List<HoaDon> hds = new HoaDonController().getHoaDonHistory();
                totalHD = hds.size();
                doanhThu = hds.stream().mapToDouble(HoaDon::thanhTien).sum();
                
                Map<String, Integer> productSales = new HashMap<>();
                for (HoaDon hd : hds) {
                    productSales.put(hd.getTenSanPham(), productSales.getOrDefault(hd.getTenSanPham(), 0) + hd.getSoLuong());
                }
                topProducts = productSales.entrySet().stream()
                        .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                        .limit(10)
                        .collect(Collectors.toList());
            } catch (Exception ignored) {}

            final int fSP = totalSP, fKH = totalKH, fHD = totalHD;
            final double fDT = doanhThu;
            final List<Map.Entry<String, Integer>> fTop = topProducts;
            
            javafx.application.Platform.runLater(() -> {
                valSP.setText(String.valueOf(fSP));
                valKH.setText(String.valueOf(fKH));
                valHD.setText(String.valueOf(fHD));
                valDT.setText(formatMoney(fDT));
                // Pulse effect khi số liệu được cập nhật
                AnimationUtil.pulse(valSP);
                AnimationUtil.pulse(valKH);
                AnimationUtil.pulse(valHD);
                AnimationUtil.pulse(valDT);
                
                if (fTop != null && !fTop.isEmpty()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    for (Map.Entry<String, Integer> entry : fTop) {
                        String name = entry.getKey();
                        if (name.length() > 20) name = name.substring(0, 17) + "..."; // truncate long names
                        series.getData().add(new XYChart.Data<>(name, entry.getValue()));
                    }
                    barChart.getData().clear();
                    barChart.getData().add(series);
                }
            });
        });
        dbThread.setDaemon(true);
        dbThread.start();
    }

    private Label placeholder() {
        Label l = new Label("-");
        l.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
        return l;
    }

    private VBox statCard(String icon, String label, Label val,
                          String bgColor, String valueColor, String borderColor) {
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 28px;");

        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        val.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + valueColor + ";");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox card = new VBox(8, iconLbl, spacer, lbl, val);
        card.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 18;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        card.setMinHeight(120);
        card.setAlignment(Pos.TOP_LEFT);

        // Hover scale được bổ sung bởng ScaleTransition
        card.setOnMouseEntered(e -> AnimationUtil.scaleUp(card, 1.04));
        card.setOnMouseExited(e -> AnimationUtil.scaleDown(card));

        return card;
    }

    // Removed infoPanel as it is replaced by chart

    private ColumnConstraints cc() {
        ColumnConstraints c = new ColumnConstraints();
        c.setHgrow(Priority.ALWAYS);
        c.setPercentWidth(25);
        return c;
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB₫", amount / 1_000_000_000);
        if (amount >= 1_000_000)     return String.format("%.1fM₫", amount / 1_000_000);
        if (amount >= 1_000)         return String.format("%.0fK₫", amount / 1_000);
        return NumberFormat.getInstance(new Locale("vi", "VN")).format((long) amount) + "₫";
    }
}
