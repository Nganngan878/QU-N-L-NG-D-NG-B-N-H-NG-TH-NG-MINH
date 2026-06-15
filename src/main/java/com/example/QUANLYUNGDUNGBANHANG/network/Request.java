package com.example.QUANLYUNGDUNGBANHANG.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Gói tin yêu cầu từ Client gửi lên Server.
 * action: tên hành động (LOGIN, GET_SANPHAM, ADD_SANPHAM, ...)
 * params: các tham số dạng key-value string đơn giản
 * payload: dữ liệu đối tượng phức tạp (DTO) nếu cần
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private String action;
    private Map<String, String> params = new HashMap<>();
    private Object payload; // SanPhamDTO, KhachHangDTO, ...

    public Request() {}

    public Request(String action) {
        this.action = action;
    }

    public Request(String action, Map<String, String> params) {
        this.action = action;
        this.params = params;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Map<String, String> getParams() { return params; }
    public void setParams(Map<String, String> params) { this.params = params; }
    public void addParam(String key, String value) { this.params.put(key, value); }
    public String getParam(String key) { return params.getOrDefault(key, ""); }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}
