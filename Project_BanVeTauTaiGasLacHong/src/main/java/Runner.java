import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.LoaiToa;
import model.Tau;
import model.ToaTau;
import model.TuyenTau;
import net.datafaker.Faker;

import java.util.HashSet;
import java.util.Locale;

public class Runner {
    public static void main(String[] args) {
        // Khởi tạo EntityManager và EntityTransaction để tương tác với cơ sở dữ liệu
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        // Sử dụng Faker để tạo dữ liệu giả
        Faker faker = new Faker();

        // Tạo dữ liệu mẫu cho 10 nhóm
        for (int i = 0; i < 10; i++) {
            // Tạo đối tượng LoaiToa và gán giá trị
            LoaiToa loaiToa = new LoaiToa();
            loaiToa.setMaLoai(faker.idNumber().valid());
            loaiToa.setTenLoai(faker.commerce().department());

            // Tạo đối tượng TuyenTau và gán giá trị
            TuyenTau tuyenTau = new TuyenTau();
            tuyenTau.setMaTuyen(faker.idNumber().valid());
            tuyenTau.setTenTuyen(faker.address().city());
            tuyenTau.setGaDi(faker.address().city());
            tuyenTau.setGaDen(faker.address().city());
            tuyenTau.setDiaDiemDi(faker.address().cityName());
            tuyenTau.setDiaDiemDen(faker.address().cityName());

            // Tạo đối tượng Tau và gán giá trị
            Tau tau = new Tau();
            tau.setMaTau(faker.idNumber().valid());
            tau.setTenTau(faker.company().name());
            tau.setSoToa(faker.number().numberBetween(5, 15));
            tau.setTuyenTau(tuyenTau);

            // Tạo đối tượng ToaTau và gán giá trị
            ToaTau toaTau = new ToaTau();
            toaTau.setMaToa(faker.number().numberBetween(1, 100));
            toaTau.setTenToa(faker.commerce().productName());
            toaTau.setSoGhe(faker.number().numberBetween(20, 60));
            toaTau.setThuTu(i + 1);
            toaTau.setLoaiToa(loaiToa);
            toaTau.setTau(tau);

            // Lưu các đối tượng vào cơ sở dữ liệu trong một giao dịch
            transaction.begin();
            em.persist(loaiToa);
            em.persist(tuyenTau);
            em.persist(tau);
            em.persist(toaTau);
            transaction.commit();
        }

        // Đóng EntityManager sau khi hoàn thành công việc
        em.close();
    }
}
