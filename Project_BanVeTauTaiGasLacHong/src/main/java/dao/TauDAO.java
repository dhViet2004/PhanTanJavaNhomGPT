package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.LoaiCho;
import model.Tau;

import java.util.List;

public class TauDAO {
    public static List<Tau> getAllList() {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<Tau> list = null;
        tx.begin();
        try {
            list = em.createQuery("select t from Tau t", Tau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách Tau");
        }
        return list;
    }
}
