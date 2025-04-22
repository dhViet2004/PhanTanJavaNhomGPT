package GUI.component;

import dao.TuyenTauDAO;
import dao.impl.TuyenTauDAOImpl;
import jakarta.persistence.NoResultException;
import model.TuyenTau;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TraCuuTuyenPanel extends JPanel implements ActionListener {
    private static final String RMI_SERVER_IP = "192.168.2.21";
    private static final int RMI_SERVER_PORT = 9090;
    // Màu sắc chính
    private Color primaryColor = new Color(41, 128, 185); // Màu xanh dương
    private Color successColor = new Color(46, 204, 113); // Màu xanh lá
    private Color warningColor = new Color(243, 156, 18); // Màu vàng cam
    private Color dangerColor = new Color(231, 76, 60);   // Màu đỏ
    private Color grayColor = new Color(108, 117, 125);   // Màu xám
    private Color darkTextColor = new Color(52, 73, 94);  // Màu chữ tối
    private Color hoverColor = new Color(66, 139, 202);

    private Color lightBackground = new Color(240, 240, 240); // Màu nền nhạt
    private Font labelFont = new Font("Segoe UI", Font.PLAIN, 12); // Font cho label
    private Dimension inputSize = new Dimension(200, 30); // Kích thước cho input
    private Font mediumFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font boldFont;
    private IconFactory iconFactory = new IconFactory();
    private final int BUTTON_ICON_SIZE = 18;
    private final int BUTTON_HEIGHT = 30;
    private final int ICON_SIZE = 16;

    // Các thành phần bên trái (Tìm kiếm)
    private JTextField txtMaTuyenNhanh;
    private JButton btnTimNhanh;

    private JTextField txtTenTuyenTK;
    private JTextField txtDiemDiTK, txtDiemDenTK;
    private JTextField txtGaDiTK, txtGaDenTK;
    private JButton btnTimKiem;

    // Các thành phần bên phải (Chi tiết)
    private JTextField txtMaTuyenCT, txtTenTuyenCT, txtDiemDiCT, txtDiemDenCT, txtGaDiCT, txtGaDenCT;
    private JTable tblKetQua;
    private DefaultTableModel tblModel;

    //dao
    TuyenTauDAO tuyenTauDAO;
    //


    public TraCuuTuyenPanel() throws RemoteException {
        tuyenTauDAO = new TuyenTauDAOImpl();
        setLayout(new BorderLayout());
        setBackground(lightBackground);

        // Tiêu đề trang
        JPanel pnlTitle = new JPanel(new BorderLayout());
        pnlTitle.setBackground(primaryColor);
        JLabel lblTitle = new JLabel("TRA CỨU TUYẾN TÀU");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnlTitle.add(lblTitle, BorderLayout.CENTER);

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



        pnlTitle.add(dateLabel, BorderLayout.EAST);
        add(pnlTitle, BorderLayout.NORTH);

        // Panel chính chứa hai phần trái và phải
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.3); // Phần tìm kiếm nhỏ hơn
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerLocation(300); // Vị trí divider ban đầu
        add(mainSplitPane, BorderLayout.CENTER);

        // Panel bên trái (Tìm kiếm)
        JPanel pnlTimKiem = new JPanel();
        pnlTimKiem.setLayout(new BoxLayout(pnlTimKiem, BoxLayout.Y_AXIS));
        pnlTimKiem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlTimKiem.setBackground(lightBackground);

        Font italicFont = new Font("Segoe UI", Font.ITALIC, 15);
        boldFont = new Font("Segoe UI", Font.BOLD, 15);
        Border defaultBorder = new LineBorder(Color.GRAY); // Tạo một border mặc định

        // Tìm kiếm nhanh
        JPanel pnlTimNhanh = new JPanel(new GridBagLayout()); // Sử dụng GridBagLayout
        TitledBorder titledBorderTimNhanh = new TitledBorder(defaultBorder, "Tìm nhanh");
        titledBorderTimNhanh.setTitleFont(italicFont);
        pnlTimNhanh.setBorder(titledBorderTimNhanh);
        pnlTimNhanh.setBackground(lightBackground);
        GridBagConstraints gbcTimNhanh = new GridBagConstraints();
        gbcTimNhanh.insets = new Insets(5, 5, 5, 5);
        gbcTimNhanh.anchor = GridBagConstraints.WEST;

        JLabel lblMaTuyenNhanh = new JLabel("Mã tuyến:");
        lblMaTuyenNhanh.setFont(labelFont);
        gbcTimNhanh.gridx = 0;
        gbcTimNhanh.gridy = 0;
        pnlTimNhanh.add(lblMaTuyenNhanh, gbcTimNhanh);

        txtMaTuyenNhanh = new JTextField(15); // Đặt kích thước cột gợi ý
        txtMaTuyenNhanh.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtMaTuyenNhanh.setPreferredSize(inputSize);
        txtMaTuyenNhanh.setMaximumSize(inputSize);
        gbcTimNhanh.gridx = 1;
        gbcTimNhanh.gridy = 0;
        gbcTimNhanh.weightx = 1.0;
        gbcTimNhanh.fill = GridBagConstraints.HORIZONTAL;
        pnlTimNhanh.add(txtMaTuyenNhanh, gbcTimNhanh);

        btnTimNhanh = new RoundedButton("Lọc", iconFactory.getWhiteIcon("filter", BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        btnTimNhanh.setFont(new Font("Arial", Font.BOLD, 13));
        btnTimNhanh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTimNhanh.setIconTextGap(8);
        btnTimNhanh.setPreferredSize(new Dimension(100, BUTTON_HEIGHT));
        btnTimNhanh.setBackground(primaryColor);
        btnTimNhanh.setForeground(Color.WHITE);



        gbcTimNhanh.gridx = 2;
        gbcTimNhanh.gridy = 0;
        gbcTimNhanh.weightx = 0;
        gbcTimNhanh.fill = GridBagConstraints.NONE;
        pnlTimNhanh.add(btnTimNhanh, gbcTimNhanh);

        pnlTimKiem.add(pnlTimNhanh);
        pnlTimKiem.add(Box.createVerticalStrut(10)); // Khoảng cách

        // Tìm kiếm chi tiết
        JPanel pnlTimChiTiet = new JPanel(); // Tạo JPanel trước
        BoxLayout boxLayoutTimChiTiet = new BoxLayout(pnlTimChiTiet, BoxLayout.Y_AXIS); // Tạo BoxLayout
        pnlTimChiTiet.setLayout(boxLayoutTimChiTiet); // Thiết lập LayoutManager cho pnlTimChiTiet
        TitledBorder titledBorderTimChiTiet = new TitledBorder(defaultBorder, "Tìm kiếm chi tiết");
        titledBorderTimChiTiet.setTitleFont(italicFont);
        pnlTimChiTiet.setBorder(titledBorderTimChiTiet);
        pnlTimChiTiet.setBackground(lightBackground);

        // Nhóm tìm theo tuyến
        JPanel pnlTenTuyen = new JPanel(new GridBagLayout()); // Sử dụng GridBagLayout
        JLabel lblTenTuyenTK = new JLabel("Tên tuyến:");
        lblTenTuyenTK.setFont(labelFont);
        GridBagConstraints gbcTenTuyen = new GridBagConstraints();
        gbcTenTuyen.insets = new Insets(5, 5, 5, 5);
        gbcTenTuyen.anchor = GridBagConstraints.WEST;
        gbcTenTuyen.gridx = 0;
        gbcTenTuyen.gridy = 0;
        pnlTenTuyen.add(lblTenTuyenTK, gbcTenTuyen);

        txtTenTuyenTK = new JTextField(15); // Chỉnh kích thước input
        txtTenTuyenTK.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtTenTuyenTK.setPreferredSize(inputSize);
        txtTenTuyenTK.setMaximumSize(inputSize);
        gbcTenTuyen.gridx = 1;
        gbcTenTuyen.gridy = 0;
        gbcTenTuyen.weightx = 1.0;
        gbcTenTuyen.fill = GridBagConstraints.HORIZONTAL;
        pnlTenTuyen.add(txtTenTuyenTK, gbcTenTuyen);

        pnlTimChiTiet.add(pnlTenTuyen);
        pnlTimChiTiet.add(Box.createVerticalStrut(5));

        // Nhóm tìm theo địa điểm (giữ nguyên)
        JPanel grpDiaDiem = new JPanel(new GridBagLayout());
        grpDiaDiem.setBorder(BorderFactory.createTitledBorder(defaultBorder, "Địa điểm"));
        grpDiaDiem.setFont(mediumFont);
        GridBagConstraints gbcDiaDiem = new GridBagConstraints();
        gbcDiaDiem.insets = new Insets(5, 5, 5, 5);
        gbcDiaDiem.anchor = GridBagConstraints.WEST; // Căn trái các thành phần

        JLabel lblDiemDiTK = new JLabel("Điểm đi:");
        lblDiemDiTK.setFont(labelFont);
        txtDiemDiTK = new JTextField(15);
        txtDiemDiTK.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtDiemDiTK.setPreferredSize(inputSize);
        txtDiemDiTK.setMaximumSize(inputSize);
        gbcDiaDiem.gridx = 0;
        gbcDiaDiem.gridy = 0;
        grpDiaDiem.add(lblDiemDiTK, gbcDiaDiem);

        gbcDiaDiem.gridx = 1;
        gbcDiaDiem.weightx = 1.0; // Cho input có thể giãn nở
        gbcDiaDiem.fill = GridBagConstraints.HORIZONTAL;
        grpDiaDiem.add(txtDiemDiTK, gbcDiaDiem);

        JLabel lblDiemDenTK = new JLabel("Điểm đến:");
        lblDiemDenTK.setFont(labelFont);
        txtDiemDenTK = new JTextField(15);
        txtDiemDenTK.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtDiemDenTK.setPreferredSize(inputSize);
        txtDiemDenTK.setMaximumSize(inputSize);
        gbcDiaDiem.gridx = 0;
        gbcDiaDiem.gridy = 1;
        gbcDiaDiem.weightx = 0; // Reset weightx
        gbcDiaDiem.fill = GridBagConstraints.NONE; // Reset fill
        grpDiaDiem.add(lblDiemDenTK, gbcDiaDiem);

        gbcDiaDiem.gridx = 1;
        gbcDiaDiem.weightx = 1.0;
        gbcDiaDiem.fill = GridBagConstraints.HORIZONTAL;
        grpDiaDiem.add(txtDiemDenTK, gbcDiaDiem);

        pnlTimChiTiet.add(grpDiaDiem);
        pnlTimChiTiet.add(Box.createVerticalStrut(5));

        // Nhóm tìm theo ga (giữ nguyên)
        JPanel grpGa = new JPanel(new GridBagLayout());
        grpGa.setBorder(BorderFactory.createTitledBorder(defaultBorder, "Ga"));
        grpGa.setFont(mediumFont);
        GridBagConstraints gbcGa = new GridBagConstraints();
        gbcGa.insets = new Insets(5, 5, 5, 5);
        gbcGa.anchor = GridBagConstraints.WEST;

        JLabel lblGaDiTK = new JLabel("Ga đi:");
        lblGaDiTK.setFont(labelFont);
        txtGaDiTK = new JTextField(15);
        txtGaDiTK.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtGaDiTK.setPreferredSize(inputSize);
        txtGaDiTK.setMaximumSize(inputSize);
        gbcGa.gridx = 0;
        gbcGa.gridy = 0;
        grpGa.add(lblGaDiTK, gbcGa);

        gbcGa.gridx = 1;
        gbcGa.weightx = 1.0;
        gbcGa.fill = GridBagConstraints.HORIZONTAL;
        grpGa.add(txtGaDiTK, gbcGa);

        JLabel lblGaDenTK = new JLabel("Ga đến:");
        lblGaDenTK.setFont(labelFont);
        txtGaDenTK = new JTextField(15);
        txtGaDenTK.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtGaDenTK.setPreferredSize(inputSize);
        txtGaDenTK.setMaximumSize(inputSize);
        gbcGa.gridx = 0;
        gbcGa.gridy = 1;
        gbcGa.weightx = 0;
        gbcGa.fill = GridBagConstraints.NONE;
        grpGa.add(lblGaDenTK, gbcGa);

        gbcGa.gridx = 1;
        gbcGa.weightx = 1.0;
        gbcGa.fill = GridBagConstraints.HORIZONTAL;
        grpGa.add(txtGaDenTK, gbcGa);

        pnlTimChiTiet.add(grpGa);
        pnlTimChiTiet.add(Box.createVerticalStrut(10));

        // Nút tìm kiếm chung
        JPanel pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Căn phải nút
        // Để nút không dính vào bên phải khi kéo, không cần thay đổi layout của panel chứa nút,
        // mà BoxLayout của pnlTimKiem sẽ quản lý vị trí của nó.
//        btnTimKiem = new JButton("Tìm kiếm");
        // Button Tìm vé với góc bo tròn
        btnTimKiem = new RoundedButton("Tìm kiếm", iconFactory.getWhiteIcon("search", BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        btnTimKiem.setFont(new Font("Arial", Font.BOLD, 13));
        btnTimKiem.setBackground(primaryColor);
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTimKiem.setIconTextGap(8);
        btnTimKiem.setPreferredSize(new Dimension(150, BUTTON_HEIGHT));
        btnTimKiem.setBackground(primaryColor);
        btnTimKiem.setForeground(Color.WHITE);
        pnlButton.add(btnTimKiem);
        pnlTimChiTiet.add(pnlButton);
        pnlTimChiTiet.add(Box.createVerticalGlue()); // Để nút tìm kiếm ở cuối

        pnlTimKiem.add(pnlTimChiTiet);

        // Panel bên phải (Chi tiết tuyến tàu)
        JPanel pnlChiTiet = new JPanel(new GridBagLayout());
        TitledBorder titledBorderChiTiet = new TitledBorder(defaultBorder, "Chi tiết tuyến tàu");
        titledBorderChiTiet.setTitleFont(boldFont);
        pnlChiTiet.setBorder(titledBorderChiTiet);
        pnlChiTiet.setBackground(lightBackground);
        GridBagConstraints gbcChiTiet = new GridBagConstraints();
        gbcChiTiet.insets = new Insets(10, 10, 10, 10); // Tăng insets để tạo khoảng cách dọc và ngang
        gbcChiTiet.fill = GridBagConstraints.HORIZONTAL;
        gbcChiTiet.weightx = 1.0;

        txtMaTuyenCT = new JTextField(20); // Tăng kích thước input
        txtMaTuyenCT.setEditable(false);
        txtMaTuyenCT.setBackground(Color.WHITE);
        txtMaTuyenCT.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtMaTuyenCT.setPreferredSize(inputSize);
        txtMaTuyenCT.setMaximumSize(inputSize);
        txtTenTuyenCT = new JTextField(20); // Tăng kích thước input
        txtTenTuyenCT.setEditable(false);
        txtTenTuyenCT.setBackground(Color.WHITE);
        txtTenTuyenCT.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtTenTuyenCT.setPreferredSize(inputSize);
        txtTenTuyenCT.setMaximumSize(inputSize);
        txtDiemDiCT = new JTextField(20); // Tăng kích thước input
        txtDiemDiCT.setEditable(false);
        txtDiemDiCT.setBackground(Color.WHITE);
        txtDiemDiCT.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtDiemDiCT.setPreferredSize(inputSize);
        txtDiemDiCT.setMaximumSize(inputSize);
        txtDiemDenCT = new JTextField(20); // Tăng kích thước input
        txtDiemDenCT.setEditable(false);
        txtDiemDenCT.setBackground(Color.WHITE);
        txtDiemDenCT.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtDiemDenCT.setPreferredSize(inputSize);
        txtDiemDenCT.setMaximumSize(inputSize);
        txtGaDiCT = new JTextField(20); // Tăng kích thước input
        txtGaDiCT.setEditable(false);
        txtGaDiCT.setBackground(Color.WHITE);
        txtGaDiCT.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtGaDiCT.setPreferredSize(inputSize);
        txtGaDiCT.setMaximumSize(inputSize);
        txtGaDenCT = new JTextField(20); // Tăng kích thước input
        txtGaDenCT.setEditable(false);
        txtGaDenCT.setBackground(Color.WHITE);
        txtGaDenCT.setBorder(BorderFactory.createLineBorder(primaryColor)); // Set border màu primary
        txtGaDenCT.setPreferredSize(inputSize);
        txtGaDenCT.setMaximumSize(inputSize);

        // enable
        txtMaTuyenCT.setEditable(false);
        txtTenTuyenCT.setEditable(false);
        txtDiemDenCT.setEditable(false);
        txtDiemDiCT.setEditable(false);
        txtGaDenCT.setEditable(false);
        txtGaDiCT.setEditable(false);

        addLabelAndComponent(pnlChiTiet, "Mã tuyến:", txtMaTuyenCT, gbcChiTiet, 0,"train");
        addLabelAndComponent(pnlChiTiet, "Tên tuyến:", txtTenTuyenCT, gbcChiTiet, 1,"ticket");
        addLabelAndComponent(pnlChiTiet, "Điểm đi:", txtDiemDiCT, gbcChiTiet, 2,"detail");
        addLabelAndComponent(pnlChiTiet, "Điểm đến:", txtDiemDenCT, gbcChiTiet, 3,"detail");
        addLabelAndComponent(pnlChiTiet, "Ga đi:", txtGaDiCT, gbcChiTiet, 4,"print");
        addLabelAndComponent(pnlChiTiet, "Ga đến:", txtGaDenCT, gbcChiTiet, 5,"info");

        gbcChiTiet.gridx = 0;
        gbcChiTiet.gridy = 6;
        gbcChiTiet.gridwidth = 2;
        gbcChiTiet.weightx = 0;
        gbcChiTiet.fill = GridBagConstraints.NONE;
        gbcChiTiet.anchor = GridBagConstraints.LINE_END;

        // Thêm hai panel vào JSplitPane
        mainSplitPane.setLeftComponent(pnlTimKiem);
        mainSplitPane.setRightComponent(pnlChiTiet);

        // Bảng kết quả (ban đầu ẩn)
        tblModel = new DefaultTableModel(new String[]{"Mã tuyến", "Tên tuyến", "Điểm đi", "Điểm đến", "Ga đi", "Ga đến"}, 0);
        tblKetQua = new JTable(tblModel);
        JScrollPane scrollPane = new JScrollPane(tblKetQua);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kết quả tra cứu"));
        add(scrollPane, BorderLayout.SOUTH);
        scrollPane.setVisible(false); // Ẩn bảng kết quả ban đầu

        setPreferredSize(new Dimension(1000, 600));

        // sự kiện
        btnTimNhanh.addActionListener(this);
        btnTimKiem.addActionListener(this);
        connectToServer();

    }
    private boolean isactive = false;
    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_SERVER_IP, RMI_SERVER_PORT);
            tuyenTauDAO = (TuyenTauDAO) registry.lookup("tuyenTauDAO");
            isactive = true;
            // Thông báo kết nối thành công sau khi lookup thành công
//            JOptionPane.showMessageDialog(this,
//                    "Kết nối đến server RMI thành công!",
//                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);

        } catch (RemoteException | NotBoundException e) {
            // Không hiển thị thông báo lỗi kết nối ở đây
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối đến server RMI: " + e.getMessage(),
                    "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
            isactive = false;

            e.printStackTrace();
        }
    }
    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component,
                                      GridBagConstraints gbc, int y,String icontext) {
        gbc.gridx = 0;
        gbc.gridy = y;
//        JLabel tmp = new JLabel(labelText);
        JLabel lblMaVe = new JLabel(labelText, iconFactory.getIcon(icontext, ICON_SIZE, ICON_SIZE), JLabel.LEFT);
        lblMaVe.setFont(boldFont);
        panel.add(lblMaVe, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(component, gbc);
    }
    public void loadLenTXT(TuyenTau tuyenTau){
        txtMaTuyenCT.setText(tuyenTau.getMaTuyen());
        txtTenTuyenCT.setText(tuyenTau.getTenTuyen());
        txtDiemDenCT.setText(tuyenTau.getDiaDiemDen());
        txtDiemDiCT.setText(tuyenTau.getDiaDiemDi());
        txtGaDenCT.setText(tuyenTau.getGaDen());
        txtGaDiCT.setText(tuyenTau.getGaDi());
    }
    public void clearTimKiem(){
        txtMaTuyenNhanh.setText("");
        txtTenTuyenTK.setText("");
        txtDiemDenTK.setText("");
        txtDiemDiTK.setText("");
        txtGaDenTK.setText("");
        txtGaDiTK.setText("");
    }
    public void clear(){
        txtMaTuyenCT.setText("");
        txtTenTuyenCT.setText("");
        txtDiemDenCT.setText("");
        txtDiemDiCT.setText("");
        txtGaDenCT.setText("");
        txtGaDiCT.setText("");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(isactive){
            if (o == btnTimKiem) {
                if (!txtTenTuyenTK.getText().trim().isEmpty()) {
                    txtMaTuyenNhanh.setText("");
                    txtGaDiTK.setText("");
                    txtGaDenTK.setText("");
                    txtDiemDiTK.setText("");
                    txtDiemDenTK.setText("");
                    String tenTuyen = txtTenTuyenTK.getText().trim();
                    clear();
                    try {
                        TuyenTau tuyenTau = tuyenTauDAO.getTuyenTauByName(tenTuyen);
                        if (tuyenTau != null) {
                            loadLenTXT(tuyenTau);
                        } else {
                            JOptionPane.showMessageDialog(null, "Không tìm thấy tuyến tàu với tên: " + tenTuyen, "Lỗi tìm kiếm", JOptionPane.ERROR_MESSAGE);
                        }
                        clearTimKiem();
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                } else if (!txtGaDiTK.getText().trim().isEmpty() && !txtGaDenTK.getText().trim().isEmpty()) {
                    txtMaTuyenNhanh.setText("");
                    txtTenTuyenTK.setText("");
                    txtDiemDiTK.setText("");
                    txtDiemDenTK.setText("");
                    String gaDen = txtGaDenTK.getText().trim();
                    String gaDi = txtGaDiTK.getText().trim();
                    clear();
                    try {
                        TuyenTau tuyenTau = tuyenTauDAO.getListTuyenTauByGaDiGaDen(gaDi, gaDen);
                        if (tuyenTau != null) {
                            loadLenTXT(tuyenTau);
                            clearTimKiem();
                        } else {
                            JOptionPane.showMessageDialog(null, "Không tìm thấy tuyến tàu giữa ga đi: " + gaDi + " và ga đến: " + gaDen, "Lỗi tìm kiếm", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }catch (NoResultException ex){
                        throw new RuntimeException(ex);
                    }

                } else if (!txtDiemDiTK.getText().trim().isEmpty() && !txtDiemDenTK.getText().trim().isEmpty()) {
                    txtMaTuyenNhanh.setText("");
                    txtTenTuyenTK.setText("");
                    txtGaDiTK.setText("");
                    txtGaDenTK.setText("");
                    String diemDi = txtDiemDiTK.getText().trim();
                    String diemDen = txtDiemDenTK.getText().trim();
                    clear();
                    try {
                        TuyenTau tuyenTau = tuyenTauDAO.getListTuyenTauByDiemDiDiemDen(diemDi, diemDen);
                        if (tuyenTau != null) {
                            loadLenTXT(tuyenTau);
                        } else {
                            JOptionPane.showMessageDialog(null, "Không tìm thấy tuyến tàu với điểm đi: " + diemDi + " và điểm đến: " + diemDen, "Lỗi tìm kiếm", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            else if (o == btnTimNhanh) {
                String maTuyen = txtMaTuyenNhanh.getText();
                clear();
                try {
                    TuyenTau tuyenTau = tuyenTauDAO.getTuyenTauById(maTuyen);
                    if (tuyenTau != null) {
                        loadLenTXT(tuyenTau);
                    } else {
                        JOptionPane.showMessageDialog(null, "Không tìm thấy tuyến tàu với mã: " + maTuyen, "Lỗi tìm kiếm", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }else {
            JOptionPane.showMessageDialog(null, "Không thể kết nối dữ liệu, vui lòng kiểm tra kết nối: ", "Đã xãy ra lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

    }


    private class IconFactory {
        // Tạo và trả về icon theo yêu cầu với màu mặc định
        public Icon getIcon(String iconName, int width, int height) {
            return new VectorIcon(iconName, width, height);
        }

        // Tạo và trả về icon màu trắng (dùng cho các nút có nền màu)
        public Icon getWhiteIcon(String iconName, int width, int height) {
            return new VectorIcon(iconName, width, height, Color.WHITE);
        }

        // Class custom icon sử dụng vector graphics
        private class VectorIcon implements Icon {
            private final String iconName;
            private final int width;
            private final int height;
            private final Color forcedColor; // Màu bắt buộc (nếu có)

            public VectorIcon(String iconName, int width, int height) {
                this.iconName = iconName;
                this.width = width;
                this.height = height;
                this.forcedColor = null; // Không có màu bắt buộc
            }

            public VectorIcon(String iconName, int width, int height, Color forcedColor) {
                this.iconName = iconName;
                this.width = width;
                this.height = height;
                this.forcedColor = forcedColor; // Sử dụng màu được chỉ định
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);

                // Xác định màu biểu tượng
                Color iconColor;
                if (forcedColor != null) {
                    iconColor = forcedColor; // Sử dụng màu bắt buộc nếu có
                } else {
                    iconColor = c.isEnabled() ? new Color(41, 128, 185) : Color.GRAY;
                }
                g2.setColor(iconColor);

                // Scale icon to fit the specified width and height
                g2.scale(width / 24.0, height / 24.0);

                switch (iconName) {
                    case "train":
                        drawTrainIcon(g2, iconColor);
                        break;
                    case "detail":
                        drawDetailIcon(g2, iconColor);
                        break;
                    case "search":
                        drawSearchIcon(g2, iconColor);
                        break;
                    case "ticket":
                        drawTicketIcon(g2, iconColor);
                        break;
                    case "info":
                        drawInfoIcon(g2, iconColor);
                        break;
                    case "seat":
                        drawSeatIcon(g2, iconColor);
                        break;
                    case "user":
                        drawUserIcon(g2, iconColor);
                        break;
                    case "id-card":
                        drawIdCardIcon(g2, iconColor);
                        break;
                    case "calendar":
                        drawCalendarIcon(g2, iconColor);
                        break;
                    case "person":
                        drawPersonIcon(g2, iconColor);
                        break;
                    case "money":
                        drawMoneyIcon(g2, iconColor);
                        break;
                    case "status":
                        drawStatusIcon(g2, iconColor);
                        break;
                    case "print":
                        drawPrintIcon(g2, iconColor);
                        break;
                    case "time-search":
                        drawTimeSearchIcon(g2, iconColor);
                        break;
                    case "clock":
                        drawClockIcon(g2, iconColor);
                        break;
                    case "filter":
                        drawFilterIcon(g2, iconColor);
                        break;
                    case "search-detail":
                        drawSearchDetailIcon(g2, iconColor);
                        break;
                    case "quick-search":
                        drawQuickSearchIcon(g2, iconColor);
                        break;
                    case "qrcode":
                        drawQrCodeIcon(g2, iconColor);
                        break;
                    case "list":
                        drawListIcon(g2, iconColor);
                        break;
                    default:
                        drawDefaultIcon(g2, iconColor);
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return width;
            }

            @Override
            public int getIconHeight() {
                return height;
            }

            // Các phương thức vẽ icon cụ thể
            private void drawTrainIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Thân tàu
                g2.fillRoundRect(2, 8, 20, 12, 4, 4);

                // Đầu tàu
                g2.fillRect(18, 5, 4, 3);

                // Cửa sổ
                g2.setColor(Color.WHITE);
                g2.fillRect(5, 11, 3, 3);
                g2.fillRect(10, 11, 3, 3);
                g2.fillRect(15, 11, 3, 3);

                // Bánh xe
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval(4, 18, 4, 4);
                g2.fillOval(16, 18, 4, 4);
            }

            private void drawDetailIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Background
                g2.fillRoundRect(3, 3, 18, 18, 2, 2);

                // Lines
                g2.setColor(Color.WHITE);
                g2.fillRect(6, 7, 12, 1);
                g2.fillRect(6, 11, 12, 1);
                g2.fillRect(6, 15, 12, 1);
            }

            private void drawSearchIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Kính lúp
                g2.setStroke(new BasicStroke(2));
                g2.draw(new Ellipse2D.Double(4, 4, 12, 12));
                g2.draw(new Line2D.Double(14, 14, 20, 20));
            }

            private void drawTicketIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Vé
                g2.fillRoundRect(2, 6, 20, 12, 4, 4);

                // Đường kẻ đục lỗ
                g2.setColor(Color.WHITE);
                float[] dash = {2.0f, 2.0f};
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, dash, 0));
                g2.drawLine(7, 6, 7, 18);

                // Nội dung vé
                g2.fillRect(10, 10, 8, 1);
                g2.fillRect(10, 13, 8, 1);
            }

            private void drawInfoIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Biểu tượng i
                g2.fillOval(8, 4, 8, 8);
                g2.fillRoundRect(11, 14, 2, 6, 1, 1);

                // Chữ i
                g2.setColor(Color.WHITE);
                g2.fillOval(11, 6, 2, 2);
                g2.fillRoundRect(11, 10, 2, 2, 1, 1);
            }

            private void drawSeatIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Ghế
                g2.fillRoundRect(3, 7, 18, 15, 2, 2);

                // Lưng ghế
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(5, 9, 14, 8, 2, 2);

                // Chân ghế
                g2.setColor(iconColor);
                g2.fillRect(5, 19, 3, 3);
                g2.fillRect(16, 19, 3, 3);
            }

            private void drawUserIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Đầu
                g2.fillOval(8, 4, 8, 8);

                // Thân
                g2.fillOval(4, 14, 16, 8);
            }

            private void drawIdCardIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Card
                g2.fillRoundRect(2, 5, 20, 14, 2, 2);

                // Avatar
                g2.setColor(Color.WHITE);
                g2.fillOval(5, 8, 6, 6);

                // Thông tin
                g2.fillRect(13, 8, 6, 1);
                g2.fillRect(13, 11, 6, 1);
                g2.fillRect(13, 14, 6, 1);
            }

            private void drawCalendarIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Calendar body
                g2.fillRoundRect(3, 6, 18, 16, 2, 2);

                // Calendar top
                g2.fillRect(7, 3, 2, 4);
                g2.fillRect(15, 3, 2, 4);

                // Calendar lines
                g2.setColor(Color.WHITE);
                g2.fillRect(6, 10, 12, 1);
                g2.fillRect(6, 14, 12, 1);
                g2.fillRect(6, 18, 12, 1);
                g2.fillRect(10, 10, 1, 9);
                g2.fillRect(14, 10, 1, 9);
            }

            private void drawPersonIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Head
                g2.fillOval(9, 3, 6, 6);

                // Body
                g2.fillRoundRect(4, 11, 16, 10, 4, 4);
            }

            private void drawMoneyIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Coin
                g2.fillOval(4, 4, 16, 16);

                // $ Symbol
                g2.setColor(Color.WHITE);
                g2.fillRect(11, 8, 2, 8);
                g2.fillRect(9, 8, 6, 2);
                g2.fillRect(9, 14, 6, 2);
            }

            private void drawStatusIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Status circles
                g2.fillOval(3, 10, 4, 4);
                g2.fillOval(10, 10, 4, 4);
                g2.fillOval(17, 10, 4, 4);

                // Lines connecting
                g2.setStroke(new BasicStroke(1));
                g2.drawLine(7, 12, 10, 12);
                g2.drawLine(14, 12, 17, 12);
            }

            private void drawPrintIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Printer body
                g2.fillRect(3, 10, 18, 8);

                // Paper
                g2.setColor(Color.WHITE);
                g2.fillRect(7, 5, 10, 5);
                g2.fillRect(7, 18, 10, 5);

                // Detail
                g2.setColor(iconColor);
                g2.fillOval(16, 13, 2, 2);
            }

            private void drawTimeSearchIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Clock face
                g2.drawOval(4, 4, 16, 16);

                // Clock hands
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(12, 12, 12, 6);
                g2.drawLine(12, 12, 16, 12);

                // Dots
                g2.fillOval(12, 12, 1, 1);

                // Magnifying glass
                g2.drawOval(16, 2, 6, 6);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(20, 8, 22, 10);
            }

            private void drawClockIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Clock face
                g2.drawOval(2, 2, 20, 20);

                // Clock hands
                g2.drawLine(12, 12, 12, 4);
                g2.drawLine(12, 12, 18, 12);

                // Center dot
                g2.fillOval(11, 11, 2, 2);

                // Clock markers
                for (int i = 0; i < 12; i++) {
                    double angle = Math.toRadians(i * 30);
                    int x1 = (int) (12 + 9 * Math.sin(angle));
                    int y1 = (int) (12 - 9 * Math.cos(angle));
                    int x2 = (int) (12 + 10 * Math.sin(angle));
                    int y2 = (int) (12 - 10 * Math.cos(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }
            }

            private void drawFilterIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Filter shape - improved design
                g2.setStroke(new BasicStroke(1.5f));
                int[] xPoints = {2, 22, 15, 15, 9, 9};
                int[] yPoints = {4, 4, 12, 20, 20, 12};
                g2.fillPolygon(xPoints, yPoints, 6);

                // Filter lines
                g2.setColor(Color.WHITE);
                g2.drawLine(6, 8, 18, 8);
                g2.drawLine(10, 16, 14, 16);
            }

            private void drawSearchDetailIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Magnifying glass with document
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Ellipse2D.Double(4, 4, 10, 10));
                g2.draw(new Line2D.Double(12, 12, 18, 18));

                // Document outline
                g2.drawRoundRect(12, 3, 9, 12, 2, 2);

                // Document lines
                g2.drawLine(14, 6, 19, 6);
                g2.drawLine(14, 9, 19, 9);
                g2.drawLine(14, 12, 17, 12);
            }

            private void drawQuickSearchIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Magnifying glass
                g2.setStroke(new BasicStroke(2));
                g2.draw(new Ellipse2D.Double(4, 4, 12, 12));
                g2.draw(new Line2D.Double(14, 14, 20, 20));

                // Flash (lightning bolt)
                g2.fillPolygon(
                        new int[]{10, 8, 12, 10, 14, 12},
                        new int[]{4, 10, 10, 14, 7, 7}, 6
                );
            }

            private void drawQrCodeIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // QR code frame
                g2.fillRect(4, 4, 16, 16);

                // QR code pattern
                g2.setColor(Color.WHITE);
                // Upper left corner pattern
                g2.fillRect(6, 6, 4, 4);
                g2.setColor(iconColor);
                g2.fillRect(7, 7, 2, 2);

                // Upper right corner pattern
                g2.setColor(Color.WHITE);
                g2.fillRect(14, 6, 4, 4);
                g2.setColor(iconColor);
                g2.fillRect(15, 7, 2, 2);

                // Bottom left corner pattern
                g2.setColor(Color.WHITE);
                g2.fillRect(6, 14, 4, 4);
                g2.setColor(iconColor);
                g2.fillRect(7, 15, 2, 2);

                // Random QR code pattern
                g2.setColor(Color.WHITE);
                g2.fillRect(12, 12, 2, 2);
                g2.fillRect(15, 12, 2, 2);
                g2.fillRect(12, 15, 2, 2);
            }

            private void drawListIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // List lines
                g2.setStroke(new BasicStroke(2));
                for (int i = 0; i < 4; i++) {
                    int y = 6 + i * 4;
                    // Bullet point
                    g2.fillOval(4, y, 2, 2);
                    // Line
                    g2.drawLine(8, y + 1, 20, y + 1);
                }
            }

            private void drawDefaultIcon(Graphics2D g2, Color iconColor) {
                g2.setColor(iconColor);

                // Empty box with question mark
                g2.drawRect(4, 4, 16, 16);
                g2.setFont(new Font("Dialog", Font.BOLD, 16));
                g2.drawString("?", 10, 18);
            }
        }
    }
    public static void main(String[] args) {

    }
}

class RoundedButton extends JButton {
    private final int arcWidth = 15;
    private final int arcHeight = 15;

    public RoundedButton(String text) {
        super(text);
        setupButton();
    }

    public RoundedButton(String text, Icon icon) {
        super(text, icon);
        setupButton();
    }

    private void setupButton() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            Color hoverColor = new Color(66, 139, 202);
            g2.setColor(hoverColor);
        } else {
            g2.setColor(getBackground());
        }

        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arcWidth, arcHeight));

        super.paintComponent(g2);
        g2.dispose();
    }


    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground().darker());
        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight));
        g2.dispose();
    }
}
