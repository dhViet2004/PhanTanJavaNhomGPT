package GUI.ui.menu;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.net.URL;

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
            {"Tra cứu", "Tra cứu vé", "Tra cứu theo tuyến", "Tra cứu hóa đơn", "Tra cứu theo chuyến"},
            {"Thống kê", "Doanh thu theo ca", "Lượng vé theo thời gian", "Doanh thu bán vé"},
            {"Quản lý nhân viên"}
    };

    public Menu() {
        init();
    }

    private void init() {
        layout = new MigLayout("wrap 1, fillx, gapy 0, inset 2", "fill");  // Giảm gapy

        setLayout(layout);
        setOpaque(true);
        //  Init MenuItem
        for (int i = 0; i < menuItems.length; i++) {
            addMenu(menuItems[i][0], i);
        }
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
                    if (event != null) {
                        event.selected(index, 0);
                    }
                }
            }
        });
        add(item, "h 50!");
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
                        event.selected(index, subItem.getIndex());
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
