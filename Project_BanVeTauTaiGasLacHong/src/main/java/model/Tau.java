package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "tau")
@NamedNativeQueries({
        @NamedNativeQuery(name = "Tau.findById",
            query = "select * from tau where ma_tau like :id")
})
public class Tau {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_tau", columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String maTau; // Mã tàu
    @Column(name = "ten_tau", columnDefinition = "varchar(255)", nullable = false)
    @EqualsAndHashCode.Exclude
    private String tenTau;
    @Column(name = "so_toa", nullable = false)
    @EqualsAndHashCode.Exclude// Tên tàu
    private int soToa;
    // Số toa
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "ma_tuyen", nullable = false)
    private TuyenTau tuyenTau; // Đối tượng TuyenTau

    @OneToMany(mappedBy = "tau", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<ToaTau> danhSachToaTau;

    @OneToMany(mappedBy = "tau", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<LichTrinhTau> LichTrinhTau;
}
