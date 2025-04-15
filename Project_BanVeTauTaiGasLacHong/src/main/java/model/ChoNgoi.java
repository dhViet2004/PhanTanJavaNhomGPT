package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "cho_ngoi")
public class ChoNgoi implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_cho", columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    private String maCho;
    @Column(name = "ten_cho", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String tenCho;
    @Column(name = "tinh_trang", columnDefinition = "BIT", nullable = false)
    private boolean tinhTrang;
    @Column(name = "gia_tien", columnDefinition = "FLOAT", nullable = false)
    private double giaTien;

    @ManyToOne
    @JoinColumn(name = "loaicho_maloai", referencedColumnName = "ma_loai", nullable = false)
    // ma_loai là tên trường lưu trữ giá trị khóa ngoại, với giá trị là MaLoai của LoaiCho
    @ToString.Exclude
    private LoaiCho loaiCho;

    @OneToOne(mappedBy = "choNgoi")
    private VeTau veTau;

    @ManyToOne
    @JoinColumn(name = "toa_tau_ma_toa", referencedColumnName = "ma_toa", nullable = false)
    @ToString.Exclude
    private ToaTau toaTau;
}
