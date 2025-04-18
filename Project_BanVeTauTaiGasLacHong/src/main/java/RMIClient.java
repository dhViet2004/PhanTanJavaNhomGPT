import dao.LichTrinhTauDAO;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.time.LocalDate;

public class RMIClient {

    public static void main(String[] args) throws Exception {

        Context context = new InitialContext();
       LichTrinhTauDAO lichTrinhTauDAO = (LichTrinhTauDAO) context.lookup("rmi://LAPTOP-LOVAL8V5/lichTrinhTauDAO");
                System.out.println("Client is ready!!!");
       lichTrinhTauDAO.getListLichTrinhTauByDate(LocalDate.of(2025,01,24))
               .forEach(st -> System.out.println(st));
    }

}
