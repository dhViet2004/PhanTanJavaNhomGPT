import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.LoaiToa;
import model.Tau;
import model.ToaTau;
import model.TuyenTau;
import net.datafaker.Faker;

import java.util.HashSet;
import java.util.Locale;

public class Runner {
    public static void main(String[] args) {
        // Khởi tạo EntityManager và EntityTransaction để tương tác với cơ sở dữ liệu
        EntityManager em = Persistence.createEntityManagerFactory("mariadb")
                .createEntityManager();

    }
}
