package dao.impl;

import dao.TuyenTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.TuyenTau;
import util.JPAUtil;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;


public class TuyenTauDAOImpl extends UnicastRemoteObject implements TuyenTauDAO, Serializable {
//    private EntityManager em;

    public TuyenTauDAOImpl() throws RemoteException {
//        this.em = JPAUtil.getEntityManager();
    }

    public TuyenTau getListTuyenTauByGaDiGaDen(String gaDi, String gaDen) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        return em.createNamedQuery("TuyenTau.findByGaDiGaDen", TuyenTau.class)
                .setParameter(1, gaDi)
                .setParameter(2, gaDen)
                .getSingleResult();
    }
    public TuyenTau getListTuyenTauByDiemDiDiemDen(String diemDi, String diemDen) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        return em.createNamedQuery("TuyenTau.findByDiemDiDiemDen", TuyenTau.class)
                .setParameter(1, "%"+diemDi+"%")
                .setParameter(2, "%"+diemDen+"%")
                .getSingleResult();
    }

    public List<TuyenTau> getListTuyenTau() throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        return em.createNamedQuery("TuyenTau.findAll", TuyenTau.class).getResultList();
    }

    public TuyenTau getTuyenTauById(String id) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(TuyenTau.class, id);
    }
    public TuyenTau getTuyenTauByName(String name) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        return (TuyenTau) em.createNamedQuery("TuyenTau.findByName", TuyenTau.class)
                .setParameter(1, "%"+name+"%")
                .getSingleResult();
    }

    public  boolean save(TuyenTau tuyenTau) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
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
    public  boolean delete(String id) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
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
    public  boolean update(TuyenTau tuyenTau) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
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

    public static void main(String[] args) throws RemoteException {
        TuyenTauDAO tuyenTauDAO = new TuyenTauDAOImpl();
        TuyenTau tmp = tuyenTauDAO.getListTuyenTauByGaDiGaDen("sài gòn","hà nội");
        System.out.println(tmp);
    }
}
