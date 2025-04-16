package guiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MainGUI extends JFrame {

    private JPanel contentPanel; // Content panel managed by CardLayout
    private CardLayout cardLayout; // CardLayout for switching panels
    private Map<String, JPanel> panelMap; // Cache for panels

    public MainGUI() {
        setTitle("Quản lý tàu hỏa");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize panel map
        panelMap = new HashMap<>();

        // Create the main layout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create vertical menu
        JPanel verticalMenu = createVerticalMenu();
        mainPanel.add(verticalMenu, BorderLayout.WEST);

        // Create content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add default content panel
        JPanel defaultPanel = createDefaultContentPanel();
        contentPanel.add(defaultPanel, "Trang chủ");
        panelMap.put("Trang chủ", defaultPanel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185)); // Blue header background
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("Hệ thống quản lý tàu hỏa", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createVerticalMenu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(52, 73, 94)); // Dark gray menu background
        menuPanel.setPreferredSize(new Dimension(250, 0));

        String[] menuItems = {
                "Trang chủ", "Thông tin hoạt động", "Quản lý khách hàng",
                "Quản lý vé", "Quản lý lịch trình", "Báo cáo", "Cài đặt hệ thống"
        };

        for (String item : menuItems) {
            JPanel menuItemPanel = new JPanel(new BorderLayout());
            menuItemPanel.setBackground(new Color(52, 73, 94));
            menuItemPanel.setMaximumSize(new Dimension(250, 50));

            JLabel menuLabel = new JLabel(item);
            menuLabel.setForeground(Color.WHITE);
            menuLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            menuLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

            menuItemPanel.add(menuLabel, BorderLayout.CENTER);

            // Hover effect
            menuItemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    menuItemPanel.setBackground(new Color(41, 128, 185)); // Blue hover background
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    menuItemPanel.setBackground(new Color(52, 73, 94));
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Switch content based on the menu item clicked
                    switchToPanel(item);
                }
            });

            menuPanel.add(menuItemPanel);
        }

        return menuPanel;
    }

    private JPanel createDefaultContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel contentLabel = new JLabel("Chào mừng đến hệ thống quản lý tàu hỏa!", JLabel.CENTER);
        contentLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        contentLabel.setForeground(Color.GRAY);

        panel.add(contentLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPlaceholderPanel(String menuName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel placeholderLabel = new JLabel("Nội dung cho " + menuName + " đang được phát triển.", JLabel.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        placeholderLabel.setForeground(Color.GRAY);

        panel.add(placeholderLabel, BorderLayout.CENTER);
        return panel;
    }

    private void switchToPanel(String panelName) {
        // Check if the panel already exists in the cache
        if (!panelMap.containsKey(panelName)) {
            JPanel newPanel;
            if (panelName.equals("Quản lý lịch trình")) {
                newPanel = new LichTrinhTauPanel(); // Replace with your actual panel class
            } else {
                newPanel = createPlaceholderPanel(panelName);
            }
            contentPanel.add(newPanel, panelName);
            panelMap.put(panelName, newPanel);
        }

        // Show the panel
        cardLayout.show(contentPanel, panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            new MainGUI().setVisible(true);
        });
    }
}