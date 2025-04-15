package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
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
@Table(name = "nhanvien")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NamedNativeQueries({
        @NamedNativeQuery(name = "NhanVien.findByMaNV",
                query = "select * from nhanvien where ma_nv = :maNV",
                resultClass = NhanVien.class),
        @NamedNativeQuery(name = "NhanVien.findAll",
                query = "select * from nhanvien",
                resultClass = NhanVien.class)
})
public class NhanVien implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_nv", columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String maNV; // Mã nhân viên

    @Column(name = "ten_nv", columnDefinition = "varchar(255)", nullable = false)
    private String tenNV; // Tên nhân viên
    @EqualsAndHashCode.Exclude

    @Column(name = "so_dt", columnDefinition = "varchar(255)", nullable = false)
    private String soDT; // Số điện thoại
    @EqualsAndHashCode.Exclude

    @Column(name = "trang_thai", columnDefinition = "varchar(255)", nullable = false)
    private String trangThai; // Trạng thái
    @EqualsAndHashCode.Exclude

    @Column(name = "cccd", columnDefinition = "varchar(255)", nullable = false)
    private String cccd; // Chứng chỉ công dân
    @EqualsAndHashCode.Exclude

    @Column(name = "dia_chi", columnDefinition = "varchar(255)", nullable = false)
    private String diaChi; // Địa chỉ
    @EqualsAndHashCode.Exclude

    @Column(name = "ngay_vao_lam", columnDefinition = "date", nullable = false)
    private LocalDate ngayVaoLam; // Ngày vào làm
    @EqualsAndHashCode.Exclude

    @Column(name = "chuc_vu", columnDefinition = "varchar(255)", nullable = false)
    private String chucVu; // Chức vụ
    @EqualsAndHashCode.Exclude

    @Column(name = "avata", columnDefinition = "varchar(255)", nullable = false)
    private String avata; // Ảnh đại diện
    @EqualsAndHashCode.Exclude

    // Một nhân viên chỉ có một tài khoản
    @OneToOne(mappedBy = "nhanVien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private TaiKhoan taiKhoan;


    // Một nhân viên có nhiều lịch làm việc
    @OneToMany(mappedBy = "nhanVien", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<LichLamViec> danhSachLichLamViec;

    // Một nhân viên có nhiều hóa đơn
    @OneToMany(mappedBy = "nv", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<HoaDon> danhSachHoaDon;



}
