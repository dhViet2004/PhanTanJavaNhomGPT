package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@ToString
@Table(name = "khuyenmai")
public class KhuyenMai implements Serializable {
    @Id
    @Column(name = "ma_km", columnDefinition = "VARCHAR(255)", nullable = false,unique = true)
    private String maKM;
    @Column(name = "ten_km", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String tenKM;
    @Column(name = "thoi_gian_bat_dau", columnDefinition = "DATE", nullable = false)
    private LocalDate thoiGianBatDau;
    @Column(name = "thoi_gian_ket_thuc", columnDefinition = "DATE", nullable = false)
    private LocalDate thoiGianKetThuc;
    @Column(name = "noi_dung_km", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String noiDungKM;
    @Column(name = "chiet_khau", columnDefinition = "double", nullable = false)
    private double chietKhau;
    @Column(name = "doi_tuong_ap_dung", columnDefinition = "NVARCHAR(255)", nullable = false)
    private DoiTuongApDung doiTuongApDung;
    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)", nullable = false)
    private TrangThaiKM trangThai;

    @OneToMany(mappedBy = "khuyenMai")
    @ToString.Exclude
    private Set<VeTau> ve_taus;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        KhuyenMai khuyenMai = (KhuyenMai) o;
        return getMaKM() != null && Objects.equals(getMaKM(), khuyenMai.getMaKM());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
