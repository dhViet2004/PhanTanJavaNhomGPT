import dao.impl.LichTrinhTauDAOImpl;
import model.LichTrinhTau;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize DAO implementation
            LichTrinhTauDAOImpl lichTrinhTauDAO = new LichTrinhTauDAOImpl();

            // Input parameters
            LocalDate date = LocalDate.of(2025, 1, 24); // Example date
            String gaDi = "SÃ i GÃ²n"; // Example departure station
            String gaDen = "HÃ  Ná»™i"; // Example arrival station

            // Fetch the list of train schedules
            List<LichTrinhTau> lichTrinhTauList = lichTrinhTauDAO.getListLichTrinhTauByDateAndGaDiGaDen(date, gaDi, gaDen);

            // Print the results
            if (!lichTrinhTauList.isEmpty()) {
                System.out.println("Train schedules on " + date + " from " + gaDi + " to " + gaDen + ":");
                for (LichTrinhTau lichTrinhTau : lichTrinhTauList) {
                    System.out.println(" - Train ID: " + lichTrinhTau.getTau().getMaTau() +
                            ", Train Name: " + lichTrinhTau.getTau().getTenTau() +
                            ", Departure Time: " + lichTrinhTau.getGioDi());
                }
            } else {
                System.out.println("No trains found on " + date + " from " + gaDi + " to " + gaDen + ".");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}