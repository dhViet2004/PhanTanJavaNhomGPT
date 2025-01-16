package model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Data
@Entity
@Table(name = "khuyenmai")
public class KhuyenMai {
    @Id
    @Column(name = "ma_km", columnDefinition = "VARCHAR(255)", nullable = false,unique = true)
    private String maKM;
    @Column(name = "ten_km", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String tenKM;
    @Column(name = "thoi_gian_bat_dau", columnDefinition = "DATE", nullable = false)
    private LocalDate thoiGianBatDau;
    @Column(name = "thoi_gian_ket_thuc", columnDefinition = "DATE", nullable = false)
    private LocalDate thoiGianKetThuc;
    @Column(name = "noi_dung_km", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String noiDungKM;
    @Column(name = "chiet_khau", columnDefinition = "double", nullable = false)
    private double chietKhau;
    @Column(name = "doi_tuong_ap_dung", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String doiTuongApDung;
    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)", nullable = false)
    private String trangThai;

    @OneToMany(mappedBy = "khuyenMai")
    private Set<VeTau> ve_taus;


}
