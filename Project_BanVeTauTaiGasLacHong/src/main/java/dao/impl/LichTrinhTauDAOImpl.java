package dao.impl;

import dao.LichTrinhTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.LichTrinhTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class LichTrinhTauDAOImpl extends UnicastRemoteObject implements LichTrinhTauDAO  {
    private EntityManager em;
    public LichTrinhTauDAOImpl() throws RemoteException {
        this.em = JPAUtil.getEntityManager();;
    }
    @Override
    public List<LichTrinhTau> getAllList() throws RemoteException{
        EntityTransaction tx = em.getTransaction();
        List<LichTrinhTau> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT ltt FROM LichTrinhTau ltt", LichTrinhTau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public LichTrinhTau getById(String id) throws RemoteException{
        return em.find(LichTrinhTau.class, id);
    }

    @Override
    public boolean save(LichTrinhTau t) throws RemoteException{
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
    public boolean update(LichTrinhTau t) throws RemoteException {
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
    public boolean delete(LichTrinhTau lichTrinhTau) throws RemoteException{
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            if (!em.contains(lichTrinhTau)) {
                lichTrinhTau = em.merge(lichTrinhTau);
            }
            em.remove(lichTrinhTau);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public boolean delete(String id) throws RemoteException{
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            LichTrinhTau t = em.find(LichTrinhTau.class, id);
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
    public List<LichTrinhTau> getListLichTrinhTauByDate(LocalDate date) throws RemoteException{
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;
        try {
            tr.begin();
            String jpql = "SELECT ltt FROM LichTrinhTau ltt WHERE ltt.ngayDi = :date";
            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .getResultList();
            tr.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày");
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDi(LocalDate date, String gaDi) throws RemoteException {
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;
        try {
            tr.begin();
            String jpql = "SELECT ltt FROM LichTrinhTau ltt WHERE ltt.ngayDi = :date AND ltt.tau.tuyenTau.gaDi = :gaDi";
            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .getResultList();
            tr.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày và ga đi");
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDen(LocalDate date, String gaDi, String gaDen)throws RemoteException {
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;
        try {
            tr.begin();
            String jpql = "SELECT ltt FROM LichTrinhTau ltt WHERE ltt.ngayDi = :date AND ltt.tau.tuyenTau.gaDi = :gaDi AND ltt.tau.tuyenTau.gaDen = :gaDen";
            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .setParameter("gaDen", gaDen)
                    .getResultList();
            tr.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi và ga đến");
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDenAndGioDi(LocalDate date, String gaDi, String gaDen, String gioDi) throws RemoteException{
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;
        try {
            tr.begin();
            // Convert gioDi string to LocalTime
            LocalTime time = LocalTime.parse(gioDi);
            String jpql = "SELECT ltt FROM LichTrinhTau ltt WHERE ltt.ngayDi = :date AND ltt.tau.tuyenTau.gaDi = :gaDi AND ltt.tau.tuyenTau.gaDen = :gaDen AND ltt.gioDi = :gioDi";
            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .setParameter("gaDen", gaDen)
                    .setParameter("gioDi", time)
                    .getResultList();
            tr.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi, ga đến và giờ đi");
        }
        return list;
    }
}
