package com.example.QUANLYUNGDUNGBANHANG.server;

import com.example.QUANLYUNGDUNGBANHANG.dao.UserDAO;
import com.example.QUANLYUNGDUNGBANHANG.dao.impl.UserDAOImpl;
import com.example.QUANLYUNGDUNGBANHANG.model.User;
import com.example.QUANLYUNGDUNGBANHANG.util.PasswordUtil;

/**
 * AuthService — Dịch vụ xác thực tài khoản phía Server.
 * Sử dụng UserDAO để truy vấn DB và PasswordUtil để băm/so sánh mật khẩu.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();

    /**
     * Xác thực người dùng bằng username và mật khẩu gốc (plaintext).
     * @param username Tên đăng nhập
     * @param rawPassword Mật khẩu chưa băm nhận từ client
     * @return Đối tượng User nếu thành công, null nếu thất bại
     */
    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }
        // Băm mật khẩu người dùng gửi lên trước khi truy vấn/so sánh
        String hashedPw = PasswordUtil.hashPassword(rawPassword);
        try {
            return userDAO.login(username, hashedPw);
        } catch (Exception e) {
            System.err.println("[AuthService] Lỗi khi đăng nhập: " + e.getMessage());
            return null;
        }
    }

    /**
     * Đăng ký tài khoản mới cho người dùng.
     * @param username Tên đăng nhập
     * @param rawPassword Mật khẩu gốc chưa băm
     * @return true nếu đăng ký thành công, ngược lại trả về false hoặc quăng exception
     */
    public boolean register(String username, String rawPassword) throws Exception {
        if (username == null || rawPassword == null || username.trim().isEmpty() || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập và mật khẩu không được để trống!");
        }
        
        // Kiểm tra xem username đã tồn tại chưa
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trên hệ thống!");
        }
        
        // Băm mật khẩu bằng PasswordUtil
        String hashedPw = PasswordUtil.hashPassword(rawPassword);
        
        // Tạo đối tượng User mới với phân quyền mặc định là USER
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(hashedPw);
        newUser.setRole("USER");
        
        return userDAO.insert(newUser);
    }

    public User getProfile(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        return userDAO.findByUsername(username);
    }

    public boolean updateProfile(User user) {
        if (user == null || user.getUsername() == null) return false;
        return userDAO.updateProfile(user);
    }
}
