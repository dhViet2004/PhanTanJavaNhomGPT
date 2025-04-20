import dao.*;
import dao.LoaiKhachHangDAO;
import dao.impl.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
//    netstat -aon | findstr :<port>
//    taskkill /PID <pid> /F


    public static void main(String[] args) {
        try {
            System.out.println("Khởi động RMI Server...");

            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
//            System.setProperty("java.rmi.server.hostname", "192.168.1.113");
            // Tạo đối tượng DAO
            LichTrinhTauDAO lichTrinhTauDAO = new LichTrinhTauDAOImpl();
            TauDAO tauDAO = new TauDAOImpl();
            TuyenTauDAO tuyenTauDAO = new TuyenTauDAOImpl();

            HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();
            KhachHangDAO khachHangDAO = new KhachHangDAOImpl();
            LoaiKhachHangDAO loaiKhachHangDAO = new LoaiKhachHangDAOImpl();
            VeTauDAO veTauDAO = new VeTauDAOImpl();
            DoiVeDAO doiVeDAO = new DoiVeDAOImpl(); // Thêm dòng này
            ToaTauDoiVeDAO toaTauDoiVeDAO = new ToaTauDoiVeDAOImpl();
            ChoNgoiDoiVeDAO choNgoiDoiVeDAO = new ChoNgoiDoiVeDAOImpl();
            LoaiHoaDonDAO loaiHoaDonDAO = new LoaiHoaDonDAOImpl();
            ChiTietHoaDonDAO chiTietHoaDonDAO = new ChiTietHoaDonDAOImpl();
            NhanVienDAO nhanVienDAO = new NhanVienDAOImpl();
            TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAOImpl();

            // Tạo registry
            Registry registry = LocateRegistry.createRegistry(9090);

            // Đăng ký các đối tượng DAO
            registry.rebind("lichTrinhTauDAO", lichTrinhTauDAO);
            registry.rebind("tauDAO", tauDAO);
            registry.rebind("tuyenTauDAO", tuyenTauDAO);

            registry.rebind("hoaDonDAO", hoaDonDAO);
            registry.rebind("veTauDAO", veTauDAO);
            registry.rebind("khachHangDAO", khachHangDAO);
            registry.rebind("loaiKhachHangDAO", loaiKhachHangDAO);

            registry.rebind("doiVeDAO", doiVeDAO); // Thêm dòng này
            registry.rebind("toaTauDoiVeDAO", toaTauDoiVeDAO);
            registry.rebind("choNgoiDoiVeDAO", choNgoiDoiVeDAO);
            registry.rebind("loaiHoaDonDAO", loaiHoaDonDAO);
            registry.rebind("chiTietHoaDonDAO", chiTietHoaDonDAO);
            registry.rebind("nhanVienDAO",nhanVienDAO);
            registry.rebind("taiKhoanDAO",taiKhoanDAO);

            System.out.println("RMI Server đã sẵn sàng!");
            System.out.println("Registry đang chạy tại rmi://127.0.0.1:9090");
            System.out.println("Các đối tượng đã đăng ký:");
            System.out.println("- lichTrinhTauDAO");
            System.out.println("- tauDAO");
            System.out.println("- tuyenTauDAO");

            System.out.println("- KhachHangDAO");
            System.out.println("- LoaiKhachHangDAO");
            System.out.println("- hoaDonDAO");
            System.out.println("- VeTauDAO");
            System.out.println("- doiVeDAO");
            System.out.println("- toaTauDAO");
            System.out.println("- choNgoiDAO");
            System.out.println("- LoaiHoaDonDAO");
            System.out.println("- chiTietHoaDonDAO");
            System.out.println("- nhanVienDAO");
            System.out.println("- taiKhoanDAO");
            System.out.println("Server đang lắng nghe...");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}