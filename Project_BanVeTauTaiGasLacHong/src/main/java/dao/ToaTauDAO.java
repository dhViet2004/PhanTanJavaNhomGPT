package dao;

import model.ToaTau;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ToaTauDAO extends Remote {

    ToaTau getToaTauById(String id) throws RemoteException;

    List<ToaTau> getToaByTau(String maTau) throws RemoteException;
}
