package com.example.QUANLYUNGDUNGBANHANG.network.dto;

import java.io.Serializable;

/** DTO trung gian — truyền dữ liệu User qua Socket. */
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String role;
    
    // Profile fields
    private String fullname;
    private String bio;
    private String gender;
    private String dob;
    private String phone;
    private String email;
    private String avatarUrl;

    public UserDTO() {}

    public UserDTO(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
