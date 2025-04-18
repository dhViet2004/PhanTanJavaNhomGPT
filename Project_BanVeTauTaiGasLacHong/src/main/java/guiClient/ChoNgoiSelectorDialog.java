package guiClient;

import dao.ChoNgoiCallback;
import dao.ChoNgoiDoiVeDAO;
import dao.ToaTauDoiVeDAO;
import model.ChoNgoi;
import model.LichTrinhTau;
import model.ToaTau;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;

public class ChoNgoiSelectorDialog extends JDialog implements ChoNgoiCallback {
    private JComboBox<ToaTau> cboToaTau;
    private JPanel pnlChoNgoi;
    private JButton btnXacNhan;
    private JButton btnHuy;

    private LichTrinhTau lichTrinhTau;
    private ChoNgoiDoiVeDAO choNgoiDAO;
    private ToaTauDoiVeDAO toaTauDAO;
    private ChoNgoi choNgoiDaChon;
    private String sessionId;
    private Map<String, JToggleButton> btnChoNgoi;
    private List<ChoNgoi> dsChoNgoi;

    private ChoNgoiSelectorCallback callback;

    public ChoNgoiSelectorDialog(Frame owner, LichTrinhTau lichTrinhTau,
                                 ChoNgoiDoiVeDAO choNgoiDAO, ToaTauDoiVeDAO toaTauDAO,
                                 ChoNgoiSelectorCallback callback) {
        super(owner, "Chọn chỗ ngồi", true);
        this.lichTrinhTau = lichTrinhTau;
        this.choNgoiDAO = choNgoiDAO;
        this.toaTauDAO = toaTauDAO;
        this.callback = callback;
        this.sessionId = UUID.randomUUID().toString();
        this.btnChoNgoi = new HashMap<>();
        this.dsChoNgoi = new ArrayList<>();

        initComponents();
        loadToaTau();

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
                huyKhoaChoNgoi();
                try {
                    choNgoiDAO.huyDangKyClientChoThongBao(ChoNgoiSelectorDialog.this);
                    UnicastRemoteObject.unexportObject(ChoNgoiSelectorDialog.this, true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(800, 600);
        setLocationRelativeTo(getOwner());

        // Panel chọn toa tàu
        JPanel pnlSelectToa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSelectToa.add(new JLabel("Chọn toa tàu:"));
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
        cboToaTau.addActionListener(e -> loadChoNgoi());
        pnlSelectToa.add(cboToaTau);
        add(pnlSelectToa, BorderLayout.NORTH);

        // Panel hiển thị chỗ ngồi
        pnlChoNgoi = new JPanel();
        pnlChoNgoi.setLayout(new GridLayout(0, 8, 10, 10)); // 8 chỗ ngồi mỗi hàng
        JScrollPane scrollPane = new JScrollPane(pnlChoNgoi);
        add(scrollPane, BorderLayout.CENTER);

        // Panel nút điều khiển
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
        add(pnlControls, BorderLayout.SOUTH);

        // Panel thông tin
        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.add(new JLabel("<html><h3>Thông tin lịch trình:</h3></html>"));
        pnlInfo.add(new JLabel("Mã lịch: " + lichTrinhTau.getMaLich()));
        pnlInfo.add(new JLabel("Ngày đi: " + lichTrinhTau.getNgayDi()));
        pnlInfo.add(new JLabel("Giờ đi: " + lichTrinhTau.getGioDi()));
        pnlInfo.add(new JLabel("Tuyến: " +
                lichTrinhTau.getTau().getTuyenTau().getGaDi() + " - " +
                lichTrinhTau.getTau().getTuyenTau().getGaDen()));

        // Chú thích màu sắc
        pnlInfo.add(Box.createVerticalStrut(20));
        pnlInfo.add(new JLabel("<html><h3>Chú thích:</h3></html>"));

        JPanel pnlChuThich = new JPanel(new GridLayout(4, 2, 5, 5));

        JPanel pnlTrong = new JPanel();
        pnlTrong.setBackground(Color.WHITE);
        pnlTrong.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlChuThich.add(new JLabel("Trống: ", JLabel.RIGHT));
        pnlChuThich.add(pnlTrong);

        JPanel pnlDaDat = new JPanel();
        pnlDaDat.setBackground(Color.RED);
        pnlDaDat.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlChuThich.add(new JLabel("Đã đặt: ", JLabel.RIGHT));
        pnlChuThich.add(pnlDaDat);

        JPanel pnlDaChon = new JPanel();
        pnlDaChon.setBackground(Color.GREEN);
        pnlDaChon.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlChuThich.add(new JLabel("Đang chọn: ", JLabel.RIGHT));
        pnlChuThich.add(pnlDaChon);

        JPanel pnlSuaChua = new JPanel();
        pnlSuaChua.setBackground(Color.GRAY);
        pnlSuaChua.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlChuThich.add(new JLabel("Đang sửa chữa: ", JLabel.RIGHT));
        pnlChuThich.add(pnlSuaChua);

        pnlInfo.add(pnlChuThich);

        pnlInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(pnlInfo, BorderLayout.EAST);
    }

    private void loadToaTau() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            String maTau = lichTrinhTau.getTau().getMaTau();
            List<ToaTau> dsToaTau = toaTauDAO.getToaTauByMaTau(maTau);

            // Đảm bảo các thuộc tính lazy được tải trước khi đóng EntityManager
            for (ToaTau toaTau : dsToaTau) {
                if (toaTau.getLoaiToa() != null) {
                    // Truy cập thuộc tính để kích hoạt lazy loading
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
                loadChoNgoi();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách toa tàu: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void loadChoNgoi() {
        pnlChoNgoi.removeAll();
        btnChoNgoi.clear();
        dsChoNgoi.clear();
        choNgoiDaChon = null;
        btnXacNhan.setEnabled(false);

        ToaTau toaTau = (ToaTau) cboToaTau.getSelectedItem();
        if (toaTau == null) {
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            dsChoNgoi = choNgoiDAO.getChoNgoiByToaTau(toaTau.getMaToa());

            for (ChoNgoi choNgoi : dsChoNgoi) {
                JToggleButton btn = createChoNgoiButton(choNgoi);
                pnlChoNgoi.add(btn);
                btnChoNgoi.put(choNgoi.getMaCho(), btn);
            }

            pnlChoNgoi.revalidate();
            pnlChoNgoi.repaint();
        } catch (RemoteException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách chỗ ngồi: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private JToggleButton createChoNgoiButton(ChoNgoi choNgoi) {
        JToggleButton btn = new JToggleButton(choNgoi.getTenCho());
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(80, 80));

        // Thiết lập màu sắc và trạng thái
        updateChoNgoiButtonState(btn, choNgoi);

        btn.addActionListener(e -> {
            try {
                // Chọn chỗ ngồi mới
                if (btn.isSelected()) {
                    String maLichTrinh = lichTrinhTau.getMaLich();

                    // Kiểm tra xem chỗ ngồi có thể sử dụng không (không đang sửa chữa)
                    if (!choNgoi.isTinhTrang()) {
                        btn.setSelected(false);
                        JOptionPane.showMessageDialog(this,
                                "Chỗ ngồi này đang sửa chữa, không thể đặt.",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Kiểm tra xem chỗ ngồi đã được đặt trong cùng lịch trình chưa
                    if (choNgoiDAO.kiemTraChoNgoiDaDat(choNgoi.getMaCho(), maLichTrinh)) {
                        btn.setSelected(false);
                        JOptionPane.showMessageDialog(this,
                                "Chỗ ngồi này đã được đặt trong lịch trình này. Vui lòng chọn chỗ khác.",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Hủy khóa chỗ ngồi cũ
                    if (choNgoiDaChon != null) {
                        choNgoiDAO.huyKhoaChoNgoi(choNgoiDaChon.getMaCho(), maLichTrinh, sessionId);
                        JToggleButton oldBtn = btnChoNgoi.get(choNgoiDaChon.getMaCho());
                        if (oldBtn != null) {
                            oldBtn.setSelected(false);
                            updateChoNgoiButtonState(oldBtn, choNgoiDaChon);
                        }
                    }

                    // Khóa chỗ ngồi mới
                    boolean success = choNgoiDAO.khoaChoNgoi(choNgoi.getMaCho(), maLichTrinh, sessionId, 5 * 60 * 1000); // 5 phút
                    if (success) {
                        choNgoiDaChon = choNgoi;
                        btnXacNhan.setEnabled(true);
                        btn.setBackground(Color.GREEN);
                    } else {
                        btn.setSelected(false);
                        JOptionPane.showMessageDialog(this,
                                "Không thể chọn chỗ ngồi này. Vui lòng thử lại.",
                                "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    // Hủy chọn chỗ ngồi
                    String maLichTrinh = lichTrinhTau.getMaLich();
                    choNgoiDAO.huyKhoaChoNgoi(choNgoi.getMaCho(), maLichTrinh, sessionId);
                    if (choNgoi.equals(choNgoiDaChon)) {
                        choNgoiDaChon = null;
                        btnXacNhan.setEnabled(false);
                    }
                    updateChoNgoiButtonState(btn, choNgoi);
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi chọn chỗ ngồi: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        return btn;
    }

    private void updateChoNgoiButtonState(JToggleButton btn, ChoNgoi choNgoi) {
        try {
            String maLichTrinh = lichTrinhTau.getMaLich();

            // Kiểm tra tình trạng chỗ ngồi (có thể sử dụng hay đang sửa chữa)
            if (!choNgoi.isTinhTrang()) {
                // Chỗ ngồi đang sửa chữa (tinh_trang = false)
                btn.setBackground(Color.GRAY);
                btn.setEnabled(false);
                btn.setSelected(false);
                btn.setToolTipText("Chỗ ngồi đang sửa chữa");
                return;
            }

            // Kiểm tra xem chỗ ngồi đã được đặt trong cùng lịch trình chưa
            boolean daDat = choNgoiDAO.kiemTraChoNgoiDaDat(choNgoi.getMaCho(), maLichTrinh);

            if (daDat) {
                // Chỗ ngồi đã được đặt trong cùng lịch trình
                btn.setBackground(Color.RED);
                btn.setEnabled(false);
                btn.setSelected(false);
                btn.setToolTipText("Chỗ ngồi đã được đặt");
            } else {
                // Chỗ ngồi trống và có thể đặt
                btn.setBackground(Color.WHITE);
                btn.setEnabled(true);
                btn.setToolTipText("Chỗ ngồi trống");
            }

            // Nếu đây là chỗ ngồi đang chọn
            if (choNgoiDaChon != null && choNgoi.getMaCho().equals(choNgoiDaChon.getMaCho())) {
                btn.setBackground(Color.GREEN);
                btn.setSelected(true);
                btn.setToolTipText("Chỗ ngồi đang chọn");
            }

            // Hiển thị thông tin giá khi hover
            String tooltip = "<html>" +
                    "Mã chỗ: " + choNgoi.getMaCho() + "<br>" +
                    "Tên chỗ: " + choNgoi.getTenCho() + "<br>" +
                    "Loại chỗ: " + (choNgoi.getLoaiCho() != null ? choNgoi.getLoaiCho().getTenLoai() : "Không xác định") + "<br>" +
                    "Giá: " + String.format("%,.0f VNĐ", choNgoi.getGiaTien()) + "<br>" +
                    "Trạng thái: " + (choNgoi.isTinhTrang() ? "Khả dụng" : "Đang sửa chữa") +
                    "</html>";
            btn.setToolTipText(tooltip);

        } catch (RemoteException e) {
            e.printStackTrace();
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setToolTipText("Không thể xác định trạng thái");
        }
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
            try {
                String maLichTrinh = lichTrinhTau.getMaLich();
                choNgoiDAO.huyKhoaChoNgoi(choNgoiDaChon.getMaCho(), maLichTrinh, sessionId);
                choNgoiDaChon = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void capNhatTrangThaiDatChoNgoi(String maCho, String maLichTrinh, boolean daDat, String sessionId) throws RemoteException {
        // Không xử lý nếu là thay đổi do chính client này gây ra
        if (this.sessionId.equals(sessionId)) {
            return;
        }

        // Chỉ xử lý nếu đang xem cùng một lịch trình
        if (!lichTrinhTau.getMaLich().equals(maLichTrinh)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JToggleButton btn = btnChoNgoi.get(maCho);
            if (btn != null) {
                try {
                    ChoNgoi choNgoi = null;
                    // Tìm ChoNgoi tương ứng với mã chỗ
                    for (ChoNgoi cn : dsChoNgoi) {
                        if (cn.getMaCho().equals(maCho)) {
                            choNgoi = cn;
                            break;
                        }
                    }

                    if (choNgoi != null) {
                        // Nếu chỗ ngồi đã được đặt bởi client khác và đang được chọn bởi client hiện tại
                        if (daDat && choNgoiDaChon != null && choNgoiDaChon.getMaCho().equals(maCho)) {
                            // Hủy chọn chỗ ngồi hiện tại
                            choNgoiDaChon = null;
                            btnXacNhan.setEnabled(false);
                            JOptionPane.showMessageDialog(ChoNgoiSelectorDialog.this,
                                    "Chỗ ngồi bạn đang chọn đã được đặt bởi người khác.",
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
    public void capNhatKhaNangSuDungChoNgoi(String maCho, boolean khaDung) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JToggleButton btn = btnChoNgoi.get(maCho);
            if (btn != null) {
                try {
                    ChoNgoi choNgoi = null;
                    // Tìm ChoNgoi tương ứng với mã chỗ
                    for (ChoNgoi cn : dsChoNgoi) {
                        if (cn.getMaCho().equals(maCho)) {
                            choNgoi = cn;
                            cn.setTinhTrang(khaDung); // Cập nhật trạng thái
                            break;
                        }
                    }

                    if (choNgoi != null) {
                        // Nếu chỗ ngồi không còn khả dụng và đang được chọn bởi client hiện tại
                        if (!khaDung && choNgoiDaChon != null && choNgoiDaChon.getMaCho().equals(maCho)) {
                            // Hủy chọn chỗ ngồi hiện tại
                            choNgoiDAO.huyKhoaChoNgoi(maCho, lichTrinhTau.getMaLich(), sessionId);
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
        SwingUtilities.invokeLater(() -> {
            try {
                // Tải lại danh sách chỗ ngồi
                if (cboToaTau.getSelectedItem() != null) {
                    ToaTau toaTau = (ToaTau) cboToaTau.getSelectedItem();
                    dsChoNgoi = choNgoiDAO.getChoNgoiByToaTau(toaTau.getMaToa());

                    // Cập nhật giao diện
                    loadChoNgoi();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    // Interface callback để thông báo về chỗ ngồi đã chọn
    public interface ChoNgoiSelectorCallback {
        void onChoNgoiSelected(ChoNgoi choNgoi);
    }
}