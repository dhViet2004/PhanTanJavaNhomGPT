package GUI.component;

import com.toedter.calendar.JDateChooser;
import dao.ThongKeDAO;
import model.KetQuaThongKeDoanhThu;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Dự án: Project_BanVeTauTaiGasLacHong
 * @Class: ThongKeDoanhThuBanVePanel
 * @Tạo vào ngày: 21/04/2025
 * @Tác giả: Nguyen Huu Sang
 */


/**
 * Class quản lý giao diện thống kê doanh thu bán vé
 */
public class ThongKeDoanhThuPanel extends JPanel {
    private static final String RMI_SERVER_IP = "192.168.2.21";
    private static final int RMI_SERVER_PORT = 9090;


    // Màu sắc và theme
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(39, 174, 96);
    private static final Color ACCENT_COLOR = new Color(243, 156, 18);

    // Các tùy chọn cho combobox
    private static final String[] TIME_RANGES = {"Ngày", "Tháng", "Quý", "Năm"};
    private static final String[] CHART_TYPES = {"Biểu Đồ Cột", "Biểu Đồ Đường", "Biểu Đồ Tròn", "Kết Hợp Cột-Đường"};
    private static final String[] GROUP_BY_OPTIONS = {"Tuyến Tàu", "Loại Toa", "Loại Vé"};

    // Các thành phần giao diện
    private JDateChooser dateFrom;
    private JDateChooser dateTo;
    private JComboBox<String> cboTimeRange;
    private JComboBox<String> cboChartType;
    private JComboBox<String> cboGroupBy;
    private JButton btnRefresh;
    private JButton btnExport;

    // Panels
    private JPanel chartPanel;
    private JPanel summaryPanel;
    private JPanel aiAnalysisPanel;

    // DAO
    private ThongKeDAO thongKeDAO;

    // Dữ liệu
    private List<KetQuaThongKeDoanhThu> thongKeData;

    // Format tiền tệ
    private DecimalFormat currencyFormatter = new DecimalFormat("#,###");

    /**
     * Constructor
     */
    public ThongKeDoanhThuPanel() {
        try {
            // Thiết lập look and feel hiện đại
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Kết nối đến RMI Server để lấy DAO
            Registry registry = LocateRegistry.getRegistry(RMI_SERVER_IP, RMI_SERVER_PORT);
            thongKeDAO = (ThongKeDAO) registry.lookup("thongKeDAO");

            System.out.println("Đã kết nối thành công đến ThongKeDAO qua RMI");

            // Khởi tạo các thành phần UI
            initComponents();
            loadData();

        } catch (RemoteException | NotBoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi kết nối đến RMI server: " + e.getMessage(),
                    "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo giao diện
     */
    private void initComponents() {
        // Thiết lập giao diện chính
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(BACKGROUND_COLOR);

        // Panel điều khiển trên cùng
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Panel chứa các biểu đồ
        chartPanel = new JPanel(new BorderLayout(10, 10));
        chartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true),
                "Biểu Đồ Thống Kê Doanh Thu",
                TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR));
        chartPanel.setBackground(BACKGROUND_COLOR);

