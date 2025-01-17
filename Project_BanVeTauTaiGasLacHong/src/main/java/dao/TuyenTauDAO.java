package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.TuyenTau;

import java.util.List;

@AllArgsConstructor
public class TuyenTauDAO {
    private EntityManager em;

    public List<TuyenTau> listTuyenTauBySoGhe(int form, int to){
        String query = "select tt from TuyenTau tt "+
                "where so_ghe between :form and :to";
        return em.createQuery(query)
                .setParameter("form", form)
                .setParameter("to", to)
                .getResultList();
    }

    public  boolean save(TuyenTau tuyenTau){
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(tuyenTau);
            tr.commit();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }
    public  boolean delete(String id){
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            TuyenTau tt = em.find(TuyenTau.class, id);
            em.remove(tt);
            tr.commit();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

}
