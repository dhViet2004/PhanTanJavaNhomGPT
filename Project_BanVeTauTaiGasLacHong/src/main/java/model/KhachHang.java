package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class KhachHang implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_khach_hang", columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String maKhachHang;
    @Column(name = "sdt" , columnDefinition = "varchar(255)", nullable = false)
    private String soDienThoai;
    @Column(name = "ten_khach_hang", columnDefinition = "varchar(255)", nullable = false)
    private String tenKhachHang;
    @Column(name = "giay_to", columnDefinition = "varchar(255)", nullable = false)
    private String giayTo;
    @Column(name = "dia_chi", columnDefinition = "varchar(255)", nullable = false)
    private String diaChi;
    @Column(name = "diem_tich_luy", columnDefinition = "double", nullable = false)
    private double diemTichLuy;
    @Column(name = "ngay_sinh", columnDefinition = "date", nullable = false)
    private LocalDate ngaySinh;
    @Column(name = "ngay_tham_gia", columnDefinition = "date", nullable = false)
    private LocalDate ngayThamgGia;
    @Column(name = "hang_thanh_vien", columnDefinition = "varchar(255)", nullable = false)
    private String hangThanhVien;

    @ManyToOne
    @JoinColumn(name = "ma_loai_khach_hang", nullable = false)
    @ToString.Exclude
    private LoaiKhachHang loaiKhachHang;

    @ToString.Exclude
    @OneToMany(mappedBy = "khachHang")
    private Set<HoaDon> hoaDons;
}
