package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @Dự án: App
 * @Class: LichLamViec
 * @Tạo vào ngày: 15/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "lichlamviec")
public class LichLamViec {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_lich_lam_viec", columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String maLichLamViec; // Mã lịch làm việc

    @Column(name = "gio_bat_dau", columnDefinition = "datetime", nullable = false)
    private LocalDateTime gioBatDau; // Giờ bắt đầu
    @Column(name = "gio_ket_thuc", columnDefinition = "datetime", nullable = false)
    private LocalDateTime gioKetThuc; // Giờ kết thúc
    @Column(name = "trang_thai", columnDefinition = "varchar(255)", nullable = false)
    private String trangThai; // Trạng thái
    @Column(name = "ten_ca", columnDefinition = "varchar(255)", nullable = false)
    private String tenCa; // Tên ca

    // Một lịch làm việc chỉ thuộc về một nhân viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nv", nullable = false)
    private NhanVien nhanVien;



}