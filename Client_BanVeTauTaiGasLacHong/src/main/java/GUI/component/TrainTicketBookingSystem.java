package GUI.component;

import com.toedter.calendar.JDateChooser;
import dao.*;
import model.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Train Ticket Booking System GUI
 * Allows users to search for trains, view cars, select seats, and book tickets
 * @author luongtan204
 */
public class TrainTicketBookingSystem extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(TrainTicketBookingSystem.class.getName());
    // Địa chỉ IP và port của RMI server
    private static final String RMI_SERVER_IP = "192.168.113.105";
    private static final int RMI_SERVER_PORT = 9090;
    private final String sessionId;
    // UI Components
    private JPanel seatsPanel;
    private JPanel carsPanel;
    private JPanel trainsPanel;
    private JScrollPane trainsScrollPane;
    private JScrollPane carsScrollPane;
    private JLabel seatingSectionLabel;
    private JLabel totalLabel;

    // Form fields
    private JTextField departureField;
    private JTextField arrivalField;
    private JTextField departureDateField;
    private JTextField returnDateField;
    private JRadioButton oneWayRadio;
    private JRadioButton roundTripRadio;

    // State variables
    private String selectedTrainId = "";   // Compound ID for selection (train code + time)
    private String currentTrainId = "";    // Just the train code
    private String currentToaId = "";      // Car ID
    private String currentMaLich = "";     // Schedule ID for seat loading
    private ArrayList<TicketItem> cartItems = new ArrayList<>();
    private double totalAmount = 0.0;

    // Seat status constants
    private static final String STATUS_AVAILABLE = "Trống";
    private static final String STATUS_BOOKED = "Đã đặt";
    private static final String STATUS_PENDING = "Chờ xác nhận";

    // Seat colors
    private static final Color SEAT_AVAILABLE_COLOR = Color.WHITE;
    private static final Color SEAT_BOOKED_COLOR = Color.LIGHT_GRAY;
    private static final Color SEAT_PENDING_COLOR = Color.YELLOW;

    private Map<String, Timer> reservationTimers = new HashMap<>(); // Store timers for each reserved seat
    private Map<String, Integer> remainingTimes = new HashMap<>();  // Store remaining seconds for each reservation
    private static final int RESERVATION_TIMEOUT = 300; // 300 seconds (5 minutes)

    // Visual effects
    private Color activeColor = new Color(0, 136, 204);
    private Color inactiveColor = new Color(153, 153, 153);
    private Color hoverColor = new Color(51, 153, 255);
    private Border activeBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2), // Gold border
            BorderFactory.createEmptyBorder(1, 1, 1, 1)
    );
    private Border normalBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);

    // DAOs
    private LichTrinhTauDAO lichTrinhTauDAO;
    private TauDAO tauDAO;
    private ToaTauDAO toaTauDAO;
    private ChoNgoiDAO choNgoiDAO;
    private KhuyenMaiDAO khuyenMaiDAO;
    private NhanVienDAO nhanVienDAO;
    private  ChoNgoiDoiVeDAO choNgoiGiuDAO;

    // Employee information
    private NhanVien nhanVien;

    // Asynchronous processing
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private JProgressBar progressLoading;
    private JLabel lblStatus;

    // Timer for periodic seat status refresh
    private Timer seatStatusRefreshTimer;
    private static final int SEAT_REFRESH_INTERVAL = 3000; // 3 seconds

    private String activeSeatId = null; // Track the currently active seat
    private Set<String> selectedSeatIds = new HashSet<>(); // Track all selected seats

    /**
     * Constructor - initializes the application
     * @param nv The employee who is using the system
     */
    public TrainTicketBookingSystem(NhanVien nv) throws RemoteException {
        this.nhanVien = nv;
        this.sessionId = UUID.randomUUID().toString();
        // Initialize RMI services
        try {
            initRMIServices();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize RMI services", e);
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối đến máy chủ: " + e.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            return; // Dừng nếu không kết nối được
        }

        // Set layout for this panel - remove gaps between components
        this.setLayout(new BorderLayout(0, 0));

        // Main panel (center content) - add a border for separation
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(200, 200, 200)), // Thin line borders on left and right
                BorderFactory.createEmptyBorder(10, 10, 10, 10)                       // Inner padding
        ));

        // Create loading panel with progress indicator and status
        JPanel loadingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        progressLoading = new JProgressBar();
        progressLoading.setIndeterminate(true);
        progressLoading.setVisible(false);
        progressLoading.setPreferredSize(new Dimension(100, 20));
        lblStatus = new JLabel("Sẵn sàng");
        loadingPanel.add(lblStatus);
        loadingPanel.add(progressLoading);

        // Create header panel with loading indicator
        JPanel headerWithLoading = new JPanel(new BorderLayout());
        headerWithLoading.add(createHeaderPanel(), BorderLayout.CENTER);
        headerWithLoading.add(loadingPanel, BorderLayout.EAST);

        // Add components to main panel
        mainPanel.add(headerWithLoading, BorderLayout.NORTH);
        mainPanel.add(createTrainsPanel(), BorderLayout.CENTER);

        // Left panel - Trip Information
        JPanel leftPanel = createTripInfoPanel();
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0)); // Right padding is 0

        // Right panel - Ticket Cart
        JPanel rightPanel = createTicketCartPanel();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10)); // Left padding is 0

        // Add all panels to this panel
        this.add(leftPanel, BorderLayout.WEST);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(rightPanel, BorderLayout.EAST);
    }
    private void initRMIServices() throws Exception {
        LOGGER.info("Initializing RMI services...");
        setLoading(true, "Đang kết nối đến máy chủ...");

        // Use CompletableFuture to connect to RMI registry asynchronously
        CompletableFuture<Registry> registryFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry(RMI_SERVER_IP, RMI_SERVER_PORT);
                LOGGER.info("Connected to RMI registry at " + RMI_SERVER_IP + ":" + RMI_SERVER_PORT);
                return registry;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to RMI registry", e);
                throw new RuntimeException("Không thể kết nối đến máy chủ RMI: " + e.getMessage(), e);
            }
        }, executorService);

        // Wait for registry connection to complete
        Registry registry;
        try {
            registry = registryFuture.join();
        } catch (Exception e) {
            setLoading(false, "Lỗi kết nối");
            throw new Exception("Không thể kết nối đến máy chủ RMI: " + e.getCause().getMessage(), e);
        }

        // Create a list of futures for each service lookup
        List<CompletableFuture<Void>> serviceFutures = new ArrayList<>();
        final boolean[] anyServiceConnected = {false};
        StringBuilder errorMessages = new StringBuilder();

        // LichTrinhTauDAO
        serviceFutures.add(CompletableFuture.runAsync(() -> {
            try {
                setLoading(true, "Đang kết nối LichTrinhTauDAO...");
                lichTrinhTauDAO = (LichTrinhTauDAO) registry.lookup("lichTrinhTauDAO");
                LOGGER.info("LichTrinhTauDAO service connected successfully");
                anyServiceConnected[0] = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to LichTrinhTauDAO service", e);
                synchronized (errorMessages) {
                    errorMessages.append("LichTrinhTauDAO: ").append(e.getMessage()).append("\n");
                }
            }
        }, executorService));

        // TauDAO
        serviceFutures.add(CompletableFuture.runAsync(() -> {
            try {
                setLoading(true, "Đang kết nối TauDAO...");
                tauDAO = (TauDAO) registry.lookup("tauDAO");
                LOGGER.info("TauDAO service connected successfully");
                anyServiceConnected[0] = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to TauDAO service", e);
                synchronized (errorMessages) {
                    errorMessages.append("TauDAO: ").append(e.getMessage()).append("\n");
                }
            }
        }, executorService));

        // ToaTauDAO
        serviceFutures.add(CompletableFuture.runAsync(() -> {
            try {
                setLoading(true, "Đang kết nối ToaTauDAO...");
                toaTauDAO = (ToaTauDAO) registry.lookup("toaTauDAO");
                LOGGER.info("ToaTauDAO service connected successfully");
                anyServiceConnected[0] = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to ToaTauDAO service", e);
                synchronized (errorMessages) {
                    errorMessages.append("ToaTauDAO: ").append(e.getMessage()).append("\n");
                }
            }
        }, executorService));

        // ChoNgoiDAO
        serviceFutures.add(CompletableFuture.runAsync(() -> {
            try {
                setLoading(true, "Đang kết nối ChoNgoiDAO...");
                choNgoiDAO = (ChoNgoiDAO) registry.lookup("choNgoiDAO");
                LOGGER.info("ChoNgoiDAO service connected successfully");
                anyServiceConnected[0] = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to ChoNgoiDAO service", e);
                synchronized (errorMessages) {
                    errorMessages.append("ChoNgoiDAO: ").append(e.getMessage()).append("\n");
                }
            }
        }, executorService));

        // KhuyenMaiDAO
        serviceFutures.add(CompletableFuture.runAsync(() -> {
            try {
                setLoading(true, "Đang kết nối KhuyenMaiDAO...");
                khuyenMaiDAO = (KhuyenMaiDAO) registry.lookup("KhuyenMaiDAO");
                LOGGER.info("KhuyenMaiDAO service connected successfully");
                anyServiceConnected[0] = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to KhuyenMaiDAO service", e);
                synchronized (errorMessages) {
                    errorMessages.append("KhuyenMaiDAO: ").append(e.getMessage()).append("\n");
                }
            }
        }, executorService));

        // NhanVienDAO
        serviceFutures.add(CompletableFuture.runAsync(() -> {
            try {
                setLoading(true, "Đang kết nối NhanVienDAO...");
                nhanVienDAO = (NhanVienDAO) registry.lookup("nhanVienDAO");
                LOGGER.info("NhanVienDAO service connected successfully");
                anyServiceConnected[0] = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to connect to NhanVienDAO service", e);
                synchronized (errorMessages) {
                    errorMessages.append("NhanVienDAO: ").append(e.getMessage()).append("\n");
                }
            }
        }, executorService));

        //    registry.rebind("choNgoiDoiVeDAO", choNgoiDoiVeDAO);

        serviceFutures.add(CompletableFuture.runAsync(() -> {
            try {
                setLoading(true, "Đang kết nối ChoNgoiGiuDAO...");
                choNgoiGiuDAO = (ChoNgoiDoiVeDAO) registry.lookup("choNgoiDoiVeDAO");
                LOGGER.info("choNgoiDoiVeDAO service connected successfully");
                anyServiceConnected[0] = true;

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService));

        // Remove callback registration and setup polling instead
        LOGGER.info("Using polling mechanism for seat status updates");

        // Wait for all service lookups to complete
        CompletableFuture.allOf(serviceFutures.toArray(new CompletableFuture[0])).join();

        // Check if any services were connected
        setLoading(false, "Kết nối hoàn tất");
        if (!anyServiceConnected[0]) {
            LOGGER.severe("Failed to connect to any RMI services");
            throw new Exception("Không thể kết nối đến bất kỳ dịch vụ RMI nào:\n" + errorMessages);
        } else if (errorMessages.length() > 0) {
            // Some services failed but not all
            LOGGER.warning("Some RMI services failed to connect: " + errorMessages);
            JOptionPane.showMessageDialog(this,
                    "Kết nối thành công một phần. Một số tính năng có thể không hoạt động:\n" + errorMessages,
                    "Cảnh báo kết nối", JOptionPane.WARNING_MESSAGE);
        } else {
            LOGGER.info("All RMI services initialized successfully");
        }
    }

    /**
     * Creates the header panel with route information
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());

        JLabel headerLabel = new JLabel("Chiều đi: ngày 20/04/2025 từ Sài Gòn đến Hà Nội");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(new EmptyBorder(10, 20, 10, 10));

        JPanel bluePanel = new JPanel();
        bluePanel.setLayout(new BorderLayout());
        bluePanel.setBackground(activeColor);
        bluePanel.add(headerLabel, BorderLayout.CENTER);

        // Add "arrow" shape to the right
        JPanel arrowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(activeColor);
                int[] xPoints = {0, getWidth() - 20, getWidth(), getWidth() - 20, 0};
                int[] yPoints = {0, 0, getHeight()/2, getHeight(), getHeight()};
                g2d.fillPolygon(xPoints, yPoints, 5);
            }
        };
        arrowPanel.setOpaque(false);
        arrowPanel.setPreferredSize(new Dimension(30, 50));

        bluePanel.add(arrowPanel, BorderLayout.EAST);
        headerPanel.add(bluePanel, BorderLayout.NORTH);

        return headerPanel;
    }

    /**
     * Creates the panel that displays trains and their details
     */
    private JPanel createTrainsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Train options cards panel (initially empty)
        trainsPanel = new JPanel();
        trainsPanel.setLayout(new BoxLayout(trainsPanel, BoxLayout.X_AXIS));

        // Add an empty placeholder panel with instructions
        JPanel placeholderPanel = new JPanel(new BorderLayout());
        placeholderPanel.setPreferredSize(new Dimension(400, 150));
        JLabel placeholderLabel = new JLabel("Vui lòng tìm kiếm chuyến tàu", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        placeholderLabel.setForeground(Color.GRAY);
        placeholderPanel.add(placeholderLabel, BorderLayout.CENTER);
        trainsPanel.add(placeholderPanel);

        // Add ScrollPane for trains panel
        trainsScrollPane = new JScrollPane(trainsPanel);
        trainsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        trainsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        trainsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // Set explicit height for train panel
        trainsScrollPane.setPreferredSize(new Dimension(mainPanel.getWidth(), 180));

        mainPanel.add(trainsScrollPane, BorderLayout.NORTH);

        // Train cars diagram - initially empty
        carsPanel = new JPanel();
        carsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Add ScrollPane for cars panel
        carsScrollPane = new JScrollPane(carsPanel);
        carsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        carsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        carsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        carsScrollPane.setPreferredSize(new Dimension(mainPanel.getWidth(), 80));

        // Wrap the ScrollPane in a panel with padding
        JPanel carsContainer = new JPanel(new BorderLayout());
        carsContainer.setBorder(new EmptyBorder(20, 0, 20, 0));
        carsContainer.add(carsScrollPane, BorderLayout.CENTER);

        mainPanel.add(carsContainer, BorderLayout.CENTER);

        // Seating chart panel - simplified for now
        JPanel seatingSectionPanel = new JPanel(new BorderLayout());
        seatingSectionLabel = new JLabel("Toa: Chưa chọn", SwingConstants.CENTER);
        seatingSectionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        seatingSectionLabel.setForeground(activeColor);
        seatingSectionLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        seatingSectionPanel.add(seatingSectionLabel, BorderLayout.NORTH);

        // Navigation and seats panel - simplified
        JPanel navigationPanel = new JPanel(new BorderLayout());

        // Left navigation button
        JButton leftButton = new JButton("<");
        leftButton.setPreferredSize(new Dimension(30, 200));
        leftButton.setFont(new Font("Arial", Font.BOLD, 24));
        navigationPanel.add(leftButton, BorderLayout.WEST);

        // Create placeholder seating chart
        seatsPanel = createPlaceholderSeatsPanel();
        seatsPanel.setBorder(new LineBorder(Color.GRAY, 1));
        navigationPanel.add(seatsPanel, BorderLayout.CENTER);

        // Right navigation button
        JButton rightButton = new JButton(">");
        rightButton.setPreferredSize(new Dimension(30, 200));
        rightButton.setFont(new Font("Arial", Font.BOLD, 24));
        navigationPanel.add(rightButton, BorderLayout.EAST);

        seatingSectionPanel.add(navigationPanel, BorderLayout.CENTER);
        mainPanel.add(seatingSectionPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * Creates a placeholder panel for the seating chart
     */
    private JPanel createPlaceholderSeatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel placeholder = new JLabel("Vui lòng chọn toa để xem sơ đồ ghế", SwingConstants.CENTER);
        placeholder.setFont(new Font("Arial", Font.ITALIC, 14));
        placeholder.setForeground(Color.GRAY);
        panel.add(placeholder, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates a train card panel with the given details
     */
    private JPanel createTrainCard(String trainCode, String departTime, long availableSeats,
                                   boolean isSelected, TrangThai status, String maLich, long unavailableSeats) {
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(isSelected ? activeBorder : normalBorder);

        // Create a unique identifier using train code and departure time
        String uniqueId = trainCode + "_" + departTime.replace("/", "").replace(":", "").replace(" ", "");

        // Store important data as client properties
        cardPanel.putClientProperty("trainId", uniqueId);
        cardPanel.putClientProperty("trainCode", trainCode);
        cardPanel.putClientProperty("maLich", maLich);

        // Set background color based on selection
        Color bgColor = isSelected ? activeColor : inactiveColor;
        cardPanel.setBackground(bgColor);

        // Set minimum size and margin
        cardPanel.setPreferredSize(new Dimension(180, 150));
        cardPanel.setMinimumSize(new Dimension(180, 150));
        cardPanel.setMaximumSize(new Dimension(180, 150));

        // Header with train code
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel codeLabel = new JLabel(trainCode);
        codeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        codeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(codeLabel, BorderLayout.CENTER);

        // Add status indicator if provided
        if (status != null) {
            JLabel statusLabel = new JLabel();
            statusLabel.setFont(new Font("Arial", Font.ITALIC, 10));

            if (status == TrangThai.CHUA_KHOI_HANH) {
                statusLabel.setText("Chưa khởi hành");
                statusLabel.setForeground(new Color(0, 128, 0));  // Green
            } else if (status == TrangThai.HOAT_DONG) {
                statusLabel.setText("Đang hoạt động");
                statusLabel.setForeground(new Color(0, 0, 255));  // Blue
            }

            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            headerPanel.add(statusLabel, BorderLayout.SOUTH);
        }

        cardPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel with times and seats info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(4, 2));
        centerPanel.setBackground(bgColor);

        Color textColor = Color.WHITE;

        JLabel departLabel = new JLabel("TG đi");
        departLabel.setForeground(textColor);
        centerPanel.add(departLabel);

        JLabel departTimeLabel = new JLabel(departTime);
        departTimeLabel.setForeground(textColor);
        centerPanel.add(departTimeLabel);

        JLabel bookedLabel = new JLabel("SL chỗ đặt");
        bookedLabel.setForeground(textColor);
        centerPanel.add(bookedLabel);

        JLabel availableLabel = new JLabel("SL chỗ trống");
        availableLabel.setForeground(textColor);
        centerPanel.add(availableLabel);

        JLabel bookedCountLabel = new JLabel(String.valueOf(unavailableSeats));
        bookedCountLabel.setForeground(textColor);
        centerPanel.add(bookedCountLabel);

        JLabel availableCountLabel = new JLabel(String.valueOf(availableSeats));
        availableCountLabel.setForeground(textColor);
        centerPanel.add(availableCountLabel);

        cardPanel.add(centerPanel, BorderLayout.CENTER);

        // Train icon at bottom
        JPanel iconPanel = createTrainIconPanel(bgColor);
        cardPanel.add(iconPanel, BorderLayout.SOUTH);

        // Add tooltip for hover information
        String statusText = (status != null) ?
                "<br>Trạng thái: " + status.getValue() : "";

        cardPanel.setToolTipText(
                "<html>" +
                        "<b>Tàu: " + trainCode + "</b><br>" +
                        "Thời gian đi: " + departTime + "<br>" +
                        "Số chỗ trống: " + availableSeats +
                        statusText +
                        "<br>Nhấp để xem chi tiết toa tàu." +
                        "</html>"
        );

        // Add selection listener with clear event handling
        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    // Store the unique ID, train code and schedule ID
                    selectedTrainId = uniqueId;
                    currentMaLich = maLich;

                    // Update selection in UI and load the corresponding cars
                    updateTrainSelection(uniqueId, trainCode);
                } catch (RemoteException ex) {
                    JOptionPane.showMessageDialog(cardPanel,
                            "Lỗi khi tải thông tin tàu: " + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Add hover effect
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!uniqueId.equals(selectedTrainId)) {
                    cardPanel.setBackground(hoverColor);
                    centerPanel.setBackground(hoverColor);
                    iconPanel.setBackground(hoverColor);

                    cardPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(173, 216, 230), 2),
                            BorderFactory.createEmptyBorder(1, 1, 1, 1)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!uniqueId.equals(selectedTrainId)) {
                    cardPanel.setBackground(inactiveColor);
                    centerPanel.setBackground(inactiveColor);
                    iconPanel.setBackground(inactiveColor);

                    cardPanel.setBorder(normalBorder);
                }
            }
        });

        return cardPanel;
    }

    /**
     * Creates a realistic train locomotive icon
     */
    private JPanel createTrainIconPanel(Color bgColor) {
        JPanel iconPanel = new JPanel();
        iconPanel.setLayout(new BorderLayout());
        iconPanel.setBackground(bgColor);

        // Create train with detailed graphics
        JPanel trainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Main locomotive body
                g2d.setColor(new Color(50, 50, 50));
                g2d.fillRoundRect(20, 5, 100, 25, 10, 10);

                // Front of locomotive (chimney)
                g2d.setColor(new Color(70, 70, 70));
                g2d.fillRoundRect(10, 10, 20, 15, 5, 5);

                // Chimney/smokestack
                g2d.setColor(new Color(40, 40, 40));
                g2d.fillRect(15, 0, 10, 10);

                // Windows
                g2d.setColor(new Color(173, 216, 230)); // Light blue
                g2d.fillRoundRect(40, 10, 15, 10, 3, 3);
                g2d.fillRoundRect(65, 10, 15, 10, 3, 3);
                g2d.fillRoundRect(90, 10, 15, 10, 3, 3);

                // Window frames
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawRoundRect(40, 10, 15, 10, 3, 3);
                g2d.drawRoundRect(65, 10, 15, 10, 3, 3);
                g2d.drawRoundRect(90, 10, 15, 10, 3, 3);

                // Wheels
                g2d.setColor(Color.BLACK);
                g2d.fillOval(30, 30, 15, 15);
                g2d.fillOval(60, 30, 15, 15);
                g2d.fillOval(90, 30, 15, 15);

                // Wheel centers
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillOval(34, 34, 7, 7);
                g2d.fillOval(64, 34, 7, 7);
                g2d.fillOval(94, 34, 7, 7);

                // Connecting rods
                g2d.setColor(new Color(150, 150, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(37, 37, 67, 37);
                g2d.drawLine(67, 37, 97, 37);

                // Headlight
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(12, 15, 6, 6);

                // Outline
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(20, 5, 100, 25, 10, 10);
                g2d.drawRoundRect(10, 10, 20, 15, 5, 5);
            }
        };
        trainPanel.setOpaque(false);
        trainPanel.setPreferredSize(new Dimension(140, 50));

        iconPanel.add(trainPanel, BorderLayout.CENTER);

        return iconPanel;
    }

    /**
     * Creates a realistic train car panel
     */
    private JPanel createTrainCarPanel(Color bgColor, String carNumber, boolean isVIP, boolean isSelected) {
        JPanel carPanel = new JPanel();
        carPanel.setLayout(new BorderLayout());
        carPanel.setPreferredSize(new Dimension(60, 50));

        if (isSelected) {
            carPanel.setBorder(activeBorder);
        }

        // Create train car with detailed graphics
        JPanel trainCarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Main car body
                g2d.setColor(bgColor);
                g2d.fillRoundRect(5, 5, 50, 25, 8, 8);

                // Windows
                g2d.setColor(new Color(173, 216, 230)); // Light blue
                int windowCount = 3;
                int windowWidth = 10;
                int windowSpacing = 5;
                int startX = 10;

                for (int i = 0; i < windowCount; i++) {
                    g2d.fillRoundRect(startX + i * (windowWidth + windowSpacing), 10, windowWidth, 8, 2, 2);
                }

                // Window frames
                g2d.setColor(new Color(100, 100, 100));
                for (int i = 0; i < windowCount; i++) {
                    g2d.drawRoundRect(startX + i * (windowWidth + windowSpacing), 10, windowWidth, 8, 2, 2);
                }

                // Door
                g2d.setColor(new Color(80, 80, 80));
                g2d.fillRoundRect(40, 10, 10, 15, 2, 2);
                g2d.setColor(new Color(60, 60, 60));
                g2d.drawRoundRect(40, 10, 10, 15, 2, 2);

                // Wheels
                g2d.setColor(Color.BLACK);
                g2d.fillOval(15, 30, 10, 10);
                g2d.fillOval(35, 30, 10, 10);

                // Wheel centers
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillOval(17, 32, 6, 6);
                g2d.fillOval(37, 32, 6, 6);

                // Connecting rod
                g2d.setColor(new Color(150, 150, 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(20, 35, 40, 35);

                // Car outline
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(5, 5, 50, 25, 8, 8);

                // VIP indicator
                if (isVIP) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString("★", 25, 20);
                }
            }
        };
        trainCarPanel.setOpaque(false);

        // Add car number label
        JLabel carLabel = new JLabel(carNumber, SwingConstants.CENTER);
        carLabel.setPreferredSize(new Dimension(60, 10));
        carLabel.setForeground(Color.BLACK);
        carLabel.setFont(new Font("Arial", Font.BOLD, 12));

        carPanel.add(trainCarPanel, BorderLayout.CENTER);
        carPanel.add(carLabel, BorderLayout.SOUTH);

        return carPanel;
    }

    /**
     * Load train cars for the selected train
     */
    private void loadTrainCars(String trainId) {
        // Clear the current panel
        carsPanel.removeAll();

        if (trainId == null || trainId.isEmpty()) {
            // No train selected, show placeholder
            JLabel placeholder = new JLabel("Vui lòng chọn tàu để xem toa", SwingConstants.CENTER);
            placeholder.setFont(new Font("Arial", Font.ITALIC, 14));
            placeholder.setForeground(Color.GRAY);
            carsPanel.add(placeholder);
            carsPanel.revalidate();
            carsPanel.repaint();
            return;
        }

        // Show loading indicator
        setLoading(true, "Đang tải danh sách toa tàu...");

        // Load data asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                // Get train cars from DAO
                return toaTauDAO.getToaByTau(trainId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, executorService).thenAccept(toaTauList -> {
            // Process data and update UI on EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    // Sort cars by order (thuTu) - still reversed for display order
                    Collections.sort(toaTauList, Comparator.comparingInt(ToaTau::getThuTu).reversed());

                    // Create car panels
                    for (ToaTau toaTau : toaTauList) {
                        // Extract car number from maToa rather than using thuTu field
                        String toaNumber;
                        String maToa = toaTau.getMaToa();

                        if (maToa != null && maToa.contains("-")) {
                            // Extract number from the first part (before the dash)
                            String firstPart = maToa.split("-")[0];
                            // Remove non-numeric characters to get just the number
                            toaNumber = firstPart.replaceAll("\\D+", "");
                        } else {
                            // Fallback to thuTu if maToa format is different
                            toaNumber = String.valueOf(toaTau.getThuTu());
                        }

                        // Determine background color based on car type (loaiToa)
                        boolean isVIP = isVipCar(toaTau);

                        // Check if this car is currently selected
                        boolean isSelected = maToa.equals(currentToaId);

                        // Determine color based on car type and selection state
                        Color bgColor;
                        if (isSelected) {
                            // Selected car color - bright green
                            bgColor = new Color(76, 175, 80);
                        } else {
                            // Standard colors based on car type
                            bgColor = isVIP ?
                                    new Color(255, 69, 0) : // Red-orange for Vietnam flag (VIP)
                                    new Color(100, 181, 246); // Light blue (standard)
                        }

                        // Create realistic train car panel
                        JPanel carPanel = createTrainCarPanel(bgColor, toaNumber, isVIP, isSelected);
                        carPanel.setName(maToa); // Store the toa ID for selection

                        // Add tooltip for hovering
                        String tooltip = "<html>" +
                                "<b>Toa số " + toaNumber + "</b><br>" +
                                "Loại toa: " + toaTau.getLoaiToa().getTenLoai() + "<br>" +
                                "Số ghế: " + toaTau.getSoGhe() + "<br>" +
                                (isVIP ? "<i>Toa VIP</i>" : "") +
                                "</html>";

                        carPanel.setToolTipText(tooltip);

                        // Add click listener for car selection
                        final String toaId = toaTau.getMaToa();
                        final String toaName = toaTau.getTenToa();
                        final String finalToaNumber = toaNumber;

                        carPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                try {
                                    // Update current toa ID
                                    currentToaId = toaId;

                                    // Update the seating section label
                                    seatingSectionLabel.setText("Toa số " + finalToaNumber + ": " + toaName);

                                    // Update visual selection for all cars
                                    updateCarSelection(toaId);

                                    // Load and display the seat chart
                                    loadSeatChart(currentTrainId, currentToaId, currentMaLich);

                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(carPanel,
                                            "Lỗi khi chọn toa: " + ex.getMessage(),
                                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                                }
                            }

                            // Enhanced hover effect
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                if (!toaId.equals(currentToaId)) {
                                    // Hover effect for non-selected cars
                                    carPanel.setBorder(BorderFactory.createCompoundBorder(
                                            BorderFactory.createLineBorder(new Color(255, 255, 0), 2), // Bright yellow for hover
                                            BorderFactory.createEmptyBorder(1, 1, 1, 1)
                                    ));

                                    // Highlight the train car panel
                                    for (Component child : carPanel.getComponents()) {
                                        if (child instanceof JPanel && child.getName() == null) {
                                            // Apply hover highlight to the train car visualization panel
                                            child.setBackground(new Color(255, 255, 240)); // Very light yellow
                                        }
                                    }
                                } else {
                                    // Enhanced hover effect for already selected car
                                    carPanel.setBorder(BorderFactory.createCompoundBorder(
                                            BorderFactory.createLineBorder(new Color(255, 165, 0), 3), // Orange for selected hover
                                            BorderFactory.createEmptyBorder(1, 1, 1, 1)
                                    ));
                                }
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                if (!toaId.equals(currentToaId)) {
                                    // Remove hover effect for non-selected cars
                                    carPanel.setBorder(null);

                                    // Reset background of train car panel
                                    for (Component child : carPanel.getComponents()) {
                                        if (child instanceof JPanel && child.getName() == null) {
                                            // Reset to transparent background
                                            child.setBackground(null);
                                        }
                                    }
                                } else {
                                    // Restore selection effect for selected car
                                    carPanel.setBorder(BorderFactory.createCompoundBorder(
                                            BorderFactory.createLineBorder(new Color(255, 215, 0), 2), // Gold border for selection
                                            BorderFactory.createEmptyBorder(1, 1, 1, 1)
                                    ));
                                }
                            }
                        });

                        carsPanel.add(carPanel);
                    }

                    // Add locomotive using the realistic train icon panel
                    JPanel locomotivePanel = new JPanel();
                    locomotivePanel.setLayout(new BorderLayout());
                    locomotivePanel.setPreferredSize(new Dimension(70, 50));

                    // Create realistic locomotive using our updated method
                    JPanel trainIconPanel = createTrainIconPanel(activeColor);

                    JLabel carLabel = new JLabel(trainId, SwingConstants.CENTER);
                    carLabel.setPreferredSize(new Dimension(70, 10));
                    carLabel.setForeground(Color.BLACK);
                    carLabel.setFont(new Font("Arial", Font.BOLD, 12));

                    // Add tooltip for locomotive
                    locomotivePanel.setToolTipText("<html><b>Đầu máy tàu " + trainId + "</b></html>");

                    locomotivePanel.add(trainIconPanel, BorderLayout.CENTER);
                    locomotivePanel.add(carLabel, BorderLayout.SOUTH);

                    carsPanel.add(locomotivePanel);

                    // Refresh UI
                    carsPanel.revalidate();
                    carsPanel.repaint();

                    // Hide loading indicator
                    setLoading(false, "Đã tải xong " + toaTauList.size() + " toa tàu");

                } catch (Exception e) {
                    handleException("Lỗi khi xử lý danh sách toa tàu", e);
                }
            });
        }).exceptionally(e -> {
            handleException("Không thể tải danh sách toa tàu", e);
            return null;
        });
    }

    /**
     * Load and display the seat chart
     */
    private void loadSeatChart(String trainId, String toaId, String maLich) {
        if (trainId.isEmpty() || toaId.isEmpty() || maLich.isEmpty()) {
            seatsPanel.removeAll();
            seatsPanel = createPlaceholderSeatsPanel();
            return;
        }

        // Show loading indicator
        setLoading(true, "Đang tải sơ đồ ghế...");

        // Load data asynchronously
        CompletableFuture.supplyAsync(() -> {
            try {
                // Get seats with their status
                return choNgoiDAO.getAvailableSeatsMapByScheduleAndToa(maLich, toaId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, executorService).thenAccept(seatsMap -> {
            // Process data and update UI on EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    // If no seats returned, show message
                    if (seatsMap == null || seatsMap.isEmpty()) {
                        seatsPanel.removeAll();
                        JLabel noSeatsLabel = new JLabel("Không tìm thấy thông tin ghế cho toa này", SwingConstants.CENTER);
                        noSeatsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                        noSeatsLabel.setForeground(Color.GRAY);
                        seatsPanel.add(noSeatsLabel, BorderLayout.CENTER);
                        seatsPanel.revalidate();
                        seatsPanel.repaint();
                        setLoading(false, "Không tìm thấy ghế");
                        return;
                    }

                    // Create a new panel for the seat layout
                    seatsPanel.removeAll();
                    seatsPanel.setLayout(new BorderLayout());

                    // Create seat visualization panel
                    JPanel seatVisualizationPanel = createSeatVisualizationPanel(seatsMap);

                    // Add a legend panel at the bottom
                    JPanel legendPanel = createSeatLegendPanel();

                    // Add both panels to the seats panel
                    seatsPanel.add(seatVisualizationPanel, BorderLayout.CENTER);
                    seatsPanel.add(legendPanel, BorderLayout.SOUTH);

                    System.out.println("Loading seat chart for train: " + trainId + ", car: " + toaId);
                    System.out.println("Schedule ID: " + maLich);
                    System.out.println("Received " + seatsMap.size() + " seats from DAO");
                    if (seatsMap.size() > 100) {
                        System.out.println("WARNING: Unusually high seat count detected!");
                        // Print first 10 seats for inspection
                        int count = 0;
                        for (Map.Entry<String, String> entry : seatsMap.entrySet()) {
                            System.out.println("Seat: " + entry.getKey() + ", Status: " + entry.getValue());
                            if (++count >= 10) break;
                        }
                    }

                    seatsPanel.revalidate();
                    seatsPanel.repaint();

                    // Hide loading indicator
                    setLoading(false, "Đã tải xong " + seatsMap.size() + " ghế");

                    // Start the polling for seat status updates
                    startSeatStatusPolling();

                } catch (Exception e) {
                    handleException("Lỗi khi xử lý sơ đồ ghế", e);
                }
            });
        }).exceptionally(e -> {
            handleException("Không thể tải sơ đồ ghế", e);
            return null;
        });
    }

    /**
     * Create the visualization panel for seats
     */
    /**
     * Create the visualization panel for seats with exactly 4 rows as requested
     * IMPROVED: Uses ChoNgoi.getById() to get proper seat names
     */

    private JPanel createSeatVisualizationPanel(Map<String, String> seatsMap) throws RemoteException {
        // Main panel with border layout
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(Color.WHITE);

        // Create the seat grid panel with fixed 4-row layout
        JPanel seatGridPanel = new JPanel();
        seatGridPanel.setBackground(Color.WHITE);
        seatGridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Get all seat IDs and sort them
        List<String> seatIds = new ArrayList<>(seatsMap.keySet());
        Collections.sort(seatIds, (s1, s2) -> {
            // Extract numeric parts for comparison
            String num1 = s1.replaceAll("[^0-9]", "");
            String num2 = s2.replaceAll("[^0-9]", "");

            try {
                return Integer.parseInt(num1) - Integer.parseInt(num2);
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });

        System.out.println("Total seats: " + seatIds.size());

        // Calculate columns based on total seats (ensuring 4 rows)
        int totalRows = 4;
        int totalSeatsPerSide = (int)Math.ceil(seatIds.size() / (double)totalRows / 2);
        int totalColumns = totalSeatsPerSide * 2 * 2 + 1; // 2 sides, 2 seats per block, +1 for aisle
        int aislePosition = totalColumns / 2;

        // Create a grid layout with 4 rows
        GridLayout gridLayout = new GridLayout(totalRows, totalColumns);
        gridLayout.setHgap(2);
        gridLayout.setVgap(2);
        seatGridPanel.setLayout(gridLayout);

        // First, create empty panels for all positions in the grid
        JPanel[][] seatPanels = new JPanel[totalRows][totalColumns];

        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalColumns; col++) {
                // Create aisle at middle position
                if (col == aislePosition) {
                    JPanel aislePanel = createAislePanel();
                    seatPanels[row][col] = aislePanel;
                    seatGridPanel.add(aislePanel);
                }
                // Create divider after each pair of seats
                else if ((col % 2 == 0) && col != 0 && col != aislePosition + 1) {
                    JPanel dividerPanel = createVerticalDivider();
                    seatPanels[row][col] = dividerPanel;
                    seatGridPanel.add(dividerPanel);
                }
                else {
                    // Create empty panel initially
                    JPanel emptyPanel = new JPanel();
                    emptyPanel.setBackground(Color.WHITE);
                    seatPanels[row][col] = emptyPanel;
                    seatGridPanel.add(emptyPanel);
                }
            }
        }

        // Now place actual seats according to the zigzag pattern
        int currentSeat = 0;

        // This tracks actual seat positions (skipping dividers and aisles)
        int actualCol = 0;

        // Place seats into appropriate positions
        while (currentSeat < seatIds.size()) {
            // Calculate the column group (0, 1, 2...) and position within group (0 or 1)
            int colGroup = actualCol / 2;
            int posInGroup = actualCol % 2;

            // Calculate the actual column in the grid (accounting for dividers)
            int gridCol;
            if (colGroup < totalSeatsPerSide) {
                // Left side of aisle
                gridCol = colGroup * 3 + posInGroup;
            } else {
                // Right side of aisle (after aisle)
                gridCol = aislePosition + 1 + (colGroup - totalSeatsPerSide) * 3 + posInGroup;
            }

            // Skip if we're out of grid bounds
            if (gridCol >= totalColumns) break;

            // Apply zigzag pattern
            if (posInGroup == 0) {
                // Top to bottom
                for (int row = 0; row < totalRows && currentSeat < seatIds.size(); row++) {
                    placeSeatInGrid(seatGridPanel, seatPanels, seatIds, seatsMap, currentSeat, row, gridCol);
                    currentSeat++;
                }
            } else {
                // Bottom to top
                for (int row = totalRows - 1; row >= 0 && currentSeat < seatIds.size(); row--) {
                    placeSeatInGrid(seatGridPanel, seatPanels, seatIds, seatsMap, currentSeat, row, gridCol);
                    currentSeat++;
                }
            }

            actualCol++;
        }

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(seatGridPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // Only horizontal scrolling
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        containerPanel.add(scrollPane, BorderLayout.CENTER);
        return containerPanel;
    }

    /**
     * Helper method to place a seat in the grid
     */
    private void placeSeatInGrid(JPanel seatGridPanel, JPanel[][] seatPanels, List<String> seatIds,
                                 Map<String, String> seatsMap, int currentSeat, int row, int col) throws RemoteException {
        String seatId = seatIds.get(currentSeat);

        // Get the actual seat name from ChoNgoi object
        ChoNgoi choNgoi = null;
        choNgoi = choNgoiDAO.getById(seatId);

        // Use friendly display name if available, otherwise use ID
        String displayName = (choNgoi != null && choNgoi.getTenCho() != null) ?
                choNgoi.getTenCho() : seatId;

        JPanel seatPanel = createSeatPanelWithFriendlyName(seatId, displayName, seatsMap.get(seatId));
        seatGridPanel.remove(seatPanels[row][col]);
        seatPanels[row][col] = seatPanel;
        seatGridPanel.add(seatPanel, row * seatPanels[0].length + col);
    }

    /**
     * Create a vertical divider
     */
    private JPanel createVerticalDivider() {
        JPanel dividerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(184, 134, 11)); // Gold color
                g.fillRect(getWidth() / 2 - 1, 0, 3, getHeight());
            }
        };
        dividerPanel.setPreferredSize(new Dimension(8, 30));
        dividerPanel.setOpaque(false);
        return dividerPanel;
    }
    private Map<String, JPanel> seatPanelMap = new HashMap<>();


    private JPanel createSeatPanelWithFriendlyName(String seatId, String displayName, String status) {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.WHITE);

        // Create gold vertical divider on left
        JPanel leftDivider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(184, 134, 11)); // Gold color
                g.fillRect(0, 0, 3, getHeight());
            }
        };
        leftDivider.setPreferredSize(new Dimension(3, 30));
        leftDivider.setOpaque(false);

        // Create the actual seat panel with custom painting for active state
        JPanel seatPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Add highlight border if this is the active seat
                if (seatId.equals(activeSeatId)) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor(new Color(255, 215, 0)); // Gold color
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                    g2d.dispose();
                }
            }
        };
        seatPanel.setPreferredSize(new Dimension(60, 30));
        seatPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        // Store reference to this seat panel in our map
        seatPanelMap.put(seatId, seatPanel);

        // Check if this seat is in reservation process
        boolean isReserved = reservationTimers.containsKey(seatId);
        if (isReserved) {
            // Override status for reserved seats
            status = STATUS_PENDING;
        }

        // Set background color based on status
        Color bgColor;
        switch (status) {
            case STATUS_BOOKED:
                bgColor = SEAT_BOOKED_COLOR;
                break;
            case STATUS_PENDING:
                bgColor = SEAT_PENDING_COLOR;
                break;
            case STATUS_AVAILABLE:
            default:
                bgColor = SEAT_AVAILABLE_COLOR;
                break;
        }
        seatPanel.setBackground(bgColor);

        // Add seat name label
        JLabel nameLabel = new JLabel(displayName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 10));
        seatPanel.add(nameLabel, BorderLayout.CENTER);

        try {
            // Get the seat price for tooltip
            ChoNgoi choNgoi = choNgoiDAO.getById(seatId);
            double price = getSeatPrice(choNgoi);
            String formattedPrice = formatCurrency(price);

            // Add tooltip with price
            String tooltipStatus = status;
            if (isReserved) {
                int remaining = remainingTimes.get(seatId);
                int minutes = remaining / 60;
                int seconds = remaining % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                tooltipStatus = "Chờ xác nhận (còn " + timeString + ")";
            }

            seatPanel.setToolTipText("<html>" +
                    "Ghế: <b>" + displayName + "</b><br>" +
                    "Trạng thái: " + tooltipStatus + "<br>" +
                    "Giá vé: <b>" + formattedPrice + "</b>" +
                    "</html>");
        } catch (Exception e) {
            // Fallback tooltip
            seatPanel.setToolTipText("Ghế " + displayName + ": " + status);
        }

        // Add click listener for available seats (no hover effect)
        if (status.equals(STATUS_AVAILABLE)) {
            seatPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            seatPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectSeat(seatId);
                }
                // No mouseEntered or mouseExited effects
            });
        }

        // Assemble the complete seat with divider
        outerPanel.add(leftDivider, BorderLayout.WEST);
        outerPanel.add(seatPanel, BorderLayout.CENTER);

        return outerPanel;
    }

    private void setActiveSeat(String seatId) {
        // Clear the active state of the previous active seat
        if (activeSeatId != null && !activeSeatId.equals(seatId)) {
            JPanel prevSeatPanel = seatPanelMap.get(activeSeatId);
            if (prevSeatPanel != null) {
                prevSeatPanel.repaint();
            }
        }

        // Set the new active seat
        activeSeatId = seatId;

        // Repaint the new active seat
        JPanel seatPanel = seatPanelMap.get(seatId);
        if (seatPanel != null) {
            seatPanel.repaint();
        }
    }

    private void updateSeatStatus(String seatId, String newStatus) {
        try {
            // Get the direct reference to the seat panel from our map
            JPanel seatPanel = seatPanelMap.get(seatId);
            if (seatPanel != null) {
                // Determine the new background color based on status
                Color bgColor;
                switch (newStatus) {
                    case STATUS_BOOKED:
                        bgColor = SEAT_BOOKED_COLOR;
                        break;
                    case STATUS_PENDING:
                        bgColor = SEAT_PENDING_COLOR; // Yellow color
                        // Set as active seat when set to pending
                        SwingUtilities.invokeLater(() -> setActiveSeat(seatId));
                        break;
                    case STATUS_AVAILABLE:
                    default:
                        bgColor = SEAT_AVAILABLE_COLOR;
                        break;
                }

                // Update background color immediately on EDT
                SwingUtilities.invokeLater(() -> {
                    seatPanel.setBackground(bgColor);
                    seatPanel.repaint();

                    // Update tooltip if needed
                    try {
                        ChoNgoi choNgoi = choNgoiDAO.getById(seatId);
                        String displayName = (choNgoi.getTenCho() != null) ? choNgoi.getTenCho() : seatId;
                        double price = getSeatPrice(choNgoi);
                        String formattedPrice = formatCurrency(price);

                        // Update tooltip with new status
                        String tooltipStatus = newStatus;
                        if (newStatus.equals(STATUS_PENDING) && reservationTimers.containsKey(seatId)) {
                            int remaining = remainingTimes.get(seatId);
                            int minutes = remaining / 60;
                            int seconds = remaining % 60;
                            String timeString = String.format("%02d:%02d", minutes, seconds);
                            tooltipStatus = "Chờ xác nhận (còn " + timeString + ")";
                        }

                        seatPanel.setToolTipText("<html>" +
                                "Ghế: <b>" + displayName + "</b><br>" +
                                "Trạng thái: " + tooltipStatus + "<br>" +
                                "Giá vé: <b>" + formattedPrice + "</b>" +
                                "</html>");
                    } catch (Exception e) {
                        // Fallback tooltip
                        seatPanel.setToolTipText("Ghế " + seatId + ": " + newStatus);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật trạng thái ghế: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private JPanel createAislePanel() {
        JPanel aislePanel = new JPanel();
        aislePanel.setBackground(new Color(100, 100, 100)); // Gray color
        aislePanel.setPreferredSize(new Dimension(15, 30));
        return aislePanel;
    }


    private void selectSeat(String seatId) {
        // Don't select the same seat twice
        if (selectedSeatIds.contains(seatId)) {
            JOptionPane.showMessageDialog(this,
                    "Chỗ ngồi này đã được thêm vào giỏ vé của bạn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Hiển thị loading
        setLoading(true, "Đang kiểm tra chỗ ngồi...");

        // Khóa chỗ ngồi bất đồng bộ
        CompletableFuture.supplyAsync(() -> {
            try {
                // Kiểm tra chỗ ngồi có khả dụng không
                boolean isAvailable = choNgoiGiuDAO.isChoNgoiAvailable(seatId, currentMaLich);
                if (!isAvailable) {
                    throw new RuntimeException("Chỗ ngồi không khả dụng");
                }

                // Khóa chỗ ngồi với timeout 5 phút
                boolean locked = choNgoiGiuDAO.khoaChoNgoi(seatId, currentMaLich, sessionId, 5 * 60 * 1000);
                if (!locked) {
                    throw new RuntimeException("Không thể khóa chỗ ngồi");
                }

                return choNgoiDAO.getById(seatId);
            } catch (RemoteException e) {
                throw new RuntimeException("Lỗi kết nối: " + e.getMessage());
            }
        }, executorService).thenAccept(choNgoi -> {
            if (choNgoi != null) {
                SwingUtilities.invokeLater(() -> {
                    // Cập nhật UI khi khóa thành công
                    updateSeatStatus(seatId, STATUS_PENDING);

                    // Add to selected seats
                    selectedSeatIds.add(seatId);
                    activeSeatId = seatId;

                    // Bắt đầu đếm ngược
                    startReservationCountdown(seatId, choNgoi.getTenCho(), getSeatPrice(choNgoi));

                    // Thêm vào giỏ hàng
                    addToCartWithTimeout(seatId, choNgoi.getTenCho(), getSeatPrice(choNgoi));

                    // Tắt loading
                    setLoading(false, "");
                });
            }
        }).exceptionally(e -> {
            SwingUtilities.invokeLater(() -> {
                // Tắt loading
                setLoading(false, "");

                // Hiển thị thông báo lỗi
                JOptionPane.showMessageDialog(this,
                        "Không thể chọn chỗ ngồi: " + e.getCause().getMessage(),
                        "Thông báo", JOptionPane.WARNING_MESSAGE);

                // Cập nhật lại UI chỗ ngồi
                reloadSeatUI(seatId);
            });
            return null;
        });
    }

    private void reloadSeatUI(String seatId) {
        // Use SwingUtilities.invokeLater to ensure UI updates happen on EDT
        SwingUtilities.invokeLater(() -> {
            // Find the seating visualization panel (first component of seatsPanel)
            if (seatsPanel.getComponentCount() > 0) {
                Component visualPanel = seatsPanel.getComponent(0);
                if (visualPanel instanceof JPanel) {
                    // Get scroll pane
                    Component scrollComp = ((JPanel)visualPanel).getComponent(0);
                    if (scrollComp instanceof JScrollPane) {
                        // Force repaint of the viewport and all its children
                        JViewport viewport = ((JScrollPane)scrollComp).getViewport();

                        // Get the view component (the actual seat grid)
                        Component view = viewport.getView();
                        if (view != null) {
                            // Force immediate validation and repaint of the view and all its children
                            view.validate();
                            view.repaint();
                        }

                        // Force immediate validation and repaint of the viewport
                        viewport.validate();
                        viewport.repaint();

                        // Force immediate validation and repaint of the scroll pane
                        ((JScrollPane)scrollComp).validate();
                        ((JScrollPane)scrollComp).repaint();

                        // Force immediate validation and repaint of the visual panel
                        ((JPanel)visualPanel).validate();
                        ((JPanel)visualPanel).repaint();

                        // Force immediate validation and repaint of the seats panel
                        seatsPanel.validate();
                        seatsPanel.repaint();

                        // Queue another repaint after a short delay to ensure UI updates
                        Timer repaintTimer = new Timer(50, e -> {
                            if (view != null) view.repaint();
                            viewport.repaint();
                            ((JScrollPane)scrollComp).repaint();
                            ((JPanel)visualPanel).repaint();
                            seatsPanel.repaint();
                        });
                        repaintTimer.setRepeats(false);
                        repaintTimer.start();
                    }
                }
            }
        });
    }

    private boolean findAndUpdateComponentRecursively(Container container, String seatId, String status) {
        // Check all components in this container
        for (Component comp : container.getComponents()) {
            // If it's the outer seat panel that contains a panel with BorderLayout
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;

                // Check if this panel has a BorderLayout and contains our seat panel
                if (panel.getLayout() instanceof BorderLayout) {
                    Component centerComp = ((BorderLayout)panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    if (centerComp instanceof JPanel) {
                        JPanel seatPanel = (JPanel) centerComp;

                        // Check for tooltip text containing the seat ID (most reliable way)
                        String tooltip = seatPanel.getToolTipText();
                        if (tooltip != null && tooltip.contains("Ghế: <b>" + seatId + "</b>")) {
                            // Found it! Update color based on status
                            // If status is not provided, determine it from reservationTimers
                            String effectiveStatus = status != null ? status :
                                    (reservationTimers.containsKey(seatId) ? STATUS_PENDING : STATUS_AVAILABLE);

                            Color bgColor = switch (effectiveStatus) {
                                case STATUS_PENDING -> SEAT_PENDING_COLOR;
                                case STATUS_BOOKED -> SEAT_BOOKED_COLOR;
                                default -> SEAT_AVAILABLE_COLOR;
                            };

                            // Set the background color
                            seatPanel.setBackground(bgColor);

                            // Force immediate repaint of this seat panel
                            seatPanel.invalidate();
                            seatPanel.validate();
                            seatPanel.repaint();

                            // Also repaint the parent panel to ensure changes are visible
                            panel.invalidate();
                            panel.validate();
                            panel.repaint();

                            return true;
                        }
                    }
                }

                // Recursively search children
                if (findAndUpdateComponentRecursively(panel, seatId, status)) {
                    return true;
                }
            } else if (comp instanceof Container) {
                // Check other containers (like scroll panes)
                if (findAndUpdateComponentRecursively((Container) comp, seatId, status)) {
                    return true;
                }
            }
        }
        return false;
    }
    private void updateCartDisplay() {
        try {
            // Find the right panel directly using BorderLayout constraints
            Component comp = ((BorderLayout)this.getLayout()).getLayoutComponent(BorderLayout.EAST);
            if (comp instanceof JPanel) {
                JPanel rightPanel = (JPanel) comp;

                // Get scroll pane from right panel
                JScrollPane scrollPane = null;
                for (Component c : rightPanel.getComponents()) {
                    if (c instanceof JScrollPane) {
                        scrollPane = (JScrollPane) c;
                        break;
                    }
                }

                if (scrollPane != null) {
                    JViewport viewport = scrollPane.getViewport();
                    JPanel itemsPanel = (JPanel) viewport.getView();

                    itemsPanel.removeAll();
                    itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
                    itemsPanel.setMaximumSize(new Dimension(220, Short.MAX_VALUE));
                    itemsPanel.setPreferredSize(new Dimension(200, cartItems.size() * 80));

                    if (cartItems.isEmpty()) {
                        JPanel emptyPanel = new JPanel(new BorderLayout());
                        emptyPanel.setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
                        emptyPanel.setOpaque(false);
                        emptyPanel.setMaximumSize(new Dimension(220, 50));

                        JLabel emptyLabel = new JLabel("Giỏ vé trống", JLabel.CENTER);
                        emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                        emptyLabel.setForeground(Color.GRAY);
                        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
                        itemsPanel.add(emptyPanel);
                    } else {
                        for (TicketItem item : cartItems) {
                            JPanel ticketPanel = createTicketItemPanel(item);
                            itemsPanel.add(ticketPanel);
                            itemsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                        }
                    }

                    double total = cartItems.stream().mapToDouble(item -> item.price).sum();
                    totalLabel.setText("Tổng cộng: " + formatCurrency(total));

                    itemsPanel.revalidate();
                    itemsPanel.repaint();
                    scrollPane.revalidate();
                    scrollPane.repaint();

                    if (!cartItems.isEmpty()) {
                        JScrollPane finalScrollPane = scrollPane;
                        SwingUtilities.invokeLater(() -> {
                            JScrollBar verticalBar = finalScrollPane.getVerticalScrollBar();
                            verticalBar.setValue(verticalBar.getMaximum());
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật giỏ vé: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void startReservationCountdown(String seatId, String displayName, double price) {
        // Initialize the remaining time for this seat in the countdown map
        remainingTimes.put(seatId, RESERVATION_TIMEOUT);

        // Create and start the timer
        Timer timer = new Timer(1000, null); // 1 second intervals
        timer.addActionListener(e -> {
            // Update the countdown
            int remaining = remainingTimes.getOrDefault(seatId, 0) - 1;

            // Check if we still have the seat in our cart
            boolean seatInCart = selectedSeatIds.contains(seatId);

            if (remaining <= 0 || !seatInCart) {
                // Time is up or seat was removed
                timer.stop();
                reservationTimers.remove(seatId);
                remainingTimes.remove(seatId);

                // Khi thời gian đếm ngược kết thúc, tự động xóa khỏi giỏ vé
                if (remaining <= 0 && seatInCart) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                TrainTicketBookingSystem.this,
                                "Thời gian giữ chỗ cho ghế " + displayName + " đã hết. " +
                                        "Chỗ ngồi đã bị xóa khỏi giỏ vé.",
                                "Thông báo",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        // Xóa ghế khỏi giỏ vé
                        removeFromCart(seatId);
                    });
                }
                return;
            }

            // Update the remaining time
            remainingTimes.put(seatId, remaining);

            // Update the countdown in the cart
            updateCartItemCountdown(seatId, remaining);
        });

        // Store the timer and start it
        reservationTimers.put(seatId, timer);
        timer.start();
    }


    private void releaseReservation(String seatId) {
        // Release the reservation
        releaseReservationInternal(seatId);

        // Remove from cart (which will update the UI)
        removeFromCart(seatId);
    }


    private double getSeatPrice(ChoNgoi choNgoi) {
        // In a real app, you would get this from your pricing service or database
        // For this example, we'll use a base price based on car type
        double basePrice = 250000.0; // Base price for regular seats

        try {
            // Get the car info to determine if it's a VIP or regular seat
            ToaTau toaTau = toaTauDAO.getToaTauById(currentToaId);
            if (toaTau != null && toaTau.getLoaiToa() != null) {
                // Apply multiplier for VIP cars
                if (isVipCar(toaTau)) {
                    basePrice *= 1.5; // 50% premium for VIP seats
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting seat price: " + e.getMessage());
        }

        return basePrice;
    }

    /**
     * Format currency in Vietnamese format
     */
    private String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount) + " đ";
    }

    /**
     * Add to cart with timeout feature
     */
    private void addToCartWithTimeout(String seatId, String displayName, double price) {
        String from = departureField.getText();
        String to = arrivalField.getText();
        String departureDate = departureDateField.getText();

        // Get train car information
        String carId = currentToaId;
        String carName = "";

        try {
            // Get the car name or number from the ToaTauDAO
            ToaTau toaTau = toaTauDAO.getToaTauById(carId);
            if (toaTau != null) {
                // Extract car number from maToa rather than using thuTu field
                String toaNumber;
                String maToa = toaTau.getMaToa();

                if (maToa != null && maToa.contains("-")) {
                    // Extract number from the first part (before the dash)
                    String firstPart = maToa.split("-")[0];
                    // Remove non-numeric characters to get just the number
                    toaNumber = firstPart.replaceAll("\\D+", "");
                } else {
                    // Fallback to thuTu if maToa format is different
                    toaNumber = String.valueOf(toaTau.getThuTu());
                }

                carName = "Toa số " + toaNumber;
            }
        } catch (Exception e) {
            System.err.println("Error getting train car information: " + e.getMessage());
            carName = "Toa không xác định";
        }

        // Create and add ticket item to cart with seatId for later reference
        TicketItem item = new TicketItem(currentTrainId, seatId, displayName, from, to, departureDate, price, carId, carName);
        cartItems.add(item);

        // Update cart display
        updateCartDisplay();

        // Immediately update the seat status in UI to show as pending (yellow)
        updateSeatStatus(seatId, STATUS_PENDING);
    }


    /**
     * Update cart item to show countdown
     */


    /**
     * Helper to update a panel with countdown
     */

    /**
     * Remove an item from cart by seat ID
     */
    private void removeFromCart(String seatId) {
        // Remove the item from cart list
        Iterator<TicketItem> iterator = cartItems.iterator();
        while (iterator.hasNext()) {
            TicketItem item = iterator.next();
            if (item.seatId.equals(seatId)) {
                iterator.remove();
                break;
            }
        }

        // Release seat reservation
        releaseReservationInternal(seatId);

        // Update the cart display
        updateCartDisplay();
    }

    /**
     * Internal method to release a seat reservation
     * Used both by releaseReservation and removeFromCart to avoid circular calls
     */
    private void releaseReservationInternal(String seatId) {
        // Stop the countdown timer if it exists
        Timer timer = reservationTimers.get(seatId);
        if (timer != null) {
            timer.stop();
            reservationTimers.remove(seatId);
        }
        remainingTimes.remove(seatId);

        // Đảm bảo ghế không còn trong danh sách ghế đã chọn
        selectedSeatIds.remove(seatId);

        // Hiển thị loading
        setLoading(true, "Đang hủy đặt chỗ...");

        // Release the seat reservation on the server
        CompletableFuture.runAsync(() -> {
            try {
                boolean success = choNgoiGiuDAO.huyKhoaChoNgoi(seatId, currentMaLich, sessionId);

                SwingUtilities.invokeLater(() -> {
                    setLoading(false, "");

                    if (success) {
                        // Update the UI to show the seat as available again
                        updateSeatStatus(seatId, STATUS_AVAILABLE);

                        // If this was the active seat, clear it
                        if (seatId.equals(activeSeatId)) {
                            activeSeatId = null;
                        }

                        // Thông báo thành công
                        // (lưu ý: chỉ hiển thị thông báo khi gọi trực tiếp từ nút xóa, không hiển thị khi xóa từ các phương thức khác)
                    } else {
                        LOGGER.warning("Không thể hủy đặt chỗ ID: " + seatId);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setLoading(false, "");
                    LOGGER.log(Level.WARNING, "Lỗi khi hủy đặt chỗ: " + e.getMessage(), e);
                });
            }
        }, executorService);
    }

    /**
     * Update seat status in UI
     */
    /**
     * Implementation of seat status update
     */
    private void updateSeatStatusImpl(String seatId, String status) {
        try {
            // First try to find and update the specific seat component
            if (seatsPanel != null && seatsPanel.getComponentCount() > 0) {
                boolean updated = findAndUpdateComponentRecursively(seatsPanel, seatId, status);

                // If we couldn't find and update the specific seat, fall back to reloading the entire chart
                if (!updated) {
                    // Re-load the seat chart to reflect changes
                    loadSeatChart(currentTrainId, currentToaId, currentMaLich);
                } else {
                    // Force UI refresh to make the color change visible immediately
                    reloadSeatUI(seatId);
                }
            } else {
                // If seatsPanel is not initialized yet, reload the entire chart
                loadSeatChart(currentTrainId, currentToaId, currentMaLich);
            }
        } catch (Exception ex) {
            System.err.println("Error in updateSeatStatusImpl: " + ex.getMessage());
        }
    }

    /**

     /**
     * Create legend panel
     */
    private JPanel createSeatLegendPanel() {
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.setBackground(Color.WHITE);

        // Available seat legend
        addLegendItem(legendPanel, SEAT_AVAILABLE_COLOR, "Trống");

        // Booked seat legend
        addLegendItem(legendPanel, SEAT_BOOKED_COLOR, "Đã đặt");

        // Pending seat legend
        addLegendItem(legendPanel, SEAT_PENDING_COLOR, "Chờ xác nhận");

        return legendPanel;
    }

    /**
     * Helper to add legend items
     */
    private void addLegendItem(JPanel legendPanel, Color color, String text) {
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        colorBox.setBackground(color);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        itemPanel.setOpaque(false);
        itemPanel.add(colorBox);
        itemPanel.add(label);

        legendPanel.add(itemPanel);
    }

    /**
     * Update car selection visual state with enhanced visual feedback
     */
    private void updateCarSelection(String selectedToaId) {
        try {
            // Get train cars from DAO to recreate them with updated colors
            List<ToaTau> toaTauList = toaTauDAO.getToaByTau(currentTrainId);

            // If no train cars found, just return
            if (toaTauList == null || toaTauList.isEmpty()) {
                return;
            }

            // Sort cars by order (thuTu) - still reversed for display order
            Collections.sort(toaTauList, Comparator.comparingInt(ToaTau::getThuTu).reversed());

            // Clear existing cars
            carsPanel.removeAll();

            // Recreate car panels with updated colors
            for (ToaTau toaTau : toaTauList) {
                // Extract car number from maToa rather than using thuTu field
                String toaNumber;
                String maToa = toaTau.getMaToa();

                if (maToa != null && maToa.contains("-")) {
                    // Extract number from the first part (before the dash)
                    String firstPart = maToa.split("-")[0];
                    // Remove non-numeric characters to get just the number
                    toaNumber = firstPart.replaceAll("\\D+", "");
                } else {
                    // Fallback to thuTu if maToa format is different
                    toaNumber = String.valueOf(toaTau.getThuTu());
                }

                // Determine background color based on car type and selection state
                boolean isVIP = isVipCar(toaTau);
                boolean isSelected = maToa.equals(selectedToaId);

                // Determine color based on selection state and car type
                Color bgColor;
                if (isSelected) {
                    // Selected car color - bright green
                    bgColor = new Color(76, 175, 80);
                } else {
                    // Standard colors based on car type
                    bgColor = isVIP ?
                            new Color(255, 69, 0) : // Red-orange for Vietnam flag (VIP)
                            new Color(100, 181, 246); // Light blue (standard)
                }

                // Create realistic train car panel with updated color
                JPanel carPanel = createTrainCarPanel(bgColor, toaNumber, isVIP, isSelected);
                carPanel.setName(maToa); // Store the toa ID for selection

                // Add tooltip for hovering
                String tooltip = "<html>" +
                        "<b>Toa số " + toaNumber + "</b><br>" +
                        "Loại toa: " + (toaTau.getLoaiToa() != null ? toaTau.getLoaiToa().getTenLoai() : "Thường") + "<br>" +
                        "Số ghế: " + toaTau.getSoGhe() + "<br>" +
                        (isVIP ? "<i>Toa VIP</i>" : "") +
                        "</html>";

                carPanel.setToolTipText(tooltip);

                // Add click listener for car selection
                final String toaId = toaTau.getMaToa();
                final String toaName = toaTau.getTenToa();
                final String finalToaNumber = toaNumber;

                carPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            // Update current toa ID
                            currentToaId = toaId;

                            // Update the seating section label
                            seatingSectionLabel.setText("Toa số " + finalToaNumber + ": " + toaName);

                            // Update visual selection for all cars
                            updateCarSelection(toaId);

                            // Load and display the seat chart
                            loadSeatChart(currentTrainId, currentToaId, currentMaLich);

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(carPanel,
                                    "Lỗi khi chọn toa: " + ex.getMessage(),
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    // Enhanced hover effect
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!toaId.equals(currentToaId)) {
                            // Hover effect for non-selected cars
                            carPanel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(255, 255, 0), 2), // Bright yellow for hover
                                    BorderFactory.createEmptyBorder(1, 1, 1, 1)
                            ));

                            // Highlight the train car panel
                            for (Component child : carPanel.getComponents()) {
                                if (child instanceof JPanel && child.getName() == null) {
                                    // Apply hover highlight to the train car visualization panel
                                    child.setBackground(new Color(255, 255, 240)); // Very light yellow
                                }
                            }
                        } else {
                            // Enhanced hover effect for already selected car
                            carPanel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(255, 165, 0), 3), // Orange for selected hover
                                    BorderFactory.createEmptyBorder(1, 1, 1, 1)
                            ));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!toaId.equals(currentToaId)) {
                            // Remove hover effect for non-selected cars
                            carPanel.setBorder(null);

                            // Reset background of train car panel
                            for (Component child : carPanel.getComponents()) {
                                if (child instanceof JPanel && child.getName() == null) {
                                    // Reset to transparent background
                                    child.setBackground(null);
                                }
                            }
                        } else {
                            // Restore selection effect for selected car
                            carPanel.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(255, 215, 0), 2), // Gold border for selection
                                    BorderFactory.createEmptyBorder(1, 1, 1, 1)
                            ));
                        }
                    }
                });

                carsPanel.add(carPanel);
            }

            // Add locomotive using the realistic train icon panel
            JPanel locomotivePanel = new JPanel();
            locomotivePanel.setLayout(new BorderLayout());
            locomotivePanel.setPreferredSize(new Dimension(70, 50));

            // Create realistic locomotive using our updated method
            JPanel trainIconPanel = createTrainIconPanel(activeColor);

            JLabel carLabel = new JLabel(currentTrainId, SwingConstants.CENTER);
            carLabel.setPreferredSize(new Dimension(70, 10));
            carLabel.setForeground(Color.BLACK);
            carLabel.setFont(new Font("Arial", Font.BOLD, 12));

            // Add tooltip for locomotive
            locomotivePanel.setToolTipText("<html><b>Đầu máy tàu " + currentTrainId + "</b></html>");

            locomotivePanel.add(trainIconPanel, BorderLayout.CENTER);
            locomotivePanel.add(carLabel, BorderLayout.SOUTH);

            carsPanel.add(locomotivePanel);

            // Apply visual update
            carsPanel.revalidate();
            carsPanel.repaint();
        } catch (Exception ex) {
            System.err.println("Error in updateCarSelection: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Determine if a car is VIP based on loaiToa
     */
    private boolean isVipCar(ToaTau toaTau) {
        return false;



    }

    /**
     * Update train selection and load its cars
     */
    private void updateTrainSelection(String selectedId, String trainCode) throws RemoteException {
        System.out.println("Selecting train with ID: " + selectedId + ", code: " + trainCode);

        // Update selection in train cards
        for (Component component : trainsPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;

                // Get the unique train ID from client property
                Object trainIdObj = panel.getClientProperty("trainId");

                // Skip if this panel doesn't have a train ID
                if (trainIdObj == null) {
                    continue;
                }

                String trainId = (String) trainIdObj;
                boolean isSelected = trainId.equals(selectedId);

                System.out.println("Train card: " + trainId + ", Selected: " + isSelected);

                // Update the card appearance based on selection state
                updateCardAppearance(panel, isSelected);
            }
        }

        // Update current train ID and load its cars
        currentTrainId = trainCode;
        currentToaId = ""; // Reset current toa
        loadTrainCars(currentTrainId);
    }

    /**
     * Helper method to update a train card's appearance
     */
    private void updateCardAppearance(JPanel cardPanel, boolean isSelected) {
        // Set the card border
        cardPanel.setBorder(isSelected ? activeBorder : normalBorder);

        // Set the card background
        cardPanel.setBackground(isSelected ? activeColor : inactiveColor);

        // Update each contained panel except the header (which is at NORTH)
        for (Component comp : cardPanel.getComponents()) {
            if (comp instanceof JPanel) {
                // Don't change the header panel's background
                if (((BorderLayout)cardPanel.getLayout()).getConstraints(comp) == BorderLayout.NORTH) {
                    continue;
                }

                // Update background of other panels
                comp.setBackground(isSelected ? activeColor : inactiveColor);
            }
        }
    }

    /**
     * Create the trip information panel (left panel)
     */
    private JPanel createTripInfoPanel() {
        JPanel tripInfoPanel = new JPanel();
        tripInfoPanel.setLayout(new BorderLayout());
        tripInfoPanel.setPreferredSize(new Dimension(250, 0));
        tripInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(activeColor);

        // Header with icon
        JLabel headerLabel = new JLabel("≡ Thông tin hành trình", JLabel.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        titlePanel.add(headerLabel, BorderLayout.CENTER);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Departure station
        formPanel.add(createFormLabel("Ga đi"));
        departureField = new JTextField("Sài Gòn");
        departureField.setFont(new Font("Arial", Font.PLAIN, 17));
        formPanel.add(departureField);
        formPanel.add(Box.createVerticalStrut(10));

        // Arrival station
        formPanel.add(createFormLabel("Ga đến"));
        arrivalField = new JTextField("Hà Nội");
        arrivalField.setFont(new Font("Arial", Font.PLAIN, 17));
        formPanel.add(arrivalField);
        formPanel.add(Box.createVerticalStrut(10));

        // Trip type
        JPanel tripTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        oneWayRadio = new JRadioButton("Một chiều");
        oneWayRadio.setSelected(true);
        roundTripRadio = new JRadioButton("Khứ hồi");
        ButtonGroup tripTypeGroup = new ButtonGroup();
        tripTypeGroup.add(oneWayRadio);
        tripTypeGroup.add(roundTripRadio);
        tripTypePanel.add(oneWayRadio);
        tripTypePanel.add(Box.createHorizontalStrut(10));
        tripTypePanel.add(roundTripRadio);
        formPanel.add(tripTypePanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Departure date
        formPanel.add(createFormLabel("Ngày đi"));
        JPanel departureDatePanel = new JPanel(new BorderLayout(5, 0));

        departureDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        //set text size

        departureDateField.setFont(new Font("Arial", Font.PLAIN, 17));
        departureDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 204, 204)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JButton departureDateButton = createCalendarButton(departureDateField);
        departureDatePanel.add(departureDateField, BorderLayout.CENTER);
        departureDatePanel.add(departureDateButton, BorderLayout.EAST);
        formPanel.add(departureDatePanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Return date
        formPanel.add(createFormLabel("Ngày về"));
        JPanel returnDatePanel = new JPanel(new BorderLayout(5, 0));
        returnDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        returnDateField.setFont(new Font("Arial", Font.PLAIN, 17));
        returnDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 204, 204)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        JButton returnDateButton = createCalendarButton(returnDateField);
        returnDatePanel.add(returnDateField, BorderLayout.CENTER);
        returnDatePanel.add(returnDateButton, BorderLayout.EAST);
        formPanel.add(returnDatePanel);
        formPanel.add(Box.createVerticalStrut(20));

        // Search button
        JButton searchButton = new JButton("Tìm kiếm" );
        searchButton.setFont(new Font("Arial", Font.BOLD, 17));
        searchButton.setBackground(activeColor);
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.addActionListener(e -> searchTrains());

        // Add hover effect to search button
        searchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                searchButton.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                searchButton.setBackground(activeColor);
            }
        });

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.add(searchButton, BorderLayout.EAST);
        formPanel.add(searchPanel);

        // Add components to trip info panel
        tripInfoPanel.add(titlePanel, BorderLayout.NORTH);
        tripInfoPanel.add(formPanel, BorderLayout.CENTER);

        return tripInfoPanel;
    }

    /**
     * Create a form field label
     */
    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 17));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return label;
    }

    /**
     * Create a calendar button for date fields
     * @param textField The text field to update with the selected date
     * @return A styled calendar button with date picker functionality
     */
    private JButton createCalendarButton(JTextField textField) {
        JButton calendarButton = new JButton();
        calendarButton.setBackground(activeColor);
        calendarButton.setPreferredSize(new Dimension(32, 32));
        calendarButton.setFocusPainted(false);
        calendarButton.setBorderPainted(false);
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add a subtle border
        calendarButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(41, 128, 185, 100), 1, true),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        // Enhanced calendar icon
        calendarButton.setIcon(new ImageIcon(getClass().getResource("/Anh_HeThong/calendar.png")) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw calendar background with shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(x + 4, y + 4, 16, 16, 4, 4);

                // Draw calendar background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x + 2, y + 2, 16, 16, 4, 4);

                // Draw calendar top bar
                g2.setColor(activeColor);
                g2.fillRoundRect(x + 2, y + 2, 16, 5, 4, 4);

                // Draw calendar outline
                g2.setColor(new Color(41, 128, 185));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(x + 2, y + 2, 16, 16, 4, 4);

                // Draw calendar hangers
                g2.setColor(new Color(41, 128, 185));
                g2.fillRect(x + 6, y, 2, 3);
                g2.fillRect(x + 12, y, 2, 3);

                // Draw calendar grid lines
                g2.setColor(new Color(200, 200, 200));
                g2.drawLine(x + 2, y + 10, x + 18, y + 10);
                g2.drawLine(x + 2, y + 14, x + 18, y + 14);
                g2.drawLine(x + 7, y + 7, x + 7, y + 18);
                g2.drawLine(x + 13, y + 7, x + 13, y + 18);

                g2.dispose();
            }
            public int getIconWidth() { return 22; }
            public int getIconHeight() { return 22; }
        });

        // Add hover effect
        calendarButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                calendarButton.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                calendarButton.setBackground(activeColor);
            }
        });

        // Add action listener to show promotion calendar panel
        calendarButton.addActionListener(e -> {
            // Create a popup menu to hold the promotion calendar panel
            JPopupMenu popup = new JPopupMenu();
            popup.setBorder(BorderFactory.createEmptyBorder());

            // Create a panel with a border layout to hold the promotion calendar panel
            JPanel popupPanel = new JPanel(new BorderLayout());
            popupPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            popupPanel.setBackground(Color.WHITE);

            // Create the promotion calendar panel
            PromotionCalendarPanel calendarPanel = new PromotionCalendarPanel(khuyenMaiDAO);
            calendarPanel.setPreferredSize(new Dimension(400, 400));

            // Try to parse current date from text field
            try {
                if (!textField.getText().isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate date = LocalDate.parse(textField.getText(), formatter);
                    calendarPanel.setSelectedDate(date);
                }
            } catch (Exception ex) {
                // If parsing fails, use current date (default)
            }

            // Add a listener to handle date selection
            calendarPanel.setDayPanelClickListener((date, promotions) -> {
                // Update the text field with the selected date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                textField.setText(date.format(formatter));

                // Hide the popup after selection
                popup.setVisible(false);
            });

            // Add the calendar panel to the popup panel
            popupPanel.add(calendarPanel, BorderLayout.CENTER);

            // Add the panel to the popup
            popup.add(popupPanel);

            // Show the popup below the button
            popup.show(calendarButton, 0, calendarButton.getHeight());
        });

        return calendarButton;
    }

    /**
     * Create a styled date chooser
     */
    private JDateChooser createStyledDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(150, 30));
        dateChooser.setFont(new Font("Arial", Font.PLAIN, 12));

        // Style the text field inside the date chooser
        JTextField textField = (JTextField) dateChooser.getDateEditor().getUiComponent();
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 204, 204)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        return dateChooser;
    }

    /**
     * Search for trains based on form data
     */
    private void searchTrains() {
        try {
            // Get input data
            String gaDi = departureField.getText().trim();
            String gaDen = arrivalField.getText().trim();

            // Parse date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate departureDate = LocalDate.parse(departureDateField.getText().trim(), formatter);

            setLoading(true, "Đang tìm kiếm chuyến tàu...");

            // Use CompletableFuture for asynchronous search
            CompletableFuture.supplyAsync(() -> {
                try {
                    return lichTrinhTauDAO.getListLichTrinhTauByDateAndGaDiGaDen(departureDate, gaDi, gaDen);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }, executorService).thenAccept(allLichTrinhList -> {
                SwingUtilities.invokeLater(() -> {
                    try {
                        // Filter to only include trains that haven't departed or are in operation
                        List<LichTrinhTau> filteredLichTrinhList = allLichTrinhList.stream()
                                .filter(lichTrinh -> {
                                    TrangThai trangThai = lichTrinh.getTrangThai();
                                    return trangThai == TrangThai.CHUA_KHOI_HANH ||
                                            trangThai == TrangThai.HOAT_DONG;
                                })
                                .collect(Collectors.toList());

                        if (filteredLichTrinhList.isEmpty()) {
                            JOptionPane.showMessageDialog(this,
                                    "Không tìm thấy lịch trình tàu phù hợp đang hoạt động hoặc chưa khởi hành vào ngày " +
                                            departureDateField.getText(),
                                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            setLoading(false, "Không tìm thấy chuyến tàu");
                            return;
                        }

                        // Clear the train panel and reset global state
                        trainsPanel.removeAll();
                        selectedTrainId = "";
                        currentTrainId = "";
                        currentToaId = "";
                        currentMaLich = "";

                        trainsPanel.setLayout(new BoxLayout(trainsPanel, BoxLayout.X_AXIS));
                        trainsPanel.add(Box.createHorizontalStrut(10));

                        // Create train cards only for filtered schedules
                        for (LichTrinhTau lichTrinh : filteredLichTrinhList) {
                            // Get train object
                            Tau tau = tauDAO.getTauByLichTrinhTau(lichTrinh);

                            // Format departure time
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                            String departTime = lichTrinh.getGioDi().format(timeFormatter);

                            // Format date
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
                            String departDate = lichTrinh.getNgayDi().format(dateFormatter);

                            // Create formatted times for display
                            String departTimeFormatted = departDate + " " + departTime;

                            // Get available seats
                            long availableSeats = lichTrinhTauDAO.getAvailableSeatsBySchedule(lichTrinh.getMaLich());
                            long unavailableSeats = choNgoiDAO.unAvailableSeats(lichTrinh.getMaLich());

                            // Create train card with status indicator
                            JPanel trainCard = createTrainCard(
                                    tau.getMaTau(),
                                    departTimeFormatted,
                                    availableSeats,
                                    false, // Initially not selected
                                    lichTrinh.getTrangThai(),  // Pass the status to possibly display it
                                    lichTrinh.getMaLich() ,
                                    unavailableSeats// Store schedule ID
                            );

                            trainsPanel.add(trainCard);
                            trainsPanel.add(Box.createHorizontalStrut(10));
                        }

                        setLoading(false, "Đã tìm thấy " + filteredLichTrinhList.size() + " chuyến tàu");
                    } catch (Exception e) {
                        handleException("Lỗi khi xử lý kết quả tìm kiếm", e);
                    }
                });
            }).exceptionally(e -> {
                handleException("Lỗi khi tìm kiếm chuyến tàu", e);
                return null;
            });
        } catch (Exception e) {
            handleException("Lỗi khi tìm kiếm chuyến tàu", e);
        }
    }

    /**
     * Update header information with route details
     */
    private void updateHeaderInfo(String from, String to, String date) {
        // 'this' chính là JPanel
        Component[] components = this.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                updateHeaderInPanel((JPanel) comp, from, to, date);
            }
        }
    }


    /**
     * Update header text in panel recursively
     */
    private void updateHeaderInPanel(JPanel panel, String from, String to, String date) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] innerComps = ((JPanel) comp).getComponents();
                for (Component innerComp : innerComps) {
                    if (innerComp instanceof JLabel) {
                        JLabel label = (JLabel) innerComp;
                        if (label.getText().contains("Chiều đi")) {
                            label.setText("Chiều đi: ngày " + date + " từ " + from + " đến " + to);
                            return;
                        }
                    }
                    if (innerComp instanceof JPanel) {
                        updateHeaderInPanel((JPanel)innerComp, from, to, date);
                    }
                }
            }
        }
    }

    /**
     * Create the ticket cart panel (right side)
     */
    private JPanel createTicketItemPanel(TicketItem item) {
        // Main panel with border layout for compact display
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)
        ));
        panel.setBackground(Color.WHITE);

        // Set fixed width to prevent horizontal expansion
        panel.setMaximumSize(new Dimension(220, 80));
        panel.setPreferredSize(new Dimension(200, 70));

        // Left section - Train info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        // Train and route info
        JLabel trainLabel = new JLabel(item.trainCode + " " + item.from + "-" + item.to);
        trainLabel.setFont(new Font("Arial", Font.BOLD, 12));
        trainLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(trainLabel);

        // Date info
        JLabel dateLabel = new JLabel(item.departureDate);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(dateLabel);

        // Car and seat info
        JLabel carSeatLabel = new JLabel(item.carName + " - " + item.seatNumber);
        carSeatLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        carSeatLabel.setForeground(new Color(0, 102, 204)); // Blue color for emphasis
        carSeatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(carSeatLabel);

        // Add countdown if applicable
        if (remainingTimes.containsKey(item.seatId)) {
            int remaining = remainingTimes.get(item.seatId);
            int minutes = remaining / 60;
            int seconds = remaining % 60;
            String timeString = String.format("Còn %02d:%02d", minutes, seconds);

            JLabel timerLabel = new JLabel(timeString);
            timerLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            timerLabel.setForeground(new Color(255, 153, 0));
            timerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Set the seatId property on the label itself
            timerLabel.putClientProperty("seatId", item.seatId);

            // Add a name to the component for easier debugging
            timerLabel.setName("timerLabel_" + item.seatId);

            infoPanel.add(timerLabel);
        }

        panel.add(infoPanel, BorderLayout.CENTER);

        // Right section - Price and delete button
        JPanel rightPanel = new JPanel(new BorderLayout(5, 0));
        rightPanel.setBackground(Color.WHITE);

        // Price in red
        JLabel priceLabel = new JLabel(formatNumber(item.price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        priceLabel.setForeground(new Color(192, 0, 0)); // Dark red
        rightPanel.add(priceLabel, BorderLayout.CENTER);

        // Delete button with trash icon
        JButton deleteButton = createTrashButton();
        deleteButton.addActionListener(e -> {
            // Gọi phương thức removeFromCart để xử lý đúng cách việc xóa ghế
            removeFromCart(item.seatId);
        });
        rightPanel.add(deleteButton, BorderLayout.EAST);

        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private String formatNumber(double number) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        return format.format(number);
    }
    private JButton createTrashButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(24, 24));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Create trash icon
        button.setIcon(new ImageIcon(getClass().getResource("/Anh_HeThong/trash.png")) {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();

                // Draw simple trash icon if image not found
                g2.setColor(new Color(65, 130, 180)); // Steel blue

                // Draw trash can
                g2.fillRect(x + 5, y + 6, 12, 14);
                g2.setColor(Color.WHITE);
                g2.fillRect(x + 7, y + 8, 8, 10);

                // Draw trash lid
                g2.setColor(new Color(65, 130, 180));
                g2.fillRect(x + 3, y + 3, 16, 3);

                // Draw handle
                g2.drawLine(x + 9, y + 8, x + 9, y + 16);
                g2.drawLine(x + 13, y + 8, x + 13, y + 16);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 24;
            }

            @Override
            public int getIconHeight() {
                return 24;
            }
        });

        return button;
    }
    private void updateCartItemCountdown(String seatId, int remainingSeconds) {
        try {
            // Format the time string
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            String timeString = String.format("Còn %02d:%02d", minutes, seconds);

            // Use SwingUtilities.invokeLater for thread safety
            SwingUtilities.invokeLater(() -> {
                try {
                    // Từ đây, 'this' chính là JPanel
                    JPanel containerPanel = findMainContainerPanel(this);
                    if (containerPanel == null) return;

                    JPanel rightPanel = findRightPanel(containerPanel);
                    if (rightPanel == null) return;

                    JScrollPane scrollPane = findScrollPane(rightPanel);
                    if (scrollPane == null) return;

                    // Get the viewport and items panel
                    JViewport viewport = scrollPane.getViewport();
                    if (viewport == null) return;

                    JPanel itemsPanel = (JPanel) viewport.getView();
                    if (itemsPanel == null) return;

                    // Update timer label for seatId
                    boolean found = updateTimerInPanel(itemsPanel, seatId, timeString);

                    if (!found) {
                        System.out.println("Không tìm thấy đồng hồ đếm ngược cho ghế " + seatId);
                    }
                } catch (Exception ex) {
                    System.err.println("Lỗi khi cập nhật đếm ngược: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Lỗi chuẩn bị cập nhật đếm ngược: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JPanel findMainContainerPanel(Container contentPane) {
        for (Component comp : contentPane.getComponents()) {
            if (comp instanceof JPanel) {
                return (JPanel) comp;
            }
        }
        return null;
    }

    /**
     * Helper method to find right panel
     */
    private JPanel findRightPanel(JPanel containerPanel) {
        Component[] components = containerPanel.getComponents();
        // Right panel is typically the third component (index 2)
        if (components.length > 2 && components[2] instanceof JPanel) {
            return (JPanel) components[2];
        }
        return null;
    }

    /**
     * Helper method to find scroll pane
     */
    private JScrollPane findScrollPane(JPanel rightPanel) {
        for (Component comp : rightPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                return (JScrollPane) comp;
            }
        }
        return null;
    }
    private boolean updateTimerInPanel(JPanel panel, String seatId, String timeString) {
        // Look at each cart item
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel itemPanel = (JPanel) comp;

                // Look for the center component (info panel)
                Component centerComp = ((BorderLayout)itemPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if (centerComp instanceof JPanel) {
                    JPanel infoPanel = (JPanel) centerComp;

                    // Check each component in the info panel
                    for (Component infoComp : infoPanel.getComponents()) {
                        if (infoComp instanceof JLabel) {
                            JLabel label = (JLabel) infoComp;

                            // Print some debug information
                            Object property = label.getClientProperty("seatId");


                            // Check if this is our timer label
                            if (property != null && property.equals(seatId)) {
                                // Found it, update the text
                                label.setText(timeString);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }



    private JPanel createTicketCartPanel() {
        JPanel cartPanel = new JPanel();
        cartPanel.setLayout(new BorderLayout());
        cartPanel.setPreferredSize(new Dimension(220, 0)); // Fixed width
        cartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(activeColor);

        // Cart title with icon
        JLabel cartTitle = new JLabel(" Giỏ vé", JLabel.LEFT);
        cartTitle.setIcon(new ImageIcon(getClass().getResource("/Anh_HeThong/cart.png")) {
            // Fallback icon if resource is not found
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.WHITE);
                g2.fillOval(x, y, 18, 18);
                g2.setColor(activeColor);
                g2.fillOval(x + 3, y + 3, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillRect(x + 7, y + 3, 4, 7);
                g2.fillRect(x + 3, y + 7, 12, 4);
                g2.dispose();
            }
            public int getIconWidth() { return 20; }
            public int getIconHeight() { return 20; }
        });
        cartTitle.setFont(new Font("Arial", Font.BOLD, 14));
        cartTitle.setForeground(Color.WHITE);
        cartTitle.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        titlePanel.add(cartTitle, BorderLayout.CENTER);

        // Cart items panel
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setMaximumSize(new Dimension(220, Short.MAX_VALUE));
        itemsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Empty cart message
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(15, 5, 5, 5));
        emptyPanel.setOpaque(false);

        JLabel emptyLabel = new JLabel("Giỏ vé trống", JLabel.CENTER);
        emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        emptyLabel.setForeground(Color.GRAY);
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        itemsPanel.add(emptyPanel);

        // Scrollable cart with enhanced scrolling functionality
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // For smoother scrolling
        scrollPane.getVerticalScrollBar().setBlockIncrement(64); // Faster page scrolling

        // Use preferred size but don't set minimum size to allow dynamic resizing
        scrollPane.setPreferredSize(new Dimension(220, 250));

        // Make sure the viewport tracks the view's size changes
        scrollPane.getViewport().setViewSize(itemsPanel.getPreferredSize());

        // Make the scrollbar look better with custom UI
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 180, 180);
                this.trackColor = Color.WHITE;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        // Total panel
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        totalPanel.setBackground(new Color(245, 245, 245));
        totalLabel = new JLabel("Tổng cộng: 0 đ", JLabel.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JButton checkoutButton = new JButton("Thanh toán");
        checkoutButton.setPreferredSize(new Dimension(200, 40));
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 17));
        checkoutButton.setBackground(activeColor);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);

        // Add hover effect to checkout button
        checkoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                checkoutButton.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                checkoutButton.setBackground(activeColor);
            }
        });
        checkoutButton.addActionListener(e -> {
            if (cartItems.isEmpty()) {
                JOptionPane.showMessageDialog(
                        TrainTicketBookingSystem.this,
                        "Giỏ vé trống. Vui lòng chọn vé trước khi thanh toán.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            // Create a map of seat IDs to schedule IDs
            Map<String, String> ticketsToCheckout = new HashMap<>();
            for (TicketItem item : cartItems) {
                ticketsToCheckout.put(item.seatId, currentMaLich);
            }

            // Open checkout screen with only the necessary identifiers and employee information
            ThanhToanGUI checkoutScreen = null;
            try {
                checkoutScreen = new ThanhToanGUI(ticketsToCheckout, nhanVien);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
            checkoutScreen.setVisible(true);
        });

        // Open checkout screen

        totalPanel.add(totalLabel, BorderLayout.NORTH);
        totalPanel.add(checkoutButton, BorderLayout.SOUTH);

        // Add components to cart panel
        cartPanel.add(titlePanel, BorderLayout.NORTH);
        cartPanel.add(scrollPane, BorderLayout.CENTER);
        cartPanel.add(totalPanel, BorderLayout.SOUTH);

        return cartPanel;
    }

    /**
     * Helper method to show/hide loading indicator
     * @param isLoading Whether loading is in progress
     * @param status Status message to display
     */
    private void setLoading(boolean isLoading, String status) {
        SwingUtilities.invokeLater(() -> {
            if (progressLoading != null) {
                progressLoading.setVisible(isLoading);
                progressLoading.setIndeterminate(isLoading);
            }
            if (lblStatus != null) {
                lblStatus.setText(status);
            }
            // Disable/enable relevant UI components during loading
            setComponentsEnabled(!isLoading);
        });
    }

    /**
     * Helper method for exception handling
     * @param message Error message
     * @param e Exception that occurred
     */
    private void handleException(String message, Throwable e) {
        LOGGER.log(Level.SEVERE, message, e);
        SwingUtilities.invokeLater(() -> {
            setLoading(false, "Đã xảy ra lỗi");
            String errorMessage = message;
            if (e instanceof RemoteException) {
                errorMessage += "\nLỗi kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.";
            } else if (e.getCause() != null) {
                errorMessage += "\nChi tiết: " + e.getCause().getMessage();
            } else {
                errorMessage += "\nChi tiết: " + e.getMessage();
            }
            JOptionPane.showMessageDialog(this,
                    errorMessage,
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Cleanup method to shutdown ExecutorService
     */
    public void cleanup() {
        try {
            // Stop seat status polling
            stopSeatStatusPolling();

            // Release all selected seats
            for (String seatId : new ArrayList<>(selectedSeatIds)) {
                releaseReservation(seatId);
            }
            selectedSeatIds.clear();

            // Shutdown executor service
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }

            // Clear any pending timers
            for (Timer timer : reservationTimers.values()) {
                timer.stop();
            }
            reservationTimers.clear();
            remainingTimes.clear();

            // Clear DAO references to allow garbage collection
            lichTrinhTauDAO = null;
            tauDAO = null;
            toaTauDAO = null;
            choNgoiDAO = null;
            khuyenMaiDAO = null;
            nhanVienDAO = null;
            choNgoiGiuDAO = null;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during cleanup", e);
        }
    }

    private void setComponentsEnabled(boolean enabled) {
        // Add components that should be disabled during loading
        if (departureField != null) departureField.setEnabled(enabled);
        if (arrivalField != null) arrivalField.setEnabled(enabled);
        if (departureDateField != null) departureDateField.setEnabled(enabled);
        if (returnDateField != null) returnDateField.setEnabled(enabled);
        if (oneWayRadio != null) oneWayRadio.setEnabled(enabled);
        if (roundTripRadio != null) roundTripRadio.setEnabled(enabled);
        // Add other components as needed
    }

    /**
     * Class to represent a ticket item in the cart
     */
    private class TicketItem {
        String trainCode;
        String seatId;      // The actual seat ID used by the system
        String seatNumber;  // The display name shown to user
        String from;
        String to;
        String departureDate;
        double price;
        String carId;       // The train car ID
        String carName;     // The train car name or number

        public TicketItem(String trainCode, String seatId, String seatNumber,
                          String from, String to, String departureDate, double price,
                          String carId, String carName) {
            this.trainCode = trainCode;
            this.seatId = seatId;
            this.seatNumber = seatNumber;
            this.from = from;
            this.to = to;
            this.departureDate = departureDate;
            this.price = price;
            this.carId = carId;
            this.carName = carName;
        }
    }

    /**
     * Start periodic polling for seat status updates
     */
    private void startSeatStatusPolling() {
        if (seatStatusRefreshTimer != null) {
            seatStatusRefreshTimer.stop();
        }

        // Create a timer that periodically refreshes the seat status
        seatStatusRefreshTimer = new Timer(SEAT_REFRESH_INTERVAL, e -> {
            if (!currentMaLich.isEmpty() && !currentToaId.isEmpty()) {
                try {
                    // Check if the current seat chart needs updating
                    refreshSeatStatuses();
                } catch (Exception ex) {
                    // Log but don't show error to avoid frequent popups
                    LOGGER.log(Level.WARNING, "Error refreshing seat statuses", ex);
                }
            }
        });

        // Start the timer
        seatStatusRefreshTimer.start();
        LOGGER.info("Started seat status polling timer");
    }

    /**
     * Stop periodic polling for seat status updates
     */
    private void stopSeatStatusPolling() {
        if (seatStatusRefreshTimer != null) {
            seatStatusRefreshTimer.stop();
            seatStatusRefreshTimer = null;
            LOGGER.info("Stopped seat status polling timer");
        }
    }

    /**
     * Refresh the status of all seats in the current car
     */
    private void refreshSeatStatuses() {
        if (currentMaLich.isEmpty() || currentToaId.isEmpty()) {
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                // Get updated seat statuses from server
                return choNgoiDAO.getAvailableSeatsMapByScheduleAndToa(currentMaLich, currentToaId);
            } catch (RemoteException e) {
                throw new RuntimeException("Failed to refresh seat statuses", e);
            }
        }, executorService).thenAccept(seatsMap -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Update the UI with the new seat statuses
                    updateSeatStatuses(seatsMap);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error updating seat statuses in UI", e);
                }
            });
        }).exceptionally(e -> {
            LOGGER.log(Level.WARNING, "Failed to fetch seat statuses", e);
            return null;
        });
    }

    /**
     * Update the UI with the new seat statuses
     */
    private void updateSeatStatuses(Map<String, String> seatsMap) {
        // Go through all seats and update their status
        for (Map.Entry<String, String> entry : seatsMap.entrySet()) {
            String seatId = entry.getKey();
            String status = entry.getValue();

            // Ghế đã nằm trong giỏ hàng không cần cập nhật
            if (selectedSeatIds.contains(seatId)) {
                continue;
            }

            // Đảm bảo ghế "Đã đặt" luôn được hiển thị chính xác với màu xám
            if (STATUS_BOOKED.equals(status)) {
                SwingUtilities.invokeLater(() -> {
                    JPanel seatPanel = seatPanelMap.get(seatId);
                    if (seatPanel != null) {
                        seatPanel.setBackground(SEAT_BOOKED_COLOR);
                        seatPanel.repaint();
                    }
                });
            }
            else {
                // Cập nhật trạng thái cho các ghế khác
                updateSeatStatus(seatId, status);
            }
        }
    }

}

