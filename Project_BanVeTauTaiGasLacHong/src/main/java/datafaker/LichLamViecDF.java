package datafaker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.LichLamViec;
import net.datafaker.Faker;

import java.time.LocalDateTime;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: LichLamViecDF
 * @Tạo vào ngày: 19/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
public class LichLamViecDF {
    public static void main(String[] args) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        Faker faker = new Faker();

        for(int i = 0; i < 10; i++)
        {

            transaction.begin();
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
//          lichLamViec.setNhanVien(nhanVien);

//          Lưu lịch làm việc
//          em.persist(lichLamViec);
            System.out.println(lichLamViec);

            transaction.commit();
        }

    }

}