package GUI.component;

import com.toedter.calendar.JDateChooser;
import dao.HoaDonDAO;
import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.VeTauDAO;
import model.HoaDon;
import model.KhachHang;
import model.NhanVien;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Font;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: TraCuuHoaDon
 * @Tạo vào ngày: 21/04/2025
 * @Tác giả: Nguyen Huu Sang
 */

public class TraCuuHoaDonPanel extends JPanel {

    // Thành phần giao diện
    private JTextField txtMaHoaDon;
    private JTextField txtSoDienThoai;
    private JTextField txtMaNhanVien;
    private JDateChooser dateFrom;
    private JDateChooser dateTo;
    private JButton btnTimKiem;
    private JButton btnXoaRong;
    private JButton btnXemChiTiet;
    private JButton btnXuatExcel;
    private JTable tableHoaDon;
    private DefaultTableModel tableModel;
    private JPanel detailPanel;

    // DAO
    private HoaDonDAO hoaDonDAO;
    private KhachHangDAO khachHangDAO;
    private NhanVienDAO nhanVienDAO;

    // Constants
    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 9090;
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color ACCENT_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(231, 76, 60);

    // Định dạng số
    private DecimalFormat currencyFormat = new DecimalFormat("#,###");
    private JRadioButton rdoMaHoaDon;
    private JRadioButton rdoSoDienThoai;
    private JRadioButton rdoMaNhanVien;
    private JRadioButton rdoNgayLap;
    private ButtonGroup searchOptionGroup;
    private VeTauDAO veTauDAO;

    public TraCuuHoaDonPanel() {
        initializeDAO();
        initializeUI();
    }

//    private void initializeDAO() {
//        try {
//            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
//            hoaDonDAO = (HoaDonDAO) registry.lookup("hoaDonDAO");
//            khachHangDAO = (KhachHangDAO) registry.lookup("khachHangDAO");
//            nhanVienDAO = (NhanVienDAO) registry.lookup("nhanVienDAO");
//        } catch (RemoteException | NotBoundException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server: " + e.getMessage(),
//                    "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
//        }
//    }

    private void initializeDAO() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            hoaDonDAO = (HoaDonDAO) registry.lookup("hoaDonDAO");
            khachHangDAO = (KhachHangDAO) registry.lookup("khachHangDAO");
            nhanVienDAO = (NhanVienDAO) registry.lookup("nhanVienDAO");
            veTauDAO = (VeTauDAO) registry.lookup("veTauDAO");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server: " + e.getMessage(),
                    "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
        }
    }

//    private void initializeUI() {
//        setLayout(new BorderLayout(10, 10));
//        setBorder(new EmptyBorder(15, 15, 15, 15));
//        setBackground(BACKGROUND_COLOR);
//
//        // Panel tìm kiếm
//        JPanel searchPanel = createSearchPanel();
//        add(searchPanel, BorderLayout.NORTH);
//
//        // Panel bảng kết quả
//        JPanel resultPanel = createResultPanel();
//        add(resultPanel, BorderLayout.CENTER);
//
//        // Panel hiển thị thông tin chi tiết
//        detailPanel = createDetailPanel();
//        add(detailPanel, BorderLayout.SOUTH);
//    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(BACKGROUND_COLOR);

        // Panel tìm kiếm
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Panel bảng kết quả
        JPanel resultPanel = createResultPanel();
        add(resultPanel, BorderLayout.CENTER);

        // Panel hiển thị thông tin chi tiết
//        detailPanel = createDetailPanel();
//        add(detailPanel, BorderLayout.SOUTH);
    }


