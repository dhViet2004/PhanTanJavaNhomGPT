package GUI.ui.main;

import GUI.component.*;
import GUI.ui.menu.MenuEvent;
import dao.NhanVienDAO;
import dao.impl.NhanVienDAOImpl;
import model.NhanVien;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

/**
 *
 * @author RAVEN
 */
public class Main extends javax.swing.JFrame {

    /**
     * Creates new form Main
     */
    private NhanVien nhanVien;
    public Main(NhanVien nv) throws RemoteException {
        nhanVien = nv;
        initComponents();
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);

        // Thiết lập giao diện phẳng
        setupFlatDesign();

        // Hiển thị tên nhân viên ở menu
        menu1.setTenNhanVien(nhanVien.getTenNV());

        // Kiểm tra vai trò của nhân viên và điều chỉnh menu
        if (nhanVien != null && nhanVien.getChucVu().equals("Quản lý")) {
            menu1.enableMenuItem(6, true); // Kích hoạt "Quản lý nhân viên" cho quản lý
            menu1.enableMenuItem(3, true); // Kích hoạt "Quản lý lịch trình tàu" cho quản lý (ví dụ)
        } else {
            menu1.enableMenuItem(6, false); // Vô hiệu hóa "Quản lý nhân viên" cho nhân viên
            menu1.enableMenuItem(3, false); // Vô hiệu hóa "Quản lý lịch trình tàu" cho nhân viên (ví dụ)
        }

        menu1.setEvent(new MenuEvent() {
            @Override
            public void selected(int index, int subIndex) throws RemoteException {
                if (index == 0) {
                    showForm(new HomeForm());
                }
                else if (index == 2 && subIndex == 1) {
                    showForm(new DoiVePanel(nhanVien));
                } else if (index==2 && subIndex==2) {
                    // trả vé
                    showForm(new TraVePanel(nhanVien));

                } else if (index == 3){
                    showForm(new LichTrinhTauPanel());
                } else if (index == 4 && subIndex == 1) {
                    showForm(new TraCuuVePanel());
                } else if (index == 4 && subIndex == 2) {
                    showForm(new TraCuuTuyenPanel());
                } else if (index==4 && subIndex == 3) {
                    // tra cứu hóa đơn
                    showForm(new TraCuuHoaDonPanel());
                } else if (index == 5 && subIndex == 1) {
                    showForm(new ThongKeVePanel());
                } else if (index == 5 && subIndex == 2) {
                    // doanh thu bán vé
                    showForm(new ThongKeDoanhThuPanel());
                }
                else if (index == 6) {
                    if (!menu1.getComponent(index).isEnabled()) {
                        JOptionPane.showMessageDialog(Main.this, "Chức năng này chỉ dành cho quản lý.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        return; // Ngăn không cho thực hiện hành động tiếp theo
                    } else {
                        showForm(new QuanLyNhanVienPanel());
                    }
                } else if (index == 7) {
                    // quản lý khách hàng
                        showForm(new QuanLyKhachHangPanel());
                } else if (index == 8) {
                    showForm(new DoanhThuTheoCaPanel());
                } else if (index == 9) {
                    int confirm = JOptionPane.showConfirmDialog(
                            Main.this,
                            "Bạn có chắc chắn muốn đăng xuất?",
                            "Xác nhận đăng xuất",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (confirm == JOptionPane.YES_OPTION) {
                        FrmDangNhap dn = new FrmDangNhap();
                        dn.setVisible(true);
                        Main.this.dispose();
                    }
                }
            }
        });
    }

    // Phương thức thiết lập giao diện phẳng
    private void setupFlatDesign() {
        // Thiết lập tiêu đề và màu nền cho JFrame
        setTitle("Hệ thống quản lý");

        // Thiết lập màu nền hiện đại cho panel chính
        jPanel1.setBackground(new Color(250, 250, 250));
        jPanel1.setBorder(BorderFactory.createEmptyBorder()); // Loại bỏ viền để có vẻ phẳng hơn

        // Thiết lập màu sắc cho body
        body.setBackground(new Color(255, 255, 255));

        // Tạo bóng nhẹ cho menu để tạo hiệu ứng nổi
        scrollPaneWin111.setBorder(BorderFactory.createEmptyBorder());
    }

    private void showForm(Component com) {
        body.removeAll();
        body.add(com);
        body.repaint();
        body.revalidate();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        scrollPaneWin111 = new GUI.ui.scroll.win11.ScrollPaneWin11();
        menu1 = new GUI.ui.menu.Menu();
        header1 = new GUI.component.Header();
        body = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Cài đặt màu nền và viền cho jPanel1 (thiết kế phẳng)
        jPanel1.setBackground(new java.awt.Color(250, 250, 250));
        jPanel1.setBorder(BorderFactory.createEmptyBorder());

        // Cài đặt cho scrollPane
        scrollPaneWin111.setBorder(null);
        scrollPaneWin111.setViewportView(menu1);

        // Cài đặt header với màu sắc phẳng
        header1.setBackground(new java.awt.Color(41, 128, 185));

        // Thiết lập body với màu phẳng và layout
        body.setBackground(new java.awt.Color(255, 255, 255));
        body.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(scrollPaneWin111, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                        .addComponent(header1, javax.swing.GroupLayout.DEFAULT_SIZE, 1096, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(header1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPaneWin111, javax.swing.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(6, 6, 6)
                                                .addComponent(body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap())))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
        System.out.println(nhanVien);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            // Chuyển sang FlatLaf Look and Feel (hoặc System Look and Feel nếu không có FlatLaf)
            try {
                // Thử dùng FlatLightLaf nếu có sẵn
                Class.forName("com.formdev.flatlaf.FlatLightLaf");
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            } catch (ClassNotFoundException e) {
                // Nếu không có FlatLaf, sử dụng System Look and Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception ex) {
            // Fallback to Nimbus
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex1) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            } catch (InstantiationException ex1) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            } catch (IllegalAccessException ex1) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            } catch (javax.swing.UnsupportedLookAndFeelException ex1) {
                java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex1);
            }
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
//                new Main().setVisible(true);
                Loading loading  = new Loading();
                loading.setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel body;
    private GUI.component.Header header1;
    private javax.swing.JPanel jPanel1;
    private GUI.ui.menu.Menu menu1;
    private GUI.ui.scroll.win11.ScrollPaneWin11 scrollPaneWin111;
    // End of variables declaration//GEN-END:variables
}