package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "LoaiToa")
@NamedNativeQueries({
        @NamedNativeQuery(name = "LoaiToa.findAll",
                query = "select * from loaitoa",
                resultClass = LoaiToa.class),
        @NamedNativeQuery(name = "LoaiToa.findByID",
                query = "select * from loaitoa where ma_loai_toa like :maLoaiToa",
                resultClass = LoaiToa.class)
})
public class LoaiToa {
    @Id
    @Column(name = "ma_loai_toa", columnDefinition = "varchar(255)", nullable = false)
    @EqualsAndHashCode.Include
    private String maLoai;

    @Column(name = "ten_loai", columnDefinition = "varchar(255)", nullable = false)
    private String tenLoai;

    @OneToMany(mappedBy = "loaiToa", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<ToaTau> danhSachToaTau;
}
