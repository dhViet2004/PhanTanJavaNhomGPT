package model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Objects;
@Data
@Embeddable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChiTietHoaDonId implements Serializable {
    @EqualsAndHashCode.Include
    private String maHD;
    @EqualsAndHashCode.Include
    private String maVe;

}
