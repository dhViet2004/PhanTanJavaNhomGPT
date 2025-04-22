package datafaker;



import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import model.KhuyenMai;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class KhuyenMaiDF {

    // Hàm tạo mã khuyến mãi tự tăng
    private static String generateNextMaKM(EntityManager em) {
        String prefix = "KM";
        String queryStr = "SELECT MAX(k.maKM) FROM KhuyenMai k WHERE k.maKM LIKE :prefix";
        Query query = em.createQuery(queryStr);
        query.setParameter("prefix", prefix + "%");

        String maxMaKM = (String) query.getSingleResult();
        int nextNumber = 1;

        if (maxMaKM != null) {
            // Lấy phần số từ mã cuối cùng và tăng lên 1
            String numberPart = maxMaKM.substring(prefix.length());
            nextNumber = Integer.parseInt(numberPart) + 1;
        }

        // Trả về mã khuyến mãi với định dạng KMXXXXXX
        return String.format("%s%06d", prefix, nextNumber);
    }

//    // Hàm để hiển thị dữ liệu mẫu
//    public static void generateAndPrintSampleData(EntityManager em) {
//
//
//        try {
//            em.getTransaction().begin();
//            for (int i = 0; i < 15; i++) {
//                KhuyenMai khuyenMai = new KhuyenMai();
//                khuyenMai.setMaKM(generateNextMaKM(em)); // Sinh mã tự động
//                khuyenMai.setTenKM("Khuyến mãi số " + (i + 1));
//                khuyenMai.setThoiGianBatDau(LocalDate.now().minusDays(i));
//                khuyenMai.setThoiGianKetThuc(LocalDate.now().plusDays(i + 10));
//                khuyenMai.setNoiDungKM("Giảm giá " + (10 + i) + "% cho khách hàng");
//                khuyenMai.setChietKhau(10 + i);
//                khuyenMai.setDoiTuongApDung("Khách hàng loại " + (i % 3 + 1));
//                khuyenMai.setTrangThai(i % 2 == 0 ? "Đang áp dụng" : "Hết hạn");
//
//                em.persist(khuyenMai); // Lưu vào cơ sở dữ liệu
//            }
//            em.getTransaction().commit();
//        } catch (Exception e) {
//            em.getTransaction().rollback();
//            e.printStackTrace();
//        }
//    }


}
