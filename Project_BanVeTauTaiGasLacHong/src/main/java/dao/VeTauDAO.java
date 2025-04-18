package dao;

import model.VeTau;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @Dự án: PhanTanJavaNhomGPT
 * @Class: VeDAO
 * @Tạo vào ngày: 18/04/2025
 * @Tác giả: Nguyen Huu Sang
 */
public interface VeTauDAO extends Remote {
    boolean save(VeTau veTau) throws RemoteException;
    List<VeTau> getAllList() throws RemoteException;
    VeTau getById(String id) throws RemoteException;
    boolean update(VeTau veTau) throws RemoteException;
    boolean delete(String id) throws RemoteException;

    List<VeTau> getByInvoiceId(String invoiceId) throws RemoteException;
}