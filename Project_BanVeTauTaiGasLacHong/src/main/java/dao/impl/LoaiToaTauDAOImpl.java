package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.LoaiToa;
import util.JPAUtil;

import java.util.List;
@AllArgsConstructor
public class LoaiToaTauDAOImpl {
    private EntityManager em;
    public LoaiToaTauDAOImpl() {
        this.em = JPAUtil.getEntityManager();;
    }
    public List<LoaiToa> getListLoaiToa(){
        return em.createNamedQuery("LoaiToa.findAll", LoaiToa.class)
                .getResultList();
    }

    public LoaiToa getLoaiToaById(String id){
        return em.createNamedQuery("LoaiToa.findByID", LoaiToa.class)
                .setParameter("maLoaiToa", id)
                .getSingleResult();
    }
    public boolean save(LoaiToa loaiToa){
        EntityTransaction tx = em.getTransaction();
       try {
           tx.begin();
           em.persist(loaiToa);
           tx.commit();
           return true;
       }catch (Exception e){
           e.printStackTrace();
           tx.rollback();
       }
       return false;
    }

    public boolean update(LoaiToa loaiToa){
        EntityTransaction tr = em.getTransaction();
        try{
            tr.begin();
            em.merge(loaiToa);
            tr.commit();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public boolean deleteById(String id){
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            LoaiToa loaiToa = getLoaiToaById(id);
            em.remove(loaiToa);
            tr.commit();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }
}
