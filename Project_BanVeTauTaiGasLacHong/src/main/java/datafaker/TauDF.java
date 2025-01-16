package datafaker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Tau;
import model.TuyenTau;
import net.datafaker.Faker;

import java.util.List;

public class TauDF {

    public static void generateSampleData(EntityManager em) {
        // Khởi tạo Faker
        Faker faker = new Faker();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Lấy danh sách các TuyenTau từ cơ sở dữ liệu
            List<TuyenTau> tuyenTauList = em.createQuery("SELECT t FROM TuyenTau t", TuyenTau.class).getResultList();

            if (tuyenTauList.isEmpty()) {
                System.out.println("Không có dữ liệu TuyenTau. Hãy tạo dữ liệu TuyenTau trước.");
                return;
            }

            // Tạo 10 dữ liệu mẫu cho Tau
            for (int i = 1; i <= 10; i++) {
                Tau tau = new Tau();
                tau.setMaTau("T" + i); // Mã tàu định dạng T1, T2, ...
                tau.setTenTau("Tàu " + faker.name().lastName()); // Tên tàu với họ ngẫu nhiên
                tau.setSoToa(faker.number().numberBetween(5, 20)); // Số toa từ 5 đến 20

                // Liên kết với một TuyenTau ngẫu nhiên
                TuyenTau randomTuyenTau = tuyenTauList.get(faker.number().numberBetween(0, tuyenTauList.size()));
                tau.setTuyenTau(randomTuyenTau);

                // Lưu đối tượng Tau vào cơ sở dữ liệu
                em.persist(tau);
            }

            transaction.commit();
            System.out.println("Dữ liệu mẫu cho Tau đã được tạo thành công.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            System.err.println("Đã xảy ra lỗi khi tạo dữ liệu mẫu cho Tau.");
        }
    }
}