        // Panel hiển thị thông tin tóm tắt
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true),
                "Tóm Tắt Doanh Thu",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), SECONDARY_COLOR));
        summaryPanel.setBackground(BACKGROUND_COLOR);

        // Panel phân tích AI
        aiAnalysisPanel = new JPanel();
        aiAnalysisPanel.setLayout(new BoxLayout(aiAnalysisPanel, BoxLayout.Y_AXIS));
        aiAnalysisPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
                "Phân Tích & Dự Đoán",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), ACCENT_COLOR));
        aiAnalysisPanel.setBackground(BACKGROUND_COLOR);

        // Panel thông tin bên phải
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(summaryPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(aiAnalysisPanel);
        rightPanel.setBackground(BACKGROUND_COLOR);

        // Thiết lập kích thước cho panel bên phải
        rightPanel.setPreferredSize(new Dimension(350, getHeight()));

        // Panel chính chứa biểu đồ và thông tin
        JSplitPane mainSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, chartPanel, rightPanel);
        mainSplitPane.setResizeWeight(0.7);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerSize(8);
        mainSplitPane.setBorder(null);
        add(mainSplitPane, BorderLayout.CENTER);

        // Thiết lập theme cho biểu đồ
        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();
        theme.setTitlePaint(new Color(44, 62, 80));
        theme.setSubtitlePaint(new Color(52, 73, 94));
        theme.setLegendBackgroundPaint(new Color(255, 255, 255, 100));
        theme.setPlotBackgroundPaint(new Color(255, 255, 255));
        theme.setChartBackgroundPaint(new Color(255, 255, 255));
        theme.setGridBandPaint(new Color(252, 252, 252));
        theme.setAxisLabelPaint(new Color(44, 62, 80));
        theme.setTickLabelPaint(new Color(44, 62, 80));
        theme.setBarPainter(new StandardBarPainter());
        theme.setXYBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
        theme.setShadowVisible(false);
        ChartFactory.setChartTheme(theme);
    }

    /**
     * Tạo panel điều khiển phía trên
     */
    private JPanel createControlPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout(10, 10));
        outerPanel.setBackground(BACKGROUND_COLOR);
        outerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                "Bộ Lọc Dữ Liệu",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), new Color(60, 60, 60)));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Thời gian từ
        JLabel lblFrom = new JLabel("Từ ngày:");
        lblFrom.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblFrom, gbc);

        dateFrom = new JDateChooser();
        dateFrom.setDate(Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateFrom.setPreferredSize(new Dimension(150, 30));
        dateFrom.setFont(new Font("Arial", Font.PLAIN, 13));
        dateFrom.setDateFormatString("dd/MM/yyyy");
        gbc.gridx = 1;
        panel.add(dateFrom, gbc);

        // Thời gian đến
        JLabel lblTo = new JLabel("Đến ngày:");
        lblTo.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 2;
        panel.add(lblTo, gbc);

        dateTo = new JDateChooser();
        dateTo.setDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateTo.setPreferredSize(new Dimension(150, 30));
        dateTo.setFont(new Font("Arial", Font.PLAIN, 13));
        dateTo.setDateFormatString("dd/MM/yyyy");
        gbc.gridx = 3;
        panel.add(dateTo, gbc);

        // Loại thời gian
        JLabel lblTimeRange = new JLabel("Thống kê theo:");
        lblTimeRange.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblTimeRange, gbc);

        cboTimeRange = new JComboBox<>(TIME_RANGES);
        cboTimeRange.setPreferredSize(new Dimension(150, 30));
        cboTimeRange.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(cboTimeRange, gbc);

        // Loại biểu đồ
        JLabel lblChartType = new JLabel("Loại biểu đồ:");
        lblChartType.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 2;
        panel.add(lblChartType, gbc);

        cboChartType = new JComboBox<>(CHART_TYPES);
        cboChartType.setPreferredSize(new Dimension(150, 30));
        cboChartType.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridx = 3;
        panel.add(cboChartType, gbc);

        // Nhóm theo
        JLabel lblGroupBy = new JLabel("Nhóm theo:");
        lblGroupBy.setFont(new Font("Arial", Font.BOLD, 13));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblGroupBy, gbc);

        cboGroupBy = new JComboBox<>(GROUP_BY_OPTIONS);
        cboGroupBy.setPreferredSize(new Dimension(150, 30));
        cboGroupBy.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridx = 1;
        panel.add(cboGroupBy, gbc);

        outerPanel.add(panel, BorderLayout.CENTER);

        // Panel chứa các nút điều khiển
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        // Nút làm mới
        btnRefresh = createStyledButton("Cập Nhật Biểu Đồ", PRIMARY_COLOR, createRefreshIcon(20));
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });
        buttonPanel.add(btnRefresh);

        // Nút xuất dữ liệu
        btnExport = createStyledButton("Xuất Báo Cáo", SECONDARY_COLOR, createExportIcon(20));
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });
        buttonPanel.add(btnExport);

        outerPanel.add(buttonPanel, BorderLayout.SOUTH);

        return outerPanel;
    }

    /**
     * Tạo nút với style đẹp
     */
    private JButton createStyledButton(String text, Color color, Icon icon) {
        JButton button = new JButton(text);
        button.setIcon(icon);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(180, 36));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    /**
     * Tạo biểu tượng làm mới (refresh)
     */
    private ImageIcon createRefreshIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Thiết lập chế độ làm mịn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int padding = 2;
        int arcSize = size - 2 * padding;

        // Vẽ vòng tròn với khoảng hở
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(new Arc2D.Float(padding, padding, arcSize, arcSize, 30, 300, Arc2D.OPEN));

        // Vẽ mũi tên
        int arrowSize = size / 3;
        int arrowThickness = size / 6;

        // Mũi tên trên
        int x1 = padding + arcSize;
        int y1 = padding + arcSize / 3;

        Path2D.Float arrow = new Path2D.Float();
        arrow.moveTo(x1, y1);
        arrow.lineTo(x1 - arrowThickness, y1 - arrowThickness);
        arrow.lineTo(x1 + arrowThickness, y1 - arrowThickness);
        arrow.closePath();

        g2d.fill(arrow);

        // Giải phóng tài nguyên
        g2d.dispose();

        return new ImageIcon(image);
    }

    /**
     * Tạo biểu tượng xuất báo cáo
     */
    private ImageIcon createExportIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Thiết lập chế độ làm mịn
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = 2;
        int documentWidth = size - 2 * margin;
        int documentHeight = (int)(size * 0.75);

        // Vẽ tài liệu (hình chữ nhật với góc bo tròn)
        g2d.setColor(Color.WHITE);
        g2d.fill(new RoundRectangle2D.Float(margin, margin, documentWidth, documentHeight, 4, 4));

        // Vẽ các dòng văn bản trên tài liệu
        g2d.setColor(new Color(255, 255, 255, 180));
        for (int i = 1; i <= 3; i++) {
            int y = margin + (documentHeight / 4) * i;
            g2d.fillRect(margin + 3, y - 1, documentWidth - 6, 2);
        }

        // Tạo mũi tên xuống
        g2d.setColor(Color.WHITE);

        // Vẽ thân mũi tên
        g2d.fillRect(size / 2 - 2, documentHeight, 4, size - documentHeight - margin - 5);

        // Vẽ đầu mũi tên
        int arrowWidth = size / 3;
        int arrowHeight = size / 5;
        int arrowX = size / 2 - arrowWidth / 2;
        int arrowY = size - margin - arrowHeight;

        int[] xPoints = {arrowX, size / 2, arrowX + arrowWidth};
        int[] yPoints = {arrowY, size - margin, arrowY};
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Giải phóng tài nguyên
        g2d.dispose();

        return new ImageIcon(image);
    }

    /**
     * Tải dữ liệu thống kê từ cơ sở dữ liệu
     */
    private void loadData() {
        try {
            // Hiển thị thông báo nhỏ trên thanh trạng thái thay vì dialog
            btnRefresh.setEnabled(false);
            btnRefresh.setText("Đang tải dữ liệu...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Tạo một SwingWorker để tải dữ liệu trong nền
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        // Lấy thông tin từ các điều khiển
                        LocalDate fromDate = dateFrom.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        LocalDate toDate = dateTo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        String timeRangeType = (String) cboTimeRange.getSelectedItem();

                        // Lấy dữ liệu từ DAO
                        thongKeData = thongKeDAO.thongKeDoanhThuTheoThoiGian(fromDate, toDate, timeRangeType);
                    } catch (Exception e) {
                        throw e;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    // Khôi phục trạng thái nút và con trỏ
                    btnRefresh.setEnabled(true);
                    btnRefresh.setText("Cập Nhật Biểu Đồ");
                    setCursor(Cursor.getDefaultCursor());

                    try {
                        get(); // Kiểm tra xem có lỗi không

                        // Nếu không có dữ liệu, hiển thị thông báo
                        if (thongKeData == null || thongKeData.isEmpty()) {
                            JOptionPane.showMessageDialog(ThongKeDoanhThuPanel.this,
                                    "Không có dữ liệu trong khoảng thời gian đã chọn",
                                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        // Cập nhật giao diện
                        updateCharts();
                        updateSummary();
//                        updateAIAnalysis();

                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ThongKeDoanhThuPanel.this,
                                "Lỗi khi tải dữ liệu: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };

            worker.execute();

        } catch (Exception e) {
            btnRefresh.setEnabled(true);
            btnRefresh.setText("Cập Nhật Biểu Đồ");
            setCursor(Cursor.getDefaultCursor());

            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật các biểu đồ dựa trên dữ liệu
     */
    private void updateCharts() {
        // Xóa biểu đồ cũ nếu có
        chartPanel.removeAll();

        String chartType = (String) cboChartType.getSelectedItem();
        String groupBy = (String) cboGroupBy.getSelectedItem();

        JPanel newChartPanel = null;

        switch (chartType) {
            case "Biểu Đồ Cột":
                newChartPanel = createBarChart(groupBy);
                break;
            case "Biểu Đồ Đường":
                newChartPanel = createLineChart(groupBy);
                break;
            case "Biểu Đồ Tròn":
                newChartPanel = createPieChart(groupBy);
                break;
            case "Kết Hợp Cột-Đường":
                newChartPanel = createCombinedChart(groupBy);
                break;
        }

        if (newChartPanel != null) {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(BACKGROUND_COLOR);
            wrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
            wrapper.add(newChartPanel, BorderLayout.CENTER);
            chartPanel.add(wrapper, BorderLayout.CENTER);
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    /**
     * Tạo biểu đồ cột
     */
    private ChartPanel createBarChart(String groupBy) {
        DefaultCategoryDataset dataset = createCategoryDataset(groupBy);

        JFreeChart chart = ChartFactory.createBarChart(
                "Thống Kê Doanh Thu Theo " + groupBy,  // Tiêu đề
                "Thời Gian",                    // Trục x
                "Doanh Thu (VNĐ)",              // Trục y
                dataset,                        // Dữ liệu
                PlotOrientation.VERTICAL,       // Hướng
                true,                           // Hiển thị chú thích
                true,                           // Tooltip
                false                           // URL
        );

        // Chỉnh sửa biểu đồ
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // Màu sắc thanh biểu đồ
        renderer.setSeriesPaint(0, new Color(41, 128, 185));
        renderer.setSeriesPaint(1, new Color(39, 174, 96));
        renderer.setSeriesPaint(2, new Color(142, 68, 173));
        renderer.setSeriesPaint(3, new Color(243, 156, 18));
        renderer.setSeriesPaint(4, new Color(231, 76, 60));

        renderer.setItemMargin(0.1);  // Khoảng cách giữa các cột
        renderer.setShadowVisible(false);

        // Định dạng trục x
        CategoryAxis axis = plot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        axis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        axis.setLabelFont(new Font("Arial", Font.BOLD, 14));

        // Định dạng trục y
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));
        rangeAxis.setNumberFormatOverride(currencyFormatter);

        // Định dạng tiêu đề
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(BACKGROUND_COLOR);

        return chartPanel;
    }

    /**
     * Tạo biểu đồ đường
     */
    private ChartPanel createLineChart(String groupBy) {
        DefaultCategoryDataset dataset = createCategoryDataset(groupBy);

        JFreeChart chart = ChartFactory.createLineChart(
                "Xu Hướng Doanh Thu Theo " + groupBy, // Tiêu đề
                "Thời Gian",                   // Trục x
                "Doanh Thu (VNĐ)",             // Trục y
                dataset,                       // Dữ liệu
                PlotOrientation.VERTICAL,      // Hướng
                true,                          // Hiển thị chú thích
                true,                          // Tooltip
                false                          // URL
        );

        // Chỉnh sửa biểu đồ
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

        // Hiển thị điểm dữ liệu và đường
        renderer.setDefaultShapesVisible(true);
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(true);
        renderer.setDefaultFillPaint(Color.WHITE);

        // Màu sắc đường biểu đồ
        renderer.setSeriesPaint(0, new Color(41, 128, 185));
        renderer.setSeriesPaint(1, new Color(39, 174, 96));
        renderer.setSeriesPaint(2, new Color(142, 68, 173));
        renderer.setSeriesPaint(3, new Color(243, 156, 18));
        renderer.setSeriesPaint(4, new Color(231, 76, 60));

        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        renderer.setSeriesStroke(2, new BasicStroke(2.5f));
        renderer.setSeriesStroke(3, new BasicStroke(2.5f));
        renderer.setSeriesStroke(4, new BasicStroke(2.5f));

        // Định dạng trục x
        CategoryAxis axis = plot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        axis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        axis.setLabelFont(new Font("Arial", Font.BOLD, 14));

        // Định dạng trục y
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        rangeAxis.setLabelFont(new Font("Arial", Font.BOLD, 14));
        rangeAxis.setNumberFormatOverride(currencyFormatter);

        // Định dạng tiêu đề
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(BACKGROUND_COLOR);

        return chartPanel;
    }

    /**
     * Tạo biểu đồ tròn
     */
    private ChartPanel createPieChart(String groupBy) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        // Tổng hợp dữ liệu theo nhóm
        Map<String, Double> groupData = new HashMap<>();

        for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
            String key = "";
            switch (groupBy) {
                case "Tuyến Tàu":
                    key = ketQua.getTenTuyen();
                    break;
                case "Loại Toa":
                    key = ketQua.getLoaiToa();
                    break;
                case "Loại Vé":
                    key = ketQua.getLoaiVe();
                    break;
            }

            groupData.put(key, groupData.getOrDefault(key, 0.0) + ketQua.getDoanhThu());
        }

        // Thêm dữ liệu vào dataset
        for (Map.Entry<String, Double> entry : groupData.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Tỷ Lệ Doanh Thu Theo " + groupBy,   // Tiêu đề
                dataset,                      // Dữ liệu
                true,                         // Hiển thị chú thích
                true,                         // Tooltip
                false                         // URL
        );

        // Chỉnh sửa biểu đồ
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setShadowXOffset(0);
        plot.setShadowYOffset(0);
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 180));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));

        // Màu sắc các phần
        int i = 0;
        Color[] colors = {
                new Color(41, 128, 185),  // xanh dương
                new Color(39, 174, 96),   // xanh lá
                new Color(142, 68, 173),  // tím
                new Color(243, 156, 18),  // cam
                new Color(231, 76, 60),   // đỏ
                new Color(52, 152, 219),  // xanh da trời
                new Color(46, 204, 113),  // xanh lá nhạt
                new Color(155, 89, 182),  // tím nhạt
                new Color(230, 126, 34),  // cam nhạt
                new Color(231, 76, 60)    // đỏ nhạt
        };

        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint(key.toString(), colors[i % colors.length]);
            i++;
        }

        // Định dạng tiêu đề
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(BACKGROUND_COLOR);

        return chartPanel;
    }

    /**
     * Tạo biểu đồ kết hợp cột và đường
     */
    private ChartPanel createCombinedChart(String groupBy) {
        DefaultCategoryDataset dataset = createCategoryDataset(groupBy);

        JFreeChart chart = ChartFactory.createBarChart(
                "Thống Kê Doanh Thu Theo " + groupBy, // Tiêu đề
                "Thời Gian",                   // Trục x
                "Doanh Thu (VNĐ)",             // Trục y
                dataset,                       // Dữ liệu
                PlotOrientation.VERTICAL,      // Hướng
                true,                          // Hiển thị chú thích
                true,                          // Tooltip
                false                          // URL
        );

        // Chỉnh sửa biểu đồ cột
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
        barRenderer.setItemMargin(0.1);
        barRenderer.setShadowVisible(false);

        // Màu sắc thanh biểu đồ
        barRenderer.setSeriesPaint(0, new Color(41, 128, 185));
        barRenderer.setSeriesPaint(1, new Color(39, 174, 96));
        barRenderer.setSeriesPaint(2, new Color(142, 68, 173));
        barRenderer.setSeriesPaint(3, new Color(243, 156, 18));

        // Định dạng trục y
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(currencyFormatter);

        // Tạo thêm một renderer cho biểu đồ đường
        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setDefaultShapesVisible(true);
        lineRenderer.setDrawOutlines(true);
        lineRenderer.setUseFillPaint(true);
        lineRenderer.setDefaultFillPaint(Color.WHITE);
        lineRenderer.setSeriesPaint(0, new Color(231, 76, 60));
        lineRenderer.setSeriesStroke(0, new BasicStroke(3.0f));

        // Tạo dataset thứ hai cho giá trị trung bình
        DefaultCategoryDataset datasetLine = new DefaultCategoryDataset();
        Map<String, List<Double>> timeGroupedData = new HashMap<>();

        for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
            String timeKey = ketQua.getThoiGian().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (!timeGroupedData.containsKey(timeKey)) {
                timeGroupedData.put(timeKey, new ArrayList<>());
            }
            timeGroupedData.get(timeKey).add(ketQua.getDoanhThu());
        }

        // Tính giá trị trung bình cho mỗi thời điểm
        for (Map.Entry<String, List<Double>> entry : timeGroupedData.entrySet()) {
            double sum = entry.getValue().stream().mapToDouble(Double::doubleValue).sum();
            double average = sum / entry.getValue().size();
            datasetLine.addValue(average, "Trung bình", entry.getKey());
        }

        plot.setDataset(1, datasetLine);
        plot.setRenderer(1, lineRenderer);

        // Định dạng trục x
        CategoryAxis axis = plot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        axis.setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        axis.setLabelFont(new Font("Arial", Font.BOLD, 14));

        // Định dạng trục y
        plot.getRangeAxis().setTickLabelFont(new Font("Arial", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("Arial", Font.BOLD, 14));

        // Định dạng tiêu đề
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 12));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(BACKGROUND_COLOR);

        return chartPanel;
    }

    /**
     * Tạo dataset cho biểu đồ cột và đường
     */
    private DefaultCategoryDataset createCategoryDataset(String groupBy) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
            String series = "";
            switch (groupBy) {
                case "Tuyến Tàu":
                    series = ketQua.getTenTuyen();
                    break;
                case "Loại Toa":
                    series = ketQua.getLoaiToa();
                    break;
                case "Loại Vé":
                    series = ketQua.getLoaiVe();
                    break;
            }

            String timeCategory = ketQua.getThoiGian().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dataset.addValue(ketQua.getDoanhThu(), series, timeCategory);
        }

        return dataset;
    }

    /**
     * Cập nhật thông tin tóm tắt
     */
    private void updateSummary() {
        summaryPanel.removeAll();

        // Tính toán các thống kê cơ bản
        double totalRevenue = thongKeData.stream()
                .mapToDouble(KetQuaThongKeDoanhThu::getDoanhThu)
                .sum();

        double avgRevenuePerDay = calculateAverageRevenuePerDay();

        // Top tuyến tàu doanh thu cao nhất
        Map<String, Double> routeRevenue = thongKeData.stream()
                .collect(Collectors.groupingBy(KetQuaThongKeDoanhThu::getTenTuyen,
                        Collectors.summingDouble(KetQuaThongKeDoanhThu::getDoanhThu)));

        String topRoute = routeRevenue.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Không xác định");

        double topRouteRevenue = routeRevenue.getOrDefault(topRoute, 0.0);

        // Top loại toa doanh thu cao nhất
        Map<String, Double> coachTypeRevenue = thongKeData.stream()
                .collect(Collectors.groupingBy(KetQuaThongKeDoanhThu::getLoaiToa,
                        Collectors.summingDouble(KetQuaThongKeDoanhThu::getDoanhThu)));

        String topCoachType = coachTypeRevenue.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Không xác định");

        double topCoachTypeRevenue = coachTypeRevenue.getOrDefault(topCoachType, 0.0);

        // Hiển thị thông tin
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        infoPanel.setBackground(BACKGROUND_COLOR);

        // Thêm tiêu đề
        JLabel titleLabel = new JLabel("Thông Tin Tổng Quan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(SECONDARY_COLOR);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoPanel.add(titleLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Thêm các thông tin cơ bản
        infoPanel.add(createInfoLabel("Tổng doanh thu", currencyFormatter.format(totalRevenue) + " VNĐ", true));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(createInfoLabel("Trung bình doanh thu/ngày", currencyFormatter.format(avgRevenuePerDay) + " VNĐ", true));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(createInfoLabel("Tuyến tàu doanh thu cao nhất", topRoute, true));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(createInfoLabel("", currencyFormatter.format(topRouteRevenue) + " VNĐ", false));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        infoPanel.add(createInfoLabel("Loại toa doanh thu cao nhất", topCoachType, true));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(createInfoLabel("", currencyFormatter.format(topCoachTypeRevenue) + " VNĐ", false));

        infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Tiêu đề phần phân tích
        JLabel analysisLabel = new JLabel("Phân Tích Doanh Thu");
        analysisLabel.setFont(new Font("Arial", Font.BOLD, 14));
        analysisLabel.setForeground(SECONDARY_COLOR);
        analysisLabel.setAlignmentX(LEFT_ALIGNMENT);
        infoPanel.add(analysisLabel);

        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Phân tích theo tuyến tàu (top 5)
        JPanel routeAnalysis = new JPanel();
        routeAnalysis.setLayout(new BoxLayout(routeAnalysis, BoxLayout.Y_AXIS));
        routeAnalysis.setBackground(BACKGROUND_COLOR);
        routeAnalysis.setAlignmentX(LEFT_ALIGNMENT);

        JLabel routeLabel = new JLabel("Top 5 tuyến tàu doanh thu cao nhất:");
        routeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        routeLabel.setAlignmentX(LEFT_ALIGNMENT);
        routeAnalysis.add(routeLabel);
        routeAnalysis.add(Box.createRigidArea(new Dimension(0, 5)));

        List<Map.Entry<String, Double>> sortedRoutes = routeRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        for (Map.Entry<String, Double> route : sortedRoutes) {
            double percentage = route.getValue() / totalRevenue * 100;
            JPanel routeItem = createProgressBarItem(
                    route.getKey(),
                    currencyFormatter.format(route.getValue()) + " VNĐ",
                    percentage,
                    new Color(41, 128, 185)
            );
            routeAnalysis.add(routeItem);
            routeAnalysis.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        infoPanel.add(routeAnalysis);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Thêm panel thông tin vào scrollpane
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        summaryPanel.add(scrollPane);
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }

    /**
     * Tạo item có progress bar
     */
    private JPanel createProgressBarItem(String label, String value, double percentage, Color color) {
        JPanel panel = new JPanel(new BorderLayout(5, 3));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Panel chứa label và value
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);

        JLabel lblItem = new JLabel(label);
        lblItem.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(lblItem, BorderLayout.WEST);

        JLabel lblValue = new JLabel(value + " (" + String.format("%.1f", percentage) + "%)");
        lblValue.setFont(new Font("Arial", Font.PLAIN, 12));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(lblValue, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int)percentage);
        progressBar.setForeground(color);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 8));
        panel.add(progressBar, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Tạo label thông tin với style
     */
    private JPanel createInfoLabel(String title, String value, boolean important) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel titleLabel = new JLabel(title + ":");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        panel.add(titleLabel, BorderLayout.WEST);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", important ? Font.BOLD : Font.PLAIN, 13));
        valueLabel.setForeground(important ? new Color(44, 62, 80) : Color.BLACK);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tính trung bình doanh thu mỗi ngày
     */
    private double calculateAverageRevenuePerDay() {
        if (thongKeData == null || thongKeData.isEmpty()) {
            return 0;
        }

        LocalDate minDate = thongKeData.stream()
                .map(KetQuaThongKeDoanhThu::getThoiGian)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate maxDate = thongKeData.stream()
                .map(KetQuaThongKeDoanhThu::getThoiGian)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        long daysBetween = ChronoUnit.DAYS.between(minDate, maxDate) + 1;
        double totalRevenue = thongKeData.stream()
                .mapToDouble(KetQuaThongKeDoanhThu::getDoanhThu)
                .sum();

        return (double) totalRevenue / daysBetween;
    }

    /**
     * Cập nhật phân tích và dự đoán
     */
    private void updateAIAnalysis() {
        aiAnalysisPanel.removeAll();

        JPanel analysisPanel = new JPanel();
        analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.Y_AXIS));
        analysisPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        analysisPanel.setBackground(BACKGROUND_COLOR);

        try {
            // Phát hiện xu hướng
            detectTrend(analysisPanel);

            // Phát hiện bất thường
            detectAnomalies(analysisPanel);

            // Dự đoán doanh thu
            predictRevenue(analysisPanel);

            // Đề xuất tối ưu
            provideOptimizationSuggestions(analysisPanel);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Lỗi khi phân tích dữ liệu: " + e.getMessage());
            errorLabel.setFont(new Font("Arial", Font.ITALIC, 13));
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(LEFT_ALIGNMENT);
            analysisPanel.add(errorLabel);
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(analysisPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        aiAnalysisPanel.add(scrollPane);
        aiAnalysisPanel.revalidate();
        aiAnalysisPanel.repaint();
    }

    /**
     * Phát hiện xu hướng dữ liệu
     */
    private void detectTrend(JPanel panel) {
        panel.add(createSectionTitle("Phân Tích Xu Hướng", ACCENT_COLOR));

        if (thongKeData == null || thongKeData.isEmpty() || thongKeData.size() < 3) {
            panel.add(createInfoMessage("Không đủ dữ liệu để phân tích xu hướng", false));
            return;
        }

        // Nhóm dữ liệu theo thời gian
        Map<LocalDate, Double> timeSeriesData = new TreeMap<>();
        for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
            LocalDate time = ketQua.getThoiGian();
            timeSeriesData.put(time,
                    timeSeriesData.getOrDefault(time, 0.0) + ketQua.getDoanhThu());
        }

        // Phân tích xu hướng
        List<Double> values = new ArrayList<>(timeSeriesData.values());
        int increasingCount = 0;
        int decreasingCount = 0;

        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > values.get(i-1)) {
                increasingCount++;
            } else if (values.get(i) < values.get(i-1)) {
                decreasingCount++;
            }
        }

        String trendMessage;
        Color trendColor;
        if (increasingCount > decreasingCount) {
            trendMessage = "Xu hướng tăng";
            trendColor = new Color(39, 174, 96);  // Màu xanh lá
            if (increasingCount > values.size() * 0.7) {
                trendMessage += " mạnh";
            }
        } else if (decreasingCount > increasingCount) {
            trendMessage = "Xu hướng giảm";
            trendColor = new Color(231, 76, 60);  // Màu đỏ
            if (decreasingCount > values.size() * 0.7) {
                trendMessage += " mạnh";
            }
        } else {
            trendMessage = "Xu hướng ổn định";
            trendColor = new Color(41, 128, 185); // Màu xanh dương
        }

        // So sánh đầu kỳ và cuối kỳ
        List<LocalDate> dates = new ArrayList<>(timeSeriesData.keySet());
        double firstValue = timeSeriesData.get(dates.get(0));
        double lastValue = timeSeriesData.get(dates.get(dates.size() - 1));
        double changePct = ((lastValue - firstValue) / firstValue) * 100;

        // Hiển thị kết quả
        JLabel lblTrend = new JLabel(trendMessage);
        lblTrend.setFont(new Font("Arial", Font.BOLD, 14));
        lblTrend.setForeground(trendColor);
        lblTrend.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblTrend);

        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel lblChange = new JLabel(String.format("Thay đổi từ đầu kỳ đến cuối kỳ: %.1f%%", changePct));
        lblChange.setFont(new Font("Arial", Font.PLAIN, 13));
        lblChange.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblChange);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    /**
     * Phát hiện bất thường trong dữ liệu
     */
    private void detectAnomalies(JPanel panel) {
        panel.add(createSectionTitle("Phát Hiện Bất Thường", ACCENT_COLOR));

        if (thongKeData == null || thongKeData.isEmpty() || thongKeData.size() < 5) {
            panel.add(createInfoMessage("Không đủ dữ liệu để phát hiện bất thường", false));
            return;
        }

        // Nhóm dữ liệu theo ngày
        Map<LocalDate, Double> timeSeriesData = new TreeMap<>();
        for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
            LocalDate time = ketQua.getThoiGian();
            timeSeriesData.put(time,
                    timeSeriesData.getOrDefault(time, 0.0) + ketQua.getDoanhThu());
        }

        // Tính toán trung bình và độ lệch chuẩn
        List<Double> values = new ArrayList<>(timeSeriesData.values());
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double sumSquaredDiff = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum();
        double stdDev = Math.sqrt(sumSquaredDiff / values.size());

        // Tìm các điểm bất thường (hơn 2 độ lệch chuẩn từ trung bình)
        List<Map.Entry<LocalDate, Double>> anomalies = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> entry : timeSeriesData.entrySet()) {
            if (Math.abs(entry.getValue() - mean) > 2 * stdDev) {
                anomalies.add(entry);
            }
        }

        // Hiển thị kết quả
        if (anomalies.isEmpty()) {
            panel.add(createInfoMessage("Không phát hiện bất thường đáng kể", false));
        } else {
            JLabel lblAnomalies = new JLabel(String.format("Phát hiện %d điểm dữ liệu bất thường:", anomalies.size()));
            lblAnomalies.setFont(new Font("Arial", Font.BOLD, 13));
            lblAnomalies.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(lblAnomalies);

            panel.add(Box.createRigidArea(new Dimension(0, 8)));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // Panel chứa danh sách các điểm bất thường
            JPanel anomalyList = new JPanel();
            anomalyList.setLayout(new BoxLayout(anomalyList, BoxLayout.Y_AXIS));
            anomalyList.setBackground(BACKGROUND_COLOR);
            anomalyList.setAlignmentX(LEFT_ALIGNMENT);

            for (int i = 0; i < Math.min(5, anomalies.size()); i++) {
                Map.Entry<LocalDate, Double> entry = anomalies.get(i);
                LocalDate date = entry.getKey();
                double value = entry.getValue();

                boolean isHigh = value > mean;
                Color anomalyColor = isHigh ? new Color(39, 174, 96) : new Color(231, 76, 60);
                String trend = isHigh ? "cao bất thường" : "thấp bất thường";

                JPanel anomalyItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                anomalyItem.setBackground(BACKGROUND_COLOR);
                anomalyItem.setAlignmentX(LEFT_ALIGNMENT);

                JLabel bullet = new JLabel("• ");
                bullet.setFont(new Font("Arial", Font.BOLD, 14));
                bullet.setForeground(anomalyColor);
                anomalyItem.add(bullet);

                JLabel dateLabel = new JLabel(formatter.format(date) + ": ");
                dateLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                anomalyItem.add(dateLabel);

                JLabel valueLabel = new JLabel(currencyFormatter.format(value) + " VNĐ");
                valueLabel.setFont(new Font("Arial", Font.BOLD, 13));
                anomalyItem.add(valueLabel);

                JLabel trendLabel = new JLabel(" (" + trend + ")");
                trendLabel.setFont(new Font("Arial", Font.ITALIC, 13));
                trendLabel.setForeground(anomalyColor);
                anomalyItem.add(trendLabel);

                anomalyList.add(anomalyItem);
                anomalyList.add(Box.createRigidArea(new Dimension(0, 5)));
            }

            if (anomalies.size() > 5) {
                JLabel moreLabel = new JLabel("và " + (anomalies.size() - 5)
                        + " điểm khác...");
                moreLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                moreLabel.setAlignmentX(LEFT_ALIGNMENT);
                anomalyList.add(moreLabel);
            }

            panel.add(anomalyList);
        }

        panel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    /**
     * Dự đoán doanh thu
     */
    private void predictRevenue(JPanel panel) {
        panel.add(createSectionTitle("Dự Đoán Doanh Thu", ACCENT_COLOR));

        if (thongKeData == null || thongKeData.isEmpty() || thongKeData.size() < 5) {
            panel.add(createInfoMessage("Không đủ dữ liệu để dự đoán", false));
            return;
        }

        try {
            // Nhóm dữ liệu theo ngày để tính doanh thu theo ngày
            Map<LocalDate, Double> timeSeriesData = new TreeMap<>();
            for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
                LocalDate time = ketQua.getThoiGian();
                timeSeriesData.put(time,
                        timeSeriesData.getOrDefault(time, 0.0) + ketQua.getDoanhThu());
            }

            // Đơn giản hóa: sử dụng phương pháp trung bình trượt để dự đoán
            List<LocalDate> dates = new ArrayList<>(timeSeriesData.keySet());
            List<Double> values = new ArrayList<>(timeSeriesData.values());

            if (dates.size() < 3) {
                panel.add(createInfoMessage("Cần ít nhất 3 ngày dữ liệu để dự đoán", false));
                return;
            }

            // Tính trung bình của 3 ngày gần nhất
            double avg3Days = 0;
            int count = Math.min(3, values.size());
            for (int i = values.size() - count; i < values.size(); i++) {
                avg3Days += values.get(i);
            }
            avg3Days /= count;

            // Tính tốc độ tăng trưởng dựa trên 3-5 ngày gần nhất
            double growthRate = 0;
            if (values.size() >= 3) {
                int startIdx = Math.max(0, values.size() - 5);
                double firstValue = values.get(startIdx);
                double lastValue = values.get(values.size() - 1);

                if (firstValue > 0) {
                    growthRate = Math.pow(lastValue / firstValue, 1.0 / (values.size() - startIdx)) - 1;
                }

                // Giới hạn tốc độ tăng trưởng để tránh dự đoán quá cao/thấp
                growthRate = Math.max(-0.2, Math.min(0.2, growthRate));
            }

            // Dự đoán cho 7 ngày tiếp theo
            double[] predictions = new double[7];
            double totalPrediction = 0;

            for (int i = 0; i < 7; i++) {
                // Dự đoán sử dụng tốc độ tăng trưởng
                double prediction = avg3Days * Math.pow(1 + growthRate, i + 1);
                predictions[i] = prediction;
                totalPrediction += prediction;
            }

            // Hiển thị kết quả dự đoán
            JPanel totalPredictionPanel = new JPanel(new BorderLayout(5, 5));
            totalPredictionPanel.setBackground(new Color(240, 247, 255));
            totalPredictionPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(200, 220, 240), 1, true),
                    new EmptyBorder(10, 10, 10, 10)));
            totalPredictionPanel.setAlignmentX(LEFT_ALIGNMENT);
            totalPredictionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            JLabel lblPredictionTitle = new JLabel("Dự đoán tổng doanh thu trong 7 ngày tới:");
            lblPredictionTitle.setFont(new Font("Arial", Font.PLAIN, 13));
            totalPredictionPanel.add(lblPredictionTitle, BorderLayout.NORTH);

            JLabel lblPredictionValue = new JLabel(currencyFormatter.format(totalPrediction) + " VNĐ");
            lblPredictionValue.setFont(new Font("Arial", Font.BOLD, 24));
            lblPredictionValue.setForeground(new Color(41, 128, 185));
            totalPredictionPanel.add(lblPredictionValue, BorderLayout.CENTER);

            JLabel lblAveragePerDay = new JLabel("Trung bình mỗi ngày: " +
                    currencyFormatter.format(totalPrediction / 7) + " VNĐ");
            lblAveragePerDay.setFont(new Font("Arial", Font.ITALIC, 12));
            totalPredictionPanel.add(lblAveragePerDay, BorderLayout.SOUTH);

            panel.add(totalPredictionPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));

            // Dự đoán xu hướng
            double firstPrediction = predictions[0];
            double lastPrediction = predictions[6];

            JPanel trendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            trendPanel.setBackground(BACKGROUND_COLOR);
            trendPanel.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lblTrendTitle = new JLabel("Xu hướng doanh thu dự đoán: ");
            lblTrendTitle.setFont(new Font("Arial", Font.PLAIN, 13));
            trendPanel.add(lblTrendTitle);

            String trendText;
            Color trendColor;
            double changePct = (lastPrediction - firstPrediction) / firstPrediction * 100;

            if (changePct > 10) {
                trendText = "Tăng";
                trendColor = new Color(39, 174, 96); // Xanh lá
            } else if (changePct < -10) {
                trendText = "Giảm";
                trendColor = new Color(231, 76, 60); // Đỏ
            } else {
                trendText = "Ổn định";
                trendColor = new Color(41, 128, 185); // Xanh dương
            }

            JLabel lblTrend = new JLabel(trendText);
            lblTrend.setFont(new Font("Arial", Font.BOLD, 13));
            lblTrend.setForeground(trendColor);
            trendPanel.add(lblTrend);

            panel.add(trendPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 15)));

        } catch (Exception e) {
            panel.add(createInfoMessage("Lỗi khi dự đoán: " + e.getMessage(), true));
            e.printStackTrace();
        }
    }

    /**
     * Đề xuất tối ưu
     */
    private void provideOptimizationSuggestions(JPanel panel) {
        panel.add(createSectionTitle("Đề Xuất Tối Ưu", ACCENT_COLOR));

        if (thongKeData == null || thongKeData.isEmpty() || thongKeData.size() < 5) {
            panel.add(createInfoMessage("Không đủ dữ liệu để đưa ra đề xuất", false));
            return;
        }

        // Phân tích theo tuyến tàu
        Map<String, Double> routeRevenue = thongKeData.stream()
                .collect(Collectors.groupingBy(KetQuaThongKeDoanhThu::getTenTuyen,
                        Collectors.summingDouble(KetQuaThongKeDoanhThu::getDoanhThu)));

        // Phân tích theo loại toa
        Map<String, Double> coachTypeRevenue = thongKeData.stream()
                .collect(Collectors.groupingBy(KetQuaThongKeDoanhThu::getLoaiToa,
                        Collectors.summingDouble(KetQuaThongKeDoanhThu::getDoanhThu)));

        // Phân tích theo loại vé
        Map<String, Double> ticketTypeRevenue = thongKeData.stream()
                .collect(Collectors.groupingBy(KetQuaThongKeDoanhThu::getLoaiVe,
                        Collectors.summingDouble(KetQuaThongKeDoanhThu::getDoanhThu)));

        // Đề xuất dựa trên phân tích
        List<String> suggestions = new ArrayList<>();

        // Đề xuất về tuyến tàu
        if (routeRevenue.size() > 1) {
            // Tìm tuyến có doanh thu cao nhất và thấp nhất
            Map.Entry<String, Double> topRoute = routeRevenue.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            Map.Entry<String, Double> worstRoute = routeRevenue.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .orElse(null);

            if (topRoute != null && worstRoute != null) {
                double ratio = topRoute.getValue() / worstRoute.getValue();
                if (ratio > 3) { // Nếu tuyến cao gấp 3 lần tuyến thấp
                    suggestions.add("Tuyến " + topRoute.getKey() + " có doanh thu cao gấp " +
                            String.format("%.1f", ratio) + " lần so với tuyến " +
                            worstRoute.getKey() + ". Xem xét điều chỉnh giá vé hoặc khuyến mãi cho tuyến kém hiệu quả.");
                }
            }
        }

        // Đề xuất về loại toa
        if (coachTypeRevenue.size() > 1) {
            // Tìm loại toa có doanh thu cao nhất
            Map.Entry<String, Double> topCoachType = coachTypeRevenue.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (topCoachType != null) {
                suggestions.add("Loại toa " + topCoachType.getKey() + " mang lại doanh thu cao nhất. " +
                        "Xem xét tăng số lượng toa loại này trên các tuyến phổ biến.");
            }
        }

        // Đề xuất theo loại vé
        if (ticketTypeRevenue.size() > 1) {
            // Tìm loại vé có doanh thu cao nhất và thấp nhất
            Map.Entry<String, Double> topTicketType = ticketTypeRevenue.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (topTicketType != null) {
                suggestions.add("Loại vé " + topTicketType.getKey() + " mang lại doanh thu cao nhất. " +
                        "Xem xét mở rộng các dịch vụ và tiện ích cho loại vé này.");
            }
        }

        // Phân tích xu hướng theo thời gian
        Map<LocalDate, Double> timeSeriesData = new TreeMap<>();
        for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
            LocalDate time = ketQua.getThoiGian();
            timeSeriesData.put(time,
                    timeSeriesData.getOrDefault(time, 0.0) + ketQua.getDoanhThu());
        }

        if (timeSeriesData.size() > 3) {
            // Kiểm tra tính thời vụ
            suggestions.add("Phân tích dữ liệu thêm để xác định tính thời vụ của doanh thu, " +
                    "giúp điều chỉnh giá vé và lên kế hoạch marketing phù hợp.");
        }

        // Thêm đề xuất chung
        suggestions.add("Cân nhắc áp dụng chương trình khách hàng thân thiết để tăng tỷ lệ khách hàng quay lại.");
        suggestions.add("Đánh giá hiệu quả các chiến dịch marketing hiện tại và điều chỉnh để tăng doanh thu.");

        // Hiển thị các đề xuất
        if (suggestions.isEmpty()) {
            panel.add(createInfoMessage("Không đủ dữ liệu để đưa ra đề xuất", false));
        } else {
            JPanel suggestionList = new JPanel();
            suggestionList.setLayout(new BoxLayout(suggestionList, BoxLayout.Y_AXIS));
            suggestionList.setBackground(BACKGROUND_COLOR);
            suggestionList.setAlignmentX(LEFT_ALIGNMENT);

            for (int i = 0; i < suggestions.size(); i++) {
                String suggestion = suggestions.get(i);

                JPanel suggestionItem = new JPanel();
                suggestionItem.setLayout(new BorderLayout(8, 0));
                suggestionItem.setBackground(BACKGROUND_COLOR);
                suggestionItem.setAlignmentX(LEFT_ALIGNMENT);
                suggestionItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

                JLabel bulletLabel = new JLabel("•");
                bulletLabel.setFont(new Font("Arial", Font.BOLD, 18));
                bulletLabel.setForeground(ACCENT_COLOR);
                bulletLabel.setVerticalAlignment(SwingConstants.TOP);

                JTextArea textArea = new JTextArea(suggestion);
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setEditable(false);
                textArea.setBorder(null);
                textArea.setFont(new Font("Arial", Font.PLAIN, 13));
                textArea.setBackground(BACKGROUND_COLOR);

                suggestionItem.add(bulletLabel, BorderLayout.WEST);
                suggestionItem.add(textArea, BorderLayout.CENTER);

                suggestionList.add(suggestionItem);
                if (i < suggestions.size() - 1) {
                    suggestionList.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }

            panel.add(suggestionList);
        }
    }

    /**
     * Tạo tiêu đề phần với style
     */
    private JLabel createSectionTitle(String title, Color color) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, color),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Tạo thông báo thông tin với style
     */
    private JLabel createInfoMessage(String message, boolean isError) {
        JLabel label = new JLabel(message);
        label.setFont(new Font("Arial", Font.ITALIC, 13));
        if (isError) {
            label.setForeground(new Color(231, 76, 60));
        }
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Xuất báo cáo
     */
    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu Báo Cáo Doanh Thu");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML Files", "html", "htm");
        fileChooser.setFileFilter(filter);
        fileChooser.setSelectedFile(new File("BaoCaoThongKeDoanhThu.html"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Đảm bảo tên file có đuôi .html
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".html") && !filePath.toLowerCase().endsWith(".htm")) {
                file = new File(filePath + ".html");
            }

            // Hiển thị thông báo đang xử lý
            btnExport.setEnabled(false);
            btnExport.setText("Đang xuất báo cáo...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Lưu đường dẫn file để sử dụng trong SwingWorker
            final File finalFile = file;

            // Tạo SwingWorker để xuất báo cáo trong nền
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        // Thực hiện việc tạo báo cáo HTML
                        createHtmlReport(finalFile);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    // Khôi phục trạng thái nút và con trỏ
                    btnExport.setEnabled(true);
                    btnExport.setText("Xuất Báo Cáo");
                    setCursor(Cursor.getDefaultCursor());

                    try {
                        boolean success = get();
                        if (success && finalFile.exists() && finalFile.length() > 0) {
                            int option = JOptionPane.showOptionDialog(ThongKeDoanhThuPanel.this,
                                    "Đã xuất báo cáo thành công tại:\n" + finalFile.getAbsolutePath() +
                                            "\n\nBạn có muốn mở file không?",
                                    "Xuất Báo Cáo", JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE, null,
                                    new String[]{"Mở File", "Đóng"}, "Mở File");

                            if (option == JOptionPane.YES_OPTION) {
                                openFile(finalFile);
                            }
                        } else {
                            JOptionPane.showMessageDialog(ThongKeDoanhThuPanel.this,
                                    "Có lỗi khi xuất báo cáo. Không thể tạo file.",
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ThongKeDoanhThuPanel.this,
                                "Lỗi khi xuất báo cáo: " + e.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };

            worker.execute();
        }
    }

    /**
     * Tạo báo cáo dạng HTML
     */
    private void createHtmlReport(File file) {
        try {
            // Kiểm tra nếu file đã tồn tại
            if (file.exists()) {
                // Thử xóa nếu đã tồn tại
                if (!file.delete()) {
                    throw new IOException("Không thể ghi đè file hiện có.");
                }
            }

            // Tạo thư mục chứa file nếu chưa tồn tại
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Chuẩn bị dữ liệu
            double totalRevenue = thongKeData.stream()
                    .mapToDouble(KetQuaThongKeDoanhThu::getDoanhThu)
                    .sum();

            double avgRevenuePerDay = calculateAverageRevenuePerDay();

            // Phân tích theo tuyến tàu
            Map<String, Double> routeRevenue = thongKeData.stream()
                    .collect(Collectors.groupingBy(KetQuaThongKeDoanhThu::getTenTuyen,
                            Collectors.summingDouble(KetQuaThongKeDoanhThu::getDoanhThu)));

            // Lấy thông tin từ ngày đến ngày
            LocalDate fromDate = dateFrom.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toDate = dateTo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String timeRangeType = (String) cboTimeRange.getSelectedItem();

            // Tạo file HTML với FileWriter
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("<!DOCTYPE html>\n");
                writer.write("<html lang=\"vi\">\n");
                writer.write("<head>\n");
                writer.write("    <meta charset=\"UTF-8\">\n");
                writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
                writer.write("    <title>Báo Cáo Thống Kê Doanh Thu</title>\n");
                writer.write("    <style>\n");
                writer.write("        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }\n");
                writer.write("        h1 { color: #0066cc; text-align: center; }\n");
                writer.write("        h2 { color: #0066cc; margin-top: 30px; border-bottom: 1px solid #ccc; padding-bottom: 5px; }\n");
                writer.write("        .info { margin-bottom: 30px; }\n");
                writer.write("        .info p { margin: 5px 0; }\n");
                writer.write("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n");
                writer.write("        th { background-color: #0066cc; color: white; text-align: left; padding: 8px; }\n");
                writer.write("        td { padding: 8px; border-bottom: 1px solid #ddd; }\n");
                writer.write("        tr:nth-child(even) { background-color: #f2f2f2; }\n");
                writer.write("        .status-bar { background-color: #f0f0f0; height: 20px; width: 100%; margin-top: 5px; }\n");
                writer.write("        .status-fill { height: 100%; }\n");
                writer.write("    </style>\n");
                writer.write("</head>\n");
                writer.write("<body>\n");

                // Tiêu đề
                writer.write("    <h1>BÁO CÁO THỐNG KÊ DOANH THU BÁN VÉ TÀU</h1>\n");

                // Thông tin báo cáo
                writer.write("    <div class=\"info\">\n");
                writer.write("        <p><strong>Thời gian báo cáo:</strong> " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>\n");
                writer.write("        <p><strong>Khoảng thời gian:</strong> Từ " +
                        fromDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " đến " + toDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</p>\n");
                writer.write("        <p><strong>Thống kê theo:</strong> " + timeRangeType + "</p>\n");
                writer.write("    </div>\n");

                // Thông tin tổng quan
                writer.write("    <h2>THÔNG TIN TỔNG QUAN</h2>\n");
                writer.write("    <table>\n");
                writer.write("        <tr><td>Tổng doanh thu</td><td><strong>" +
                        currencyFormatter.format(totalRevenue) + " VNĐ</strong></td></tr>\n");
                writer.write("        <tr><td>Trung bình doanh thu/ngày</td><td><strong>" +
                        currencyFormatter.format(avgRevenuePerDay) + " VNĐ</strong></td></tr>\n");

                if (!routeRevenue.isEmpty()) {
                    String topRoute = routeRevenue.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("Không xác định");
                    double topRouteRevenue = routeRevenue.getOrDefault(topRoute, 0.0);
                    writer.write("        <tr><td>Tuyến tàu doanh thu cao nhất</td><td><strong>" + topRoute +
                            " (" + currencyFormatter.format(topRouteRevenue) + " VNĐ)</strong></td></tr>\n");
                }
                writer.write("    </table>\n");

                // Thống kê theo tuyến tàu
                writer.write("    <h2>THỐNG KÊ THEO TUYẾN TÀU</h2>\n");
                writer.write("    <table>\n");
                writer.write("        <tr><th>Tuyến tàu</th><th>Doanh thu</th><th>Phần trăm</th><th>Biểu đồ</th></tr>\n");

                List<Map.Entry<String, Double>> sortedRoutes = routeRevenue.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .collect(Collectors.toList());

                for (Map.Entry<String, Double> entry : sortedRoutes) {
                    String route = entry.getKey();
                    double revenue = entry.getValue();
                    double percentage = revenue / totalRevenue * 100;

                    writer.write("        <tr>\n");
                    writer.write("            <td>" + route + "</td>\n");
                    writer.write("            <td>" + currencyFormatter.format(revenue) + " VNĐ</td>\n");
                    writer.write("            <td>" + String.format("%.1f%%", percentage) + "</td>\n");
                    writer.write("            <td>\n");
                    writer.write("                <div class=\"status-bar\">\n");
                    writer.write("                    <div class=\"status-fill\" style=\"width: " +
                            Math.min(percentage, 100) + "%; background-color: #2980b9;\"></div>\n");
                    writer.write("                </div>\n");
                    writer.write("            </td>\n");
                    writer.write("        </tr>\n");
                }
                writer.write("    </table>\n");

                // Chi tiết số liệu
                writer.write("    <h2>CHI TIẾT DOANH THU THEO THỜI GIAN</h2>\n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // Nhóm dữ liệu theo thời gian
                Map<LocalDate, Map<String, Double>> groupedData = new TreeMap<>();
                for (KetQuaThongKeDoanhThu ketQua : thongKeData) {
                    LocalDate time = ketQua.getThoiGian();
                    String key = ketQua.getTenTuyen();

                    if (!groupedData.containsKey(time)) {
                        groupedData.put(time, new HashMap<>());
                    }

                    Map<String, Double> dayData = groupedData.get(time);
                    dayData.put(key, dayData.getOrDefault(key, 0.0) + ketQua.getDoanhThu());
                }

                writer.write("    <table>\n");
                writer.write("        <tr><th>Ngày</th><th>Tuyến</th><th>Doanh thu</th></tr>\n");

                for (Map.Entry<LocalDate, Map<String, Double>> entry : groupedData.entrySet()) {
                    LocalDate date = entry.getKey();
                    Map<String, Double> dayData = entry.getValue();

                    boolean isFirstRow = true;
                    for (Map.Entry<String, Double> routeEntry : dayData.entrySet()) {
                        writer.write("        <tr>\n");

                        if (isFirstRow) {
                            writer.write("            <td rowspan=\"" + dayData.size() + "\">" +
                                    formatter.format(date) + "</td>\n");
                            isFirstRow = false;
                        }

                        writer.write("            <td>" + routeEntry.getKey() + "</td>\n");
                        writer.write("            <td>" + currencyFormatter.format(routeEntry.getValue()) + " VNĐ</td>\n");
                        writer.write("        </tr>\n");
                    }
                }
                writer.write("    </table>\n");

                writer.write("</body>\n");
                writer.write("</html>");
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo file báo cáo: " + e.getMessage());
        }
    }

    /**
     * Mở file bằng chương trình mặc định của hệ điều hành
     */
    private void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể mở file tự động trên hệ điều hành này.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể mở file: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}