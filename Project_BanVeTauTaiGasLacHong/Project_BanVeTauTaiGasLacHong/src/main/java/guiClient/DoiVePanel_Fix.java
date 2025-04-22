// This is a fix for the xuLyThanhToan method in DoiVePanel.java

private boolean xuLyThanhToan() throws RemoteException {
    try {
        // 1. Tìm khách hàng từ mã vé
        KhachHang khachHang = doiVeDAO.getKhachHangByMaVe(veTauHienTai.getMaVe());
        if (khachHang == null) {
            throw new Exception("Không tìm thấy thông tin khách hàng!");
        }
        System.out.println("Đã tìm thấy KhachHang: " + khachHang.getMaKhachHang());

        // Tạo vé mới với đầy đủ thông tin
        VeTau veTauMoi = new VeTau();
        veTauMoi.setMaVe(generateMaVe());
        
        // Sao chép thông tin từ vé hiện tại
        veTauMoi.setTenKhachHang(veTauHienTai.getTenKhachHang());
        veTauMoi.setGiayTo(veTauHienTai.getGiayTo());
        veTauMoi.setNgayDi(veTauHienTai.getNgayDi());
        veTauMoi.setDoiTuong(veTauHienTai.getDoiTuong());
        veTauMoi.setLichTrinhTau(lichTrinhDaChon);
        veTauMoi.setKhuyenMai(khuyenMaiDaChon);
        veTauMoi.setGiaVe(veTauHienTai.getGiaVe());
        veTauMoi.setTrangThai(TrangThaiVeTau.CHO_XAC_NHAN);
        
        // Lưu vé mới
        boolean luuVeMoi = doiVeDAO.datVe(veTauMoi, choNgoiDaChon.getMaCho());
        if (!luuVeMoi) {
            throw new Exception("Không thể tạo vé mới!");
        }

        double vat = 0.1; // VAT 10%
        double tienThue = veTauHienTai.getGiaVe() * vat;
        double thanhTien = veTauHienTai.getGiaVe() + tienThue;
        double tongTien = thanhTien;
        
        // 2. Tạo hóa đơn mới
        HoaDon hoaDon = new HoaDon();
        String maHD = generateMaHD();
        System.out.println("Generated MaHD: " + maHD);
        hoaDon.setMaHD(maHD);
        hoaDon.setNgayLap(LocalDateTime.now());
        hoaDon.setTienGiam(giaVeBanDau - tongTien);
        hoaDon.setTongTien(tongTien);
        hoaDon.setKhachHang(khachHang);

        // Debugging the NhanVien reference
        if (nhanVienPanel == null) {
            System.err.println("ERROR: nhanVienPanel is null");
            throw new Exception("Thiếu thông tin nhân viên!");
        }
        System.out.println("NhanVien info: " + nhanVienPanel.getClass().getName());
        hoaDon.setNv(nhanVienPanel);

        // Get LoaiHoaDon and verify it exists
        LoaiHoaDon loaiHoaDon = loaiHoaDonDAO.findById("LHD001");
        if (loaiHoaDon == null) {
            System.err.println("ERROR: Không tìm thấy loại hóa đơn LHD001");
            throw new Exception("Không tìm thấy loại hóa đơn!");
        }
        System.out.println("Found LoaiHoaDon: " + loaiHoaDon.getMaLoaiHoaDon());
        hoaDon.setLoaiHoaDon(loaiHoaDon);

        // 3. Lưu hóa đơn
        System.out.println("Attempting to save HoaDon...");
        boolean savedHoaDon = hoaDonDAO.saveHoaDon(hoaDon);
        if (!savedHoaDon) {
            System.err.println("Failed to save HoaDon!");
            throw new Exception("Không thể lưu hóa đơn!");
        }
        System.out.println("HoaDon saved successfully: " + hoaDon.getMaHD());

        // 4. Tạo chi tiết hóa đơn
        ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
        ChiTietHoaDonId chiTietId = new ChiTietHoaDonId();
        chiTietId.setMaHD(hoaDon.getMaHD());
        chiTietId.setMaVe(veTauMoi.getMaVe());
        chiTietHoaDon.setId(chiTietId);
        chiTietHoaDon.setHoaDon(hoaDon);
        chiTietHoaDon.setVeTau(veTauMoi);
        chiTietHoaDon.setSoLuong(1);
        chiTietHoaDon.setVAT(vat);
        chiTietHoaDon.setTienThue(tienThue);
        chiTietHoaDon.setThanhTien(thanhTien);

        // 5. Lưu chi tiết hóa đơn
        System.out.println("Attempting to save ChiTietHoaDon...");
        boolean savedChiTiet = chiTietHoaDonDAO.save(chiTietHoaDon);
        if (!savedChiTiet) {
            System.err.println("Failed to save ChiTietHoaDon!");
            throw new Exception("Không thể lưu chi tiết hóa đơn!");
        }
        System.out.println("ChiTietHoaDon saved successfully");

        // 6. Cập nhật trạng thái vé cũ thành DA_DOI
        boolean updatedOldTicket = doiVeDAO.capNhatTrangThaiVe(veTauHienTai.getMaVe(), TrangThaiVeTau.DA_DOI);
        if (!updatedOldTicket) {
            System.err.println("Failed to update old ticket status!");
            throw new Exception("Không thể cập nhật trạng thái vé cũ!");
        }
        System.out.println("Old ticket status updated successfully");

        // 7. Cập nhật vé hiện tại thành vé mới
        veTauHienTai = veTauMoi;

        return true;
    } catch (Exception e) {
        System.err.println("Error in xuLyThanhToan: " + e.getMessage());
        e.printStackTrace();
        throw new RemoteException("Lỗi khi xử lý thanh toán: " + e.getMessage(), e);
    }
}
