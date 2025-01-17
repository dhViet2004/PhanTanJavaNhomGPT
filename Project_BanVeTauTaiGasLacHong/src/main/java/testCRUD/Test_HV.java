package testCRUD;

import dao.LoaiToaTauDAO;
import dao.TauDAO;
import dao.TuyenTauDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import model.LoaiToa;
import model.Tau;
import model.TuyenTau;
import net.datafaker.Faker;

import java.util.List;

public class Test_HV {
    public static void main(String[] args) {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();

        Faker faker = new Faker();
//        <--------CRUD Tuyến Tàu ---------->
//        TuyenTauDAO tuyenTauDAO = new TuyenTauDAO(em);
//            <---------SAVE-------->
//        TuyenTau tuyenTau = new TuyenTau();

//        tuyenTau.setMaTuyen("TT1111");
//        tuyenTau.setTenTuyen("Tuyến " + faker.address().cityName() + " - " + faker.address().cityName()); // Tên tuyến với thành phố ngẫu nhiên
//        tuyenTau.setGaDi(faker.address().streetName()); // Ga đi ngẫu nhiên
//        tuyenTau.setGaDen(faker.address().streetName()); // Ga đến ngẫu nhiên
//        tuyenTau.setDiaDiemDi(faker.address().cityName() + ", " + faker.address().country()); // Địa điểm đi (thành phố, quốc gia)
//        tuyenTau.setDiaDiemDen(faker.address().cityName() + ", " + faker.address().country()); // Địa điểm đến (thành phố, quốc gia)
//
//        if (tuyenTauDAO.save(tuyenTau)){
//            System.out.println("Lưu thành công");
//        }else {
//            System.out.println("Lưu thất bại");
//        }
//        TuyenTau ttToUpdate = tuyenTauDAO.getTuyenTauById("TT1111");
//        System.out.println(ttToUpdate);
//      <----------UPDATE----->
//        if(ttToUpdate != null) {
//            ttToUpdate.setGaDi("HCM");
//            ttToUpdate.setGaDen("Ha Noi");
//            boolean resultUpdate = tuyenTauDAO.update(ttToUpdate);
//            System.out.println(resultUpdate ? "Cập nhật thành công" : "Cập nhật thất bại");
//        }


//        <-------DELETE--------->
//        boolean resultDelete = tuyenTauDAO.delete("TT1111");
//        System.out.println(resultDelete ? "Xóa thành công" : "Xóa thất bại");

//            <--------CRUD LoaiToa --------->
//        LoaiToaTauDAO loaiToaTauDAO = new LoaiToaTauDAO(em);
//        List<LoaiToa> loaiToaList = null;
//        loaiToaList = loaiToaTauDAO.getListLoaiToa();
//        loaiToaList.forEach(loaiToa -> {System.out.println(loaiToa);});
//
//        LoaiToa loaiToa = loaiToaTauDAO.getLoaiToaById("LT9");
//        System.out.println(loaiToa);
//            <------Save-------->
//        LoaiToa newLoaiToa = new LoaiToa();
//        newLoaiToa.setMaLoai("LT1111");
//        newLoaiToa.setTenLoai("Loại toa "+ faker.commerce().productName());
//        System.out.println(newLoaiToa);
//        boolean resultLoaiToa = loaiToaTauDAO.save(newLoaiToa);
//        System.out.println(resultLoaiToa ? "Thêm " + newLoaiToa + "Thành công" : "Thêm thất bại");

//        <---------UPDATE-------->
//        LoaiToa returnloaiToa = loaiToaTauDAO.getLoaiToaById("LT1111");
//        System.out.println(returnloaiToa);
//        returnloaiToa.setTenLoai("Loại toa "+faker.commerce().productName());
//        System.out.println(returnloaiToa);
//        boolean resultUpdate = loaiToaTauDAO.update(returnloaiToa);
//        System.out.println(resultUpdate ? "Update thành công" : "Update thất bại");

//            <--------DELETE------->
//        String maLoaiToa = "LT1111";
//        boolean resultDelete = loaiToaTauDAO.deleteById(maLoaiToa);
//        System.out.println(resultDelete ? "Xóa thành công" : "Xóa thất bại");

        TauDAO tauDAO = new TauDAO(em);
        List<Tau> taus = tauDAO.getAllListT();
        taus.forEach(tau->{System.out.println(tau);});



        em.close();
    }
}
