import dao.LichTrinhTauDAO;
import dao.TauDAO;
import dao.TuyenTauDAO;
import dao.impl.LichTrinhTauDAOImpl;
import dao.impl.TauDAOImpl;
import dao.impl.TuyenTauDAOImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
//    netstat -aon | findstr :<port>
//    taskkill /PID <pid> /F


    public static void main(String[] args) {
        try {
            System.out.println("Khởi động RMI Server...");

            System.setProperty("java.rmi.server.hostname", "127.0.0.1");

            // Tạo đối tượng DAO
            LichTrinhTauDAO lichTrinhTauDAO = new LichTrinhTauDAOImpl();
            TauDAO tauDAO = new TauDAOImpl();
            TuyenTauDAO tuyenTauDAO = new TuyenTauDAOImpl();
            // Tạo registry
            Registry registry = LocateRegistry.createRegistry(9090);

            // Đăng ký các đối tượng DAO
            registry.rebind("lichTrinhTauDAO", lichTrinhTauDAO);
            registry.rebind("tauDAO", tauDAO);
            registry.rebind("tuyenTauDAO", tuyenTauDAO);


            System.out.println("RMI Server đã sẵn sàng!");
            System.out.println("Registry đang chạy tại rmi://127.0.0.1:9090");
            System.out.println("Các đối tượng đã đăng ký:");
            System.out.println("- lichTrinhTauDAO");
            System.out.println("- tauDAO");
            System.out.println("- tuyenTauDAO");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}