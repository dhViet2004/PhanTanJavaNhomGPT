package gui;

import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {
    private JMenuBar menuBar;
    private JPanel mainPanel;

    public MainGUI() {
        setTitle("Train Management System");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // Initialize components
        createMenuBar();
        createMainPanel();
        
        // Set layout
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }

    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // Create File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // Create Management menu
        JMenu managementMenu = new JMenu("Management");
        JMenuItem scheduleItem = new JMenuItem("Train Schedule");
        scheduleItem.addActionListener(e -> openTrainSchedule());
        managementMenu.add(scheduleItem);
        
        // Add menus to menubar
        menuBar.add(fileMenu);
        menuBar.add(managementMenu);
        
        setJMenuBar(menuBar);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Add welcome label
        JLabel welcomeLabel = new JLabel("Welcome to Train Management System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(welcomeLabel, BorderLayout.CENTER);
    }

    private void openTrainSchedule() {
        TrainScheduleGUI scheduleGUI = new TrainScheduleGUI();
        scheduleGUI.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}