package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.ChoNgoi;
import model.LichTrinhTau;

import java.util.List;

public class LichTrinhTauDAO {
    public static List<LichTrinhTau> getAllList() {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<LichTrinhTau> list = null;
        tx.begin();
        try {
            list = em.createQuery("select ltt from LichTrinhTau ltt", LichTrinhTau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau");
        }
        return list;
    }
}
