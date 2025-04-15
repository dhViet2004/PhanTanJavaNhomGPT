package model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @Dự án: App
 * @Class: TaiKhoan
 * @Tạo vào ngày: 15/01/2025
 * @Tác giả: Nguyen Huu Sang
 */

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "taikhoan")
public class TaiKhoan implements Serializable {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "ma_nv", columnDefinition = "varchar(255)", nullable = false, unique = true)
    private String maNV; // Mã tài khoản

    @Column(name = "password", columnDefinition = "varchar(255)", nullable = false)
    private String passWord; // Mật khẩu

    // Một tài khoản chỉ thuộc về một nhân viên
//    @OneToOne
//    @JoinColumn(name = "ma_nv", referencedColumnName = "ma_nv", unique = true)
//    private NhanVien nhanVien;

//    @OneToOne
//    @JoinColumn(name = "ma_nv", referencedColumnName = "ma_nv", unique = true, nullable = false)
//    private NhanVien nhanVien;

    @OneToOne
    @MapsId
    @JoinColumn(name = "ma_nv")
    @ToString.Exclude
    private NhanVien nhanVien;
}
