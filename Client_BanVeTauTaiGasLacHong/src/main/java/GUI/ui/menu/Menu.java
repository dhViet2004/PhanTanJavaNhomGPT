package GUI.ui.menu;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.rmi.RemoteException;

public class Menu extends JComponent {

    public MenuEvent getEvent() {
        return event;
    }

    public void setEvent(MenuEvent event) {
        this.event = event;
    }

    private MenuEvent event;
    private MigLayout layout;
    private String[][] menuItems = new String[][]{
            {"Dashboard"},
            {"Bán vé"},
            {"Quản lý vé", "Đổi vé", "Trả vé"},
            {"Quản lý lịch trình tàu"},
            {"Tra cứu", "Tra cứu vé", "Tra cứu theo tuyến", "Tra cứu hóa đơn"},
            {"Thống kê", "Lượng vé theo thời gian", "Doanh thu bán vé"},
            {"Quản lý nhân viên"},
            {"Quản lý khách hàng"},
            {"Quản lý khuyến mãi"},
            {"Doanh thu theo ca"},
            {"Đăng xuất"}
    };

    private JLabel lblTenNhanVien; // Thêm JLabel để hiển thị tên nhân viên
    private JPanel bottomPanel; // Panel chứa tên nhân viên

    public Menu() {
        init();
    }

    public void enableMenuItem(int index, boolean enabled) {
        if (index >= 0 && index < getComponentCount()) {
            Component comp = getComponent(index);
            if (comp instanceof MenuItem) {
                ((MenuItem) comp).setEnabled(enabled);
                if (!enabled) {
                    ((MenuItem) comp).setToolTipText("Chức năng này chỉ dành cho quản lý"); // Thêm tooltip
                } else {
                    ((MenuItem) comp).setToolTipText(null); // Xóa tooltip nếu được kích hoạt lại
                }
            }
        }
    }

    private void init() {
        layout = new MigLayout("wrap 1, fillx, gapy 0, inset 2", "fill"); // Trở lại layout ban đầu

        setLayout(layout);
        setOpaque(true);
        //  Init MenuItem
        for (int i = 0; i < menuItems.length; i++) {
            addMenu(menuItems[i][0], i);
        }

        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false); // Để màu nền của Menu hiển thị
        lblTenNhanVien = new JLabel("Chưa đăng nhập"); // Khởi tạo JLabel với giá trị mặc định
        lblTenNhanVien.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblTenNhanVien.setForeground(Color.WHITE);
        lblTenNhanVien.setHorizontalAlignment(SwingConstants.CENTER);
        bottomPanel.add(lblTenNhanVien, BorderLayout.SOUTH);

        add(bottomPanel, "dock south, h 30!"); // Thêm panel vào cuối và neo ở phía nam
    }

    public void setTenNhanVien(String tenNhanVien) {
        lblTenNhanVien.setText("Nhân viên: " + tenNhanVien);
    }

    private Icon getIcon(int index) {
        URL url = getClass().getResource("/menu/" + index + ".png");
        if (url != null) {
            return new ImageIcon(url);
        } else {
            return null;
        }
    }

    private void addMenu(String menuName, int index) {
        int length = menuItems[index].length;
        MenuItem item = new MenuItem(menuName, index, length > 1);
        item.setFont(new Font("SansSerif", Font.BOLD, 16));

        Icon icon = getIcon(index);
        if (icon != null) {
            item.setIcon(icon);
        }

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // Đặt tất cả các MenuItem khác về không active
                resetAllMenuItemsActiveState();
                item.setActive(true); // Đặt mục hiện tại là active

                if (length > 1) {
                    if (!item.isSelected()) {
                        item.setSelected(true);
                        addSubMenu(item, index, length, getComponentZOrder(item));
                    } else {
                        hideMenu(item, index);
                        item.setSelected(false);
                    }
                } else {
                    if (item.isEnabled()) { // Kiểm tra xem menu item có được enable không
                        if (event != null) {
                            try {
                                event.selected(index, 0);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        // Hiển thị thông báo nếu bị vô hiệu hóa
                        JOptionPane.showMessageDialog(Menu.this, "Chức năng này chỉ dành cho quản lý.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        add(item, "h 50!");

        // Vô hiệu hóa mục "Quản lý nhân viên" ban đầu (index là 6)
        if (index == 6) {
            item.setEnabled(false);
            item.setToolTipText("Chức năng này chỉ dành cho quản lý");
        }
    }

    private void addSubMenu(MenuItem item, int index, int length, int indexZorder) {
        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, inset 0, gapy 0", "fill"));
        panel.setName(index + "");
        panel.setBackground(new Color(41, 128, 185));

        for (int i = 1; i < length; i++) {
            MenuItem subItem = new MenuItem(menuItems[index][i], i, false);
            subItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    resetAllMenuItemsActiveState();
                    subItem.setActive(true); // Đặt submenu item là active
                    if (event != null) {
                        try {
                            event.selected(index, subItem.getIndex());
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            subItem.initSubMenu(i, length);
            panel.add(subItem);
        }
        add(panel, "h 0!", indexZorder + 1);
        MenuAnimation.showMenu(panel, item, layout, true);
    }

    // Đặt tất cả các MenuItem về trạng thái không active
    private void resetAllMenuItemsActiveState() {
        for (Component comp : getComponents()) {
            if (comp instanceof MenuItem) {
                ((MenuItem) comp).setActive(false);
            } else if (comp instanceof JPanel) {
                for (Component subComp : ((JPanel) comp).getComponents()) {
                    if (subComp instanceof MenuItem) {
                        ((MenuItem) subComp).setActive(false);
                    }
                }
            }
        }
    }

    private void hideMenu(MenuItem item, int index) {
        for (Component com : getComponents()) {
            if (com instanceof JPanel && com.getName() != null && com.getName().equals(index + "")) {
                com.setName(null);
                MenuAnimation.showMenu(com, item, layout, false);
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setColor(new Color(41, 128, 185));
        g2.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        super.paintComponent(grphcs);
    }
}