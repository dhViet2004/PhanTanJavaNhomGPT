package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.ToaTau;

import java.util.List;

public class ToaTauDAO {
    public static List<ToaTau> getlistToaTau() {
        List<ToaTau> listToaTau = null;
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            listToaTau = em.createQuery("select tt from ToaTau tt", ToaTau.class).getResultList();
            tx.commit();
            System.err.println("Lấy danh sách ToaTau thành công");
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lấy danh sách ToaTau thất bại");
        }
        return listToaTau;
    }
}
