package model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "loaihoadon")
public class LoaiHoaDon implements Serializable {
    @Id
    @Column(name = "ma_loai_hd", columnDefinition = "varchar(255)", nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private String maLoaiHoaDon;
    @Column(name = "ten_loai_hd", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String tenLoaiHoaDon;

    @OneToMany(mappedBy = "loaiHoaDon")
    private Set<HoaDon> hoaDons;




}
