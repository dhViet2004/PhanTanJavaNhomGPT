package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.LoaiCho;

import java.util.List;

public class LoaiChoDAO {
    public  List<LoaiCho> getAllList() {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
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
}