//    private JPanel createSearchPanel() {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout());
//        panel.setBackground(BACKGROUND_COLOR);
//        panel.setBorder(BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
//                "Tra Cứu Hóa Đơn",
//                TitledBorder.LEFT, TitledBorder.TOP,
//                new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR));
//
//        // Panel chứa các điều khiển tìm kiếm
//        JPanel controlPanel = new JPanel(new GridBagLayout());
//        controlPanel.setBackground(BACKGROUND_COLOR);
//        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
//
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        gbc.anchor = GridBagConstraints.WEST;
//
//        // Hàng 1: Mã hóa đơn và SĐT khách hàng
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        controlPanel.add(new JLabel("Mã Hóa Đơn:"), gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        txtMaHoaDon = new JTextField(15);
//        txtMaHoaDon.setFont(new Font("Arial", Font.PLAIN, 13));
//        controlPanel.add(txtMaHoaDon, gbc);
//        gbc.weightx = 0.0;
//
//        gbc.gridx = 2;
//        controlPanel.add(new JLabel("SĐT Khách Hàng:"), gbc);
//
//        gbc.gridx = 3;
//        gbc.weightx = 1.0;
//        txtSoDienThoai = new JTextField(15);
//        txtSoDienThoai.setFont(new Font("Arial", Font.PLAIN, 13));
//        controlPanel.add(txtSoDienThoai, gbc);
//        gbc.weightx = 0.0;
//
//        // Hàng 2: Mã NV và Từ ngày
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        controlPanel.add(new JLabel("Mã Nhân Viên:"), gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        txtMaNhanVien = new JTextField(15);
//        txtMaNhanVien.setFont(new Font("Arial", Font.PLAIN, 13));
//        controlPanel.add(txtMaNhanVien, gbc);
//        gbc.weightx = 0.0;
//
//        gbc.gridx = 2;
//        controlPanel.add(new JLabel("Từ Ngày:"), gbc);
//
//        gbc.gridx = 3;
//        gbc.weightx = 1.0;
//        dateFrom = new JDateChooser();
//        dateFrom.setDateFormatString("dd/MM/yyyy");
//        dateFrom.setFont(new Font("Arial", Font.PLAIN, 13));
//        dateFrom.setDate(Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
//        controlPanel.add(dateFrom, gbc);
//        gbc.weightx = 0.0;
//
//        // Hàng 3: Đến ngày và các button
//        gbc.gridx = 0;
//        gbc.gridy = 2;
//        controlPanel.add(new JLabel("Đến Ngày:"), gbc);
//
//        gbc.gridx = 1;
//        gbc.weightx = 1.0;
//        dateTo = new JDateChooser();
//        dateTo.setDateFormatString("dd/MM/yyyy");
//        dateTo.setFont(new Font("Arial", Font.PLAIN, 13));
//        dateTo.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
//        controlPanel.add(dateTo, gbc);
//        gbc.weightx = 0.0;
//
//        // Panel chứa các nút điều khiển
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        buttonPanel.setBackground(BACKGROUND_COLOR);
//
//        btnTimKiem = createStyledButton("Tìm Kiếm", ACCENT_COLOR, new ImageIcon("assets/search.png"));
//        btnTimKiem.addActionListener(this::timKiemHoaDon);
//        buttonPanel.add(btnTimKiem);
//
//        btnXoaRong = createStyledButton("Xóa Rỗng", new Color(52, 152, 219), new ImageIcon("assets/clear.png"));
//        btnXoaRong.addActionListener(e -> xoaRong());
//        buttonPanel.add(btnXoaRong);
//
//        gbc.gridx = 2;
//        gbc.gridy = 3;
//        gbc.gridwidth = 2;
//        gbc.anchor = GridBagConstraints.EAST;
//        controlPanel.add(buttonPanel, gbc);
//
//        panel.add(controlPanel, BorderLayout.CENTER);
//        return panel;
//    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                "Tra Cứu Hóa Đơn",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR));

        // Panel chứa các điều khiển tìm kiếm
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Tạo radio buttons cho các tùy chọn tìm kiếm
        rdoMaHoaDon = new JRadioButton("Tìm theo Mã Hóa Đơn");
        rdoMaHoaDon.setBackground(BACKGROUND_COLOR);
        rdoMaHoaDon.setFont(new Font("Arial", Font.BOLD, 12));
        rdoMaHoaDon.setSelected(true);

        rdoSoDienThoai = new JRadioButton("Tìm theo SĐT Khách Hàng");
        rdoSoDienThoai.setBackground(BACKGROUND_COLOR);
        rdoSoDienThoai.setFont(new Font("Arial", Font.BOLD, 12));

        rdoMaNhanVien = new JRadioButton("Tìm theo Mã Nhân Viên");
        rdoMaNhanVien.setBackground(BACKGROUND_COLOR);
        rdoMaNhanVien.setFont(new Font("Arial", Font.BOLD, 12));

        rdoNgayLap = new JRadioButton("Tìm theo Ngày Lập");
        rdoNgayLap.setBackground(BACKGROUND_COLOR);
        rdoNgayLap.setFont(new Font("Arial", Font.BOLD, 12));

        searchOptionGroup = new ButtonGroup();
        searchOptionGroup.add(rdoMaHoaDon);
        searchOptionGroup.add(rdoSoDienThoai);
        searchOptionGroup.add(rdoMaNhanVien);
        searchOptionGroup.add(rdoNgayLap);

        // Panel chứa radio buttons
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        optionsPanel.setBackground(BACKGROUND_COLOR);
        optionsPanel.add(rdoMaHoaDon);
        optionsPanel.add(rdoSoDienThoai);
        optionsPanel.add(rdoMaNhanVien);
        optionsPanel.add(rdoNgayLap);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        controlPanel.add(optionsPanel, gbc);
        gbc.gridwidth = 1;

        // Hàng 1: Mã hóa đơn
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(new JLabel("Mã Hóa Đơn:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtMaHoaDon = new JTextField(15);
        txtMaHoaDon.setFont(new Font("Arial", Font.PLAIN, 13));
        controlPanel.add(txtMaHoaDon, gbc);
        gbc.weightx = 0.0;

        // Hàng 2: SĐT khách hàng
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlPanel.add(new JLabel("SĐT Khách Hàng:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtSoDienThoai = new JTextField(15);
        txtSoDienThoai.setFont(new Font("Arial", Font.PLAIN, 13));
        txtSoDienThoai.setEnabled(false);
        controlPanel.add(txtSoDienThoai, gbc);
        gbc.weightx = 0.0;

        // Hàng 3: Mã nhân viên
        gbc.gridx = 0;
        gbc.gridy = 3;
        controlPanel.add(new JLabel("Mã Nhân Viên:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtMaNhanVien = new JTextField(15);
        txtMaNhanVien.setFont(new Font("Arial", Font.PLAIN, 13));
        txtMaNhanVien.setEnabled(false);
        controlPanel.add(txtMaNhanVien, gbc);
        gbc.weightx = 0.0;

        // Hàng 4: Từ ngày
        gbc.gridx = 0;
        gbc.gridy = 4;
        controlPanel.add(new JLabel("Từ Ngày:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateFrom = new JDateChooser();
        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateFrom.setFont(new Font("Arial", Font.PLAIN, 13));
        dateFrom.setDate(Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateFrom.setEnabled(false);
        controlPanel.add(dateFrom, gbc);
        gbc.weightx = 0.0;

        // Hàng 5: Đến ngày
        gbc.gridx = 0;
        gbc.gridy = 5;
        controlPanel.add(new JLabel("Đến Ngày:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateTo = new JDateChooser();
        dateTo.setDateFormatString("dd/MM/yyyy");
        dateTo.setFont(new Font("Arial", Font.PLAIN, 13));
        dateTo.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateTo.setEnabled(false);
        controlPanel.add(dateTo, gbc);
        gbc.weightx = 0.0;

        // Thêm action listeners cho radio buttons
        rdoMaHoaDon.addActionListener(e -> updateSearchControls());
        rdoSoDienThoai.addActionListener(e -> updateSearchControls());
        rdoMaNhanVien.addActionListener(e -> updateSearchControls());
        rdoNgayLap.addActionListener(e -> updateSearchControls());

        // Panel chứa các nút điều khiển
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        btnTimKiem = createStyledButton("Tìm Kiếm", ACCENT_COLOR, new ImageIcon("assets/search.png"));
        btnTimKiem.addActionListener(this::timKiemHoaDon);
        buttonPanel.add(btnTimKiem);

        btnXoaRong = createStyledButton("Xóa Rỗng", new Color(52, 152, 219), new ImageIcon("assets/clear.png"));
        btnXoaRong.addActionListener(e -> xoaRong());
        buttonPanel.add(btnXoaRong);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        controlPanel.add(buttonPanel, gbc);

        panel.add(controlPanel, BorderLayout.CENTER);
        return panel;
    }

    private void updateSearchControls() {
        // Kích hoạt các điều khiển theo tùy chọn tìm kiếm
        txtMaHoaDon.setEnabled(rdoMaHoaDon.isSelected());
        txtSoDienThoai.setEnabled(rdoSoDienThoai.isSelected());
        txtMaNhanVien.setEnabled(rdoMaNhanVien.isSelected());
        dateFrom.setEnabled(rdoNgayLap.isSelected());
        dateTo.setEnabled(rdoNgayLap.isSelected());

        // Clear các trường không được chọn
        if (!rdoMaHoaDon.isSelected()) txtMaHoaDon.setText("");
        if (!rdoSoDienThoai.isSelected()) txtSoDienThoai.setText("");
        if (!rdoMaNhanVien.isSelected()) txtMaNhanVien.setText("");
        if (!rdoNgayLap.isSelected()) {
            dateFrom.setDate(Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            dateTo.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
    }


//    private JPanel createResultPanel() {
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.setBackground(BACKGROUND_COLOR);
//        panel.setBorder(BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
//                "Kết Quả Tìm Kiếm",
//                TitledBorder.LEFT, TitledBorder.TOP,
//                new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR));
//
//        // Tạo model cho table
//        tableModel = new DefaultTableModel() {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false; // Không cho phép sửa trực tiếp trên bảng
//            }
//        };
//        tableModel.addColumn("Mã Hóa Đơn");
//        tableModel.addColumn("Ngày Lập");
//        tableModel.addColumn("Tên Khách Hàng");
//        tableModel.addColumn("SĐT Khách Hàng");
//        tableModel.addColumn("Tên Nhân Viên");
//        tableModel.addColumn("Loại Hóa Đơn");
//        tableModel.addColumn("Tổng Tiền");
//        tableModel.addColumn("Tiền Giảm");
//
//        tableHoaDon = new JTable(tableModel);
//        tableHoaDon.setRowHeight(25);
//        tableHoaDon.setFont(new Font("Arial", Font.PLAIN, 13));
//        tableHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        tableHoaDon.setGridColor(new Color(230, 230, 230));
//        tableHoaDon.setSelectionBackground(new Color(237, 242, 255));
//        tableHoaDon.setSelectionForeground(Color.BLACK);
//        tableHoaDon.setAutoCreateRowSorter(true);
//
//        // Style header
//        JTableHeader header = tableHoaDon.getTableHeader();
//        header.setFont(new Font("Arial", Font.BOLD, 13));
//        header.setBackground(PRIMARY_COLOR);
//        header.setForeground(Color.WHITE);
//        header.setPreferredSize(new Dimension(header.getWidth(), 35));
//
//        // Thêm sự kiện double click để hiển thị chi tiết
//        tableHoaDon.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    showHoaDonDetail();
//                }
//            }
//        });
//
//        // Thêm table vào scroll pane
//        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
//        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//        panel.add(scrollPane, BorderLayout.CENTER);
//
//        // Thêm panel chứa các nút chức năng
//        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        actionPanel.setBackground(BACKGROUND_COLOR);
//
//        btnXemChiTiet = createStyledButton("Xem Chi Tiết", PRIMARY_COLOR, new ImageIcon("assets/view.png"));
//        btnXemChiTiet.addActionListener(e -> showHoaDonDetail());
//        actionPanel.add(btnXemChiTiet);
//
//        btnXuatExcel = createStyledButton("Xuất Excel", new Color(46, 204, 113), new ImageIcon("assets/excel.png"));
//        btnXuatExcel.addActionListener(e -> xuatExcel());
//        actionPanel.add(btnXuatExcel);
//
//        panel.add(actionPanel, BorderLayout.SOUTH);
//
//        return panel;
//    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                "Kết Quả Tìm Kiếm",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR));

        // Tạo model cho table
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        tableModel.addColumn("Mã Hóa Đơn");
        tableModel.addColumn("Ngày Lập");
        tableModel.addColumn("Tên Khách Hàng");
        tableModel.addColumn("SĐT Khách Hàng");
        tableModel.addColumn("Tên Nhân Viên");
        tableModel.addColumn("Loại Hóa Đơn");
        tableModel.addColumn("Tổng Tiền");
        tableModel.addColumn("Tiền Giảm");

        tableHoaDon = new JTable(tableModel);
        tableHoaDon.setRowHeight(25);
        tableHoaDon.setFont(new Font("Arial", Font.PLAIN, 13));
        tableHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableHoaDon.setGridColor(new Color(230, 230, 230));
        tableHoaDon.setSelectionBackground(new Color(237, 242, 255));
        tableHoaDon.setSelectionForeground(Color.BLACK);
        tableHoaDon.setAutoCreateRowSorter(true);

        // Style header
        JTableHeader header = tableHoaDon.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // Thêm sự kiện double click để hiển thị chi tiết
        tableHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showHoaDonDetail();
                }
            }
        });

        // Thêm table vào scroll pane
        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Thêm panel chứa các nút chức năng
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(BACKGROUND_COLOR);

        btnXemChiTiet = createStyledButton("Xem Chi Tiết", PRIMARY_COLOR, new ImageIcon("assets/view.png"));
        btnXemChiTiet.addActionListener(e -> showHoaDonDetail());
        actionPanel.add(btnXemChiTiet);

        btnXuatExcel = createStyledButton("Xuất Excel", new Color(46, 204, 113), new ImageIcon("assets/excel.png"));
        btnXuatExcel.addActionListener(e -> xuatExcel());
        actionPanel.add(btnXuatExcel);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

//    private JPanel createDetailPanel() {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout());
//        panel.setBackground(BACKGROUND_COLOR);
//        panel.setBorder(BorderFactory.createTitledBorder(
//                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
//                "Thông Tin Chi Tiết",
//                TitledBorder.LEFT, TitledBorder.TOP,
//                new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR));
//        panel.setPreferredSize(new Dimension(getWidth(), 200));
//
//        // Ban đầu hiển thị thông báo chọn hóa đơn
//        JLabel lblMessage = new JLabel("Chọn một hóa đơn để xem chi tiết");
//        lblMessage.setHorizontalAlignment(JLabel.CENTER);
//        lblMessage.setFont(new Font("Arial", Font.ITALIC, 14));
//        panel.add(lblMessage, BorderLayout.CENTER);
//
//        return panel;
//    }


//    private JButton createStyledButton(String text, Color color, Icon icon) {
//        JButton button = new JButton(text);
//        if (icon != null) {
//            button.setIcon(icon);
//        }
//        button.setFont(new Font("Arial", Font.BOLD, 13));
//        button.setForeground(Color.WHITE);
//        button.setBackground(color);
//        button.setFocusPainted(false);
//        button.setBorderPainted(false);
//        button.setPreferredSize(new Dimension(130, 35));
//        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        // Hiệu ứng hover
//        button.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                button.setBackground(color.darker());
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                button.setBackground(color);
//            }
//        });
//
//        return button;
//    }

    private JButton createStyledButton(String text, Color color, Icon icon) {
        JButton button = new JButton(text);
        if (icon != null) {
            button.setIcon(icon);
        }
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(130, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

//    private void timKiemHoaDon(ActionEvent e) {
//        // Disable button và hiển thị trạng thái đang tìm kiếm
//        btnTimKiem.setEnabled(false);
//        btnTimKiem.setText("Đang tìm...");
//        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//
//        // Lấy giá trị tìm kiếm từ các điều khiển
//        String maHoaDon = txtMaHoaDon.getText().trim();
//        String soDienThoai = txtSoDienThoai.getText().trim();
//        String maNhanVien = txtMaNhanVien.getText().trim();
//        LocalDate tuNgay = dateFrom.getDate() != null ?
//                dateFrom.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
//        LocalDate denNgay = dateTo.getDate() != null ?
//                dateTo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
//
//        // Sử dụng SwingWorker để tìm kiếm không đồng bộ
//        SwingWorker<List<HoaDon>, Void> worker = new SwingWorker<>() {
//            @Override
//            protected List<HoaDon> doInBackground() throws Exception {
//                // Gọi phương thức tìm kiếm từ DAO
//                if (hoaDonDAO == null) {
//                    throw new Exception("Chưa kết nối được đến server");
//                }
//
//                return hoaDonDAO.timKiemHoaDon(maHoaDon, soDienThoai, maNhanVien, tuNgay, denNgay);
//            }
//
//            @Override
//            protected void done() {
//                // Khôi phục nút tìm kiếm và con trỏ
//                btnTimKiem.setEnabled(true);
//                btnTimKiem.setText("Tìm Kiếm");
//                setCursor(Cursor.getDefaultCursor());
//
//                try {
//                    // Lấy kết quả và hiển thị lên table
//                    List<HoaDon> ketQua = get();
//                    updateTableData(ketQua);
//                } catch (InterruptedException | ExecutionException ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
//                            "Lỗi khi tìm kiếm hóa đơn: " + ex.getMessage(),
//                            "Lỗi", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        };
//
//        worker.execute();
//    }

    private void timKiemHoaDon(ActionEvent e) {
        // Disable button và hiển thị trạng thái đang tìm kiếm
        btnTimKiem.setEnabled(false);
        btnTimKiem.setText("Đang tìm...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Lấy giá trị tìm kiếm từ các điều khiển dựa trên tùy chọn đã chọn
        String maHoaDon = rdoMaHoaDon.isSelected() ? txtMaHoaDon.getText().trim() : "";
        String soDienThoai = rdoSoDienThoai.isSelected() ? txtSoDienThoai.getText().trim() : "";
        String maNhanVien = rdoMaNhanVien.isSelected() ? txtMaNhanVien.getText().trim() : "";
        LocalDate tuNgay = null;
        LocalDate denNgay = null;

        if (rdoNgayLap.isSelected()) {
            tuNgay = dateFrom.getDate() != null ?
                    dateFrom.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
            denNgay = dateTo.getDate() != null ?
                    dateTo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        }

        // Kiểm tra xem có ít nhất 1 tiêu chí tìm kiếm được nhập
        if ((rdoMaHoaDon.isSelected() && maHoaDon.isEmpty()) ||
                (rdoSoDienThoai.isSelected() && soDienThoai.isEmpty()) ||
                (rdoMaNhanVien.isSelected() && maNhanVien.isEmpty()) ||
                (rdoNgayLap.isSelected() && (tuNgay == null || denNgay == null))) {

            btnTimKiem.setEnabled(true);
            btnTimKiem.setText("Tìm Kiếm");
            setCursor(Cursor.getDefaultCursor());

            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập tiêu chí tìm kiếm",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Sử dụng SwingWorker để tìm kiếm không đồng bộ
        LocalDate finalTuNgay = tuNgay;
        LocalDate finalDenNgay = denNgay;
        SwingWorker<List<HoaDon>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<HoaDon> doInBackground() throws Exception {
                // Gọi phương thức tìm kiếm từ DAO
                if (hoaDonDAO == null) {
                    throw new Exception("Chưa kết nối được đến server");
                }

                return hoaDonDAO.timKiemHoaDon(maHoaDon, soDienThoai, maNhanVien, finalTuNgay, finalDenNgay);
            }

            @Override
            protected void done() {
                // Khôi phục nút tìm kiếm và con trỏ
                btnTimKiem.setEnabled(true);
                btnTimKiem.setText("Tìm Kiếm");
                setCursor(Cursor.getDefaultCursor());

                try {
                    // Lấy kết quả và hiển thị lên table
                    List<HoaDon> ketQua = get();
                    updateTableData(ketQua);
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
                            "Lỗi khi tìm kiếm hóa đơn: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }


//    private void updateTableData(List<HoaDon> hoaDons) {
//        // Xóa tất cả dữ liệu cũ
//        tableModel.setRowCount(0);
//
//        // Nếu không có kết quả
//        if (hoaDons == null || hoaDons.isEmpty()) {
//            JOptionPane.showMessageDialog(this,
//                    "Không tìm thấy hóa đơn nào phù hợp với tiêu chí tìm kiếm.",
//                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        // Thêm dữ liệu mới vào bảng
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//
//        for (HoaDon hoaDon : hoaDons) {
//            tableModel.addRow(new Object[]{
//                    hoaDon.getMaHD(),
//                    hoaDon.getNgayLap().format(formatter),
//                    hoaDon.getKhachHang().getTenKhachHang(),
//                    hoaDon.getKhachHang().getSoDienThoai(),
//                    hoaDon.getNv().getTenNV(),
//                    hoaDon.getLoaiHoaDon().getTenLoaiHoaDon(),
//                    currencyFormat.format(hoaDon.getTongTien()) + " VNĐ",
//                    currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ"
//            });
//        }
//
//        // Thông báo số lượng kết quả tìm thấy
//        JOptionPane.showMessageDialog(this,
//                "Tìm thấy " + hoaDons.size() + " hóa đơn phù hợp với tiêu chí tìm kiếm.",
//                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//    }

//    private void updateTableData(List<HoaDon> hoaDons) {
//        // Xóa tất cả dữ liệu cũ
//        tableModel.setRowCount(0);
//
//        // Nếu không có kết quả
//        if (hoaDons == null || hoaDons.isEmpty()) {
//            JOptionPane.showMessageDialog(this,
//                    "Không tìm thấy hóa đơn nào phù hợp với tiêu chí tìm kiếm.",
//                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        // Thêm dữ liệu mới vào bảng
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//
//        for (HoaDon hoaDon : hoaDons) {
//            tableModel.addRow(new Object[]{
//                    hoaDon.getMaHD(),
//                    hoaDon.getNgayLap().format(formatter),
//                    hoaDon.getKhachHang().getTenKhachHang(),
//                    hoaDon.getKhachHang().getSoDienThoai(), // Thay đổi từ sdt sang soDT theo class của bạn
//                    hoaDon.getNv().getTenNV(),
//                    hoaDon.getLoaiHoaDon().getTenLoaiHoaDon(),
//                    currencyFormat.format(hoaDon.getTongTien()) + " VNĐ",
//                    currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ"
//            });
//        }
//
//        // Thông báo số lượng kết quả tìm thấy
//        JOptionPane.showMessageDialog(this,
//                "Tìm thấy " + hoaDons.size() + " hóa đơn phù hợp với tiêu chí tìm kiếm.",
//                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//    }


    private void updateTableData(List<HoaDon> hoaDons) {
        // Xóa tất cả dữ liệu cũ
        tableModel.setRowCount(0);

        // Nếu không có kết quả
        if (hoaDons == null || hoaDons.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy hóa đơn nào phù hợp với tiêu chí tìm kiếm.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Thêm dữ liệu mới vào bảng
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (HoaDon hoaDon : hoaDons) {
            tableModel.addRow(new Object[]{
                    hoaDon.getMaHD(),
                    hoaDon.getNgayLap().format(formatter),
                    hoaDon.getKhachHang().getTenKhachHang(),
                    hoaDon.getKhachHang().getSoDienThoai(), // Sử dụng soDienThoai thay vì sdt
                    hoaDon.getNv().getTenNV(),
                    hoaDon.getLoaiHoaDon().getTenLoaiHoaDon(),
                    currencyFormat.format(hoaDon.getTongTien()) + " VNĐ",
                    currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ"
            });
        }

        // Thông báo số lượng kết quả tìm thấy
        JOptionPane.showMessageDialog(this,
                "Tìm thấy " + hoaDons.size() + " hóa đơn phù hợp với tiêu chí tìm kiếm.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }


//    private void xoaRong() {
//        txtMaHoaDon.setText("");
//        txtSoDienThoai.setText("");
//        txtMaNhanVien.setText("");
//        dateFrom.setDate(Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
//        dateTo.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
//    }

    private void xoaRong() {
        txtMaHoaDon.setText("");
        txtSoDienThoai.setText("");
        txtMaNhanVien.setText("");
        dateFrom.setDate(Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateTo.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // Reset về tùy chọn tìm theo mã hóa đơn
        rdoMaHoaDon.setSelected(true);
        updateSearchControls();
    }


//    private void showHoaDonDetail() {
//        int selectedRow = tableHoaDon.getSelectedRow();
//        if (selectedRow == -1) {
//            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn để xem chi tiết",
//                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        // Lấy mã hóa đơn từ hàng được chọn
//        String maHoaDon = tableModel.getValueAt(
//                tableHoaDon.convertRowIndexToModel(selectedRow), 0).toString();
//
//        // Hiển thị dialog chi tiết hóa đơn
//        JDialog detailDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Hóa Đơn", true);
//        detailDialog.setSize(800, 600);
//        detailDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
//
//        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
//            @Override
//            protected JPanel doInBackground() throws Exception {
//                // Lấy chi tiết hóa đơn từ server
//                return createHoaDonDetailPanel(maHoaDon);
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    JPanel detailContent = get();
//                    detailDialog.add(detailContent);
//                    detailDialog.setVisible(true);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
//                            "Lỗi khi lấy chi tiết hóa đơn: " + ex.getMessage(),
//                            "Lỗi", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        };
//
//        worker.execute();
//    }

    private void showHoaDonDetail() {
        int selectedRow = tableHoaDon.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn để xem chi tiết",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Lấy mã hóa đơn từ hàng được chọn
        String maHoaDon = tableModel.getValueAt(
                tableHoaDon.convertRowIndexToModel(selectedRow), 0).toString();

        // Hiển thị dialog chi tiết hóa đơn
        JDialog detailDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Hóa Đơn", true);
        detailDialog.setSize(800, 600);
        detailDialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));

        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                // Lấy chi tiết hóa đơn từ server
                return createHoaDonDetailPanel(maHoaDon);
            }

            @Override
            protected void done() {
                try {
                    JPanel detailContent = get();
                    detailDialog.add(detailContent);
                    detailDialog.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
                            "Lỗi khi lấy chi tiết hóa đơn: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

//    private JPanel createHoaDonDetailPanel(String maHoaDon) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout(10, 10));
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
//
//        try {
//            // Lấy thông tin chi tiết hóa đơn từ DAO
//            HoaDon hoaDon = hoaDonDAO.getHoaDonByMa(maHoaDon);
//
//            if (hoaDon == null) {
//                JLabel lblError = new JLabel("Không tìm thấy thông tin hóa đơn: " + maHoaDon);
//                lblError.setFont(new Font("Arial", Font.BOLD, 16));
//                lblError.setForeground(Color.RED);
//                panel.add(lblError, BorderLayout.CENTER);
//                return panel;
//            }
//
//            // Header panel - thông tin cơ bản của hóa đơn
//            JPanel headerPanel = new JPanel();
//            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
//            headerPanel.setBackground(Color.WHITE);
//            headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
//
//            JLabel lblTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
//            lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
//            lblTitle.setForeground(PRIMARY_COLOR);
//            lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            JLabel lblInvoiceId = new JLabel("Mã Hóa Đơn: " + hoaDon.getMaHD());
//            lblInvoiceId.setFont(new Font("Arial", Font.BOLD, 14));
//            lblInvoiceId.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//            JLabel lblDate = new JLabel("Ngày lập: " + hoaDon.getNgayLap().format(formatter));
//            lblDate.setFont(new Font("Arial", Font.PLAIN, 14));
//            lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            headerPanel.add(Box.createVerticalStrut(10));
//            headerPanel.add(lblTitle);
//            headerPanel.add(Box.createVerticalStrut(5));
//            headerPanel.add(lblInvoiceId);
//            headerPanel.add(Box.createVerticalStrut(5));
//            headerPanel.add(lblDate);
//            headerPanel.add(Box.createVerticalStrut(20));
//
//            // Panel chứa thông tin khách hàng và nhân viên
//            JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
//            infoPanel.setBackground(Color.WHITE);
//
//            // Thông tin khách hàng
//            JPanel customerPanel = new JPanel();
//            customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
//            customerPanel.setBackground(Color.WHITE);
//            customerPanel.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Thông tin khách hàng",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            KhachHang kh = hoaDon.getKhachHang();
//
//            customerPanel.add(createInfoRow("Mã KH:", kh.getMaKhachHang()));
//            customerPanel.add(createInfoRow("Tên KH:", kh.getTenKhachHang()));
//            customerPanel.add(createInfoRow("SĐT:", kh.getSoDienThoai()));
//            customerPanel.add(createInfoRow("Địa chỉ:", kh.getDiaChi()));
//            customerPanel.add(createInfoRow("Điểm tích lũy:", String.valueOf(kh.getDiemTichLuy())));
//
//            // Thông tin nhân viên và hóa đơn
//            JPanel staffPanel = new JPanel();
//            staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
//            staffPanel.setBackground(Color.WHITE);
//            staffPanel.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Thông tin nhân viên và hóa đơn",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            NhanVien nv = hoaDon.getNv();
//
//            staffPanel.add(createInfoRow("Mã NV:", nv.getMaNV()));
//            staffPanel.add(createInfoRow("Tên NV:", nv.getTenNV()));
//            staffPanel.add(createInfoRow("Loại HĐ:", hoaDon.getLoaiHoaDon().getTenLoaiHoaDon()));
//            staffPanel.add(createInfoRow("Tổng tiền:", currencyFormat.format(hoaDon.getTongTien()) + " VNĐ"));
//            staffPanel.add(createInfoRow("Tiền giảm:", currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ"));
//            staffPanel.add(createInfoRow("Thanh toán:", currencyFormat.format(hoaDon.getTongTien() - hoaDon.getTienGiam()) + " VNĐ"));
//
//            infoPanel.add(customerPanel);
//            infoPanel.add(staffPanel);
//
//            // Panel hiển thị chi tiết các vé trong hóa đơn
//            JPanel detailContent = new JPanel(new BorderLayout());
//            detailContent.setBackground(Color.WHITE);
//            detailContent.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Chi tiết vé",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            // Bảng chi tiết vé
//            DefaultTableModel detailModel = new DefaultTableModel();
//            detailModel.addColumn("STT");
//            detailModel.addColumn("Mã Vé");
//            detailModel.addColumn("Tuyến Tàu");
//            detailModel.addColumn("Ngày Đi");
//            detailModel.addColumn("Loại Vé");
//            detailModel.addColumn("Số Lượng");
//            detailModel.addColumn("VAT");
//            detailModel.addColumn("Thành Tiền");
//
//            JTable detailTable = new JTable(detailModel);
//            detailTable.setRowHeight(25);
//            detailTable.setFont(new Font("Arial", Font.PLAIN, 13));
//            detailTable.setGridColor(new Color(230, 230, 230));
//            detailTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
//            detailTable.getTableHeader().setBackground(PRIMARY_COLOR);
//            detailTable.getTableHeader().setForeground(Color.WHITE);
//
//            // Lấy chi tiết hóa đơn
//            if (hoaDon.getChiTietHoaDons() != null) {
//                int stt = 1;
//                double tongTien = 0;
//
//                // Thêm dữ liệu vào bảng chi tiết
//                // Trong môi trường thực tế, bạn sẽ cần truy vấn dựa trên ChiTietHoaDon
//                // và lấy thông tin tuyến tàu từ vé hoặc lịch trình tàu
//                // Đây chỉ là mẫu để hiển thị cấu trúc
//                for (var chiTiet : hoaDon.getChiTietHoaDons()) {
//                    if (chiTiet.getVeTau() != null) {
//                        var ve = chiTiet.getVeTau();
//                        String tuyenTau = "N/A"; // Trong thực tế, bạn sẽ lấy từ đối tượng vé
//                        LocalDate ngayDi = ve.getNgayDi();
//                        String loaiVe = ve.getDoiTuong();
//
//                        detailModel.addRow(new Object[] {
//                                stt++,
//                                ve.getMaVe(),
//                                tuyenTau,
//                                ngayDi.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
//                                loaiVe,
//                                chiTiet.getSoLuong(),
//                                chiTiet.getVAT() + "%",
//                                currencyFormat.format(chiTiet.getThanhTien()) + " VNĐ"
//                        });
//
//                        tongTien += chiTiet.getThanhTien();
//                    }
//                }
//
//                // Thêm hàng tổng cộng
//                detailModel.addRow(new Object[] {
//                        "", "", "", "", "", "", "Tổng cộng:", currencyFormat.format(tongTien) + " VNĐ"
//                });
//            }
//
//            JScrollPane detailScroll = new JScrollPane(detailTable);
//            detailScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//            detailContent.add(detailScroll, BorderLayout.CENTER);
//
//            // Footer panel với các nút chức năng
//            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//            footerPanel.setBackground(Color.WHITE);
//
//            JButton btnPrint = createStyledButton("In Hóa Đơn", new Color(52, 152, 219), null);
//            JButton btnClose = createStyledButton("Đóng", new Color(231, 76, 60), null);
//            btnClose.addActionListener(e -> ((JDialog) SwingUtilities.getWindowAncestor(panel)).dispose());
//
//            footerPanel.add(btnPrint);
//            footerPanel.add(btnClose);
//
//            // Thêm tất cả vào panel chính
//            panel.add(headerPanel, BorderLayout.NORTH);
//            panel.add(infoPanel, BorderLayout.CENTER);
//            panel.add(detailContent, BorderLayout.CENTER);
//            panel.add(footerPanel, BorderLayout.SOUTH);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            JLabel lblError = new JLabel("Lỗi lấy dữ liệu: " + e.getMessage());
//            lblError.setFont(new Font("Arial", Font.BOLD, 14));
//            lblError.setForeground(Color.RED);
//            panel.add(lblError, BorderLayout.CENTER);
//        }
//
//        return panel;
//    }

//    private JPanel createHoaDonDetailPanel(String maHoaDon) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout(10, 10));
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
//
//        try {
//            // Lấy thông tin chi tiết hóa đơn từ DAO
//            HoaDon hoaDon = hoaDonDAO.getHoaDonByMa(maHoaDon);
//
//            if (hoaDon == null) {
//                JLabel lblError = new JLabel("Không tìm thấy thông tin hóa đơn: " + maHoaDon);
//                lblError.setFont(new Font("Arial", Font.BOLD, 16));
//                lblError.setForeground(Color.RED);
//                panel.add(lblError, BorderLayout.CENTER);
//                return panel;
//            }
//
//            // Header panel - thông tin cơ bản của hóa đơn
//            JPanel headerPanel = new JPanel();
//            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
//            headerPanel.setBackground(Color.WHITE);
//            headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
//
//            JLabel lblTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
//            lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
//            lblTitle.setForeground(PRIMARY_COLOR);
//            lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            JLabel lblInvoiceId = new JLabel("Mã Hóa Đơn: " + hoaDon.getMaHD());
//            lblInvoiceId.setFont(new Font("Arial", Font.BOLD, 14));
//            lblInvoiceId.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//            JLabel lblDate = new JLabel("Ngày lập: " + hoaDon.getNgayLap().format(formatter));
//            lblDate.setFont(new Font("Arial", Font.PLAIN, 14));
//            lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            headerPanel.add(Box.createVerticalStrut(10));
//            headerPanel.add(lblTitle);
//            headerPanel.add(Box.createVerticalStrut(5));
//            headerPanel.add(lblInvoiceId);
//            headerPanel.add(Box.createVerticalStrut(5));
//            headerPanel.add(lblDate);
//            headerPanel.add(Box.createVerticalStrut(20));
//
//            // Panel chứa thông tin khách hàng và nhân viên
//            JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
//            infoPanel.setBackground(Color.WHITE);
//
//            // Thông tin khách hàng
//            JPanel customerPanel = new JPanel();
//            customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
//            customerPanel.setBackground(Color.WHITE);
//            customerPanel.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Thông tin khách hàng",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            KhachHang kh = hoaDon.getKhachHang();
//
//            customerPanel.add(createInfoRow("Mã KH:", kh.getMaKhachHang()));
//            customerPanel.add(createInfoRow("Tên KH:", kh.getTenKhachHang()));
//            customerPanel.add(createInfoRow("SĐT:", kh.getSoDienThoai()));
//            customerPanel.add(createInfoRow("Địa chỉ:", kh.getDiaChi()));
//            customerPanel.add(createInfoRow("Điểm tích lũy:", String.valueOf(kh.getDiemTichLuy())));
//
//            // Thông tin nhân viên và hóa đơn
//            JPanel staffPanel = new JPanel();
//            staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
//            staffPanel.setBackground(Color.WHITE);
//            staffPanel.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Thông tin nhân viên và hóa đơn",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            NhanVien nv = hoaDon.getNv();
//
//            staffPanel.add(createInfoRow("Mã NV:", nv.getMaNV()));
//            staffPanel.add(createInfoRow("Tên NV:", nv.getTenNV()));
//            staffPanel.add(createInfoRow("Loại HĐ:", hoaDon.getLoaiHoaDon().getTenLoaiHoaDon()));
//            staffPanel.add(createInfoRow("Tổng tiền:", currencyFormat.format(hoaDon.getTongTien()) + " VNĐ"));
//            staffPanel.add(createInfoRow("Tiền giảm:", currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ"));
//            staffPanel.add(createInfoRow("Thanh toán:", currencyFormat.format(hoaDon.getTongTien() - hoaDon.getTienGiam()) + " VNĐ"));
//
//            infoPanel.add(customerPanel);
//            infoPanel.add(staffPanel);
//
//            // Panel hiển thị chi tiết các vé trong hóa đơn
//            JPanel detailContent = new JPanel(new BorderLayout());
//            detailContent.setBackground(Color.WHITE);
//            detailContent.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Chi tiết vé",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            // Bảng chi tiết vé
//            DefaultTableModel detailModel = new DefaultTableModel();
//            detailModel.addColumn("STT");
//            detailModel.addColumn("Mã Vé");
//            detailModel.addColumn("Ngày Đi");
//            detailModel.addColumn("Loại Vé");
//            detailModel.addColumn("Số Lượng");
//            detailModel.addColumn("VAT");
//            detailModel.addColumn("Thành Tiền");
//
//            JTable detailTable = new JTable(detailModel);
//            detailTable.setRowHeight(25);
//            detailTable.setFont(new Font("Arial", Font.PLAIN, 13));
//            detailTable.setGridColor(new Color(230, 230, 230));
//            detailTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
//            detailTable.getTableHeader().setBackground(PRIMARY_COLOR);
//            detailTable.getTableHeader().setForeground(Color.BLACK);
//
//            // Lấy chi tiết hóa đơn
//            if (hoaDon.getChiTietHoaDons() != null) {
//                int stt = 1;
//                double tongTien = 0;
//
//                // Thêm dữ liệu vào bảng chi tiết
//                for (var chiTiet : hoaDon.getChiTietHoaDons()) {
//                    if (chiTiet.getVeTau() != null) {
//                        var ve = chiTiet.getVeTau();
//                        String loaiVe = ve.getDoiTuong();
//
//                        detailModel.addRow(new Object[]{
//                                stt++,
//                                ve.getMaVe(),
//                                ve.getNgayDi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
//                                loaiVe,
//                                chiTiet.getSoLuong(),
//                                chiTiet.getVAT() + "%",
//                                currencyFormat.format(chiTiet.getThanhTien()) + " VNĐ"
//                        });
//
//                        tongTien += chiTiet.getThanhTien();
//                    }
//                }
//
//                // Thêm hàng tổng cộng
//                detailModel.addRow(new Object[]{
//                        "", "", "", "", "", "Tổng cộng:", currencyFormat.format(tongTien) + " VNĐ"
//                });
//            }
//
//            JScrollPane detailScroll = new JScrollPane(detailTable);
//            detailScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//            detailContent.add(detailScroll, BorderLayout.CENTER);
//
//            // Footer panel với các nút chức năng
//            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//            footerPanel.setBackground(Color.WHITE);
//
//            JButton btnPrint = createStyledButton("In Hóa Đơn", new Color(52, 152, 219), null);
//            btnPrint.addActionListener(e -> inHoaDon(hoaDon));
//
//            JButton btnClose = createStyledButton("Đóng", new Color(231, 76, 60), null);
//            btnClose.addActionListener(e -> ((JDialog) SwingUtilities.getWindowAncestor(panel)).dispose());
//
//            footerPanel.add(btnPrint);
//            footerPanel.add(btnClose);
//
//            // Thêm tất cả vào panel chính
//            panel.add(headerPanel, BorderLayout.NORTH);
//            panel.add(infoPanel, BorderLayout.CENTER);
//            JPanel centerPanel = new JPanel(new BorderLayout());
//            centerPanel.setBackground(Color.WHITE);
//            centerPanel.add(infoPanel, BorderLayout.NORTH);
//            centerPanel.add(detailContent, BorderLayout.CENTER);
//            panel.add(centerPanel, BorderLayout.CENTER);
//            panel.add(footerPanel, BorderLayout.SOUTH);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            JLabel lblError = new JLabel("Lỗi lấy dữ liệu: " + e.getMessage());
//            lblError.setFont(new Font("Arial", Font.BOLD, 14));
//            lblError.setForeground(Color.RED);
//            panel.add(lblError, BorderLayout.CENTER);
//        }
//
//        return panel;
//    }

//    private JPanel createHoaDonDetailPanel(String maHoaDon) {
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout(10, 10));
//        panel.setBackground(Color.WHITE);
//        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
//
//        try {
//            // Lấy thông tin chi tiết hóa đơn từ DAO
//            HoaDon hoaDon = hoaDonDAO.getHoaDonByMa(maHoaDon);
//
//            if (hoaDon == null) {
//                JLabel lblError = new JLabel("Không tìm thấy thông tin hóa đơn: " + maHoaDon);
//                lblError.setFont(new Font("Arial", Font.BOLD, 16));
//                lblError.setForeground(Color.RED);
//                panel.add(lblError, BorderLayout.CENTER);
//                return panel;
//            }
//
//            // Header panel - thông tin cơ bản của hóa đơn
//            JPanel headerPanel = new JPanel();
//            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
//            headerPanel.setBackground(Color.WHITE);
//            headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
//
//            JLabel lblTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
//            lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
//            lblTitle.setForeground(PRIMARY_COLOR);
//            lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            JLabel lblInvoiceId = new JLabel("Mã Hóa Đơn: " + hoaDon.getMaHD());
//            lblInvoiceId.setFont(new Font("Arial", Font.BOLD, 14));
//            lblInvoiceId.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//            JLabel lblDate = new JLabel("Ngày lập: " + hoaDon.getNgayLap().format(formatter));
//            lblDate.setFont(new Font("Arial", Font.PLAIN, 14));
//            lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);
//
//            headerPanel.add(Box.createVerticalStrut(10));
//            headerPanel.add(lblTitle);
//            headerPanel.add(Box.createVerticalStrut(5));
//            headerPanel.add(lblInvoiceId);
//            headerPanel.add(Box.createVerticalStrut(5));
//            headerPanel.add(lblDate);
//            headerPanel.add(Box.createVerticalStrut(20));
//
//            // Panel chứa thông tin khách hàng và nhân viên
//            JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
//            infoPanel.setBackground(Color.WHITE);
//
//            // Thông tin khách hàng
//            JPanel customerPanel = new JPanel();
//            customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
//            customerPanel.setBackground(Color.WHITE);
//            customerPanel.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Thông tin khách hàng",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            KhachHang kh = hoaDon.getKhachHang();
//
//            customerPanel.add(createInfoRow("Mã KH:", kh.getMaKhachHang()));
//            customerPanel.add(createInfoRow("Tên KH:", kh.getTenKhachHang()));
//            customerPanel.add(createInfoRow("SĐT:", kh.getSoDienThoai()));
//            customerPanel.add(createInfoRow("Địa chỉ:", kh.getDiaChi()));
//            customerPanel.add(createInfoRow("Điểm tích lũy:", String.valueOf(kh.getDiemTichLuy())));
//
//            // Thông tin nhân viên và hóa đơn
//            JPanel staffPanel = new JPanel();
//            staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
//            staffPanel.setBackground(Color.WHITE);
//            staffPanel.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Thông tin nhân viên và hóa đơn",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            NhanVien nv = hoaDon.getNv();
//
//            staffPanel.add(createInfoRow("Mã NV:", nv.getMaNV()));
//            staffPanel.add(createInfoRow("Tên NV:", nv.getTenNV()));
//            staffPanel.add(createInfoRow("Loại HĐ:", hoaDon.getLoaiHoaDon().getTenLoaiHoaDon()));
//            staffPanel.add(createInfoRow("Tổng tiền:", currencyFormat.format(hoaDon.getTongTien()) + " VNĐ"));
//            staffPanel.add(createInfoRow("Tiền giảm:", currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ"));
//            staffPanel.add(createInfoRow("Thanh toán:", currencyFormat.format(hoaDon.getTongTien() - hoaDon.getTienGiam()) + " VNĐ"));
//
//            infoPanel.add(customerPanel);
//            infoPanel.add(staffPanel);
//
//            // Panel hiển thị chi tiết các vé trong hóa đơn
//            JPanel detailContent = new JPanel(new BorderLayout());
//            detailContent.setBackground(Color.WHITE);
//            detailContent.setBorder(BorderFactory.createTitledBorder(
//                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
//                    "Chi tiết vé",
//                    TitledBorder.LEFT, TitledBorder.TOP,
//                    new Font("Arial", Font.BOLD, 14), Color.BLACK));
//
//            // Bảng chi tiết vé
//            DefaultTableModel detailModel = new DefaultTableModel();
//            detailModel.addColumn("STT");
//            detailModel.addColumn("Mã Vé");
//            detailModel.addColumn("Tuyến Tàu"); // Thêm cột tuyến tàu
//            detailModel.addColumn("Ngày Đi");
//            detailModel.addColumn("Loại Vé");
//            detailModel.addColumn("Số Lượng");
//            detailModel.addColumn("VAT");
//            detailModel.addColumn("Thành Tiền");
//
//            JTable detailTable = new JTable(detailModel);
//            detailTable.setRowHeight(25);
//            detailTable.setFont(new Font("Arial", Font.PLAIN, 13));
//            detailTable.setGridColor(new Color(230, 230, 230));
//
//            // Style header với màu chữ đen để dễ nhìn
//            JTableHeader header = detailTable.getTableHeader();
//            header.setFont(new Font("Arial", Font.BOLD, 13));
//            header.setBackground(PRIMARY_COLOR);
//            header.setForeground(Color.BLACK);
//            header.setPreferredSize(new Dimension(header.getWidth(), 30));
//
//            // Lấy chi tiết hóa đơn
//            if (hoaDon.getChiTietHoaDons() != null) {
//                int stt = 1;
//                double tongTien = 0;
//
//                // Thêm dữ liệu vào bảng chi tiết
//                for (var chiTiet : hoaDon.getChiTietHoaDons()) {
//                    if (chiTiet.getVeTau() != null) {
//                        var ve = chiTiet.getVeTau();
//                        String loaiVe = ve.getDoiTuong();
//
//                        // Lấy thông tin tuyến tàu thông qua các quan hệ
//                        String tenTuyen = "N/A";
//                        if (ve.getLichTrinhTau() != null &&
//                                ve.getLichTrinhTau().getTau() != null &&
//                                ve.getLichTrinhTau().getTau().getTuyenTau() != null) {
//
//                            tenTuyen = ve.getLichTrinhTau().getTau().getTuyenTau().getTenTuyen();
//                        }
//
//                        detailModel.addRow(new Object[]{
//                                stt++,
//                                ve.getMaVe(),
//                                tenTuyen, // Thêm tên tuyến tàu
//                                ve.getNgayDi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
//                                loaiVe,
//                                chiTiet.getSoLuong(),
//                                chiTiet.getVAT() + "%",
//                                currencyFormat.format(chiTiet.getThanhTien()) + " VNĐ"
//                        });
//
//                        tongTien += chiTiet.getThanhTien();
//                    }
//                }
//
//                // Thêm hàng tổng cộng
//                detailModel.addRow(new Object[]{
//                        "", "", "", "", "", "", "Tổng cộng:", currencyFormat.format(tongTien) + " VNĐ"
//                });
//            }
//
//            JScrollPane detailScroll = new JScrollPane(detailTable);
//            detailScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//            detailContent.add(detailScroll, BorderLayout.CENTER);
//
//            // Footer panel với các nút chức năng
//            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//            footerPanel.setBackground(Color.WHITE);
//
//            JButton btnPrint = createStyledButton("In Hóa Đơn", new Color(52, 152, 219), null);
//            btnPrint.addActionListener(e -> inHoaDon(hoaDon));
//
//            JButton btnClose = createStyledButton("Đóng", new Color(231, 76, 60), null);
//            btnClose.addActionListener(e -> ((JDialog) SwingUtilities.getWindowAncestor(panel)).dispose());
//
//            footerPanel.add(btnPrint);
//            footerPanel.add(btnClose);
//
//            // Thêm tất cả vào panel chính
//            panel.add(headerPanel, BorderLayout.NORTH);
//            JPanel centerPanel = new JPanel(new BorderLayout());
//            centerPanel.setBackground(Color.WHITE);
//            centerPanel.add(infoPanel, BorderLayout.NORTH);
//            centerPanel.add(detailContent, BorderLayout.CENTER);
//            panel.add(centerPanel, BorderLayout.CENTER);
//            panel.add(footerPanel, BorderLayout.SOUTH);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            JLabel lblError = new JLabel("Lỗi lấy dữ liệu: " + e.getMessage());
//            lblError.setFont(new Font("Arial", Font.BOLD, 14));
//            lblError.setForeground(Color.RED);
//            panel.add(lblError, BorderLayout.CENTER);
//        }
//
//        return panel;
//    }

    private JPanel createHoaDonDetailPanel(String maHoaDon) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        try {
            // Lấy thông tin chi tiết hóa đơn từ DAO
            HoaDon hoaDon = hoaDonDAO.getHoaDonByMa(maHoaDon);

            if (hoaDon == null) {
                JLabel lblError = new JLabel("Không tìm thấy thông tin hóa đơn: " + maHoaDon);
                lblError.setFont(new Font("Arial", Font.BOLD, 16));
                lblError.setForeground(Color.RED);
                panel.add(lblError, BorderLayout.CENTER);
                return panel;
            }

            // Header panel - thông tin cơ bản của hóa đơn
            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            headerPanel.setBackground(Color.WHITE);
            headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

            JLabel lblTitle = new JLabel("CHI TIẾT HÓA ĐƠN");
            lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
            lblTitle.setForeground(PRIMARY_COLOR);
            lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblInvoiceId = new JLabel("Mã Hóa Đơn: " + hoaDon.getMaHD());
            lblInvoiceId.setFont(new Font("Arial", Font.BOLD, 14));
            lblInvoiceId.setAlignmentX(Component.CENTER_ALIGNMENT);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            JLabel lblDate = new JLabel("Ngày lập: " + hoaDon.getNgayLap().format(formatter));
            lblDate.setFont(new Font("Arial", Font.PLAIN, 14));
            lblDate.setAlignmentX(Component.CENTER_ALIGNMENT);

            headerPanel.add(Box.createVerticalStrut(10));
            headerPanel.add(lblTitle);
            headerPanel.add(Box.createVerticalStrut(5));
            headerPanel.add(lblInvoiceId);
            headerPanel.add(Box.createVerticalStrut(5));
            headerPanel.add(lblDate);
            headerPanel.add(Box.createVerticalStrut(20));

            // Panel chứa thông tin khách hàng và nhân viên
            JPanel infoPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            infoPanel.setBackground(Color.WHITE);

            // Thông tin khách hàng
            JPanel customerPanel = new JPanel();
            customerPanel.setLayout(new BoxLayout(customerPanel, BoxLayout.Y_AXIS));
            customerPanel.setBackground(Color.WHITE);
            customerPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    "Thông tin khách hàng",
                    TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 14), Color.BLACK));

            KhachHang kh = hoaDon.getKhachHang();

            customerPanel.add(createInfoRow("Mã KH:", kh.getMaKhachHang()));
            customerPanel.add(createInfoRow("Tên KH:", kh.getTenKhachHang()));
            customerPanel.add(createInfoRow("SĐT:", kh.getSoDienThoai()));
            customerPanel.add(createInfoRow("Địa chỉ:", kh.getDiaChi()));
            customerPanel.add(createInfoRow("Điểm tích lũy:", String.valueOf(kh.getDiemTichLuy())));

            // Thông tin nhân viên và hóa đơn
            JPanel staffPanel = new JPanel();
            staffPanel.setLayout(new BoxLayout(staffPanel, BoxLayout.Y_AXIS));
            staffPanel.setBackground(Color.WHITE);
            staffPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    "Thông tin nhân viên và hóa đơn",
                    TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 14), Color.BLACK));

            NhanVien nv = hoaDon.getNv();

            staffPanel.add(createInfoRow("Mã NV:", nv.getMaNV()));
            staffPanel.add(createInfoRow("Tên NV:", nv.getTenNV()));
            staffPanel.add(createInfoRow("Loại HĐ:", hoaDon.getLoaiHoaDon().getTenLoaiHoaDon()));
            staffPanel.add(createInfoRow("Tổng tiền:", currencyFormat.format(hoaDon.getTongTien()) + " VNĐ"));
            staffPanel.add(createInfoRow("Tiền giảm:", currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ"));
            staffPanel.add(createInfoRow("Thanh toán:", currencyFormat.format(hoaDon.getTongTien() - hoaDon.getTienGiam()) + " VNĐ"));

            infoPanel.add(customerPanel);
            infoPanel.add(staffPanel);

            // Panel hiển thị chi tiết các vé trong hóa đơn
            JPanel detailContent = new JPanel(new BorderLayout());
            detailContent.setBackground(Color.WHITE);
            detailContent.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    "Chi tiết vé",
                    TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 14), Color.BLACK));

            // Bảng chi tiết vé
            DefaultTableModel detailModel = new DefaultTableModel();
            detailModel.addColumn("STT");
            detailModel.addColumn("Mã Vé");
            detailModel.addColumn("Tuyến Tàu");
            detailModel.addColumn("Ngày Đi");
            detailModel.addColumn("Loại Vé");
            detailModel.addColumn("Số Lượng");
            detailModel.addColumn("VAT");
            detailModel.addColumn("Thành Tiền");

            JTable detailTable = new JTable(detailModel);
            detailTable.setRowHeight(25);
            detailTable.setFont(new Font("Arial", Font.PLAIN, 13));
            detailTable.setGridColor(new Color(230, 230, 230));

            // Style header với màu chữ đen
            JTableHeader header = detailTable.getTableHeader();
            header.setFont(new Font("Arial", Font.BOLD, 13));
            header.setBackground(PRIMARY_COLOR);
            header.setForeground(Color.BLACK);
            header.setPreferredSize(new Dimension(header.getWidth(), 30));

            // Lấy chi tiết hóa đơn
            if (hoaDon.getChiTietHoaDons() != null) {
                int stt = 1;
                double tongTien = 0;

                // Thêm dữ liệu vào bảng chi tiết
                for (var chiTiet : hoaDon.getChiTietHoaDons()) {
                    if (chiTiet.getVeTau() != null) {
                        var ve = chiTiet.getVeTau();
                        String loaiVe = ve.getDoiTuong();

                        // Lấy thông tin ga từ server
                        String thongTinTuyen = "N/A";
                        try {
                            if (veTauDAO != null) {
                                Map<String, String> thongTinGa = veTauDAO.getThongTinGaByMaVe(ve.getMaVe());
                                if (thongTinGa != null && !thongTinGa.isEmpty()) {
                                    String gaDi = thongTinGa.get("gaDi");
                                    String gaDen = thongTinGa.get("gaDen");
                                    if (gaDi != null && gaDen != null) {
                                        thongTinTuyen = gaDi + " - " + gaDen;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            // Giữ giá trị mặc định "N/A"
                        }

                        detailModel.addRow(new Object[]{
                                stt++,
                                ve.getMaVe(),
                                thongTinTuyen,
                                ve.getNgayDi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                loaiVe,
                                chiTiet.getSoLuong(),
                                chiTiet.getVAT() + "%",
                                currencyFormat.format(chiTiet.getThanhTien()) + " VNĐ"
                        });

                        tongTien += chiTiet.getThanhTien();
                    }
                }

                // Thêm hàng tổng cộng
                detailModel.addRow(new Object[]{
                        "", "", "", "", "", "", "Tổng cộng:", currencyFormat.format(tongTien) + " VNĐ"
                });
            }

            JScrollPane detailScroll = new JScrollPane(detailTable);
            detailScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            detailContent.add(detailScroll, BorderLayout.CENTER);

            // Footer panel với các nút chức năng
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            footerPanel.setBackground(Color.WHITE);

            JButton btnPrint = createStyledButton("In Hóa Đơn", new Color(52, 152, 219), null);
            btnPrint.addActionListener(e -> inHoaDon(hoaDon));

            JButton btnClose = createStyledButton("Đóng", new Color(231, 76, 60), null);
            btnClose.addActionListener(e -> ((JDialog) SwingUtilities.getWindowAncestor(panel)).dispose());

            footerPanel.add(btnPrint);
            footerPanel.add(btnClose);

            // Thêm tất cả vào panel chính
            panel.add(headerPanel, BorderLayout.NORTH);
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setBackground(Color.WHITE);
            centerPanel.add(infoPanel, BorderLayout.NORTH);
            centerPanel.add(detailContent, BorderLayout.CENTER);
            panel.add(centerPanel, BorderLayout.CENTER);
            panel.add(footerPanel, BorderLayout.SOUTH);

        } catch (Exception e) {
            e.printStackTrace();
            JLabel lblError = new JLabel("Lỗi lấy dữ liệu: " + e.getMessage());
            lblError.setFont(new Font("Arial", Font.BOLD, 14));
            lblError.setForeground(Color.RED);
            panel.add(lblError, BorderLayout.CENTER);
        }

        return panel;
    }


//    private JPanel createInfoRow(String label, String value) {
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.setBackground(Color.WHITE);
//        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
//
//        JLabel lblLabel = new JLabel(label);
//        lblLabel.setFont(new Font("Arial", Font.BOLD, 13));
//        lblLabel.setPreferredSize(new Dimension(100, 20));
//
//        JLabel lblValue = new JLabel(value);
//        lblValue.setFont(new Font("Arial", Font.PLAIN, 13));
//
//        panel.add(lblLabel, BorderLayout.WEST);
//        panel.add(lblValue, BorderLayout.CENTER);
//
//        return panel;
//    }

    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Arial", Font.BOLD, 13));
        lblLabel.setPreferredSize(new Dimension(100, 20));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(lblLabel, BorderLayout.WEST);
        panel.add(lblValue, BorderLayout.CENTER);

        return panel;
    }

    //    private void xuatExcel() {
//        if (tableModel.getRowCount() == 0) {
//            JOptionPane.showMessageDialog(this,
//                    "Không có dữ liệu để xuất ra Excel.",
//                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//
//        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//            // Trong một ứng dụng thực tế, bạn sẽ sử dụng thư viện như Apache POI để tạo file Excel
//            JOptionPane.showMessageDialog(this,
//                    "Đã xuất dữ liệu thành công!\nFile được lưu tại: " +
//                            fileChooser.getSelectedFile().getAbsolutePath() + ".xlsx",
//                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//        }
//    }

//    private void xuatExcel() {
//        if (tableModel.getRowCount() == 0) {
//            JOptionPane.showMessageDialog(this,
//                    "Không có dữ liệu để xuất ra Excel.",
//                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//            return;
//        }
//
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        fileChooser.setSelectedFile(new File("DanhSachHoaDon.xlsx"));
//
//        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//
//            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
//                @Override
//                protected Boolean doInBackground() throws Exception {
//                    try {
//                        File file = fileChooser.getSelectedFile();
//                        // Đảm bảo file có phần mở rộng .xlsx
//                        if (!file.getName().endsWith(".xlsx")) {
//                            file = new File(file.getAbsolutePath() + ".xlsx");
//                        }
//
//                        // Trong thực tế, bạn sẽ sử dụng thư viện Apache POI hoặc tương tự
//                        // Đây chỉ là mã giả để mô phỏng thao tác xuất excel
//                        Thread.sleep(1000); // Giả lập thời gian xử lý
//
//                        return true;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return false;
//                    }
//                }
//
//                @Override
//                protected void done() {
//                    setCursor(Cursor.getDefaultCursor());
//                    try {
//                        boolean success = get();
//                        if (success) {
//                            JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
//                                    "Đã xuất dữ liệu thành công!\nFile được lưu tại: " +
//                                            fileChooser.getSelectedFile().getAbsolutePath(),
//                                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
//                        } else {
//                            JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
//                                    "Lỗi xuất file Excel!",
//                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
//                                "Lỗi: " + e.getMessage(),
//                                "Lỗi", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//            };
//
//            worker.execute();
//        }
//    }

    private void xuatExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu để xuất ra Excel.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("DanhSachHoaDon.xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        File file = fileChooser.getSelectedFile();
                        // Đảm bảo file có phần mở rộng .xlsx
                        if (!file.getName().endsWith(".xlsx")) {
                            file = new File(file.getAbsolutePath() + ".xlsx");
                        }

                        // Tạo workbook mới
                        XSSFWorkbook workbook = new XSSFWorkbook();

                        // Tạo style cho tiêu đề
                        XSSFCellStyle headerStyle = workbook.createCellStyle();
                        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        headerStyle.setAlignment(HorizontalAlignment.CENTER);
                        headerStyle.setBorderBottom(BorderStyle.THIN);
                        headerStyle.setBorderLeft(BorderStyle.THIN);
                        headerStyle.setBorderRight(BorderStyle.THIN);
                        headerStyle.setBorderTop(BorderStyle.THIN);

                        XSSFFont headerFont = workbook.createFont();
                        headerFont.setBold(true);
                        headerFont.setColor(IndexedColors.BLACK.getIndex());
                        headerStyle.setFont(headerFont);

                        // Tạo style cho nội dung
                        XSSFCellStyle contentStyle = workbook.createCellStyle();
                        contentStyle.setBorderBottom(BorderStyle.THIN);
                        contentStyle.setBorderLeft(BorderStyle.THIN);
                        contentStyle.setBorderRight(BorderStyle.THIN);
                        contentStyle.setBorderTop(BorderStyle.THIN);

                        // Tạo style cho số tiền
                        XSSFCellStyle currencyStyle = workbook.createCellStyle();
                        currencyStyle.setBorderBottom(BorderStyle.THIN);
                        currencyStyle.setBorderLeft(BorderStyle.THIN);
                        currencyStyle.setBorderRight(BorderStyle.THIN);
                        currencyStyle.setBorderTop(BorderStyle.THIN);
                        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0 \"VND\""));

                        // Tạo sheet tổng hợp hóa đơn
                        XSSFSheet summarySheet = workbook.createSheet("Danh sách hóa đơn");

                        // Tạo tiêu đề cho sheet tổng hợp
                        Row titleRow = summarySheet.createRow(0);
                        Cell titleCell = titleRow.createCell(0);
                        titleCell.setCellValue("DANH SÁCH HÓA ĐƠN");

                        // Style cho tiêu đề lớn
                        XSSFCellStyle titleStyle = workbook.createCellStyle();
                        titleStyle.setAlignment(HorizontalAlignment.CENTER);
                        XSSFFont titleFont = workbook.createFont();
                        titleFont.setBold(true);
                        titleFont.setFontHeightInPoints((short) 16);
                        titleStyle.setFont(titleFont);
                        titleCell.setCellStyle(titleStyle);

                        // Merge các ô để tạo tiêu đề lớn
                        summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

                        // Tạo tiêu đề cột
                        Row headerRow = summarySheet.createRow(2);

                        String[] headers = {
                                "Mã Hóa Đơn", "Ngày Lập", "Tên Khách Hàng", "SĐT Khách Hàng",
                                "Tên Nhân Viên", "Loại Hóa Đơn", "Tổng Tiền", "Tiền Giảm"
                        };

                        for (int i = 0; i < headers.length; i++) {
                            Cell cell = headerRow.createCell(i);
                            cell.setCellValue(headers[i]);
                            cell.setCellStyle(headerStyle);
                        }

                        // Thêm dữ liệu từ bảng vào sheet tổng hợp
                        List<String> maHoaDonList = new ArrayList<>();
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            Row row = summarySheet.createRow(i + 3);

                            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                Cell cell = row.createCell(j);
                                Object value = tableModel.getValueAt(i, j);

                                if (j == 0) { // Mã hóa đơn - lưu để tạo sheet chi tiết sau
                                    cell.setCellValue(value.toString());
                                    cell.setCellStyle(contentStyle);
                                    maHoaDonList.add(value.toString());
                                }
                                else if (j == 6 || j == 7) { // Cột tiền
                                    // Xử lý dữ liệu tiền đưa vào dạng số
                                    String moneyStr = value.toString().replace(" VNĐ", "").replace(",", "");
                                    try {
                                        double money = Double.parseDouble(moneyStr);
                                        cell.setCellValue(money);
                                        cell.setCellStyle(currencyStyle);
                                    } catch (NumberFormatException e) {
                                        cell.setCellValue(value.toString());
                                        cell.setCellStyle(contentStyle);
                                    }
                                }
                                else {
                                    cell.setCellValue(value.toString());
                                    cell.setCellStyle(contentStyle);
                                }
                            }
                        }

                        // Tự động điều chỉnh độ rộng cột
                        for (int i = 0; i < headers.length; i++) {
                            summarySheet.autoSizeColumn(i);
                        }

                        // Tạo các sheet chi tiết hóa đơn cho mỗi hóa đơn
                        for (String maHoaDon : maHoaDonList) {
                            try {
                                // Lấy thông tin chi tiết hóa đơn
                                HoaDon hoaDon = hoaDonDAO.getHoaDonByMa(maHoaDon);
                                if (hoaDon == null) continue;

                                // Tạo sheet chi tiết cho hóa đơn này
                                XSSFSheet detailSheet = workbook.createSheet("HD_" + maHoaDon);

                                // Tiêu đề sheet
                                Row detailTitleRow = detailSheet.createRow(0);
                                Cell detailTitleCell = detailTitleRow.createCell(0);
                                detailTitleCell.setCellValue("CHI TIẾT HÓA ĐƠN: " + maHoaDon);
                                detailTitleCell.setCellStyle(titleStyle);
                                detailSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

                                // Thông tin hóa đơn
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                                detailSheet.createRow(2).createCell(0).setCellValue("Ngày lập: " + hoaDon.getNgayLap().format(formatter));

                                // Thông tin khách hàng
                                Row khRow = detailSheet.createRow(4);
                                khRow.createCell(0).setCellValue("THÔNG TIN KHÁCH HÀNG");
                                khRow.getCell(0).setCellStyle(headerStyle);
                                detailSheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 6));

                                KhachHang kh = hoaDon.getKhachHang();
                                int rowKH = 5;
                                detailSheet.createRow(rowKH).createCell(0).setCellValue("Mã KH: " + kh.getMaKhachHang());
                                detailSheet.createRow(rowKH+1).createCell(0).setCellValue("Tên KH: " + kh.getTenKhachHang());
                                detailSheet.createRow(rowKH+2).createCell(0).setCellValue("SĐT: " + kh.getSoDienThoai());
                                detailSheet.createRow(rowKH+3).createCell(0).setCellValue("Địa chỉ: " + kh.getDiaChi());

                                // Thông tin nhân viên
                                Row nvRow = detailSheet.createRow(rowKH+5);
                                nvRow.createCell(0).setCellValue("THÔNG TIN NHÂN VIÊN");
                                nvRow.getCell(0).setCellStyle(headerStyle);
                                detailSheet.addMergedRegion(new CellRangeAddress(rowKH+5, rowKH+5, 0, 6));

                                NhanVien nv = hoaDon.getNv();
                                int rowNV = rowKH+6;
                                detailSheet.createRow(rowNV).createCell(0).setCellValue("Mã NV: " + nv.getMaNV());
                                detailSheet.createRow(rowNV+1).createCell(0).setCellValue("Tên NV: " + nv.getTenNV());
                                detailSheet.createRow(rowNV+2).createCell(0).setCellValue("Loại HĐ: " + hoaDon.getLoaiHoaDon().getTenLoaiHoaDon());
                                detailSheet.createRow(rowNV+3).createCell(0).setCellValue("Tổng tiền: " + currencyFormat.format(hoaDon.getTongTien()) + " VNĐ");
                                detailSheet.createRow(rowNV+4).createCell(0).setCellValue("Tiền giảm: " + currencyFormat.format(hoaDon.getTienGiam()) + " VNĐ");
                                detailSheet.createRow(rowNV+5).createCell(0).setCellValue("Thanh toán: " + currencyFormat.format(hoaDon.getTongTien() - hoaDon.getTienGiam()) + " VNĐ");

                                // Chi tiết vé
                                Row veRow = detailSheet.createRow(rowNV+7);
                                veRow.createCell(0).setCellValue("CHI TIẾT VÉ");
                                veRow.getCell(0).setCellStyle(headerStyle);
                                detailSheet.addMergedRegion(new CellRangeAddress(rowNV+7, rowNV+7, 0, 6));

                                // Tạo header cho bảng chi tiết vé
                                Row veHeaderRow = detailSheet.createRow(rowNV+9);
                                String[] veHeaders = {"STT", "Mã Vé", "Tuyến Tàu", "Ngày Đi", "Loại Vé", "VAT", "Thành Tiền"};

                                for (int i = 0; i < veHeaders.length; i++) {
                                    Cell cell = veHeaderRow.createCell(i);
                                    cell.setCellValue(veHeaders[i]);
                                    cell.setCellStyle(headerStyle);
                                }

                                // Thêm thông tin vé
                                if (hoaDon.getChiTietHoaDons() != null) {
                                    int stt = 1;
                                    int rowVe = rowNV+10;
                                    double tongTien = 0;

                                    for (var chiTiet : hoaDon.getChiTietHoaDons()) {
                                        if (chiTiet.getVeTau() != null) {
                                            var ve = chiTiet.getVeTau();
                                            Row dataRow = detailSheet.createRow(rowVe++);

                                            dataRow.createCell(0).setCellValue(stt++);
                                            dataRow.getCell(0).setCellStyle(contentStyle);

                                            dataRow.createCell(1).setCellValue(ve.getMaVe());
                                            dataRow.getCell(1).setCellStyle(contentStyle);

                                            // Lấy thông tin tuyến tàu
                                            String thongTinTuyen = "N/A";
                                            try {
                                                if (veTauDAO != null) {
                                                    Map<String, String> thongTinGa = veTauDAO.getThongTinGaByMaVe(ve.getMaVe());
                                                    if (thongTinGa != null && !thongTinGa.isEmpty()) {
                                                        String gaDi = thongTinGa.get("gaDi");
                                                        String gaDen = thongTinGa.get("gaDen");
                                                        if (gaDi != null && gaDen != null) {
                                                            thongTinTuyen = gaDi + " - " + gaDen;
                                                        }
                                                    }
                                                }
                                            } catch (Exception ex) {
                                                // Giữ giá trị mặc định "N/A"
                                            }

                                            dataRow.createCell(2).setCellValue(thongTinTuyen);
                                            dataRow.getCell(2).setCellStyle(contentStyle);

                                            dataRow.createCell(3).setCellValue(ve.getNgayDi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                                            dataRow.getCell(3).setCellStyle(contentStyle);

                                            dataRow.createCell(4).setCellValue(ve.getDoiTuong());
                                            dataRow.getCell(4).setCellStyle(contentStyle);

                                            dataRow.createCell(5).setCellValue(chiTiet.getVAT() + "%");
                                            dataRow.getCell(5).setCellStyle(contentStyle);

                                            Cell cellThanhTien = dataRow.createCell(6);
                                            cellThanhTien.setCellValue(chiTiet.getThanhTien());
                                            cellThanhTien.setCellStyle(currencyStyle);

                                            tongTien += chiTiet.getThanhTien();
                                        }
                                    }

                                    // Thêm dòng tổng cộng
                                    Row totalRow = detailSheet.createRow(rowVe);
                                    Cell cellTongTien = totalRow.createCell(6);
                                    cellTongTien.setCellValue(tongTien);
                                    cellTongTien.setCellStyle(currencyStyle);

                                    Cell cellLabel = totalRow.createCell(5);
                                    cellLabel.setCellValue("Tổng cộng:");
                                    XSSFCellStyle boldStyle = workbook.createCellStyle();
                                    boldStyle.cloneStyleFrom(contentStyle);
                                    XSSFFont boldFont = workbook.createFont();
                                    boldFont.setBold(true);
                                    boldStyle.setFont(boldFont);
                                    cellLabel.setCellStyle(boldStyle);
                                }

                                // Tự động điều chỉnh độ rộng cột
                                for (int i = 0; i < veHeaders.length; i++) {
                                    detailSheet.autoSizeColumn(i);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                // Bỏ qua hóa đơn này nếu có lỗi
                            }
                        }

                        // Ghi file
                        try (FileOutputStream outputStream = new FileOutputStream(file)) {
                            workbook.write(outputStream);
                        }
                        workbook.close();

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
                                    "Đã xuất dữ liệu thành công!\nFile được lưu tại: " +
                                            fileChooser.getSelectedFile().getAbsolutePath(),
                                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
                                    "Lỗi xuất file Excel!",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(TraCuuHoaDonPanel.this,
                                "Lỗi: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
        }
    }

    private void inHoaDon(HoaDon hoaDon) {
        try {
            // Đây là chức năng giả lập in hóa đơn
            // Trong thực tế, bạn sẽ dùng JasperReports hoặc các thư viện in ấn khác

            JOptionPane.showMessageDialog(this,
                    "Đang in hóa đơn " + hoaDon.getMaHD() + "...\nVui lòng kiểm tra máy in.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi in hóa đơn: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

}