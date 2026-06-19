package com.example.QUANLYUNGDUNGBANHANG.dao;

import com.example.QUANLYUNGDUNGBANHANG.model.User;
import java.util.List;

public interface UserDAO {
    User login(String username, String hashedPassword);
    boolean insert(User user);
    User findByUsername(String username);
    boolean updateProfile(User user);

    /** Lấy toàn bộ danh sách tài khoản (Admin only) */
    List<User> findAll();

    /** Xóa tài khoản theo username */
    boolean deleteByUsername(String username);

    /** Đổi role tài khoản */
    boolean updateRole(String username, String newRole);

    /** Đổi mật khẩu (Admin đặt lại) */
    boolean updatePassword(String username, String hashedPassword);
}
