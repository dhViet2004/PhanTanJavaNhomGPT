package datafaker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.TuyenTau;
import net.datafaker.Faker;

public class TuyenTauDF {

    public static void generateSampleData(EntityManager em) {
        // Khởi tạo Faker
        Faker faker = new Faker();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Tạo 10 dữ liệu mẫu cho TuyenTau
            for (int i = 1; i <= 10; i++) {
                TuyenTau tuyenTau = new TuyenTau();
                tuyenTau.setMaTuyen("TT" + i); // Mã tuyến định dạng TT1, TT2, ...
                tuyenTau.setTenTuyen("Tuyến " + faker.address().cityName() + " - " + faker.address().cityName()); // Tên tuyến với thành phố ngẫu nhiên
                tuyenTau.setGaDi(faker.address().streetName()); // Ga đi ngẫu nhiên
                tuyenTau.setGaDen(faker.address().streetName()); // Ga đến ngẫu nhiên
                tuyenTau.setDiaDiemDi(faker.address().cityName() + ", " + faker.address().country()); // Địa điểm đi (thành phố, quốc gia)
                tuyenTau.setDiaDiemDen(faker.address().cityName() + ", " + faker.address().country()); // Địa điểm đến (thành phố, quốc gia)

                // Lưu đối tượng TuyenTau vào cơ sở dữ liệu
                em.persist(tuyenTau);
            }

            transaction.commit();
            System.out.println("Dữ liệu mẫu cho TuyenTau đã được tạo thành công.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            System.err.println("Đã xảy ra lỗi khi tạo dữ liệu mẫu cho TuyenTau.");
        }
    }
}
