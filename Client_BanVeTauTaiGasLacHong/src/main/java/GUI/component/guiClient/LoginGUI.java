package GUI.component.guiClient;

import javax.swing.*;

public class LoginGUI {


    public static void main(String[] args) {
        // Chạy ứng dụng trên Event Dispatch Thread (luồng quản lý giao diện)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Loading loading = new Loading();
                loading.setVisible(true);

            }
        });
    }
}
