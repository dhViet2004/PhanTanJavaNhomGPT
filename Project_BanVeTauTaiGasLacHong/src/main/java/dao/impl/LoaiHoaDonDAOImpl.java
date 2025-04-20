package dao.impl;

import dao.LoaiHoaDonDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import model.LoaiHoaDon;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class LoaiHoaDonDAOImpl extends UnicastRemoteObject implements LoaiHoaDonDAO {

    public LoaiHoaDonDAOImpl() throws RemoteException {
        // Constructor without initialization
    }

    @Override
    public LoaiHoaDon findById(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        LoaiHoaDon loaiHoaDon = null;

        try {
            tx.begin();

            // First try to find by direct entity lookup
            loaiHoaDon = em.find(LoaiHoaDon.class, id);

            // If not found with direct lookup, try with JPQL
            if (loaiHoaDon == null) {
                try {
                    String jpql = "SELECT lhd FROM LoaiHoaDon lhd WHERE lhd.maLoaiHoaDon = :id";
                    loaiHoaDon = em.createQuery(jpql, LoaiHoaDon.class)
                            .setParameter("id", id)
                            .getSingleResult();
                } catch (NoResultException e) {
                    // No result found
                    return null;
                }
            }

            // Ensure data is loaded if found
            if (loaiHoaDon != null) {
                loaiHoaDon.getMaLoaiHoaDon(); // Trigger loading
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi tìm loại hóa đơn theo ID: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm loại hóa đơn theo ID: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return loaiHoaDon;
    }
}