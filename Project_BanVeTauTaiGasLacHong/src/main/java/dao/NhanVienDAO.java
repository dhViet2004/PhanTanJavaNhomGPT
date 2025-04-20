package dao;

import model.NhanVien;

import java.util.List;

public interface NhanVienDAO {
    NhanVien getnhanvienById(String id);

    boolean save(NhanVien nv);

    boolean update(NhanVien nv);

    boolean delete(String id);

    List<NhanVien> getAllNhanVien();

}
