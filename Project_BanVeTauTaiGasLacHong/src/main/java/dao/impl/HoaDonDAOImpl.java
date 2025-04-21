package dao.impl;

import dao.HoaDonDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import model.HoaDon;
import model.LoaiHoaDon;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HoaDonDAOImpl extends UnicastRemoteObject implements HoaDonDAO {
    public HoaDonDAOImpl() throws RemoteException {

    }

    @Override
    // Create: Thêm hóa đơn mới
    public boolean saveHoaDon(HoaDon hoaDon) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();

            // Kiểm tra xem mã hóa đơn đã được thiết lập chưa
            if (hoaDon.getMaHD() == null || hoaDon.getMaHD().isEmpty()) {
                // Nếu chưa có mã hóa đơn, tự động sinh mã
                String maHD = generateMaHoaDon(
                        hoaDon.getNgayLap() != null ?
                                hoaDon.getNgayLap().toLocalDate() :
                                LocalDate.now()
                );
                hoaDon.setMaHD(maHD);
            }

            // Kiểm tra các đối tượng liên quan
            if (hoaDon.getLoaiHoaDon() != null && hoaDon.getLoaiHoaDon().getMaLoaiHoaDon() != null) {
                // Lấy loại hóa đơn từ database để đảm bảo liên kết đúng
                LoaiHoaDon loaiHD = getLoaiHoaDonById(hoaDon.getLoaiHoaDon().getMaLoaiHoaDon());
                if (loaiHD != null) {
                    hoaDon.setLoaiHoaDon(loaiHD);
                } else {
                    throw new Exception("Không tìm thấy loại hóa đơn với mã: " + hoaDon.getLoaiHoaDon().getMaLoaiHoaDon());
                }
            }

            // Lưu hóa đơn vào database
            em.persist(hoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            if (tr.isActive()) {
                tr.rollback();
            }
            ex.printStackTrace();
            throw new RemoteException("Lỗi khi lưu hóa đơn: " + ex.getMessage(), ex);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // Read: Lấy danh sách hóa đơn
    @Override
    public List<HoaDon> getAllHoaDons() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<HoaDon> list = null;

        try {
            tx.begin();
            // Use JOIN FETCH to eagerly load related entities
            String jpql = "SELECT h FROM HoaDon h JOIN FETCH h.khachHang";
            list = em.createQuery(jpql, HoaDon.class).getResultList();

            // Ensure all related data is loaded within the transaction
            for (HoaDon hd : list) {
                if (hd.getKhachHang() != null) {
                    hd.getKhachHang().getTenKhachHang();
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách HoaDon: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách HoaDon", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return list;
    }

    // Read: Tìm hóa đơn theo mã hóa đơn
    @Override
    public HoaDon getHoaDonById(String maHD) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(HoaDon.class, maHD);
    }

    // Update: Cập nhật thông tin hóa đơn
    @Override
    public boolean updateHoaDon(HoaDon hoaDon) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(hoaDon);
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }

    // Delete: Xóa hóa đơn theo mã hóa đơn
    @Override
    public boolean deleteHoaDon(String maHD) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            HoaDon hoaDon = em.find(HoaDon.class, maHD);
            if (hoaDon != null) {
                em.remove(hoaDon);
            }
            tr.commit();
            return true;
        } catch (Exception ex) {
            tr.rollback();
            ex.printStackTrace();
        }
        return false;
    }


    // Retrieve invoices by customer ID
    @Override
    public List<HoaDon> getByCustomerId(String customerId) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        String query = "SELECT h FROM HoaDon h WHERE h.khachHang.maKhachHang = :customerId";
        return em.createQuery(query, HoaDon.class)
                .setParameter("customerId", customerId)
                .getResultList();
    }

    @Override
    public String generateMaHoaDon(LocalDate ngay) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();

        try {
            tr.begin();

            // Định dạng phần ngày tháng của mã hóa đơn không có dấu "/"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String datePrefix = "HD" + formatter.format(ngay);

            // Query để tìm mã hóa đơn lớn nhất trong ngày
            String jpql = "SELECT h.maHD FROM HoaDon h WHERE h.maHD LIKE :prefix ORDER BY h.maHD DESC";
            Query query = em.createQuery(jpql)
                    .setParameter("prefix", datePrefix + "%")
                    .setMaxResults(1);

            List<?> results = query.getResultList();

            // Xác định số thứ tự tiếp theo
            int nextNumber = 1;

            if (!results.isEmpty()) {
                String lastCode = (String) results.get(0);
                // Trích xuất số thứ tự từ mã hóa đơn cuối cùng
                try {
                    // Format: HDyyyyMMddXXXX
                    // Lấy 4 ký tự cuối cùng của mã hóa đơn
                    String numberStr = lastCode.substring(lastCode.length() - 4);
                    nextNumber = Integer.parseInt(numberStr) + 1;
                } catch (Exception e) {
                    // Nếu có lỗi khi parse, mặc định là 1
                    nextNumber = 1;
                }
            }

            tr.commit();

            // Tạo mã hóa đơn mới với định dạng HDyyyyMMddXXXX
            return String.format("%s%04d", datePrefix, nextNumber);

        } catch (Exception ex) {
            if (tr.isActive()) {
                tr.rollback();
            }
            ex.printStackTrace();
            throw new RemoteException("Lỗi khi sinh mã hóa đơn: " + ex.getMessage(), ex);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public LoaiHoaDon getLoaiHoaDonById(String maLoaiHD) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();

        try {
            tr.begin();
            LoaiHoaDon loaiHD = em.find(LoaiHoaDon.class, maLoaiHD);

            // Đảm bảo dữ liệu được load trong transaction
            if (loaiHD != null) {
                loaiHD.getTenLoaiHoaDon(); // Touch để load dữ liệu
            }

            tr.commit();
            return loaiHD;
        } catch (Exception ex) {
            if (tr.isActive()) {
                tr.rollback();
            }
            ex.printStackTrace();
            throw new RemoteException("Lỗi khi lấy thông tin loại hóa đơn: " + ex.getMessage(), ex);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

    }
}
