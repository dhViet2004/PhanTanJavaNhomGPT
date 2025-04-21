package guiClient;

import dao.*;
import dao.impl.*;
import model.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Checkout interface for train ticket booking system
 * Displays selected tickets and collects passenger information
 * @author luongtan204
 */
public class ThanhToanGUI extends JFrame {
    // Main panels
    private JPanel mainPanel;
    private JTable ticketTable;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField nameField;
    private JTextField idCardField;
    private JTextField emailField;
    private JTextField confirmEmailField;
    private JTextField phoneField;
    private JTextField promotionField;

    // Summary fields
    private JLabel totalAmountLabel;
    private double totalAmount = 0.0;

    // Data
    private Map<String, String> ticketsMap; // Map of seat IDs to schedule IDs
    private Color primaryColor = new Color(0, 136, 204);

    // DAOs
    private LichTrinhTauDAO lichTrinhTauDAO;
    private ChoNgoiDAO choNgoiDAO;
    private ToaTauDAO toaTauDAO;
    private TauDAO tauDAO;

    /**
     * Constructor
     * @param ticketsMap Map of seat IDs to schedule IDs
     */
    public ThanhToanGUI(Map<String, String> ticketsMap) throws RemoteException {
        this.ticketsMap = ticketsMap;

        // Initialize DAOs
        lichTrinhTauDAO = new LichTrinhTauDAOImpl();
        choNgoiDAO = new ChoNgoiDAOImpl();
        toaTauDAO = new ToaTauDAOImpl();
        tauDAO = new TauDAOImpl();

        setTitle("Thanh toán vé tàu");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        calculateTotal();
    }

