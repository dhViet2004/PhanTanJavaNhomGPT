package dao.impl;

import dao.LoaiKhachHangDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LoaiKhachHang;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * @Dự án: PhanTanJavaNhomGPT
 * @Class: LoaiKhachHangDAOImpl
 * @Tạo vào ngày: 18/04/2025
 * @Tác giả: Nguyen Huu Sang
 */
public class LoaiKhachHangDAOImpl extends UnicastRemoteObject implements LoaiKhachHangDAO {

    public LoaiKhachHangDAOImpl() throws RemoteException {

    }

    // Save a new customer type
    @Override
    public boolean save(LoaiKhachHang loaiKhachHang) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(loaiKhachHang);
            tr.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    // Update an existing customer type
    public boolean update(LoaiKhachHang loaiKhachHang) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(loaiKhachHang);
            tr.commit();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    // Delete a customer type by ID
    public boolean delete(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            LoaiKhachHang loaiKhachHang = em.find(LoaiKhachHang.class, id);
            if (loaiKhachHang != null) {
                em.remove(loaiKhachHang);
                tr.commit();
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            tr.rollback();
        }
        return false;
    }

//    @Override
//    public List<LoaiKhachHang> getAllList() {
//        return List.of();
//    }

    @Override
    public List<LoaiKhachHang> getAll() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager(); // Ensure EntityManager is initialized
        try {
            String query = "SELECT lkh FROM LoaiKhachHang lkh";
            return em.createQuery(query, LoaiKhachHang.class).getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteException("Error retrieving customer types", ex);
        } finally {
            if (em != null && em.isOpen()) {
                em.close(); // Ensure EntityManager is closed to release resources
            }
        }
    }

    // Find a customer type by ID
    @Override
    public LoaiKhachHang findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(LoaiKhachHang.class, id);
    }



}