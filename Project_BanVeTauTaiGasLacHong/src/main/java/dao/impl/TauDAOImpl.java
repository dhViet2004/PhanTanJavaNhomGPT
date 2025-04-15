package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.Tau;
import util.JPAUtil;

import java.util.List;
@AllArgsConstructor
public class TauDAOImpl {
    private EntityManager em;
    public TauDAOImpl() {
        this.em = JPAUtil.getEntityManager();;
    }
    public List<Tau> getAllListT() {
        EntityTransaction tx = em.getTransaction();
        List<Tau> list = null;
        tx.begin();
        try {
            list = em.createQuery("select t from Tau t", Tau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            tx.rollback();
        }
        return list;
    }

    public Tau getById(String id) {
        EntityTransaction tr = em.getTransaction();
        return em.find(Tau.class, id);
    }

    public boolean save(Tau t) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
            tr.commit();
            return true;
        }catch(Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public boolean update(Tau t) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(t);
            tr.commit();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public boolean delete(String id) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            Tau t = em.find(Tau.class,id);
            em.remove(t);
            tr.commit();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

}
