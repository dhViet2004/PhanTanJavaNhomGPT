package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.LoaiCho;

import java.util.List;

public class LoaiChoDAO {
    public List<LoaiCho> getAllList() {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<LoaiCho> list = null;
        tx.begin();
        try {
            list = em.createQuery("select lc from LoaiCho lc", LoaiCho.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách LoaiCho");
        }
        return list;
    }

    public LoaiCho getById(String id) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tr = em.getTransaction();
        return em.find(LoaiCho.class, id);
    }

    public boolean save(LoaiCho t) {
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

    public boolean update(LoaiCho t) {
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
            LoaiCho t = em.find(LoaiCho.class, id);
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
