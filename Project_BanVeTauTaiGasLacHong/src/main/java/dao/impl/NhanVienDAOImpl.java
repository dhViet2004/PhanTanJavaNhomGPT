package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.AllArgsConstructor;
import model.NhanVien;
import model.TaiKhoan;
import util.JPAUtil;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: NhanVienDAO
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
@AllArgsConstructor
public class NhanVienDAOImpl {
    private EntityManager em;
    public NhanVienDAOImpl() {
        this.em = JPAUtil.getEntityManager();;
    }
    public NhanVien getnhanvienById(String id) {
        EntityTransaction tr = em.getTransaction();
        return em.find(NhanVien.class, id);
    }

    public boolean save(NhanVien nv) {
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

    public boolean update(NhanVien nv) {
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

    public boolean delete(String id) {
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

}