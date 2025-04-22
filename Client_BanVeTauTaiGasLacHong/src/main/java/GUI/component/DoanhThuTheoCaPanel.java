package GUI.component;

import com.toedter.calendar.JDateChooser;
import dao.HoaDonDAO;
import dao.impl.HoaDonDAOImpl;
import model.HoaDon;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DoanhThuTheoCaPanel extends JPanel implements ActionListener {
    // RMI Server
    private static final String RMI_SERVER_IP = "192.168.1.39";
    private static final int RMI_SERVER_PORT = 9090;


    private Color primaryColor = new Color(41, 128, 185); // Màu xanh dương
    private Color successColor = new Color(46, 204, 113); // Màu xanh lá
    private Color warningColor = new Color(243, 156, 18); // Màu vàng cam
    private Color dangerColor = new Color(231, 76, 60);   // Màu đỏ
    private Color grayColor = new Color(108, 117, 125);   // Màu xám
    private Color darkTextColor = new Color(52, 73, 94);  // Màu chữ tối
    private Color hoverColor = new Color(66, 139, 202);
    private Color lightBackground = new Color(240, 240, 240); // Màu nền nhạt
    private Font labelFont = new Font("Segoe UI", Font.BOLD, 12); // Font cho label
    private Dimension inputSize = new Dimension(200, 30); // Kích thước cho input
    private Font mediumFont = new Font("Segoe UI", Font.PLAIN, 14);
    private Font bold = new Font("Segoe UI", Font.BOLD, 20);
    private HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();
    private IconFactory iconFactory = new IconFactory();
    private final int BUTTON_ICON_SIZE = 18;
    private final int BUTTON_HEIGHT = 30;


        private JTabbedPane tabbedPane;
        private JPanel tongQuanPanel;
        private JPanel chiTietPanel;
    private JDateChooser dateFrom;
    private JDateChooser dateTo;
    private JRadioButton ca1;
    private JRadioButton ca2;
    private JRadioButton ca3;
    private JButton btnLoc;


    public DoanhThuTheoCaPanel() throws RemoteException {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            initComponents();
            connectToServer();
        }

        private void initComponents() {
            tabbedPane = new JTabbedPane();
            tongQuanPanel = new JPanel(new BorderLayout());
            chiTietPanel = new JPanel(new BorderLayout());

//            // Biểu đồ tổng quan (bar chart theo nhân viên và ngày)
//            tongQuanPanel.add(createBarChartPanel(), BorderLayout.CENTER);
//
//            // Biểu đồ chi tiết (bar + line, có thông tin nhân viên)
//            chiTietPanel.add(createDetailedChartPanel(), BorderLayout.CENTER);

            tabbedPane.addTab("Tổng quan", tongQuanPanel);
            tabbedPane.addTab("Chi tiết", chiTietPanel);

            add(tabbedPane, BorderLayout.CENTER);
            add(createFilterPanel(),BorderLayout.NORTH);

            //đăng kí sự kiện
            ca1.addActionListener(this);
            ca2.addActionListener(this);
            ca3.addActionListener(this);
            btnLoc.addActionListener(this);
        }
    private void capNhatBarChart(Map<String, Map<String, Double>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String maNV : data.keySet()) {
            Map<String, Double> ngayDoanhThu = data.get(maNV);
            for (String ngay : ngayDoanhThu.keySet()) {
                dataset.addValue(ngayDoanhThu.get(ngay), maNV, ngay);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Doanh Thu Theo Nhân Viên",
                "Ngày",
                "Doanh Thu (VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        styleChart(chart);
        tongQuanPanel.removeAll();
        tongQuanPanel.add(wrapChart(chart, "Biểu Đồ Tổng Quan"), BorderLayout.CENTER);
        tongQuanPanel.revalidate();
        tongQuanPanel.repaint();
    }
    private void capNhatLineChart(Map<String, Map<String, Double>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (String tenNV : data.keySet()) {
            Map<String, Double> ngayDoanhThu = data.get(tenNV);
            for (String ngay : ngayDoanhThu.keySet()) {
                dataset.addValue(ngayDoanhThu.get(ngay), tenNV, ngay);
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Chi Tiết Doanh Thu Theo Nhân Viên",
                "Ngày",
                "Doanh Thu (VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        styleSmoothLine(chart);
        chiTietPanel.removeAll();
        chiTietPanel.add(wrapChart(chart, "Biểu Đồ Chi Tiết"), BorderLayout.CENTER);
        chiTietPanel.revalidate();
        chiTietPanel.repaint();
    }
    private JPanel wrapChart(JFreeChart chart, String title) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(lightBackground);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(lightBackground);
        container.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1, true),
                title,
                TitledBorder.CENTER, TitledBorder.TOP,
                mediumFont, primaryColor));
        container.add(chartPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createFilterPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);

        // ====== Tiêu đề ======
        JPanel pnlTitle  = new JPanel(new BorderLayout());
        pnlTitle.setBackground(primaryColor);
        JLabel title = new JLabel("Thống kê doanh thu theo ca", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(primaryColor);
        title.setForeground(Color.WHITE);
        title.setFont(bold);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); // full width
        pnlTitle.add(title,BorderLayout.CENTER);

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
        // ====== Chọn ngày bắt đầu - kết thúc ======
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        datePanel.setBackground(Color.WHITE);
        JLabel lblFrom = new JLabel("Từ:");
        lblFrom.setFont(labelFont);
        dateFrom = new JDateChooser();
        dateFrom.setDateFormatString("yyyy-MM-dd");
        dateFrom.setPreferredSize(new Dimension(150, 30));

        JLabel lblTo = new JLabel("Đến:");
        dateTo = new JDateChooser();
        dateTo.setDateFormatString("yyyy-MM-dd");
        dateTo.setPreferredSize(new Dimension(150, 30));
        lblTo.setFont(labelFont);

        // Ràng buộc ngày: ngày bắt đầu <= ngày kết thúc, ngày kết thúc <= ngày hiện tại
        dateFrom.addPropertyChangeListener("date", evt -> {
            Date selectedDate = dateFrom.getDate();
            if (selectedDate != null && dateTo.getDate() != null && selectedDate.after(dateTo.getDate())) {
                dateTo.setDate(selectedDate);
            }
        });

        dateTo.addPropertyChangeListener("date", evt -> {
            Date selectedDate = dateTo.getDate();
            Date today = new Date();
            if (selectedDate != null && selectedDate.after(today)) {
                dateTo.setDate(today);
            }
            if (selectedDate != null && dateFrom.getDate() != null && selectedDate.before(dateFrom.getDate())) {
                dateFrom.setDate(selectedDate);
            }
        });
        dateTo.setMaxSelectableDate(new Date()); // Không cho chọn ngày tương lai

        datePanel.add(lblFrom);
        datePanel.add(dateFrom);
        datePanel.add(lblTo);
        datePanel.add(dateTo);
//        btnLoc = new JButton("Lọc");
        btnLoc = new RoundedButton("Lọc", iconFactory.getWhiteIcon("filter", BUTTON_ICON_SIZE, BUTTON_ICON_SIZE));
        btnLoc.setFont(new Font("Arial", Font.BOLD, 13));
        btnLoc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLoc.setIconTextGap(8);
        btnLoc.setPreferredSize(new Dimension(100, BUTTON_HEIGHT));
        btnLoc.setBackground(primaryColor);
        btnLoc.setForeground(Color.WHITE);
        datePanel.add(btnLoc);

        // ====== Chọn ca làm ======
        JPanel shiftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        shiftPanel.setBackground(Color.WHITE);

        ca1 = new JRadioButton("Ca 1");
        ca2 = new JRadioButton("Ca 2");
        ca3 = new JRadioButton("Ca 3");

        ButtonGroup caGroup = new ButtonGroup();
        caGroup.add(ca1);
        caGroup.add(ca2);
        caGroup.add(ca3);

        shiftPanel.add(ca1);
        shiftPanel.add(ca2);
        shiftPanel.add(ca3);

        // Thêm các thành phần vào mainPanel
        mainPanel.add(pnlTitle);
        mainPanel.add(datePanel);
        mainPanel.add(shiftPanel);

        return mainPanel;
    }

    private JPanel createBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(120000, "NV001", "2025-04-20");
        dataset.addValue(95000, "NV002", "2025-04-20");

        JFreeChart barChart = ChartFactory.createBarChart(
                "Doanh Thu Theo Nhân Viên",
                "Ngày",
                "Doanh Thu (VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        styleChart(barChart);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(lightBackground);

        // Bọc vào JPanel có Border & nền
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(lightBackground);
        container.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1, true),
                "Biểu Đồ Tổng Quan",
                TitledBorder.CENTER, TitledBorder.TOP,
                mediumFont, primaryColor));
        container.add(chartPanel, BorderLayout.CENTER);

        return container;
    }


    private JPanel createDetailedChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(100000, "NV001 - Nguyễn Văn A", "2025-04-20");
        dataset.addValue(110000, "NV001 - Nguyễn Văn A", "2025-04-21");

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Chi Tiết Doanh Thu Theo Nhân Viên",
                "Ngày",
                "Doanh Thu (VNĐ)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        styleSmoothLine(lineChart);
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(lightBackground);

        // Bọc vào JPanel có Border & nền
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(lightBackground);
        container.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1, true),
                "Biểu Đồ Chi Tiết",
                TitledBorder.CENTER, TitledBorder.TOP,
                mediumFont, primaryColor));
        container.add(chartPanel, BorderLayout.CENTER);

        return container;
    }


        private void styleChart(JFreeChart chart) {
            chart.setBackgroundPaint(Color.WHITE);
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.GRAY);

            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(41, 128, 185)); // Primary color
            renderer.setItemMargin(0.02);
        }

        private void styleSmoothLine(JFreeChart chart) {
            chart.setBackgroundPaint(Color.WHITE);
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.GRAY);

            LineAndShapeRenderer renderer = new LineAndShapeRenderer();
            renderer.setSeriesPaint(0, new Color(243, 156, 18)); // Warning color
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            plot.setRenderer(renderer);
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
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Thống kê Doanh Thu Theo Ca");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            DoanhThuTheoCaPanel panel = null;
            try {
                panel = new DoanhThuTheoCaPanel();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            frame.add(panel);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
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
    private void locDuLieuVaCapNhatBieuDo() {
        Date fromDate = dateFrom.getDate();
        Date toDate = dateTo.getDate();

        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ca = 0;
        if (ca1.isSelected()) ca = 1;
        else if (ca2.isSelected()) ca = 2;
        else if (ca3.isSelected()) ca = 3;

        if (ca == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ca làm.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Convert java.util.Date -> java.time.LocalDate
            LocalDate startDate = fromDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = toDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

            // Lấy danh sách hóa đơn từ DAO
            List<HoaDon> hoaDons = hoaDonDAO.getHoaDonsByDateRangeAndShift(startDate, endDate, ca);

            // Chuẩn bị dữ liệu để cập nhật biểu đồ
            Map<String, Map<String, Double>> dataMap = new java.util.HashMap<>();

            for (HoaDon hd : hoaDons) {
                String maNV = hd.getNv().getMaNV();
                String ngay = hd.getNgayLap().toLocalDate().toString();
                double tongTien = hd.getTongTien();

                dataMap.putIfAbsent(maNV, new java.util.HashMap<>());
                Map<String, Double> nvData = dataMap.get(maNV);
                nvData.put(ngay, nvData.getOrDefault(ngay, 0.0) + tongTien);
            }

            // Cập nhật cả hai biểu đồ
            capNhatBarChart(dataMap);   // Tổng quan theo nhân viên
            capNhatLineChart(dataMap);  // Chi tiết theo ngày

        } catch (RemoteException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lấy dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean isactive = false;
    private void connectToServer() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_SERVER_IP, RMI_SERVER_PORT);
            hoaDonDAO = (HoaDonDAO) registry.lookup("hoaDonDAO");
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if(isactive){
            if (e.getSource() == btnLoc) {
                locDuLieuVaCapNhatBieuDo();
            }
        }else {
            JOptionPane.showMessageDialog(null, "Không thể kết nối dữ liệu, vui lòng kiểm tra kết nối: ", "Đã xãy ra lỗi", JOptionPane.ERROR_MESSAGE);
        }

    }

}
