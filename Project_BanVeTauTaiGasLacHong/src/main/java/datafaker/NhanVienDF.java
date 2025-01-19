package datafaker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.NhanVien;
import model.TaiKhoan;
import net.datafaker.Faker;

import java.time.ZoneId;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: NhanVienDF
 * @Tạo vào ngày: 19/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
public class NhanVienDF {
    public static void main(String[] args) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        Faker faker = new Faker();

        for (int i = 0; i < 10; i++) {

            transaction.begin();

            // Tạo dữ liệu mẫu cho NhanVien
            NhanVien nhanVien = new NhanVien();
            nhanVien.setMaNV(faker.idNumber().valid());
            nhanVien.setTenNV(faker.name().fullName());
            nhanVien.setSoDT(faker.phoneNumber().cellPhone());
            nhanVien.setTrangThai("Active");
            nhanVien.setCccd(faker.idNumber().valid());
            nhanVien.setDiaChi(faker.address().fullAddress());
            nhanVien.setNgayVaoLam(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            nhanVien.setChucVu("Nhân viên");
            nhanVien.setAvata(faker.avatar().image());

            // Tạo dữ liệu mẫu cho TaiKhoan
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaNV(nhanVien.getMaNV());
            taiKhoan.setPassWord(faker.internet().password());
            taiKhoan.setNhanVien(nhanVien);


            // Liên kết NhanVien với TaiKhoan
            nhanVien.setTaiKhoan(taiKhoan);

            // Lưu nhân viên và tài khoản
            em.persist(nhanVien);
            em.persist(taiKhoan);
            System.out.println(nhanVien);
            System.out.println(taiKhoan);

            transaction.commit();
        }
    }

}