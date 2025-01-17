package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.ChoNgoi;
import model.LoaiCho;

import java.util.ArrayList;
import java.util.List;

public class ChoNgoiDAO {
    public static List<ChoNgoi> getAllList() {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<ChoNgoi> list = null;
        tx.begin();
        try {
            list = em.createQuery("select cn from ChoNgoi cn", ChoNgoi.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách LoaiCho");
        }
        return list;
    }

}
