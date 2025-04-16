package dao;

import model.LichTrinhTau;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.List;

public interface LichTrinhTauDAO extends Remote {
    List<LichTrinhTau> getAllList() throws RemoteException;
    LichTrinhTau getById(String id) throws RemoteException;
    boolean save(LichTrinhTau lichTrinhTau) throws RemoteException;
    boolean update(LichTrinhTau lichTrinhTau) throws RemoteException;
    boolean delete(LichTrinhTau lichTrinhTau) throws RemoteException;
    boolean delete(String id) throws RemoteException;
    List<LichTrinhTau> getListLichTrinhTauByDate(LocalDate date) throws RemoteException;
    List<LichTrinhTau> getListLichTrinhTauByDateAndGaDi(LocalDate date, String gaDi) throws RemoteException;
    List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDen(LocalDate date, String gaDi, String gaDen) throws RemoteException;
    List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDenAndGioDi(LocalDate date, String gaDi, String gaDen, String gioDi) throws RemoteException;
    // Phương thức kiểm tra kết nối
    boolean testConnection() throws RemoteException;
    List<String> getTrangThai() throws RemoteException;
}
