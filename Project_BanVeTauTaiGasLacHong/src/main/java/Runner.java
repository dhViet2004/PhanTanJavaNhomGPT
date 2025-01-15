import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.*;
import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Runner {
    public static void main(String[] args) {
        // Khởi tạo EntityManager và EntityTransaction để tương tác với cơ sở dữ liệu
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();

        // Dùng datafaker tạo dữ liệu mẫu của lịch làm việc, nhân viên, tài khoản
        EntityTransaction transaction = em.getTransaction();

        Faker faker = new Faker();

        for (int i = 0; i < 10; i++)
        {

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

            // Tạo dữ liệu mẫu cho LichLamViec
            LichLamViec lichLamViec = new LichLamViec();
            lichLamViec.setMaLichLamViec(faker.idNumber().valid());

            // Sinh ngẫu nhiên giờ bắt đầu và kết thúc
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime gioBatDau = now.plusHours(faker.number().numberBetween(0, 5));
            LocalDateTime gioKetThuc = gioBatDau.plusHours(faker.number().numberBetween(4, 8));

            lichLamViec.setGioBatDau(gioBatDau);
            lichLamViec.setGioKetThuc(gioKetThuc);
            lichLamViec.setTrangThai(faker.options().option("Scheduled", "Completed", "Cancelled"));
            lichLamViec.setTenCa(faker.options().option("Ca sáng", "Ca chiều", "Ca tối"));
            lichLamViec.setNhanVien(nhanVien);

            // Lưu lịch làm việc
            em.persist(lichLamViec);

            transaction.commit();


        }


    }
}
