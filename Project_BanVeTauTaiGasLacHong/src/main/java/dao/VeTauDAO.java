package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.VeTau;

import java.util.List;

public class VeTauDAO {
    public List<VeTau> getAllList() {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<VeTau> list = null;
        tx.begin();
        try {
            list = em.createQuery("select vt from VeTau vt", VeTau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách VeTau");
        }
        return list;
    }

    public VeTau getById(String id) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        EntityTransaction tr = em.getTransaction();
        return em.find(VeTau.class, id);
    }

    public boolean save(VeTau t) {
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

    public boolean update(VeTau t) {
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
            VeTau t = em.find(VeTau.class, id);
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
