package dao.impl;

import dao.ChoNgoiCallback;
import dao.ChoNgoiDoiVeDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.ChoNgoi;
import model.TrangThaiVeTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChoNgoiDoiVeDAOImpl extends UnicastRemoteObject implements ChoNgoiDoiVeDAO {
    // Lưu trữ các client callback để thông báo khi có thay đổi
    private final List<ChoNgoiCallback> clientCallbacks = new CopyOnWriteArrayList<>();

    // Lưu trữ các chỗ ngồi đã khóa tạm thời: <maCho:maLichTrinh, <sessionId, thời điểm hết hạn>>
    private final Map<String, Map<String, Long>> choNgoiDaKhoa = new ConcurrentHashMap<>();

    public ChoNgoiDoiVeDAOImpl() throws RemoteException {
        super();
        // Khởi động thread dọn dẹp các khóa hết hạn
        Thread cleanupThread = new Thread(this::cleanupExpiredLocks);
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    @Override
    public List<ChoNgoi> getChoNgoiByToaTau(String maToaTau) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        List<ChoNgoi> dsChoNgoi = new ArrayList<>();

        try {
            tx.begin();

            // Sử dụng JOIN FETCH để tải trước dữ liệu liên quan
            String jpql = "SELECT DISTINCT c FROM ChoNgoi c " +
                    "LEFT JOIN FETCH c.loaiCho lc " +
                    "WHERE c.toaTau.maToa = :maToaTau";

            dsChoNgoi = em.createQuery(jpql, ChoNgoi.class)
                    .setParameter("maToaTau", maToaTau)
                    .getResultList();

            // Đảm bảo các thuộc tính lazy được tải
            for (ChoNgoi choNgoi : dsChoNgoi) {
                if (choNgoi.getLoaiCho() != null) {
                    choNgoi.getLoaiCho().getTenLoai();
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RemoteException("Lỗi khi lấy danh sách chỗ ngồi: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return dsChoNgoi;
    }

    @Override
    public boolean kiemTraChoNgoiKhaDung(String maCho) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            // Kiểm tra chỗ ngồi có khả dụng không (true = có thể sử dụng, false = đang sửa chữa)
            ChoNgoi choNgoi = em.find(ChoNgoi.class, maCho);
            if (choNgoi == null) {
                return false; // Không tìm thấy chỗ ngồi
            }

            return choNgoi.isTinhTrang(); // true = có thể sử dụng, false = đang sửa chữa
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public boolean kiemTraChoNgoiDaDat(String maCho, String maLichTrinh) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();

        try {
            // Kiểm tra chỗ ngồi có tồn tại không
            ChoNgoi choNgoi = em.find(ChoNgoi.class, maCho);
            if (choNgoi == null) {
                return true; // Không tìm thấy chỗ ngồi, xem như không thể đặt
            }

            // Kiểm tra chỗ ngồi có khả dụng không (tinhTrang = true)
            if (!choNgoi.isTinhTrang()) {
                return true; // Chỗ ngồi đang sửa chữa, không thể đặt
            }

            // Kiểm tra xem chỗ ngồi có đang được đặt trong cùng lịch trình không
            String jpql = "SELECT COUNT(v) FROM VeTau v WHERE v.choNgoi.maCho = :maCho " +
                    "AND v.lichTrinhTau.maLich = :maLichTrinh " +
                    "AND v.trangThai NOT IN (:trangThaiDaTra)";

            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("maCho", maCho)
                    .setParameter("maLichTrinh", maLichTrinh)
                    .setParameter("trangThaiDaTra", TrangThaiVeTau.DA_TRA)
                    .getSingleResult();

            if (count > 0) {
                return true; // Chỗ ngồi đã được đặt trong lịch trình này
            }

            // Kiểm tra trong cache khóa tạm thời
            String lockKey = maCho + ":" + maLichTrinh;
            Map<String, Long> locks = choNgoiDaKhoa.get(lockKey);
            return locks != null && !locks.isEmpty(); // Nếu có khóa tạm thời, xem như đã đặt

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public synchronized boolean khoaChoNgoi(String maCho, String maLichTrinh, String sessionId, long thoiGianKhoaMillis) throws RemoteException {
        // Kiểm tra chỗ ngồi có khả dụng không
        if (!kiemTraChoNgoiKhaDung(maCho)) {
            return false; // Chỗ ngồi không khả dụng (đang sửa chữa)
        }

        // Kiểm tra chỗ ngồi đã được đặt trong lịch trình này chưa
        EntityManager em = JPAUtil.getEntityManager();

        try {
            // Kiểm tra xem chỗ ngồi đã có vé đặt trong cùng lịch trình chưa
            String jpql = "SELECT COUNT(v) FROM VeTau v WHERE v.choNgoi.maCho = :maCho " +
                    "AND v.lichTrinhTau.maLich = :maLichTrinh " +
                    "AND v.trangThai NOT IN (:trangThaiDaTra)";

            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("maCho", maCho)
                    .setParameter("maLichTrinh", maLichTrinh)
                    .setParameter("trangThaiDaTra", TrangThaiVeTau.DA_TRA)
                    .getSingleResult();

            if (count > 0) {
                return false; // Chỗ ngồi đã được đặt trong lịch trình này
            }

            // Kiểm tra trong cache khóa tạm thời
            String lockKey = maCho + ":" + maLichTrinh;
            Map<String, Long> sessionLocks = choNgoiDaKhoa.get(lockKey);

            if (sessionLocks != null && !sessionLocks.isEmpty()) {
                // Nếu session hiện tại đang khóa, cập nhật thời gian hết hạn
                if (sessionLocks.containsKey(sessionId)) {
                    long expirationTime = System.currentTimeMillis() + thoiGianKhoaMillis;
                    sessionLocks.put(sessionId, expirationTime);
                    return true;
                }

                // Nếu session khác đang khóa, không thể khóa
                return false;
            }

            // Tạo khóa mới
            Map<String, Long> newLock = new ConcurrentHashMap<>();
            long expirationTime = System.currentTimeMillis() + thoiGianKhoaMillis;
            newLock.put(sessionId, expirationTime);
            choNgoiDaKhoa.put(lockKey, newLock);

            // Thông báo cho các client khác
            thongBaoCapNhatTrangThaiChoNgoi(maCho, maLichTrinh, true, sessionId);

            return true;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public synchronized boolean huyKhoaChoNgoi(String maCho, String maLichTrinh, String sessionId) throws RemoteException {
        String lockKey = maCho + ":" + maLichTrinh;
        Map<String, Long> sessionLocks = choNgoiDaKhoa.get(lockKey);

        if (sessionLocks != null && sessionLocks.containsKey(sessionId)) {
            sessionLocks.remove(sessionId);

            if (sessionLocks.isEmpty()) {
                choNgoiDaKhoa.remove(lockKey);
            }

            // Thông báo cho các client khác
            thongBaoCapNhatTrangThaiChoNgoi(maCho, maLichTrinh, false, sessionId);

            return true;
        }

        return false;
    }

    @Override
    public boolean capNhatKhaNangSuDungChoNgoi(String maCho, boolean khaDung) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ChoNgoi choNgoi = em.find(ChoNgoi.class, maCho);
            if (choNgoi == null) {
                tx.rollback();
                return false;
            }

            // Cập nhật trạng thái sử dụng của chỗ ngồi
            choNgoi.setTinhTrang(khaDung);
            em.merge(choNgoi);

            tx.commit();

            // Thông báo cập nhật
            for (ChoNgoiCallback callback : clientCallbacks) {
                try {
                    callback.capNhatKhaNangSuDungChoNgoi(maCho, khaDung);
                } catch (RemoteException e) {
                    System.err.println("Lỗi khi gửi thông báo đến client: " + e.getMessage());
                }
            }

            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RemoteException("Lỗi khi cập nhật khả năng sử dụng chỗ ngồi: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public int dongBoTrangThaiDatCho() throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        int count = 0;

        try {
            tx.begin();

            // Reset cache khóa tạm thời
            choNgoiDaKhoa.clear();

            // Lấy danh sách tất cả các chỗ ngồi có thể sử dụng
            String jpql = "SELECT c FROM ChoNgoi c";
            List<ChoNgoi> choNgoiList = em.createQuery(jpql, ChoNgoi.class).getResultList();

            for (ChoNgoi choNgoi : choNgoiList) {
                // Chúng ta không thay đổi giá trị tinh_trang ở đây vì nó chỉ đại diện cho việc chỗ ngồi có thể sử dụng hay không
                count++;
            }

            tx.commit();

            // Thông báo cho tất cả clients
            thongBaoTatCaClient();

            return count;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RemoteException("Lỗi khi đồng bộ trạng thái đặt chỗ: " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public void dangKyClientChoThongBao(ChoNgoiCallback callback) throws RemoteException {
        if (!clientCallbacks.contains(callback)) {
            clientCallbacks.add(callback);
        }
    }

    @Override
    public void huyDangKyClientChoThongBao(ChoNgoiCallback callback) throws RemoteException {
        clientCallbacks.remove(callback);
    }

    // Phương thức thông báo cập nhật trạng thái chỗ ngồi cho tất cả client
    private void thongBaoCapNhatTrangThaiChoNgoi(String maCho, String maLichTrinh, boolean daDat, String sessionId) {
        for (ChoNgoiCallback callback : clientCallbacks) {
            try {
                callback.capNhatTrangThaiDatChoNgoi(maCho, maLichTrinh, daDat, sessionId);
            } catch (RemoteException e) {
                System.err.println("Lỗi khi gửi thông báo đến client: " + e.getMessage());
                // Xóa callback không hoạt động
                clientCallbacks.remove(callback);
            }
        }
    }

    // Phương thức thông báo cho tất cả client
    private void thongBaoTatCaClient() {
        for (ChoNgoiCallback callback : clientCallbacks) {
            try {
                callback.capNhatDanhSachChoNgoi();
            } catch (RemoteException e) {
                System.err.println("Lỗi khi gửi thông báo đến client: " + e.getMessage());
                // Xóa callback không hoạt động
                clientCallbacks.remove(callback);
            }
        }
    }

    // Thread dọn dẹp các khóa hết hạn
    private void cleanupExpiredLocks() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(10000); // Kiểm tra mỗi 10 giây

                long currentTime = System.currentTimeMillis();
                boolean hasChanges = false;

                // Duyệt qua từng lock key
                for (String lockKey : new ArrayList<>(choNgoiDaKhoa.keySet())) {
                    Map<String, Long> sessionLocks = choNgoiDaKhoa.get(lockKey);

                    if (sessionLocks != null) {
                        // Duyệt qua từng session ID
                        for (String sessionId : new ArrayList<>(sessionLocks.keySet())) {
                            Long expirationTime = sessionLocks.get(sessionId);

                            if (expirationTime != null && expirationTime < currentTime) {
                                // Khóa đã hết hạn
                                sessionLocks.remove(sessionId);
                                hasChanges = true;
                            }
                        }

                        // Nếu không còn session nào, xóa lock key
                        if (sessionLocks.isEmpty()) {
                            choNgoiDaKhoa.remove(lockKey);

                            // Phân tách mã chỗ và mã lịch trình
                            String[] parts = lockKey.split(":");
                            if (parts.length == 2) {
                                String maCho = parts[0];
                                String maLichTrinh = parts[1];
                                thongBaoCapNhatTrangThaiChoNgoi(maCho, maLichTrinh, false, null);
                            }
                        }
                    }
                }

                // Nếu có thay đổi, thông báo cho tất cả client
                if (hasChanges) {
                    thongBaoTatCaClient();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Lỗi khi dọn dẹp các khóa hết hạn: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}