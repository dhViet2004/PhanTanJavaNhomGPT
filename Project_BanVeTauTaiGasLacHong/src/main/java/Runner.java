import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import model.Clazz;
import model.Student;

public class Runner {

    public static void main(String[] args) {

        Student st = new Student("222222","Le Lan", 20,"NAM");

        Clazz cls = new Clazz("DHKTPM18BAB",55);

        st.setClazz(cls);

        EntityManager em = Persistence.createEntityManagerFactory("default")
                .createEntityManager();

        EntityTransaction tr = em.getTransaction();

        tr.begin();
        try{
            em.persist(cls);
            em.persist(st);
            tr.commit();
        }catch (Exception ex){
            ex.printStackTrace();
            tr.rollback();
        }

    }

}
