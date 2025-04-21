package dao;

import model.TuyenTau;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TuyenTauDAO extends Remote {
    TuyenTau getListTuyenTauByGaDiGaDen(String gaDi, String gaDen) throws RemoteException;
    List<TuyenTau> getListTuyenTau() throws RemoteException;
    TuyenTau getTuyenTauById(String id) throws RemoteException;
    boolean save(TuyenTau tuyenTau) throws RemoteException;
    boolean delete(String id) throws RemoteException;
    boolean update(TuyenTau tuyenTau) throws RemoteException;
    TuyenTau getTuyenTauByName(String name) throws RemoteException;
    TuyenTau getListTuyenTauByDiemDiDiemDen(String diemDi, String diemDen) throws RemoteException;
}
