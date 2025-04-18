package guiClient;

import dao.*;
import dao.impl.KhachHangDAOImpl;
import dao.impl.LoaiKhachHangDAOImpl;
import dao.impl.VeTauDAOImpl;
import model.*;
import service.AITravelTimePredictor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QuanLyKhachHangPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(QuanLyKhachHangPanel.class.getName());
    private AITravelTimePredictor aiPredictor;
    private JTable customerTable, invoiceTable, ticketTable;
    private DefaultTableModel customerTableModel, invoiceTableModel, ticketTableModel;
    private JTextField searchField;
    private JComboBox<String> customerTypeFilter;
    private JButton updateButton, searchButton, resetFilterButton;
    private List<KhachHang> customerList;
    private List<LoaiKhachHang> customerTypeList;
    private LoaiKhachHangDAO loaiKhachHangDAO;
    private VeTauDAO veTauDAO;
    private KhachHangDAO khachHangDAO;
    private List<HoaDon> invoiceList;
    private List<VeTau> ticketList;
    private boolean isConnected = false;
    private HoaDonDAO hoaDonDAO;



    private void connectToRMIServer() {
        try {
            // Get the registry
            Registry registry = LocateRegistry.getRegistry("localhost", 9090);

            // Look up the remote objects
            khachHangDAO = (KhachHangDAO) registry.lookup("khachHangDAO");
            loaiKhachHangDAO = (LoaiKhachHangDAO) registry.lookup("loaiKhachHangDAO");
            hoaDonDAO = (HoaDonDAO) registry.lookup("hoaDonDAO");
            veTauDAO = (VeTauDAO) registry.lookup("veTauDAO");

            // Test the connection
            if (khachHangDAO.testConnection()) {
                isConnected = true;
                LOGGER.info("Kết nối RMI server thành công");
            } else {
                isConnected = false;
                LOGGER.warning("Kết nối RMI server thất bại trong quá trình kiểm tra");
            }
        } catch (Exception e) {
            isConnected = false;
            LOGGER.log(Level.SEVERE, "Lỗi kết nối RMI server: " + e.getMessage(), e);
        }
    }

    private void loadDataInBackground() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Kết nối đến RMI server
                connectToRMIServer();
                return isConnected;
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        try {
                            loadCustomerTypes();
                            loadCustomers();
                        } catch (RemoteException ex) {
                            LOGGER.log(Level.SEVERE, "Lỗi khi tải dữ liệu khách hàng", ex);
                            showErrorMessage("Không thể tải dữ liệu khách hàng", ex);
                        }
                    } else {
                        customerTableModel.setRowCount(0);
                        customerTableModel.addRow(new Object[]{"Lỗi kết nối", "Không thể kết nối tới server", "", ""});
                        showErrorMessage("Không thể kết nối đến máy chủ RMI", null);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi tải dữ liệu khách hàng", e);
                    customerTableModel.setRowCount(0);
                    customerTableModel.addRow(new Object[]{"Lỗi: " + e.getMessage(), "", "", ""});
                    showErrorMessage("Không thể tải dữ liệu khách hàng", e);
                }
            }
        };

        worker.execute();
    }

    private void reconnectAndLoadData() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                connectToRMIServer();
                return isConnected;
            }

            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    if (connected) {
                        try {
                            loadCustomers();
                        } catch (RemoteException ex) {
                            LOGGER.log(Level.SEVERE, "Lỗi khi tải lại dữ liệu khách hàng", ex);
                            showErrorMessage("Không thể tải lại dữ liệu khách hàng", ex);
                        }
                    } else {
                        showErrorMessage("Không thể kết nối đến máy chủ RMI", null);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Lỗi trong quá trình tái kết nối", e);
                    showErrorMessage("Lỗi trong quá trình tái kết nối", e);
                }
            }
        };

        worker.execute();
    }

    private void showErrorMessage(String message, Exception ex) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                message + (ex != null ? "\nChi tiết: " + ex.getMessage() : ""),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            );
        });
    }

    public QuanLyKhachHangPanel() throws RemoteException {
        setLayout(new BorderLayout());
        this.loaiKhachHangDAO = new LoaiKhachHangDAOImpl();
        this.khachHangDAO = new KhachHangDAOImpl();
        this.veTauDAO = new VeTauDAOImpl();


        // Connect to RMI server
        connectToRMIServer();

        // Add components to the panel
        // [existing UI setup code...]

        // Load initial data
        if (isConnected) {
            try {
                loadCustomerTypes();
                loadCustomers();
            } catch (RemoteException ex) {
                LOGGER.log(Level.SEVERE, "Error loading customer data", ex);
                showErrorMessage("Không thể tải dữ liệu khách hàng", ex);
            }
        } else {
            showErrorMessage("Không thể kết nối đến máy chủ", null);
        }

        // Start background loading task
        loadDataInBackground();

        // Top Panel: Search and Filter
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        customerTypeFilter = new JComboBox<>();
        resetFilterButton = new JButton("Reset Filter");
        topPanel.add(new JLabel("Search by Phone:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(new JLabel("Filter by Type:"));
        topPanel.add(customerTypeFilter);
        topPanel.add(resetFilterButton);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Customer Table
        customerTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Type"}, 0);
        customerTable = new JTable(customerTableModel);
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Bottom Panel: Invoice and Ticket Tables
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        invoiceTableModel = new DefaultTableModel(new String[]{"Invoice ID", "Date", "Total"}, 0);
        invoiceTable = new JTable(invoiceTableModel);
        bottomPanel.add(new JScrollPane(invoiceTable));

        ticketTableModel = new DefaultTableModel(new String[]{"Ticket ID", "Seat", "Price"}, 0);
        ticketTable = new JTable(ticketTableModel);
        bottomPanel.add(new JScrollPane(ticketTable));
        add(bottomPanel, BorderLayout.SOUTH);

        // Right Panel: Update Form
        JPanel rightPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<String> typeComboBox = new JComboBox<>();
        updateButton = new JButton("Update");
        rightPanel.add(new JLabel("ID:"));
        rightPanel.add(idField);
        rightPanel.add(new JLabel("Name:"));
        rightPanel.add(nameField);
        rightPanel.add(new JLabel("Phone:"));
        rightPanel.add(phoneField);
        rightPanel.add(new JLabel("Type:"));
        rightPanel.add(typeComboBox);
        rightPanel.add(new JLabel());
        rightPanel.add(updateButton);
        add(rightPanel, BorderLayout.EAST);

        // Load Data
        loadCustomerTypes();
        loadCustomers();

        // Event Listeners
        searchButton.addActionListener(e -> {
            try {
                searchCustomerByPhone();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        resetFilterButton.addActionListener(e -> {
            try {
                resetFilters();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        customerTypeFilter.addActionListener(e -> {
            try {
                filterCustomersByType();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            try {
                loadInvoicesForCustomer();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        invoiceTable.getSelectionModel().addListSelectionListener(e -> {
            try {
                loadTicketsForInvoice();
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        updateButton.addActionListener(e -> {
            try {
                updateCustomer(idField, nameField, phoneField, typeComboBox);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
    }



    private void loadCustomerTypes() throws RemoteException {
        // Load customer types into the filter and form combo box
        customerTypeList = loaiKhachHangDAO.getAll();
        customerTypeFilter.addItem("All");
        for (LoaiKhachHang type : customerTypeList) {
            customerTypeFilter.addItem(type.getTenLoaiKhachHang());
        }
    }

    private void loadCustomers() throws RemoteException {
        // Load all customers into the table
        customerList = khachHangDAO.getAll();
        customerTableModel.setRowCount(0);
        for (KhachHang customer : customerList) {
            customerTableModel.addRow(new Object[]{
                    customer.getMaKhachHang(),
                    customer.getTenKhachHang(),
                    customer.getSoDienThoai(),
                    customer.getLoaiKhachHang().getTenLoaiKhachHang()
            });
        }
    }

    private void searchCustomerByPhone() throws RemoteException {
        // Search customers by phone number
        String phone = searchField.getText();
        customerList = khachHangDAO.searchByPhone(phone);
        customerTableModel.setRowCount(0);
        for (KhachHang customer : customerList) {
            customerTableModel.addRow(new Object[]{
                    customer.getMaKhachHang(),
                    customer.getTenKhachHang(),
                    customer.getSoDienThoai(),
                    customer.getLoaiKhachHang().getTenLoaiKhachHang()
            });
        }
    }

    private void resetFilters() throws RemoteException {
        // Reset filters and reload all customers
        searchField.setText("");
        customerTypeFilter.setSelectedIndex(0);
        loadCustomers();
    }

    private void filterCustomersByType() throws RemoteException {
        // Filter customers by selected type
        String selectedType = (String) customerTypeFilter.getSelectedItem();
        if ("All".equals(selectedType)) {
            loadCustomers();
        } else {
            customerList = khachHangDAO.filterByType(selectedType);
            customerTableModel.setRowCount(0);
            for (KhachHang customer : customerList) {
                customerTableModel.addRow(new Object[]{
                        customer.getMaKhachHang(),
                        customer.getTenKhachHang(),
                        customer.getSoDienThoai(),
                        customer.getLoaiKhachHang().getTenLoaiKhachHang()
                });
            }
        }
    }

    private void loadInvoicesForCustomer() throws RemoteException {
        // Load invoices for the selected customer
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String customerId = (String) customerTableModel.getValueAt(selectedRow, 0);
            invoiceList = hoaDonDAO.getByCustomerId(customerId);
            invoiceTableModel.setRowCount(0);
            for (HoaDon invoice : invoiceList) {
                invoiceTableModel.addRow(new Object[]{
                        invoice.getMaHD(),
                        invoice.getNgayLap(),
                        invoice.getTongTien()
                });
            }
        }
    }

    private void loadTicketsForInvoice() throws RemoteException {
        // Load tickets for the selected invoice
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow >= 0) {
            String invoiceId = (String) invoiceTableModel.getValueAt(selectedRow, 0);
            ticketList = veTauDAO.getByInvoiceId(invoiceId);
            ticketTableModel.setRowCount(0);
            for (VeTau ticket : ticketList) {
                ticketTableModel.addRow(new Object[]{
                        ticket.getMaVe(),
                        ticket.getChoNgoi().getMaCho(),
                        ticket.getGiaVe()
                });
            }
        }
    }

    private void updateCustomer(JTextField idField, JTextField nameField, JTextField phoneField, JComboBox<String> typeComboBox) throws RemoteException {
        // Update customer information
        String id = idField.getText();
        String name = nameField.getText();
        String phone = phoneField.getText();
        String type = (String) typeComboBox.getSelectedItem();

        if (id.isEmpty() || name.isEmpty() || phone.isEmpty() || type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        KhachHang customer = new KhachHang();
        customer.setMaKhachHang(id);
        customer.setTenKhachHang(name);
        customer.setSoDienThoai(phone);
        customer.setLoaiKhachHang(customerTypeList.stream()
                .filter(t -> t.getTenLoaiKhachHang().equals(type))
                .findFirst()
                .orElse(null));

        boolean success = khachHangDAO.update(customer);
        if (success) {
            JOptionPane.showMessageDialog(this, "Customer updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCustomers();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update customer.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}

