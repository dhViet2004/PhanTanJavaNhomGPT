package guiClient;

/**
 * @Dự án: PhanTanJavaNhomGPT
 * @Class: TraVePanel
 * @Tạo vào ngày: 19/04/2025
 * @Tác giả: Nguyen Huu Sang
 */

import dao.VeTauDAO;
import model.TrangThaiVeTau;
import model.VeTau;
//import utils.PrintPDF;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static guiClient.IconFactory.createTicketIcon;

public class TraVePanel extends JPanel {
    private VeTauDAO veTauDAO;
    private JTextField txtMaVe;
    private JTextField txtTenKhachHang;
    private JTextField txtGiayTo;
    private JTextField txtNgayDi;
    private JComboBox<String> cboDoiTuong;
    private JButton btnTimKiem;
    private JButton btnTraVe;
    private JButton btnLamMoi;
    private JButton btnThoat;
    private JLabel lblLichTrinh;
    private JLabel lblChoNgoi;
    private JLabel lblTrangThai;
    private JLabel lblGiaVe;
    private JButton btnChonLichTrinh;
    private JButton btnChonChoNgoi;
    private JTextField txtPhiTraVe;
    private JLabel lblTienTraLai;

    // Màu sắc chính
    private final Color primaryColor = new Color(41, 128, 185);
    private Locale locale;
    private NumberFormat currencyFormatter;

    public TraVePanel() {
        locale = new Locale("vi", "VN");
        currencyFormatter = NumberFormat.getCurrencyInstance(locale);

        // Thiết lập layout và border
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // Khởi tạo giao diện
        initializeUI();

        // Kết nối đến RMI server
        connectToServer();

        // Thêm các event listener sau khi giao diện đã được khởi tạo
        addEventListeners();
    }

    private void initializeUI() {
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);

        // Thêm tiêu đề
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Panel chứa nội dung chính
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(Color.WHITE);

        // Panel tìm kiếm vé
        JPanel searchPanel = createSearchPanel();
        contentPanel.add(searchPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Panel thông tin vé
        JPanel infoPanel = createInfoPanel();
        contentPanel.add(infoPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Panel nút thao tác
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Thêm event listeners
        addEventListeners();
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(primaryColor);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("QUẢN LÝ TRẢ VÉ TÀU HỎA", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setPreferredSize(new Dimension(0, 30));

        titlePanel.add(titleLabel, BorderLayout.CENTER);

        return titlePanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Tìm Kiếm Vé",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(41, 128, 185)));
        searchPanel.setBackground(Color.WHITE);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBackground(Color.WHITE);

        JLabel lblMaVe = new JLabel("Mã vé:");
        lblMaVe.setIcon(createTicketIcon(16, 16, primaryColor));
        lblMaVe.setFont(new Font("Arial", Font.BOLD, 12));
        inputPanel.add(lblMaVe);

        txtMaVe = new JTextField(20);
        txtMaVe.setFont(new Font("Arial", Font.PLAIN, 12));
        inputPanel.add(txtMaVe);

        btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.setIcon(createSearchIcon(16, 16));
        btnTimKiem.setFont(new Font("Arial", Font.BOLD, 12));
        btnTimKiem.setBackground(primaryColor);
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setBorderPainted(false);
        btnTimKiem.setFocusPainted(false);
        inputPanel.add(btnTimKiem);

        searchPanel.add(inputPanel, BorderLayout.CENTER);

        return searchPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Thông Tin Vé",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(41, 128, 185)));
        infoPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: Tên khách hàng
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblTenKhachHang = new JLabel("Tên khách hàng:");
        lblTenKhachHang.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblTenKhachHang, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        txtTenKhachHang = new JTextField(25);
        formPanel.add(txtTenKhachHang, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel lblGiayTo = new JLabel("Giấy tờ:");
        lblGiayTo.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblGiayTo, gbc);

        gbc.gridx = 5;
        gbc.gridy = 0;
        txtGiayTo = new JTextField(15);
        formPanel.add(txtGiayTo, gbc);

        // Row 2: Ngày đi & Đối tượng
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblNgayDi = new JLabel("Ngày đi:");
        lblNgayDi.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblNgayDi, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        txtNgayDi = new JTextField(25);
        formPanel.add(txtNgayDi, gbc);

        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel lblDoiTuong = new JLabel("Đối tượng:");
        lblDoiTuong.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblDoiTuong, gbc);

        gbc.gridx = 5;
        gbc.gridy = 1;
        cboDoiTuong = new JComboBox<>(new String[]{"Người lớn", "Trẻ em", "Người cao tuổi"});
        formPanel.add(cboDoiTuong, gbc);

        // Row 3: Lịch trình
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel lblLichTrinhText = new JLabel("Lịch trình:");
        lblLichTrinhText.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblLichTrinhText, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        lblLichTrinh = new JLabel("Chưa chọn");
        formPanel.add(lblLichTrinh, gbc);

//        gbc.gridx = 5;
//        gbc.gridy = 2;
//        btnChonLichTrinh = new JButton("Chọn");
//        btnChonLichTrinh.setBackground(Color.LIGHT_GRAY);
//        formPanel.add(btnChonLichTrinh, gbc);

        gbc.gridx = 4;
        gbc.gridy = 2;
        JLabel lblChoNgoiText = new JLabel("Chỗ ngồi:");
        lblChoNgoiText.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblChoNgoiText, gbc);

        gbc.gridx = 5;
        gbc.gridy = 2;
        lblChoNgoi = new JLabel("Chưa chọn");
        formPanel.add(lblChoNgoi, gbc);

//        // Row 4: Chỗ ngồi
//        gbc.gridx = 0;
//        gbc.gridy = 3;
//        JLabel lblChoNgoiText = new JLabel("Chỗ ngồi:");
//        lblChoNgoiText.setFont(new Font("Arial", Font.BOLD, 12));
//        formPanel.add(lblChoNgoiText, gbc);
//
//        gbc.gridx = 1;
//        gbc.gridy = 3;
//        lblChoNgoi = new JLabel("Chưa chọn");
//        formPanel.add(lblChoNgoi, gbc);

//        gbc.gridx = 5;
//        gbc.gridy = 3;
//        btnChonChoNgoi = new JButton("Chọn");
//        btnChonChoNgoi.setBackground(Color.LIGHT_GRAY);
//        formPanel.add(btnChonChoNgoi, gbc);

        // Row 5: Trạng thái & Giá vé
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel lblTrangThaiText = new JLabel("Trạng thái:");
        lblTrangThaiText.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblTrangThaiText, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        lblTrangThai = new JLabel("---");
        formPanel.add(lblTrangThai, gbc);

        gbc.gridx = 4;
        gbc.gridy = 4;
        JLabel lblGiaVeText = new JLabel("Giá vé:");
        lblGiaVeText.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblGiaVeText, gbc);

