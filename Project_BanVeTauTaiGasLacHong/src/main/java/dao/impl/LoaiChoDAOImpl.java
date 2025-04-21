package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.LichTrinhTau;
import model.LoaiCho;
import model.TrangThaiVeTau;
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

    public long getAvailableSeatsCount(LichTrinhTau lichTrinhTau) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        long availableSeatsCount = 0;

        try {
            tx.begin();

            // JPQL query to count available seats for the given LichTrinhTau
            String jpql = """
                SELECT COUNT(cn)
                FROM ChoNgoi cn
                LEFT JOIN cn.veTau vt
                WHERE (vt IS NULL OR vt.trangThai IN (:returned, :exchanged))
                AND vt.lichTrinhTau = :lichTrinhTau
            """;

            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("returned", TrangThaiVeTau.DA_TRA);
            query.setParameter("exchanged", TrangThaiVeTau.DA_DOI);
            query.setParameter("lichTrinhTau", lichTrinhTau);

            availableSeatsCount = query.getSingleResult();

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return availableSeatsCount;
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
