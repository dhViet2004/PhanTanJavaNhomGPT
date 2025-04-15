package dao;

import model.LichTrinhTau;

import java.time.LocalDate;
import java.util.List;

public interface LichTrinhTauDAO {
    List<LichTrinhTau> getAllList();
    LichTrinhTau getById(String id);
    boolean save(LichTrinhTau lichTrinhTau);
    boolean update(LichTrinhTau lichTrinhTau);
    boolean delete(LichTrinhTau lichTrinhTau);
    List<LichTrinhTau> getListLichTrinhTauByDate(LocalDate date);
    List<LichTrinhTau> getListLichTrinhTauByDateAndGaDi(LocalDate date, String gaDi);
    List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDen(LocalDate date, String gaDi, String gaDen);
    List<LichTrinhTau> getListLichTrinhTauByDateAndGaDiGaDenAndGioDi(LocalDate date, String gaDi, String gaDen, String gioDi);
}
