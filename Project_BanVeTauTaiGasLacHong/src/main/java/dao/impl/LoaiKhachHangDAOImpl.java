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

    private static EntityManager em;

    public LoaiKhachHangDAOImpl() throws RemoteException {
        super();
        this.em = JPAUtil.getEntityManager();
    }

    // Save a new customer type
    public boolean save(LoaiKhachHang loaiKhachHang) {
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

    // Update an existing customer type
    public boolean update(LoaiKhachHang loaiKhachHang) {
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

    // Delete a customer type by ID
    public boolean delete(String id) {
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
        try {
            String query = "SELECT lkh FROM LoaiKhachHang lkh";
            return em.createQuery(query, LoaiKhachHang.class).getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RemoteException("Error retrieving customer types", ex);
        }
    }

    // Find a customer type by ID
    public LoaiKhachHang findById(String id) {
        return em.find(LoaiKhachHang.class, id);
    }



}