    /**
     * Initialize components
     */
    private void initComponents() {
        // Main container with padding
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Center panel containing tickets table and bottom controls
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));

        // Create tickets table
        createTicketsTable();
        JScrollPane tableScrollPane = new JScrollPane(ticketTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Bottom panel for buttons and discount
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));

        // Left - Remove all tickets button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton removeAllButton = new JButton("Xóa tất cả các vé");
        removeAllButton.setIcon(createTrashIcon());
        removeAllButton.addActionListener(e -> removeAllTickets());
        buttonPanel.add(removeAllButton);
        bottomPanel.add(buttonPanel, BorderLayout.WEST);

        // Center - Discount code field
        JPanel promotionPanel = new JPanel(new BorderLayout(5, 0));
        promotionField = new JTextField();
        promotionField.setPreferredSize(new Dimension(200, 30));
        JLabel promoLabel = new JLabel("Nhập mã giảm giá tại đây");
        promoLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        promotionPanel.add(promoLabel, BorderLayout.WEST);
        promotionPanel.add(promotionField, BorderLayout.CENTER);

        // Apply button
        JButton applyButton = new JButton("Áp dụng");
        applyButton.addActionListener(e -> applyPromotion());
        promotionPanel.add(applyButton, BorderLayout.EAST);
        bottomPanel.add(promotionPanel, BorderLayout.CENTER);

        // Right - Total amount
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel totalLabel = new JLabel("Tổng tiền");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalAmountLabel = new JLabel();
        totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalPanel.add(totalLabel);
        totalPanel.add(totalAmountLabel);
        bottomPanel.add(totalPanel, BorderLayout.EAST);

        // Add bottom panel to center panel
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Add center panel to main panel
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Create south panel for customer info and payment button
        JPanel southPanel = new JPanel(new BorderLayout(0, 10));

        // Customer information section
        JPanel customerPanel = createCustomerInfoPanel();
        southPanel.add(customerPanel, BorderLayout.NORTH);

        // Payment button
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton payButton = new JButton("Thanh toán");
        payButton.setBackground(primaryColor);
        payButton.setForeground(Color.WHITE);
        payButton.setFont(new Font("Arial", Font.BOLD, 14));
        payButton.setPreferredSize(new Dimension(120, 35));
        payButton.addActionListener(e -> processPayment());
        paymentPanel.add(payButton);
        southPanel.add(paymentPanel, BorderLayout.SOUTH);

        // Add south panel to main panel
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);
    }

    /**
     * Create title panel
     */
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Thanh toán vé tàu", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create tickets table
     */
    private void createTicketsTable() {
        // Define table columns
        String[] columns = {
                "Họ tên", "Thông tin chỗ", "Giá vé", "VAT",
                "Khuyến mại", "Bảo hiểm", "Thành tiền", ""
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 7; // Only name and delete button columns are editable
            }
        };

        // Create table
        ticketTable = new JTable(tableModel);
        ticketTable.setRowHeight(150);
        ticketTable.setShowVerticalLines(true);
        ticketTable.setGridColor(new Color(230, 230, 230));

        // Configure column widths
        ticketTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        ticketTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        ticketTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        ticketTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        ticketTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        ticketTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        ticketTable.getColumnModel().getColumn(7).setPreferredWidth(40);

        // Add table data for each ticket
        try {
            for (Map.Entry<String, String> entry : ticketsMap.entrySet()) {
                String seatId = entry.getKey();
                String lichTrinhId = entry.getValue();

                // Get ticket information from database using DAOs
                addTicketToTable(seatId, lichTrinhId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi tải thông tin vé: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }

        // Set custom renderer and editor for the Họ tên (name) column
        ticketTable.getColumnModel().getColumn(0).setCellRenderer(new PassengerInfoRenderer());
        ticketTable.getColumnModel().getColumn(0).setCellEditor(new PassengerInfoEditor());

        // Add delete button renderer and editor
        ticketTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        ticketTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    /**
     * Add a ticket to the table using seatId and scheduleId
     */
    private void addTicketToTable(String seatId, String scheduleId) {
        try {
            // Get seat information
            ChoNgoi choNgoi = choNgoiDAO.getById(seatId);
            if (choNgoi == null) {
                System.err.println("Không tìm thấy thông tin ghế: " + seatId);
                return;
            }

            // Get schedule information
            LichTrinhTau lichTrinh = lichTrinhTauDAO.getById(scheduleId);
            if (lichTrinh == null) {
                System.err.println("Không tìm thấy thông tin lịch trình: " + scheduleId);
                return;
            }

            // Get toa information (from seat's toaId)
            String toaId = choNgoi.getToaTau().getMaToa();
            ToaTau toaTau = toaTauDAO.getToaTauById(toaId);
            if (toaTau == null) {
                System.err.println("Không tìm thấy thông tin toa: " + toaId);
                return;
            }

            // Get train information
            Tau tau = tauDAO.getTauByLichTrinhTau(lichTrinh);
            if (tau == null) {
                System.err.println("Không tìm thấy thông tin tàu cho lịch trình: " + scheduleId);
                return;
            }

            // Get seat name/number
            String seatName = choNgoi.getTenCho() != null ? choNgoi.getTenCho() : seatId;

            // Get price based on seat class and route
            double price = getSeatPrice(choNgoi, toaTau);

            // Get route information
            String from = lichTrinh.getTau().getTuyenTau().getGaDi();
            String to = lichTrinh.getTau().getTuyenTau().getGaDen();

            // Format date and time
            String departDate = lichTrinh.getNgayDi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String departTime = lichTrinh.getGioDi().format(DateTimeFormatter.ofPattern("HH:mm"));

            // Create seat info text matching the image format
            String seatInfo = String.format("<html>" +
                            "Ghế: <b>%s</b><br>" +
                            "Toa: <b>Toa %s</b><br>" +
                            "Tàu: <b>Tàu %s</b><br>" +
                            "Tuyến: <b>%s → %s</b><br>" +
                            "Khởi hành: <b>%s %s</b></html>",
                    seatName, toaTau.getTenToa(), tau.getTenTau(), from, to, departDate, departTime);

            // Format price
            String priceStr = formatCurrency(price);

            // VAT calculation (fixed at 10%)
            String vatStr = "10%";

            // Promotion (empty for now)
            String promotion = "";

            // Insurance
            double insurance = 1000;
            String insuranceStr = formatCurrency(insurance);

            // Calculate total (price + VAT + insurance)
            double totalForTicket = price + (price * 0.1) + insurance;
            String totalStr = formatCurrency(totalForTicket);

            // Create passenger info component - will be replaced by the custom renderer
            PassengerInfo passengerInfo = new PassengerInfo();

            // Add row to table
            tableModel.addRow(new Object[] {
                    passengerInfo, seatInfo, priceStr, vatStr,
                    promotion, insuranceStr, totalStr, "X"
            });

        } catch (Exception e) {
            System.err.println("Lỗi khi thêm vé vào bảng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Passenger information class to hold data
     */
    class PassengerInfo {
        String name = "";
        String idNumber = "";
        String passengerType = "Người lớn"; // Default to adult
        int age = 0;

        public PassengerInfo() {}

        public PassengerInfo(String name, String idNumber, String passengerType) {
            this.name = name;
            this.idNumber = idNumber;
            this.passengerType = passengerType;
        }

        // Get discount percentage based on passenger type
        public double getDiscountPercentage() {
            switch (passengerType) {
                case "Sinh viên":
                    return 0.10; // 10% discount for students
                case "Trẻ em":
                    return 0.25; // 25% discount for children
                case "Người cao tuổi":
                    return 0.15; // 15% discount for elderly
                default:
                    return 0.0;  // No discount for regular adults
            }
        }
    }

    /**
     * Renderer for passenger information cell
     */
    class PassengerInfoRenderer extends JPanel implements TableCellRenderer {
        private JTextField nameField = new JTextField();
        private JComboBox<String> typeCombo = new JComboBox<>(
                new String[]{"Người lớn", "Sinh viên", "Trẻ em", "Người cao tuổi"});
        private JTextField idField = new JTextField();
        private JTextField ageField = new JTextField();

        public PassengerInfoRenderer() {
            setLayout(new GridLayout(4, 1, 0, 2));
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            nameField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Họ tên",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            typeCombo.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Đối tượng",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            idField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Số giấy tờ",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            ageField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Tuổi (cho trẻ em)",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            add(nameField);
            add(typeCombo);
            add(idField);
            add(ageField);

            setBackground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof PassengerInfo) {
                PassengerInfo info = (PassengerInfo) value;
                nameField.setText(info.name);
                idField.setText(info.idNumber);
                typeCombo.setSelectedItem(info.passengerType);
                ageField.setText(info.age > 0 ? String.valueOf(info.age) : "");
            }

            return this;
        }
    }

    /**
     * Editor for passenger information cell
     */
    class PassengerInfoEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel = new JPanel();
        private JTextField nameField = new JTextField();
        private JComboBox<String> typeCombo = new JComboBox<>(
                new String[]{"Người lớn", "Sinh viên", "Trẻ em", "Người cao tuổi"});
        private JTextField idField = new JTextField();
        private JTextField ageField = new JTextField();
        private PassengerInfo currentInfo = new PassengerInfo();

        public PassengerInfoEditor() {
            panel.setLayout(new GridLayout(4, 1, 0, 2));
            panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            nameField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Họ tên",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            typeCombo.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Đối tượng",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            idField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Số giấy tờ",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            ageField.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    "Tuổi (cho trẻ em)",
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.TOP,
                    new Font("Arial", Font.PLAIN, 10),
                    new Color(100, 100, 100)
            ));

            panel.add(nameField);
            panel.add(typeCombo);
            panel.add(idField);
            panel.add(ageField);

            panel.setBackground(Color.WHITE);

            // Add listeners
            nameField.addActionListener(e -> updateCurrentInfo());
            typeCombo.addActionListener(e -> {
                updateCurrentInfo();
                // Enable age field only for children
                ageField.setEnabled("Trẻ em".equals(typeCombo.getSelectedItem()));

                // Recalculate totals when passenger type changes
                if (ticketTable.isEditing()) {
                    stopEditing();
                    calculateTotal();
                }
            });
            idField.addActionListener(e -> updateCurrentInfo());
            ageField.addActionListener(e -> updateCurrentInfo());

            // Add focus listeners
            nameField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateCurrentInfo();
                }
            });

            idField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateCurrentInfo();
                }
            });

            ageField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateCurrentInfo();
                }
            });

            // Initially disable age field unless "Trẻ em" is selected
            ageField.setEnabled(false);
        }

        private void stopEditing() {
            if (ticketTable.isEditing()) {
                TableCellEditor editor = ticketTable.getCellEditor();
                if (editor != null) {
                    editor.stopCellEditing();
                }
            }
        }

        private void updateCurrentInfo() {
            currentInfo.name = nameField.getText();
            currentInfo.idNumber = idField.getText();
            currentInfo.passengerType = (String) typeCombo.getSelectedItem();

            try {
                currentInfo.age = ageField.getText().isEmpty() ? 0 : Integer.parseInt(ageField.getText());
            } catch (NumberFormatException e) {
                currentInfo.age = 0;
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            if (value instanceof PassengerInfo) {
                currentInfo = (PassengerInfo) value;
                nameField.setText(currentInfo.name);
                idField.setText(currentInfo.idNumber);
                typeCombo.setSelectedItem(currentInfo.passengerType);
                ageField.setText(currentInfo.age > 0 ? String.valueOf(currentInfo.age) : "");
                ageField.setEnabled("Trẻ em".equals(currentInfo.passengerType));
            }

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            updateCurrentInfo();
            return currentInfo;
        }
    }

    /**
     * Calculate seat price based on seat and car type
     */
    private double getSeatPrice(ChoNgoi choNgoi, ToaTau toaTau) {
        // Base price for regular seats
        double basePrice = 250000.0;

        try {
            // Apply multiplier for VIP cars
            if (isVipCar(toaTau)) {
                basePrice *= 1.5; // 50% premium for VIP seats
            }
        } catch (Exception e) {
            System.err.println("Error getting seat price: " + e.getMessage());
        }

        return basePrice;
    }

    /**
     * Determine if a car is VIP based on loaiToa
     */
    private boolean isVipCar(ToaTau toaTau) {
        if (toaTau.getLoaiToa() == null) return false;

        LoaiToa loaiToa = toaTau.getLoaiToa();
        String tenLoai = loaiToa.getTenLoai();

        // Check if the type name contains VIP or premium indicators
        return tenLoai != null && (
                tenLoai.contains("VIP") ||
                        tenLoai.contains("Cao cấp") ||
                        tenLoai.contains("Đặc biệt")
        );
    }

    /**
     * Create customer information panel
     */
    private JPanel createCustomerInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        ));

        // Form panel with grid layout to match the image
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 1: Full name and ID number
        // Full name label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.15;
        JLabel nameLabel = new JLabel();
        nameLabel.setText("<html>Họ và tên<span style='color:red'>*</span></html>");
        formPanel.add(nameLabel, gbc);

        // Full name field
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        nameField = new JTextField();
        formPanel.add(nameField, gbc);

        // ID card label
        gbc.gridx = 2;
        gbc.weightx = 0.15;
        JLabel idLabel = new JLabel();
        idLabel.setText("<html>Số CMND/Hộ chiếu<span style='color:red'>*</span></html>");
        formPanel.add(idLabel, gbc);

        // ID card field
        gbc.gridx = 3;
        gbc.weightx = 0.35;
        idCardField = new JTextField();
        formPanel.add(idCardField, gbc);

        // Row 2: Email and Confirm email
        // Email label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        JLabel emailLabel = new JLabel("Email để nhận vé điện tử");
        formPanel.add(emailLabel, gbc);

        // Email field
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        emailField = new JTextField();
        formPanel.add(emailField, gbc);

        // Confirm email label
        gbc.gridx = 2;
        gbc.weightx = 0.15;
        JLabel confirmEmailLabel = new JLabel("Xác nhận email");
        formPanel.add(confirmEmailLabel, gbc);

        // Confirm email field
        gbc.gridx = 3;
        gbc.weightx = 0.35;
        confirmEmailField = new JTextField();
        formPanel.add(confirmEmailField, gbc);

        // Row 3: Phone number
        // Phone label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.15;
        JLabel phoneLabel = new JLabel();
        phoneLabel.setText("<html>Số di động<span style='color:red'>*</span></html>");
        formPanel.add(phoneLabel, gbc);

        // Phone field
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        phoneField = new JTextField();
        formPanel.add(phoneField, gbc);

        // Set consistent font and field sizes
        Font labelFont = new Font("Arial", Font.PLAIN, 12);
        nameLabel.setFont(labelFont);
        idLabel.setFont(labelFont);
        emailLabel.setFont(labelFont);
        confirmEmailLabel.setFont(labelFont);
        phoneLabel.setFont(labelFont);

        Dimension fieldSize = new Dimension(0, 30);
        nameField.setPreferredSize(fieldSize);
        idCardField.setPreferredSize(fieldSize);
        emailField.setPreferredSize(fieldSize);
        confirmEmailField.setPreferredSize(fieldSize);
        phoneField.setPreferredSize(fieldSize);

        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Format currency
     */
    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        return format.format(amount);
    }

    /**
     * Calculate total amount and update discount based on passenger type
     */
    private void calculateTotal() {
        totalAmount = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            // Get base price (without VAT) from the third column
            String priceStr = (String) tableModel.getValueAt(i, 2);
            double basePrice = 0.0;

            try {
                // Parse the formatted number by removing commas
                basePrice = Double.parseDouble(priceStr.replace(",", ""));
            } catch (Exception e) {
                System.err.println("Error parsing price: " + e.getMessage());
                continue;
            }

            // Get passenger info to check for discounts
            PassengerInfo info = (PassengerInfo) tableModel.getValueAt(i, 0);
            double discountPercentage = info.getDiscountPercentage();

            // Calculate discount amount
            double discountAmount = basePrice * discountPercentage;

            // Calculate VAT (10%)
            double vat = basePrice * 0.1;

            // Get insurance amount
            String insuranceStr = (String) tableModel.getValueAt(i, 5);
            double insurance = 0.0;
            try {
                insurance = Double.parseDouble(insuranceStr.replace(",", ""));
            } catch (Exception e) {
                System.err.println("Error parsing insurance: " + e.getMessage());
            }

            // Calculate ticket total: base price + VAT - discount + insurance
            double ticketTotal = basePrice + vat - discountAmount + insurance;

            // Update the row with new values
            String discountStr = discountAmount > 0 ?
                    String.format("-%s (%s%%)", formatCurrency(discountAmount),
                            (int)(discountPercentage * 100)) : "";

            tableModel.setValueAt(discountStr, i, 4); // Update promotion column with discount
            tableModel.setValueAt(formatCurrency(ticketTotal), i, 6); // Update total column

            totalAmount += ticketTotal;
        }

        totalAmountLabel.setText(formatCurrency(totalAmount));
    }

    /**
     * Remove all tickets
     */
    private void removeAllTickets() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa tất cả vé?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.setRowCount(0);
            ticketsMap.clear();
            calculateTotal();
        }
    }

    /**
     * Apply promotion code
     */
    private void applyPromotion() {
        String code = promotionField.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập mã khuyến mãi",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // In a real app, you would validate the code against a database
        // For now, just show a message
        JOptionPane.showMessageDialog(
                this,
                "Mã khuyến mãi không hợp lệ hoặc đã hết hạn",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Process payment
     */
    private void processPayment() {
        // Validate required fields
        if (nameField.getText().trim().isEmpty() ||
                idCardField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng điền đầy đủ các trường bắt buộc",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Validate email match if provided
        if (!emailField.getText().trim().isEmpty() &&
                !emailField.getText().equals(confirmEmailField.getText())) {

            JOptionPane.showMessageDialog(
                    this,
                    "Email xác nhận không khớp với email đã nhập",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Validate passenger information is entered for all tickets
        boolean missingPassengerInfo = false;
        boolean invalidChildAge = false;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object cellValue = tableModel.getValueAt(i, 0);
            if (cellValue instanceof PassengerInfo) {
                PassengerInfo info = (PassengerInfo) cellValue;

                // Check if required fields are filled
                if (info.name.trim().isEmpty() || info.idNumber.trim().isEmpty()) {
                    missingPassengerInfo = true;
                    break;
                }

                // Validate child age
                if ("Trẻ em".equals(info.passengerType) && (info.age <= 0 || info.age >= 10)) {
                    invalidChildAge = true;
                    break;
                }
            }
        }

        if (missingPassengerInfo) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng điền đầy đủ thông tin hành khách cho tất cả các vé",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (invalidChildAge) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đối tượng 'Trẻ em' phải có tuổi từ 1-9",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Show payment options dialog
        String[] options = {"Thẻ tín dụng", "Chuyển khoản", "Ví điện tử"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Chọn phương thức thanh toán",
                "Thanh toán",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        // Process based on selected payment method
        if (choice >= 0) {
            // Simulate payment processing
            JOptionPane.showMessageDialog(
                    this,
                    "Đang xử lý thanh toán...",
                    "Thanh toán",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Simulate success
            JOptionPane.showMessageDialog(
                    this,
                    "Thanh toán thành công! Vé của bạn đã được đặt.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Close checkout window
            dispose();
        }
    }

    /**
     * Create trash icon for buttons
     */
    private ImageIcon createTrashIcon() {
        return new ImageIcon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();

                // Draw simple trash icon
                g2d.setColor(Color.RED);

                // Draw trash can
                g2d.fillRect(x + 3, y + 6, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x + 5, y + 8, 6, 6);

                // Draw lid
                g2d.setColor(Color.RED);
                g2d.fillRect(x + 1, y + 3, 14, 3);

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    /**
     * Button renderer for the delete column
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setBackground(Color.WHITE);
            setIcon(createTrashIcon());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /**
     * Button editor for the delete column
     */
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setBackground(Color.WHITE);
            button.setIcon(createTrashIcon());

            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Remove the ticket at the specified row
                if (currentRow >= 0 && currentRow < tableModel.getRowCount()) {
                    tableModel.removeRow(currentRow);
                    calculateTotal();
                }
            }
            isPushed = false;
            return "";
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}