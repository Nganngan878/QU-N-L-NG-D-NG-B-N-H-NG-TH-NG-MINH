package com.example.QUANLYUNGDUNGBANHANG.util;

import com.example.QUANLYUNGDUNGBANHANG.model.KhachHang;
import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * XmlImportUtil — Phân tích (parse) chuỗi dữ liệu XML nhận được thành các đối tượng tương ứng.
 * Kiểm tra định dạng cấu trúc XML và ném ra ngoại lệ rõ ràng khi có lỗi.
 */
public class XmlImportUtil {

    /**
     * Parse danh sách sản phẩm từ nội dung chuỗi XML.
     */
    public static List<SanPham> parseSanPhamFromXmlString(String xmlContent) throws Exception {
        Document doc = parseXmlString(xmlContent);
        Element root = doc.getDocumentElement();

        if (!root.getTagName().equals("DanhSachSanPham")) {
            throw new Exception("File XML sai định dạng: root element phải là <DanhSachSanPham>, hiện tại là <" + root.getTagName() + ">");
        }

        List<SanPham> list = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("SanPham");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element spEl = (Element) nodes.item(i);

            String ma       = getRequired(spEl, "MaSP",      i + 1);
            String ten      = getRequired(spEl, "TenSP",     i + 1);
            String loai     = getText(spEl, "Loai",     "Khác");
            String giaNhap  = getText(spEl, "GiaNhap",  "0");
            int soLuong;
            try {
                soLuong = Integer.parseInt(getText(spEl, "SoLuongTon", "0").trim());
            } catch (NumberFormatException e) {
                throw new Exception("Dòng " + (i + 1) + ": SoLuongTon phải là số nguyên.");
            }
            String hinhAnh  = getText(spEl, "HinhAnh",  "");

            list.add(new SanPham(ma, ten, loai, giaNhap, soLuong, hinhAnh));
        }

        return list;
    }

    /**
     * Parse danh sách khách hàng từ nội dung chuỗi XML.
     */
    public static List<KhachHang> parseKhachHangFromXmlString(String xmlContent) throws Exception {
        Document doc = parseXmlString(xmlContent);
        Element root = doc.getDocumentElement();

        if (!root.getTagName().equals("DanhSachKhachHang")) {
            throw new Exception("File XML sai định dạng: root element phải là <DanhSachKhachHang>, hiện tại là <" + root.getTagName() + ">");
        }

        List<KhachHang> list = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("KhachHang");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element khEl = (Element) nodes.item(i);

            String maKH  = getRequired(khEl, "MaKH",       i + 1);
            String tenKH = getRequired(khEl, "TenKH",       i + 1);
            String ngay  = getText(khEl, "NgaySinh",   "");
            String sdt   = getText(khEl, "SoDienThoai","");
            String email = getText(khEl, "Email",      "");
            String dia   = getText(khEl, "DiaChi",     "");
            String loai  = getText(khEl, "LoaiKH",     "Thường");

            list.add(new KhachHang(maKH, tenKH, ngay, sdt, email, dia, loai));
        }

        return list;
    }

    private static Document parseXmlString(String xmlContent) throws Exception {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new Exception("Dữ liệu XML trống hoặc rỗng.");
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Chống XXE Injection (XML External Entity)
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xmlContent)));
    }

    private static String getRequired(Element parent, String tag, int rowNumber) throws Exception {
        String val = getText(parent, tag, null);
        if (val == null || val.trim().isEmpty()) {
            throw new Exception("Dòng " + rowNumber + ": Thiếu trường dữ liệu bắt buộc <" + tag + ">.");
        }
        return val.trim();
    }

    private static String getText(Element parent, String tag, String defaultValue) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return defaultValue;
        String text = nl.item(0).getTextContent();
        return (text != null && !text.trim().isEmpty()) ? text.trim() : defaultValue;
    }
}
