import dao.LichTrinhTauDAO;

import dao.impl.LichTrinhTauDAOImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer {

    public static void main(String[] args) {
        try {
            System.out.println("Khởi động RMI Server...");


            System.setProperty("java.rmi.server.hostname", "127.0.0.1");


            LichTrinhTauDAO lichTrinhTauDAO = new LichTrinhTauDAOImpl();


            Registry registry = LocateRegistry.createRegistry(9090);


            registry.rebind("lichTrinhTauDAO", lichTrinhTauDAO);

            System.out.println("RMI Server đã sẵn sàng!");
            System.out.println("Registry đang chạy tại rmi://127.0.0.1:9090");
            System.out.println("Các đối tượng đã đăng ký:");
            System.out.println("- lichTrinhTauDAO");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}