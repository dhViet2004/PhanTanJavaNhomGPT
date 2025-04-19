package guiClient;

import com.toedter.calendar.JDateChooser;
import dao.NhanVienDAO;
import dao.impl.NhanVienDAOImpl;
import model.NhanVien;
import model.RoundedBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QuanLyNhanVienPanel extends JPanel implements ActionListener {

    private ArrayList<NhanVien> danhSachNhanVien;
    private JTextField txtMaNV, txtTenNV, txtSoDT, txtCCCD, txtDiaChi;
    private JButton btnTaiAnh;
    private String tenFileAnhDuocChon = "";

    private JComboBox<String> cmbChucVu;
    private JLabel lblAnh;
    private static final int AVATAR_WIDTH = 200;
    private static final int AVATAR_HEIGHT = 200;
    private JLabel lblNhanVienDangChon = null;
    private JDateChooser dateChooserNgayVaoLam;
    private NhanVienDAO nhanVienDAO = new NhanVienDAOImpl();
    private JButton btnThem;
    private JButton btnSua;
    private JButton btnLamMoi;
    private JButton btnLuu;
    private JPanel danhSachPanel;
    private JButton btnXoaNV;
    private boolean isEditMode = false;
    private NhanVien nhanVienDangSua; // thêm biến này ở lớp chứa actionPerformed


    public QuanLyNhanVienPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        try {
            danhSachNhanVien = (ArrayList<NhanVien>) nhanVienDAO.getAllNhanVien();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến cơ sở dữ liệu!");
            danhSachNhanVien = new ArrayList<>();
        }

        initUI();
    }

    private void initUI() {
        Font labelFont = new Font("Arial", Font.ITALIC, 18);
        Font textFont = new Font("Arial", Font.PLAIN, 16);
        Font btnFont = new Font("Arial", Font.PLAIN, 16);

        danhSachPanel = new JPanel();
        danhSachPanel.setLayout(new BoxLayout(danhSachPanel, BoxLayout.Y_AXIS));
        danhSachPanel.setBackground(Color.WHITE);

        for (NhanVien nv : danhSachNhanVien) {
            JPanel panelNhanVien = new JPanel(new BorderLayout());
            panelNhanVien.setBackground(Color.WHITE);
            panelNhanVien.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
            panelNhanVien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel lblNV = new JLabel(nv.getMaNV());
            lblNV.setFont(textFont);
            lblNV.setOpaque(true);
            lblNV.setBackground(Color.WHITE);
            lblNV.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            lblNV.setCursor(new Cursor(Cursor.HAND_CURSOR));

            lblNV.addMouseListener(new MouseAdapter() {
                Color originalBg = lblNV.getBackground();

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (lblNV != lblNhanVienDangChon) {
                        lblNV.setBackground(new Color(220, 220, 220));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (lblNV != lblNhanVienDangChon) {
                        lblNV.setBackground(Color.WHITE);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    hienThiThongTinNhanVien(nv);
                    if (lblNhanVienDangChon != null) {
                        lblNhanVienDangChon.setBackground(Color.WHITE);
                    }
                    lblNV.setBackground(new Color(173, 216, 230));
                    lblNhanVienDangChon = lblNV;
                }
            });

            panelNhanVien.add(lblNV, BorderLayout.CENTER);

            JButton btnXoaNV = taoButton("Xóa", Color.WHITE, Color.ORANGE);
            btnXoaNV.setActionCommand(nv.getMaNV());
            btnXoaNV.addActionListener(e -> {
                String maNVCanXoa = e.getActionCommand();
                int option = JOptionPane.showConfirmDialog(QuanLyNhanVienPanel.this, "Bạn có chắc chắn muốn xóa nhân viên có mã " + maNVCanXoa + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    boolean isDeleted = nhanVienDAO.delete(maNVCanXoa);
                    if (isDeleted) {
                        danhSachNhanVien.removeIf(nhanVien -> nhanVien.getMaNV().equals(maNVCanXoa));
                        for (Component comp : danhSachPanel.getComponents()) {
                            if (comp instanceof JPanel) {
                                JPanel panel = (JPanel) comp;
                                if (panel.getComponentCount() > 1) {
                                    JLabel lbl = (JLabel) panel.getComponent(0);
                                    if (lbl.getText().equals(maNVCanXoa)) {
                                        danhSachPanel.remove(panel);
                                        danhSachPanel.revalidate();
                                        danhSachPanel.repaint();
                                        break;
                                    }
                                }
                            }
                        }
                        JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Nhân viên có mã " + maNVCanXoa + " đã được xóa.");
                        if (lblNhanVienDangChon != null && lblNhanVienDangChon.getText().equals(maNVCanXoa)) {
                            clearThongTinNhanVien();
                            lblNhanVienDangChon = null;
                        }
                    } else {
                        JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Xóa nhân viên không thành công.");
                    }
                }
            });

            panelNhanVien.add(btnXoaNV, BorderLayout.EAST);
            danhSachPanel.add(panelNhanVien);
        }

        JScrollPane scrollPane = new JScrollPane(danhSachPanel);
        scrollPane.setPreferredSize(new Dimension(300, 0));
        add(scrollPane, BorderLayout.WEST);

        JPanel chiTietPanel = new JPanel(new GridBagLayout());
        chiTietPanel.setBackground(new Color(245, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 10, 0);

        lblAnh = new JLabel();
        lblAnh.setPreferredSize(new Dimension(AVATAR_WIDTH, AVATAR_HEIGHT));
        lblAnh.setHorizontalAlignment(SwingConstants.CENTER);
        lblAnh.setVerticalAlignment(SwingConstants.TOP);
        lblAnh.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        chiTietPanel.add(lblAnh, gbc);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.insets = new Insets(5, 10, 5, 10); // Khoảng cách mặc định cho label

        String[] labels = {"Mã NV:", "Tên NV:", "SĐT:", "CCCD:", "Địa chỉ:", "Ngày vào:", "Chức vụ:", "Avatar:"};
        JComponent[] fields = {
                taoTextField(textFont, 200),
                taoTextField(textFont, 200),
                taoTextField(textFont, 200),
                taoTextField(textFont, 200),
                taoTextField(textFont, 200),
                taoDateChooser(),
                taoComboBox(textFont),
                taoButtonTaiAnh(textFont)
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            JLabel label = new JLabel(labels[i]);
            label.setFont(labelFont);
            label.setForeground(Color.DARK_GRAY);
            chiTietPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(5, 5, 5, 10); // Giảm khoảng cách bên trái cho field
            chiTietPanel.add(fields[i], gbc);
        }

        txtMaNV = (JTextField) fields[0];
        txtTenNV = (JTextField) fields[1];
        txtSoDT = (JTextField) fields[2];
        txtCCCD = (JTextField) fields[3];
        txtDiaChi = (JTextField) fields[4];
        dateChooserNgayVaoLam = (JDateChooser) fields[5];
        cmbChucVu = (JComboBox<String>) fields[6];
        btnTaiAnh = (JButton) fields[7];

        txtMaNV.addActionListener(e -> {
            String maTim = txtMaNV.getText().trim();
            if (maTim.isEmpty()) return;

            NhanVien nvTim = null;
            for (NhanVien nv : danhSachNhanVien) {
                if (nv.getMaNV().equalsIgnoreCase(maTim)) {
                    nvTim = nv;
                    break;
                }
            }

            if (nvTim != null) {
                hienThiThongTinNhanVien(nvTim);
                if (lblNhanVienDangChon != null) {
                    lblNhanVienDangChon.setBackground(Color.WHITE);
                }
                for (Component comp : danhSachPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel panelNhanVien = (JPanel) comp;
                        if (panelNhanVien.getComponentCount() > 0) {
                            JLabel lbl = (JLabel) panelNhanVien.getComponent(0);
                            if (lbl.getText().equalsIgnoreCase(maTim)) {
                                lbl.dispatchEvent(new MouseEvent(lbl, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 1, false));
                                break;
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy mã nhân viên: " + maTim);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 50));
        btnThem = taoButton("Thêm", new Color(0x4CAF50), Color.WHITE);      // Xanh lá
        btnSua = taoButton("Sửa", new Color(0xFFEB3B), Color.BLACK);        // Vàng
        btnLamMoi = taoButton("Làm mới", new Color(0x03A9F4), Color.WHITE); // Xanh nước biển
        btnLuu = taoButton("Lưu", new Color(0xE91E63), Color.WHITE);        // Hồng

        btnSua.addActionListener(e -> {
            String ma = txtMaNV.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên hợp lệ!");
                return;
            }

            // Tìm nhân viên theo mã
            for (NhanVien nv : danhSachNhanVien) {
                if (nv.getMaNV().equals(ma)) {
                    nhanVienDangSua = nv;
                    isEditMode = true;
                    setEditableFields(true);
                    txtMaNV.setEditable(false); // không sửa mã
                    break;
                }
            }

            if (nhanVienDangSua == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên cần sửa!");
            }
        });


        btnLuu.addActionListener(this);
        btnThem.addActionListener(this);
        btnLamMoi.addActionListener(this);

        buttonPanel.add(btnThem);
        buttonPanel.add(btnSua);
        buttonPanel.add(btnLamMoi);
        buttonPanel.add(btnLuu);

        gbc.gridx = 0;
        gbc.gridy = labels.length + 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 10, 0);
        chiTietPanel.add(buttonPanel, gbc);

        add(chiTietPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == btnThem) {
            String maNV = generateMaNV();
            LocalDate ngayVaoLam = LocalDate.now();
            txtMaNV.setText(maNV);
            txtTenNV.setText("");
            txtSoDT.setText("");
            txtCCCD.setText("");
            txtDiaChi.setText("");
            dateChooserNgayVaoLam.setDate(java.sql.Date.valueOf(ngayVaoLam));
            cmbChucVu.setSelectedIndex(0);
            lblAnh.setIcon(null);
            setEditableFields(true);
            txtMaNV.setEditable(false);
        } else if (o == btnLamMoi) {
            clearThongTinNhanVien();
            taiLaiDanhSachNhanVien();
            txtMaNV.setEnabled(true);
            setEditableFields(true);
            isEditMode = false;
        }
        else if (o == btnLuu) {
            if (isValidInput()) {
                NhanVien nv;
                if (isEditMode && nhanVienDangSua != null) {
                    // Nếu đang trong chế độ sửa, dùng lại đối tượng cũ
                    nv = nhanVienDangSua;
                } else {
                    // Nếu đang thêm mới
                    nv = new NhanVien();
                    nv.setMaNV(txtMaNV.getText());
                }

                nv.setTenNV(txtTenNV.getText());
                nv.setSoDT(txtSoDT.getText());
                nv.setCccd(txtCCCD.getText());
                nv.setDiaChi(txtDiaChi.getText());

                java.util.Date selectedDate = dateChooserNgayVaoLam.getDate();
                if (selectedDate != null) {
                    nv.setNgayVaoLam(selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                } else {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày vào làm.");
                    return;
                }

                nv.setChucVu(cmbChucVu.getSelectedItem().toString());
                nv.setAvata(tenFileAnhDuocChon);
                nv.setTrangThai("Hoạt động");

                boolean result;

                if (isEditMode) {
                    result = nhanVienDAO.update(nv);
                    if (result) {
                        JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
                        taiLaiDanhSachNhanVien();
                        isEditMode = false;
                        nhanVienDangSua = null;
                        clearThongTinNhanVien();
                    } else {
                        JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại!");
                    }
                } else {
                    result = nhanVienDAO.save(nv);
                    if (result) {
                        JOptionPane.showMessageDialog(this, "Lưu nhân viên thành công!");
                        danhSachNhanVien.add(nv);
                        taiLaiDanhSachNhanVien();
                        clearThongTinNhanVien();
                    } else {
                        JOptionPane.showMessageDialog(this, "Lưu nhân viên không thành công!");
                    }
                }

            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!");
            }


        }

        else if (o == btnTaiAnh) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn ảnh đại diện");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                tenFileAnhDuocChon = file.getName();
                try {
                    java.nio.file.Path source = file.toPath();
                    java.nio.file.Path destination = java.nio.file.Paths.get("src/main/resources/Anh_HeThong/" + tenFileAnhDuocChon);
                    java.nio.file.Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    ImageIcon icon = new ImageIcon(destination.toString());
                    Image scaledImage = icon.getImage().getScaledInstance(AVATAR_WIDTH, AVATAR_HEIGHT, Image.SCALE_SMOOTH);
                    lblAnh.setIcon(new ImageIcon(scaledImage));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi tải ảnh: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    private void clearThongTinNhanVien() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        txtSoDT.setText("");
        txtCCCD.setText("");
        txtDiaChi.setText("");
        dateChooserNgayVaoLam.setDate(null);
        cmbChucVu.setSelectedIndex(0);
        lblAnh.setIcon(null);
        txtMaNV.setEnabled(true);
        setEditableFields(true);
    }

    private JTextField taoTextField(Font font, int width) {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(width, 30));
        tf.setFont(font);
        return tf;
    }

    private JComboBox<String> taoComboBox(Font font) {
        JComboBox<String> cb = new JComboBox<>(new String[]{"Nhân viên", "Quản lý", "Trưởng phòng"});
        cb.setFont(font);
        cb.setPreferredSize(new Dimension(200, 30));
        return cb;
    }

    private JDateChooser taoDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setFont(new Font("Arial", Font.PLAIN, 16));
        dateChooser.setPreferredSize(new Dimension(200, 30));
        return dateChooser;
    }



    private void hienThiThongTinNhanVien(NhanVien nv) {
        txtMaNV.setText(nv.getMaNV());
        txtTenNV.setText(nv.getTenNV());
        txtSoDT.setText(nv.getSoDT());
        txtCCCD.setText(nv.getCccd());
        txtDiaChi.setText(nv.getDiaChi());

        // Chuyển đổi LocalDate sang java.util.Date cho JDateChooser
        LocalDate ngayVaoLam = nv.getNgayVaoLam();
        if (ngayVaoLam != null) {
            dateChooserNgayVaoLam.setDate(java.sql.Date.valueOf(ngayVaoLam));
        } else {
            dateChooserNgayVaoLam.setDate(null); // Hoặc xử lý trường hợp null theo nhu cầu
        }

        cmbChucVu.setSelectedItem(nv.getChucVu());
        tenFileAnhDuocChon = nv.getAvata();

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Anh_HeThong/" + nv.getAvata()));
            Image scaledImage = icon.getImage().getScaledInstance(AVATAR_WIDTH, AVATAR_HEIGHT, Image.SCALE_SMOOTH);
            lblAnh.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            lblAnh.setIcon(null);
            System.err.println("Không load được ảnh: " + e.getMessage());
        }

        setEditableFields(false); // Ngăn chỉnh sửa sau khi hiển thị
    }

    private void setEditableFields(boolean isEditable) {
        txtTenNV.setEditable(isEditable);
        txtSoDT.setEditable(isEditable);
        txtCCCD.setEditable(isEditable);
        txtDiaChi.setEditable(isEditable);
        dateChooserNgayVaoLam.setEnabled(false); // Ngày vào làm không chỉnh sửa
//        txtAvata.setEditable(isEditable);
        cmbChucVu.setEnabled(isEditable);

        // Mã nhân viên sẽ chỉ không cho sửa khi đã hiển thị thông tin
        txtMaNV.setEditable(isEditable);  // Đảm bảo rằng khi làm mới, mã có thể sửa
    }

    private JButton taoButtonTaiAnh(Font font) {
        JButton button = new JButton("Tải ảnh lên");
        button.setFont(font);
        button.setPreferredSize(new Dimension(150, 30));
        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn ảnh đại diện");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                tenFileAnhDuocChon = file.getName();

                // Copy ảnh vào thư mục "Anh_HeThong"
                try {
                    java.nio.file.Path source = file.toPath();
                    java.nio.file.Path destination = java.nio.file.Paths.get("src/main/resources/Anh_HeThong/" + tenFileAnhDuocChon);
                    java.nio.file.Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // Hiển thị ảnh vừa tải lên
                    ImageIcon icon = new ImageIcon(destination.toString());
                    Image scaledImage = icon.getImage().getScaledInstance(AVATAR_WIDTH, AVATAR_HEIGHT, Image.SCALE_SMOOTH);
                    lblAnh.setIcon(new ImageIcon(scaledImage));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi tải ảnh: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        return button;
    }

    private String generateMaNV() {
        // Tạo mã nhân viên tự động theo cú pháp NVXXXX
        int nextId = danhSachNhanVien.size() + 1;
        return String.format("NV%04d", nextId);
    }

    private boolean isValidInput() {
        // Kiểm tra tính hợp lệ của các trường thông tin
        return !txtTenNV.getText().isEmpty() && !txtSoDT.getText().isEmpty() && !txtCCCD.getText().isEmpty() && !txtDiaChi.getText().isEmpty();
    }

    public void taiLaiDanhSachNhanVien() {
        danhSachPanel.removeAll(); // Xóa tất cả nhân viên cũ trong danh sách

        Font textFont = new Font("Arial", Font.PLAIN, 16);
        Font btnFont = new Font("Arial", Font.BOLD, 16);

        List<NhanVien> danhSach = nhanVienDAO.getAllNhanVien(); // Lấy danh sách mới từ DAO
        for (NhanVien nv : danhSach) {
            JPanel panelNhanVien = new JPanel(new BorderLayout());
            panelNhanVien.setBackground(Color.WHITE);
            panelNhanVien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel lblNV = new JLabel(nv.getMaNV());
            lblNV.setFont(textFont);
            lblNV.setOpaque(true);
            lblNV.setBackground(Color.WHITE);
            lblNV.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            lblNV.setCursor(new Cursor(Cursor.HAND_CURSOR));

            lblNV.addMouseListener(new MouseAdapter() {
                Color originalBg = lblNV.getBackground();

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (lblNV != lblNhanVienDangChon) {
                        lblNV.setBackground(new Color(220, 220, 220));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (lblNV != lblNhanVienDangChon) {
                        lblNV.setBackground(Color.WHITE);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    hienThiThongTinNhanVien(nv);
                    if (lblNhanVienDangChon != null) {
                        lblNhanVienDangChon.setBackground(Color.WHITE);
                    }
                    lblNV.setBackground(new Color(173, 216, 230));
                    lblNhanVienDangChon = lblNV;
                }
            });

            panelNhanVien.add(lblNV, BorderLayout.CENTER);
            panelNhanVien.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

            btnXoaNV = taoButton("Xóa", Color.WHITE, Color.ORANGE);
            btnXoaNV.setActionCommand(nv.getMaNV());
            btnXoaNV.addActionListener(e -> {
                String maNVCanXoa = e.getActionCommand();
                int option = JOptionPane.showConfirmDialog(QuanLyNhanVienPanel.this, "Bạn có chắc chắn muốn xóa nhân viên có mã " + maNVCanXoa + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    boolean isDeleted = nhanVienDAO.delete(maNVCanXoa);
                    if (isDeleted) {
                        danhSachNhanVien.removeIf(nhanVien -> nhanVien.getMaNV().equals(maNVCanXoa));
                        for (Component comp : danhSachPanel.getComponents()) {
                            if (comp instanceof JPanel) {
                                JPanel panel = (JPanel) comp;
                                if (panel.getComponentCount() > 1) {
                                    JLabel lbl = (JLabel) panel.getComponent(0);
                                    if (lbl.getText().equals(maNVCanXoa)) {
                                        danhSachPanel.remove(panel);
                                        danhSachPanel.revalidate();
                                        danhSachPanel.repaint();
                                        break;
                                    }
                                }
                            }
                        }
                        JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Nhân viên có mã " + maNVCanXoa + " đã được xóa.");
                        if (lblNhanVienDangChon != null && lblNhanVienDangChon.getText().equals(maNVCanXoa)) {
                            clearThongTinNhanVien();
                            lblNhanVienDangChon = null;
                        }
                    } else {
                        JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Xóa nhân viên không thành công.");
                    }
                }
            });

            panelNhanVien.add(btnXoaNV, BorderLayout.EAST);
            danhSachPanel.add(panelNhanVien);
        }

        danhSachPanel.revalidate();
        danhSachPanel.repaint();
    }

    private JButton taoButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(new RoundedBorder(10));

        // Thiết lập kích thước cố định (ví dụ: 120x40)
        Dimension fixedSize = new Dimension(120, 40);
        button.setPreferredSize(fixedSize);
        button.setMinimumSize(fixedSize);
        button.setMaximumSize(fixedSize);

        return button;
    }


}
