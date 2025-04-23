package dao.impl;

import dao.TaiKhoanDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import model.NhanVien;
import model.TaiKhoan;
import util.JPAUtil;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: TaiKhoanDAO
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */

//@AllArgsConstructor
public class TaiKhoanDAOImpl extends UnicastRemoteObject implements TaiKhoanDAO, Serializable {
    private EntityManager em;
    public TaiKhoanDAOImpl() throws RemoteException {
        this.em = JPAUtil.getEntityManager();;
    }
    @Override
    public TaiKhoan getTaiKhoanById(String id) throws RemoteException {
        return em.find(TaiKhoan.class, id);
    }

    @Override
    public boolean save(TaiKhoan tk) throws RemoteException{
        EntityTransaction tr =  em.getTransaction();
        try {
            tr.begin();
            em.persist(tk);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }
    // update password với maNV
    @Override
    public boolean updatePassword(String maNV, String passWord) throws RemoteException{
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            TaiKhoan tk = em.find(TaiKhoan.class, maNV);
            if (tk != null){
                tk.setPassWord(passWord);
                em.merge(tk);
                tr.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    public boolean delete(String id) throws RemoteException{
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            TaiKhoan tk = em.find(TaiKhoan.class, id);
            em.remove(tk);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    public String getPasswordByPhone(String phone) throws RemoteException{
        String jpql = "SELECT tk.passWord FROM TaiKhoan tk " +
                "JOIN tk.nhanVien nv WHERE nv.soDT = :phone";

        try {
            return em.createQuery(jpql, String.class)
                    .setParameter("phone", phone)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Không tìm thấy
        }
    }

    public String getPasswordByEmail(String email) throws RemoteException{
        String jpql = "SELECT tk.passWord FROM TaiKhoan tk WHERE tk.nhanVien.diaChi = :email";
        try {
            return em.createQuery(jpql, String.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public NhanVien checkLogin(String maNhanVien, String password) throws RemoteException{
        System.out.println(maNhanVien + ": "+password);
        String jpql = "SELECT nv FROM TaiKhoan tk " +
                "JOIN tk.nhanVien nv " + // Sửa JOIN thành với nhanVien
                "WHERE tk.nhanVien.maNV = :maNV AND tk.passWord = :password";


        try {
            return em.createQuery(jpql, NhanVien.class)
                    .setParameter("maNV", maNhanVien)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public boolean insert(TaiKhoan nd) throws RemoteException {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(nd);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
            return false;
        }
    }



}