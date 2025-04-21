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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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


//    @Override
//    public List<HoaDon> timKiemHoaDon(String maHoaDon, String soDienThoai, String maNhanVien, LocalDate tuNgay, LocalDate denNgay) throws RemoteException {
//        EntityManager em = JPAUtil.getEntityManager();
//
//        try {
//            StringBuilder jpql = new StringBuilder("SELECT DISTINCT h FROM HoaDon h " +
//                    "JOIN FETCH h.khachHang kh " +
//                    "JOIN FETCH h.nv nv " +
//                    "JOIN FETCH h.loaiHoaDon " +
//                    "WHERE 1=1 ");
//
//            List<String> conditions = new ArrayList<>();
//
//            if (maHoaDon != null && !maHoaDon.isEmpty()) {
//                conditions.add("h.maHD LIKE :maHoaDon");
//            }
//
//            if (soDienThoai != null && !soDienThoai.isEmpty()) {
//                conditions.add("kh.sdt LIKE :sdt");
//            }
//
//            if (maNhanVien != null && !maNhanVien.isEmpty()) {
//                conditions.add("nv.maNV LIKE :maNhanVien");
//            }
//
//            if (tuNgay != null) {
//                conditions.add("h.ngayLap >= :tuNgay");
//            }
//
//            if (denNgay != null) {
//                conditions.add("h.ngayLap <= :denNgay");
//            }
//
//            for (int i = 0; i < conditions.size(); i++) {
//                jpql.append(" AND ").append(conditions.get(i));
//            }
//
//            jpql.append(" ORDER BY h.ngayLap DESC");
//
//            Query query = em.createQuery(jpql.toString());
//
//            if (maHoaDon != null && !maHoaDon.isEmpty()) {
//                query.setParameter("maHoaDon", "%" + maHoaDon + "%");
//            }
//
//            if (soDienThoai != null && !soDienThoai.isEmpty()) {
//                query.setParameter("soDienThoai", "%" + soDienThoai + "%");
//            }
//
//            if (maNhanVien != null && !maNhanVien.isEmpty()) {
//                query.setParameter("maNhanVien", "%" + maNhanVien + "%");
//            }
//
//            if (tuNgay != null) {
//                LocalDateTime tuDateTime = LocalDateTime.of(tuNgay, LocalTime.MIN);
//                query.setParameter("tuNgay", tuDateTime);
//            }
//
//            if (denNgay != null) {
//                LocalDateTime denDateTime = LocalDateTime.of(denNgay, LocalTime.MAX);
//                query.setParameter("denNgay", denDateTime);
//            }
//
//            return query.getResultList();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RemoteException("Lỗi khi tìm kiếm hóa đơn: " + e.getMessage());
//        } finally {
//            if (em != null && em.isOpen()) {
//                em.close();
//            }
//        }
//    }
//
//
//    @Override
//    public HoaDon getHoaDonByMa(String maHoaDon) throws RemoteException {
//        EntityManager em = JPAUtil.getEntityManager();
//
//        try {
//            String jpql = "SELECT h FROM HoaDon h " +
//                    "JOIN FETCH h.khachHang " +
//                    "JOIN FETCH h.nv " +
//                    "JOIN FETCH h.loaiHoaDon " +
//                    "LEFT JOIN FETCH h.chiTietHoaDons ct " +
//                    "LEFT JOIN FETCH ct.veTau " +
//                    "WHERE h.maHD = :maHoaDon";
//
//            return em.createQuery(jpql, HoaDon.class)
//                    .setParameter("maHoaDon", maHoaDon)
//                    .getSingleResult();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RemoteException("Lỗi khi lấy thông tin hóa đơn: " + e.getMessage());
//        } finally {
//            if (em != null && em.isOpen()) {
//                em.close();
//            }
//        }
//    }

    @Override
    public List<HoaDon> timKiemHoaDon(String maHoaDon, String soDienThoai, String maNhanVien,
                                      LocalDate tuNgay, LocalDate denNgay) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            StringBuilder jpql = new StringBuilder("SELECT DISTINCT h FROM HoaDon h " +
                    "JOIN FETCH h.khachHang kh " +
                    "JOIN FETCH h.nv nv " +
                    "JOIN FETCH h.loaiHoaDon " +
                    "WHERE 1=1 ");

            // Ưu tiên tìm theo mã hóa đơn nếu có
            if (maHoaDon != null && !maHoaDon.isEmpty()) {
                jpql.append(" AND h.maHD LIKE :maHoaDon");
                Query query = em.createQuery(jpql.toString());
                query.setParameter("maHoaDon", "%" + maHoaDon + "%");
                return query.getResultList();
            }

            // Nếu không tìm theo mã, tiếp tục tìm theo SĐT khách hàng
            if (soDienThoai != null && !soDienThoai.isEmpty()) {
                // Cập nhật tên trường đúng theo Entity KhachHang
                // mặc dù tên trường trong Entity là "soDienThoai" nhưng mapping với cột "sdt" trong database
                jpql.append(" AND kh.soDienThoai LIKE :soDienThoai");
                Query query = em.createQuery(jpql.toString());
                query.setParameter("soDienThoai", "%" + soDienThoai + "%");
                return query.getResultList();
            }

            // Nếu không tìm theo SĐT, tiếp tục tìm theo mã nhân viên
            if (maNhanVien != null && !maNhanVien.isEmpty()) {
                jpql.append(" AND nv.maNV LIKE :maNhanVien");
                Query query = em.createQuery(jpql.toString());
                query.setParameter("maNhanVien", "%" + maNhanVien + "%");
                return query.getResultList();
            }

            // Nếu không tìm theo tiêu chí nào ở trên, tìm theo khoảng thời gian
            if (tuNgay != null || denNgay != null) {
                if (tuNgay != null) {
                    jpql.append(" AND h.ngayLap >= :tuNgay");
                }

                if (denNgay != null) {
                    jpql.append(" AND h.ngayLap <= :denNgay");
                }

                jpql.append(" ORDER BY h.ngayLap DESC");

                Query query = em.createQuery(jpql.toString());

                if (tuNgay != null) {
                    LocalDateTime tuDateTime = LocalDateTime.of(tuNgay, LocalTime.MIN);
                    query.setParameter("tuNgay", tuDateTime);
                }

                if (denNgay != null) {
                    LocalDateTime denDateTime = LocalDateTime.of(denNgay, LocalTime.MAX);
                    query.setParameter("denNgay", denDateTime);
                }

                return query.getResultList();
            }

            // Nếu không có tiêu chí nào được nhập, trả về tất cả hóa đơn (có thể giới hạn số lượng)
            jpql.append(" ORDER BY h.ngayLap DESC");
            Query query = em.createQuery(jpql.toString());
            query.setMaxResults(100); // Giới hạn chỉ lấy 100 hóa đơn gần nhất
            return query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm kiếm hóa đơn: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public HoaDon getHoaDonByMa(String maHoaDon) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            String jpql = "SELECT h FROM HoaDon h " +
                    "JOIN FETCH h.khachHang " +
                    "JOIN FETCH h.nv " +
                    "JOIN FETCH h.loaiHoaDon " +
                    "LEFT JOIN FETCH h.chiTietHoaDons ct " +
                    "LEFT JOIN FETCH ct.veTau " +
                    "WHERE h.maHD = :maHoaDon";

            return em.createQuery(jpql, HoaDon.class)
                    .setParameter("maHoaDon", maHoaDon)
                    .getSingleResult();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy thông tin hóa đơn: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<HoaDon> getHoaDonsByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT h FROM HoaDon h " +
                    "JOIN FETCH h.nv " +
                    "WHERE FUNCTION('DATE', h.ngayLap) BETWEEN :startDate AND :endDate";

            return em.createQuery(jpql, HoaDon.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy hóa đơn theo ngày: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }

    public List<HoaDon> getHoaDonsByDateRangeAndShift(LocalDate startDate, LocalDate endDate, int ca) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT h FROM HoaDon h " +
                    "JOIN FETCH h.nv " +
                    "WHERE FUNCTION('DATE', h.ngayLap) BETWEEN :startDate AND :endDate ";

            switch (ca) {
                case 1:
                    jpql += "AND FUNCTION('HOUR', h.ngayLap) BETWEEN 6 AND 13";
                    break;
                case 2:
                    jpql += "AND FUNCTION('HOUR', h.ngayLap) BETWEEN 14 AND 21";
                    break;
                case 3:
                    jpql += "AND (FUNCTION('HOUR', h.ngayLap) >= 22 OR FUNCTION('HOUR', h.ngayLap) <= 5)";
                    break;
            }

            return em.createQuery(jpql, HoaDon.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy hóa đơn theo ca: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
    }



}
