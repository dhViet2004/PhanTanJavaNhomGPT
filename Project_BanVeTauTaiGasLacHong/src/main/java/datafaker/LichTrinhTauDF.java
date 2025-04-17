//package datafaker;
//
//import dao.impl.LichTrinhTauDAOImpl;
//import dao.impl.LoaiChoDAOImpl;
//import dao.impl.ToaTauDAOImpl;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityTransaction;
//import model.*;
//import net.datafaker.Faker;
//import org.hibernate.Hibernate;
//
//import java.rmi.RemoteException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class LichTrinhTauDF {
//    private static LocalDate lastDate = LocalDate.now(); // Ngày cuối cùng đã tạo vé
//    private static int ticketCount = 0; // Số vé đã tạo trong ngày
//    private static LocalDate lastGeneratedDate = LocalDate.now(); // Ngày cuối cùng đã tạo
//    private static int count = 0; // Số đếm các lịch trình đã tạo
//
//    // tạo lich trình tàu với tham số là ngày cho trước
//    public static void generateLichTrinhForDay(EntityManager em, LocalDate day) {
//        Faker faker = new Faker();
//        EntityTransaction tx = em.getTransaction();
//        ToaTauDAOImpl toaTauDAOImpl = new ToaTauDAOImpl(em);
//        List<ToaTau> listToaTau = toaTauDAOImpl.getlistToaTau();
//        if (listToaTau == null || listToaTau.size() == 0) {
//            System.err.println("Chưa có ToaTau trong CSDL");
//            return;
//        }
//
//        try {
//            tx.begin();
//                LichTrinhTau lichTrinh = new LichTrinhTau();
//                lichTrinh.setMaLich(generateRandomLichTrinhCode(em));
//                lichTrinh.setTrangThai(TrangThai.valueOf("Hoạt động"));
//
//                // Lấy giờ ngẫu nhiên trong ngày cho trước
//                LocalTime gioDi = LocalTime.of(faker.number().numberBetween(0, 23), faker.number().numberBetween(0, 59));
//
//                // Sử dụng ngày được truyền vào thay vì tạo ngẫu nhiên
//                lichTrinh.setGioDi(gioDi);
//                lichTrinh.setNgayDi(day); // Đặt ngày cố định cho lịch trình tàu
//
//                // Lấy ngẫu nhiên một ToaTau từ danh sách
//                ToaTau randomToaTau = listToaTau.get(ThreadLocalRandom.current().nextInt(listToaTau.size()));
//                Hibernate.initialize(randomToaTau.getTau()); // Khởi tạo Tau trước khi sử dụng
//                lichTrinh.setTau(randomToaTau.getTau());
//
//                // Lưu lịch trình tàu vào cơ sở dữ liệu
//                em.persist(lichTrinh);
//            tx.commit();
//
//            System.err.println("Thêm lịch trình tàu cho ngày " + day+" thành công");
//        } catch (Exception e) {
//            tx.rollback();
//            System.err.println("Đã xảy ra lỗi khi tạo lịch trình tàu.");
//        }
//    }
//
//    // Hàm tạo mã lịch trình tàu ngẫu nhiên
//    public static String generateRandomLichTrinhCode(EntityManager em) {
//        // Tạo ngày ngẫu nhiên trong khoảng 1-30 ngày trước hiện tại
//        int randomDaysBeforeToday = ThreadLocalRandom.current().nextInt(1, 31);
//        LocalDate randomDate = LocalDate.now().minusDays(randomDaysBeforeToday);
//
//        // Tạo giờ ngẫu nhiên trong ngày ngẫu nhiên này
//        LocalTime randomTime = LocalTime.of(
//                ThreadLocalRandom.current().nextInt(0, 24), // Giờ
//                ThreadLocalRandom.current().nextInt(0, 60), // Phút
//                ThreadLocalRandom.current().nextInt(0, 60)  // Giây
//        );
//
//        // Kết hợp ngày và giờ để tạo thành LocalDateTime
//        LocalDateTime randomDateTime = LocalDateTime.of(randomDate, randomTime);
//
//        // Định dạng ngày tháng giờ phút giây
//        String dateTimePart = randomDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//
//        // Nếu là ngày mới, đặt lại số đếm về 0
//        if (!randomDateTime.toLocalDate().isEqual(lastGeneratedDate)) {
//            count = 0;
//            lastGeneratedDate = randomDateTime.toLocalDate();
//        }
//
//        // Tăng số đếm để đảm bảo mã lịch trình không trùng
//        if (count >= 1000) {
//            count = 1; // Đặt lại số đếm nếu vượt quá 1000
//        } else {
//            count++; // Tăng số đếm
//        }
//
//        // Tạo phần số đếm, đảm bảo có tối đa 3 chữ số
//        String countPart = String.format("%03d", count);
//
//        // Tạo mã lịch trình với cấu trúc: LLT + ngày tháng giờ phút giây + số đếm
//        String lichTrinhCode = "LLT" + dateTimePart + "-" + countPart;
//
//        // Kiểm tra trùng lặp trong cơ sở dữ liệu (hoặc session)
//        LichTrinhTau existingLichTrinh = em.find(LichTrinhTau.class, lichTrinhCode);
//        if (existingLichTrinh != null) {
//            // Nếu mã đã tồn tại, gọi lại hàm để tạo mã khác
//            return generateRandomLichTrinhCode(em);
//        }
//
//        return lichTrinhCode;
//    }
//
//    private static String generateTicketCode(String maTau, String maLichTrinh) {
//        // Tạo ngày ngẫu nhiên trong khoảng từ 1 đến 30 ngày trước hôm nay
//        int randomDaysBeforeToday = ThreadLocalRandom.current().nextInt(1, 31);
//        LocalDate randomDate = LocalDate.now().minusDays(randomDaysBeforeToday);
//
//        // Tạo thời gian ngẫu nhiên cho giờ, phút, giây trong ngày ngẫu nhiên này
//        LocalTime randomTime = LocalTime.of(
//                ThreadLocalRandom.current().nextInt(0, 24), // giờ
//                ThreadLocalRandom.current().nextInt(0, 60), // phút
//                ThreadLocalRandom.current().nextInt(0, 60)  // giây
//        );
//
//        // Kết hợp ngày và giờ để tạo thành LocalDateTime
//        LocalDateTime randomDateTime = LocalDateTime.of(randomDate, randomTime);
//
//        // Nếu là ngày mới, đặt lại số đếm về 0
//        if (!randomDateTime.toLocalDate().isEqual(lastDate)) {
//            ticketCount = 0; // Đặt lại số vé
//            lastDate = randomDateTime.toLocalDate(); // Cập nhật ngày cuối cùng
//        }
//
//        // Định dạng ngày tháng giờ phút giây theo "yyyyMMddHHmmss"
//        String dateTimePart = randomDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//
//        // Tăng số đếm nếu có nhiều vé trong cùng một giây (lên đến 1000)
//        if (ticketCount >= 1000) {
//            ticketCount = 1; // Đặt lại số đếm nếu vượt quá 1000
//        } else {
//            ticketCount++; // Tăng số đếm
//        }
//
//        // Tạo phần số đếm, đảm bảo có tối đa 3 chữ số
//        String countPart = String.format("%03d", ticketCount);
//
//        // Tạo mã vé với cấu trúc: mã tàu + ngày tháng giờ phút giây + số đếm
//        return maTau + dateTimePart + "-" + "-" + countPart + maLichTrinh;
//    }
//
//    public static void genarateSampleData(EntityManager em) throws RemoteException {
//        EntityTransaction tx = em.getTransaction();
//        Faker faker = new Faker();
//        LoaiChoDAOImpl loaiChoDAOImpl = new LoaiChoDAOImpl();
//        ToaTauDAOImpl toaTauDAOImpl = new ToaTauDAOImpl(em);
//        LichTrinhTauDAOImpl lichTrinhTauDAOImpl = new LichTrinhTauDAOImpl();
//
//        // Lấy danh sách ToaTau và LoaiCho từ cơ sở dữ liệu
//        List<ToaTau> listToaTau = toaTauDAOImpl.getlistToaTau();
//        System.err.println("Lấy danh sách Toatau thành công");
//        List<LoaiCho> listLoaiCho = loaiChoDAOImpl.getAllList();
//        List<LichTrinhTau> listLichTrinhTau  = lichTrinhTauDAOImpl.getAllList();
//        if (listToaTau == null || listToaTau.size() == 0) {
//            System.err.println("Chưa có ToaTau trong CSDL");
//            return;
//        }
//
//        if (listLoaiCho == null || listLoaiCho.size() == 0) {
//            System.err.println("Chưa có LoaiCho trong CSDL");
//            return;
//        }
//
//        if (listLichTrinhTau == null || listLichTrinhTau.size() == 0) {
//            System.err.println("Chưa có LichTrinhTau trong CSDL");
//        }
//        try {
//            tx.begin();
//
//            for (int i = 0; i < 1; i++) { // Tạo 20 lịch trình tàu
//                LichTrinhTau lichTrinh = new LichTrinhTau();
//                lichTrinh.setMaLich(generateRandomLichTrinhCode(em));
//                lichTrinh.setTrangThai(TrangThai.valueOf("Hoạt động"));
//
//                // Tạo thời gian và ngày ngẫu nhiên
//                LocalTime gioDi = LocalTime.of(faker.number().numberBetween(0, 23), faker.number().numberBetween(0, 59));
//                LocalDate ngayDi = LocalDate.now().plus(ThreadLocalRandom.current().nextInt(1, 31), ChronoUnit.DAYS);
//
//                lichTrinh.setGioDi(gioDi);
//                lichTrinh.setNgayDi(ngayDi);
//
//                // Lấy ngẫu nhiên một ToaTau từ danh sách
//                ToaTau randomToaTau = listToaTau.get(ThreadLocalRandom.current().nextInt(listToaTau.size()));
//                Hibernate.initialize(randomToaTau.getTau()); // Khởi tạo Tau trước khi sử dụng
//                lichTrinh.setTau(randomToaTau.getTau());
//
//                em.persist(lichTrinh);
//
//                // Tạo vé tàu cho mỗi ToaTau (20 chỗ ngồi)
//                for (ToaTau toaTau : listToaTau) {
//                    for (int j = 0; j < toaTau.getSoGhe(); j++) {
//                        // Tạo chỗ ngồi
//                        ChoNgoi choNgoi = new ChoNgoi();
//                        choNgoi.setLoaiCho(listLoaiCho.get(ThreadLocalRandom.current().nextInt(listLoaiCho.size())));
//                        choNgoi.setToaTau(toaTau);
//
//                        // Phát sinh tên chỗ ngồi (1A, 1B, ..., 14D)
//                        int row = j / 4 + 1;
//                        char column = (char) ('A' + j % 4);
//                        String tenCN = row + "" + column;
//                        // Tạo mã chỗ ngồi
//                        String maCho = "CN" + tenCN + toaTau.getMaToa();
//                        choNgoi.setMaCho(maCho);
//
//                        choNgoi.setTenCho(tenCN);
//
//                        choNgoi.setTinhTrang(faker.bool().bool());
//                        choNgoi.setGiaTien(faker.number().randomDouble(2, 50, 500));
//
//                        em.persist(choNgoi);
//
//                        // Tạo vé tàu tương ứng
//
//
//                        VeTau veTau = new VeTau();
//                        veTau.setMaVe(generateTicketCode(toaTau.getTau().getMaTau(), lichTrinh.getMaLich()));
//                        veTau.setChoNgoi(choNgoi);
//                        veTau.setLichTrinhTau(lichTrinh);
//                        veTau.setTenKhachHang(faker.name().fullName());
//                        veTau.setGiayTo(faker.idNumber().valid());
//                        veTau.setNgayDi(ngayDi);
//                        veTau.setDoiTuong(faker.options().option("Người lớn", "Trẻ em"));
//                        veTau.setGiaVe(faker.number().randomDouble(2, 100, 1000));
//                        veTau.setTrangThai(faker.options().option("Đã trả", "Đã thanh toán"));
//                        VeTau existingVeTau = em.find(VeTau.class, veTau.getMaVe());
//                        if (existingVeTau != null) {
//                            // Nếu mã vé đã tồn tại, tạo lại mã vé
//                            veTau.setMaVe(generateTicketCode(toaTau.getTau().getMaTau(), lichTrinh.getMaLich()));
//                        }
//                        em.persist(veTau);
//                    }
//                }
//            }
//
//            tx.commit();
//            System.out.println("Dữ liệu mẫu cho LichTrinhTau và Vé tàu đã được tạo thành công.");
//        } catch (Exception e) {
//            if (tx.isActive()) {
//                tx.rollback();
//            }
//            e.printStackTrace();
//            System.err.println("Đã xảy ra lỗi khi tạo dữ liệu mẫu.");
//        }
//    }
//}