        gbc.gridx = 5;
        gbc.gridy = 4;
        lblGiaVe = new JLabel("0 VND");
        lblGiaVe.setForeground(new Color(0, 180, 0));
        lblGiaVe.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblGiaVe, gbc);


//        // Row 6: Phí trả vé
//        gbc.gridx = 0;
//        gbc.gridy = 5;
//        JLabel lblPhiTraVeText = new JLabel("Phí trả vé:");
//        lblPhiTraVeText.setFont(new Font("Arial", Font.BOLD, 12));
//        formPanel.add(lblPhiTraVeText, gbc);
//
//        gbc.gridx = 1;
//        gbc.gridy = 5;
//        JTextField txtPhiTraVe = new JTextField("0 VND");
//        txtPhiTraVe.setEditable(true);
//        txtPhiTraVe.setFont(new Font("Arial", Font.PLAIN, 12));
//        formPanel.add(txtPhiTraVe, gbc);
//
//        // Row 7: Tiền trả lại khách
//        gbc.gridx = 4;
//        gbc.gridy = 5;
//        JLabel lblTienTraLaiText = new JLabel("Tiền trả lại:");
//        lblTienTraLaiText.setFont(new Font("Arial", Font.BOLD, 12));
//        formPanel.add(lblTienTraLaiText, gbc);
//
//        gbc.gridx = 5;
//        gbc.gridy = 5;
//        JLabel lblTienTraLai = new JLabel("0 VND");
//        lblTienTraLai.setForeground(new Color(0, 128, 0));
//        lblTienTraLai.setFont(new Font("Arial", Font.BOLD, 14));
//        formPanel.add(lblTienTraLai, gbc);

        // Row 6: Phí trả vé
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel lblPhiTraVeText = new JLabel("Phí trả vé:");
        lblPhiTraVeText.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblPhiTraVeText, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        txtPhiTraVe = new JTextField("0 VND"); // Khởi tạo đúng cách ở đây
        txtPhiTraVe.setEditable(true);
        txtPhiTraVe.setFont(new Font("Arial", Font.PLAIN, 12));
        formPanel.add(txtPhiTraVe, gbc);

