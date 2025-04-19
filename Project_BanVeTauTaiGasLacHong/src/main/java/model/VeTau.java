package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "vetau")
public class VeTau implements Serializable {
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

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "VARCHAR(255)",nullable = false)
    private TrangThaiVeTau trangThai;

    @ManyToOne
    @JoinColumn(name = "lich_trinh_tau_ma_lich", referencedColumnName = "ma_lich")
    @ToString.Exclude
    private LichTrinhTau lichTrinhTau;

    @ManyToOne
    @JoinColumn(name = "khuyen_mai_ma_km", referencedColumnName = "ma_km")
    @ToString.Exclude
    private KhuyenMai khuyenMai;

    @OneToOne
    @JoinColumn(name = "cho_ngoi_ma_cho",referencedColumnName = "ma_cho")
    @ToString.Exclude
     private ChoNgoi choNgoi;

//    @ManyToMany(mappedBy = "veTaus")
//    private Set<HoaDon> hoaDons;

    // Mối quan hệ với ChiTietHoaDon
    @OneToMany(mappedBy = "veTau", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<ChiTietHoaDon> chiTietHoaDons;

}
