package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@Entity
@Table(name = "chitiet_hoadon")
public class ChiTietHoaDon implements Serializable {

    @EmbeddedId
    private ChiTietHoaDonId id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maHD")  // Maps maHD in the composite key
    @JoinColumn(name = "ma_hd", referencedColumnName = "ma_hd", nullable = false)
    private HoaDon hoaDon;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maVe")  // Maps maVe in the composite key
    @JoinColumn(name = "ma_ve", referencedColumnName = "ma_ve", nullable = false)
    private VeTau veTau;

    @Column(name = "so_luong", nullable = false)
    private int soLuong;

    @Column(name = "VAT", nullable = false)
    private double VAT;

    @Column(name = "thanh_tien", nullable = false)
    private double thanhTien;

    @Column(name = "tien_thue", nullable = false)
    private double tienThue;


}
