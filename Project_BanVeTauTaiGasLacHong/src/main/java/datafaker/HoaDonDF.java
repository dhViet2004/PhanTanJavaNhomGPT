package datafaker;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.*;
import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class HoaDonDF {

    private final EntityManager em;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    public HoaDonDF(EntityManager em) {
        this.em = em;
    }

    public void generateHoaDonData(int count) {
        EntityTransaction tr = em.getTransaction();

        List<KhachHang> khachHangs = em.createQuery("SELECT k FROM KhachHang k", KhachHang.class).getResultList();
        List<NhanVien> nhanViens = em.createQuery("SELECT n FROM NhanVien n", NhanVien.class).getResultList();
        List<LoaiHoaDon> loaiHoaDons = em.createQuery("SELECT l FROM LoaiHoaDon l", LoaiHoaDon.class).getResultList();

        if (khachHangs.isEmpty() || nhanViens.isEmpty() || loaiHoaDons.isEmpty()) {
            System.out.println("Cần thêm dữ liệu Khách hàng, Nhân viên và Loại hóa đơn trước khi tạo hóa đơn.");
            return;
        }

        try {
            tr.begin();

            for (int i = 0; i < count; i++) {
                HoaDon hoaDon = new HoaDon();
                hoaDon.setMaHD("HD" + faker.number().digits(6));
                hoaDon.setNgayLap(LocalDateTime.now().minusDays(random.nextInt(30)));
                hoaDon.setTienGiam(faker.number().randomDouble(2, 50, 500));
                hoaDon.setTongTien(faker.number().randomDouble(2, 1000, 10000));
                hoaDon.setKhachHang(khachHangs.get(random.nextInt(khachHangs.size())));
                hoaDon.setNv(nhanViens.get(random.nextInt(nhanViens.size())));
                hoaDon.setLoaiHoaDon(loaiHoaDons.get(random.nextInt(loaiHoaDons.size())));

                em.persist(hoaDon);
            }

            tr.commit();
            System.out.println("Đã tạo thành công " + count + " hóa đơn.");
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        }
    }

}
