package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.HoaDon;

import java.util.List;

public class HoaDonDAO {

    private EntityManager em;

    public HoaDonDAO(EntityManager em) {
        this.em = em;
    }

    // Create: Thêm hóa đơn mới
    public boolean saveHoaDon(HoaDon hoaDon) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(hoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }

    // Read: Lấy danh sách hóa đơn
    public List<HoaDon> getAllHoaDons() {
        return em.createQuery("SELECT h FROM HoaDon h", HoaDon.class).getResultList();
    }

    // Read: Tìm hóa đơn theo mã hóa đơn
    public HoaDon getHoaDonById(String maHD) {
        return em.find(HoaDon.class, maHD);
    }

    // Update: Cập nhật thông tin hóa đơn
    public boolean updateHoaDon(HoaDon hoaDon) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(hoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }

    // Delete: Xóa hóa đơn theo mã hóa đơn
    public boolean deleteHoaDon(String maHD) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            HoaDon hoaDon = em.find(HoaDon.class, maHD);
            if (hoaDon != null) {
                em.remove(hoaDon);
            }
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }
}
