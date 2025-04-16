package dao.impl;

import dao.TauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.Tau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class TauDAOImpl extends UnicastRemoteObject implements TauDAO {
//    private EntityManager em;
    public TauDAOImpl() throws RemoteException {
//        this.em = JPAUtil.getEntityManager();;
    }
    public List<Tau> getAllListT() {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<Tau> list = null;
        tx.begin();
        try {
            // Sử dụng JOIN FETCH để lấy TuyenTau cùng với Tau
            list = em.createQuery("select t from Tau t JOIN FETCH t.tuyenTau", Tau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            // Đảm bảo đóng EntityManager
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<Tau> getAllWithRoutes() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<Tau> list = null;
        tx.begin();
        try {
            list = em.createQuery("select t from Tau t JOIN FETCH t.tuyenTau", Tau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RemoteException("Lỗi khi lấy danh sách tàu với tuyến", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    public Tau getById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        Tau tau = null;
        try {
            tr.begin();
            // Sử dụng JOIN FETCH trong câu truy vấn
            tau = em.createQuery("SELECT t FROM Tau t LEFT JOIN FETCH t.tuyenTau WHERE t.maTau = :id", Tau.class)
                    .setParameter("id", id)
                    .getSingleResult();
            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            e.printStackTrace();
            // Nếu không tìm thấy kết quả, thử phương pháp thông thường
            tau = em.find(Tau.class, id);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return tau;
    }

    public boolean save(Tau t) {
        EntityManager em = JPAUtil.getEntityManager();
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
        EntityManager em = JPAUtil.getEntityManager();
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
        EntityManager em = JPAUtil.getEntityManager();
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
