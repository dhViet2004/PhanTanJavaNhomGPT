package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "loai_cho")
public class LoaiCho {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_loai", columnDefinition = "VARCHAR(255)", nullable = false,unique = true)
    private String maLoai;
    @Column(name = "ten_loai", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String tenLoai;


    @OneToMany(mappedBy = "loaiCho")
   @ToString.Exclude
    private Set<ChoNgoi> cho_ngois;
}
