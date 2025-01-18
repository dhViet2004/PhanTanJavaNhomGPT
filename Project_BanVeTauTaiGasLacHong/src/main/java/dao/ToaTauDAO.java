package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import lombok.AllArgsConstructor;
import lombok.Data;
import model.ToaTau;
import model.TuyenTau;

import java.util.List;
@AllArgsConstructor
public class ToaTauDAO {
    private EntityManager em ;


    public List<ToaTau> listToaTauBySoGhe(int form, int to){
        String query = "select tt from ToaTau tt "+
                "where soGhe between :form and :to";
        return em.createQuery(query)
                .setParameter("form", form)
                .setParameter("to", to)
                .getResultList();
    }

    public List<ToaTau> getlistToaTau() {
        List<ToaTau> listToaTau = null;
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
