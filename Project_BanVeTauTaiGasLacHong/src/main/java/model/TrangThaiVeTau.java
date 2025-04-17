package model;

public enum TrangThaiVeTau {
    CHO_XAC_NHAN("Chờ xác nhận"),
    DA_THANH_TOAN("Đã thanh toán"),
    DA_DOI("Đã đổi"),
    DA_TRA("Đã trả");

    private final String value;

    TrangThaiVeTau(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TrangThaiVeTau fromValue(String value) {
        for (TrangThaiVeTau status : TrangThaiVeTau.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}