package com.example.QUANLYUNGDUNGBANHANG.controller;

import com.example.QUANLYUNGDUNGBANHANG.network.Request;
import com.example.QUANLYUNGDUNGBANHANG.network.Response;
import com.example.QUANLYUNGDUNGBANHANG.network.SocketClient;
import com.example.QUANLYUNGDUNGBANHANG.network.dto.UserDTO;

/**
 * UserController — gửi yêu cầu LOGIN qua SocketClient thay vì gọi DAO trực tiếp.
 * Trả về UserDTO khi đăng nhập thành công (bao gồm role).
 */
public class UserController {

    /**
     * Xác thực đăng nhập.
     * @return UserDTO (chứa username và role) nếu thành công, null nếu sai.
     */
    public UserDTO login(String username, String password) {
        Request req = new Request("LOGIN");
        req.addParam("username", username);
        req.addParam("password", password);

        Response res = SocketClient.getInstance().sendRequest(req);
        if (res.isSuccess() && res.getData() instanceof UserDTO) {
            return (UserDTO) res.getData();
        }
        return null;
    }

    /**
     * Tương thích ngược với code cũ (chỉ trả true/false).
     */
    public boolean validateLogin(String username, String password) {
        return login(username, password) != null;
    }

    /**
     * Đăng ký tài khoản người dùng mới.
     * @return Response kết quả từ server (thành công/thất bại kèm thông báo)
     */
    public Response register(String username, String password) {
        Request req = new Request("REGISTER");
        req.addParam("username", username);
        req.addParam("password", password);
        return SocketClient.getInstance().sendRequest(req);
    }

    /**
     * Lấy hồ sơ người dùng.
     */
    public UserDTO getProfile(String username) {
        Request req = new Request("GET_PROFILE");
        req.addParam("username", username);
        Response res = SocketClient.getInstance().sendRequest(req);
        if (res.isSuccess() && res.getData() instanceof UserDTO) {
            return (UserDTO) res.getData();
        }
        return null;
    }

    /**
     * Cập nhật hồ sơ người dùng.
     */
    public boolean updateProfile(UserDTO dto) {
        Request req = new Request("UPDATE_PROFILE");
        req.setPayload(dto);
        Response res = SocketClient.getInstance().sendRequest(req);
        return res.isSuccess();
    }
}
