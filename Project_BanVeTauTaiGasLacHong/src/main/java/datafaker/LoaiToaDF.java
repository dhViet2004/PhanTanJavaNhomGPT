package datafaker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LoaiToa;
import net.datafaker.Faker;

public class LoaiToaDF {

    public static void generateSampleData(EntityManager em) {
        // Sử dụng Faker để tạo dữ liệu mẫu
        Faker faker = new Faker();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            // Tạo 10 dữ liệu mẫu cho LoaiToa
            for (int i = 1; i <= 10; i++) {
                LoaiToa loaiToa = new LoaiToa();
                loaiToa.setMaLoai("LT" + i); // Đặt mã loại định dạng LT1, LT2, ...
                loaiToa.setTenLoai("Loại toa " + faker.commerce().productName()); // Tên loại với mô tả từ Faker
                // Lưu vào cơ sở dữ liệu
                em.persist(loaiToa);
            }

            transaction.commit();
            System.out.println("Dữ liệu mẫu cho LoaiToa đã được tạo thành công.");
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            System.err.println("Đã xảy ra lỗi khi tạo dữ liệu mẫu cho LoaiToa.");
        }
    }
}
