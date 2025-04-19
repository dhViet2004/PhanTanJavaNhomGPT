package dao.impl;

import dao.LichTrinhTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import model.LichTrinhTau;
import model.TrangThai;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LichTrinhTauDAOImpl extends UnicastRemoteObject implements LichTrinhTauDAO {

    public LichTrinhTauDAOImpl() throws RemoteException {
        // Không khởi tạo EntityManager trong constructor
    }

    @Override
    public List<LichTrinhTau> getAllList() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tx.begin();
            // Sử dụng JOIN FETCH để tải trước dữ liệu của tàu và tuyến tàu
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt";

            list = em.createQuery(jpql, LichTrinhTau.class).getResultList();

            // Đảm bảo dữ liệu đã được tải đầy đủ
            for (LichTrinhTau ltt : list) {
                if (ltt.getTau() != null) {
                    ltt.getTau().getMaTau();
                    if (ltt.getTau().getTuyenTau() != null) {
                        ltt.getTau().getTuyenTau().getGaDi();
                        ltt.getTau().getTuyenTau().getGaDen();
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau", e);
        } finally {
            // Đóng EntityManager sau khi hoàn thành
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public LichTrinhTau getById(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        LichTrinhTau lichTrinhTau = null;

        try {
            lichTrinhTau = em.find(LichTrinhTau.class, id);

            // Đảm bảo dữ liệu liên kết được tải
            if (lichTrinhTau != null && lichTrinhTau.getTau() != null) {
                lichTrinhTau.getTau().getMaTau();
                if (lichTrinhTau.getTau().getTuyenTau() != null) {
                    lichTrinhTau.getTau().getTuyenTau().getGaDi();
                    lichTrinhTau.getTau().getTuyenTau().getGaDen();
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm LichTrinhTau theo ID: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm LichTrinhTau theo ID", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return lichTrinhTau;
    }

    @Override
    public boolean save(LichTrinhTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.persist(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lưu LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lưu LichTrinhTau", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean update(LichTrinhTau t) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            em.merge(t);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi cập nhật LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi cập nhật LichTrinhTau", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean delete(LichTrinhTau lichTrinhTau) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            if (!em.contains(lichTrinhTau)) {
                lichTrinhTau = em.merge(lichTrinhTau);
            }
            em.remove(lichTrinhTau);
            tr.commit();
            return true;
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi xóa LichTrinhTau: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi xóa LichTrinhTau", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean delete(String id) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            LichTrinhTau t = em.find(LichTrinhTau.class, id);
            if (t != null) {
                em.remove(t);
                tr.commit();
                return true;
            } else {
                tr.rollback();
                return false;
            }
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi xóa LichTrinhTau theo ID: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi xóa LichTrinhTau theo ID", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDate(LocalDate date) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = new ArrayList<>();

        try {
            tr.begin();
            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDi(LocalDate date, String gaDi) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tr.begin();
            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date AND tt.gaDi = :gaDi";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày và ga đi: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày và ga đi", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDen(LocalDate date, String gaDi, String gaDen) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tr.begin();
            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date AND tt.gaDi = :gaDi AND tt.gaDen = :gaDen";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .setParameter("gaDen", gaDen)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi và ga đến: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi và ga đến", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDenAndGioDi(LocalDate date, String gaDi, String gaDen, String gioDi) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tr = em.getTransaction();
        List<LichTrinhTau> list = null;

        try {
            tr.begin();

            // Xử lý định dạng giờ đi đúng
            LocalTime time;
            try {
                // Kiểm tra định dạng giờ (HH:mm) trước
                if (gioDi.matches("\\d{1,2}:\\d{2}")) {
                    time = LocalTime.parse(gioDi);
                } else if (gioDi.matches("\\d{1,2}")) {
                    // Nếu người dùng chỉ nhập số giờ (không có phút)
                    time = LocalTime.of(Integer.parseInt(gioDi), 0);
                } else {
                    throw new IllegalArgumentException("Định dạng giờ không hợp lệ. Sử dụng định dạng HH:mm hoặc HH");
                }
            } catch (Exception e) {
                throw new RemoteException("Định dạng giờ không hợp lệ: " + gioDi + ". Vui lòng sử dụng định dạng HH:mm hoặc HH", e);
            }

            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.ngayDi = :date AND tt.gaDi = :gaDi " +
                    "AND tt.gaDen = :gaDen AND ltt.gioDi = :gioDi";

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("date", date)
                    .setParameter("gaDi", gaDi)
                    .setParameter("gaDen", gaDen)
                    .setParameter("gioDi", time)
                    .getResultList();

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi, ga đến và giờ đi: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo ngày, ga đi, ga đến và giờ đi", e);
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
    public List<TrangThai> getTrangThai() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<TrangThai> trangThaiList = new ArrayList<>();

        try {
            tx.begin();

            // Sử dụng native query để lấy các giá trị chuỗi từ DB
            Query query = em.createNativeQuery("SELECT DISTINCT trang_thai FROM lichtrinhtau ORDER BY trang_thai");
            List<String> result = query.getResultList();

            // Thêm các giá trị mặc định
            trangThaiList.add(TrangThai.DA_KHOI_HANH);
            trangThaiList.add(TrangThai.CHUA_KHOI_HANH);
            trangThaiList.add(TrangThai.DA_HUY);
            trangThaiList.add(TrangThai.HOAT_DONG); // Thêm giá trị này

            // Xử lý các giá trị từ database
            for (String statusStr : result) {
                try {
                    // Thử chuyển đổi chuỗi thành enum
                    TrangThai status = TrangThai.valueOf(statusStr);
                    // Chỉ thêm nếu chưa có trong danh sách
                    if (!trangThaiList.contains(status)) {
                        trangThaiList.add(status);
                    }
                } catch (IllegalArgumentException e) {
                    // Ghi log và bỏ qua giá trị không hợp lệ
                    System.err.println("Cảnh báo: Bỏ qua giá trị trạng thái không xác định trong cơ sở dữ liệu: " + statusStr);
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách trạng thái: " + e.getMessage());
            e.printStackTrace();

            // Đảm bảo luôn trả về ít nhất một số trạng thái cơ bản
            trangThaiList.clear();
            trangThaiList.add(TrangThai.DA_KHOI_HANH);
            trangThaiList.add(TrangThai.CHUA_KHOI_HANH);
            trangThaiList.add(TrangThai.DA_HUY);
            trangThaiList.add(TrangThai.HOAT_DONG);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return trangThaiList;
    }
    @Override
    public List<LichTrinhTau> getListLichTrinhTauByDateRange(LocalDate startDate, LocalDate endDate) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<LichTrinhTau> result = new ArrayList<>();

        try {
            tx.begin();
            // Sử dụng JOIN FETCH để tải trước dữ liệu của tàu và tuyến tàu để tránh lỗi LazyInitializationException
            String jpql = "SELECT lt FROM LichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE lt.ngayDi BETWEEN :startDate AND :endDate";

            result = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();

            // Đảm bảo dữ liệu đã được tải đầy đủ
            for (LichTrinhTau lt : result) {
                if (lt.getTau() != null) {
                    lt.getTau().getMaTau();
                    if (lt.getTau().getTuyenTau() != null) {
                        lt.getTau().getTuyenTau().getGaDi();
                        lt.getTau().getTuyenTau().getGaDen();
                    }
                }
            }

            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo khoảng ngày: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo khoảng ngày", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<String> getAllStations() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<String> stations = new ArrayList<>();

        try {
            tx.begin();

            // Lấy tất cả các ga đi từ bảng TuyenTau
            String queryGaDi = "SELECT DISTINCT tt.gaDi FROM TuyenTau tt ORDER BY tt.gaDi";
            List<String> gaDiList = em.createQuery(queryGaDi, String.class).getResultList();
            stations.addAll(gaDiList);

            // Lấy tất cả các ga đến từ bảng TuyenTau mà không trùng với ga đi đã có
            String queryGaDen = "SELECT DISTINCT tt.gaDen FROM TuyenTau tt WHERE tt.gaDen NOT IN (:gaDiList) ORDER BY tt.gaDen";
            List<String> gaDenList = em.createQuery(queryGaDen, String.class)
                    .setParameter("gaDiList", gaDiList)
                    .getResultList();
            stations.addAll(gaDenList);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách ga: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách ga", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return stations;
    }

    @Override
    public List<LichTrinhTau> getListLichTrinhTauByTrangThai(TrangThai... trangThai) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<LichTrinhTau> list = new ArrayList<>();

        try {
            tx.begin();

            // Sử dụng JOIN FETCH để tránh lỗi LazyInitializationException
            String jpql = "SELECT ltt FROM LichTrinhTau ltt " +
                    "JOIN FETCH ltt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE ltt.trangThai IN :trangThaiList " +
                    "ORDER BY ltt.ngayDi, ltt.gioDi";

            // Chuyển danh sách trạng thái thành danh sách để sử dụng trong query
            List<TrangThai> trangThaiList = List.of(trangThai);

            list = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("trangThaiList", trangThaiList)
                    .getResultList();

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo trạng thái: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo trạng thái", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }
    @Override
    public List<LichTrinhTau> getListLichTrinhTauByMaTauAndNgayDi(String maTau, LocalDate ngayDi) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<LichTrinhTau> result = new ArrayList<>();

        try {
            tx.begin();

            // Sử dụng JOIN FETCH để tải trước dữ liệu của tàu và tuyến tàu
            String jpql = "SELECT lt FROM LichTrinhTau lt " +
                    "JOIN FETCH lt.tau t " +
                    "JOIN FETCH t.tuyenTau tt " +
                    "WHERE lt.ngayDi = :ngayDi AND t.maTau = :maTau";

            result = em.createQuery(jpql, LichTrinhTau.class)
                    .setParameter("ngayDi", ngayDi)
                    .setParameter("maTau", maTau)
                    .getResultList();

            // Đảm bảo dữ liệu đã được tải đầy đủ
            for (LichTrinhTau lt : result) {
                if (lt.getTau() != null) {
                    lt.getTau().getMaTau();
                    if (lt.getTau().getTuyenTau() != null) {
                        lt.getTau().getTuyenTau().getGaDi();
                        lt.getTau().getTuyenTau().getGaDen();
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi lấy danh sách LichTrinhTau theo tàu và ngày đi: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi lấy danh sách LichTrinhTau theo tàu và ngày đi", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return result;
    }
}