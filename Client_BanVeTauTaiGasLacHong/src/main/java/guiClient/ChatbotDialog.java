package guiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog nhỏ hiển thị trợ lý ảo ở góc màn hình
 */
public class ChatbotDialog extends JDialog {

    private AIAssistantPanel chatbotPanel;
    private final JButton minimizeButton;
    private final JLabel titleLabel;
    private boolean isExpanded = true;

    public ChatbotDialog(Frame owner) {
        super(owner, "Trợ lý ảo", false);

        // Thiết lập dialog
        setSize(350, 500);
        setLocation(owner.getWidth() - 370, owner.getHeight() - 530);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);

        // Panel tiêu đề với nút thu nhỏ
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(41, 128, 185));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        titleLabel = new JLabel("Trợ lý ảo", JLabel.LEFT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        minimizeButton = new JButton("-");
        minimizeButton.setFocusPainted(false);
        minimizeButton.setPreferredSize(new Dimension(25, 25));
        minimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleExpand();
            }
        });

        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(minimizeButton, BorderLayout.EAST);

        // Tạo panel chatbot
        chatbotPanel = new AIAssistantPanel();

        // Thêm các thành phần vào dialog
        add(titlePanel, BorderLayout.NORTH);
        add(chatbotPanel, BorderLayout.CENTER);

        // Không cho phép resize dialog
        setResizable(false);
    }

    /**
     * Chuyển đổi trạng thái thu nhỏ/mở rộng của dialog
     */
    private void toggleExpand() {
        if (isExpanded) {
            // Thu nhỏ dialog
            remove(chatbotPanel);
            setSize(350, 40);
            minimizeButton.setText("+");
            isExpanded = false;
        } else {
            // Mở rộng dialog
            add(chatbotPanel, BorderLayout.CENTER);
            setSize(350, 500);
            minimizeButton.setText("-");
            isExpanded = true;
            validate(); // Cần gọi validate để cập nhật layout
        }
    }

    /**
     * Hiển thị dialog ở góc phải dưới của frame cha
     */
    public void showAtCorner() {
        Frame owner = (Frame) getOwner();

        // Tính toán vị trí để hiển thị ở góc phải dưới
        int x = owner.getX() + owner.getWidth() - this.getWidth() - 20;
        int y = owner.getY() + owner.getHeight() - this.getHeight() - 40;

        // Đảm bảo dialog không vượt ra ngoài màn hình
        x = Math.max(x, 0);
        y = Math.max(y, 0);

        setLocation(x, y);
        setVisible(true);
    }

    /**
     * Xử lý khi cửa sổ chính thay đổi kích thước
     */
    public void adjustPosition() {
        if (isVisible()) {
            Frame owner = (Frame) getOwner();
            int x = owner.getX() + owner.getWidth() - this.getWidth() - 20;
            int y = owner.getY() + owner.getHeight() - this.getHeight() - 40;

            // Đảm bảo dialog không vượt ra ngoài màn hình
            x = Math.max(x, 0);
            y = Math.max(y, 0);

            setLocation(x, y);
        }
    }

    /**
     * Để tự động cập nhật vị trí khi frame cha thay đổi kích thước hoặc vị trí
     */
    public void attachToOwner() {
        Frame owner = (Frame) getOwner();

        // Thêm ComponentListener để theo dõi sự thay đổi kích thước và vị trí của frame cha
        owner.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                adjustPosition();
            }
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                adjustPosition();
            }
        });
    }
}