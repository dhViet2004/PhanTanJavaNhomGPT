package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.ChoNgoi;

import java.util.List;

public class ChoNgoiDAOImpl {
    private EntityManager em;

    public ChoNgoiDAOImpl(EntityManager em) {
        this.em = em;
    }

    public List<ChoNgoi> getAllList() {
        EntityTransaction tx = em.getTransaction();
        List<ChoNgoi> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT cn FROM ChoNgoi cn", ChoNgoi.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách ChoNgoi");
            e.printStackTrace();
        }
        return list;
    }

    public ChoNgoi getById(String id) {
        return em.find(ChoNgoi.class, id);
    }

    public boolean save(ChoNgoi t) {
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
            ChoNgoi t = em.find(ChoNgoi.class, id);
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
