package testCRUD;

import dao.impl.ChiTietHoaDonDAOImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.ChiTietHoaDon;
import model.ChiTietHoaDonId;

public class ChiTietHoaDonDAOTest {

    public static void main(String[] args) {
        // Tạo EntityManagerFactory và EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mariadb");
        EntityManager em = emf.createEntityManager();

        // Tạo instance của ChiTietHoaDonDAO
        ChiTietHoaDonDAOImpl chiTietHoaDonDAOImpl = new ChiTietHoaDonDAOImpl();

        // 1. Test thêm chi tiết hóa đơn
        ChiTietHoaDonId id = new ChiTietHoaDonId(); // Mã hóa đơn và mã vé giả định
        id.setMaHD("HD001");
        id.setMaVe("VT001");
        ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();// Số lượng và đơn giá giả định

        chiTietHoaDon.setId(id);
        chiTietHoaDon.setSoLuong(5);
        chiTietHoaDon.setVAT(0.1); // 10% VAT
        chiTietHoaDon.setTienThue(chiTietHoaDon.getSoLuong() * 100000 * 0.1);
        chiTietHoaDon.setThanhTien((chiTietHoaDon.getSoLuong() * 100000) + chiTietHoaDon.getTienThue());
        boolean isSaved = chiTietHoaDonDAOImpl.saveChiTietHoaDon(chiTietHoaDon);
        System.out.println("Thêm chi tiết hóa đơn: " + (isSaved ? "Thành công" : "Thất bại"));

        // 2. Test lấy danh sách chi tiết hóa đơn
        System.out.println("Danh sách chi tiết hóa đơn:");
        chiTietHoaDonDAOImpl.getAllChiTietHoaDons().forEach(System.out::println);

        // 3. Test tìm chi tiết hóa đơn theo ID
        ChiTietHoaDon fetchedChiTietHoaDon = chiTietHoaDonDAOImpl.getChiTietHoaDonById(id);
        System.out.println("Chi tiết hóa đơn tìm thấy: " + fetchedChiTietHoaDon);

        // 4. Test cập nhật chi tiết hóa đơn
        if (fetchedChiTietHoaDon != null) {
            fetchedChiTietHoaDon.setSoLuong(10); // Cập nhật số lượng
            boolean isUpdated = chiTietHoaDonDAOImpl.updateChiTietHoaDon(fetchedChiTietHoaDon);
            System.out.println("Cập nhật chi tiết hóa đơn: " + (isUpdated ? "Thành công" : "Thất bại"));
        }

        // 5. Test xóa chi tiết hóa đơn
        boolean isDeleted = chiTietHoaDonDAOImpl.deleteChiTietHoaDon(id);
        System.out.println("Xóa chi tiết hóa đơn: " + (isDeleted ? "Thành công" : "Thất bại"));

        // Đóng EntityManager và EntityManagerFactory
        em.close();
        emf.close();
    }
}
