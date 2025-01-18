package testCRUD;

import dao.LichLamViecDAO;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import model.LichLamViec;
import model.NhanVien;
import net.datafaker.Faker;

import java.time.ZoneId;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: Test_NV_TK_LLV
 * @Tạo vào ngày: 18/01/2025
 * @Tác giả: Nguyen Huu Sang
 */
public class Test_NV_TK_LLV {
    public static void main(String[] args) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();

        Faker faker = new Faker();
//        <--------CRUD Nhân Viên ---------->
//        NhanVienDAO nhanVienDAO = new NhanVienDAO(em);
//            <---------SAVE-------->
//        NhanVien nhanVien = new NhanVien();
//
//        nhanVien.setMaNV("NV1111");
//        nhanVien.setTenNV(faker.name().fullName());
//        nhanVien.setSoDT(faker.phoneNumber().cellPhone());
//        nhanVien.setTrangThai("Đang làm");
//        nhanVien.setCccd(faker.idNumber().valid());
//        nhanVien.setDiaChi(faker.address().fullAddress());
//        nhanVien.setNgayVaoLam(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//        nhanVien.setChucVu("Nhân viên");
//        nhanVien.setAvata(faker.avatar().image());
//
//        if (nhanVienDAO.save(nhanVien)) {
//            System.out.println("Lưu thành công");
//        } else {
//            System.out.println("Lưu thất bại");
//        }
//        NhanVien nvToUpdate = nhanVienDAO.getnhanvienById("NV1111");
//        System.out.println(nvToUpdate);

//      <----------UPDATE----->
//        if (nvToUpdate != null) {
//            nvToUpdate.setChucVu("Quản lý");
//            boolean resultUpdate = nhanVienDAO.update(nvToUpdate);
//            System.out.println(resultUpdate ? "Cập nhật thành công" : "Cập nhật thất bại");
//        }

//        <-------DELETE--------->
//        boolean resultDelete = nhanVienDAO.delete("NV1111");
//        System.out.println(resultDelete ? "Xóa thành công" : "Xóa thất bại");

//            <--------CRUD TaiKhoan --------->
//      Update Password taiKhoan với maNV
//        String maNV = "NV1111";
//        String newPassword ="PassWordNew123.";
//        boolean resultUpdatePassword = new TaiKhoanDAO(em).updatePassword(maNV,newPassword);
//        System.out.println(resultUpdatePassword ? "Cập nhật mật khẩu thành công" : "Cập nhật mật khẩu thất bại");


//        CRUD Lịch làm việc
//        LichLamViecDAO lichLamViecDAO = new LichLamViecDAO(em);
//        <---------SAVE-------->
//        LichLamViec lichLamViec = new LichLamViec();
//
//        lichLamViec.setMaLichLamViec("LLV1111");
//        lichLamViec.setGioBatDau(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//        lichLamViec.setGioKetThuc(faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
//        lichLamViec.setTrangThai("Đang làm");
//        lichLamViec.setTenCa("Ca sáng");
//        lichLamViec.setNhanVien(nhanVienDAO.getnhanvienById("NV1111"));
//
//        if (lichLamViecDAO.save(lichLamViec)) {
//            System.out.println("Lưu thành công");
//        } else {
//            System.out.println("Lưu thất bại");
//        }
//        LichLamViec llvToUpdate = lichLamViecDAO.getLichLamViecById("LLV1111");
//        System.out.println(llvToUpdate);

//      <----------UPDATE----->
//        if (lichLamViecDAO.getLichLamViecById("LLV1111") != null) {
//            LichLamViec llvToUpdate = lichLamViecDAO.getLichLamViecById("LLV1111");
//            llvToUpdate.setTenCa("Ca chiều");
//            boolean resultUpdate = lichLamViecDAO.update(llvToUpdate);
//            System.out.println(resultUpdate ? "Cập nhật thành công" : "Cập nhật thất bại");
//        }

        //       <-------DELETE--------->
//        boolean resultDelete = lichLamViecDAO.delete("LLV1111");
//        System.out.println(resultDelete ? "Xóa thành công" : "Xóa thất bại");




    }
}