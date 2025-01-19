package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.KhuyenMai;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
public class KhuyenMaiDAO {

    private EntityManager em;

    // Lấy danh sách tất cả các khuyến mãi
    public List<KhuyenMai> findAll() {
        String query = "select km from KhuyenMai km";
        return em.createQuery(query, KhuyenMai.class).getResultList();
    }

    // Lấy danh sách khuyến mãi theo tên
    public List<KhuyenMai> findByName(String name) {
        String query = "select km from KhuyenMai km where km.tenKM like :name";
        return em.createQuery(query, KhuyenMai.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    // Lấy khuyến mãi theo mã
    public KhuyenMai findById(String id) {
        return em.find(KhuyenMai.class, id);
    }

    // Thêm hoặc cập nhật khuyến mãi
    public boolean save(KhuyenMai khuyenMai) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            if (em.find(KhuyenMai.class, khuyenMai.getMaKM()) == null) {
                em.persist(khuyenMai); // Thêm mới
            } else {
                em.merge(khuyenMai); // Cập nhật
            }
            tr.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    // Xóa khuyến mãi theo mã
    public boolean delete(String id) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            KhuyenMai km = em.find(KhuyenMai.class, id);
            if (km != null) {
                em.remove(km);
                tr.commit();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }
    // Tìm các khuyến mãi đang áp dụng
    public List<KhuyenMai> findOngoingPromotions() {
        String query = "select km from KhuyenMai km " +
                "where km.trangThai = :trangThai " +
                "and km.thoiGianBatDau <= :today " +
                "and km.thoiGianKetThuc >= :today";

        return em.createQuery(query, KhuyenMai.class)
                .setParameter("trangThai", "Đang diễn ra")
                .setParameter("today", LocalDate.now())
                .getResultList();
    }
}
