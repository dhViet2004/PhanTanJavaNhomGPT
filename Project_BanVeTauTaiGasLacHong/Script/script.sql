select * from toatau;
select * from tau;
select * from tuyentau;
select * from loaitoa;
#native query
select * from toatau where so_ghe between 75 and 80;
select * from tuyentau where ga_di = "Mayme Centers" and ga_den = "Heller Cliff";
select * from taikhoan;
select * from nhanvien;
select * from lichlamviec;

SELECT DISTINCT trang_thai FROM lichtrinhtau;

UPDATE vetau SET trang_thai = 'DA_TRA' WHERE trang_thai = 'Đã trả';

select * from vetau