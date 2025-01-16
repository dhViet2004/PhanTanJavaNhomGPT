package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "vetau")
public class VeTau {
    @Id
    @Column(name = "ma_ve", columnDefinition = "VARCHAR(255)", unique = true, nullable = false)
    private String maVe;
    @Column(name = "ten_khach_hang", columnDefinition = "VARCHAR(255)", nullable = false)

    private String tenKhachHang;
    @Column(name = "giay_to", columnDefinition = "VARCHAR(255)", nullable = false)

    private String giayTo;
    @Column(name = "ngay_di", columnDefinition = "DATE",nullable = false)

    private LocalDate ngayDi;
    @Column(name = "doi_tuong", columnDefinition = "VARCHAR(255)", nullable = false)

    private String doiTuong;
    @Column(name = "gia_ve", columnDefinition = "FLOAT", nullable = false)

    private double giaVe;
    @Column(name = "trang_thai", columnDefinition = "VARCHAR(255)",nullable = false)

    private String trangThai;

    @ManyToOne
    @JoinColumn(name = "lich_trinh_tau_ma_lich", referencedColumnName = "ma_lich")
    private LichTrinhTau lichTrinhTau;

    @ManyToOne
    @JoinColumn(name = "khuyen_mai_ma_km", referencedColumnName = "ma_km")
    private KhuyenMai khuyenMai;

    @OneToOne
    @JoinColumn(name = "cho_ngoi_ma_cho",referencedColumnName = "ma_cho")
     private ChoNgoi choNgoi;

//    @ManyToMany(mappedBy = "veTaus")
//    private Set<HoaDon> hoaDons;

    // Mối quan hệ với ChiTietHoaDon
    @OneToMany(mappedBy = "veTau", fetch = FetchType.LAZY)
    private Set<ChiTietHoaDon> chiTietHoaDons;

}
