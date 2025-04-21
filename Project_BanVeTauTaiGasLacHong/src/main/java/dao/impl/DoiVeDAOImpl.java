package dao.impl;

import dao.DoiVeDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import model.*;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DoiVeDAOImpl extends UnicastRemoteObject implements DoiVeDAO {

    public DoiVeDAOImpl() throws RemoteException {
        // Không khởi tạo EntityManager trong constructor
    }

    @Override
    public VeTau getVeTau(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        VeTau veTau = null;

        try {
            tx.begin();

            // Sử dụng JOIN FETCH để tải trước dữ liệu liên quan để tránh lỗi LazyInitializationException
            String jpql = "SELECT vt FROM VeTau vt " +
                    "LEFT JOIN FETCH vt.lichTrinhTau ltt " +
                    "LEFT JOIN FETCH vt.choNgoi cn " +
                    "LEFT JOIN FETCH vt.khuyenMai km " +
                    "LEFT JOIN FETCH ltt.tau t " +
                    "LEFT JOIN FETCH t.tuyenTau tt " +
                    "WHERE vt.maVe = :id";

            veTau = em.createQuery(jpql, VeTau.class)
                    .setParameter("id", id)
                    .getSingleResult();

            // Đảm bảo dữ liệu đã được tải đầy đủ
            if (veTau.getLichTrinhTau() != null) {
                veTau.getLichTrinhTau().getMaLich();
                if (veTau.getLichTrinhTau().getTau() != null) {
                    veTau.getLichTrinhTau().getTau().getMaTau();
                    if (veTau.getLichTrinhTau().getTau().getTuyenTau() != null) {
                        veTau.getLichTrinhTau().getTau().getTuyenTau().getGaDi();
                        veTau.getLichTrinhTau().getTau().getTuyenTau().getGaDen();
                    }
                }
            }

            if (veTau.getChoNgoi() != null) {
                veTau.getChoNgoi().getMaCho();
                veTau.getChoNgoi().getGiaTien();
            }

            if (veTau.getKhuyenMai() != null) {
                veTau.getKhuyenMai().getMaKM();
                veTau.getKhuyenMai().getChietKhau();
            }

            tx.commit();
        } catch (NoResultException e) {
            // Không tìm thấy vé
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return null;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi tìm vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return veTau;
    }

    @Override
    public boolean doiVe(VeTau veTau) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Kiểm tra vé có tồn tại không
            VeTau existingVe = em.find(VeTau.class, veTau.getMaVe());
            if (existingVe == null) {
                tx.rollback();
                return false;
            }

            // Kiểm tra trạng thái vé (chỉ đổi được vé DA_THANH_TOAN)
            if (existingVe.getTrangThai() != TrangThaiVeTau.DA_THANH_TOAN) {
                tx.rollback();
                return false;
            }

            // Lưu thông tin chỗ ngồi và lịch trình cũ
            ChoNgoi choNgoiCu = existingVe.getChoNgoi();
            LichTrinhTau lichTrinhCu = existingVe.getLichTrinhTau();

            // Kiểm tra chỗ ngồi mới có khả dụng không và chưa được đặt trong lịch trình mới
            if (veTau.getChoNgoi() != null && veTau.getLichTrinhTau() != null) {
                String maChoNgoi = veTau.getChoNgoi().getMaCho();
                String maLichTrinh = veTau.getLichTrinhTau().getMaLich();

                // Kiểm tra trường hợp không phải là giữ nguyên chỗ ngồi và lịch trình
                boolean giuNguyenChoVaLich = choNgoiCu != null && lichTrinhCu != null &&
                        choNgoiCu.getMaCho().equals(maChoNgoi) &&
                        lichTrinhCu.getMaLich().equals(maLichTrinh);

                if (!giuNguyenChoVaLich) {
                    // Kiểm tra chỗ ngồi mới có khả dụng không
                    ChoNgoi choNgoi = em.find(ChoNgoi.class, maChoNgoi);
                    if (choNgoi == null || !choNgoi.isTinhTrang()) {
                        tx.rollback();
                        throw new RemoteException("Chỗ ngồi không khả dụng hoặc đang sửa chữa.");
                    }

                    // Kiểm tra chỗ ngồi mới đã được đặt trong lịch trình mới chưa
                    String jpql = "SELECT COUNT(v) FROM VeTau v " +
                            "WHERE v.choNgoi.maCho = :maCho " +
                            "AND v.lichTrinhTau.maLich = :maLichTrinh " +
                            "AND v.maVe != :maVe " +
                            "AND v.trangThai NOT IN (:trangThaiDaTra)";

                    Long count = em.createQuery(jpql, Long.class)
                            .setParameter("maCho", maChoNgoi)
                            .setParameter("maLichTrinh", maLichTrinh)
                            .setParameter("maVe", veTau.getMaVe())
                            .setParameter("trangThaiDaTra", TrangThaiVeTau.DA_TRA)
                            .getSingleResult();

                    if (count > 0) {
                        tx.rollback();
                        throw new RemoteException("Chỗ ngồi đã được đặt bởi vé khác trong cùng lịch trình.");
                    }
                }
            }

            // Cập nhật thông tin cơ bản
            existingVe.setTenKhachHang(veTau.getTenKhachHang());
            existingVe.setGiayTo(veTau.getGiayTo());
            existingVe.setNgayDi(veTau.getNgayDi());
            existingVe.setDoiTuong(veTau.getDoiTuong());
            existingVe.setTrangThai(veTau.getTrangThai());
            existingVe.setGiaVe(veTau.getGiaVe());

            // Xóa bỏ liên kết với chỗ ngồi hiện tại
            existingVe.setChoNgoi(null);
            em.flush(); // Đẩy thay đổi xuống DB

            // Cập nhật lịch trình và khuyến mãi
            if (veTau.getLichTrinhTau() != null) {
                LichTrinhTau lichTrinhTau = em.find(LichTrinhTau.class, veTau.getLichTrinhTau().getMaLich());
                existingVe.setLichTrinhTau(lichTrinhTau);
            }

            if (veTau.getKhuyenMai() != null) {
                KhuyenMai khuyenMai = em.find(KhuyenMai.class, veTau.getKhuyenMai().getMaKM());
                existingVe.setKhuyenMai(khuyenMai);
            } else {
                existingVe.setKhuyenMai(null);
            }

            // Cập nhật chỗ ngồi mới
            if (veTau.getChoNgoi() != null) {
                ChoNgoi choNgoi = em.find(ChoNgoi.class, veTau.getChoNgoi().getMaCho());
                existingVe.setChoNgoi(choNgoi);
            }

            em.merge(existingVe);
            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi đổi vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi đổi vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<VeTau> getVeTauByTrangThai(TrangThaiVeTau trangThai) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<VeTau> list = new ArrayList<>();

        try {
            tx.begin();

            // Sử dụng JOIN FETCH để tải trước dữ liệu liên quan
            String jpql = "SELECT vt FROM VeTau vt " +
                    "LEFT JOIN FETCH vt.lichTrinhTau ltt " +
                    "LEFT JOIN FETCH vt.choNgoi cn " +
                    "LEFT JOIN FETCH vt.khuyenMai km " +
                    "LEFT JOIN FETCH ltt.tau t " +
                    "LEFT JOIN FETCH t.tuyenTau tt " +
                    "WHERE vt.trangThai = :trangThai";

            list = em.createQuery(jpql, VeTau.class)
                    .setParameter("trangThai", trangThai)
                    .getResultList();

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách vé theo trạng thái: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách vé theo trạng thái", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return list;
    }

    @Override
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
    public List<TrangThaiVeTau> getAllTrangThaiVe() throws RemoteException {
        List<TrangThaiVeTau> trangThaiList = new ArrayList<>();

        // Thêm tất cả các giá trị enum TrangThaiVeTau
        for (TrangThaiVeTau trangThai : TrangThaiVeTau.values()) {
            trangThaiList.add(trangThai);
        }

        return trangThaiList;
    }

    @Override
    public boolean datVe(VeTau veTau, String choNgoiId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Kiểm tra chỗ ngồi có tồn tại không
            ChoNgoi choNgoi = em.find(ChoNgoi.class, choNgoiId);
            if (choNgoi == null) {
                tx.rollback();
                return false;
            }

            // Kiểm tra chỗ ngồi có trống không
            if (choNgoi.isTinhTrang()) {
                tx.rollback();
                return false;
            }

            // Kiểm tra lịch trình có tồn tại không
            LichTrinhTau lichTrinhTau = em.find(LichTrinhTau.class, veTau.getLichTrinhTau().getMaLich());
            if (lichTrinhTau == null) {
                tx.rollback();
                return false;
            }

            // Tạo mã vé mới nếu chưa có
            if (veTau.getMaVe() == null || veTau.getMaVe().isEmpty()) {
                String maVe = "VE" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                veTau.setMaVe(maVe);
            }

            // Cập nhật thông tin vé
            veTau.setChoNgoi(choNgoi);
            veTau.setTrangThai(TrangThaiVeTau.CHO_XAC_NHAN);

            // Tính giá vé
            double giaVe = choNgoi.getGiaTien();
            if (veTau.getKhuyenMai() != null) {
                KhuyenMai khuyenMai = em.find(KhuyenMai.class, veTau.getKhuyenMai().getMaKM());
                veTau.setKhuyenMai(khuyenMai);
                giaVe *= (1 - khuyenMai.getChietKhau());
            }
            veTau.setGiaVe(giaVe);

            // Cập nhật trạng thái chỗ ngồi
            choNgoi.setTinhTrang(true);
            em.merge(choNgoi);

            // Lưu vé mới
            em.persist(veTau);

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi đặt vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi đặt vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean huyVe(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Kiểm tra vé có tồn tại không
            VeTau veTau = em.find(VeTau.class, maVe);
            if (veTau == null) {
                tx.rollback();
                return false;
            }

            // Kiểm tra trạng thái vé (chỉ hủy được vé ở trạng thái CHO_XAC_NHAN hoặc DA_THANH_TOAN)
            if (veTau.getTrangThai() != TrangThaiVeTau.CHO_XAC_NHAN &&
                    veTau.getTrangThai() != TrangThaiVeTau.DA_THANH_TOAN) {
                tx.rollback();
                return false;
            }

            // Giải phóng chỗ ngồi
            if (veTau.getChoNgoi() != null) {
                ChoNgoi choNgoi = veTau.getChoNgoi();
                choNgoi.setTinhTrang(false);
                em.merge(choNgoi);
            }

            // Cập nhật trạng thái vé thành DA_TRA
            veTau.setTrangThai(TrangThaiVeTau.DA_TRA);
            em.merge(veTau);

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi hủy vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi hủy vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean thanhToanVe(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Kiểm tra vé có tồn tại không
            VeTau veTau = em.find(VeTau.class, maVe);
            if (veTau == null) {
                tx.rollback();
                return false;
            }

            // Kiểm tra trạng thái vé (chỉ thanh toán được vé ở trạng thái CHO_XAC_NHAN)
            if (veTau.getTrangThai() != TrangThaiVeTau.CHO_XAC_NHAN) {
                tx.rollback();
                return false;
            }

            // Cập nhật trạng thái vé thành DA_THANH_TOAN
            veTau.setTrangThai(TrangThaiVeTau.DA_THANH_TOAN);
            em.merge(veTau);

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi thanh toán vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi thanh toán vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean capNhatTrangThaiVe(String maVe, TrangThaiVeTau trangThai) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Kiểm tra vé có tồn tại không
            VeTau veTau = em.find(VeTau.class, maVe);
            if (veTau == null) {
                tx.rollback();
                return false;
            }

            // Cập nhật trạng thái vé
            veTau.setTrangThai(trangThai);
            em.merge(veTau);

            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi cập nhật trạng thái vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi cập nhật trạng thái vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // Add this method to your existing DoiVeDAOImpl class

    @Override
    public KhachHang getKhachHangByMaVe(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        KhachHang khachHang = null;

        try {
            tx.begin();

            // For native queries with named parameters, use :name notation
            String nativeQuery =
                    "SELECT kh.* FROM khachhang kh " +
                            "JOIN hoadon hd ON hd.ma_khach_hang = kh.ma_khach_hang " +
                            "JOIN chitiet_hoadon cthd ON cthd.ma_hd = hd.ma_hd " +
                            "WHERE cthd.ma_ve = ?";

            try {
                khachHang = (KhachHang) em.createNativeQuery(nativeQuery, KhachHang.class)
                        .setParameter(1, maVe)
                        .getSingleResult();

                // Ensure related data is loaded if needed
                if (khachHang != null) {
                    khachHang.getMaKhachHang(); // Trigger loading

                    // If you need to load the loaiKhachHang relation
                    if (khachHang.getLoaiKhachHang() != null) {
                        khachHang.getLoaiKhachHang().getMaLoaiKhachHang();
                    }
                }

            } catch (NoResultException e) {
                // No customer found for this ticket
                return null;
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi tìm khách hàng từ mã vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm khách hàng từ mã vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return khachHang;
    }
}