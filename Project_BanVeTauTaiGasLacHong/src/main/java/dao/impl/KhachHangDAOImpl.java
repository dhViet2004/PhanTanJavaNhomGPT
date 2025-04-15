package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.KhachHang;

import java.util.List;

@AllArgsConstructor
public class KhachHangDAOImpl {

    private EntityManager em;

    // Lấy danh sách khách hàng theo tên
    public List<KhachHang> listKhachHangsByName(String name) {
        String query = "select kh from KhachHang kh where kh.tenKhachHang like :name";
        return em.createQuery(query, KhachHang.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    // Lấy danh sách khách hàng có điểm tích lũy trong khoảng
    public List<KhachHang> listKhachHangsByPoints(double from, double to) {
        String query = "select kh from KhachHang kh where kh.diemTichLuy between :from and :to";
        return em.createQuery(query, KhachHang.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    // Lưu khách hàng
    public boolean save(KhachHang khachHang) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(khachHang);
            tr.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    // Xóa khách hàng
    public boolean delete(String id) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            KhachHang kh = em.find(KhachHang.class, id);
            if (kh != null) {
                em.remove(kh);
                tr.commit();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    // Cập nhật thông tin khách hàng
    public boolean update(KhachHang khachHang) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(khachHang);
            tr.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    // Lấy khách hàng theo mã
    public KhachHang findById(String id) {
        return em.find(KhachHang.class, id);
    }
}
