package com.example.QUANLYUNGDUNGBANHANG.network;

import java.io.Serializable;

/**
 * Gói tin phản hồi từ Server trả về Client.
 * success: kết quả thành công hay thất bại
 * message: thông báo mô tả
 * data: dữ liệu trả về (List<DTO>, single DTO, Boolean...)
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private Object data; // có thể là List<SanPhamDTO>, UserDTO, Boolean, ...

    public Response() {}

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
