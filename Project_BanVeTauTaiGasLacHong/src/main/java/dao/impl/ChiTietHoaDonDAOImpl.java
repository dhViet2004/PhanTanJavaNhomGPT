package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.ChiTietHoaDon;
import model.ChiTietHoaDonId;
import util.JPAUtil;

import java.util.List;

public class ChiTietHoaDonDAOImpl {

    private EntityManager em;

    public ChiTietHoaDonDAOImpl() {
        this.em = JPAUtil.getEntityManager();
    }

    // Create: Thêm chi tiết hóa đơn
    public boolean saveChiTietHoaDon(ChiTietHoaDon chiTietHoaDon) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(chiTietHoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }

    // Read: Lấy danh sách chi tiết hóa đơn
    public List<ChiTietHoaDon> getAllChiTietHoaDons() {
        return em.createQuery("SELECT c FROM ChiTietHoaDon c", ChiTietHoaDon.class).getResultList();
    }

    // Read: Tìm chi tiết hóa đơn theo mã hóa đơn và mã vé
    public ChiTietHoaDon getChiTietHoaDonById(ChiTietHoaDonId id) {
        return em.find(ChiTietHoaDon.class, id);
    }

    // Update: Cập nhật thông tin chi tiết hóa đơn
    public boolean updateChiTietHoaDon(ChiTietHoaDon chiTietHoaDon) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(chiTietHoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }

    // Delete: Xóa chi tiết hóa đơn theo mã hóa đơn và mã vé
    public boolean deleteChiTietHoaDon(ChiTietHoaDonId id) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            ChiTietHoaDon chiTietHoaDon = em.find(ChiTietHoaDon.class, id);
            if (chiTietHoaDon != null) {
                em.remove(chiTietHoaDon);
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
