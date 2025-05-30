package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.ToaTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ToaTauDAOImpl extends UnicastRemoteObject implements dao.ToaTauDAO {
    private EntityManager em ;
    public ToaTauDAOImpl() throws RemoteException {
        this.em = JPAUtil.getEntityManager();
    }

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
    @Override
    public ToaTau getToaTauById(String id) throws RemoteException {
        return em.find(ToaTau.class, id);
    }

    public boolean save(ToaTau toaTau) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(toaTau);
            tx.commit();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        }
        return false;
    }

    public boolean update(ToaTau toaTau) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(toaTau);
            tx.commit();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        }
        return false;
    }

    public boolean delete(String id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ToaTau toaTau = em.find(ToaTau.class, id);
            em.remove(toaTau);
            tx.commit();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        }
        return false;
    }


    @Override
    public List<ToaTau> getToaByTau(String maTau) throws RemoteException {
        EntityTransaction tx = em.getTransaction();
        List<ToaTau> toaTauList = null;

        try {
            tx.begin();
            String jpql = "SELECT tt FROM ToaTau tt WHERE tt.tau.maTau = :maTau";
            toaTauList = em.createQuery(jpql, ToaTau.class)
                    .setParameter("maTau", maTau)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        }

        return toaTauList;
    }

}
