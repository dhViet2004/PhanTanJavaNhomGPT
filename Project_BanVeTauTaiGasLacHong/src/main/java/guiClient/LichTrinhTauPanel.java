package guiClient;

import com.toedter.calendar.JDateChooser;
import dao.LichTrinhTauDAO;
import model.LichTrinhTau;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LichTrinhTauPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(LichTrinhTauPanel.class.getName());
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JDateChooser dateChooser;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JComboBox<String> filterComboBox;

    private LichTrinhTauDAO lichTrinhTauDAO;
    private boolean isConnected = false;

    public LichTrinhTauPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Connect to RMI server
        connectToRMIServer();

        // Add components to the panel
        add(createTitlePanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);

        // Load initial data
        if (isConnected) {
            try {
                loadAllScheduleData();
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, "Error loading schedule data", ex);
                showErrorMessage("Không thể tải dữ liệu lịch trình", ex);
            }
        } else {
            showErrorMessage("Không thể kết nối đến máy chủ", null);
        }
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("QUẢN LÝ LỊCH TRÌNH TÀU", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        panel.add(titleLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        panel.add(createSearchPanel(), BorderLayout.NORTH);
        panel.add(createTablePanel(), BorderLayout.CENTER);
        panel.add(createActionPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private void connectToRMIServer() {
        try {
            System.out.println("Đang kết nối đến RMI server...");

            // Sử dụng trực tiếp RMI registry thay vì JNDI
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9090);
            lichTrinhTauDAO = (LichTrinhTauDAO) registry.lookup("lichTrinhTauDAO");

            // Kiểm tra kết nối đến cơ sở dữ liệu
            try {
                boolean dbConnected = lichTrinhTauDAO.testConnection();
                if (dbConnected) {
                    isConnected = true;
                    LOGGER.info("Kết nối thành công đến RMI server và cơ sở dữ liệu");

                    // Kiểm tra và ghi log danh sách trạng thái
                    try {
                        List<String> statuses = lichTrinhTauDAO.getTrangThai();
                        LOGGER.info("Đã tải " + (statuses != null ? statuses.size() : 0) + " trạng thái từ cơ sở dữ liệu");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Lỗi khi tải danh sách trạng thái trong quá trình kết nối", e);
                    }
                } else {
                    isConnected = false;
                    LOGGER.warning("Kết nối thành công đến RMI server nhưng không thể kết nối đến cơ sở dữ liệu");
                    showErrorMessage("Kết nối đến RMI server thành công nhưng không thể kết nối đến cơ sở dữ liệu", null);
                }
            } catch (Exception e) {
                isConnected = false;
                LOGGER.log(Level.SEVERE, "Kiểm tra kết nối cơ sở dữ liệu thất bại", e);
                showErrorMessage("Kiểm tra kết nối cơ sở dữ liệu thất bại", e);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Không thể kết nối đến RMI server", ex);
            isConnected = false;
            showErrorMessage("Không thể kết nối đến RMI server: " + ex.getMessage(), ex);
        }
    }

    private JPanel createSearchPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(BorderFactory.createTitledBorder("Tìm Kiếm Lịch Trình"));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        // Date search components
        JLabel dateLabel = new JLabel("Ngày đi:");
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(new Date());
        dateChooser.setPreferredSize(new Dimension(150, 28));

        // Filter components
        JLabel filterLabel = new JLabel("Lọc theo:");

        // Tạo danh sách trạng thái từ cơ sở dữ liệu
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Tất cả"); // Luôn thêm lựa chọn "Tất cả"

        try {
            if (isConnected && lichTrinhTauDAO != null) {
                List<String> dbStatuses = lichTrinhTauDAO.getTrangThai();
                if (dbStatuses != null && !dbStatuses.isEmpty()) {
                    statusOptions.addAll(dbStatuses);
                    LOGGER.info("Đã tải thành công " + dbStatuses.size() + " trạng thái từ cơ sở dữ liệu");
                } else {
                    LOGGER.warning("Không tìm thấy trạng thái nào trong cơ sở dữ liệu");
                    // Thêm các trạng thái mặc định
                    statusOptions.add("Đã khởi hành");
                    statusOptions.add("Chưa khởi hành");
                    statusOptions.add("Đã hủy");
                }
            } else {
                LOGGER.warning("Chưa kết nối đến server hoặc lichTrinhTauDAO là null");
                // Thêm các trạng thái mặc định
                statusOptions.add("Đã khởi hành");
                statusOptions.add("Chưa khởi hành");
                statusOptions.add("Đã hủy");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi tải danh sách trạng thái: " + e.getMessage(), e);
            // Thêm các trạng thái mặc định nếu có lỗi xảy ra
            statusOptions.add("Đã khởi hành");
            statusOptions.add("Chưa khởi hành");
            statusOptions.add("Đã hủy");
        }

        filterComboBox = new JComboBox<>(statusOptions.toArray(new String[0]));
        filterComboBox.setPreferredSize(new Dimension(150, 28));

        // Search button with custom icon
        searchButton = new JButton("Tìm Kiếm");
        searchButton.setIcon(createSearchIcon(16, 16));
        searchButton.addActionListener(this::searchButtonClicked);

        // Refresh button with custom icon
        refreshButton = new JButton("Làm Mới");
        refreshButton.setIcon(createRefreshIcon(16, 16));
        refreshButton.addActionListener(e -> refreshData());

        // Add components to panel
        panel.add(dateLabel);
        panel.add(dateChooser);
        panel.add(filterLabel);
        panel.add(filterComboBox);
        panel.add(searchButton);
        panel.add(refreshButton);

        outerPanel.add(panel, BorderLayout.CENTER);
        return outerPanel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh Sách Lịch Trình"));

        // Create table model with non-editable cells
        String[] columns = {"ID", "Ngày Đi", "Mã Tàu - Tên Tàu", "Tuyến Đường", "Giờ Đi", "Giờ Đến", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create and configure table
        scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(25);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.setAutoCreateRowSorter(true);

        // Thiết lập màu nền cho hàng lẻ và hàng chẵn
        scheduleTable.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

        // Style the table header
        JTableHeader header = scheduleTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(new Color(41, 128, 185)); // Màu xanh dương cho header
        header.setForeground(Color.WHITE);  // Màu trắng cho chữ

        // Áp dụng custom UI cho bảng để có hiệu ứng hover
        setupTableUI();

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Đảm bảo hiển thị header đúng màu sắc
        scrollPane.setColumnHeaderView(header);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void setupTableUI() {
        // Đặt một số thuộc tính cho bảng
        scheduleTable.setShowHorizontalLines(true);
        scheduleTable.setShowVerticalLines(true);
        scheduleTable.setGridColor(new Color(230, 230, 230));
        scheduleTable.setBackground(Color.WHITE);
        scheduleTable.setForeground(Color.BLACK);
        scheduleTable.setSelectionBackground(new Color(66, 139, 202)); // Màu khi chọn
        scheduleTable.setSelectionForeground(Color.WHITE);

        // Thêm hiệu ứng hover bằng cách sử dụng MouseAdapter
        scheduleTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                Point point = e.getPoint();
                int row = scheduleTable.rowAtPoint(point);

                if (row >= 0) {
                    // Đặt hàng đang hover thành màu xanh nhạt
                    if (scheduleTable.getSelectedRow() != row) {
                        scheduleTable.clearSelection();
                        scheduleTable.addRowSelectionInterval(row, row);
                        scheduleTable.setSelectionBackground(new Color(173, 216, 230)); // Màu xanh dương nhạt
                        scheduleTable.setSelectionForeground(Color.BLACK);
                    }
                }
            }
        });

        scheduleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Khi chuột rời khỏi bảng, xóa hiệu ứng hover
                scheduleTable.clearSelection();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = scheduleTable.getSelectedRow();
                if (row >= 0) {
                    // Khi click, đặt màu chọn thành màu xanh đậm
                    scheduleTable.setSelectionBackground(new Color(66, 139, 202));
                    scheduleTable.setSelectionForeground(Color.WHITE);
                }
            }
        });

        // Thiết lập UI cho header
        scheduleTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setBackground(new Color(41, 128, 185));
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Create action buttons with custom icons
        addButton = new JButton("Thêm Lịch Trình");
        addButton.setIcon(createAddIcon(16, 16));
        addButton.addActionListener(e -> addSchedule());

        editButton = new JButton("Chỉnh Sửa");
        editButton.setIcon(createEditIcon(16, 16));
        editButton.addActionListener(e -> editSchedule());

        deleteButton = new JButton("Xóa");
        deleteButton.setIcon(createDeleteIcon(16, 16));
        deleteButton.addActionListener(e -> deleteSchedule());

        // Add buttons to panel
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);

        return panel;
    }

    // Method to create a search icon
    private Icon createSearchIcon(int width, int height) {
        return new ImageIcon(createIconImage(width, height, new Color(41, 128, 185), icon -> {
            // Draw a magnifying glass
            Graphics2D g2 = icon;
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(3, 3, 7, 7);
            g2.drawLine(9, 9, 13, 13);
        }));
    }

    // Method to create a refresh icon
    private Icon createRefreshIcon(int width, int height) {
        return new ImageIcon(createIconImage(width, height, new Color(41, 128, 185), icon -> {
            // Draw circular arrows
            Graphics2D g2 = icon;
            g2.setStroke(new BasicStroke(2));
            g2.drawArc(3, 3, 10, 10, 0, 270);
            g2.drawLine(13, 8, 13, 3);
            g2.drawLine(13, 3, 8, 3);
        }));
    }

    // Method to create an add icon (plus sign)
    private Icon createAddIcon(int width, int height) {
        return new ImageIcon(createIconImage(width, height, new Color(46, 204, 113), icon -> {
            // Draw a plus sign
            Graphics2D g2 = icon;
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(8, 4, 8, 12);
            g2.drawLine(4, 8, 12, 8);
        }));
    }

    // Method to create an edit icon
    private Icon createEditIcon(int width, int height) {
        return new ImageIcon(createIconImage(width, height, new Color(243, 156, 18), icon -> {
            // Draw a pencil
            Graphics2D g2 = icon;
            g2.setStroke(new BasicStroke(1));
            g2.drawLine(3, 13, 13, 3);
            g2.drawLine(13, 3, 11, 1);
            g2.drawLine(3, 13, 1, 11);
            g2.drawLine(1, 11, 3, 3);
        }));
    }

    // Method to create a delete icon
    private Icon createDeleteIcon(int width, int height) {
        return new ImageIcon(createIconImage(width, height, new Color(231, 76, 60), icon -> {
            // Draw a trash can
            Graphics2D g2 = icon;
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(4, 5, 8, 10);
            g2.drawLine(3, 5, 13, 5);
            g2.drawLine(6, 3, 10, 3);
            g2.drawLine(6, 5, 6, 3);
            g2.drawLine(10, 5, 10, 3);
            g2.drawLine(6, 8, 6, 12);
            g2.drawLine(10, 8, 10, 12);
        }));
    }

    // Helper method to create icon images
    private Image createIconImage(int width, int height, Color color, DrawIcon drawer) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        // Set up high quality rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Draw the icon
        g2.setColor(color);
        drawer.draw(g2);
        g2.dispose();

        return image;
    }

    // Interface for drawing icons
    private interface DrawIcon {
        void draw(Graphics2D g2);
    }

    private void searchButtonClicked(ActionEvent e) {
        try {
            Date selectedDate = dateChooser.getDate();
            if (selectedDate == null) {
                throw new IllegalArgumentException("Vui lòng chọn ngày hợp lệ.");
            }

            // Convert java.util.Date to java.time.LocalDate
            LocalDate localDate = selectedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            System.out.println("Đang tìm kiếm cho ngày: " + localDate);

            if (isConnected) {
                loadScheduleData(localDate);
            } else {
                reconnectAndLoadData(localDate);
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "Invalid date selection", ex);
            showErrorMessage("Vui lòng chọn ngày hợp lệ.", null);
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, "Error loading schedule data", ex);
            showErrorMessage("Lỗi khi tải dữ liệu", ex);
        }
    }

    private void loadScheduleData(LocalDate date) throws RemoteException {
        if (!isConnected || lichTrinhTauDAO == null) {
            reconnectAndLoadData(date);
            if (!isConnected) {
                throw new RemoteException("Not connected to RMI server");
            }
        }

        tableModel.setRowCount(0);

        try {
            System.out.println("Đang tìm kiếm với ngày: " + date);
            List<LichTrinhTau> schedules = lichTrinhTauDAO.getListLichTrinhTauByDate(date);
            System.out.println("Kết quả trả về: " + (schedules != null ? schedules.size() + " lịch" : "null"));

            if (schedules == null || schedules.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có lịch trình nào cho ngày: " + date,
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Apply filter if selected
            String filterOption = (String) filterComboBox.getSelectedItem();
            for (LichTrinhTau schedule : schedules) {
                if (matchesFilter(schedule, filterOption)) {
                    tableModel.addRow(createTableRow(schedule));
                }
            }

        } catch (Exception e) {
            System.out.println("Lỗi chi tiết: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tìm kiếm: " + e.getMessage(), e);
        }
    }

    private boolean matchesFilter(LichTrinhTau schedule, String filterOption) {
        if (filterOption == null || filterOption.equals("Tất cả")) {
            return true;
        }

        // So sánh trực tiếp với trạng thái
        return filterOption.equals(schedule.getTrangThai());
    }

    private Object[] createTableRow(LichTrinhTau schedule) {
        return new Object[]{
                schedule.getMaLich(),
                schedule.getNgayDi().toString(),
                schedule.getTau().getMaTau() + " - " + schedule.getTau().getTenTau(),
                "TT" + schedule.getTau().getMaTau() + " - " +
                        schedule.getTau().getTuyenTau().getGaDi() + " - " +
                        schedule.getTau().getTuyenTau().getGaDen(),
                schedule.getGioDi().toString(),
                schedule.getGioDi().plusHours(estimateTravelTime(schedule)).toString(),
                schedule.getTrangThai()
        };
    }

    private int estimateTravelTime(LichTrinhTau schedule) {
        // This is a placeholder for estimating travel time based on route distance
        return 2; // Default 2 hours
    }

    private void reconnectAndLoadData(LocalDate localDate) {
        connectToRMIServer();
        if (isConnected) {
            try {
                loadScheduleData(localDate);
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, "Failed to load data after reconnection", ex);
                showErrorMessage("Không thể tải dữ liệu sau khi kết nối lại", ex);
            }
        } else {
            showErrorMessage("Không thể kết nối đến server", null);
        }
    }

    private void refreshData() {
        try {
            if (isConnected) {
                loadAllScheduleData();
            } else {
                connectToRMIServer();
                if (isConnected) {
                    loadAllScheduleData();
                } else {
                    showErrorMessage("Không thể kết nối đến server", null);
                }
            }
        } catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, "Error refreshing data", ex);
            showErrorMessage("Lỗi khi làm mới dữ liệu", ex);
        }
    }

    private void loadAllScheduleData() throws RemoteException {
        if (!isConnected || lichTrinhTauDAO == null) {
            connectToRMIServer();
            if (!isConnected) {
                throw new RemoteException("Not connected to RMI server");
            }
        }

        tableModel.setRowCount(0);

        try {
            List<LichTrinhTau> schedules = lichTrinhTauDAO.getAllList();

            if (schedules == null || schedules.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không có lịch trình nào để hiển thị.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Apply filter if selected
            String filterOption = (String) filterComboBox.getSelectedItem();
            for (LichTrinhTau schedule : schedules) {
                if (matchesFilter(schedule, filterOption)) {
                    tableModel.addRow(createTableRow(schedule));
                }
            }

        } catch (Exception e) {
            System.out.println("Lỗi chi tiết khi tải tất cả dữ liệu: " + e.getMessage());
            e.printStackTrace();
            throw new RemoteException("Lỗi khi tải dữ liệu: " + e.getMessage(), e);
        }
    }

    private void addSchedule() {
        JOptionPane.showMessageDialog(this,
                "Chức năng thêm lịch trình sẽ được triển khai trong phiên bản tiếp theo.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void editSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn lịch trình để chỉnh sửa.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object scheduleId = scheduleTable.getValueAt(selectedRow, 0);

        JOptionPane.showMessageDialog(this,
                "Chức năng chỉnh sửa lịch trình sẽ được triển khai trong phiên bản tiếp theo.",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn lịch trình để xóa.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object scheduleId = scheduleTable.getValueAt(selectedRow, 0);

        int option = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa lịch trình này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "Chức năng xóa lịch trình sẽ được triển khai trong phiên bản tiếp theo.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showErrorMessage(String message, Exception ex) {
        String fullMessage = message;
        if (ex != null) {
            fullMessage += ": " + ex.getMessage();
        }

        JOptionPane.showMessageDialog(this,
                fullMessage,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Custom renderer để hiển thị màu sắc cho hàng lẻ và hàng chẵn
     */
    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component comp = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                // Khi hàng được chọn
                comp.setBackground(table.getSelectionBackground());
                comp.setForeground(table.getSelectionForeground());
            } else {
                // Màu nền cho các hàng khác nhau
                if (row % 2 == 0) {
                    comp.setBackground(Color.WHITE);
                } else {
                    comp.setBackground(new Color(245, 245, 245)); // Màu xám rất nhạt
                }
                comp.setForeground(Color.BLACK);
            }

            // Canh lề và font
            ((JLabel) comp).setHorizontalAlignment(SwingConstants.CENTER);
            comp.setFont(new Font("Arial", Font.PLAIN, 12));

            return comp;
        }
    }
}