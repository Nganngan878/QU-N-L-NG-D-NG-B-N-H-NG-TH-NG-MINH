package com.example.QUANLYUNGDUNGBANHANG.dao;

import com.example.QUANLYUNGDUNGBANHANG.model.User;

public interface UserDAO {
    User login(String username, String hashedPassword);
    boolean insert(User user);
    User findByUsername(String username);
    boolean updateProfile(User user);
}
