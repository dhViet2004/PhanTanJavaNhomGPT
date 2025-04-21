package dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import model.ChoNgoi;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.TrangThaiVeTau;
public class ChoNgoiDAOImpl implements dao.ChoNgoiDAO {
    private EntityManager em;

    public ChoNgoiDAOImpl() {
        this.em = JPAUtil.getEntityManager();
    }

    public List<ChoNgoi> getAllList() {
        EntityTransaction tx = em.getTransaction();
        List<ChoNgoi> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT cn FROM ChoNgoi cn", ChoNgoi.class).getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách ChoNgoi");
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public ChoNgoi getById(String id) {
        return em.find(ChoNgoi.class, id);
    }

    public boolean save(ChoNgoi t) {
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

    public boolean update(ChoNgoi t) {
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

    public boolean delete(String id) {
        EntityTransaction tr = em.getTransaction();
        try {
            tr.begin();
            ChoNgoi t = em.find(ChoNgoi.class, id);
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
    public List<ChoNgoi> getListByToa(String maToa) {
        EntityTransaction tx = em.getTransaction();
        List<ChoNgoi> list = null;
        try {
            tx.begin();
            list = em.createQuery("SELECT cn FROM ChoNgoi cn WHERE cn.toaTau.maToa = :maToa", ChoNgoi.class)
                    .setParameter("maToa", maToa)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Lỗi khi lấy danh sách ChoNgoi theo toa");
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Map<String, String> getAvailableSeatsMapByScheduleAndToa(String maLich, String maToa) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        Map<String, String> seatAvailabilityMap = new HashMap<>();

        try {
            tx.begin();

            // JPQL query to get seat IDs and their availability status as string
            String jpql = "SELECT cn.maCho, " +
                    "CASE " +
                    "WHEN vt IS NULL OR vt.trangThai IN (:trongStatuses) THEN 'Trống' " +
                    "WHEN vt.trangThai = :choXacNhan THEN 'Chờ xác nhận' " +
                    "WHEN vt.trangThai = :daThanhToan THEN 'Đã đặt' " +
                    "END AS trangThai " +
                    "FROM LichTrinhTau lt " +
                    "JOIN lt.tau t " +
                    "JOIN t.danhSachToaTau tt " +
                    "JOIN tt.danhSachChoNgoi cn " +
                    "LEFT JOIN cn.veTau vt " +
                    "ON vt.lichTrinhTau.maLich = lt.maLich " +
                    "WHERE lt.maLich = :maLich AND tt.maToa = :maToa";

            Query query = em.createQuery(jpql)
                    .setParameter("maLich", maLich)
                    .setParameter("maToa", maToa)
                    .setParameter("trongStatuses", List.of(TrangThaiVeTau.DA_DOI, TrangThaiVeTau.DA_TRA))
                    .setParameter("choXacNhan", TrangThaiVeTau.CHO_XAC_NHAN)
                    .setParameter("daThanhToan", TrangThaiVeTau.DA_THANH_TOAN);

            // Lấy danh sách kết quả
            List<Object[]> results = query.getResultList();

            // Chuyển kết quả thành Map
            for (Object[] result : results) {
                String maCho = (String) result[0];
                String trangThai = (String) result[1];
                seatAvailabilityMap.put(maCho, trangThai);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RemoteException("Error fetching seat availability map by schedule and toa", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return seatAvailabilityMap;
    }

    public static void main(String[] args) {
        try {
            // Initialize the DAO
            ChoNgoiDAOImpl choNgoiDAO = new ChoNgoiDAOImpl();

            // Test with sample inputs (replace with actual values from your database)
            String maLich = "LLT20250418192106-013"; // Example schedule ID
            String maToa = "T101"; // Example toa ID

            // Call the method and get the result
            Map<String, String> seatAvailabilityMap = choNgoiDAO.getAvailableSeatsMapByScheduleAndToa(maLich, maToa);

            // Print the results
            System.out.println("Seat availability for Schedule: " + maLich + " and Toa: " + maToa);
            if (seatAvailabilityMap.isEmpty()) {
                System.out.println("No seats found.");
            } else {
                seatAvailabilityMap.forEach((maCho, trangThai) ->
                        System.out.println("Seat ID: " + maCho + ", Status: " + trangThai)
                );
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