// Row 7: Tiền trả lại khách
        gbc.gridx = 4;
        gbc.gridy = 5;
        JLabel lblTienTraLaiText = new JLabel("Tiền trả lại:");
        lblTienTraLaiText.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(lblTienTraLaiText, gbc);

        gbc.gridx = 5;
        gbc.gridy = 5;
        lblTienTraLai = new JLabel("0 VND"); // Khởi tạo đúng cách ở đây
        lblTienTraLai.setForeground(new Color(0, 128, 0));
        lblTienTraLai.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblTienTraLai, gbc);

        infoPanel.add(formPanel, BorderLayout.CENTER);
        return infoPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        btnTraVe = new JButton("Trả Vé");
        btnTraVe.setFont(new Font("Arial", Font.BOLD, 12));
        btnTraVe.setBackground(primaryColor);
        btnTraVe.setForeground(Color.WHITE);
        btnTraVe.setBorderPainted(false);
        btnTraVe.setFocusPainted(false);
        btnTraVe.setPreferredSize(new Dimension(120, 35));
        btnTraVe.setIcon(createIconArrow(16, 16, Color.WHITE));
        btnTraVe.setEnabled(false);

        btnLamMoi = new JButton("Làm Mới");
        btnLamMoi.setFont(new Font("Arial", Font.BOLD, 12));
        btnLamMoi.setBackground(new Color(108, 122, 137));
        btnLamMoi.setForeground(Color.WHITE);
        btnLamMoi.setBorderPainted(false);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setPreferredSize(new Dimension(120, 35));
        btnLamMoi.setIcon(createIconRefresh(16, 16, Color.WHITE));

        btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Arial", Font.BOLD, 12));
        btnThoat.setBackground(new Color(231, 76, 60));
        btnThoat.setForeground(Color.WHITE);
        btnThoat.setBorderPainted(false);
        btnThoat.setFocusPainted(false);
        btnThoat.setPreferredSize(new Dimension(120, 35));
        btnThoat.setIcon(createIconExit(16, 16, Color.WHITE));

        buttonPanel.add(btnTraVe);
        buttonPanel.add(btnLamMoi);
        buttonPanel.add(btnThoat);

        return buttonPanel;
    }

    private ImageIcon createSearchIcon(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);

        // Vẽ hình tròn của kính lúp
        g2.drawOval(1, 1, width - 8, height - 8);
        g2.drawLine(width - 4, height - 4, width - 8, height - 8);

        g2.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon createIconArrow(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ mũi tên trở lại
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(width / 2, 4, 4, height / 2);
        g2.drawLine(4, height / 2, width / 2, height - 4);
        g2.drawLine(4, height / 2, width - 4, height / 2);

        g2.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon createIconRefresh(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ biểu tượng refresh
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(2, 2, width - 4, height - 4, 45, 270);
        // Vẽ mũi tên
        int[] xPoints = {width - 4, width - 8, width};
        int[] yPoints = {2, 6, 8};
        g2.fillPolygon(xPoints, yPoints, 3);

        g2.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon createIconExit(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ biểu tượng thoát
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(4, 4, width - 4, height - 4);
        g2.drawLine(width - 4, 4, 4, height - 4);

        g2.dispose();
        return new ImageIcon(image);
    }

    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 9090);
            this.veTauDAO = (VeTauDAO) registry.lookup("veTauDAO");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối đến server: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addEventListeners() {
        btnTimKiem.addActionListener(e -> timVe());

        btnTraVe.addActionListener(e -> traVe());

        btnLamMoi.addActionListener(e -> {
            // Clear all fields
            txtMaVe.setText("");
            txtTenKhachHang.setText("");
            txtGiayTo.setText("");
            txtNgayDi.setText("");
            cboDoiTuong.setSelectedIndex(0);
            lblLichTrinh.setText("Chưa chọn");
            lblChoNgoi.setText("Chưa chọn");
            lblTrangThai.setText("---");
            lblGiaVe.setText("0 VND");
            txtPhiTraVe.setText("0 VND");
            lblTienTraLai.setText("0 VND");
            btnTraVe.setEnabled(false);
        });

        btnThoat.addActionListener(e -> {
            // Exit functionality
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn thoát không?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window instanceof JFrame) {
                    window.dispose();
                }
            }
        });

        // Thêm DocumentListener cho txtPhiTraVe
        if (txtPhiTraVe != null) {
            txtPhiTraVe.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void updateTienTraLai() {
                    try {
                        // Lấy giá trị phí trả vé từ trường văn bản, loại bỏ các ký tự không phải số
                        String phiTraVeStr = txtPhiTraVe.getText().replaceAll("[^0-9]", "");
                        double phiTraVe = phiTraVeStr.isEmpty() ? 0 : Double.parseDouble(phiTraVeStr);

                        // Lấy giá trị vé từ nhãn, loại bỏ các ký tự không phải số
                        String giaVeStr = lblGiaVe.getText().replaceAll("[^0-9]", "");
                        double giaVe = giaVeStr.isEmpty() ? 0 : Double.parseDouble(giaVeStr);

                        // Tính tiền trả lại theo quy tắc: giá vé - phí trả vé
                        double tienTraLai = giaVe - phiTraVe;

                        // Nếu tiền trả lại âm, đặt về 0
                        if (tienTraLai < 0) tienTraLai = 0;

                        lblTienTraLai.setText(currencyFormatter.format(tienTraLai));
                    } catch (Exception e) {
                        lblTienTraLai.setText("0 VND");
                    }
                }

                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    updateTienTraLai();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    updateTienTraLai();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    updateTienTraLai();
                }
            });
        }
    }




    private void timVe() {
        try {
            String maVe = txtMaVe.getText().trim();

            if (maVe.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập mã vé!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            VeTau veTau = veTauDAO.getById(maVe);

            if (veTau == null) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy vé!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Hiển thị thông tin vé
            txtTenKhachHang.setText(veTau.getTenKhachHang());
            txtGiayTo.setText(veTau.getGiayTo());
            txtNgayDi.setText(veTau.getNgayDi().toString());

            if (veTau.getLichTrinhTau() != null) {
                lblLichTrinh.setText(veTau.getLichTrinhTau().getMaLich());
            }

            if (veTau.getChoNgoi() != null) {
                lblChoNgoi.setText(veTau.getChoNgoi().getMaCho());
            }

            lblTrangThai.setText(veTau.getTrangThai().toString());
            double giaVe = veTau.getGiaVe();
            lblGiaVe.setText(currencyFormatter.format(giaVe));

            // Cập nhật phí trả vé là 20% giá vé theo quy định mới
            double phiTraVe = giaVe * 0.2;
            txtPhiTraVe.setText(currencyFormatter.format(phiTraVe));

            // Tính tiền trả lại khách (giá vé - phí trả vé)
            double tienTraLai = giaVe - phiTraVe;
            lblTienTraLai.setText(currencyFormatter.format(tienTraLai));

            // Kiểm tra điều kiện để kích hoạt nút trả vé
            boolean coTheTraVe = (veTau.getTrangThai() == TrangThaiVeTau.CHO_XAC_NHAN ||
                    veTau.getTrangThai() == TrangThaiVeTau.DA_THANH_TOAN);
            btnTraVe.setEnabled(coTheTraVe);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tìm vé: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void traVe() {
        try {
            String maVe = txtMaVe.getText().trim();

            if (maVe.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng tìm vé trước khi trả vé!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn trả vé này không?",
                    "Xác nhận trả vé",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            boolean success = veTauDAO.updateStatusToReturned(maVe);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Trả vé thành công!",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                // Refresh thông tin vé
                VeTau veTau = veTauDAO.getById(maVe);
                if (veTau != null) {
                    lblTrangThai.setText(veTau.getTrangThai().toString());
                    btnTraVe.setEnabled(false);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Trả vé không thành công!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi trả vé: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Tính phí trả vé dựa trên giá vé
     * @param giaVe Giá vé gốc
     * @return Phí trả vé (20% giá vé)
     */
    private double tinhPhiTraVe(double giaVe) {
        return giaVe * 0.2; // 20% giá vé
    }

    /**
     * Tính tiền trả lại khách dựa trên giá vé và phí trả vé
     * @param giaVe Giá vé gốc
     * @param phiTraVe Phí trả vé
     * @return Tiền trả lại cho khách
     */
    private double tinhTienTraLai(double giaVe, double phiTraVe) {
        double tienTraLai = giaVe - phiTraVe;
        return tienTraLai > 0 ? tienTraLai : 0;
    }


}