package GUI.ui.menu;

import java.rmi.RemoteException;

/**
 *
 * @author RAVEN
 */
public interface MenuEvent {

    public void selected(int index, int subIndex) throws RemoteException;
}
