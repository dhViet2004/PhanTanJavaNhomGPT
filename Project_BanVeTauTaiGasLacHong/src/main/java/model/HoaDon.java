package model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "hoadon")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HoaDon {
    @Id
    @Column(name = "ma_hd", columnDefinition = "varchar(255)", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String maHD;

    @Column(name = "ngay_lap", columnDefinition = "datetime", nullable = false)
    private LocalDateTime ngayLap;
    @Column(name = "tien_giam", columnDefinition = "double", nullable = false)
    private double tienGiam;
    @Column(name = "tong_tien", columnDefinition = "double", nullable = false)
    private double tongTien;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nhan_vien", nullable = false)
    private NhanVien nv;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_hd", nullable = false)
    private LoaiHoaDon loaiHoaDon;

//    @ManyToMany
//    @JoinTable(name = "chitiet_hoadon",
//            joinColumns = @JoinColumn(name = "ma_hd"),
//            inverseJoinColumns = @JoinColumn(name = "ma_ve"))
//    private Set<VeTau> veTaus;

    @OneToMany(mappedBy = "hoaDon", fetch = FetchType.LAZY)
    private Set<ChiTietHoaDon> chiTietHoaDons;
}
