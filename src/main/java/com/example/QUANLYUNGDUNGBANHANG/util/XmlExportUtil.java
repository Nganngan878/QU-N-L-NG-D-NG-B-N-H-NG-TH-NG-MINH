package com.example.QUANLYUNGDUNGBANHANG.util;

import com.example.QUANLYUNGDUNGBANHANG.model.KhachHang;
import com.example.QUANLYUNGDUNGBANHANG.model.SanPham;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * XmlExportUtil — Hỗ trợ xuất danh sách đối tượng thành chuỗi XML ( UTF-8 )
 * và hỗ trợ lưu trữ vào file vật lý.
 */
public class XmlExportUtil {

    /**
     * Chuyển danh sách sản phẩm thành chuỗi XML.
     */
    public static String exportSanPhamToXmlString(List<SanPham> list) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element root = doc.createElement("DanhSachSanPham");
        doc.appendChild(root);

        for (SanPham sp : list) {
            Element spEl = doc.createElement("SanPham");

            appendElement(doc, spEl, "MaSP",      sp.getMa());
            appendElement(doc, spEl, "TenSP",     sp.getTen());
            appendElement(doc, spEl, "Loai",      sp.getLoai());
            appendElement(doc, spEl, "GiaNhap",   sp.getGiaNhap());
            appendElement(doc, spEl, "SoLuongTon",String.valueOf(sp.getSoLuongTon()));
            appendElement(doc, spEl, "HinhAnh",   sp.getHinhAnh() != null ? sp.getHinhAnh() : "");

            root.appendChild(spEl);
        }

        return convertDocumentToString(doc);
    }

    /**
     * Chuyển danh sách khách hàng thành chuỗi XML.
     */
    public static String exportKhachHangToXmlString(List<KhachHang> list) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element root = doc.createElement("DanhSachKhachHang");
        doc.appendChild(root);

        for (KhachHang kh : list) {
            Element khEl = doc.createElement("KhachHang");

            appendElement(doc, khEl, "MaKH",       kh.getMaKH());
            appendElement(doc, khEl, "TenKH",      kh.getTenKH());
            appendElement(doc, khEl, "NgaySinh",   kh.getNgaySinh());
            appendElement(doc, khEl, "SoDienThoai",kh.getSoDienThoai());
            appendElement(doc, khEl, "Email",      kh.getEmail());
            appendElement(doc, khEl, "DiaChi",     kh.getDiaChi());
            appendElement(doc, khEl, "LoaiKH",     kh.getLoaiKH());

            root.appendChild(khEl);
        }

        return convertDocumentToString(doc);
    }

    /**
     * Ghi nội dung chuỗi vào một file vật lý (UTF-8).
     */
    public static void writeStringToLocalFile(String content, File file) throws Exception {
        // Tạo thư mục cha nếu chưa tồn tại
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(content);
        }
    }

    private static void appendElement(Document doc, Element parent, String tag, String value) {
        Element el = doc.createElement(tag);
        el.setTextContent(value != null ? value : "");
        parent.appendChild(el);
    }

    private static String convertDocumentToString(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
