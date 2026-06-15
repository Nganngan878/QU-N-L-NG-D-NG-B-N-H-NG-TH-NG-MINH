package com.example.QUANLYUNGDUNGBANHANG.network;

import com.example.QUANLYUNGDUNGBANHANG.util.ConfigUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * SocketClient — Singleton trong ứng dụng Client JavaFX.
 * Mở một kết nối Socket đến Server và tái sử dụng suốt phiên làm việc.
 * Cung cấp phương thức sendRequest() để gửi Request và nhận Response từ Server.
 */
public class SocketClient {

    private static SocketClient instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private SocketClient() {
        connect();
    }

    public static synchronized SocketClient getInstance() {
        if (instance == null || instance.isClosed()) {
            instance = new SocketClient();
        }
        return instance;
    }

    private void connect() {
        try {
            String host = ConfigUtil.serverHost();
            int port = ConfigUtil.serverPort();
            socket = new Socket(host, port);
            // Tạo out trước để tránh deadlock
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in  = new ObjectInputStream(socket.getInputStream());
            System.out.println("[Client] Đã kết nối đến Server " + host + ":" + port);
        } catch (Exception e) {
            System.err.println("[Client] Không thể kết nối đến Server: " + e.getMessage());
            socket = null; out = null; in = null;
        }
    }

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }


    /**
     * Gửi một Request đến Server và nhận Response trả về.
     * Thread-safe: synchronized để tránh xung đột khi nhiều Controller gọi đồng thời.
     */
    public synchronized Response sendRequest(Request request) {
        if (isClosed()) {
            connect(); // Thử kết nối lại
        }
        if (isClosed()) {
            return new Response(false, "Không thể kết nối đến Server. Hãy kiểm tra Server đã khởi động chưa.");
        }
        try {
            out.writeObject(request);
            out.flush();
            out.reset(); // Xoá cache để tránh gửi object cũ
            return (Response) in.readObject();
        } catch (Exception e) {
            System.err.println("[Client] Lỗi khi gửi request: " + e.getMessage());
            // Đánh dấu socket lỗi để lần sau reconnect
            try { if (socket != null) socket.close(); } catch (Exception ignored) {}
            socket = null;
            return new Response(false, "Lỗi kết nối: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
