package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.ChoNgoi;
import model.LichTrinhTau;
import model.LoaiCho;

import java.util.ArrayList;
import java.util.List;

public class ChoNgoiDAO {
    public static List<ChoNgoi> getAllList() {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<ChoNgoi> list = null;
        tx.begin();
        try {
            list = em.createQuery("select cn from ChoNgoi cn", ChoNgoi.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách LoaiCho");
        }
        return list;
    }

    public ChoNgoi getById(String id) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tr = em.getTransaction();
        return em.find(ChoNgoi.class, id);
    }

    public boolean save(ChoNgoi t) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public boolean update(ChoNgoi t) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public boolean delete(String id) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            ChoNgoi t = em.find(ChoNgoi.class, id);
            em.remove(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }
}
