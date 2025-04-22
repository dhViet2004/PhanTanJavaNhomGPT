package GUI.component;

import dao.ChoNgoiCallback;
import dao.ChoNgoiDoiVeDAO;
import dao.ToaTauDoiVeDAO;
import model.ChoNgoi;
import model.LichTrinhTau;
import model.ToaTau;
import model.TrangThaiVeTau;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChoNgoiSelectorDialog extends JDialog implements ChoNgoiCallback {
    private JComboBox<ToaTau> cboToaTau;
    private JPanel pnlChoNgoi;
    private JButton btnXacNhan;
    private JButton btnHuy;
    private JProgressBar progressLoading;
    private JLabel lblStatus;

    private LichTrinhTau lichTrinhTau;
    private ChoNgoiDoiVeDAO choNgoiDAO;
    private ToaTauDoiVeDAO toaTauDAO;
    private ChoNgoi choNgoiDaChon;
    private String sessionId;
    private Map<String, JToggleButton> btnChoNgoi;
    private List<ChoNgoi> dsChoNgoi;
    private String maVeHienTai;
    private ChoNgoiSelectorCallback callback;

    // Thread pool for background tasks
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    // Map to store seat availability status by seat ID
    private Map<String, TrangThaiVeTau> trangThaiChoNgoi = new HashMap<>();

    // Màu sắc cho các trạng thái ghế
    private final Color COLOR_EMPTY = Color.WHITE; // Trống
    private final Color COLOR_SELECTED = new Color(40, 167, 69); // Xanh lá - đang chọn
    private final Color COLOR_REPAIR = Color.GRAY; // Đang sửa chữa
    private final Color COLOR_OCCUPIED = new Color(220, 53, 69); // Đỏ - đã đặt trong cùng lịch trình
    private final Color COLOR_AVAILABLE_IN_OTHER = new Color(255, 193, 7); // Vàng - đã đặt trong lịch trình khác nhưng có thể đặt

    public ChoNgoiSelectorDialog(Frame owner, LichTrinhTau lichTrinhTau,
                                 ChoNgoiDoiVeDAO choNgoiDAO, ToaTauDoiVeDAO toaTauDAO,
                                 ChoNgoiSelectorCallback callback, String maVeHienTai) {
        super(owner, "Chọn chỗ ngồi", true);
        this.lichTrinhTau = lichTrinhTau;
        this.choNgoiDAO = choNgoiDAO;
        this.toaTauDAO = toaTauDAO;
        this.callback = callback;
        this.sessionId = UUID.randomUUID().toString();
        this.btnChoNgoi = new HashMap<>();
        this.dsChoNgoi = new ArrayList<>();
        this.maVeHienTai = maVeHienTai; // Lưu mã vé hiện tại
        initComponents();

        // Load data asynchronously
        loadToaTauAsync();

        // Đăng ký callback để nhận thông báo từ server
        try {
            UnicastRemoteObject.exportObject(this, 0);
            choNgoiDAO.dangKyClientChoThongBao(this);
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể đăng ký nhận thông báo từ server: " + e.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private void cleanup() {
        huyKhoaChoNgoi();
        try {
            choNgoiDAO.huyDangKyClientChoThongBao(ChoNgoiSelectorDialog.this);
            UnicastRemoteObject.unexportObject(ChoNgoiSelectorDialog.this, true);
            executorService.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(970, 770);
        setLocationRelativeTo(getOwner());

        // Panel chọn toa tàu
        JPanel pnlSelectToa = new JPanel(new BorderLayout(5, 0));
        JPanel pnlToaTauCombo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlToaTauCombo.add(new JLabel("Chọn toa tàu:"));
        cboToaTau = new JComboBox<>();
        cboToaTau.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                if (value instanceof ToaTau) {
                    ToaTau toaTau = (ToaTau) value;
                    String displayText = toaTau.getMaToa();
                    try {
                        if (toaTau.getLoaiToa() != null) {
                            displayText += " - " + toaTau.getLoaiToa().getTenLoai();
                        }
                    } catch (Exception e) {
                        displayText += " - (Không có thông tin loại toa)";
                    }
                    value = displayText;
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        cboToaTau.addActionListener(e -> loadChoNgoiAsync());
        pnlToaTauCombo.add(cboToaTau);
        pnlSelectToa.add(pnlToaTauCombo, BorderLayout.WEST);

        // Progress indicator and status
        JPanel pnlProgress = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        progressLoading = new JProgressBar();
        progressLoading.setIndeterminate(true);
        progressLoading.setVisible(false);
        progressLoading.setPreferredSize(new Dimension(100, 20));
        lblStatus = new JLabel("Sẵn sàng");
        pnlProgress.add(lblStatus);
        pnlProgress.add(progressLoading);
        pnlSelectToa.add(pnlProgress, BorderLayout.EAST);

        add(pnlSelectToa, BorderLayout.NORTH);

        // Panel thông tin lịch trình (bên phải)
        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlInfo.setPreferredSize(new Dimension(250, 0));

        // Tiêu đề thông tin
        JLabel lblTitle = new JLabel("<html><h3>Thông tin lịch trình:</h3></html>");
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlInfo.add(lblTitle);

        // Thêm thông tin lịch trình
        JPanel pnlLichTrinh = new JPanel();
        pnlLichTrinh.setLayout(new BoxLayout(pnlLichTrinh, BoxLayout.Y_AXIS));
        pnlLichTrinh.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlLichTrinh.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        addInfoLabel(pnlLichTrinh, "Mã lịch:", lichTrinhTau.getMaLich());
        addInfoLabel(pnlLichTrinh, "Ngày đi:", lichTrinhTau.getNgayDi().toString());
        addInfoLabel(pnlLichTrinh, "Giờ đi:", lichTrinhTau.getGioDi().toString());
        addInfoLabel(pnlLichTrinh, "Tuyến:",
                lichTrinhTau.getTau().getTuyenTau().getGaDi() + " - " +
                        lichTrinhTau.getTau().getTuyenTau().getGaDen());

        pnlInfo.add(pnlLichTrinh);
        pnlInfo.add(Box.createVerticalGlue()); // Đẩy nội dung lên trên

        add(pnlInfo, BorderLayout.EAST);

        // Panel hiển thị chỗ ngồi ở giữa
        pnlChoNgoi = new JPanel();
        pnlChoNgoi.setLayout(new GridLayout(0, 8, 10, 10)); // 8 chỗ ngồi mỗi hàng
        JScrollPane scrollPane = new JScrollPane(pnlChoNgoi);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Panel bottom chứa nút điều khiển và chú thích màu sắc
        JPanel pnlBottom = new JPanel(new BorderLayout());

        // Panel chú thích màu sắc (nằm ở bottom-center)
        JPanel pnlChuThich = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlChuThich.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 0, 5, 0),
                BorderFactory.createTitledBorder("Chú thích:")
        ));

        // Tạo các ô màu cho chú thích
        addColorLegendItem(pnlChuThich, "Trống", COLOR_EMPTY);
        addColorLegendItem(pnlChuThich, "Đã đặt trong lịch trình này", COLOR_OCCUPIED);
        addColorLegendItem(pnlChuThich, "Có thể đặt (đã đặt ở lịch trình khác)", COLOR_AVAILABLE_IN_OTHER);
        addColorLegendItem(pnlChuThich, "Đang chọn", COLOR_SELECTED);
        addColorLegendItem(pnlChuThich, "Đang sửa chữa", COLOR_REPAIR);

        pnlBottom.add(pnlChuThich, BorderLayout.CENTER);

        // Panel nút điều khiển (nằm ở bottom-east)
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnXacNhan = new JButton("Xác nhận");
        btnXacNhan.addActionListener(e -> xacNhanChonChoNgoi());
        btnXacNhan.setEnabled(false);

        btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> {
            huyKhoaChoNgoi();
            dispose();
        });

        pnlControls.add(btnXacNhan);
        pnlControls.add(btnHuy);

        pnlBottom.add(pnlControls, BorderLayout.EAST);

        add(pnlBottom, BorderLayout.SOUTH);
    }

    // Helper method to add information labels
    private void addInfoLabel(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font(lblName.getFont().getName(), Font.BOLD, lblName.getFont().getSize()));
        row.add(lblName, BorderLayout.WEST);

        JLabel lblValue = new JLabel(value);
        row.add(lblValue, BorderLayout.CENTER);

        panel.add(row);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    // Phương thức hỗ trợ thêm mục vào chú thích màu sắc
    private void addColorLegendItem(JPanel panel, String text, Color color) {
        // Tạo panel con để chứa màu và text
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);

        // Tạo panel màu
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Tạo label text
        JLabel label = new JLabel(text);

        // Thêm vào panel item
        item.add(colorBox);
        item.add(label);

        // Thêm panel item vào panel chính
        panel.add(item);
    }

    // Load danh sách toa tàu bất đồng bộ
    private void loadToaTauAsync() {
        setLoading(true, "Đang tải danh sách toa tàu...");

        CompletableFuture.supplyAsync(() -> {
            try {
                String maTau = lichTrinhTau.getTau().getMaTau();
                return toaTauDAO.getToaTauByMaTau(maTau);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, executorService).thenAccept(dsToaTau -> {
            // Switch back to EDT for UI updates
            SwingUtilities.invokeLater(() -> {
                try {
                    // Preload any lazy loaded properties
                    for (ToaTau toaTau : dsToaTau) {
                        if (toaTau.getLoaiToa() != null) {
                            toaTau.getLoaiToa().getTenLoai();
                        }
                    }

                    DefaultComboBoxModel<ToaTau> model = new DefaultComboBoxModel<>();
                    for (ToaTau toaTau : dsToaTau) {
                        model.addElement(toaTau);
                    }

                    cboToaTau.setModel(model);

                    if (model.getSize() > 0) {
                        cboToaTau.setSelectedIndex(0);
                        loadChoNgoiAsync();
                    } else {
                        setLoading(false, "Không tìm thấy toa tàu");
                    }
                } catch (Exception e) {
                    handleException("Lỗi khi xử lý danh sách toa tàu", e);
                    setLoading(false, "Đã xảy ra lỗi");
                }
            });
        }).exceptionally(e -> {
            handleException("Không thể tải danh sách toa tàu", e);
            return null;
        });
    }

    // Load danh sách chỗ ngồi bất đồng bộ
    private void loadChoNgoiAsync() {
        ToaTau toaTau = (ToaTau) cboToaTau.getSelectedItem();
        if (toaTau == null) {
            return;
        }

        setLoading(true, "Đang tải danh sách chỗ ngồi...");

        // Xóa dữ liệu cũ
        pnlChoNgoi.removeAll();
        btnChoNgoi.clear();
        dsChoNgoi.clear();
        trangThaiChoNgoi.clear();
        choNgoiDaChon = null;
        btnXacNhan.setEnabled(false);

        // Tải dữ liệu mới bất đồng bộ
        CompletableFuture<List<ChoNgoi>> choNgoiFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return choNgoiDAO.getChoNgoiByToaTau(toaTau.getMaToa());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, executorService);

        // Tải trạng thái vé cho lịch trình này
        CompletableFuture<Map<String, TrangThaiVeTau>> trangThaiFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return choNgoiDAO.getTrangThaiChoNgoiTheoLichTrinh(lichTrinhTau.getMaLich());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, executorService);

        // Khi cả hai kết quả đã sẵn sàng
        CompletableFuture.allOf(choNgoiFuture, trangThaiFuture).thenAccept(v -> {
            try {
                List<ChoNgoi> dsChoNgoi = choNgoiFuture.get();
                Map<String, TrangThaiVeTau> trangThaiMap = trangThaiFuture.get();

                // Cập nhật UI trên EDT
                SwingUtilities.invokeLater(() -> {
                    try {
                        this.dsChoNgoi = dsChoNgoi;
                        this.trangThaiChoNgoi = trangThaiMap;

                        updateChoNgoiPanel();
                        setLoading(false, "Đã tải xong " + dsChoNgoi.size() + " chỗ ngồi");
                    } catch (Exception e) {
                        handleException("Lỗi khi hiển thị chỗ ngồi", e);
                    }
                });
            } catch (Exception e) {
                handleException("Không thể lấy dữ liệu chỗ ngồi", e);
            }
        }).exceptionally(e -> {
            handleException("Lỗi khi tải dữ liệu chỗ ngồi", e);
            return null;
        });
    }

    // Update the seat panel with the loaded data
    private void updateChoNgoiPanel() {
        pnlChoNgoi.removeAll();
        btnChoNgoi.clear();

        // Sort seats by name for better display
        Collections.sort(dsChoNgoi, Comparator.comparing(ChoNgoi::getTenCho));

        for (ChoNgoi choNgoi : dsChoNgoi) {
            JToggleButton btn = createChoNgoiButton(choNgoi);
            pnlChoNgoi.add(btn);
            btnChoNgoi.put(choNgoi.getMaCho(), btn);
        }

        pnlChoNgoi.revalidate();
        pnlChoNgoi.repaint();
    }

    private JToggleButton createChoNgoiButton(ChoNgoi choNgoi) {
        JToggleButton btn = new JToggleButton(choNgoi.getTenCho());
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(80, 80));

        // UI improvements
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        // Set initial state
        updateChoNgoiButtonState(btn, choNgoi);

        btn.addActionListener(e -> {
            if (btn.isSelected()) {
                handleSeatSelection(btn, choNgoi);
            } else {
                handleSeatDeselection(choNgoi);
            }
        });

        return btn;
    }

    private void handleSeatSelection(JToggleButton btn, ChoNgoi choNgoi) {
        CompletableFuture.runAsync(() -> {
            try {
                String maLichTrinh = lichTrinhTau.getMaLich();

                // Check seat status
                if (!choNgoi.isTinhTrang()) {
                    updateButtonStateWithMessage(btn, false, "Chỗ ngồi này đang sửa chữa, không thể đặt.");
                    return;
                }

                // Enhanced check for seat availability
                // First check if seat is available for the current ticket (excluding maVeHienTai)
                boolean isAvailable = choNgoiDAO.isChoNgoiAvailable(choNgoi.getMaCho(), maVeHienTai);
                if (!isAvailable) {
                    updateButtonStateWithMessage(btn, false, "Chỗ ngồi đã được đặt bởi vé khác. Vui lòng chọn chỗ khác.");
                    return;
                }

                // Check if seat is already booked in this schedule with a paid ticket
                TrangThaiVeTau trangThai = trangThaiChoNgoi.get(choNgoi.getMaCho());
                if (trangThai != null && trangThai == TrangThaiVeTau.DA_THANH_TOAN) {
                    updateButtonStateWithMessage(btn, false, "Chỗ ngồi này đã được đặt trong lịch trình này. Vui lòng chọn chỗ khác.");
                    return;
                }

                // Release lock on previously selected seat
                if (choNgoiDaChon != null) {
                    final String oldSeatId = choNgoiDaChon.getMaCho();
                    choNgoiDAO.huyKhoaChoNgoi(oldSeatId, maLichTrinh, sessionId);

                    // Update UI for old seat
                    SwingUtilities.invokeLater(() -> {
                        JToggleButton oldBtn = btnChoNgoi.get(oldSeatId);
                        if (oldBtn != null) {
                            oldBtn.setSelected(false);
                            updateChoNgoiButtonState(oldBtn, findChoNgoiByMa(oldSeatId));
                        }
                    });
                }

                // Lock the new seat
                boolean success = choNgoiDAO.khoaChoNgoi(choNgoi.getMaCho(), maLichTrinh, sessionId, 5 * 60 * 1000);

                // Update UI for new seat
                if (success) {
                    SwingUtilities.invokeLater(() -> {
                        choNgoiDaChon = choNgoi;
                        btnXacNhan.setEnabled(true);
                        btn.setBackground(COLOR_SELECTED);
                        btn.setSelected(true);
                    });
                } else {
                    updateButtonStateWithMessage(btn, false, "Không thể chọn chỗ ngồi này. Vui lòng thử lại.");
                }

            } catch (RemoteException ex) {
                updateButtonStateWithMessage(btn, false, "Lỗi khi chọn chỗ ngồi: " + ex.getMessage());
            }
        }, executorService);
    }

    private void handleSeatDeselection(ChoNgoi choNgoi) {
        CompletableFuture.runAsync(() -> {
            try {
                String maLichTrinh = lichTrinhTau.getMaLich();
                choNgoiDAO.huyKhoaChoNgoi(choNgoi.getMaCho(), maLichTrinh, sessionId);

                // Update UI
                SwingUtilities.invokeLater(() -> {
                    if (choNgoi.equals(choNgoiDaChon)) {
                        choNgoiDaChon = null;
                        btnXacNhan.setEnabled(false);
                    }
                    JToggleButton btn = btnChoNgoi.get(choNgoi.getMaCho());
                    if (btn != null) {
                        updateChoNgoiButtonState(btn, choNgoi);
                    }
                });
            } catch (RemoteException ex) {
                handleException("Lỗi khi hủy chọn chỗ ngồi", ex);
            }
        }, executorService);
    }

    private void updateButtonStateWithMessage(JToggleButton btn, boolean selected, String message) {
        SwingUtilities.invokeLater(() -> {
            btn.setSelected(selected);
            updateChoNgoiButtonState(btn, findChoNgoiByButton(btn));
            JOptionPane.showMessageDialog(ChoNgoiSelectorDialog.this,
                    message, "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
    }

    private void updateChoNgoiButtonState(JToggleButton btn, ChoNgoi choNgoi) {
        if (choNgoi == null) return;

        try {
            // Check if seat is under maintenance
            if (!choNgoi.isTinhTrang()) {
                setButtonState(btn, COLOR_REPAIR, false, "Chỗ ngồi đang sửa chữa");
                return;
            }

            // Get ticket status for this seat in this schedule
            TrangThaiVeTau trangThai = trangThaiChoNgoi.get(choNgoi.getMaCho());

            // Special case: If this seat belongs to the current ticket being changed,
            // we should allow it to be reselected
            try {
                boolean isCurrentTicketSeat = choNgoiDAO.kiemTraChoNgoiThuocVe(choNgoi.getMaCho(), maVeHienTai);
                if (isCurrentTicketSeat) {
                    setButtonState(btn, new Color(173, 216, 230), true, "Chỗ ngồi hiện tại của vé");
                    return;
                }
            } catch (RemoteException e) {
                // Continue with normal flow if this check fails
            }

            // Determine button state based on ticket status
            if (trangThai == TrangThaiVeTau.DA_THANH_TOAN) {
                // Seat is booked with a paid ticket in this schedule
                setButtonState(btn, COLOR_OCCUPIED, false, "Chỗ ngồi đã được đặt trong lịch trình này");
            } else if (trangThai == TrangThaiVeTau.DA_TRA || trangThai == TrangThaiVeTau.DA_DOI) {
                // Seat has a returned or exchanged ticket in this schedule - can be booked
                setButtonState(btn, COLOR_AVAILABLE_IN_OTHER, true, "Chỗ ngồi có thể đặt (đã có vé cũ đã trả/đổi)");
            } else {
                // Seat is available in this schedule
                setButtonState(btn, COLOR_EMPTY, true, "Chỗ ngồi trống");
            }

            // If this is the selected seat, override color
            if (choNgoiDaChon != null && choNgoi.getMaCho().equals(choNgoiDaChon.getMaCho())) {
                setButtonState(btn, COLOR_SELECTED, true, "Chỗ ngồi đang chọn");
                btn.setSelected(true);
            }

            // Rest of your method...
        } catch (Exception e) {
            e.printStackTrace();
            setButtonState(btn, Color.LIGHT_GRAY, false, "Không thể xác định trạng thái");
        }
    }

    // Phương thức hỗ trợ thiết lập trạng thái cho button
    private void setButtonState(JToggleButton btn, Color bgColor, boolean enabled, String tooltip) {
        btn.setBackground(bgColor);
        btn.setEnabled(enabled);
        if (!enabled) {
            btn.setSelected(false);
        }
        SwingUtilities.invokeLater(btn::repaint);
    }

    private void xacNhanChonChoNgoi() {
        if (choNgoiDaChon == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một chỗ ngồi.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (callback != null) {
            callback.onChoNgoiSelected(choNgoiDaChon);
        }

        // Không hủy khóa vì client sẽ tiếp tục quy trình đặt vé
        dispose();
    }

    private void huyKhoaChoNgoi() {
        if (choNgoiDaChon != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    String maLichTrinh = lichTrinhTau.getMaLich();
                    choNgoiDAO.huyKhoaChoNgoi(choNgoiDaChon.getMaCho(), maLichTrinh, sessionId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }, executorService);
            choNgoiDaChon = null;
        }
    }

    @Override
    public void capNhatTrangThaiDatChoNgoi(String maCho, String maLichTrinh, boolean daDat, String sessionId) throws RemoteException {
        // Ignore if caused by this client or different schedule
        if (this.sessionId.equals(sessionId) || !lichTrinhTau.getMaLich().equals(maLichTrinh)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JToggleButton btn = btnChoNgoi.get(maCho);
            if (btn != null) {
                try {
                    ChoNgoi choNgoi = findChoNgoiByMa(maCho);

                    if (choNgoi != null) {
                        // If seat was selected by this client but booked by another client
                        if (daDat && choNgoiDaChon != null && choNgoiDaChon.getMaCho().equals(maCho)) {
                            choNgoiDaChon = null;
                            btnXacNhan.setEnabled(false);
                            JOptionPane.showMessageDialog(ChoNgoiSelectorDialog.this,
                                    "Chỗ ngồi bạn đang chọn đã được đặt bởi người khác.",
                                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                        }

                        // Update seat status in our map
                        if (daDat) {
                            trangThaiChoNgoi.put(maCho, TrangThaiVeTau.DA_THANH_TOAN);
                        } else {
                            trangThaiChoNgoi.remove(maCho);
                        }

                        // Update button state
                        updateChoNgoiButtonState(btn, choNgoi);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void capNhatKhaNangSuDungChoNgoi(String maCho, boolean khaDung) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JToggleButton btn = btnChoNgoi.get(maCho);
            if (btn != null) {
                try {
                    ChoNgoi choNgoi = findChoNgoiByMa(maCho);

                    if (choNgoi != null) {
                        // Update seat status
                        choNgoi.setTinhTrang(khaDung);

                        // If seat is no longer available and was selected
                        if (!khaDung && choNgoiDaChon != null && choNgoiDaChon.getMaCho().equals(maCho)) {
                            CompletableFuture.runAsync(() -> {
                                try {
                                    choNgoiDAO.huyKhoaChoNgoi(maCho, lichTrinhTau.getMaLich(), sessionId);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }, executorService);

                            choNgoiDaChon = null;
                            btnXacNhan.setEnabled(false);
                            JOptionPane.showMessageDialog(ChoNgoiSelectorDialog.this,
                                    "Chỗ ngồi bạn đang chọn đã chuyển sang trạng thái sửa chữa.",
                                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                        }

                        updateChoNgoiButtonState(btn, choNgoi);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void capNhatDanhSachChoNgoi() throws RemoteException {
        // Reload all data asynchronously
        SwingUtilities.invokeLater(this::loadChoNgoiAsync);
    }

    // Helper method to find a seat by ID
    private ChoNgoi findChoNgoiByMa(String maCho) {
        for (ChoNgoi choNgoi : dsChoNgoi) {
            if (choNgoi.getMaCho().equals(maCho)) {
                return choNgoi;
            }
        }
        return null;
    }

    // Helper method to find a seat by button
    private ChoNgoi findChoNgoiByButton(JToggleButton btn) {
        for (Map.Entry<String, JToggleButton> entry : btnChoNgoi.entrySet()) {
            if (entry.getValue() == btn) {
                return findChoNgoiByMa(entry.getKey());
            }
        }
        return null;
    }

    // Helper method to show/hide loading indicator
    private void setLoading(boolean isLoading, String status) {
        SwingUtilities.invokeLater(() -> {
            progressLoading.setVisible(isLoading);
            lblStatus.setText(status);
        });
    }

    // Helper method for exception handling
    private void handleException(String message, Throwable e) {
        e.printStackTrace();
        SwingUtilities.invokeLater(() -> {
            setLoading(false, "Đã xảy ra lỗi");
            JOptionPane.showMessageDialog(this,
                    message + ": " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
    }

    // Interface callback để thông báo về chỗ ngồi đã chọn
    public interface ChoNgoiSelectorCallback {
        void onChoNgoiSelected(ChoNgoi choNgoi);
    }
}