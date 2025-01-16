package datafaker;

import dao.LoaiChoDAO;
import dao.ToaTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.*;
import net.datafaker.Faker;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class LichTrinhTauDF {

    public static void genarateSampleData(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        Faker faker = new Faker();
        LoaiChoDAO loaiChoDAO = new LoaiChoDAO();
        ToaTauDAO toaTauDAO = new ToaTauDAO();

        // Lấy danh sách ToaTau và LoaiCho từ cơ sở dữ liệu
        List<ToaTau> listToaTau = toaTauDAO.getlistToaTau();
        System.err.println("Lấy danh sách Toatau thành công");
        List<LoaiCho> listLoaiCho = loaiChoDAO.getAllList();

        if (listToaTau == null || listToaTau.size() == 0) {
            System.err.println("Chưa có ToaTau trong CSDL");
            return;
        }

        if (listLoaiCho == null || listLoaiCho.size() == 0) {
            System.err.println("Chưa có LoaiCho trong CSDL");
            return;
        }

        try {
            tx.begin();

            for (int i = 0; i < 20; i++) { // Tạo 20 lịch trình tàu
                LichTrinhTau lichTrinh = new LichTrinhTau();
                lichTrinh.setMaLich(UUID.randomUUID().toString());
                lichTrinh.setTrangThai("Hoạt động");

                // Tạo thời gian và ngày ngẫu nhiên
                LocalTime gioDi = LocalTime.of(faker.number().numberBetween(0, 23), faker.number().numberBetween(0, 59));
                LocalDate ngayDi = LocalDate.now().plus(ThreadLocalRandom.current().nextInt(1, 31), ChronoUnit.DAYS);

                lichTrinh.setGioDi(gioDi);
                lichTrinh.setNgayDi(ngayDi);

                // Lấy ngẫu nhiên một ToaTau từ danh sách
                ToaTau randomToaTau = listToaTau.get(ThreadLocalRandom.current().nextInt(listToaTau.size()));
                Hibernate.initialize(randomToaTau.getTau()); // Khởi tạo Tau trước khi sử dụng
                lichTrinh.setTau(randomToaTau.getTau());

                em.persist(lichTrinh);

                // Tạo vé tàu cho mỗi ToaTau (56 chỗ ngồi)
                for (ToaTau toaTau : listToaTau) {
                    for (int j = 0; j < 56; j++) {
                        // Tạo chỗ ngồi
                        ChoNgoi choNgoi = new ChoNgoi();
                        choNgoi.setMaCho(UUID.randomUUID().toString());
                        choNgoi.setLoaiCho(listLoaiCho.get(ThreadLocalRandom.current().nextInt(listLoaiCho.size())));
                        choNgoi.setToaTau(toaTau);

                        // Phát sinh tên chỗ ngồi (1A, 1B, ..., 14D)
                        int row = j / 4 + 1;
                        char column = (char) ('A' + j % 4);
                        choNgoi.setTenCho(row + "" + column);

                        choNgoi.setTinhTrang(faker.bool().bool());
                        choNgoi.setGiaTien(faker.number().randomDouble(2, 50, 500));

                        em.persist(choNgoi);

                        // Tạo vé tàu tương ứng
                        VeTau veTau = new VeTau();
                        veTau.setMaVe(UUID.randomUUID().toString());
                        veTau.setChoNgoi(choNgoi);
                        veTau.setLichTrinhTau(lichTrinh);
                        veTau.setTenKhachHang(faker.name().fullName());
                        veTau.setGiayTo(faker.idNumber().valid());
                        veTau.setNgayDi(ngayDi);
                        veTau.setDoiTuong(faker.options().option("Người lớn", "Trẻ em"));
                        veTau.setGiaVe(faker.number().randomDouble(2, 100, 1000));
                        veTau.setTrangThai(faker.options().option("Đã trả", "Đã thanh toán"));

                        em.persist(veTau);
                    }
                }
            }

            tx.commit();
            System.out.println("Dữ liệu mẫu cho LichTrinhTau và Vé tàu đã được tạo thành công.");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            System.err.println("Đã xảy ra lỗi khi tạo dữ liệu mẫu.");
        }
    }
}
