package datafaker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.ChiTietHoaDon;
import model.ChiTietHoaDonId;
import model.HoaDon;
import model.VeTau;
import net.datafaker.Faker;

import java.util.List;
import java.util.Random;

public class ChiTietHoaDonDF {

    private final EntityManager em;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    public ChiTietHoaDonDF(EntityManager em) {
        this.em = em;
    }

    public void generateChiTietHoaDonData(int count) {
        EntityTransaction tr = em.getTransaction();

        List<HoaDon> hoaDons = em.createQuery("SELECT h FROM HoaDon h", HoaDon.class).getResultList();
        List<VeTau> veTaus = em.createQuery("SELECT v FROM VeTau v", VeTau.class).getResultList();

        if (hoaDons.isEmpty() || veTaus.isEmpty()) {
            System.out.println("Cần thêm dữ liệu Hóa đơn và Vé tàu trước khi tạo chi tiết hóa đơn.");
            return;
        }

        try {
            tr.begin();

            for (int i = 0; i < count; i++) {
                ChiTietHoaDon chiTiet = new ChiTietHoaDon();
                ChiTietHoaDonId id = new ChiTietHoaDonId();

                HoaDon hoaDon = hoaDons.get(random.nextInt(hoaDons.size()));
                VeTau veTau = veTaus.get(random.nextInt(veTaus.size()));

                id.setMaHD(hoaDon.getMaHD());
                id.setMaVe(veTau.getMaVe());
                chiTiet.setId(id);
                chiTiet.setHoaDon(hoaDon);
                chiTiet.setVeTau(veTau);
                chiTiet.setSoLuong(random.nextInt(5) + 1);
                chiTiet.setVAT(0.1); // 10% VAT
                chiTiet.setTienThue(chiTiet.getSoLuong() * veTau.getGiaVe() * 0.1);
                chiTiet.setThanhTien((chiTiet.getSoLuong() * veTau.getGiaVe()) + chiTiet.getTienThue());

                em.persist(chiTiet);
            }

            tr.commit();
            System.out.println("Đã tạo thành công " + count + " chi tiết hóa đơn.");
        } catch (Exception e) {
            tr.rollback();
            e.printStackTrace();
        }
    }
}
