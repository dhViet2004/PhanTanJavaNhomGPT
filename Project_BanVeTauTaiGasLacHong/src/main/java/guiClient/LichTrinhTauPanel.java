package guiClient;

import dao.LichTrinhTauDAO;
import model.LichTrinhTau;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LichTrinhTauPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(LichTrinhTauPanel.class.getName());

    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JTextField dateField, trainIdField, routeIdField, departTimeField, arriveTimeField;
    private JButton searchButton, addButton, updateButton, deleteButton, clearButton;

    private LichTrinhTauDAO lichTrinhTauDAO;
    private boolean isConnected = false;

    public LichTrinhTauPanel() {
        setLayout(new BorderLayout());

        // Connect to RMI server
        connectToRMIServer();

        // Create search panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // Create table panel with pagination
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);

        // Create form panel for data entry
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.SOUTH);

        // Load initial data
        if (isConnected) {
            try {
                // Load all train schedules
                loadAllScheduleData();
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, "Error loading schedule data", ex);
                // Fallback to dummy data if there's an error
                loadDummyData();
            }
        } else {
            // Fallback to dummy data if not connected
            loadDummyData();
        }
    }

    /**
     * Connect to the RMI server
     */
    private void connectToRMIServer() {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            props.put(Context.PROVIDER_URL, "rmi://localhost:9090");

            Context context = new InitialContext(props);
            lichTrinhTauDAO = (LichTrinhTauDAO) context.lookup("lichTrinhTauDAO");
            isConnected = true;
            LOGGER.info("Connected to RMI server successfully");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to connect to RMI server: " + ex.getMessage(), ex);
            isConnected = false;
        }
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Tìm Kiếm"));

        JLabel dateLabel = new JLabel("Ngày (yyyy-MM-dd):");
        dateField = new JTextField(10);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateField.setText(dateFormat.format(new Date()));

        searchButton = new JButton("Tìm Kiếm");
        searchButton.addActionListener(e -> {
            try {
                String dateStr = dateField.getText();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateStr);

                // Convert java.util.Date to java.time.LocalDate
                LocalDate localDate = LocalDate.parse(dateStr);

                if (isConnected) {
                    try {
                        loadScheduleData(localDate);
                    } catch (RemoteException ex) {
                        LOGGER.log(Level.WARNING, "Error loading schedule data by date", ex);
                        // Try to reconnect
                        connectToRMIServer();
                        if (isConnected) {
                            try {
                                loadScheduleData(localDate);
                            } catch (RemoteException ex2) {
                                LOGGER.log(Level.SEVERE, "Failed to load data after reconnection", ex2);
                                JOptionPane.showMessageDialog(this,
                                        "Không thể tải dữ liệu từ server. Sử dụng dữ liệu mẫu.",
                                        "Lỗi Kết Nối",
                                        JOptionPane.ERROR_MESSAGE);
                                loadDummyData();
                            }
                        } else {
                            loadDummyData();
                        }
                    }
                } else {
                    // Try to connect
                    connectToRMIServer();
                    if (isConnected) {
                        try {
                            loadScheduleData(localDate);
                        } catch (RemoteException ex) {
                            LOGGER.log(Level.SEVERE, "Failed to load data after connection", ex);
                            loadDummyData();
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Không thể kết nối đến server. Sử dụng dữ liệu mẫu.",
                                "Lỗi Kết Nối",
                                JOptionPane.WARNING_MESSAGE);
                        loadDummyData();
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error parsing date", ex);
                JOptionPane.showMessageDialog(this,
                        "Định dạng ngày không hợp lệ. Vui lòng sử dụng định dạng yyyy-MM-dd",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(dateLabel);
        panel.add(dateField);
        panel.add(searchButton);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Danh Sách Lịch Trình"));

        String[] columns = {"ID", "Ngày", "Tàu", "Tuyến", "Giờ Đi", "Giờ Đến", "Trạng Thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable editing in table cells
            }
        };

        scheduleTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(scheduleTable);

        scheduleTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && scheduleTable.getSelectedRow() != -1) {
                int row = scheduleTable.getSelectedRow();
                populateFormFromSelectedRow(row);
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton prevButton = new JButton("<< Trước");
        JButton nextButton = new JButton("Tiếp >>");

        prevButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chuyển trang trước (đang phát triển)", "Thông Báo", JOptionPane.INFORMATION_MESSAGE));
        nextButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chuyển trang tiếp theo (đang phát triển)", "Thông Báo", JOptionPane.INFORMATION_MESSAGE));

        paginationPanel.add(prevButton);
        paginationPanel.add(nextButton);

        panel.add(paginationPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Thông Tin Lịch Trình"));

        JPanel formFields = new JPanel(new GridLayout(3, 4, 10, 10));
        formFields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formFields.add(new JLabel("ID Tàu:"));
        trainIdField = new JTextField(10);
        formFields.add(trainIdField);

        formFields.add(new JLabel("ID Tuyến:"));
        routeIdField = new JTextField(10);
        formFields.add(routeIdField);

        formFields.add(new JLabel("Giờ Đi (HH:mm):"));
        departTimeField = new JTextField(10);
        formFields.add(departTimeField);

        formFields.add(new JLabel("Giờ Đến (HH:mm):"));
        arriveTimeField = new JTextField(10);
        formFields.add(arriveTimeField);

        panel.add(formFields, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        addButton = new JButton("Thêm");
        addButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Chức năng thêm lịch trình đang được phát triển",
                "Thông Báo",
                JOptionPane.INFORMATION_MESSAGE));

        updateButton = new JButton("Cập Nhật");
        updateButton.addActionListener(e -> {
            if (scheduleTable.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một lịch trình để cập nhật",
                        "Thông Báo",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cập nhật lịch trình thành công (đang phát triển)",
                        "Thông Báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        deleteButton = new JButton("Xóa");
        deleteButton.addActionListener(e -> {
            if (scheduleTable.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn một lịch trình để xóa",
                        "Thông Báo",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn xóa lịch trình này không?",
                        "Xác Nhận Xóa",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this,
                            "Xóa lịch trình thành công (đang phát triển)",
                            "Thông Báo",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        clearButton = new JButton("Làm Mới");
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Load all schedule data from the DAO
     * @throws RemoteException If there's an error communicating with the RMI server
     */
    private void loadAllScheduleData() throws RemoteException {
        if (!isConnected || lichTrinhTauDAO == null) {
            throw new RemoteException("Not connected to RMI server");
        }

        // Clear existing data
        tableModel.setRowCount(0);

        // Get all data from DAO
        List<LichTrinhTau> schedules = lichTrinhTauDAO.getAllList();

        if (schedules.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có lịch trình nào trong hệ thống",
                    "Thông Báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Add data to table
        for (LichTrinhTau schedule : schedules) {
            Object[] row = {
                schedule.getMaLich(),
                schedule.getNgayDi().toString(),
                schedule.getTau().getMaTau()+ " - " + schedule.getTau().getTenTau(),
                "TT" + schedule.getTau().getMaTau() + " - " + schedule.getTau().getTuyenTau().getGaDi() + " - " + schedule.getTau().getTuyenTau().getGaDen(),
                schedule.getGioDi().toString(),
                "20:00", // Assuming arrival time is not directly available
                schedule.getTrangThai()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Load schedule data from the DAO based on the given date
     * @param date The date to load schedules for
     * @throws RemoteException If there's an error communicating with the RMI server
     */
    private void loadScheduleData(LocalDate date) throws RemoteException {
        if (!isConnected || lichTrinhTauDAO == null) {
            throw new RemoteException("Not connected to RMI server");
        }

        // Clear existing data
        tableModel.setRowCount(0);

        // Get data from DAO
        List<LichTrinhTau> schedules = lichTrinhTauDAO.getListLichTrinhTauByDate(date);

        if (schedules.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có lịch trình nào cho ngày " + date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    "Thông Báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Add data to table
        for (LichTrinhTau schedule : schedules) {
            Object[] row = {
                schedule.getMaLich(),
                schedule.getNgayDi().toString(),
                schedule.getTau().getMaTau()+ " - " + schedule.getTau().getTenTau(),
                "TT" + schedule.getTau().getMaTau() + " - " + schedule.getTau().getTuyenTau().getGaDi() + " - " + schedule.getTau().getTuyenTau().getGaDen(),
                schedule.getGioDi().toString(),
                "20:00", // Assuming arrival time is not directly available
                schedule.getTrangThai()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Load dummy data when RMI connection fails
     */
    private void loadDummyData() {
        tableModel.setRowCount(0);

        Object[][] dummyData = {
                {"LT001", "2025-01-24", "T001 - Tàu Hỏa Sài Gòn", "TT001 - Sài Gòn - Hà Nội", "08:00", "20:00", "Hoạt động"},
                {"LT002", "2025-01-24", "T002 - Tàu Hỏa Thống Nhất", "TT002 - Sài Gòn - Đà Nẵng", "09:30", "16:45", "Hoạt động"},
                {"LT003", "2025-01-24", "T003 - Tàu Hỏa Bắc Nam", "TT003 - Hà Nội - Sài Gòn", "07:15", "19:30", "Hoạt động"},
                {"LT004", "2025-01-25", "T001 - Tàu Hỏa Sài Gòn", "TT001 - Sài Gòn - Hà Nội", "08:00", "20:00", "Hoạt động"},
                {"LT005", "2025-01-25", "T002 - Tàu Hỏa Thống Nhất", "TT002 - Sài Gòn - Đà Nẵng", "09:30", "16:45", "Không hoạt động"}
        };

        for (Object[] row : dummyData) {
            tableModel.addRow(row);
        }
    }

    private void populateFormFromSelectedRow(int row) {
        if (row >= 0) {
            String trainInfo = tableModel.getValueAt(row, 2).toString();
            String routeInfo = tableModel.getValueAt(row, 3).toString();
            String departTime = tableModel.getValueAt(row, 4).toString();
            String arriveTime = tableModel.getValueAt(row, 5).toString();

            String trainId = trainInfo.split(" - ")[0];
            String routeId = routeInfo.split(" - ")[0];

            trainIdField.setText(trainId);
            routeIdField.setText(routeId);
            departTimeField.setText(departTime);
            arriveTimeField.setText(arriveTime);
        }
    }

    private void clearForm() {
        trainIdField.setText("");
        routeIdField.setText("");
        departTimeField.setText("");
        arriveTimeField.setText("");
        scheduleTable.clearSelection();
    }
}