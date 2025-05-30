package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "lichtrinhtau")
public class LichTrinhTau implements Serializable {
    @Id
    @Column(name = "ma_lich", columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    private String maLich;

    @Column(name = "gio_di", columnDefinition = "TIME", nullable = false)
    private LocalTime gioDi;

    @Column(name = "ngay_di", columnDefinition = "DATE", nullable = false)
    private LocalDate ngayDi;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)", nullable = false)
    private TrangThai trangThai;

    @ManyToOne
    @JoinColumn(name = "tau_ma_tau", referencedColumnName = "ma_tau", nullable = false)
    private Tau tau;

    @OneToMany(mappedBy = "lichTrinhTau")
    @ToString.Exclude
    private Set<VeTau> ve_taus;
}