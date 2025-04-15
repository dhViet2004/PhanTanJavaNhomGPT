package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LoaiCho;
import util.JPAUtil;

import java.util.List;

public class LoaiChoDAOImpl {
    private EntityManager em;

    public LoaiChoDAOImpl() {
        this.em = JPAUtil.getEntityManager();;
    }

    public List<LoaiCho> getAllList() {
        EntityTransaction tx = em.getTransaction();
        List<LoaiCho> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT lc FROM LoaiCho lc", LoaiCho.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách LoaiCho");
            e.printStackTrace();
        }
        return list;
    }

    public LoaiCho getById(String id) {
        return em.find(LoaiCho.class, id);
    }

    public boolean save(LoaiCho t) {
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
            LoaiCho t = em.find(LoaiCho.class, id);
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
