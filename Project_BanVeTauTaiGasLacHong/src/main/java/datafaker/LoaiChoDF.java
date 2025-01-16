package datafaker;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.LoaiCho;
import net.datafaker.Faker;

public class LoaiChoDF {
    public static void generateSampleData(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        Faker faker = new Faker();

        // set dữ liệu cho LoaiCho
        LoaiCho lc1 = new LoaiCho();
        lc1.setMaLoai("LC01");
        lc1.setTenLoai("Ghế ngồi cứng");

        LoaiCho lc2 = new LoaiCho();
        lc2.setMaLoai("LC02");
        lc2.setTenLoai("Ghế ngồi mềm");

        LoaiCho lc3 = new LoaiCho();
        lc3.setMaLoai("LC03");
        lc3.setTenLoai("Giường nằm mềm");
        try {
            tx.begin();
            em.persist(lc1);
            em.persist(lc2);
            em.persist(lc3);
            tx.commit();
            System.out.println("Phát sinh dữ liệu thành công cho LoaiCho");
        } catch (Exception e) {
            tx.rollback();
            System.err.println("Phát sinh dữ liệu thất bại cho LoaiCho");

        }
    }
}
