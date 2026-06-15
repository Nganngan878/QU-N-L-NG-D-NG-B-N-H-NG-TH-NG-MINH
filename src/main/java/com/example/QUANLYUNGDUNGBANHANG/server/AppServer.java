package com.example.QUANLYUNGDUNGBANHANG.server;

import com.example.QUANLYUNGDUNGBANHANG.util.ConfigUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.DBConnection;
import com.example.QUANLYUNGDUNGBANHANG.util.LoggerUtil;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AppServer — Ứng dụng Server chạy độc lập.
 * - Kết nối cơ sở dữ liệu khi khởi động.
 * - Lắng nghe kết nối từ Client tại cổng 8888.
 * - Dùng ThreadPool (10 luồng) để xử lý đồng thời nhiều Client.
 *
 * Cách chạy: Chạy main() trong class này TRƯỚC khi khởi động ứng dụng JavaFX Client.
 */
public class AppServer {

    public static void main(String[] args) {
        int PORT = ConfigUtil.serverPort();
        int THREAD_POOL_SIZE = 10;
        System.out.println("====================================");
        System.out.println("  🛒 QUANLY BAN HANG — APP SERVER  ");
        System.out.println("====================================");

        // Bước 1: Khởi tạo và kiểm tra kết nối database
        System.out.println("[Server] Đang kết nối Database...");
        DBConnection.setupDatabase();
        System.out.println("[Server] Database sẵn sàng!");
        LoggerUtil.info("SYSTEM", "SERVER_START", "Server khởi động tại cổng " + PORT);

        // Bước 2: Tạo ThreadPool để xử lý đa luồng
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        System.out.println("[Server] ThreadPool tạo thành công (" + THREAD_POOL_SIZE + " luồng).");

        // Bước 3: Khởi động ServerSocket và lắng nghe
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[Server] Đang lắng nghe tại cổng " + PORT + "...");
            System.out.println("[Server] Nhấn Ctrl+C để dừng Server.\n");

            // Vòng lặp chính: chấp nhận kết nối liên tục
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    System.out.println("[Server] ✅ Client kết nối từ: " + clientAddress
                        + " | Tổng luồng đang chạy: " + Thread.activeCount());
                    LoggerUtil.info("SYSTEM", "CLIENT_CONNECTED", "IP=" + clientAddress);

                    // Giao mỗi Client cho một Thread trong Pool xử lý
                    executor.submit(new ClientHandler(clientSocket));

                } catch (Exception e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("[Server] Lỗi chấp nhận kết nối: " + e.getMessage());
                        LoggerUtil.error("SYSTEM", "ACCEPT_ERROR", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Server] ❌ Lỗi khởi động Server tại cổng " + PORT + ": " + e.getMessage());
            System.err.println("[Server] Kiểm tra xem cổng " + PORT + " có đang bị dùng bởi ứng dụng khác không.");
        } finally {
            executor.shutdownNow();
            System.out.println("[Server] Server đã dừng.");
        }
    }
}
