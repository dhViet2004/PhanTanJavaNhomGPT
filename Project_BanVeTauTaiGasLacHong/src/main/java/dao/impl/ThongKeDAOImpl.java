package dao.impl;

import dao.ThongKeDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import model.KetQuaThongKeVe;
import model.TrangThaiVeTau;
import util.JPAUtil;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai của ThongKeDAO sử dụng JPA
 */
public class ThongKeDAOImpl extends UnicastRemoteObject implements ThongKeDAO {

    public ThongKeDAOImpl() throws RemoteException {
        super();
    }

    @Override
    public List<KetQuaThongKeVe> thongKeVeTheoThoiGian(LocalDate tuNgay, LocalDate denNgay, String loaiThoiGian) throws RemoteException {
        EntityManager em = JPAUtil.getEntityManager();
        List<KetQuaThongKeVe> ketQuaList = new ArrayList<>();

        try {
            // Xác định format date group trong SQL dựa vào loại thời gian
            String sqlDateFormat;
            String jpqlGroupBy;

            switch (loaiThoiGian) {
                case "Ngày":
                    sqlDateFormat = "DATE(v.ngay_di)";
                    jpqlGroupBy = "CAST(v.ngayDi AS DATE)";
                    break;
                case "Tuần":
                    // MySQL: YEARWEEK format
                    sqlDateFormat = "DATE(DATE_ADD(v.ngay_di, INTERVAL(-WEEKDAY(v.ngay_di)) DAY))";
                    jpqlGroupBy = "FUNCTION('DATE_ADD', v.ngayDi, FUNCTION('INTERVAL', FUNCTION('-WEEKDAY', v.ngayDi), 'DAY'))";
                    break;
                case "Tháng":
                    // Format Year-Month
                    sqlDateFormat = "DATE(DATE_FORMAT(v.ngay_di, '%Y-%m-01'))";
                    jpqlGroupBy = "FUNCTION('DATE', FUNCTION('DATE_FORMAT', v.ngayDi, '%Y-%m-01'))";
                    break;
                case "Quý":
                    // Quý trong năm (Q1, Q2, Q3, Q4)
                    sqlDateFormat = "DATE(DATE_FORMAT(v.ngay_di, '%Y-%m-01'))";
                    jpqlGroupBy = "FUNCTION('DATE', FUNCTION('DATE_FORMAT', v.ngayDi, '%Y-%m-01'))";
                    break;
                case "Năm":
                    // Chỉ lấy năm
                    sqlDateFormat = "DATE(DATE_FORMAT(v.ngay_di, '%Y-01-01'))";
                    jpqlGroupBy = "FUNCTION('DATE', FUNCTION('DATE_FORMAT', v.ngayDi, '%Y-01-01'))";
                    break;
                default:
                    sqlDateFormat = "DATE(v.ngay_di)";
                    jpqlGroupBy = "CAST(v.ngayDi AS DATE)";
            }

            // JPQL query
            String jpql = "SELECT " + jpqlGroupBy + " AS thoiGian, " +
                    "v.trangThai AS trangThai, " +
                    "tt.tenTuyen AS tenTuyen, " +
                    "lt.tenLoai AS loaiToa, " +
                    "COUNT(v) AS soLuong " +
                    "FROM VeTau v " +
                    "JOIN v.lichTrinhTau ltt " +
                    "JOIN ltt.tau t " +
                    "JOIN t.tuyenTau tt " +
                    "JOIN v.choNgoi cn " +
                    "JOIN cn.toaTau toa " +
                    "JOIN toa.loaiToa lt " +
                    "WHERE v.ngayDi BETWEEN :tuNgay AND :denNgay " +
                    "GROUP BY " + jpqlGroupBy + ", v.trangThai, tt.tenTuyen, lt.tenLoai " +
                    "ORDER BY " + jpqlGroupBy + ", tt.tenTuyen";

            Query query = em.createQuery(jpql);
            query.setParameter("tuNgay", tuNgay);
            query.setParameter("denNgay", denNgay);

            List<Object[]> results = query.getResultList();

            // Chuyển đổi kết quả
            for (Object[] row : results) {
                KetQuaThongKeVe ketQua = new KetQuaThongKeVe();

                // FIX HERE: Chuyển đổi java.sql.Date sang java.time.LocalDate
                if (row[0] instanceof java.sql.Date) {
                    java.sql.Date sqlDate = (java.sql.Date) row[0];
                    ketQua.setThoiGian(sqlDate.toLocalDate());
                } else if (row[0] instanceof java.time.LocalDate) {
                    ketQua.setThoiGian((LocalDate) row[0]);
                } else if (row[0] instanceof java.util.Date) {
                    java.util.Date utilDate = (java.util.Date) row[0];
                    ketQua.setThoiGian(utilDate.toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate());
                } else {
                    // Nếu không phải kiểu date đã biết, in ra lớp thực tế để debug
                    System.err.println("Unexpected date type: " +
                            (row[0] != null ? row[0].getClass().getName() : "null"));
                    // Nếu null, đặt thành ngày hiện tại
                    ketQua.setThoiGian(row[0] != null ? LocalDate.parse(row[0].toString()) : LocalDate.now());
                }

                ketQua.setTrangThai((TrangThaiVeTau) row[1]);
                ketQua.setTenTuyen((String) row[2]);
                ketQua.setLoaiToa((String) row[3]);
                ketQua.setSoLuong(((Number) row[4]).intValue());
                ketQuaList.add(ketQua);
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi thống kê vé theo thời gian: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi thống kê vé theo thời gian", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return ketQuaList;
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