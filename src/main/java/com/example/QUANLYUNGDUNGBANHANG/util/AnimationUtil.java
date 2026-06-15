package com.example.QUANLYUNGDUNGBANHANG.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * AnimationUtil — tập hợp các animation mượt mà dùng chung toàn ứng dụng.
 */
public class AnimationUtil {

    /**
     * Fade + Slide vào từ dưới lên (dùng cho card, row, dialog).
     * @param node   Node cần animate
     * @param delayMs delay (ms) trước khi bắt đầu — dùng cho stagger effect
     */
    public static void fadeSlideIn(Node node, double delayMs) {
        node.setOpacity(0);
        node.setTranslateY(18);

        FadeTransition ft = new FadeTransition(Duration.millis(320), node);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition tt = new TranslateTransition(Duration.millis(320), node);
        tt.setFromY(18); tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
    }

    /**
     * Fade + Slide vào từ phải sang (dùng cho page navigation).
     */
    public static void fadeSlideInFromRight(Node node) {
        node.setOpacity(0);
        node.setTranslateX(30);

        FadeTransition ft = new FadeTransition(Duration.millis(280), node);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition tt = new TranslateTransition(Duration.millis(280), node);
        tt.setFromX(30); tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
    }

    /**
     * Scale bounce khi hover vào (dùng cho card, button).
     */
    public static void scaleUp(Node node, double toScale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setToX(toScale); st.setToY(toScale);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    /**
     * Scale trở về 1.0 khi hover ra.
     */
    public static void scaleDown(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
        st.setToX(1.0); st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    /**
     * Hiệu ứng rung (shake) khi nhập sai.
     */
    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(0); tt.setByX(9);
        tt.setCycleCount(6); tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    /**
     * Pulse (nhấp nháy scale) — dùng cho badge, icon thông báo.
     */
    public static void pulse(Node node) {
        ScaleTransition up = new ScaleTransition(Duration.millis(180), node);
        up.setToX(1.18); up.setToY(1.18);
        up.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition down = new ScaleTransition(Duration.millis(180), node);
        down.setToX(1.0); down.setToY(1.0);
        down.setInterpolator(Interpolator.EASE_IN);

        new SequentialTransition(up, down).play();
    }

    /**
     * Fade in đơn giản.
     */
    public static void fadeIn(Node node, double durationMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    /**
     * Stagger fade+slide cho danh sách nodes (mỗi item delay nhau 40ms).
     */
    public static void staggerFadeIn(Iterable<Node> nodes) {
        int i = 0;
        for (Node n : nodes) {
            fadeSlideIn(n, i * 40);
            i++;
        }
    }
}
