package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.NhanVien;
import model.TaiKhoan;
import util.JPAUtil;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: NhanVienDAO
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
//@AllArgsConstructor
public class NhanVienDAOImpl extends UnicastRemoteObject implements dao.NhanVienDAO, Serializable {
    private EntityManager em;
    public NhanVienDAOImpl() throws RemoteException {
        this.em = JPAUtil.getEntityManager();;
    }
    @Override
    public NhanVien getnhanvienById(String id) throws RemoteException {
        EntityTransaction tr = em.getTransaction();
        return em.find(NhanVien.class, id);
    }
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
    public boolean save(NhanVien nv) throws RemoteException {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(nv);

            //create tai khoan với NhanVien
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaNV(nv.getMaNV());
            taiKhoan.setPassWord("Abc123.");
            taiKhoan.setNhanVien(nv);
            em.persist(taiKhoan);

            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    public boolean update(NhanVien nv) throws RemoteException {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(nv);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    public boolean delete(String id) throws RemoteException {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            NhanVien nv = em.find(NhanVien.class, id);
            if (nv != null) {
                // TÌm và xóa tài khoản liên kết
                TaiKhoan taiKhoan = em.find(TaiKhoan.class, id);
                if (taiKhoan != null) {
                    em.remove(taiKhoan);
                }
                em.remove(nv);
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
    public List<NhanVien> getAllNhanVien() throws RemoteException {
        return em.createQuery("from NhanVien nv", NhanVien.class).getResultList();
    }


}