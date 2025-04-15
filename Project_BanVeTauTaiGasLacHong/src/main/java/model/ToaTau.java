package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "ToaTau")

public class ToaTau implements Serializable {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_toa",columnDefinition = "varchar(255)",nullable = false, unique = true)
    private String maToa;

    @Column(name = "ten_toa", columnDefinition = "varchar(255)", nullable = false)
    private String tenToa;

    @Column(name = "so_ghe", nullable = false)
    private int soGhe;

    @Column(name = "thu_tu", nullable = false)
    private int thuTu;

    @OneToMany(mappedBy = "toaTau", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<ChoNgoi> danhSachChoNgoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_loai_toa", nullable = false)
    @ToString.Exclude
    private LoaiToa loaiToa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tau", nullable = false)
    private Tau tau;
}
