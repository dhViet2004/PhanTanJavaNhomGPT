package dao.impl;

import dao.DoiVeDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.TrangThaiVeTau;
import model.VeTau;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DoiVeDAOImpl extends UnicastRemoteObject implements DoiVeDAO {
    private EntityManager entityManager;
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("QuanLyDuongSat");

    public DoiVeDAOImpl() throws RemoteException {
        this.entityManager = emf.createEntityManager();
    }

    @Override
    public VeTau getVeTau(String id) throws RemoteException {
        try {
            return entityManager.find(VeTau.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm vé: " + e.getMessage());
        }
    }

    @Override
    public boolean doiVe(VeTau veTau) throws RemoteException {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            // Kiểm tra vé có tồn tại không
            VeTau existingVe = entityManager.find(VeTau.class, veTau.getMaVe());
            if (existingVe == null) {
                transaction.rollback();
                return false;
            }

            // Kiểm tra trạng thái vé (chỉ đổi được vé chưa sử dụng)
            if (existingVe.getTrangThai() != TrangThaiVeTau.CHO_XAC_NHAN) {
                transaction.rollback();
                return false;
            }

            // Cập nhật thông tin vé
            existingVe.setTenKhachHang(veTau.getTenKhachHang());
            existingVe.setGiayTo(veTau.getGiayTo());
            existingVe.setNgayDi(veTau.getNgayDi());
            existingVe.setDoiTuong(veTau.getDoiTuong());

            // Cập nhật quan hệ nếu thay đổi
            if (veTau.getLichTrinhTau() != null) {
                existingVe.setLichTrinhTau(veTau.getLichTrinhTau());
            }

            if (veTau.getChoNgoi() != null) {
                existingVe.setChoNgoi(veTau.getChoNgoi());
            }

            if (veTau.getKhuyenMai() != null) {
                existingVe.setKhuyenMai(veTau.getKhuyenMai());
                // Tính toán lại giá vé nếu áp dụng khuyến mãi mới
                double giaGoc = existingVe.getChoNgoi().getGiaTien();
                double chietKhau = veTau.getKhuyenMai().getChietKhau();
                existingVe.setGiaVe(giaGoc * (1 - chietKhau));
            }

            entityManager.merge(existingVe);
            transaction.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RemoteException("Lỗi khi đổi vé: " + e.getMessage());
        }
    }

    // Phương thức để đóng EntityManager khi không cần sử dụng nữa
    public void closeEntityManager() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }

    // Phương thức để đóng EntityManagerFactory khi ứng dụng kết thúc
    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}