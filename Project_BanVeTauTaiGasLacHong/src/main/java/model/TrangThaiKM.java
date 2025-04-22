package model;

public enum TrangThaiKM {
    DANG_DIEN_RA("Đang diễn ra"),
    HET_HAN("Hết hạn");

    private final String value;

    TrangThaiKM(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
