package dao.impl;

import dao.LichLamViecDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import model.LichLamViec;
import util.JPAUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: LichLamViecDAO
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
@AllArgsConstructor
public class LichLamViecDAOImpl implements LichLamViecDAO {
    private EntityManager em;

    public LichLamViecDAOImpl(){
        this.em = JPAUtil.getEntityManager();
    }
    @Override
    public LichLamViec getLichLamViecById(String id) {
        return em.find(LichLamViec.class, id);
    }

    @Override
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

    @Override
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

    @Override
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
    @Override
    public List<LichLamViec> getCaLamViecForDate(String maNhanVien, LocalDate today) {
        try {
            String jpql = "SELECT llv FROM LichLamViec llv " +
                    "WHERE llv.nhanVien.maNV = :maNV AND FUNCTION('DATE', llv.gioBatDau) = :ngay";

            return em.createQuery(jpql, LichLamViec.class)
                    .setParameter("maNV", maNhanVien)
                    .setParameter("ngay", today)
                    .getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    @Override
    public void updateTrangThai(String maLichLamViec, String trangThai) {
        String jpql = "UPDATE LichLamViec llv SET llv.trangThai = :tt WHERE llv.maLichLamViec = :ma";

        em.createQuery(jpql)
                .setParameter("tt", trangThai)
                .setParameter("ma", maLichLamViec)
                .executeUpdate();

        System.out.println("Cập nhật trạng thái thành công!");
    }


}