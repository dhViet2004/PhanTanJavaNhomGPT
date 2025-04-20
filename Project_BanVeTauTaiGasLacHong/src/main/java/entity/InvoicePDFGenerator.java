package entity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoicePDFGenerator {

    public static void generateInvoicePdf(String fileName, InvoiceDetails invoice, List<InvoiceItemDetails> items) {
        try {
            // Tạo đối tượng PdfWriter
            PdfWriter writer = new PdfWriter(fileName);

            // Tạo đối tượng PdfDocument
            PdfDocument pdf = new PdfDocument(writer);

            // Tạo đối tượng Document để thêm các phần tử vào PDF
            Document document = new Document(pdf);
            
            // Thêm font không dấu
            PdfFont font = PdfFontFactory.createFont();

            // Tiêu đề hóa đơn
            document.add(new Paragraph("CONG TY CO PHAN VAN TAI DUONG SAT LAC HONG")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(font)
                    .setBold()
                    .setFontSize(14));

            document.add(new Paragraph("HOA DON BAN VE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(font)
                    .setBold()
                    .setFontSize(18));

            // Tạo mã QR cho mã hóa đơn
            String qrCodePath = "QR_" + invoice.getMaHoaDon() + ".png";
            try {
                createQRCode(invoice.getMaHoaDon(), qrCodePath);
                
                // Thêm mã QR vào PDF
                ImageData qrCodeImage = ImageDataFactory.create(qrCodePath);
                Image qrImage = new Image(qrCodeImage)
                        .setWidth(80)
                        .setHeight(80);
                
                // Thêm QR code vào góc phải trên cùng
                document.add(qrImage.setFixedPosition(pdf.getDefaultPageSize().getWidth() - 100, 
                                                     pdf.getDefaultPageSize().getHeight() - 100));
                
                // Xóa file QR code sau khi sử dụng
                Path path = FileSystems.getDefault().getPath(qrCodePath);
                Files.deleteIfExists(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Thông tin hóa đơn
            document.add(new Paragraph("Ma hoa don: " + invoice.getMaHoaDon())
                    .setFont(font)
                    .setFontSize(12));
            
            // Format ngày lập
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            document.add(new Paragraph("Ngay lap: " + invoice.getNgayLap().format(formatter))
                    .setFont(font)
                    .setFontSize(12));
            
            document.add(new Paragraph("Loai hoa don: " + removeDiacritics(invoice.getLoaiHoaDon()))
                    .setFont(font)
                    .setFontSize(12));
            
            document.add(new Paragraph("Nhan vien: " + removeDiacritics(invoice.getTenNhanVien()) + " (" + invoice.getMaChucVu() + ")")
                    .setFont(font)
                    .setFontSize(12));

            // Thông tin khách hàng
            document.add(new Paragraph("\nTHONG TIN KHACH HANG:")
                    .setFont(font)
                    .setBold()
                    .setFontSize(12));
            
            document.add(new Paragraph("Ten khach hang: " + removeDiacritics(invoice.getTenKhachHang()))
                    .setFont(font)
                    .setFontSize(12));
            
            document.add(new Paragraph("Giay to: " + invoice.getGiayTo())
                    .setFont(font)
                    .setFontSize(12));
            
            if (invoice.getSoDienThoai() != null && !invoice.getSoDienThoai().isEmpty()) {
                document.add(new Paragraph("So dien thoai: " + invoice.getSoDienThoai())
                        .setFont(font)
                        .setFontSize(12));
            }
            
            if (invoice.getDiaChiKhachHang() != null && !invoice.getDiaChiKhachHang().isEmpty()) {
                document.add(new Paragraph("Dia chi: " + removeDiacritics(invoice.getDiaChiKhachHang()))
                        .setFont(font)
                        .setFontSize(12));
            }

            // Bảng chi tiết hóa đơn
            document.add(new Paragraph("\nCHI TIET HOA DON:")
                    .setFont(font)
                    .setBold()
                    .setFontSize(12));
            
            // Tạo bảng chi tiết
            Table table = new Table(new float[]{1, 3, 3, 3, 2, 2, 2});
            table.setWidth(UnitValue.createPercentValue(100));
            
            // Header của bảng
            table.addHeaderCell(createHeaderCell("STT", font));
            table.addHeaderCell(createHeaderCell("Ma Ve", font));
            table.addHeaderCell(createHeaderCell("Hanh Trinh", font));
            table.addHeaderCell(createHeaderCell("Chi Tiet", font));
            table.addHeaderCell(createHeaderCell("Gia Ve", font));
            table.addHeaderCell(createHeaderCell("VAT", font));
            table.addHeaderCell(createHeaderCell("Thanh Tien", font));
            
            // Định dạng số tiền
            DecimalFormat moneyFormatter = new DecimalFormat("#,##0 VND");
            
            // Dữ liệu của bảng
            int stt = 1;
            double totalAmount = 0;
            
            for (InvoiceItemDetails item : items) {
                table.addCell(createCell(String.valueOf(stt++), font));
                table.addCell(createCell(item.getMaVe(), font));
                
                // Hành trình
                String hanhTrinh = removeDiacritics(item.getGaDi()) + " - " + removeDiacritics(item.getGaDen()) +
                                  "\nTau: " + removeDiacritics(item.getTau()) + 
                                  "\nNgay di: " + item.getNgayDi().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
                                  "\nGio di: " + item.getGioDi().format(DateTimeFormatter.ofPattern("HH:mm"));
                table.addCell(createCell(hanhTrinh, font));
                
                // Chi tiết
                String chiTiet = "Hanh khach: " + removeDiacritics(item.getTenHanhKhach()) +
                               "\nDoi tuong: " + removeDiacritics(item.getDoiTuong()) +
                               "\nToa: " + removeDiacritics(item.getToa()) +
                               "\nCho ngoi: " + item.getChoNgoi();
                table.addCell(createCell(chiTiet, font));
                
                table.addCell(createCell(moneyFormatter.format(item.getGiaVe()), font));
                table.addCell(createCell(item.getVat() + "%", font));
                table.addCell(createCell(moneyFormatter.format(item.getThanhTien()), font));
                
                totalAmount += item.getThanhTien();
            }
            
            document.add(table);
            
            // Thông tin thanh toán
            document.add(new Paragraph("\nTHONG TIN THANH TOAN:")
                    .setFont(font)
                    .setBold()
                    .setFontSize(12));
            
            Table summaryTable = new Table(2);
            summaryTable.setWidth(UnitValue.createPercentValue(50));
            
            // Đặt bảng tổng kết ở bên phải
            summaryTable.addCell(createCell("Tong tien:", font).setBold());
            summaryTable.addCell(createCell(moneyFormatter.format(invoice.getTongTien()), font));
            
            summaryTable.addCell(createCell("Tien giam:", font).setBold());
            summaryTable.addCell(createCell(moneyFormatter.format(invoice.getTienGiam()), font));
            
            summaryTable.addCell(createCell("Thanh toan:", font).setBold());
            summaryTable.addCell(createCell(moneyFormatter.format(invoice.getThanhToan()), font));
            
            // Canh phải bảng tổng kết
            document.add(summaryTable.setHorizontalAlignment(HorizontalAlignment.RIGHT));

            // Thêm chân trang
            document.add(new Paragraph("\nCAM ON QUY KHACH DA SU DUNG DICH VU CUA CHUNG TOI!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(font)
                    .setFontSize(12));
            
            // Đóng tài liệu PDF
            document.close();
            
            System.out.println("Hóa đơn đã được tạo thành công: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Tạo cell cho header
    private static Cell createHeaderCell(String text, PdfFont font) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text)
                .setFont(font)
                .setBold()
                .setFontSize(11)
                .setTextAlignment(TextAlignment.CENTER));
        return cell;
    }
    
    // Tạo cell thông thường
    private static Cell createCell(String text, PdfFont font) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text)
                .setFont(font)
                .setFontSize(10));
        return cell;
    }

    // Phương thức tạo mã QR
    private static void createQRCode(String data, String filePath) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 200, 200);
        MatrixToImageWriter.writeToPath(matrix, "PNG", FileSystems.getDefault().getPath(filePath));
    }

    // Phương thức chuyển chuỗi có dấu thành chuỗi không dấu
    private static String removeDiacritics(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Loại bỏ dấu
        String result = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Thay 'Đ' thành 'D'
        result = result.replace('Đ', 'D').replace('đ', 'd');

        return result;
    }
}