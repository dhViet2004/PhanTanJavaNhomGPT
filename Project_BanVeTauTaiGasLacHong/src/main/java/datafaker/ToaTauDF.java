package datafaker;

import dao.TauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LoaiToa;
import model.Tau;
import model.ToaTau;
import net.datafaker.Faker;

import java.util.List;

public class ToaTauDF {

    public static void generateSampleData(EntityManager em) {
        // lấy danh sách các tàu
        TauDAO tauDAO = new TauDAO(em);
        List<Tau> tauList = tauDAO.getAllListT();
        // Khởi tạo Faker
        Faker faker = new Faker();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Lấy danh sách các Tau từ cơ sở dữ liệu
            if (tauList.isEmpty()) {
                System.out.println("Không có dữ liệu Tau. Hãy tạo dữ liệu Tau trước.");
                return;
            }

            // Lấy danh sách các LoaiToa từ cơ sở dữ liệu
            List<LoaiToa> loaiToaList = em.createQuery("SELECT l FROM LoaiToa l", LoaiToa.class).getResultList();
            if (loaiToaList.isEmpty()) {
                System.out.println("Không có dữ liệu LoaiToa. Hãy tạo dữ liệu LoaiToa trước.");
                return;
            }

            // Tạo dữ liệu mẫu cho ToaTau
            for (Tau tau : tauList) {
                for (int i = 1; i <= 20; i++) { // Tạo 20 toa tàu
                    ToaTau toaTau = new ToaTau();
                    toaTau.setMaToa(tau.getMaTau()+i); // Mã toa là số nguyên tăng dần
                    toaTau.setTenToa("Toa " + i); // Tên toa là "Toa {i}"
                    toaTau.setSoGhe(faker.number().numberBetween(20, 100)); // Số ghế từ 20 đến 100
                    toaTau.setThuTu(faker.number().numberBetween(1, 10)); // Thứ tự toa từ 1 đến 10
                    toaTau.setTau(tau);

                    // Liên kết với một LoaiToa ngẫu nhiên
                    LoaiToa randomLoaiToa = loaiToaList.get(faker.number().numberBetween(0, loaiToaList.size()));
                    toaTau.setLoaiToa(randomLoaiToa);

                    // Lưu đối tượng ToaTau vào cơ sở dữ liệu
                    em.persist(toaTau);
                }
            }


            transaction.commit();
            System.out.println("Dữ liệu mẫu cho ToaTau đã được tạo thành công.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            System.err.println("Đã xảy ra lỗi khi tạo dữ liệu mẫu cho ToaTau.");
        }
    }
}
