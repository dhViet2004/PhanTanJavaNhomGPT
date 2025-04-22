package dao.impl;

import dao.KhuyenMaiDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.KhuyenMai;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class KhuyenMaiDAOImpl extends UnicastRemoteObject implements KhuyenMaiDAO  {

    private EntityManager em;

    public KhuyenMaiDAOImpl() throws RemoteException {
        this.em = JPAUtil.getEntityManager();
    }
    // Lấy danh sách tất cả các khuyến mãi
    @Override
    public List<KhuyenMai> findAll() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<KhuyenMai> result = new ArrayList<>();

        try {
            tx.begin();
            String query = "select km from KhuyenMai km";
            result = em.createQuery(query, KhuyenMai.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách khuyến mãi", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }

    // Lấy danh sách khuyến mãi theo tên
    @Override
    public List<KhuyenMai> findByName(String name) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<KhuyenMai> result = new ArrayList<>();

        try {
            tx.begin();
            String query = "select km from KhuyenMai km where km.tenKM like :name";
            result = em.createQuery(query, KhuyenMai.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm khuyến mãi theo tên", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }

    // Lấy khuyến mãi theo mã
    @Override
    public KhuyenMai findById(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        KhuyenMai result = null;

        try {
            tx.begin();
            result = em.find(KhuyenMai.class, id);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm khuyến mãi theo mã", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }

    // Thêm hoặc cập nhật khuyến mãi
    @Override
    public boolean save(KhuyenMai khuyenMai) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            if (em.find(KhuyenMai.class, khuyenMai.getMaKM()) == null) {
                em.persist(khuyenMai); // Thêm mới
            } else {
                em.merge(khuyenMai); // Cập nhật
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lưu khuyến mãi", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // Xóa khuyến mãi theo mã
    @Override
    public boolean delete(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            KhuyenMai km = em.find(KhuyenMai.class, id);
            if (km != null) {
                em.remove(km);
                tx.commit();
                return true;
            }
            tx.commit();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi xóa khuyến mãi", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    // Tìm các khuyến mãi đang áp dụng
    @Override
    public List<KhuyenMai> findOngoingPromotions() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<KhuyenMai> result = new ArrayList<>();

        try {
            tx.begin();
            String query = "select km from KhuyenMai km " +
                    "where km.trangThai = :trangThai " +
                    "and km.thoiGianBatDau <= :today " +
                    "and km.thoiGianKetThuc >= :today";

            result = em.createQuery(query, KhuyenMai.class)
                    .setParameter("trangThai", "Đang diễn ra")
                    .setParameter("today", LocalDate.now())
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm khuyến mãi đang áp dụng", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }

    @Override
    public boolean testConnection() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("SELECT 1").getResultList();
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<KhuyenMai> findPromotionsForAllByScheduleDate(LocalDate scheduleDate) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<KhuyenMai> result = new ArrayList<>();

        try {
            tx.begin();
            String query = "select km from KhuyenMai km " +
                    "where km.trangThai = model.TrangThaiKM.DANG_DIEN_RA " +
                    "and km.doiTuongApDung = model.DoiTuongApDung.ALL " +
                    "and km.thoiGianBatDau <= :scheduleDate " +
                    "and km.thoiGianKetThuc >= :scheduleDate";

            result = em.createQuery(query, KhuyenMai.class)
                    .setParameter("scheduleDate", scheduleDate)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm khuyến mãi áp dụng cho tất cả theo ngày lịch trình", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }


}
