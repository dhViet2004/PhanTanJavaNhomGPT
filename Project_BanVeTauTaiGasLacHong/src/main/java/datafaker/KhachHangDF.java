package datafaker;


import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.KhachHang;
import model.LoaiKhachHang;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KhachHangDF  {

    private int counter = 0; // Biến đếm để tăng dần mã khách hàng
    private final Faker faker = new Faker(); // Đối tượng Faker để tạo dữ liệu giả lập
    private final List<LoaiKhachHang> loaiKhachHangList = new ArrayList<>(); // Danh sách loại khách hàng giả lập

    // Hàm tạo mã khách hàng
    private String generateMaKhachHang() {
        String prefix = "KH";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        counter++;
        return String.format("%s%s%04d", prefix, datePart, counter); // Tạo mã dạng KHddMMyyyy0000
    }

    // Hàm tạo mã loại khách hàng
    private String generateMaLoaiKhachHang(int index) {
        return String.format("LKH%03d", index); // Mã dạng LKH001, LKH002,...
    }

    // Hàm tạo dữ liệu giả lập cho LoaiKhachHang
    private  LoaiKhachHang generateFakeLoaiKhachHang(int index) {
        LoaiKhachHang loaiKhachHang = new LoaiKhachHang();
        loaiKhachHang.setMaLoaiKhachHang(generateMaLoaiKhachHang(index)); // Mã loại khách hàng
        loaiKhachHang.setTenLoaiKhachHang(faker.options().option("Thường", "Thân Thiết")); // Tên loại
        return loaiKhachHang;
    }

    // Hàm tạo dữ liệu giả lập cho KhachHang
    private KhachHang generateFakeKhachHang() {
        Random random = new Random();
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang(generateMaKhachHang()); // Gán mã tự sinh
        khachHang.setSoDienThoai(faker.phoneNumber().cellPhone()); // Số điện thoại ngẫu nhiên
        khachHang.setTenKhachHang(faker.name().fullName()); // Tên khách hàng
        khachHang.setGiayTo("CMND" + faker.number().digits(8)); // CMND giả lập
        khachHang.setDiaChi(faker.address().fullAddress()); // Địa chỉ ngẫu nhiên
        khachHang.setDiemTichLuy(faker.number().randomDouble(2, 0, 100)); // Điểm tích lũy (0 - 100)
        khachHang.setNgaySinh(LocalDate.now().minusYears(faker.number().numberBetween(18, 65))); // Ngày sinh
        khachHang.setNgayThamgGia(LocalDate.now().minusDays(faker.number().numberBetween(1, 365))); // Ngày tham gia
        khachHang.setHangThanhVien(faker.options().option("Bạc", "Vàng", "Kim Cương")); // Hạng thành viên

        // Gán ngẫu nhiên loại khách hàng từ danh sách đã tạo
        khachHang.setLoaiKhachHang(loaiKhachHangList.get(random.nextInt(loaiKhachHangList.size())));

        return khachHang;
    }

    // Hàm lưu dữ liệu giả lập vào DB
    public  void generateAndPrintSampleData() {
        EntityManager em = Persistence
                .createEntityManagerFactory("mariadb")
                .createEntityManager();

        EntityTransaction tr = em.getTransaction();

        try {
            tr.begin();

            // Tạo và lưu loại khách hàng giả lập
            for (int i = 1; i <= 2; i++) { // Tạo 4 loại khách hàng
                LoaiKhachHang loaiKhachHang = generateFakeLoaiKhachHang(i);
                em.persist(loaiKhachHang); // Lưu vào DB
                loaiKhachHangList.add(loaiKhachHang); // Thêm vào danh sách
            }

            // Tạo và lưu khách hàng giả lập
            for (int i = 0; i < 15; i++) { // Tạo 15 khách hàng
                KhachHang khachHang = generateFakeKhachHang();
                em.persist(khachHang); // Lưu vào DB
            }

            tr.commit();
        } catch (Exception e) {
            if (tr.isActive()) {
                tr.rollback(); // Hoàn tác nếu lỗi
            }
            e.printStackTrace();

        }
    }


}
