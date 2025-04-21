package model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Class chứa kết quả thống kê vé tàu
 */
@Data
public class KetQuaThongKeVe implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate thoiGian;
    private TrangThaiVeTau trangThai;
    private String tenTuyen;
    private String loaiToa;
    private int soLuong;
}