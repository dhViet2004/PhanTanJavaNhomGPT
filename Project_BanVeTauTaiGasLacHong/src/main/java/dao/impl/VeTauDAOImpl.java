package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.VeTau;

import java.util.List;

public class VeTauDAOImpl {
    private EntityManager em;

    public VeTauDAOImpl(EntityManager em) {
        this.em = em;
    }

    public List<VeTau> getAllList() {
        EntityTransaction tx = em.getTransaction();
        List<VeTau> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT vt FROM VeTau vt", VeTau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách VeTau");
            e.printStackTrace();
        }
        return list;
    }

    public VeTau getById(String id) {
        return em.find(VeTau.class, id);
    }

    public boolean save(VeTau t) {
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
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            VeTau t = em.find(VeTau.class, id);
            if (t != null) {
                em.remove(t);
            }
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }
}
