package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.LichLamViec;
import util.JPAUtil;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: LichLamViecDAO
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
@AllArgsConstructor
public class LichLamViecDAOImpl {
    private EntityManager em;

    public LichLamViecDAOImpl(){
        this.em = JPAUtil.getEntityManager();
    }
    public LichLamViec getLichLamViecById(String id) {
        return em.find(LichLamViec.class, id);
    }

    public boolean save(LichLamViec llv) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(llv);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public boolean update(LichLamViec llv) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(llv);
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
            LichLamViec llv = em.find(LichLamViec.class, id);
            em.remove(llv);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

}