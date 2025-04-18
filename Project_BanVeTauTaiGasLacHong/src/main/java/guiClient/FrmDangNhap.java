package guiClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FrmDangNhap extends JFrame implements ActionListener {
    private JPanel mainPanel;
    private JLabel backgroundLabel;
    private JTextField txtMaNhanVien;
    private JPasswordField txtMatKhau;
    private JTextField txtMatKhauVisible; // Mã mật khẩu hiển thị dưới dạng text
    private JButton btnQuenMatKhau;
    private JButton btnDangNhap;
    private JCheckBox showPasswordCheckBox;

    public FrmDangNhap() {
        setTitle("Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // Panel hình ảnh (70%)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension((int) (getWidth() * 0.7), getHeight()));
        try {
            BufferedImage originalImage = ImageIO.read(getClass().getResource("/Anh_HeThong/banner_1.jpg"));
            int targetHeight = getHeight();
            int targetWidth = (int) (((double) targetHeight / originalImage.getHeight()) * originalImage.getWidth());

            backgroundLabel = new JLabel(new ImageIcon(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)));
            imagePanel.add(backgroundLabel, BorderLayout.CENTER);
        } catch (IOException | NullPointerException e) {
            System.err.println("Không tìm thấy ảnh nền: " + e.getMessage());
            imagePanel.setBackground(new Color(240, 248, 255));
        }

        // Panel đăng nhập (30%) - container
        JPanel loginContainer = new JPanel(new GridBagLayout());
        loginContainer.setPreferredSize(new Dimension((int) (getWidth() * 0.3), getHeight()));
        loginContainer.setBackground(new Color(240, 240, 240));

        // Panel chính đăng nhập với bo góc và padding
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLabel = new JLabel("Ga Lạc Hồng", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 144, 255));
        loginPanel.add(titleLabel, gbc);

        // Mã nhân viên
        gbc.gridy++;
        gbc.insets = new Insets(30, 10, 5, 10);
        JLabel maNhanVienLabel = new JLabel("Mã nhân viên:");
        maNhanVienLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginPanel.add(maNhanVienLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 10, 15, 10);
        txtMaNhanVien = new JTextField(15);
        txtMaNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginPanel.add(txtMaNhanVien, gbc);

        // Mật khẩu
        gbc.gridy++;
        gbc.insets = new Insets(20, 10, 5, 10);
        JLabel matKhauLabel = new JLabel("Mật khẩu:");
        matKhauLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginPanel.add(matKhauLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 10, 20, 10);

        // Mật khẩu hiển thị
        txtMatKhau = new JPasswordField(15);
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginPanel.add(txtMatKhau, gbc);

        // Checkbox hiển thị mật khẩu
        showPasswordCheckBox = new JCheckBox("Hiển thị mật khẩu");
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPasswordCheckBox.isSelected()) {
                    txtMatKhau.setEchoChar((char) 0); // Hiển thị mật khẩu
                } else {
                    txtMatKhau.setEchoChar('*'); // Ẩn mật khẩu
                }
            }
        });

        gbc.gridy++;
        loginPanel.add(showPasswordCheckBox, gbc);

        // Nút
        gbc.gridy++;
        gbc.insets = new Insets(30, 10, 10, 10);
        btnDangNhap = new JButton("Đăng nhập");
        btnDangNhap.setBackground(new Color(30, 144, 255));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDangNhap.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        // Hover hiệu ứng
        btnDangNhap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnDangNhap.setBackground(new Color(0, 120, 215));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnDangNhap.setBackground(new Color(30, 144, 255));
            }
        });

        btnQuenMatKhau = new JButton("Quên mật khẩu?");
        btnQuenMatKhau.setContentAreaFilled(false);
        btnQuenMatKhau.setBorderPainted(false);
        btnQuenMatKhau.setForeground(Color.GRAY);
        btnQuenMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnQuenMatKhau.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnDangNhap, BorderLayout.NORTH);
        buttonPanel.add(btnQuenMatKhau, BorderLayout.SOUTH);
        loginPanel.add(buttonPanel, gbc);

        loginContainer.add(loginPanel);

        // Add vào main panel
        mainPanel.add(imagePanel, BorderLayout.WEST);
        mainPanel.add(loginContainer, BorderLayout.EAST);

        // Sự kiện
        btnDangNhap.addActionListener(this);
        btnQuenMatKhau.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnDangNhap) {
            MainGUI mainGUI = new MainGUI();
            mainGUI.setVisible(true);
            this.dispose();
        } else if (e.getSource() == btnQuenMatKhau) {
            JOptionPane.showMessageDialog(this, "Bạn đã nhấn quên mật khẩu!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrmDangNhap frame = new FrmDangNhap();
            frame.setVisible(true);
        });
    }
}
