import dao.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.time.LocalDate;

public class RMIClient {


    private static KhachHangDAO khachHangDAO;

    public static void main(String[] args) throws Exception {
        Context context = new InitialContext();
//       LichTrinhTauDAO lichTrinhTauDAO = (LichTrinhTauDAO) context.lookup("rmi://localhost:9090/lichTrinhTauDAO");
//                System.out.println("Client is ready!!!");
//       lichTrinhTauDAO.getListLichTrinhTauByDate(LocalDate.of(2025,01,24))
//               .forEach(st -> System.out.println(st));

//        KhachHangDAO khachHangDAO = (KhachHangDAO) context.lookup("rmi://localhost:9090/khachHangDAO");
//        khachHangDAO.getTenKhachHang().forEach(System.out::println);

//        LoaiKhachHangDAO loaiKhachHangDAO = (LoaiKhachHangDAO) context.lookup("rmi://localhost:9090/loaiKhachHangDAO");
//        loaiKhachHangDAO.getAll().forEach(System.out::println);

        HoaDonDAO  hoaDonDAO = (HoaDonDAO) context.lookup("rmi://localhost:9090/hoaDonDAO");
        hoaDonDAO.getAllHoaDons().forEach(System.out::println);

//        VeTauDAO veTauDAO = (VeTauDAO) context.lookup("rmi://localhost:9090/veTauDAO");
//        veTauDAO.getAllList().forEach(System.out::println);
    }

}
