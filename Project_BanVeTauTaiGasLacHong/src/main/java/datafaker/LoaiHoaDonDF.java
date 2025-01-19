package datafaker;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LoaiHoaDon;
import net.datafaker.Faker;

import java.util.Locale;

public class LoaiHoaDonDF {
    private Faker faker;

    public LoaiHoaDonDF() {
        this.faker = new Faker(new Locale("vi")); // Khởi tạo Datafaker với ngôn ngữ tiếng Việt
    }

    public LoaiHoaDon generateLoaiHoaDon(String maLoaiHoaDon, String tenLoaiHoaDon) {
        LoaiHoaDon loaiHoaDon = new LoaiHoaDon();
        loaiHoaDon.setMaLoaiHoaDon(maLoaiHoaDon); // Mã loại hóa đơn (được truyền vào)
        loaiHoaDon.setTenLoaiHoaDon(tenLoaiHoaDon); // Tên loại hóa đơn (mua, đổi, trả)
        return loaiHoaDon;
    }

    public void generateAndSaveLoaiHoaDons(EntityManager em) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();

            // Thêm 3 loại hóa đơn: Mua, Đổi, Trả
            em.persist(generateLoaiHoaDon("LHD001", "Mua"));
            em.persist(generateLoaiHoaDon("LHD002", "Đổi"));
            em.persist(generateLoaiHoaDon("LHD003", "Trả"));

            transaction.commit();
            System.out.println("Thêm dữ liệu mẫu cho LoaiHoaDon thành công!");
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
            System.err.println("Lỗi khi thêm dữ liệu mẫu cho LoaiHoaDon!");
        }
    }
}
