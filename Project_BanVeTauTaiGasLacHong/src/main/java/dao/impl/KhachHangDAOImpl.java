package dao.impl;

import dao.KhachHangDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.KhachHang;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class KhachHangDAOImpl extends UnicastRemoteObject implements KhachHangDAO {

    public KhachHangDAOImpl() throws RemoteException {
    }

    @Override
    public List<KhachHang> getAll() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        List<KhachHang> list = null;
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Use JOIN FETCH to eagerly load the customer type to avoid LazyInitializationException
            String jpql = "SELECT kh FROM KhachHang kh JOIN FETCH kh.loaiKhachHang";

            list = em.createQuery(jpql, KhachHang.class).getResultList();

            // Ensure data is fully loaded
            for (KhachHang kh : list) {
                if (kh.getLoaiKhachHang() != null) {
                    kh.getLoaiKhachHang().getTenLoaiKhachHang();
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách KhachHang: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách KhachHang", e);
        } finally {
            // Close EntityManager after completion
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    // Lấy danh sách khách hàng theo tên
public List<KhachHang> listKhachHangsByName(String name) throws RemoteException {
    EntityManager em = JPAUtil.getEntityManager();
    EntityTransaction tx = em.getTransaction();
    List<KhachHang> result = null;

    try {
        tx.begin();
        // Use JOIN FETCH to eagerly load customer type
        String query = "SELECT kh FROM KhachHang kh JOIN FETCH kh.loaiKhachHang WHERE kh.tenKhachHang LIKE :name";
        result = em.createQuery(query, KhachHang.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();

        // Ensure related data is accessed within the transaction
        for (KhachHang kh : result) {
            if (kh.getLoaiKhachHang() != null) {
                kh.getLoaiKhachHang().getTenLoaiKhachHang();
            }
        }

        tx.commit();
    } catch (Exception e) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        System.err.println("Lỗi khi tìm kiếm KhachHang theo tên: " + e.getMessage());
        e.printStackTrace();
        throw new RemoteException("Lỗi khi tìm kiếm KhachHang theo tên", e);
    } finally {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    return result;
}

    // Lấy danh sách khách hàng có điểm tích lũy trong khoảng
public List<KhachHang> listKhachHangsByPoints(double from, double to) throws RemoteException {
    EntityManager em = JPAUtil.getEntityManager();
    EntityTransaction tx = em.getTransaction();
    List<KhachHang> result = null;

    try {
        tx.begin();
        // Use JOIN FETCH to eagerly load customer type to avoid LazyInitializationException
        String query = "SELECT kh FROM KhachHang kh JOIN FETCH kh.loaiKhachHang WHERE kh.diemTichLuy BETWEEN :from AND :to";
        result = em.createQuery(query, KhachHang.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        // Ensure data is fully loaded within transaction
        for (KhachHang kh : result) {
            if (kh.getLoaiKhachHang() != null) {
                kh.getLoaiKhachHang().getTenLoaiKhachHang();
            }
        }

        tx.commit();
    } catch (Exception e) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        System.err.println("Lỗi khi tìm kiếm KhachHang theo điểm tích lũy: " + e.getMessage());
        e.printStackTrace();
        throw new RemoteException("Lỗi khi tìm kiếm KhachHang theo điểm tích lũy", e);
    } finally {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
    return result;
}

    // Lưu khách hàng
@Override
public boolean save(KhachHang khachHang) throws RemoteException {
    EntityManager em = JPAUtil.getEntityManager();
    EntityTransaction tr = em.getTransaction();
    try {
        tr.begin();
        em.persist(khachHang);
        tr.commit();
        return true;
    } catch (Exception e) {
        if (tr != null && tr.isActive()) {
            tr.rollback();
        }
        System.err.println("Lỗi khi lưu KhachHang: " + e.getMessage());
        e.printStackTrace();
        throw new RemoteException("Lỗi khi lưu KhachHang", e);
    } finally {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}

    // Xóa khách hàng
@Override
public boolean delete(String id) throws RemoteException {
    EntityManager em = JPAUtil.getEntityManager();
    EntityTransaction tr = em.getTransaction();
    try {
        tr.begin();
        KhachHang kh = em.find(KhachHang.class, id);
        if (kh != null) {
            em.remove(kh);
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
        System.err.println("Lỗi khi xóa KhachHang: " + e.getMessage());
        e.printStackTrace();
        throw new RemoteException("Lỗi khi xóa KhachHang", e);
    } finally {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}

    // Cập nhật thông tin khách hàng
@Override
public boolean update(KhachHang khachHang) throws RemoteException {
    EntityManager em = JPAUtil.getEntityManager();
    EntityTransaction tr = em.getTransaction();
    try {
        tr.begin();
        em.merge(khachHang);
        tr.commit();
        return true;
    } catch (Exception e) {
        if (tr != null && tr.isActive()) {
            tr.rollback();
        }
        System.err.println("Lỗi khi cập nhật KhachHang: " + e.getMessage());
        e.printStackTrace();
        throw new RemoteException("Lỗi khi cập nhật KhachHang", e);
    } finally {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}



    // Lấy khách hàng theo mã
    public KhachHang findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(KhachHang.class, id);
    }

    // Search customers by phone number
    public List<KhachHang> searchByPhone(String phone) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<KhachHang> result = null;

        try {
            tx.begin();
            String query = "SELECT kh FROM KhachHang kh JOIN FETCH kh.loaiKhachHang WHERE kh.soDienThoai LIKE :phone";
            result = em.createQuery(query, KhachHang.class)
                    .setParameter("phone", "%" + phone + "%")
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi tìm kiếm KhachHang theo số điện thoại: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm kiếm KhachHang theo số điện thoại", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }

    @Override
    public List<String> getTenKhachHang() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<String> result = null;

        try {
            tx.begin();
            String query = "SELECT kh.tenKhachHang FROM KhachHang kh";
            result = em.createQuery(query, String.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách tên khách hàng: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách tên khách hàng", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }


    // Filter customers by type
public List<KhachHang> filterByType(String typeName) throws RemoteException {
    EntityManager em = JPAUtil.getEntityManager();
    EntityTransaction tx = em.getTransaction();
    List<KhachHang> result = null;

    try {
        tx.begin();
        // Use JOIN FETCH to eagerly load customer type data
        String query = "SELECT kh FROM KhachHang kh JOIN FETCH kh.loaiKhachHang WHERE kh.loaiKhachHang.tenLoaiKhachHang = :typeName";
        result = em.createQuery(query, KhachHang.class)
                .setParameter("typeName", typeName)
                .getResultList();

        // Ensure related data is fully loaded within transaction
        for (KhachHang kh : result) {
            if (kh.getLoaiKhachHang() != null) {
                kh.getLoaiKhachHang().getTenLoaiKhachHang();
            }
        }

        tx.commit();
    } catch (Exception e) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        System.err.println("Lỗi khi lọc KhachHang theo loại: " + e.getMessage());
        e.printStackTrace();
        throw new RemoteException("Lỗi khi lọc KhachHang theo loại", e);
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
//    @Override
//    public KhachHang getById(String id) {
//        return null;
//    }
}
