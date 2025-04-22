package model;

public enum DoiTuongApDung {
    ALL("Tất cả"),
    KHACH_HANG_THUONG("Khách hàng thường"),
    KHACH_HANG_VIP("Khách hàng VIP"),
    KHACH_HANG_THAN_THIET("Khách hàng thân thiết"),
    COUPON("Coupon");

    private final String value;

    DoiTuongApDung(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DoiTuongApDung fromValue(String value) {
        for (DoiTuongApDung doiTuong : DoiTuongApDung.values()) {
            if (doiTuong.value.equals(value)) {
                return doiTuong;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}
