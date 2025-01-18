package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.TuyenTau;

import java.util.List;


@AllArgsConstructor
public class TuyenTauDAO {
    private EntityManager em;
    public List<TuyenTau> getListTuyenTauByGaDiGaDen(String gaDi, String gaDen) {
        return em.createNamedQuery("TuyenTau.findByGaDiGaDen", TuyenTau.class)
                .setParameter("gaDi", gaDi)
                .setParameter("gaDen",gaDen)
                .getResultList();
    }

    public List<TuyenTau> getListTuyenTau(){
        return em.createNamedQuery("TuyenTau.findAll", TuyenTau.class).getResultList();
    }

    public TuyenTau getTuyenTauById(String id) {
        return em.find(TuyenTau.class, id);
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
    public  boolean update(TuyenTau tuyenTau){
        EntityTransaction tr = em.getTransaction();
            try {
                tr.begin();
                em.merge(tuyenTau);
                tr.commit();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                tr.rollback();
            }
            return false;
    }
}
