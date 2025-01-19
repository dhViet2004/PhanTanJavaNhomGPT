package testCRUD;

import dao.KhachHangDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import model.KhachHang;
import model.LoaiKhachHang;

public class Test_KH {
    public static void main(String[] args) {

        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();
        KhachHangDAO khachHangDAO = new KhachHangDAO(em);
//        System.out.println("Danh sách khách hàng có tên là Nguyễn");
//        khachHangDAO.listKhachHangsByName("Tân").forEach(kh -> {
//            System.out.println(kh);
//        });
//        System.out.println("Danh sách khách hàng có điểm tích lũy từ 0 đến 100");
//        khachHangDAO.listKhachHangsByPoints(0, 100).forEach(kh -> {
//            System.out.println(kh);
//        });


//        KhachHang khachHang = new KhachHang();
//        khachHang.setTenKhachHang("Tân");
//        khachHang.setDiemTichLuy(0);
//        khachHang.setSoDienThoai("0123456789");
//        khachHang.setDiaChi("Hà Nội");
//        khachHang.setGiayTo("CMND");
//        khachHang.setHangThanhVien("VIP");
//        khachHang.setMaKhachHang("KH01");
//        khachHang.setNgaySinh(java.time.LocalDate.now());
//        khachHang.setNgayThamgGia(java.time.LocalDate.now());
//        LoaiKhachHang loaiKhachHang = new LoaiKhachHang();
//        loaiKhachHang.setMaLoaiKhachHang("LKH001");
//        khachHang.setLoaiKhachHang(loaiKhachHang);
//        khachHangDAO.save(khachHang);
        khachHangDAO.delete("KH01");


    }
}
