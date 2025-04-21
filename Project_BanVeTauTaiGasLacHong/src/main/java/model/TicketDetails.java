package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class TicketDetails implements Serializable {
    // Class để lưu thông tin chi tiết vé
        private String hoTen;
        private String doiTuong;
        private String giayTo;
        private String gaDi;
        private String gaDen;
        private String tenTau;
        private LocalDate ngayDi;
        private LocalTime gioDi;
        private String toaTau;
        private String choNgoi;
        private double giaVe;
        private double thanhTien;

        // Constructor và getter/setter

        public TicketDetails(String hoTen, String doiTuong, String giayTo, String gaDi, String gaDen,
                             String tenTau, LocalDate ngayDi, LocalTime gioDi, String toaTau, String choNgoi,
                             double giaVe, double thanhTien) {
            this.hoTen = hoTen;
            this.doiTuong = doiTuong;
            this.giayTo = giayTo;
            this.gaDi = gaDi;
            this.gaDen = gaDen;
            this.tenTau = tenTau;
            this.ngayDi = ngayDi;
            this.gioDi = gioDi;
            this.toaTau = toaTau;
            this.choNgoi = choNgoi;
            this.giaVe = giaVe;
            this.thanhTien = thanhTien;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getDoiTuong() {
            return doiTuong;
        }

        public String getGiayTo() {
            return giayTo;
        }

        public String getGaDi() {
            return gaDi;
        }

        public String getGaDen() {
            return gaDen;
        }

        public String getTenTau() {
            return tenTau;
        }

        public LocalDate getNgayDi() {
            return ngayDi;
        }

        public LocalTime getGioDi() {
            return gioDi;
        }

        public String getToaTau() {
            return toaTau;
        }

        public String getChoNgoi() {
            return choNgoi;
        }

        public double getGiaVe() {
            return giaVe;
        }

        public double getThanhTien() {
            return thanhTien;
        }

}
