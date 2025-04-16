package gui;

import dao.LichTrinhTauDAO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TrainScheduleGUI extends JFrame {
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JDateChooser dateChooser;
    private LichTrinhTauDAO lichTrinhTauDAO;
    
    public TrainScheduleGUI() {
        setTitle("Train Schedule Management");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        connectToRMI();
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        // Create table model with columns
        String[] columns = {"ID", "Train", "Departure", "Arrival", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        scheduleTable = new JTable(tableModel);
        
        // Create date chooser
        dateChooser = new JDateChooser();
        dateChooser.setDate(new java.util.Date());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create top panel with date chooser and search button
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Select Date: "));
        topPanel.add(dateChooser);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchSchedules());
        topPanel.add(searchButton);
        
        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        
        // Create bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        bottomPanel.add(refreshButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void connectToRMI() {
        try {
            Context context = new InitialContext();
            lichTrinhTauDAO = (LichTrinhTauDAO) context.lookup("rmi://MSI:9090/lichTrinhTauDAO");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error connecting to RMI server: " + e.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchSchedules() {
        try {
            // Clear existing table data
            tableModel.setRowCount(0);
            
            // Convert java.util.Date to LocalDate
            LocalDate selectedDate = LocalDate.parse(
                new java.text.SimpleDateFormat("yyyy-MM-dd")
                    .format(dateChooser.getDate())
            );
            
            // Fetch and display data
            lichTrinhTauDAO.getListLichTrinhTauByDate(selectedDate)
                .forEach(schedule -> {
                    // Add row to table (adjust according to your LichTrinhTau class structure)
                    tableModel.addRow(new Object[]{
                        schedule.getId(),
                        schedule.getTrain(),
                        schedule.getDeparture(),
                        schedule.getArrival(),
                        schedule.getDate(),
                        schedule.getStatus()
                    });
                });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error fetching schedules: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshData() {
        searchSchedules();
    }
}