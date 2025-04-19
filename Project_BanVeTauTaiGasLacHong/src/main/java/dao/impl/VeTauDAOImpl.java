package dao.impl;

import dao.VeTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.VeTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class VeTauDAOImpl extends UnicastRemoteObject implements VeTauDAO {
    public VeTauDAOImpl() throws RemoteException {

    }

    @Override
    public List<VeTau> getAllList() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<VeTau> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT vt FROM VeTau vt", VeTau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách VeTau");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public VeTau getById(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(VeTau.class, id);
    }

    @Override
    public boolean save(VeTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    public boolean update(VeTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    public boolean delete(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            VeTau t = em.find(VeTau.class, id);
            if (t != null) {
                em.remove(t);
            }
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    public List<VeTau> getByInvoiceId(String invoiceId) throws RemoteException {
//        EntityManager em = JPAUtil.getEntityManager();
//        String query = "SELECT vt FROM VeTau vt WHERE vt.hoaDon.maHD = :invoiceId";
//        return em.createQuery(query, VeTau.class)
//                .setParameter("invoiceId", invoiceId)
//                .getResultList();
            EntityManager em = JPAUtil.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            List<VeTau> list = null;
            try {
                tx.begin();
                String query = "SELECT DISTINCT vt FROM VeTau vt " +
                        "JOIN FETCH vt.chiTietHoaDons cthd " +
                        "JOIN FETCH cthd.hoaDon hd " +
                        "WHERE hd.maHD = :invoiceId";

                list = em.createQuery(query, VeTau.class)
                        .setParameter("invoiceId", invoiceId)
                        .getResultList();
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                System.err.println("Lỗi khi lấy danh sách vé theo hóa đơn: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (em != null && em.isOpen()) {
                    em.close();
                }
            }
            return list;
    }
}
