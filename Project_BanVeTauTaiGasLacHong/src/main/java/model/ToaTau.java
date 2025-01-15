package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "ToaTau")
public class ToaTau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ma_toa", nullable = false, unique = true)
    private Integer maToa;

    @Column(name = "ten_toa", columnDefinition = "varchar(255)", unique = true, nullable = false)
    private String tenToa;

    @Column(name = "so_ghe", nullable = false)
    private int soGhe;

    @Column(name = "thu_tu", nullable = false)
    private int thuTu;

//    // Quan hệ nhiều toa tàu thuộc về một loại toa
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "ma_loai_toa", nullable = false)
//    @ToString.Exclude
//    private LoaiToa loaiToa;
}
