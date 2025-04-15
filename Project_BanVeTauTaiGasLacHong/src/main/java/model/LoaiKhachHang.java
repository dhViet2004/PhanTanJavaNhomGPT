package model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoaiKhachHang implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_loai_khach_hang", columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String maLoaiKhachHang;
    @Column(name = "ten_loai_khach_hang", columnDefinition = "varchar(255)", nullable = false)
    private String tenLoaiKhachHang;

    @ToString.Exclude
    @OneToMany(mappedBy = "loaiKhachHang")
    private Set<KhachHang> khachHangs;

}

