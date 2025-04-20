package entity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

// Thêm import model.ChiTietHoaDon để giải quyết lỗi reference
import model.ChiTietHoaDon;
import model.TicketDetails;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TicketPDFGenerator {
    public static void generateTicketPdf(String fileName, List<TicketDetails> ticketDetailsList, List<ChiTietHoaDon> chiTietHoaDonList) {
        try {
            // Tạo đối tượng PdfWriter
            PdfWriter writer = new PdfWriter(fileName);

            // Tạo đối tượng PdfDocument
            PdfDocument pdf = new PdfDocument(writer);

            // Tạo đối tượng Document để thêm các phần tử vào PDF
            Document document = new Document(pdf);

            // Danh sách lưu các đường dẫn của các file QR đã tạo
            List<String> qrCodePaths = new ArrayList<>();

            // Định dạng số tiền
            DecimalFormat decimalFormat = new DecimalFormat("#,##0 VND");

            for (int i = 0; i < ticketDetailsList.size(); i++) {
                TicketDetails ticket = ticketDetailsList.get(i);
                document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------").setHorizontalAlignment(HorizontalAlignment.CENTER));
                // Tiêu đề vé
                Paragraph tenCongTy = new Paragraph("CONG TY CO PHAN VAN TAI DUONG SAT LAC HONG")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFont(PdfFontFactory.createFont())
                        .setFontSize(14);
                document.add(tenCongTy);

                // Tiêu đề vé
                Paragraph title = new Paragraph("THE LEN TAU HOA")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFont(PdfFontFactory.createFont())
                        .setFontSize(18);
                document.add(title);
                // Tiêu đề vé
                Paragraph title1 = new Paragraph("BOARDING PASS")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFont(PdfFontFactory.createFont())
                        .setFontSize(14);
                document.add(title1);
                // Tạo mã QR cho mã vé
                String qrCodePath = "QR_" + chiTietHoaDonList.get(i).getId().getMaVe() + ".png";
                try {
                    createQRCode(chiTietHoaDonList.get(i).getId().getMaVe(), qrCodePath);
                    qrCodePaths.add(qrCodePath);  // Lưu đường dẫn file QR vào danh sách
                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }

                // Thêm mã QR vào PDF với căn giữa
                ImageData qrCodeImage = ImageDataFactory.create(qrCodePath);
                Image qrImage = new Image(qrCodeImage)
                        .setWidth(100)
                        .setHeight(100)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(qrImage);
                // Thông tin khách hàng
                document.add(new Paragraph("MA VE: " + chiTietHoaDonList.get(i).getId().getMaVe()).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("\nTHONG TIN KHACH HANG").setFontSize(14));
                document.add(new Paragraph("Ho va ten: " + removeDiacritics(ticket.getHoTen())));
                document.add(new Paragraph("Doi tuong: " + removeDiacritics(ticket.getDoiTuong())));
                document.add(new Paragraph("Giay to: " + removeDiacritics(ticket.getGiayTo())));

                // Thông tin hành trình
                document.add(new Paragraph("\nTHONG TIN HANH TRINH").setFontSize(14));
                document.add(new Paragraph("Ga di - Ga den: " + removeDiacritics(ticket.getGaDi()) + " - " + removeDiacritics(ticket.getGaDen())));
                document.add(new Paragraph("Tau/Train: " + removeDiacritics(ticket.getTenTau())));
                document.add(new Paragraph("Ngay di/Date: " + ticket.getNgayDi().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
                document.add(new Paragraph("Gio di/Time: " + ticket.getGioDi().format(DateTimeFormatter.ofPattern("HH:mm"))));
                document.add(new Paragraph("Toa/Coach: " + removeDiacritics(ticket.getToaTau())));
                document.add(new Paragraph("Cho/Seat: " + removeDiacritics(ticket.getChoNgoi())));

                // Thông tin giá vé
                document.add(new Paragraph("\nTHONG TIN GIA VE").setFontSize(14));
                document.add(new Paragraph("Gia Ve: " + decimalFormat.format(ticket.getGiaVe())));
                document.add(new Paragraph("VAT 10 % "));
                document.add(new Paragraph("Thanh tien: " + decimalFormat.format(ticket.getThanhTien())));
                document.add(new Paragraph("----------------------------------------------------------------------------------------------------------------------------------").setHorizontalAlignment(HorizontalAlignment.CENTER));
                // Định dạng ngày in
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss, dd/MM/yyyy");
                String formattedDate = LocalDateTime.now().format(formatter);

                // Ngày in căn giữa với định dạng mới
                document.add(new Paragraph("Ngay in: " + formattedDate)
                        .setTextAlignment(TextAlignment.CENTER));

                // Bắt đầu trang mới nếu không phải vé cuối cùng
                if (i < ticketDetailsList.size() - 1) {
                    pdf.addNewPage();
                }
            }

            // Đóng tài liệu PDF
            document.close();

            // Xóa các file QR đã tạo
            for (String qrCodePath : qrCodePaths) {
                Path path = FileSystems.getDefault().getPath(qrCodePath);
                Files.deleteIfExists(path);  // Xóa file QR nếu tồn tại
            }

            System.out.println("Thông tin vé đã được tạo thành công: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Phương thức tạo mã QR
    public static void createQRCode(String data, String filePath) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 200, 200);
        MatrixToImageWriter.writeToPath(matrix, "PNG", FileSystems.getDefault().getPath(filePath));
    }

    // Phương thức chuyển chuỗi có dấu thành chuỗi không dấu
    public static String removeDiacritics(String input) {
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