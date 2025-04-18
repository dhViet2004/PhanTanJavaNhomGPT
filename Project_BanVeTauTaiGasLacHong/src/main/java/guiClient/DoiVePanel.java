package guiClient;

import dao.ChoNgoiDoiVeDAO;
import dao.DoiVeDAO;
import dao.LichTrinhTauDAO;
import dao.ToaTauDoiVeDAO;
import model.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DoiVePanel extends JPanel {
    // Thêm các biến cho preloading
    private boolean isPreloadingData = false;
    private SwingWorker<Map<String, List<LichTrinhTau>>, Void> preloadWorker;
    private Map<String, List<LichTrinhTau>> cachedLichTrinh = new ConcurrentHashMap<>();

    private DoiVeDAO doiVeDAO;
    private LichTrinhTauDAO lichTrinhTauDAO;
    private ToaTauDoiVeDAO toaTauDAO;
    private ChoNgoiDoiVeDAO choNgoiDAO;

    // Màu sắc chính
    private Color primaryColor = new Color(41, 128, 185); // Màu xanh dương
    private Color successColor = new Color(46, 204, 113); // Màu xanh lá
    private Color warningColor = new Color(243, 156, 18); // Màu vàng cam
    private Color dangerColor = new Color(231, 76, 60);   // Màu đỏ
    private Color grayColor = new Color(108, 117, 125);   // Màu xám
    private Color darkTextColor = new Color(52, 73, 94);  // Màu chữ tối
    private Color lightBackground = new Color(240, 240, 240); // Màu nền nhạt

    // Components for UI
    private JTextField txtMaVe;
    private JTextField txtTenKhachHang;
    private JTextField txtGiayTo;
    private JTextField txtNgayDi;
    private JComboBox<String> cboDoiTuong;
    private JButton btnTimVe;
    private JButton btnDoiVe;
    private JButton btnLamMoi;
    private JButton btnChonLichTrinh;
    private JButton btnChonChoNgoi;
    private JLabel lblTrangThai;
    private JLabel lblGiaVe;
    private JLabel lblLichTrinh;
    private JLabel lblChoNgoi;
    private JLabel lblStatus;
    private JTable tblLichSu;
    private DefaultTableModel modelLichSu;
    private JProgressBar progressBar;

    // Lưu trữ dữ liệu
    private VeTau veTauHienTai;
    private LichTrinhTau lichTrinhDaChon;
    private ChoNgoi choNgoiDaChon;
    private KhuyenMai khuyenMaiDaChon;
    private NumberFormat currencyFormatter;
    private Locale locale;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    // Constants
    private final String LOADING_TEXT = "Đang tải dữ liệu...";
    private final String READY_TEXT = "Sẵn sàng";
    private final String ERROR_TEXT = "Đã xảy ra lỗi";
    private final String SUCCESS_TEXT = "Thao tác thành công";

    public DoiVePanel() {
        locale = new Locale("vi", "VN");
        currencyFormatter = NumberFormat.getCurrencyInstance(locale);

        // Đảm bảo các nút hiển thị đúng màu sắc
        UIManager.put("Button.background", Color.WHITE);
        UIManager.put("Button.opaque", Boolean.TRUE);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // Khởi tạo giao diện trước
        initializeUI();

        // Thiết lập trạng thái ban đầu
        updateStatus(READY_TEXT, false);

        // Kết nối đến RMI server
        connectToServer();
        startPreloadingData();
    }

    private void connectToServer() {
        try {
            updateStatus(LOADING_TEXT, true);

            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9090);
            doiVeDAO = (DoiVeDAO) registry.lookup("doiVeDAO");
            lichTrinhTauDAO = (LichTrinhTauDAO) registry.lookup("lichTrinhTauDAO");
//            khuyenMaiDAO = (KhuyenMaiDAO) registry.lookup("khuyenMaiDAO");
            toaTauDAO = (ToaTauDoiVeDAO) registry.lookup("toaTauDoiVeDAO");
            choNgoiDAO = (ChoNgoiDoiVeDAO) registry.lookup("choNgoiDoiVeDAO");

            // Kiểm tra kết nối
            try {
                if (doiVeDAO.testConnection()) {
                    SwingUtilities.invokeLater(() -> {
                        updateStatus(READY_TEXT, false);
                        startPreloadingData();
                    });
                } else {
                    updateStatus(ERROR_TEXT, false);
                }
            } catch (Exception e) {
                updateStatus(ERROR_TEXT, false);
                e.printStackTrace();
            }

        } catch (RemoteException | NotBoundException e) {
            updateStatus(ERROR_TEXT, false);
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối đến server RMI: " + e.getMessage(),
                    "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void startPreloadingData() {
        if (isPreloadingData || lichTrinhTauDAO == null) {
            return;
        }

        isPreloadingData = true;

        preloadWorker = new SwingWorker<Map<String, List<LichTrinhTau>>, Void>() {
            @Override
            protected Map<String, List<LichTrinhTau>> doInBackground() throws Exception {
                Map<String, List<LichTrinhTau>> result = new ConcurrentHashMap<>();

                try {
                    // Lấy danh sách lịch trình và nhóm theo các tiêu chí phổ biến
                    List<LichTrinhTau> allLichTrinh = lichTrinhTauDAO.getAllList();

                    // Nhóm lịch trình theo ga đi
                    Map<String, List<LichTrinhTau>> lichTrinhByGaDi = new HashMap<>();
                    for (LichTrinhTau lichTrinh : allLichTrinh) {
                        String gaDi = lichTrinh.getTau().getTuyenTau().getGaDi();
                        lichTrinhByGaDi.computeIfAbsent(gaDi, k -> new ArrayList<>()).add(lichTrinh);
                    }
                    result.put("gaDi", new ArrayList<>(lichTrinhByGaDi.values().stream()
                            .flatMap(List::stream)
                            .limit(100) // Giới hạn số lượng để tối ưu bộ nhớ
                            .toList()));

                    // Nhóm lịch trình theo ga đến
                    Map<String, List<LichTrinhTau>> lichTrinhByGaDen = new HashMap<>();
                    for (LichTrinhTau lichTrinh : allLichTrinh) {
                        String gaDen = lichTrinh.getTau().getTuyenTau().getGaDen();
                        lichTrinhByGaDen.computeIfAbsent(gaDen, k -> new ArrayList<>()).add(lichTrinh);
                    }
                    result.put("gaDen", new ArrayList<>(lichTrinhByGaDen.values().stream()
                            .flatMap(List::stream)
                            .limit(100)
                            .toList()));

                    // Lưu toàn bộ danh sách (có giới hạn)
                    result.put("all", allLichTrinh.stream().limit(200).collect(Collectors.toList()));

                    // Preload thông tin toa tàu cho các lịch trình phổ biến (20 lịch trình đầu tiên)
                    for (int i = 0; i < Math.min(20, allLichTrinh.size()); i++) {
                        String maTau = allLichTrinh.get(i).getTau().getMaTau();
                        toaTauDAO.getToaTauByMaTau(maTau); // Kết quả sẽ được cache bởi ToaTauDoiVeDAO
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    cachedLichTrinh = get();
                    isPreloadingData = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    isPreloadingData = false;
                }
            }
        };

        preloadWorker.execute();
    }

    private void initializeUI() {
        // Panel chính chia làm hai phần
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);

        // Panel bên trái chứa thông tin và thao tác
        JPanel leftPanel = createLeftPanel();

        // Panel bên phải chứa lịch sử đổi vé
        JPanel rightPanel = createRightPanel();

        // Chia đôi màn hình
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(650);
        splitPane.setDividerSize(5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBackground(Color.WHITE);

        // Thêm splitPane vào panel chính
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Thêm thanh trạng thái
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Thêm tiêu đề
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(primaryColor);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("QUẢN LÝ ĐỔI VÉ TÀU HỎA", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Thêm ngày giờ hiện tại vào bên phải
        JLabel dateLabel = new JLabel();
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(Color.WHITE);

        // Cập nhật ngày giờ
        Timer timer = new Timer(1000, e -> {
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            dateLabel.setText(sdf.format(now));
        });
        timer.start();

        titlePanel.add(dateLabel, BorderLayout.EAST);

        return titlePanel;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel tìm kiếm vé
        JPanel searchPanel = createSearchPanel();
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // Panel thông tin vé
        JPanel infoPanel = createInfoPanel();
        leftPanel.add(infoPanel, BorderLayout.CENTER);

        // Panel nút thao tác
        JPanel buttonPanel = createButtonPanel();
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(15, 0));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        "Tìm Kiếm Vé",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14),
                        primaryColor
                ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        searchPanel.setBackground(Color.WHITE);

        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        searchInputPanel.setBackground(Color.WHITE);

        // Thêm icon vào label
        JLabel lblMaVe = new JLabel("Mã vé:");
        lblMaVe.setFont(new Font("Arial", Font.BOLD, 12));
        lblMaVe.setIcon(createTicketIcon(16, 16, primaryColor));
        searchInputPanel.add(lblMaVe);

        txtMaVe = new JTextField(15);
        txtMaVe.setFont(new Font("Arial", Font.PLAIN, 12));
        searchInputPanel.add(txtMaVe);

        btnTimVe = new JButton("Tìm Kiếm");
        btnTimVe.setFont(new Font("Arial", Font.BOLD, 12));
        btnTimVe.setBackground(primaryColor);
        btnTimVe.setForeground(Color.WHITE);
        btnTimVe.setOpaque(true);  // Đảm bảo màu nền được hiển thị
        btnTimVe.setBorderPainted(false);  // Không vẽ viền
        btnTimVe.setFocusPainted(false);
        btnTimVe.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTimVe.setIcon(createSearchIcon(16, 16, Color.WHITE));
        btnTimVe.addActionListener(e -> timVe());

        // Thêm hiệu ứng hover cho nút tìm kiếm
        btnTimVe.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTimVe.setBackground(primaryColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnTimVe.setBackground(primaryColor);
            }
        });

        searchInputPanel.add(btnTimVe);
        searchPanel.add(searchInputPanel, BorderLayout.CENTER);
        return searchPanel;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        "Thông Tin Vé",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14),
                        primaryColor
                ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        infoPanel.setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Sử dụng bold cho labels
        Font labelFont = new Font("Arial", Font.BOLD, 12);
        Font fieldFont = new Font("Arial", Font.PLAIN, 12);

        // Hàng 1: Tên khách hàng và Giấy tờ
        addFormRow(formPanel, gbc, 0, "Tên khách hàng:", "Giấy tờ:", labelFont);

        txtTenKhachHang = new JTextField(20);
        txtTenKhachHang.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        formPanel.add(txtTenKhachHang, gbc);

        txtGiayTo = new JTextField(15);
        txtGiayTo.setFont(fieldFont);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        formPanel.add(txtGiayTo, gbc);

        // Hàng 2: Ngày đi và Đối tượng
        addFormRow(formPanel, gbc, 1, "Ngày đi:", "Đối tượng:", labelFont);

        txtNgayDi = new JTextField(10);
        txtNgayDi.setFont(fieldFont);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(txtNgayDi, gbc);

        String[] doiTuong = {"Người lớn", "Trẻ em", "Người cao tuổi", "Sinh viên"};
        cboDoiTuong = new JComboBox<>(doiTuong);
        cboDoiTuong.setFont(fieldFont);
        cboDoiTuong.addActionListener(e -> capNhatGiaVe());
        gbc.gridx = 3;
        gbc.gridy = 1;
        formPanel.add(cboDoiTuong, gbc);

        // Hàng 3: Lịch trình
        addFormRow(formPanel, gbc, 2, "Lịch trình:", "", labelFont);

        JPanel pnlLichTrinh = new JPanel(new BorderLayout(5, 0));
        pnlLichTrinh.setOpaque(false);

        lblLichTrinh = new JLabel("Chưa chọn");
        lblLichTrinh.setFont(fieldFont);
        pnlLichTrinh.add(lblLichTrinh, BorderLayout.CENTER);


        // Tạo JButton tùy chỉnh cho lịch trình
        btnChonLichTrinh = new JButton("Chọn") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isEnabled()) {
                    if (getModel().isPressed()) {
                        g2.setColor(primaryColor.darker().darker());
                    } else if (getModel().isRollover()) {
                        g2.setColor(primaryColor.darker());
                    } else {
                        g2.setColor(primaryColor);
                    }
                } else {
                    g2.setColor(new Color(200, 200, 200)); // Màu khi nút bị vô hiệu hóa
                }

                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();

                super.paintComponent(g);
            }
        };

        btnChonLichTrinh.setFont(new Font("Arial", Font.PLAIN, 11));
        btnChonLichTrinh.setForeground(Color.WHITE);
        btnChonLichTrinh.setBorderPainted(false);
        btnChonLichTrinh.setContentAreaFilled(false);
        btnChonLichTrinh.setFocusPainted(false);
        btnChonLichTrinh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChonLichTrinh.setIcon(createCalendarIcon(12, 12, Color.WHITE));
        btnChonLichTrinh.addActionListener(e -> hienThiDialogChonLichTrinh());

        pnlLichTrinh.add(btnChonLichTrinh, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        formPanel.add(pnlLichTrinh, gbc);

        // Hàng 4: Chỗ ngồi
        addFormRow(formPanel, gbc, 3, "Chỗ ngồi:", "", labelFont);

        JPanel pnlChoNgoi = new JPanel(new BorderLayout(5, 0));
        pnlChoNgoi.setOpaque(false);

        lblChoNgoi = new JLabel("Chưa chọn");
        lblChoNgoi.setFont(fieldFont);
        pnlChoNgoi.add(lblChoNgoi, BorderLayout.CENTER);

        // Tạo JButton tùy chỉnh cho chỗ ngồi
        btnChonChoNgoi = new JButton("Chọn") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isEnabled()) {
                    if (getModel().isPressed()) {
                        g2.setColor(primaryColor.darker().darker());
                    } else if (getModel().isRollover()) {
                        g2.setColor(primaryColor.darker());
                    } else {
                        g2.setColor(primaryColor);
                    }
                } else {
                    g2.setColor(new Color(200, 200, 200)); // Màu khi nút bị vô hiệu hóa
                }

                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();

                super.paintComponent(g);
            }
        };

        btnChonChoNgoi.setFont(new Font("Arial", Font.PLAIN, 11));
        btnChonChoNgoi.setForeground(Color.WHITE);
        btnChonChoNgoi.setBorderPainted(false);
        btnChonChoNgoi.setContentAreaFilled(false);
        btnChonChoNgoi.setFocusPainted(false);
        btnChonChoNgoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChonChoNgoi.setIcon(createSeatIcon(12, 12, Color.WHITE));
        btnChonChoNgoi.addActionListener(e -> hienThiDialogChonChoNgoi());

        pnlChoNgoi.add(btnChonChoNgoi, BorderLayout.EAST);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        formPanel.add(pnlChoNgoi, gbc);

        // Hàng 5: Trạng thái và Giá vé
        addFormRow(formPanel, gbc, 4, "Trạng thái:", "Giá vé:", labelFont);

        lblTrangThai = new JLabel("---");
        lblTrangThai.setFont(new Font("Arial", Font.BOLD, 12));
        lblTrangThai.setForeground(warningColor);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        formPanel.add(lblTrangThai, gbc);

        lblGiaVe = new JLabel("0 VNĐ");
        lblGiaVe.setFont(new Font("Arial", Font.BOLD, 12));
        lblGiaVe.setForeground(successColor);
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        formPanel.add(lblGiaVe, gbc);

        infoPanel.add(formPanel, BorderLayout.CENTER);

        return infoPanel;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label1, String label2, Font font) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;

        JLabel lbl1 = new JLabel(label1);
        lbl1.setFont(font);
        panel.add(lbl1, gbc);

        if (!label2.isEmpty()) {
            gbc.gridx = 2;
            JLabel lbl2 = new JLabel(label2);
            lbl2.setFont(font);
            panel.add(lbl2, gbc);
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        btnDoiVe = new JButton("Đổi Vé");
        styleButton(btnDoiVe, primaryColor, Color.WHITE, createExchangeIcon(16, 16, Color.WHITE));
        btnDoiVe.addActionListener(e -> doiVe());

        btnLamMoi = new JButton("Làm Mới");
        styleButton(btnLamMoi, grayColor, Color.WHITE, createRefreshIcon(16, 16, Color.WHITE));
        btnLamMoi.addActionListener(e -> lamMoi());

        JButton btnThoat = new JButton("Thoát");
        styleButton(btnThoat, dangerColor, Color.WHITE, createExitIcon(16, 16, Color.WHITE));
        btnThoat.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                    this, "Bạn có chắc chắn muốn thoát khỏi chức năng này?",
                    "Xác nhận thoát", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                // Quay lại màn hình chính
                Container parent = this.getParent();
                if (parent != null) {
                    ((CardLayout) parent.getLayout()).show(parent, "Trang chủ");
                }
            }
        });

        // Vô hiệu hóa các trường thông tin và nút ban đầu
        setInputFieldsEnabled(false);
        btnDoiVe.setEnabled(false);
        btnChonLichTrinh.setEnabled(false);
        btnChonChoNgoi.setEnabled(false);

        buttonPanel.add(btnDoiVe);
        buttonPanel.add(btnLamMoi);
        buttonPanel.add(btnThoat);

        return buttonPanel;
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor, Icon icon) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setOpaque(true);     // Đảm bảo màu nền được hiển thị
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setIcon(icon);

        // Hiệu ứng hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.darker());
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                }
            }
        });
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(primaryColor, 1),
                        "Lịch Sử Đổi Vé",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14),
                        primaryColor
                ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Tạo model cho bảng lịch sử
        String[] columnNames = {"Mã Vé", "Ngày Đổi", "Trạng Thái Cũ", "Trạng Thái Mới"};
        modelLichSu = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa ô
            }
        };

        tblLichSu = new JTable(modelLichSu);
        customizeTable(tblLichSu);

        JScrollPane scrollPane = new JScrollPane(tblLichSu);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Thêm panel tìm kiếm lịch sử
        JPanel searchHistoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchHistoryPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 12));
        lblSearch.setIcon(createSearchIcon(14, 14, primaryColor));
        searchHistoryPanel.add(lblSearch);

        JTextField txtSearch = new JTextField(15);
        searchHistoryPanel.add(txtSearch);

        // Tạo JButton tùy chỉnh cho tìm kiếm lịch sử
        JButton btnSearch = new JButton("Tìm") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isEnabled()) {
                    if (getModel().isPressed()) {
                        g2.setColor(primaryColor.darker().darker());
                    } else if (getModel().isRollover()) {
                        g2.setColor(primaryColor.darker());
                    } else {
                        g2.setColor(primaryColor);
                    }
                } else {
                    g2.setColor(new Color(200, 200, 200)); // Màu khi nút bị vô hiệu hóa
                }

                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();

                super.paintComponent(g);
            }
        };

        btnSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBorderPainted(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.setIcon(createSearchIcon(12, 12, Color.WHITE));

        // Thêm hiệu ứng hover
        btnSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnSearch.isEnabled()) {
                    btnSearch.repaint(); // Trigger repaint để hiển thị màu hover
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btnSearch.isEnabled()) {
                    btnSearch.repaint(); // Trigger repaint để trở về màu bình thường
                }
            }
        });

        btnSearch.addActionListener(e -> {
            // TODO: Thực hiện tìm kiếm trong lịch sử
        });

        searchHistoryPanel.add(btnSearch);

        rightPanel.add(searchHistoryPanel, BorderLayout.NORTH);

        return rightPanel;
    }

    private void customizeTable(JTable table) {
        // Thiết lập font và màu sắc cho bảng
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setGridColor(new Color(240, 240, 240));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);

        // Tùy chỉnh header của bảng - sửa lỗi header không hiển thị
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(primaryColor);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(primaryColor);
                label.setForeground(Color.WHITE);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                label.setFont(new Font("Arial", Font.BOLD, 12));
                return label;
            }
        });

        // Đảm bảo JTable sử dụng header đã được tùy chỉnh
        table.setTableHeader(header);

        // Căn giữa nội dung các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Đặt độ rộng cho các cột
        if (table.getColumnCount() >= 4) {
            table.getColumnModel().getColumn(0).setPreferredWidth(80);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(3).setPreferredWidth(100);
        }
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.setBackground(lightBackground);

        lblStatus = new JLabel(READY_TEXT);
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        lblStatus.setIcon(createInfoIcon(16, 16, primaryColor));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(150, 20));

        statusPanel.add(lblStatus, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);

        return statusPanel;
    }

    private void updateStatus(String message, boolean isLoading) {
        SwingUtilities.invokeLater(() -> {
            if (lblStatus != null) {
                lblStatus.setText(message);

                // Cập nhật màu sắc và icon cho message
                if (message.equals(LOADING_TEXT)) {
                    lblStatus.setForeground(primaryColor); // Blue
                    lblStatus.setIcon(createLoadingIcon(16, 16, primaryColor));
                } else if (message.equals(ERROR_TEXT)) {
                    lblStatus.setForeground(dangerColor); // Red
                    lblStatus.setIcon(createErrorIcon(16, 16, dangerColor));
                } else if (message.equals(SUCCESS_TEXT)) {
                    lblStatus.setForeground(successColor); // Green
                    lblStatus.setIcon(createSuccessIcon(16, 16, successColor));
                } else {
                    lblStatus.setForeground(darkTextColor); // Default
                    lblStatus.setIcon(createInfoIcon(16, 16, primaryColor));
                }
            }

            // Cập nhật progress bar
            if (progressBar != null) {
                progressBar.setVisible(isLoading);
                progressBar.setIndeterminate(isLoading);
            }
        });
    }

    private void timVe() {
        String maVe = txtMaVe.getText().trim();
        if (maVe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã vé!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            updateStatus(LOADING_TEXT, true);

            SwingWorker<VeTau, Void> worker = new SwingWorker<>() {
                @Override
                protected VeTau doInBackground() throws Exception {
                    return doiVeDAO.getVeTau(maVe);
                }

                @Override
                protected void done() {
                    try {
                        veTauHienTai = get();
                        if (veTauHienTai == null) {
                            JOptionPane.showMessageDialog(DoiVePanel.this,
                                    "Không tìm thấy vé với mã: " + maVe,
                                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                            lamMoi();
                        } else {
                            hienThiThongTinVe();

                            // Kiểm tra xem có thể đổi vé không
                            boolean coTheDoiVe = (veTauHienTai.getTrangThai() == TrangThaiVeTau.DA_THANH_TOAN);
                            setInputFieldsEnabled(coTheDoiVe);
                            btnDoiVe.setEnabled(coTheDoiVe);
                            btnChonLichTrinh.setEnabled(coTheDoiVe);
                            btnChonChoNgoi.setEnabled(coTheDoiVe);

                            if (!coTheDoiVe) {
                                JOptionPane.showMessageDialog(DoiVePanel.this,
                                        "Vé này có trạng thái '" + veTauHienTai.getTrangThai() +
                                                "'. Chỉ vé ở trạng thái 'ĐÃ THANH TOÁN' mới có thể đổi.",
                                        "Không thể đổi vé", JOptionPane.WARNING_MESSAGE);
                            }

                            updateStatus(READY_TEXT, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(DoiVePanel.this,
                                "Lỗi khi truy vấn dữ liệu: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        updateStatus(ERROR_TEXT, false);
                    }
                }
            };

            worker.execute();
        } catch (Exception e) {
            updateStatus(ERROR_TEXT, false);
            JOptionPane.showMessageDialog(this, "Lỗi khi truy vấn dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void hienThiThongTinVe() {
        if (veTauHienTai == null) return;

        txtTenKhachHang.setText(veTauHienTai.getTenKhachHang());
        txtGiayTo.setText(veTauHienTai.getGiayTo());
        txtNgayDi.setText(veTauHienTai.getNgayDi().format(formatter));

        // Đặt đối tượng
        String doiTuong = veTauHienTai.getDoiTuong();
        for (int i = 0; i < cboDoiTuong.getItemCount(); i++) {
            if (cboDoiTuong.getItemAt(i).equals(doiTuong)) {
                cboDoiTuong.setSelectedIndex(i);
                break;
            }
        }

        // Hiển thị lịch trình
        if (veTauHienTai.getLichTrinhTau() != null) {
            lichTrinhDaChon = veTauHienTai.getLichTrinhTau();
            lblLichTrinh.setText(lichTrinhDaChon.getMaLich() + " - " +
                    lichTrinhDaChon.getTau().getTuyenTau().getGaDi() +
                    " → " +
                    lichTrinhDaChon.getTau().getTuyenTau().getGaDen() +
                    " (" + lichTrinhDaChon.getGioDi() + ")");
        }

        // Hiển thị chỗ ngồi
        if (veTauHienTai.getChoNgoi() != null) {
            choNgoiDaChon = veTauHienTai.getChoNgoi();
            lblChoNgoi.setText(choNgoiDaChon.getTenCho() + " - " +
                    (choNgoiDaChon.getLoaiCho() != null ? choNgoiDaChon.getLoaiCho().getTenLoai() : ""));
        }

        // Hiển thị khuyến mãi
        if (veTauHienTai.getKhuyenMai() != null) {
            khuyenMaiDaChon = veTauHienTai.getKhuyenMai();
        }

        // Hiển thị trạng thái và giá vé với màu sắc khác nhau
        lblTrangThai.setText(veTauHienTai.getTrangThai().toString());
        setTrangThaiColor(lblTrangThai, veTauHienTai.getTrangThai());

        lblGiaVe.setText(currencyFormatter.format(veTauHienTai.getGiaVe()));
    }

    private void setTrangThaiColor(JLabel label, TrangThaiVeTau trangThai) {
        switch (trangThai) {
            case CHO_XAC_NHAN:
                label.setForeground(warningColor); // Cam
                label.setIcon(createPendingIcon(14, 14, warningColor));
                break;
            case DA_THANH_TOAN:
                label.setForeground(successColor); // Xanh lá
                label.setIcon(createCheckIcon(14, 14, successColor));
                break;
            case DA_TRA:
                label.setForeground(dangerColor); // Đỏ
                label.setIcon(createCancelIcon(14, 14, dangerColor));
                break;
            case DA_DOI:
                label.setForeground(grayColor); // Xám
                label.setIcon(createExchangeIcon(14, 14, grayColor));
                break;
            default:
                label.setForeground(darkTextColor);
                label.setIcon(null);
        }
    }

    private void doiVe() {
        if (veTauHienTai == null) return;

        // Kiểm tra dữ liệu đầu vào
        String tenKhachHang = txtTenKhachHang.getText().trim();
        if (tenKhachHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên khách hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTenKhachHang.requestFocus();
            return;
        }

        String giayTo = txtGiayTo.getText().trim();
        if (giayTo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập giấy tờ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtGiayTo.requestFocus();
            return;
        }

        String ngayDiStr = txtNgayDi.getText().trim();
        LocalDate ngayDi;
        try {
            ngayDi = LocalDate.parse(ngayDiStr, formatter);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Ngày đi không hợp lệ. Vui lòng nhập theo định dạng dd/MM/yyyy!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtNgayDi.requestFocus();
            return;
        }

        // Kiểm tra xem đã chọn lịch trình và chỗ ngồi chưa
        if (lichTrinhDaChon == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn lịch trình tàu!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (choNgoiDaChon == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn chỗ ngồi!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            updateStatus(LOADING_TEXT, true);

            // Lưu trữ trạng thái cũ để hiển thị trong lịch sử
            final TrangThaiVeTau trangThaiCu = veTauHienTai.getTrangThai();

            // Cập nhật thông tin vé
            veTauHienTai.setTenKhachHang(tenKhachHang);
            veTauHienTai.setGiayTo(giayTo);
            veTauHienTai.setNgayDi(ngayDi);
            veTauHienTai.setDoiTuong(Objects.requireNonNull(cboDoiTuong.getSelectedItem()).toString());
            veTauHienTai.setLichTrinhTau(lichTrinhDaChon);
            veTauHienTai.setChoNgoi(choNgoiDaChon);
            veTauHienTai.setKhuyenMai(khuyenMaiDaChon);

            // Tính lại giá vé
            double giaVe = tinhGiaVe(choNgoiDaChon, khuyenMaiDaChon, Objects.requireNonNull(cboDoiTuong.getSelectedItem()).toString());
            veTauHienTai.setGiaVe(giaVe);

            // Đổi trạng thái vé thành CHO_XAC_NHAN
            veTauHienTai.setTrangThai(TrangThaiVeTau.CHO_XAC_NHAN);

            // Gọi API để cập nhật vé
            boolean success = doiVeDAO.doiVe(veTauHienTai);

            if (success) {
                updateLichSuAndShowSuccess(trangThaiCu);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Đổi vé không thành công!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                updateStatus(ERROR_TEXT, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi thực hiện đổi vé: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            updateStatus(ERROR_TEXT, false);
        }
    }

    private void hienThiDialogChonLichTrinh() {
        try {
            // Hiển thị dialog chọn lịch trình
            LichTrinhSelectorDialog dialog = new LichTrinhSelectorDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    lichTrinhTauDAO,
                    toaTauDAO,
                    choNgoiDAO,
                    this::xuLyLichTrinhDaChon
            );

            // Hiển thị dialog
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể hiển thị giao diện chọn lịch trình: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyLichTrinhDaChon(LichTrinhTau lichTrinh) {
        if (lichTrinh != null) {
            lichTrinhDaChon = lichTrinh;
            lblLichTrinh.setText(lichTrinh.getMaLich() + " - " +
                    lichTrinh.getTau().getTuyenTau().getGaDi() +
                    " → " +
                    lichTrinh.getTau().getTuyenTau().getGaDen() +
                    " (" + lichTrinh.getGioDi() + ")");

            // Reset chỗ ngồi vì đã chọn lịch trình mới
            choNgoiDaChon = null;
            lblChoNgoi.setText("Chưa chọn");

            capNhatGiaVe();
        }
    }

    private void hienThiDialogChonChoNgoi() {
        try {
            if (lichTrinhDaChon == null) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn lịch trình trước khi chọn chỗ ngồi!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Hiển thị dialog chọn chỗ ngồi
            ChoNgoiSelectorDialog dialog = new ChoNgoiSelectorDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    lichTrinhDaChon,
                    choNgoiDAO,
                    toaTauDAO,
                    this::xuLyChoNgoiDaChon
            );
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể hiển thị giao diện chọn chỗ ngồi: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuLyChoNgoiDaChon(ChoNgoi choNgoi) {
        if (choNgoi != null) {
            choNgoiDaChon = choNgoi;
            lblChoNgoi.setText(choNgoi.getTenCho() + " - " +
                    (choNgoi.getLoaiCho() != null ? choNgoi.getLoaiCho().getTenLoai() : ""));

            capNhatGiaVe();
        }
    }

    private void capNhatGiaVe() {
        if (choNgoiDaChon != null) {
            String doiTuong = Objects.requireNonNull(cboDoiTuong.getSelectedItem()).toString();
            double giaVe = tinhGiaVe(choNgoiDaChon, khuyenMaiDaChon, doiTuong);
            lblGiaVe.setText(currencyFormatter.format(giaVe));
        }
    }

    /**
     * Tính lại giá vé khi đổi vé
     *
     * @param choNgoi   Chỗ ngồi được chọn
     * @param khuyenMai Khuyến mãi áp dụng (có thể null)
     * @param doiTuong  Đối tượng khách hàng (để tính giảm giá)
     * @return Giá vé sau khi tính toán
     */
    private double tinhGiaVe(ChoNgoi choNgoi, KhuyenMai khuyenMai, String doiTuong) {
        if (choNgoi == null) {
            return 0;
        }

        // Lấy giá tiền trực tiếp từ chỗ ngồi
        double giaVe = choNgoi.getGiaTien();

        // Áp dụng khuyến mãi nếu có
        if (khuyenMai != null) {
            giaVe *= (1 - khuyenMai.getChietKhau());
        }

        // Áp dụng chiết khấu theo đối tượng
        switch (doiTuong) {
            case "Trẻ em":
                giaVe *= 0.5; // Giảm 50% cho trẻ em
                break;
            case "Người cao tuổi":
                giaVe *= 0.7; // Giảm 30% cho người cao tuổi
                break;
            case "Sinh viên":
                giaVe *= 0.8; // Giảm 20% cho sinh viên
                break;
            default:
                // Không giảm cho đối tượng bình thường
                break;
        }

        return giaVe;
    }

    private void updateLichSuAndShowSuccess(TrangThaiVeTau trangThaiCu) {
        // Thêm vào lịch sử đổi vé
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String ngayGio = sdf.format(new Date());
        modelLichSu.addRow(new Object[]{
                veTauHienTai.getMaVe(),
                ngayGio,
                trangThaiCu,
                veTauHienTai.getTrangThai()
        });

        // Hiển thị chi tiết vé đã đổi
        String thongTinVe = "Thông tin vé đã đổi:\n\n" +
                "- Mã vé: " + veTauHienTai.getMaVe() + "\n" +
                "- Tên khách hàng: " + veTauHienTai.getTenKhachHang() + "\n" +
                "- Giấy tờ: " + veTauHienTai.getGiayTo() + "\n\n" +
                "- Lịch trình: " + veTauHienTai.getLichTrinhTau().getMaLich() + "\n" +
                "- Ngày đi: " + veTauHienTai.getNgayDi() + "\n" +
                "- Giờ đi: " + veTauHienTai.getLichTrinhTau().getGioDi() + "\n" +
                "- Tuyến: " + veTauHienTai.getLichTrinhTau().getTau().getTuyenTau().getGaDi() +
                " → " + veTauHienTai.getLichTrinhTau().getTau().getTuyenTau().getGaDen() + "\n\n" +
                "- Chỗ ngồi: " + veTauHienTai.getChoNgoi().getTenCho() + "\n" +
                "- Loại chỗ: " + veTauHienTai.getChoNgoi().getLoaiCho().getTenLoai() + "\n" +
                "- Đối tượng: " + veTauHienTai.getDoiTuong() + "\n" +
                "- Giá vé: " + currencyFormatter.format(veTauHienTai.getGiaVe()) + "\n\n" +
                "- Trạng thái: " + veTauHienTai.getTrangThai().getValue() + "\n\n" +
                "Vui lòng thanh toán để hoàn tất đổi vé.";

        // Tạo dialog hiển thị thông tin vé đẹp hơn
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Đổi vé thành công", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);

        JPanel pnlContent = new JPanel(new BorderLayout(10, 10));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tạo icon thành công
        JLabel lblIcon = new JLabel(createSuccessTickIcon(64, 64, successColor));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        pnlContent.add(lblIcon, BorderLayout.NORTH);

        // Tạo panel thông tin vé
        JTextArea txtThongTin = new JTextArea(thongTinVe);
        txtThongTin.setEditable(false);
        txtThongTin.setFont(new Font("Arial", Font.PLAIN, 14));
        txtThongTin.setBackground(new Color(250, 250, 250));
        txtThongTin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(txtThongTin);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        pnlContent.add(scrollPane, BorderLayout.CENTER);

        // Tạo panel nút bấm
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setBackground(Color.WHITE); // Đặt màu nền cho panel

// Tạo button với class Anonymous để ghi đè phương thức vẽ
        JButton btnOK = new JButton("Xác nhận") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vẽ nền button với màu primaryColor
                g2.setColor(getModel().isPressed() ? primaryColor.darker().darker() :
                        getModel().isRollover() ? primaryColor.darker() : primaryColor);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();

                // Vẽ text và icon
                super.paintComponent(g);
            }
        };

// Cấu hình button
        btnOK.setForeground(Color.WHITE);
        btnOK.setFont(new Font("Arial", Font.BOLD, 12));
        btnOK.setBorderPainted(false);     // Quan trọng: Tắt việc vẽ viền
        btnOK.setContentAreaFilled(false); // Quan trọng: Tắt việc fill nội dung mặc định
        btnOK.setFocusPainted(false);      // Tắt hiệu ứng focus
        btnOK.setIcon(createCheckIcon(16, 16, Color.WHITE));
        btnOK.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOK.setPreferredSize(new Dimension(120, 30)); // Đặt kích thước cố định

// Thêm hiệu ứng hover
        btnOK.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnOK.repaint(); // Kích hoạt vẽ lại khi chuột vào
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnOK.repaint(); // Kích hoạt vẽ lại khi chuột ra
            }
        });

// Thêm hành động
        btnOK.addActionListener(e -> dialog.dispose());

// Thêm vào panel và content
        pnlButtons.add(btnOK);
        pnlContent.add(pnlButtons, BorderLayout.SOUTH);

        dialog.add(pnlContent);
        dialog.setVisible(true);

        updateStatus(SUCCESS_TEXT, false);
        lamMoi();
    }

    private void lamMoi() {
        txtMaVe.setText("");
        txtTenKhachHang.setText("");
        txtGiayTo.setText("");
        txtNgayDi.setText("");
        cboDoiTuong.setSelectedIndex(0);

        lblLichTrinh.setText("Chưa chọn");
        lblChoNgoi.setText("Chưa chọn");

        lblTrangThai.setText("---");
        lblTrangThai.setForeground(Color.BLACK);
        lblGiaVe.setText("0 VNĐ");

        veTauHienTai = null;
        lichTrinhDaChon = null;
        choNgoiDaChon = null;
        khuyenMaiDaChon = null;

        setInputFieldsEnabled(false);
        btnDoiVe.setEnabled(false);
        btnChonLichTrinh.setEnabled(false);
        btnChonChoNgoi.setEnabled(false);
        updateStatus(READY_TEXT, false);
    }

    private void setInputFieldsEnabled(boolean enabled) {
        txtTenKhachHang.setEnabled(enabled);
        txtGiayTo.setEnabled(enabled);
        txtNgayDi.setEnabled(enabled);
        cboDoiTuong.setEnabled(enabled);
        btnChonLichTrinh.setEnabled(enabled);
        btnChonChoNgoi.setEnabled(enabled);
    }

    // Giải phóng tài nguyên
    public void shutdown() {
        // Đóng kết nối hoặc giải phóng tài nguyên nếu cần
    }

    // ===== ICON CREATION METHODS =====

    /**
     * Tạo icon vé
     */
    private ImageIcon createTicketIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ hình chữ nhật cho vé
        g2.fillRoundRect(2, 4, width - 4, height - 8, 5, 5);

        // Vẽ đường đứt quãng cho vé
        g2.setColor(new Color(255, 255, 255, 180));
        float[] dash = {2f, 2f};
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10, dash, 0));
        g2.drawLine(2, height / 2, width - 2, height / 2);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon tìm kiếm
     */
    private ImageIcon createSearchIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ hình tròn
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(2, 2, width - 8, height - 8);

        // Vẽ cán của kính lúp
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(width - 4, height - 4, width - 7, height - 7);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon đổi
     */
    private ImageIcon createExchangeIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ mũi tên đi lên
        int arrowWidth = width / 3;
        g2.setStroke(new BasicStroke(1.5f));

        // Mũi tên lên
        g2.drawLine(arrowWidth, height - 4, arrowWidth, 4);
        g2.drawLine(arrowWidth, 4, arrowWidth - 3, 7);
        g2.drawLine(arrowWidth, 4, arrowWidth + 3, 7);

        // Mũi tên xuống
        g2.drawLine(width - arrowWidth, 4, width - arrowWidth, height - 4);
        g2.drawLine(width - arrowWidth, height - 4, width - arrowWidth - 3, height - 7);
        g2.drawLine(width - arrowWidth, height - 4, width - arrowWidth + 3, height - 7);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon làm mới
     */
    private ImageIcon createRefreshIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ hình tròn cho nút refresh
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(3, 3, width - 6, height - 6, 45, 270);

        // Vẽ mũi tên
        g2.fillPolygon(
                new int[]{width - 3, width - 7, width},
                new int[]{3, 7, 7},
                3
        );

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon thoát
     */
    private ImageIcon createExitIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ hình cửa
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(2, 2, width - 8, height - 4);

        // Vẽ mũi tên ra ngoài
        g2.drawLine(width - 4, height / 2, width - 10, height / 2);
        g2.drawLine(width - 7, height / 2 - 3, width - 4, height / 2);
        g2.drawLine(width - 7, height / 2 + 3, width - 4, height / 2);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon thành công
     */
    private ImageIcon createSuccessIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ dấu tích
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(width / 4, height / 2, width / 2 - 1, height - height / 4);
        g2.drawLine(width / 2 - 1, height - height / 4, width - width / 4, height / 4);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon thành công với nền tròn
     */
    private ImageIcon createSuccessTickIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ hình tròn nền
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        g2.fillOval(0, 0, width, height);

        // Vẽ viền tròn
        g2.setColor(color);
        g2.setStroke(new BasicStroke(width / 16f));
        g2.drawOval(width / 10, height / 10, width - width / 5, height - height / 5);

        // Vẽ dấu tích
        g2.setColor(color);
        g2.setStroke(new BasicStroke(width / 12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int x1 = width / 4;
        int y1 = height / 2;
        int x2 = width / 2 - width / 12;
        int y2 = height - height / 3;
        int x3 = width - width / 3;
        int y3 = height / 3;
        g2.drawLine(x1, y1, x2, y2);
        g2.drawLine(x2, y2, x3, y3);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon loading
     */
    private ImageIcon createLoadingIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int dotSize = width / 6;
        int margin = width / 12;

        // Vẽ ba chấm với độ trong suốt khác nhau
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 255));
        g2.fillOval(width / 2 - dotSize / 2, height / 2 - dotSize / 2, dotSize, dotSize);

        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        g2.fillOval(width / 2 - dotSize / 2 - dotSize - margin, height / 2 - dotSize / 2, dotSize, dotSize);

        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g2.fillOval(width / 2 - dotSize / 2 + dotSize + margin, height / 2 - dotSize / 2, dotSize, dotSize);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon lỗi
     */
    private ImageIcon createErrorIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ dấu X
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(4, 4, width - 4, height - 4);
        g2.drawLine(width - 4, 4, 4, height - 4);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon thông tin
     */
    private ImageIcon createInfoIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);

        // Vẽ hình tròn viền
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(2, 2, width - 4, height - 4);

        // Vẽ chữ i
        g2.setFont(new Font("SansSerif", Font.BOLD, height - 6));
        FontMetrics fm = g2.getFontMetrics();
        int x = (width - fm.charWidth('i')) / 2;
        int y = (height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString("i", x, y);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon dấu tích
     */
    private ImageIcon createCheckIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ dấu tích
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int x1 = width / 4;
        int y1 = height / 2;
        int x2 = width / 2 - 1;
        int y2 = height - height / 4;
        int x3 = width - width / 4;
        int y3 = height / 4;
        g2.drawLine(x1, y1, x2, y2);
        g2.drawLine(x2, y2, x3, y3);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon hủy (X)
     */
    private ImageIcon createCancelIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ dấu X
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(4, 4, width - 4, height - 4);
        g2.drawLine(width - 4, 4, 4, height - 4);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon đang chờ
     */
    private ImageIcon createPendingIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ hình tròn đồng hồ
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(2, 2, width - 4, height - 4);

        // Vẽ kim đồng hồ
        g2.drawLine(width / 2, height / 2, width / 2, 4);
        g2.drawLine(width / 2, height / 2, width - 4, height / 2);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon lịch (cho nút chọn lịch trình)
     */
    private ImageIcon createCalendarIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ hình chữ nhật calendar
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(2, 4, width - 4, height - 6);

        // Vẽ phần trên của calendar
        g2.drawLine(width / 4, 2, width / 4, 6);
        g2.drawLine(width * 3 / 4, 2, width * 3 / 4, 6);

        // Vẽ các dòng bên trong
        g2.drawLine(2, height / 3 + 2, width - 2, height / 3 + 2);

        // Vẽ dấu X đánh dấu ngày
        int centerX = width / 2;
        int centerY = (height / 3 + 2 + height) / 2;
        g2.drawLine(centerX - 2, centerY - 2, centerX + 2, centerY + 2);
        g2.drawLine(centerX + 2, centerY - 2, centerX - 2, centerY + 2);

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Tạo icon ghế (cho nút chọn chỗ ngồi)
     */
    private ImageIcon createSeatIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ hình ghế ngồi
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1f));

        // Phần ngồi của ghế
        g2.fillRect(2, height / 2, width - 4, height / 3);

        // Phần lưng ghế
        g2.fillRect(4, 3, width - 8, height / 2);

        // Phần chân ghế
        g2.drawLine(4, height - 2, 6, height / 2 + height / 3);
        g2.drawLine(width - 4, height - 2, width - 6, height / 2 + height / 3);

        g2.dispose();
        return new ImageIcon(image);
    }
}