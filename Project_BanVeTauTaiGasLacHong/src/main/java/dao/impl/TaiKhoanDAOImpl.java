package dao.impl;

import dao.TaiKhoanDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import model.NhanVien;
import model.TaiKhoan;
import util.JPAUtil;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: TaiKhoanDAO
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */

@AllArgsConstructor
public class TaiKhoanDAOImpl implements TaiKhoanDAO {
    private EntityManager em;
    public TaiKhoanDAOImpl() {
        this.em = JPAUtil.getEntityManager();;
    }
    @Override
    public TaiKhoan getTaiKhoanById(String id) {
        return em.find(TaiKhoan.class, id);
    }

    @Override
    public boolean save(TaiKhoan tk) {
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
public boolean updatePassword(String maNV, String passWord) {
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
    public boolean delete(String id) {
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

    public String getPasswordByEmail(String email) {
        String jpql = "SELECT tk.passWord FROM TaiKhoan tk " +
                "JOIN tk.nhanVien nv WHERE nv.diaChi = :email";

        try {
            return em.createQuery(jpql, String.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Không tìm thấy
        }
    }

    public NhanVien checkLogin(String maNhanVien, String password) {
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
    public boolean insert(TaiKhoan nd) {
        try {
            em.persist(nd); // Thêm vào context
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void main(String[] args) {
            TaiKhoanDAOImpl dao = new TaiKhoanDAOImpl();
            NhanVien vv = dao.checkLogin("NV0003","Abc123.");
        System.out.println(vv.toString());
    }

}