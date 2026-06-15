package com.example.QUANLYUNGDUNGBANHANG.server;

import com.example.QUANLYUNGDUNGBANHANG.dao.HoaDonDAO;
import com.example.QUANLYUNGDUNGBANHANG.dao.KhachHangDAO;
import com.example.QUANLYUNGDUNGBANHANG.dao.SanPhamDAO;
import com.example.QUANLYUNGDUNGBANHANG.dao.UserDAO;
import com.example.QUANLYUNGDUNGBANHANG.dao.impl.HoaDonDAOImpl;
import com.example.QUANLYUNGDUNGBANHANG.dao.impl.KhachHangDAOImpl;
import com.example.QUANLYUNGDUNGBANHANG.dao.impl.SanPhamDAOImpl;
import com.example.QUANLYUNGDUNGBANHANG.dao.impl.UserDAOImpl;
import com.example.QUANLYUNGDUNGBANHANG.model.HoaDon;
import com.example.QUANLYUNGDUNGBANHANG.model.KhachHang;
import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import com.example.QUANLYUNGDUNGBANHANG.model.User;
import com.example.QUANLYUNGDUNGBANHANG.network.Request;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.*;
import com.example.QUANLYUNGDUNGBANHANG.util.AESUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.DBConnection;
import com.example.QUANLYUNGDUNGBANHANG.util.LoggerUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.PasswordUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.XmlExportUtil;
import com.example.QUANLYUNGDUNGBANHANG.util.XmlImportUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * ClientHandler — Xử lý từng kết nối Client trong một Thread riêng.
 * Nhận Request, phân loại action, gọi DAO tương ứng và trả Response về Client.
 * Hỗ trợ:
 *  - Ghi log mọi hành động quan trọng (LoggerUtil)
 *  - Lưu thông tin user hiện tại (username, role) để phân quyền dữ liệu
 *  - AES masking SĐT/Email khi role là USER
 *  - Transaction CHECKOUT an toàn với rollback khi thất bại
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final SanPhamDAO  sanPhamDAO  = new SanPhamDAOImpl();
    private final KhachHangDAO khachHangDAO = new KhachHangDAOImpl();
    private final HoaDonDAO   hoaDonDAO   = new HoaDonDAOImpl();
    private final UserDAO     userDAO     = new UserDAOImpl();
    private final AuthService authService = new AuthService();

    /** Thông tin người dùng hiện tại (được set sau khi đăng nhập thành công) */
    private String currentUsername = "ANONYMOUS";
    private String currentRole     = "USER";

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        String clientAddr = clientSocket.getInetAddress().getHostAddress();
        try (
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream  in  = new ObjectInputStream(clientSocket.getInputStream())
        ) {
            out.flush();
            System.out.println("[Handler] Bắt đầu xử lý Client: " + clientAddr);

            // Vòng lặp: tiếp tục nhận request từ client cho đến khi ngắt kết nối
            while (!clientSocket.isClosed()) {
                Request req;
                try {
                    req = (Request) in.readObject();
                } catch (Exception e) {
                    System.out.println("[Handler] Client ngắt kết nối: " + clientAddr);
                    break;
                }

                System.out.println("[Handler] Nhận action: " + req.getAction() + " từ " + clientAddr);
                Response response = handleRequest(req, clientAddr);
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (Exception e) {
            System.err.println("[Handler] Lỗi xử lý Client " + clientAddr + ": " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (Exception ignored) {}
            System.out.println("[Handler] Đã đóng kết nối: " + clientAddr);
        }
    }

    /** Phân loại action và gọi handler tương ứng */
    private Response handleRequest(Request req, String clientAddr) {
        return switch (req.getAction()) {
            // === Authentication ===
            case "LOGIN"            -> handleLogin(req, clientAddr);
            case "REGISTER"         -> handleRegister(req, clientAddr);
            case "GET_PROFILE"      -> handleGetProfile(req);
            case "UPDATE_PROFILE"   -> handleUpdateProfile(req);

            // === SanPham ===
            case "GET_SANPHAM"      -> handleGetSanPham();
            case "ADD_SANPHAM"      -> handleAddSanPham(req);
            case "UPDATE_SANPHAM"   -> handleUpdateSanPham(req);
            case "DELETE_SANPHAM"   -> handleDeleteSanPham(req);

            // === KhachHang ===
            case "GET_KHACHHANG"    -> handleGetKhachHang();
            case "ADD_KHACHHANG"    -> handleAddKhachHang(req);
            case "UPDATE_KHACHHANG" -> handleUpdateKhachHang(req);
            case "DELETE_KHACHHANG" -> handleDeleteKhachHang(req);

            // === HoaDon ===
            case "GET_HOADON"       -> handleGetHoaDon();
            case "SAVE_HOADON"      -> handleSaveHoaDon(req);
            case "DELETE_HOADON"    -> handleDeleteHoaDon(req);

            // === Checkout (Transaction: lưu HĐ + trừ tồn kho, rollback nếu lỗi) ===
            case "CHECKOUT"         -> handleCheckout(req);

            // === Thống kê ===
            case "GET_THONGKE"      -> handleGetThongKe();
            
            // === XML Import/Export ===
            case "EXPORT_SANPHAM_XML"   -> handleExportSanPhamXml(req);
            case "EXPORT_KHACHHANG_XML" -> handleExportKhachHangXml(req);
            case "IMPORT_SANPHAM_XML"   -> handleImportSanPhamXml(req);
            case "IMPORT_KHACHHANG_XML" -> handleImportKhachHangXml(req);

            default -> new Response(false, "Action không được hỗ trợ: " + req.getAction());
        };
    }

    // ==================== AUTHENTICATION ====================
    private Response handleLogin(Request req, String ipAddress) {
        try {
            String username = req.getParam("username");
            String password = req.getParam("password");
            User user = authService.authenticate(username, password);
            if (user != null) {
                // Lưu thông tin user vào handler để dùng cho các request tiếp theo
                this.currentUsername = user.getUsername();
                this.currentRole     = user.getRole() != null ? user.getRole() : "USER";
                UserDTO dto = new UserDTO(user.getId(), user.getUsername(), user.getRole());
                LoggerUtil.loginSuccess(username, user.getRole(), ipAddress);
                return new Response(true, "Đăng nhập thành công!", dto);
            } else {
                LoggerUtil.loginFailed(username, ipAddress);
                return new Response(false, "Sai tài khoản hoặc mật khẩu!");
            }
        } catch (Exception e) {
            return new Response(false, "Lỗi xác thực: " + e.getMessage());
        }
    }

    private Response handleRegister(Request req, String ipAddress) {
        try {
            String username = req.getParam("username");
            String password = req.getParam("password");
            boolean success = authService.register(username, password);
            if (success) {
                LoggerUtil.info("SYSTEM", "REGISTER", "Đăng ký thành công tài khoản: " + username + " từ IP " + ipAddress);
                return new Response(true, "Đăng ký tài khoản thành công!");
            } else {
                return new Response(false, "Đăng ký tài khoản thất bại!");
            }
        } catch (IllegalArgumentException e) {
            return new Response(false, e.getMessage());
        } catch (Exception e) {
            LoggerUtil.error("SYSTEM", "REGISTER", "Lỗi đăng ký tài khoản: " + e.getMessage());
            return new Response(false, "Lỗi đăng ký tài khoản: " + e.getMessage());
        }
    }

    private Response handleGetProfile(Request req) {
        try {
            String username = req.getParam("username");
            if (username == null) username = currentUsername; // Mặc định lấy của người dùng hiện tại
            User user = authService.getProfile(username);
            if (user != null) {
                UserDTO dto = new UserDTO(user.getId(), user.getUsername(), user.getRole());
                dto.setFullname(user.getFullname());
                dto.setBio(user.getBio());
                dto.setGender(user.getGender());
                dto.setDob(user.getDob());
                dto.setPhone(user.getPhone());
                dto.setEmail(user.getEmail());
                dto.setAvatarUrl(user.getAvatarUrl());
                return new Response(true, "Lấy hồ sơ thành công", dto);
            }
            return new Response(false, "Không tìm thấy người dùng");
        } catch (Exception e) {
            return new Response(false, "Lỗi lấy hồ sơ: " + e.getMessage());
        }
    }

    private Response handleUpdateProfile(Request req) {
        try {
            UserDTO dto = (UserDTO) req.getPayload();
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setFullname(dto.getFullname());
            user.setBio(dto.getBio());
            user.setGender(dto.getGender());
            user.setDob(dto.getDob());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setAvatarUrl(dto.getAvatarUrl());

            boolean ok = authService.updateProfile(user);
            if (ok) {
                LoggerUtil.info(currentUsername, "UPDATE_PROFILE", "Đã cập nhật hồ sơ");
                return new Response(true, "Cập nhật hồ sơ thành công!");
            }
            return new Response(false, "Lỗi cập nhật hồ sơ");
        } catch (Exception e) {
            return new Response(false, "Lỗi cập nhật hồ sơ: " + e.getMessage());
        }
    }

    // ==================== SANPHAM ====================
    private Response handleGetSanPham() {
        try {
            List<SanPham> list = sanPhamDAO.findAll();
            List<SanPhamDTO> dtos = new ArrayList<>();
            for (SanPham sp : list) {
                dtos.add(new SanPhamDTO(sp.getMa(), sp.getTen(), sp.getLoai(),
                        sp.getGiaNhap(), sp.getSoLuongTon(), sp.getHinhAnh()));
            }
            return new Response(true, "OK", dtos);
        } catch (Exception e) {
            return new Response(false, "Lỗi tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private Response handleAddSanPham(Request req) {
        try {
            SanPhamDTO dto = (SanPhamDTO) req.getPayload();
            SanPham sp = new SanPham(dto.getMa(), dto.getTen(), dto.getLoai(),
                    dto.getGiaNhap(), dto.getSoLuongTon(), dto.getHinhAnh());
            boolean ok = sanPhamDAO.insert(sp);
            if (ok) LoggerUtil.info(currentUsername, "ADD_SANPHAM", "Mã=" + dto.getMa() + " | Tên=" + dto.getTen());
            else    LoggerUtil.warn(currentUsername, "ADD_SANPHAM", "Thất bại: mã=" + dto.getMa());
            return new Response(ok, ok ? "Thêm sản phẩm thành công!" : "Lỗi: không thể thêm (mã trùng?)");
        } catch (Exception e) {
            return new Response(false, "Lỗi thêm sản phẩm: " + e.getMessage());
        }
    }

    private Response handleUpdateSanPham(Request req) {
        try {
            SanPhamDTO dto = (SanPhamDTO) req.getPayload();
            SanPham sp = new SanPham(dto.getMa(), dto.getTen(), dto.getLoai(),
                    dto.getGiaNhap(), dto.getSoLuongTon(), dto.getHinhAnh());
            boolean ok = sanPhamDAO.update(sp);
            if (ok) LoggerUtil.info(currentUsername, "UPDATE_SANPHAM", "Mã=" + dto.getMa());
            return new Response(ok, ok ? "Cập nhật sản phẩm thành công!" : "Lỗi cập nhật.");
        } catch (Exception e) {
            return new Response(false, "Lỗi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    private Response handleDeleteSanPham(Request req) {
        try {
            String ma = req.getParam("ma");
            boolean ok = sanPhamDAO.delete(ma);
            if (ok) LoggerUtil.info(currentUsername, "DELETE_SANPHAM", "Mã=" + ma);
            else    LoggerUtil.warn(currentUsername, "DELETE_SANPHAM", "Thất bại: mã=" + ma);
            return new Response(ok, ok ? "Xóa sản phẩm thành công!" : "Lỗi xóa sản phẩm.");
        } catch (Exception e) {
            return new Response(false, "Lỗi xóa sản phẩm: " + e.getMessage());
        }
    }

    // ==================== KHACHHANG ====================
    private Response handleGetKhachHang() {
        try {
            List<KhachHang> list = khachHangDAO.findAll();
            List<KhachHangDTO> dtos = new ArrayList<>();
            boolean isUser = "USER".equalsIgnoreCase(currentRole);
            for (KhachHang kh : list) {
                String sdt   = kh.getSoDienThoai();
                String email = kh.getEmail();
                // Masking thông tin nhạy cảm nếu là USER
                if (isUser) {
                    sdt   = AESUtil.mask(sdt);
                    email = AESUtil.mask(email);
                }
                dtos.add(new KhachHangDTO(kh.getMaKH(), kh.getTenKH(), kh.getNgaySinh(),
                        sdt, email, kh.getDiaChi(), kh.getLoaiKH()));
            }
            return new Response(true, "OK", dtos);
        } catch (Exception e) {
            return new Response(false, "Lỗi tải danh sách khách hàng: " + e.getMessage());
        }
    }

    private Response handleAddKhachHang(Request req) {
        try {
            KhachHangDTO dto = (KhachHangDTO) req.getPayload();
            KhachHang kh = new KhachHang(dto.getMaKH(), dto.getTenKH(), dto.getNgaySinh(),
                    dto.getSoDienThoai(), dto.getEmail(), dto.getDiaChi(), dto.getLoaiKH());
            boolean ok = khachHangDAO.insert(kh);
            if (ok) LoggerUtil.info(currentUsername, "ADD_KHACHHANG", "Mã=" + dto.getMaKH() + " | Tên=" + dto.getTenKH());
            else    LoggerUtil.warn(currentUsername, "ADD_KHACHHANG", "Thất bại: mã=" + dto.getMaKH());
            return new Response(ok, ok ? "Thêm khách hàng thành công!" : "Lỗi: không thể thêm (mã trùng?)");
        } catch (Exception e) {
            return new Response(false, "Lỗi thêm khách hàng: " + e.getMessage());
        }
    }

    private Response handleUpdateKhachHang(Request req) {
        try {
            KhachHangDTO dto = (KhachHangDTO) req.getPayload();
            KhachHang kh = new KhachHang(dto.getMaKH(), dto.getTenKH(), dto.getNgaySinh(),
                    dto.getSoDienThoai(), dto.getEmail(), dto.getDiaChi(), dto.getLoaiKH());
            boolean ok = khachHangDAO.update(kh);
            if (ok) LoggerUtil.info(currentUsername, "UPDATE_KHACHHANG", "Mã=" + dto.getMaKH());
            return new Response(ok, ok ? "Cập nhật khách hàng thành công!" : "Lỗi cập nhật.");
        } catch (Exception e) {
            return new Response(false, "Lỗi cập nhật khách hàng: " + e.getMessage());
        }
    }

    private Response handleDeleteKhachHang(Request req) {
        try {
            String ma = req.getParam("ma");
            boolean ok = khachHangDAO.delete(ma);
            if (ok) LoggerUtil.info(currentUsername, "DELETE_KHACHHANG", "Mã=" + ma);
            else    LoggerUtil.warn(currentUsername, "DELETE_KHACHHANG", "Thất bại: mã=" + ma);
            return new Response(ok, ok ? "Xóa khách hàng thành công!" : "Lỗi xóa khách hàng.");
        } catch (Exception e) {
            return new Response(false, "Lỗi xóa khách hàng: " + e.getMessage());
        }
    }

    // ==================== HOADON ====================
    private Response handleGetHoaDon() {
        try {
            List<HoaDon> list;
            if ("ADMIN".equalsIgnoreCase(currentRole)) {
                list = hoaDonDAO.findAll();
            } else {
                list = hoaDonDAO.findByCreatedBy(currentUsername);
            }
            List<HoaDonDTO> dtos = new ArrayList<>();
            for (HoaDon hd : list) {
                HoaDonDTO dto = new HoaDonDTO(hd.getMaHD(), hd.getNgay(), hd.getKhachHang(),
                        hd.getSanPham(), hd.getTenSanPham(), hd.getSoLuong(),
                        hd.getDonGia(), hd.getTongTien());
                dto.setGiamGia(hd.getGiamGia());
                dto.setSdt(hd.getSoDienThoai());
                dto.setLoaiKH(hd.getLoaiKH());
                dto.setTenKhachHang(hd.getTenKhachHang());
                dtos.add(dto);
            }
            return new Response(true, "OK", dtos);
        } catch (Exception e) {
            return new Response(false, "Lỗi tải danh sách hóa đơn: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Response handleSaveHoaDon(Request req) {
        try {
            List<HoaDonDTO> dtoList = (List<HoaDonDTO>) req.getPayload();
            List<HoaDon> hdList = new ArrayList<>();
            for (HoaDonDTO dto : dtoList) {
                hdList.add(new HoaDon(dto.getMaHD(), dto.getNgay(), dto.getKhachHang(),
                        dto.getSanPham(), dto.getTenSanPham(), dto.getSoLuong(),
                        dto.getDonGia(), dto.getTongTien()));
            }
            boolean ok = hoaDonDAO.insertAll(hdList);
            if (ok) LoggerUtil.info(currentUsername, "SAVE_HOADON", "Số lượng=" + hdList.size());
            return new Response(ok, ok ? "Lưu hóa đơn thành công!" : "Lỗi lưu hóa đơn.");
        } catch (Exception e) {
            return new Response(false, "Lỗi lưu hóa đơn: " + e.getMessage());
        }
    }

    private Response handleDeleteHoaDon(Request req) {
        try {
            String ma = req.getParam("ma");
            boolean ok = hoaDonDAO.delete(ma);
            if (ok) LoggerUtil.info(currentUsername, "DELETE_HOADON", "Mã=" + ma);
            return new Response(ok, ok ? "Xóa hóa đơn thành công!" : "Lỗi xóa hóa đơn.");
        } catch (Exception e) {
            return new Response(false, "Lỗi xóa hóa đơn: " + e.getMessage());
        }
    }

    // ==================== CHECKOUT (TRANSACTION) ====================
    /**
     * Xử lý thanh toán trong một Database Transaction duy nhất.
     * Đồng thời INSERT hóa đơn và UPDATE trừ số lượng tồn kho sản phẩm.
     * Nếu bất kỳ bước nào thất bại → ROLLBACK toàn bộ.
     *
     * Payload: List<HoaDonDTO> (mỗi DTO là 1 dòng hóa đơn)
     */
    @SuppressWarnings("unchecked")
    private Response handleCheckout(Request req) {
        List<HoaDonDTO> dtoList;
        try {
            dtoList = (List<HoaDonDTO>) req.getPayload();
            if (dtoList == null || dtoList.isEmpty()) {
                return new Response(false, "Giỏ hàng rỗng!");
            }
        } catch (Exception e) {
            return new Response(false, "Lỗi đọc dữ liệu thanh toán: " + e.getMessage());
        }

        String maHD = dtoList.get(0).getMaHD(); // Tất cả dòng chung 1 mã HĐ

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction
            try {
                // === Bước 1: Kiểm tra số lượng tồn kho ===
                for (HoaDonDTO dto : dtoList) {
                    PreparedStatement psCheck = conn.prepareStatement(
                        "SELECT soluongton FROM sanpham WHERE masp = ?");
                    psCheck.setString(1, dto.getSanPham());
                    var rs = psCheck.executeQuery();
                    if (rs.next()) {
                        int tonKho = rs.getInt("soluongton");
                        if (tonKho < dto.getSoLuong()) {
                            conn.rollback();
                            return new Response(false,
                                "Sản phẩm '" + dto.getTenSanPham() + "' không đủ tồn kho! " +
                                "(Kho còn: " + tonKho + ", yêu cầu: " + dto.getSoLuong() + ")");
                        }
                    } else {
                        conn.rollback();
                        return new Response(false, "Không tìm thấy sản phẩm: " + dto.getSanPham());
                    }
                    psCheck.close();
                }

                // === Bước 2: INSERT hóa đơn ===
                String sqlInsert = "INSERT INTO hoadon (mahd, ngay, makh, masp, soluong, dongia, giamgia, tongtien, created_by) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
                for (HoaDonDTO dto : dtoList) {
                    psInsert.setString(1, dto.getMaHD());
                    psInsert.setString(2, dto.getNgay());
                    psInsert.setString(3, dto.getKhachHang());
                    psInsert.setString(4, dto.getSanPham());
                    psInsert.setInt(5, dto.getSoLuong());
                    psInsert.setDouble(6, dto.getDonGia());
                    psInsert.setDouble(7, dto.getGiamGia());
                    psInsert.setDouble(8, dto.getTongTien());
                    psInsert.setString(9, currentUsername);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
                psInsert.close();

                // === Bước 3: UPDATE trừ số lượng tồn kho ===
                String sqlUpdate = "UPDATE sanpham SET soluongton = soluongton - ? WHERE masp = ?";
                PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate);
                for (HoaDonDTO dto : dtoList) {
                    psUpdate.setInt(1, dto.getSoLuong());
                    psUpdate.setString(2, dto.getSanPham());
                    psUpdate.addBatch();
                }
                psUpdate.executeBatch();
                psUpdate.close();

                // === Commit nếu tất cả thành công ===
                conn.commit();
                double tongTien = dtoList.stream().mapToDouble(HoaDonDTO::getTongTien).sum();
                LoggerUtil.info(currentUsername, "CHECKOUT",
                    "MaHD=" + maHD + " | Số SP=" + dtoList.size() +
                    " | TổngTiền=" + String.format("%.0f", tongTien));
                return new Response(true, "Thanh toán thành công!");

            } catch (Exception ex) {
                conn.rollback();
                LoggerUtil.error(currentUsername, "CHECKOUT_ROLLBACK",
                    "MaHD=" + maHD + " | Lỗi=" + ex.getMessage());
                return new Response(false, "Thanh toán thất bại, đã rollback: " + ex.getMessage());
            }

        } catch (Exception e) {
            return new Response(false, "Lỗi kết nối Database khi thanh toán: " + e.getMessage());
        }
    }

    // ==================== THONG KE ====================
    private Response handleGetThongKe() {
        try {
            int tongSP = sanPhamDAO.findAll().size();
            int tongKH = khachHangDAO.findAll().size();
            double doanhThu = hoaDonDAO.findAll().stream().mapToDouble(HoaDon::getTongTien).sum();
            int tongHD = hoaDonDAO.findAll().size();

            java.util.Map<String, String> result = new java.util.HashMap<>();
            result.put("tongSanPham", String.valueOf(tongSP));
            result.put("tongKhachHang", String.valueOf(tongKH));
            result.put("doanhThu", String.valueOf(doanhThu));
            result.put("tongHoaDon", String.valueOf(tongHD));
            return new Response(true, "OK", result);
        } catch (Exception e) {
            return new Response(false, "Lỗi tải thống kê: " + e.getMessage());
        }
    }

    // ==================== XML IMPORT / EXPORT (Bước 9) ====================
    private Response handleExportSanPhamXml(Request req) {
        try {
            String filePathStr = req.getParam("filePath");
            List<SanPham> list = sanPhamDAO.findAll();
            String xmlContent = XmlExportUtil.exportSanPhamToXmlString(list);

            // Ghi file phía Server ở thư mục exports/
            java.io.File exportsDir = new java.io.File("exports");
            if (!exportsDir.exists()) exportsDir.mkdirs();
            java.io.File serverFile = new java.io.File(exportsDir, "sanpham.xml");
            XmlExportUtil.writeStringToLocalFile(xmlContent, serverFile);

            // Ghi ra file chỉ định nếu có (hỗ trợ lưu trực tiếp trên máy client khi chạy cùng host)
            if (filePathStr != null && !filePathStr.trim().isEmpty()) {
                XmlExportUtil.writeStringToLocalFile(xmlContent, new java.io.File(filePathStr));
            }

            LoggerUtil.info(currentUsername, "EXPORT_SANPHAM_XML", "Số lượng=" + list.size() + " | File=" + serverFile.getAbsolutePath());
            return new Response(true, "Xuất XML thành công! File lưu trên Server: " + serverFile.getName());
        } catch (Exception e) {
            return new Response(false, "Lỗi xuất XML: " + e.getMessage());
        }
    }

    private Response handleExportKhachHangXml(Request req) {
        try {
            String filePathStr = req.getParam("filePath");
            List<KhachHang> list = khachHangDAO.findAll();
            String xmlContent = XmlExportUtil.exportKhachHangToXmlString(list);

            // Ghi file phía Server
            java.io.File exportsDir = new java.io.File("exports");
            if (!exportsDir.exists()) exportsDir.mkdirs();
            java.io.File serverFile = new java.io.File(exportsDir, "khachhang.xml");
            XmlExportUtil.writeStringToLocalFile(xmlContent, serverFile);

            if (filePathStr != null && !filePathStr.trim().isEmpty()) {
                XmlExportUtil.writeStringToLocalFile(xmlContent, new java.io.File(filePathStr));
            }

            LoggerUtil.info(currentUsername, "EXPORT_KHACHHANG_XML", "Số lượng=" + list.size() + " | File=" + serverFile.getAbsolutePath());
            return new Response(true, "Xuất XML thành công! File lưu trên Server: " + serverFile.getName());
        } catch (Exception e) {
            return new Response(false, "Lỗi xuất XML: " + e.getMessage());
        }
    }

    private Response handleImportSanPhamXml(Request req) {
        try {
            String xmlContent = (String) req.getPayload();
            List<SanPham> imported = XmlImportUtil.parseSanPhamFromXmlString(xmlContent);
            if (imported.isEmpty()) {
                return new Response(false, "Dữ liệu XML trống.");
            }

            int ok = 0, skip = 0;
            for (SanPham sp : imported) {
                // Kiểm tra trùng lặp hoặc thêm vào database
                if (sanPhamDAO.insert(sp)) {
                    ok++;
                } else {
                    skip++;
                }
            }

            LoggerUtil.info(currentUsername, "IMPORT_SANPHAM_XML", "Thêm mới=" + ok + " | Bỏ qua=" + skip);
            java.util.Map<String, Integer> resMap = new java.util.HashMap<>();
            resMap.put("ok", ok);
            resMap.put("skip", skip);
            return new Response(true, "Nhập XML thành công!", resMap);
        } catch (Exception e) {
            LoggerUtil.error(currentUsername, "IMPORT_SANPHAM_XML_ERROR", e.getMessage());
            return new Response(false, "Lỗi nhập XML: " + e.getMessage());
        }
    }

    private Response handleImportKhachHangXml(Request req) {
        try {
            String xmlContent = (String) req.getPayload();
            List<KhachHang> imported = XmlImportUtil.parseKhachHangFromXmlString(xmlContent);
            if (imported.isEmpty()) {
                return new Response(false, "Dữ liệu XML trống.");
            }

            int ok = 0, skip = 0;
            for (KhachHang kh : imported) {
                if (khachHangDAO.insert(kh)) {
                    ok++;
                } else {
                    skip++;
                }
            }

            LoggerUtil.info(currentUsername, "IMPORT_KHACHHANG_XML", "Thêm mới=" + ok + " | Bỏ qua=" + skip);
            java.util.Map<String, Integer> resMap = new java.util.HashMap<>();
            resMap.put("ok", ok);
            resMap.put("skip", skip);
            return new Response(true, "Nhập XML thành công!", resMap);
        } catch (Exception e) {
            LoggerUtil.error(currentUsername, "IMPORT_KHACHHANG_XML_ERROR", e.getMessage());
            return new Response(false, "Lỗi nhập XML: " + e.getMessage());
        }
    }
}
