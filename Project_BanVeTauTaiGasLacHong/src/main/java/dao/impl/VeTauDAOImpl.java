package dao.impl;

import dao.VeTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.HoaDon;
import model.KhachHang;
import model.TrangThaiVeTau;
import model.VeTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VeTauDAOImpl extends UnicastRemoteObject implements VeTauDAO {
    public VeTauDAOImpl() throws RemoteException {

    }

    @Override
    public List<VeTau> getAllList() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<VeTau> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT vt FROM VeTau vt", VeTau.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách VeTau");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public VeTau getById(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(VeTau.class, id);
    }

    @Override
    public boolean save(VeTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            tr.rollback();
        }
        return false;
    }

    @Override
    public boolean update(VeTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(t);
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
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            VeTau t = em.find(VeTau.class, id);
            if (t != null) {
                em.remove(t);
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
    public List<VeTau> getByInvoiceId(String invoiceId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<VeTau> list = null;
        try {
            tx.begin();
            String query = "SELECT DISTINCT vt FROM VeTau vt " +
                    "JOIN FETCH vt.chiTietHoaDons cthd " +
                    "JOIN FETCH cthd.hoaDon hd " +
                    "WHERE hd.maHD = :invoiceId";

            list = em.createQuery(query, VeTau.class)
                    .setParameter("invoiceId", invoiceId)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách vé theo hóa đơn: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public boolean updateStatusToReturned(String ticketId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Tìm vé tàu theo maVe
            VeTau veTau = em.find(VeTau.class, ticketId);
            if (veTau != null) {
                // Cập nhật trạng thái vé
                veTau.setTrangThai(TrangThaiVeTau.DA_TRA);
                em.merge(veTau); // Lưu thay đổi vào database
                tx.commit();
                return true;
            } else {
                System.err.println("Không tìm thấy vé với mã: " + ticketId);
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi cập nhật trạng thái vé tàu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return false;
    }

    @Override
    public HoaDon getHoaDonThanhToanByMaVe(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();

        try {
            tr.begin();

            // Tìm hóa đơn thanh toán (có loại hóa đơn LHD003) liên quan đến vé
            String jpql = "SELECT h FROM HoaDon h " +
                    "JOIN FETCH h.chiTietHoaDons ct " +
                    "JOIN FETCH h.khachHang " +
                    "WHERE ct.veTau.maVe = :maVe " +
                    "AND h.loaiHoaDon.maLoaiHoaDon = 'LHD003'";

            List<HoaDon> hoaDons = em.createQuery(jpql, HoaDon.class)
                    .setParameter("maVe", maVe)
                    .getResultList();

            tr.commit();

            if (!hoaDons.isEmpty()) {
                return hoaDons.get(0);  // Trả về hóa đơn đầu tiên (thông thường chỉ có 1)
            }
            return null;
        } catch (Exception e) {
            if (tr.isActive()) {
                tr.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm hóa đơn thanh toán: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public KhachHang getKhachHangByMaVe(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();

        try {
            tr.begin();

            // Tìm khách hàng liên quan đến vé thông qua hóa đơn
            String jpql = "SELECT kh FROM KhachHang kh " +
                    "JOIN kh.hoaDons h " +
                    "JOIN h.chiTietHoaDons ct " +
                    "WHERE ct.veTau.maVe = :maVe";

            List<KhachHang> khachHangs = em.createQuery(jpql, KhachHang.class)
                    .setParameter("maVe", maVe)
                    .getResultList();

            tr.commit();

            if (!khachHangs.isEmpty()) {
                return khachHangs.get(0);
            }
            return null;
        } catch (Exception e) {
            if (tr.isActive()) {
                tr.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm khách hàng từ mã vé: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public Map<String, String> getThongTinGaByMaVe(String maVe) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();

        try {
            tr.begin();

            // Query để lấy thông tin ga đi và ga đến
            String jpql = "SELECT tt.gaDi, tt.gaDen " +  // Chú ý: sử dụng tên trường đúng từ lớp TuyenTau
                    "FROM VeTau v " +
                    "JOIN v.lichTrinhTau l " +
                    "JOIN l.tau t " +
                    "JOIN t.tuyenTau tt " +
                    "WHERE v.maVe = :maVe";

            Object[] result = (Object[]) em.createQuery(jpql)
                    .setParameter("maVe", maVe)
                    .getSingleResult();

            tr.commit();

            Map<String, String> thongTinGa = new HashMap<>();
            if (result != null && result.length == 2) {
                thongTinGa.put("gaDi", (String) result[0]);
                thongTinGa.put("gaDen", (String) result[1]);
            }
            return thongTinGa;

        } catch (Exception e) {
            if (tr.isActive()) {
                tr.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy thông tin ga: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }

        }
    }


}
