package dao.impl;

import dao.ToaTauDoiVeDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LoaiToa;
import model.ToaTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ToaTauDoiVeDAOImpl extends UnicastRemoteObject implements ToaTauDoiVeDAO {

    // Cache cho danh sách toa tàu, sử dụng ConcurrentHashMap để đảm bảo thread-safe
    private static final Map<String, List<ToaTau>> toaTauCache = new ConcurrentHashMap<>();

    // Thời gian cache tính theo ms (ví dụ: 5 phút)
    private static final long CACHE_EXPIRY_TIME = 5 * 60 * 1000;

    // Map lưu thời gian khi cache được tạo
    private static final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    public ToaTauDoiVeDAOImpl() throws RemoteException {
        super();
    }

    @Override
    public List<ToaTau> getToaTauByMaTau(String maTau) throws RemoteException {
        // Kiểm tra cache trước khi truy vấn database
        if (isCacheValid(maTau)) {
            return toaTauCache.get(maTau);
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            // Tối ưu query để chỉ lấy dữ liệu cần thiết
            String jpql = "SELECT DISTINCT t FROM ToaTau t " +
                    "LEFT JOIN FETCH t.loaiToa lc " +
                    "LEFT JOIN FETCH t.tau ta " +
                    "WHERE t.tau.maTau = :maTau";

            List<ToaTau> dsToaTau = em.createQuery(jpql, ToaTau.class)
                    .setParameter("maTau", maTau)
                    .getResultList();

            // Chỉ tải những thuộc tính thực sự cần thiết
            for (ToaTau toaTau : dsToaTau) {
                if (toaTau.getLoaiToa() != null) {
                    toaTau.getLoaiToa().getTenLoai();
                    toaTau.getLoaiToa().getMaLoai();
                }
                if (toaTau.getTau() != null) {
                    toaTau.getTau().getMaTau();
                }
            }

            tx.commit();

            // Lưu kết quả vào cache
            updateCache(maTau, dsToaTau);

            return dsToaTau;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RemoteException("Lỗi khi lấy danh sách toa tàu: " + e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Kiểm tra xem cache có tồn tại và còn hiệu lực không
     */
    private boolean isCacheValid(String maTau) {
        if (!toaTauCache.containsKey(maTau) || !cacheTimestamps.containsKey(maTau)) {
            return false;
        }

        long timestamp = cacheTimestamps.get(maTau);
        long currentTime = System.currentTimeMillis();

        // Kiểm tra thời gian hết hạn
        return (currentTime - timestamp) < CACHE_EXPIRY_TIME;
    }

    /**
     * Cập nhật cache với dữ liệu mới
     */
    private void updateCache(String maTau, List<ToaTau> dsToaTau) {
        toaTauCache.put(maTau, new ArrayList<>(dsToaTau)); // Tạo bản sao để tránh thay đổi ngoài ý muốn
        cacheTimestamps.put(maTau, System.currentTimeMillis());
    }

    /**
     * Xóa cache khi cần thiết (ví dụ khi dữ liệu thay đổi)
     */
    public void clearCache(String maTau) {
        if (maTau == null) {
            toaTauCache.clear();
            cacheTimestamps.clear();
        } else {
            toaTauCache.remove(maTau);
            cacheTimestamps.remove(maTau);
        }
    }
}