package dao.impl;

import dao.HoaDonDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.HoaDon;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class HoaDonDAOImpl extends UnicastRemoteObject implements HoaDonDAO {


    public HoaDonDAOImpl() throws RemoteException {

    }

    // Create: Thêm hóa đơn mới
    @Override
    public boolean saveHoaDon(HoaDon hoaDon) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(hoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }

    // Read: Lấy danh sách hóa đơn
    @Override
    public List<HoaDon> getAllHoaDons()  throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        return em.createQuery("SELECT h FROM HoaDon h", HoaDon.class).getResultList();
    }

    // Read: Tìm hóa đơn theo mã hóa đơn
    @Override
    public HoaDon getHoaDonById(String maHD)  throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(HoaDon.class, maHD);
    }

    // Update: Cập nhật thông tin hóa đơn
    @Override
    public boolean updateHoaDon(HoaDon hoaDon)   throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(hoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }

    // Delete: Xóa hóa đơn theo mã hóa đơn
    @Override
    public boolean deleteHoaDon(String maHD) throws RemoteException{
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            HoaDon hoaDon = em.find(HoaDon.class, maHD);
            if (hoaDon != null) {
                em.remove(hoaDon);
            }
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }


    // Retrieve invoices by customer ID
    @Override
    public List<HoaDon> getByCustomerId(String customerId)  throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        String query = "SELECT h FROM HoaDon h WHERE h.khachHang.maKhachHang = :customerId";
        return em.createQuery(query, HoaDon.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }
}
