package testCRUD;

import dao.LichTrinhTauDAO;
import dao.TauDAO;
import dao.impl.LichTrinhTauDAOImpl;
import dao.impl.TauDAOImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;
import model.LichTrinhTau;
import model.Tau;
import net.datafaker.Faker;

import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public class Test_HV {
    public static void main(String[] args) throws Exception {
        EntityManager em = Persistence.createEntityManagerFactory("mariadb").createEntityManager();
        TauDAO tauDAO = new TauDAOImpl();
        List<Tau> taus = tauDAO.getAllListT();
        taus.forEach(System.out::println);

//        Faker faker = new Faker();
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
////        System.out.println(resultDelete ? "Xóa thành công" : "Xóa thất bại");

//        <-------CRUD Tau------>
//        TauDAO tauDAO = new TauDAO(em);
//        TuyenTauDAO tuyenTauDAO = new TuyenTauDAO(em);
//        List<TuyenTau> tuyenTauList = tuyenTauDAO.getListTuyenTau();
//
//        List<Tau> taus = tauDAO.getAllListT();
//        taus.forEach(tau->{System.out.println(tau);});

//        Tau tau = tauDAO.getById("T1");
//        System.out.println(tau);
//        <--------SAVE------->
//        Tau newTau = new Tau();
//        newTau.setMaTau("T1111");
//        newTau.setTenTau("Tàu " + faker.name().lastName());
//        newTau.setSoToa(faker.number().numberBetween(5, 20));
//
//        // Liên kết với một TuyenTau ngẫu nhiên
//        TuyenTau randomTuyenTau = tuyenTauList.get(faker.number().numberBetween(0, tuyenTauList.size()));
//        newTau.setTuyenTau(randomTuyenTau);
//        System.out.println(newTau);
//        boolean resultSaveTau = tauDAO.save(newTau);
//        System.out.println(resultSaveTau ? "Them thanh cong " : "Them that bai");

//        <---------UPDATE--------->
//        Tau returnTau = tauDAO.getById("T1111");
//        System.out.println(returnTau);
//        returnTau.setTenTau("Tàu " + faker.name().lastName());
//        returnTau.setSoToa(faker.number().numberBetween(5, 20));
//        boolean resultUpdateTau =  tauDAO.update(returnTau);
////        System.out.println(resultUpdateTau ? returnTau : "Update that bai");
//        <-------DELETE--------->
//        String maTuyen = "T1111";
//        boolean resultDeleteTau = tauDAO.delete(maTuyen);
//        System.out.println(resultDeleteTau ? "Xoa thanh cong" : "Xoa that bai");

//        <-------CRUD ToaTau-------->
//        ToaTauDAO toaTauDAO = new ToaTauDAO(em);
//        List<ToaTau> toaTauList = toaTauDAO.getlistToaTau();
//        toaTauList.forEach(toaTau->{System.out.println(toaTau);});


//        <----------SAVE----------->
//        ToaTau newToaTau = new ToaTau();
//        newToaTau.setMaToa("TT1111");
//        newToaTau.setTenToa("Toa " + "1111"); // Tên toa là "Toa {i}"
//        newToaTau.setSoGhe(faker.number().numberBetween(20, 100)); // Số ghế từ 20 đến 100
//        newToaTau.setThuTu(faker.number().numberBetween(1, 10)); // Thứ tự toa từ 1 đến 10
//
//        // Liên kết với một LoaiToa ngẫu nhiên
//        LoaiToaTauDAO loaiTauDAO = new LoaiToaTauDAO(em);
//        List<LoaiToa> loaiToaList = loaiTauDAO.getListLoaiToa();
//        LoaiToa randomLoaiToa = loaiToaList.get(faker.number().numberBetween(0, loaiToaList.size()));
//        newToaTau.setLoaiToa(randomLoaiToa);
//
//        Tau tau = taus.get(faker.number().numberBetween(0, taus.size()));
//        newToaTau.setTau(tau);
//
//        System.out.println(newToaTau);
//        boolean resultSaveTT = toaTauDAO.save(newToaTau);
//        System.out.println(resultSaveTT ? "Them thanh cong" : "Them that bai");
//        <------UPDATE-------->
//        ToaTau returnToaTau = toaTauDAO.getToaTauById("TT1111");
//        System.out.println(returnToaTau);
//        returnToaTau.setTenToa("Toa " + "2222");
//        boolean resultUpdateTT = toaTauDAO.update(returnToaTau);
//        System.out.println(resultUpdateTT ? "Update thanh cong" : "Update that bai");

//        <---------DELETE---------->
//        boolean resultDeleteTT = toaTauDAO.delete("TT1111");
//        System.out.println(resultDeleteTT ? "Xoa thanh cong" : "Xoa that bai");


        em.close();
    }
}
