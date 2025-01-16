//package dao;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityTransaction;
//import lombok.AllArgsConstructor;
//import model.TuyenTau;
//
//@AllArgsConstructor
//public class TuyenTauDAO {
//    private EntityManager em;
//    public  boolean save(TuyenTau tuyenTau){
//        EntityTransaction tr = em.getTransaction();
//        try {
//            tr.begin();
//
//            tr.commit();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }
//}
