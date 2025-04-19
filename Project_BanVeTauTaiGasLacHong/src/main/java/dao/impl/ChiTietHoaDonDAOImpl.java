package dao.impl;

import dao.ChiTietHoaDonDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.ChiTietHoaDon;
import model.ChiTietHoaDonId;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ChiTietHoaDonDAOImpl extends UnicastRemoteObject implements ChiTietHoaDonDAO {

    public ChiTietHoaDonDAOImpl() throws RemoteException {
        // Constructor phải có throws RemoteException khi extends UnicastRemoteObject
    }

    @Override
    public List<ChiTietHoaDon> getAllList() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<ChiTietHoaDon> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT c FROM ChiTietHoaDon c", ChiTietHoaDon.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách ChiTietHoaDon");
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public ChiTietHoaDon getById(ChiTietHoaDonId id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        ChiTietHoaDon result = null;
        try {
            result = em.find(ChiTietHoaDon.class, id);
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm ChiTietHoaDon theo ID");
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }

    @Override
    public boolean save(ChiTietHoaDon chiTietHoaDon) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(chiTietHoaDon);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return false;
    }

    @Override
    public boolean update(ChiTietHoaDon chiTietHoaDon) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(chiTietHoaDon);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return false;
    }

    @Override
    public boolean delete(ChiTietHoaDonId id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            ChiTietHoaDon chiTietHoaDon = em.find(ChiTietHoaDon.class, id);
            if (chiTietHoaDon != null) {
                em.remove(chiTietHoaDon);
            }
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return false;
    }

    @Override
    public List<ChiTietHoaDon> getByHoaDonId(String hoaDonId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<ChiTietHoaDon> list = null;
        try {
            tx.begin();
            String query = "SELECT c FROM ChiTietHoaDon c WHERE c.id.maHD = :hoaDonId";
            list = em.createQuery(query, ChiTietHoaDon.class)
                    .setParameter("hoaDonId", hoaDonId)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách chi tiết hóa đơn theo mã hóa đơn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<ChiTietHoaDon> getByVeTauId(String veTauId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<ChiTietHoaDon> list = null;
        try {
            tx.begin();
            String query = "SELECT c FROM ChiTietHoaDon c WHERE c.id.maVe = :veTauId";
            list = em.createQuery(query, ChiTietHoaDon.class)
                    .setParameter("veTauId", veTauId)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách chi tiết hóa đơn theo mã vé: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }
}