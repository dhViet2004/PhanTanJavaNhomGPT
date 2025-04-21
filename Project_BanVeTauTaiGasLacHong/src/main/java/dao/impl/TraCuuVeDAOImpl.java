package dao.impl;

import dao.TraCuuVeDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import model.*;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TraCuuVeDAOImpl extends UnicastRemoteObject implements TraCuuVeDAO {

    public TraCuuVeDAOImpl() throws RemoteException {
        // Không khởi tạo EntityManager trong constructor
    }

    @Override
    public VeTau timVeTauTheoMa(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        VeTau veTau = null;

        try {
            // Sử dụng JPQL với JOIN FETCH để tải tất cả dữ liệu cần thiết trong một câu query
            String jpql = "SELECT v FROM VeTau v " +
                    "JOIN FETCH v.lichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "JOIN FETCH v.choNgoi c " +
                    "LEFT JOIN FETCH c.toaTau toa " +
                    "LEFT JOIN FETCH toa.loaiToa " +
                    "WHERE v.maVe = :maVe";

            TypedQuery<VeTau> query = em.createQuery(jpql, VeTau.class);
            query.setParameter("maVe", maVe);

            // getSingleResult() có thể ném NoResultException nếu không tìm thấy kết quả
            // nên chúng ta cần bắt ngoại lệ này và xử lý
            try {
                veTau = query.getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                // Không tìm thấy vé, trả về null
                return null;
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm vé tàu theo mã: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm vé tàu theo mã", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return veTau;
    }

    @Override
    public List<VeTau> timDanhSachVeTauTheoMa(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        List<VeTau> danhSachVeTau = new ArrayList<>();

        try {
            // Sử dụng JPQL với JOIN FETCH để tải tất cả dữ liệu cần thiết trong một câu query
            String jpql = "SELECT v FROM VeTau v " +
                    "JOIN FETCH v.lichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "JOIN FETCH v.choNgoi c " +
                    "WHERE v.maVe = :maVe";

            TypedQuery<VeTau> query = em.createQuery(jpql, VeTau.class);
            query.setParameter("maVe", maVe);

            danhSachVeTau = query.getResultList();

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm danh sách vé tàu theo mã: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm danh sách vé tàu theo mã", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return danhSachVeTau;
    }

    @Override
    public ChiTietHoaDon timChiTietHoaDonTheoMaVe(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        ChiTietHoaDon chiTietHoaDon = null;

        try {
            // Sử dụng JPQL để lấy chi tiết hóa đơn mới nhất theo ngày lập
            String jpql = "SELECT c FROM ChiTietHoaDon c " +
                    "JOIN FETCH c.hoaDon h " +
                    "JOIN FETCH c.veTau v " +
                    "WHERE c.id.maVe = :maVe " +
                    "ORDER BY h.ngayLap DESC";  // Sắp xếp theo ngày lập hóa đơn mới nhất

            TypedQuery<ChiTietHoaDon> query = em.createQuery(jpql, ChiTietHoaDon.class);
            query.setParameter("maVe", maVe);
            query.setMaxResults(1);  // Chỉ lấy kết quả đầu tiên (mới nhất)

            List<ChiTietHoaDon> results = query.getResultList();
            if (!results.isEmpty()) {
                chiTietHoaDon = results.get(0);
                System.out.println("Đã tìm thấy chi tiết hóa đơn mới nhất cho mã vé: " + maVe
                        + ", ngày lập: " + chiTietHoaDon.getHoaDon().getNgayLap());
            } else {
                System.out.println("Không tìm thấy chi tiết hóa đơn cho mã vé: " + maVe);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm chi tiết hóa đơn theo mã vé: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm chi tiết hóa đơn theo mã vé", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return chiTietHoaDon;
    }

    @Override
    public List<VeTau> timVeTauTheoGiayTo(String giayTo) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        List<VeTau> danhSachVeTau = new ArrayList<>();

        try {
            // Sử dụng JPQL với JOIN FETCH để tải tất cả dữ liệu cần thiết trong một câu query
            String jpql = "SELECT v FROM VeTau v " +
                    "JOIN FETCH v.lichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "JOIN FETCH v.choNgoi c " +
                    "WHERE v.giayTo = :giayTo";

            TypedQuery<VeTau> query = em.createQuery(jpql, VeTau.class);
            query.setParameter("giayTo", giayTo);

            danhSachVeTau = query.getResultList();

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm vé tàu theo giấy tờ: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm vé tàu theo giấy tờ", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return danhSachVeTau;
    }

    @Override
    public List<VeTau> timVeTauTheoTenKH(String tenKhachHang) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        List<VeTau> danhSachVeTau = new ArrayList<>();

        try {
            // Sử dụng JPQL với JOIN FETCH để tải tất cả dữ liệu cần thiết trong một câu query
            String jpql = "SELECT v FROM VeTau v " +
                    "JOIN FETCH v.lichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "JOIN FETCH v.choNgoi c " +
                    "WHERE v.tenKhachHang = :tenKhachHang";

            TypedQuery<VeTau> query = em.createQuery(jpql, VeTau.class);
            query.setParameter("tenKhachHang", tenKhachHang);

            danhSachVeTau = query.getResultList();

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm vé tàu theo tên khách hàng: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm vé tàu theo tên khách hàng", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return danhSachVeTau;
    }

    @Override
    public List<VeTau> timVeTauTheoChitiet(String tenKhachHang, String giayTo, LocalDate ngayDi, String maChoNgoi, String doiTuong) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        List<VeTau> veTauList = new ArrayList<>();

        try {
            // Sử dụng JPQL với JOIN FETCH để tải tất cả dữ liệu cần thiết trong một câu query
            String jpql = "SELECT v FROM VeTau v " +
                    "JOIN FETCH v.lichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "JOIN FETCH v.choNgoi c " +
                    "WHERE v.tenKhachHang = :tenKhachHang " +
                    "AND v.giayTo = :giayTo " +
                    "AND v.ngayDi = :ngayDi " +
                    "AND v.choNgoi.maCho = :maCho " +
                    "AND v.doiTuong = :doiTuong";

            TypedQuery<VeTau> query = em.createQuery(jpql, VeTau.class);
            query.setParameter("tenKhachHang", tenKhachHang);
            query.setParameter("giayTo", giayTo);
            query.setParameter("ngayDi", ngayDi);
            query.setParameter("maCho", maChoNgoi);
            query.setParameter("doiTuong", doiTuong);

            veTauList = query.getResultList();

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm vé tàu theo chi tiết: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm vé tàu theo chi tiết", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return veTauList;
    }

    @Override
    public List<VeTau> timVeTauTheoTenKHVaThoiGian(String hoTen, LocalDate ngayDiFrom, LocalDate ngayDiTo) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        List<VeTau> veTauList = new ArrayList<>();

        try {
            // Sử dụng JPQL với JOIN FETCH để tải tất cả dữ liệu cần thiết trong một câu query
            String jpql = "SELECT v FROM VeTau v " +
                    "JOIN FETCH v.lichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "JOIN FETCH v.choNgoi c " +
                    "WHERE v.tenKhachHang = :tenKhachHang " +
                    "AND v.ngayDi BETWEEN :ngayDiFrom AND :ngayDiTo";

            TypedQuery<VeTau> query = em.createQuery(jpql, VeTau.class);
            query.setParameter("tenKhachHang", hoTen);
            query.setParameter("ngayDiFrom", ngayDiFrom);
            query.setParameter("ngayDiTo", ngayDiTo);

            veTauList = query.getResultList();

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm vé tàu theo tên khách hàng và thời gian: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm vé tàu theo tên khách hàng và thời gian", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return veTauList;
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
}