package dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import lombok.AllArgsConstructor;
import model.TaiKhoan;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: TaiKhoanDAO
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */

@AllArgsConstructor
public class TaiKhoanDAO {
    private EntityManager em;

    public TaiKhoan getTaiKhoanById(String id) {
        return em.find(TaiKhoan.class, id);
    }

    public boolean save (TaiKhoan tk) {
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
    public boolean updatePassword (String maNV, String passWord) {
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

    public  boolean delete (String id) {
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
}