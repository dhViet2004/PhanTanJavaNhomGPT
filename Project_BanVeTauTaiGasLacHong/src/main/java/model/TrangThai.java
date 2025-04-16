package model;

public enum TrangThai {
    DA_KHOI_HANH("Đã khởi hành"),
    CHUA_KHOI_HANH("Chưa khởi hành"),
    DA_HUY("Đã hủy"),
    HOAT_DONG("Hoạt động");

    private final String value;

    TrangThai(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TrangThai fromValue(String value) {
        for (TrangThai status : TrangThai.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}