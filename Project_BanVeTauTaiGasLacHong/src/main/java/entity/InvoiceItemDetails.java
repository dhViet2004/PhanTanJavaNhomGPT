package entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class InvoiceItemDetails implements java.io.Serializable {
    private String maVe;
    private String tenHanhKhach;
    private String doiTuong;
    private String gaDi;
    private String gaDen;
    private String tau;
    private LocalDate ngayDi;
    private LocalTime gioDi;
    private String toa;
    private String choNgoi;
    private double giaVe;
    private double vat;
    private double thanhTien;
    
    // Constructor đầy đủ
    public InvoiceItemDetails(String maVe, String tenHanhKhach, String doiTuong, String gaDi, String gaDen,
                             String tau, LocalDate ngayDi, LocalTime gioDi, String toa, String choNgoi,
                             double giaVe, double vat, double thanhTien) {
        this.maVe = maVe;
        this.tenHanhKhach = tenHanhKhach;
        this.doiTuong = doiTuong;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.tau = tau;
        this.ngayDi = ngayDi;
        this.gioDi = gioDi;
        this.toa = toa;
        this.choNgoi = choNgoi;
        this.giaVe = giaVe;
        this.vat = vat;
        this.thanhTien = thanhTien;
    }

    // Các getter và setter
    public String getMaVe() {
        return maVe;
    }

    public void setMaVe(String maVe) {
        this.maVe = maVe;
    }

    public String getTenHanhKhach() {
        return tenHanhKhach;
    }

    public void setTenHanhKhach(String tenHanhKhach) {
        this.tenHanhKhach = tenHanhKhach;
    }

    public String getDoiTuong() {
        return doiTuong;
    }

    public void setDoiTuong(String doiTuong) {
        this.doiTuong = doiTuong;
    }

    public String getGaDi() {
        return gaDi;
    }

    public void setGaDi(String gaDi) {
        this.gaDi = gaDi;
    }

    public String getGaDen() {
        return gaDen;
    }

    public void setGaDen(String gaDen) {
        this.gaDen = gaDen;
    }

    public String getTau() {
        return tau;
    }

    public void setTau(String tau) {
        this.tau = tau;
    }

    public LocalDate getNgayDi() {
        return ngayDi;
    }

    public void setNgayDi(LocalDate ngayDi) {
        this.ngayDi = ngayDi;
    }

    public LocalTime getGioDi() {
        return gioDi;
    }

    public void setGioDi(LocalTime gioDi) {
        this.gioDi = gioDi;
    }

    public String getToa() {
        return toa;
    }

    public void setToa(String toa) {
        this.toa = toa;
    }

    public String getChoNgoi() {
        return choNgoi;
    }

    public void setChoNgoi(String choNgoi) {
        this.choNgoi = choNgoi;
    }

    public double getGiaVe() {
        return giaVe;
    }

    public void setGiaVe(double giaVe) {
        this.giaVe = giaVe;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }
}