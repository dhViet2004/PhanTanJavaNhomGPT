package entity;

import java.time.LocalDateTime;
public class InvoiceDetails implements java.io.Serializable {
    private String maHoaDon;
    private String tenKhachHang;
    private String giayTo;
    private String soDienThoai;
    private String diaChiKhachHang;
    private LocalDateTime ngayLap;
    private String tenNhanVien;
    private String maChucVu;
    private double tongTien;
    private double tienGiam;
    private double thanhToan;
    private String loaiHoaDon;
    
    // Constructor đầy đủ
    public InvoiceDetails(String maHoaDon, String tenKhachHang, String giayTo, String soDienThoai,
                         String diaChiKhachHang, LocalDateTime ngayLap, String tenNhanVien,
                         String maChucVu, double tongTien, double tienGiam, double thanhToan, String loaiHoaDon) {
        this.maHoaDon = maHoaDon;
        this.tenKhachHang = tenKhachHang;
        this.giayTo = giayTo;
        this.soDienThoai = soDienThoai;
        this.diaChiKhachHang = diaChiKhachHang;
        this.ngayLap = ngayLap;
        this.tenNhanVien = tenNhanVien;
        this.maChucVu = maChucVu;
        this.tongTien = tongTien;
        this.tienGiam = tienGiam;
        this.thanhToan = thanhToan;
        this.loaiHoaDon = loaiHoaDon;
    }

    // Các getter và setter
    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getGiayTo() {
        return giayTo;
    }

    public void setGiayTo(String giayTo) {
        this.giayTo = giayTo;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChiKhachHang() {
        return diaChiKhachHang;
    }

    public void setDiaChiKhachHang(String diaChiKhachHang) {
        this.diaChiKhachHang = diaChiKhachHang;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getTenNhanVien() {
        return tenNhanVien;
    }

    public void setTenNhanVien(String tenNhanVien) {
        this.tenNhanVien = tenNhanVien;
    }

    public String getMaChucVu() {
        return maChucVu;
    }

    public void setMaChucVu(String maChucVu) {
        this.maChucVu = maChucVu;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public double getTienGiam() {
        return tienGiam;
    }

    public void setTienGiam(double tienGiam) {
        this.tienGiam = tienGiam;
    }

    public double getThanhToan() {
        return thanhToan;
    }

    public void setThanhToan(double thanhToan) {
        this.thanhToan = thanhToan;
    }

    public String getLoaiHoaDon() {
        return loaiHoaDon;
    }

    public void setLoaiHoaDon(String loaiHoaDon) {
        this.loaiHoaDon = loaiHoaDon;
    }
}