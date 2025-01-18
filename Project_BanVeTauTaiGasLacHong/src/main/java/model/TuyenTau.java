package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@Entity
@Table(name = "tuyentau")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NamedNativeQueries({
        @NamedNativeQuery(name = "TuyenTau.findByGaDiGaDen",
                            query = "select * from tuyentau where ga_di = :gaDi and ga_den = :gaDen",
        resultClass = TuyenTau.class),
        @NamedNativeQuery(name = "TuyenTau.findAll",
                            query = "select * from tuyentau",
        resultClass = TuyenTau.class)
})
public class TuyenTau {
    @Id
    @Column(name = "ma_tuyen", columnDefinition = "varchar(255)",unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String maTuyen;
    @Column(name = "ten_tuyen", columnDefinition = "varchar(255)", nullable = false )
    @EqualsAndHashCode.Exclude
    private String tenTuyen;
    @Column(name = "ga_di", columnDefinition = "varchar(255)", nullable = false )
    @EqualsAndHashCode.Exclude
    private String gaDi;
    @Column(name = "ga_den", columnDefinition = "varchar(255)", nullable = false )
    @EqualsAndHashCode.Exclude
    private String gaDen;
    @Column(name = "dia_diem_di", columnDefinition = "varchar(255)", nullable = false )
    @EqualsAndHashCode.Exclude
    private String diaDiemDi;
    @Column(name = "dia_diem_den", columnDefinition = "varchar(255)", nullable = false )
    @EqualsAndHashCode.Exclude
    private String diaDiemDen;
    @OneToMany(mappedBy = "tuyenTau", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Tau> danhSachTau;
}
