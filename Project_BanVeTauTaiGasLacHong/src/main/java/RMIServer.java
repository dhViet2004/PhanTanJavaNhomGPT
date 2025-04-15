import dao.LichTrinhTauDAO;

import dao.impl.LichTrinhTauDAOImpl;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;

public class RMIServer {

    public static void main(String[] args) throws Exception{

        Context context = new InitialContext();

        LichTrinhTauDAO lichTrinhTauDAO = new LichTrinhTauDAOImpl();

        LocateRegistry.createRegistry(9090);

        context.bind("rmi://MSI:9090/lichTrinhTauDAO", lichTrinhTauDAO);

        System.out.println("Server is ready!!!");

    }
}
