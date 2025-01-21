package testCRUD;

import dao.KhuyenMaiDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.KhuyenMai;

import java.time.LocalDate;
import java.util.List;

public class KhuyenMaiDAOTest {

    public static void main(String[] args) {
        // Tạo EntityManagerFactory và EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mariadb");
        EntityManager em = emf.createEntityManager();

        // Tạo instance của KhuyenMaiDAO
        KhuyenMaiDAO khuyenMaiDAO = new KhuyenMaiDAO(em);

        // 1. Thêm hoặc cập nhật khuyến mãi
        System.out.println("=== Thêm hoặc cập nhật khuyến mãi ===");
        KhuyenMai khuyenMai = new KhuyenMai();
        khuyenMai.setMaKM("KM000016");
        khuyenMai.setTenKM("Khuyến mãi Tết");
        khuyenMai.setTrangThai("Đang diễn ra");
        khuyenMai.setThoiGianBatDau(LocalDate.now().minusDays(5));
        khuyenMai.setThoiGianKetThuc(LocalDate.now().plusDays(10));
        khuyenMai.setChietKhau(0.2); // 20% giảm giá
        khuyenMai.setDoiTuongApDung("Tất cả khách hàng");
        khuyenMai.setNoiDungKM("Giảm giá 20% cho tất cả sản phẩm");
        boolean isSaved = khuyenMaiDAO.save(khuyenMai);
        System.out.println("Thêm hoặc cập nhật khuyến mãi: " + (isSaved ? "Thành công" : "Thất bại"));

        // 2. Lấy danh sách tất cả các khuyến mãi
        System.out.println("\n=== Danh sách tất cả các khuyến mãi ===");
        List<KhuyenMai> allPromotions = khuyenMaiDAO.findAll();
        allPromotions.forEach(System.out::println);

        // 3. Tìm khuyến mãi theo tên
        System.out.println("\n=== Tìm khuyến mãi theo tên ===");
        List<KhuyenMai> promotionsByName = khuyenMaiDAO.findByName("Tết");
        promotionsByName.forEach(System.out::println);

        // 4. Tìm khuyến mãi theo mã
        System.out.println("\n=== Tìm khuyến mãi theo mã ===");
        KhuyenMai foundPromotion = khuyenMaiDAO.findById("KM001");
        System.out.println("Kết quả: " + (foundPromotion != null ? foundPromotion : "Không tìm thấy"));

        // 5. Tìm các khuyến mãi đang áp dụng
        System.out.println("\n=== Tìm các khuyến mãi đang áp dụng ===");
        List<KhuyenMai> ongoingPromotions = khuyenMaiDAO.findOngoingPromotions();
        ongoingPromotions.forEach(System.out::println);

        // 6. Xóa khuyến mãi theo mã
        System.out.println("\n=== Xóa khuyến mãi theo mã ===");
        boolean isDeleted = khuyenMaiDAO.delete("KM000001");
        System.out.println("Xóa khuyến mãi: " + (isDeleted ? "Thành công" : "Thất bại"));

        // Đóng EntityManager và EntityManagerFactory
        em.close();
        emf.close();
    }
}
