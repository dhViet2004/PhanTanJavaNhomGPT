package testCRUD;

import dao.HoaDonDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import model.HoaDon;
import model.KhachHang;
import model.LoaiHoaDon;
import model.NhanVien;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HoaDonDAOTest {

    public static void main(String[] args) {
        // Tạo EntityManagerFactory và EntityManager
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mariadb");
        EntityManager em = emf.createEntityManager();

        // Tạo instance của HoaDonDAO
        HoaDonDAO hoaDonDAO = new HoaDonDAO(em);

        // 1. Thêm hóa đơn mới
        System.out.println("=== Thêm hóa đơn mới ===");
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHD("HD997009");
        hoaDon.setNgayLap(LocalDateTime.now());
        hoaDon.setTongTien(500000.0); // Tổng tiền hóa đơn
        KhachHang khachHang = em.find(KhachHang.class, "KH180120250001");
        hoaDon.setKhachHang(khachHang); // Khách hàng
        NhanVien nhanVien = em.find(NhanVien.class, "076-72-2570");
        hoaDon.setNv(nhanVien);
        LoaiHoaDon loaiHoaDon = em.find(LoaiHoaDon.class, "LHD001");
        hoaDon.setLoaiHoaDon(loaiHoaDon); // Loại hóa đơn
        hoaDon.setTienGiam(0.0); // Số tiền giảm giá

        boolean isSaved = hoaDonDAO.saveHoaDon(hoaDon);
        System.out.println("Thêm hóa đơn: " + (isSaved ? "Thành công" : "Thất bại"));

        // 2. Lấy danh sách tất cả hóa đơn
        System.out.println("\n=== Danh sách tất cả hóa đơn ===");
        List<HoaDon> allHoaDons = hoaDonDAO.getAllHoaDons();
        allHoaDons.forEach(System.out::println);

        // 3. Tìm hóa đơn theo mã hóa đơn
        System.out.println("\n=== Tìm hóa đơn theo mã hóa đơn ===");
        HoaDon foundHoaDon = hoaDonDAO.getHoaDonById("HD001");
        System.out.println("Kết quả: " + (foundHoaDon != null ? foundHoaDon : "Không tìm thấy"));

        // 4. Cập nhật thông tin hóa đơn
        System.out.println("\n=== Cập nhật thông tin hóa đơn ===");
        if (foundHoaDon != null) {
            foundHoaDon.setTongTien(600000.0); // Cập nhật tổng tiền
            boolean isUpdated = hoaDonDAO.updateHoaDon(foundHoaDon);
            System.out.println("Cập nhật hóa đơn: " + (isUpdated ? "Thành công" : "Thất bại"));
        } else {
            System.out.println("Không tìm thấy hóa đơn để cập nhật.");
        }

        // 5. Xóa hóa đơn theo mã hóa đơn
        System.out.println("\n=== Xóa hóa đơn theo mã hóa đơn ===");
        boolean isDeleted = hoaDonDAO.deleteHoaDon("HD997009");
        System.out.println("Xóa hóa đơn: " + (isDeleted ? "Thành công" : "Thất bại"));

        // Đóng EntityManager và EntityManagerFactory
        em.close();
        emf.close();
    }
}
