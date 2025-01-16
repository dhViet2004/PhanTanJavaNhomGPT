package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;
/**
 * @Dự án: App
 * @Class: NhanVien
 * @Tạo vào ngày: 15/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "nhanvien")
public class NhanVien {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_nv", columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String maNV; // Mã nhân viên

    @Column(name = "ten_nv", columnDefinition = "varchar(255)", nullable = false)
    private String tenNV; // Tên nhân viên

    @Column(name = "so_dt", columnDefinition = "varchar(255)", nullable = false)
    private String soDT; // Số điện thoại

    @Column(name = "trang_thai", columnDefinition = "varchar(255)", nullable = false)
    private String trangThai; // Trạng thái

    @Column(name = "cccd", columnDefinition = "varchar(255)", nullable = false)
    private String cccd; // Chứng chỉ công dân

    @Column(name = "dia_chi", columnDefinition = "varchar(255)", nullable = false)
    private String diaChi; // Địa chỉ

    @Column(name = "ngay_vao_lam", columnDefinition = "date", nullable = false)
    private LocalDate ngayVaoLam; // Ngày vào làm

    @Column(name = "chuc_vu", columnDefinition = "varchar(255)", nullable = false)
    private String chucVu; // Chức vụ

    @Column(name = "avata", columnDefinition = "varchar(255)", nullable = false)
    private String avata; // Ảnh đại diện

    // Một nhân viên chỉ có một tài khoản
    @OneToOne(mappedBy = "nhanVien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TaiKhoan taiKhoan;



    // Một nhân viên có nhiều lịch làm việc
    @OneToMany(mappedBy = "nhanVien", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LichLamViec> danhSachLichLamViec;

    // Một nhân viên có nhiều hóa đơn
    @OneToMany(mappedBy = "nv", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HoaDon> danhSachHoaDon;
}
