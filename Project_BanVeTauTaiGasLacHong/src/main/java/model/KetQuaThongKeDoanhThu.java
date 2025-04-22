package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: KetQuaThongKeDoanhThu
 * @Tạo vào ngày: 21/04/2025
 * @Tác giả: Nguyen Huu Sang
 */

/**
 * Class lưu trữ kết quả thống kê doanh thu
 */
public class KetQuaThongKeDoanhThu implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate thoiGian;
    private double doanhThu;
    private String tenTuyen;
    private String loaiToa;
    private String loaiVe;

    public KetQuaThongKeDoanhThu() {
    }

    public KetQuaThongKeDoanhThu(LocalDate thoiGian, double doanhThu, String tenTuyen, String loaiToa, String loaiVe) {
        this.thoiGian = thoiGian;
        this.doanhThu = doanhThu;
        this.tenTuyen = tenTuyen;
        this.loaiToa = loaiToa;
        this.loaiVe = loaiVe;
    }

    public LocalDate getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDate thoiGian) {
        this.thoiGian = thoiGian;
    }

    public double getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(double doanhThu) {
        this.doanhThu = doanhThu;
    }

    public String getTenTuyen() {
        return tenTuyen;
    }

    public void setTenTuyen(String tenTuyen) {
        this.tenTuyen = tenTuyen;
    }

    public String getLoaiToa() {
        return loaiToa;
    }

    public void setLoaiToa(String loaiToa) {
        this.loaiToa = loaiToa;
    }

    public String getLoaiVe() {
        return loaiVe;
    }

    public void setLoaiVe(String loaiVe) {
        this.loaiVe = loaiVe;
    }
}