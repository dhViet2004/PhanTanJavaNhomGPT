package dao;

import model.VeTau;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DoiVeDAO extends Remote {
    VeTau getVeTau(String id) throws RemoteException;
    boolean doiVe(VeTau veTau) throws RemoteException;
}
