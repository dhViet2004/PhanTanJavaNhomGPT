package dao.impl;

import dao.LichTrinhTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LichTrinhTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LichTrinhTauDAOImpl extends UnicastRemoteObject implements LichTrinhTauDAO {

    public LichTrinhTauDAOImpl() throws RemoteException {
        // Không khởi tạo EntityManager trong constructor
    }

    @Override
    public List<LichTrinhTau> getAllList() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tx.begin();
            // Sử dụng JOIN FETCH để tải trước dữ liệu của tàu và tuyến tàu
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt";

            list = em.createQuery(jpql, LichTrinhTau.class).getResultList();

            // Đảm bảo dữ liệu đã được tải đầy đủ
            for (LichTrinhTau ltt : list) {
                if (ltt.getTau() != null) {
                    ltt.getTau().getMaTau();
                    if (ltt.getTau().getTuyenTau() != null) {
                        ltt.getTau().getTuyenTau().getGaDi();
                        ltt.getTau().getTuyenTau().getGaDen();
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau", e);
        } finally {
            // Đóng EntityManager sau khi hoàn thành
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public LichTrinhTau getById(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        LichTrinhTau lichTrinhTau = null;

        try {
            lichTrinhTau = em.find(LichTrinhTau.class, id);

            // Đảm bảo dữ liệu liên kết được tải
            if (lichTrinhTau != null && lichTrinhTau.getTau() != null) {
                lichTrinhTau.getTau().getMaTau();
                if (lichTrinhTau.getTau().getTuyenTau() != null) {
                    lichTrinhTau.getTau().getTuyenTau().getGaDi();
                    lichTrinhTau.getTau().getTuyenTau().getGaDen();
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm LichTrinhTau theo ID: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm LichTrinhTau theo ID", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return lichTrinhTau;
    }

    @Override
    public boolean save(LichTrinhTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lưu LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lưu LichTrinhTau", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean update(LichTrinhTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi cập nhật LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi cập nhật LichTrinhTau", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean delete(LichTrinhTau lichTrinhTau) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
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
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi xóa LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi xóa LichTrinhTau", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean delete(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            LichTrinhTau t = em.find(LichTrinhTau.class, id);
            if (t != null) {
                em.remove(t);
                tr.commit();
                return true;
            } else {
                tr.rollback();
                return false;
            }
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi xóa LichTrinhTau theo ID: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi xóa LichTrinhTau theo ID", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDate(LocalDate date) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = new ArrayList<>();

        try {
            tr.begin();
            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDi(LocalDate date, String gaDi) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tr.begin();
            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date AND tt.gaDi = :gaDi";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày và ga đi: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày và ga đi", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDen(LocalDate date, String gaDi, String gaDen) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tr.begin();
            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date AND tt.gaDi = :gaDi AND tt.gaDen = :gaDen";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .setParameter("gaDen", gaDen)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi và ga đến: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi và ga đến", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDenAndGioDi(LocalDate date, String gaDi, String gaDen, String gioDi) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tr.begin();
            // Chuyển đổi gioDi từ chuỗi sang LocalTime
            LocalTime time = LocalTime.parse(gioDi);

            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date AND tt.gaDi = :gaDi " +
                    "AND tt.gaDen = :gaDen AND ltt.gioDi = :gioDi";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .setParameter("gaDen", gaDen)
                    .setParameter("gioDi", time)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi, ga đến và giờ đi: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi, ga đến và giờ đi", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public boolean testConnection() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.createQuery("SELECT 1").getResultList();
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra kết nối: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<String> getTrangThai() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<String> trangThaiList = new ArrayList<>();

        try {
            tx.begin();

            // Sử dụng DISTINCT để lấy tất cả các trạng thái khác nhau
            String jpql = "SELECT DISTINCT ltt.trangThai FROM LichTrinhTau ltt ORDER BY ltt.trangThai";
            trangThaiList = em.createQuery(jpql, String.class).getResultList();

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách trạng thái: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách trạng thái", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return trangThaiList;
    }
}