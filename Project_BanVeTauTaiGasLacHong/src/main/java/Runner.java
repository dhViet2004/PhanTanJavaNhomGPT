import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.*;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class Runner {
    public static void main(String[] args) {
        // Khởi tạo EntityManager và EntityTransaction để tương tác với cơ sở dữ liệu
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();

        // Dùng datafaker tạo dữ liệu mẫu của lịch làm việc, nhân viên, tài khoản
        EntityTransaction transaction = em.getTransaction();

        Faker faker = new Faker();

        for (int i = 0; i < 10; i++)
        {

            transaction.begin();

            // Tạo dữ liệu mẫu cho NhanVien
            NhanVien nhanVien = new NhanVien();
            nhanVien.setMaNV(faker.idNumber().valid());
            nhanVien.setTenNV(faker.name().fullName());
            nhanVien.setSoDT(faker.phoneNumber().cellPhone());
            nhanVien.setTrangThai("Active");
            nhanVien.setCccd(faker.idNumber().valid());
            nhanVien.setDiaChi(faker.address().fullAddress());
            nhanVien.setNgayVaoLam(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            nhanVien.setChucVu("Nhân viên");
            nhanVien.setAvata(faker.avatar().image());

            // Tạo dữ liệu mẫu cho TaiKhoan
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setMaNV(nhanVien.getMaNV());
            taiKhoan.setPassWord(faker.internet().password());
            taiKhoan.setNhanVien(nhanVien);


            // Liên kết NhanVien với TaiKhoan
            nhanVien.setTaiKhoan(taiKhoan);

            // Lưu nhân viên và tài khoản
            em.persist(nhanVien);
            em.persist(taiKhoan);

            // Tạo dữ liệu mẫu cho LichLamViec
            LichLamViec lichLamViec = new LichLamViec();
            lichLamViec.setMaLichLamViec(faker.idNumber().valid());

            // Sinh ngẫu nhiên giờ bắt đầu và kết thúc
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime gioBatDau = now.plusHours(faker.number().numberBetween(0, 5));
            LocalDateTime gioKetThuc = gioBatDau.plusHours(faker.number().numberBetween(4, 8));

            lichLamViec.setGioBatDau(gioBatDau);
            lichLamViec.setGioKetThuc(gioKetThuc);
            lichLamViec.setTrangThai(faker.options().option("Scheduled", "Completed", "Cancelled"));
            lichLamViec.setTenCa(faker.options().option("Ca sáng", "Ca chiều", "Ca tối"));
            lichLamViec.setNhanVien(nhanVien);

            // Lưu lịch làm việc
            em.persist(lichLamViec);

            transaction.commit();


        }


    }
}


//THIỆN: TẠO DỮ LIỆU CHO LoaiCho,VeTau, ChoNgoi, LichTrinhTau

//public static void main(String[] args) {
//    EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
//    EntityTransaction tx = em.getTransaction();
//    Faker faker = new Faker();
//
//    // set dữ liệu cho LoaiCho
//    LoaiCho lc1 = new LoaiCho();
//    lc1.setMaLoai("LC01");
//    lc1.setTenLoai("Ghế ngồi cứng");
//
//    LoaiCho lc2 = new LoaiCho();
//    lc2.setMaLoai("LC02");
//    lc2.setTenLoai("Ghế ngồi mềm");
//
//    LoaiCho lc3 = new LoaiCho();
//    lc3.setMaLoai("LC03");
//    lc3.setTenLoai("Giường nằm mềm");
//
//    tx.begin();
//    em.persist(lc1);
//    em.persist(lc2);
//    em.persist(lc3);
//
//    // Tạo 10 ChoNgoi với Faker và gán chúng vào các LoaiCho
//    for (int i = 0; i < 10; i++) {
//        ChoNgoi choNgoi = new ChoNgoi();
//        String maCho = faker.code().asin();  // Faker cho mã ngẫu nhiên
//        String tenCho = faker.commerce().productName();
//        boolean tinhTrang = faker.bool().bool();
//        double giaTien = faker.number().randomDouble(2, 100, 1000);  // Giá tiền ngẫu nhiên
//
//        choNgoi.setMaCho(maCho);
//        choNgoi.setTenCho(tenCho);
//        choNgoi.setTinhTrang(tinhTrang);
//        choNgoi.setGiaTien(giaTien);
//
//        // Gán ChoNgoi vào một LoaiCho ngẫu nhiên (ở đây là gán vào 3 LoaiCho đã tạo)
//        int loaiChoIndex = faker.number().numberBetween(1, 4);  // Chọn 1 trong 3 LoaiCho
//        switch (loaiChoIndex) {
//            case 1:
//                choNgoi.setLoaiCho(lc1);
//                break;
//            case 2:
//                choNgoi.setLoaiCho(lc2);
//                break;
//            case 3:
//                choNgoi.setLoaiCho(lc3);
//                break;
//        }
//
//        // Persist ChoNgoi
//        em.persist(choNgoi);
//    }
//
//    // tạo 20 lịch trình tàu ngẫu nhiên
//    // với mỗi lịch trình tàu tạo 100 vé tàu ứng với mỗi lịch trình
//    // Tạo 20 lịch trình tàu với Faker
//    for (int i = 0; i < 20; i++) {
//        LichTrinhTau lichTrinh = new LichTrinhTau();
//        lichTrinh.setMaLich(UUID.randomUUID().toString());  // Tạo mã lịch ngẫu nhiên
//        lichTrinh.setTrangThai("Hoạt động");  // Đặt trạng thái cố định là "Hoạt động"
//
//        // Tạo thời gian ngẫu nhiên cho trường gioDi (kiểu LocalTime)
//        LocalTime gioDi = LocalTime.of(faker.number().numberBetween(0, 23), faker.number().numberBetween(0, 59));
//
//        // Lấy ngày hiện tại
//        LocalDate now = LocalDate.now();
//
//        // Phát sinh số ngày ngẫu nhiên trong tương lai, ví dụ từ 1 đến 30 ngày
//        long daysToAdd = ThreadLocalRandom.current().nextInt(1, 31); // Thêm từ 1 đến 30 ngày
//
//        // Tạo ngày đi là ngày hiện tại cộng với số ngày ngẫu nhiên
//        LocalDate ngayDi = now.plus(daysToAdd, ChronoUnit.DAYS);
//
//        lichTrinh.setGioDi(gioDi);
//        lichTrinh.setNgayDi(ngayDi);
//
//        // Persist LichTrinhTau
//        em.persist(lichTrinh);
//
//        // Tạo 100 vé tàu cho mỗi lịch trình tàu
//        for (int j = 0; j < 100; j++) {
//            VeTau veTau = new VeTau();
//            veTau.setMaVe(UUID.randomUUID().toString());  // Tạo mã vé duy nhất
//            veTau.setTenKhachHang(faker.name().fullName());
//            veTau.setGiayTo(faker.idNumber().valid());
//            veTau.setNgayDi(ngayDi);  // Ngày đi trùng với lịch trình tàu
//            veTau.setDoiTuong(faker.options().option("Người lớn", "Trẻ em"));
//            veTau.setGiaVe(faker.number().randomDouble(2, 100, 1000));
//
//            // Chọn trạng thái vé ngẫu nhiên giữa "Đã trả" và "Đã thanh toán"
//            String trangThaiVe = faker.options().option("Đã trả", "Đã thanh toán");
//            veTau.setTrangThai(trangThaiVe);
//
//            veTau.setLichTrinhTau(lichTrinh);
//
//            // Persist VeTau
//            em.persist(veTau);
//        }
//    }
//
//    tx.commit();
//    em.close();
//}
