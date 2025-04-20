select * from toatau;
select * from tau;
select * from tuyentau;
select * from loaitoa;
#native query
select * from toatau where so_ghe between 75 and 80;

select * from taikhoan;
select * from nhanvien;
select * from lichlamviec;
select * from khachhang;
select * from hoadon;
select * from chitiet_hoadon;

SELECT DISTINCT trang_thai FROM lichtrinhtau;

UPDATE vetau SET trang_thai = 'DA_TRA' WHERE trang_thai = 'Đã trả';

select * from vetau


select * from khachhang;
select * from loaikhachhang;

select * from hoadon;
select * from loaihoadon;
select * from chitiet_hoadon;

select * from khuyenmai;
select * from loaicho;


select * from vetau;

DELETE FROM chitiet_hoadon;
DELETE FROM vetau;
ALTER TABLE vetau AUTO_INCREMENT = 1;


INSERT INTO loaikhachhang (ma_loai_khach_hang, ten_loai_khach_hang)
VALUES
    ('LKH001', 'Khách VIP'),
    ('LKH002', 'Khách vãng lai');

INSERT INTO khachhang (
    ma_khach_hang, sdt, ten_khach_hang, giay_to, dia_chi, diem_tich_luy,
    ngay_sinh, ngay_tham_gia, hang_thanh_vien, ma_loai_khach_hang
)
VALUES
    ('KH180420250001', '0900000001', 'Nguyễn Văn A', 'CMND100001', 'Hà Nội', 10, '1990-01-01', '2024-01-01', 'VIP', 'LKH001'),
    ('KH180420250002', '0900000002', 'Nguyễn Văn B', 'CMND100002', 'Hà Nội', 20, '1991-02-02', '2024-01-11', 'VIP', 'LKH001'),
    ('KH180420250003', '0900000003', 'Nguyễn Văn C', 'CMND100003', 'Hà Nội', 30, '1992-03-03', '2024-01-21', 'VIP', 'LKH001'),
    ('KH180420250004', '0900000004', 'Nguyễn Văn D', 'CMND100004', 'Hà Nội', 40, '1993-04-04', '2024-01-31', 'VIP', 'LKH001'),
    ('KH180420250005', '0900000005', 'Nguyễn Văn E', 'CMND100005', 'Hà Nội', 50, '1994-05-05', '2024-02-10', 'VIP', 'LKH001'),

    ('KH180420250006', '0900000006', 'Nguyễn Văn F', 'CMND100006', 'Hà Nội', 15, '1990-06-06', '2024-03-01', 'Vãng lai', 'LKH002'),
    ('KH180420250007', '0900000007', 'Nguyễn Văn G', 'CMND100007', 'Hà Nội', 25, '1991-07-07', '2024-03-11', 'Vãng lai', 'LKH002'),
    ('KH180420250008', '0900000008', 'Nguyễn Văn H', 'CMND100008', 'Hà Nội', 35, '1992-08-08', '2024-03-21', 'Vãng lai', 'LKH002'),
    ('KH180420250009', '0900000009', 'Nguyễn Văn I', 'CMND100009', 'Hà Nội', 45, '1993-09-09', '2024-03-31', 'Vãng lai', 'LKH002'),
    ('KH180420250010', '0900000010', 'Nguyễn Văn K', 'CMND100010', 'Hà Nội', 55, '1994-10-10', '2024-04-10', 'Vãng lai', 'LKH002');

select * from cho_ngoi
where toa_tau_ma_toa = 'T101';

update cho_ngoi set tinh_trang = 1 where tinh_trang = 0;

select * from toatau
where ma_toa = 'T101';

select * from tau
where ma_tau = 'T10';

INSERT INTO vetau (ma_ve, doi_tuong, gia_ve, giay_to, ngay_di, ten_khach_hang, trang_thai, cho_ngoi_ma_cho, khuyen_mai_ma_km, lich_trinh_tau_ma_lich)
VALUES
    ('LLT20250418192106-013-001', 'Sinh viên', '200000', '012345678', '2025-04-18', 'Nguyễn Văn A', 'DA_THANH_TOAN', 'CN10AT101', NULL, 'LLT20250418192106-013');

INSERT INTO vetau (ma_ve, doi_tuong, gia_ve, giay_to, ngay_di, ten_khach_hang, trang_thai, cho_ngoi_ma_cho, khuyen_mai_ma_km, lich_trinh_tau_ma_lich)
VALUES
    ('LLT20250418192106-013-003', 'Người lớn', '417.23', '012345680', '2025-04-18', 'Nguyễn Văn C', 'DA_THANH_TOAN', 'CN10BT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-004', 'Trẻ em', '95.94', '012345681', '2025-04-18', 'Nguyễn Văn D', 'DA_THANH_TOAN', 'CN10CT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-005', 'Người lớn', '141.09', '012345682', '2025-04-18', 'Nguyễn Văn E', 'DA_THANH_TOAN', 'CN10DT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-006', 'Sinh viên', '379.07', '012345683', '2025-04-18', 'Nguyễn Văn F', 'DA_THANH_TOAN', 'CN11AT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-007', 'Trẻ em', '261.87', '012345684', '2025-04-18', 'Nguyễn Văn G', 'DA_THANH_TOAN', 'CN11BT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-008', 'Người lớn', '415.46', '012345685', '2025-04-18', 'Nguyễn Văn H', 'DA_THANH_TOAN', 'CN11CT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-009', 'Sinh viên', '404.74', '012345686', '2025-04-18', 'Nguyễn Văn I', 'DA_THANH_TOAN', 'CN11DT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-010', 'Người lớn', '205.27', '012345687', '2025-04-18', 'Nguyễn Văn J', 'DA_THANH_TOAN', 'CN12AT101', NULL, 'LLT20250418192106-013'),
    ('LLT20250418192106-013-011', 'Trẻ em', '254.76', '012345688', '2025-04-18', 'Nguyễn Văn K', 'DA_THANH_TOAN', 'CN12BT101', NULL, 'LLT20250418192106-013');

INSERT INTO nhanvien (ma_nv, ten_nv, so_dt, trang_thai, cccd, dia_chi, ngay_vao_lam, chuc_vu, avata)
VALUES
    ('NV202504180001', 'Lê Văn A', '0900123456', 'Hoạt động', 'CCCD12345', 'Hà Nội', '2025-04-01', 'Quản lý', 'avatar1.jpg'),
    ('NV202504180002', 'Trần Thị B', '0900765432', 'Hoạt động', 'CCCD67890', 'Hà Nội', '2025-04-05', 'Nhân viên bán vé', 'avatar2.jpg');
INSERT INTO loaihoadon (ma_loai_hd, ten_loai_hd)
VALUES
    ('LKH001', 'Đã đổi'),
    ('LKH002', 'Đã trả'),
    ('LKH003', 'Đã thanh toán');

INSERT INTO hoadon (ma_hd, ngay_lap, tien_giam, tong_tien, ma_khach_hang, ma_nhan_vien, ma_loai_hd)
VALUES
    ('HD202504180001', '2025-04-18 09:30:00', 50, 350000, 'KH180420250001', 'NV202504180001', 'LKH003'),
    ('HD202504180002', '2025-04-18 10:00:00', 20, 500000, 'KH180420250002', 'NV202504180002', 'LKH003');

INSERT INTO chitiet_hoadon (ma_hd, ma_ve, so_luong, VAT, thanh_tien, tien_thue)
VALUES
    ('HD202504180001', 'LLT20250418192106-013-002', 1, 10, 381.5, 38.15),
    ('HD202504180001', 'LLT20250418192106-013-003', 1, 10, 417.23, 41.72),
    ('HD202504180002', 'LLT20250418192106-013-004', 1, 10, 95.94, 9.59),
    ('HD202504180002', 'LLT20250418192106-013-005', 1, 10, 141.09, 14.11);

INSERT INTO chitiet_hoadon (ma_hd, ma_ve, so_luong, VAT, thanh_tien, tien_thue)
VALUES
    ('HD202504180001', 'LLT20250418192106-013-001', 1, 10, 220000, 22000),
    ('HD202504180001', 'LLT20250418192106-013-006', 1, 10, 379.07, 37.91),
    ('HD202504180001', 'LLT20250418192106-013-007', 1, 10, 261.87, 26.19),
    ('HD202504180001', 'LLT20250418192106-013-008', 1, 10, 415.46, 41.55),
    ('HD202504180001', 'LLT20250418192106-013-009', 1, 10, 404.74, 40.47),
    ('HD202504180001', 'LLT20250418192106-013-010', 1, 10, 205.27, 20.53),
    ('HD202504180001', 'LLT20250418192106-013-011', 1, 10, 254.76, 25.48);


select hoadon.ma_khach_hang from chitiet_hoadon
join hoadon on hoadon.ma_hd = chitiet_hoadon.ma_hd
join khachhang on khachhang.ma_khach_hang = hoadon.ma_khach_hang
where ma_ve = 'LLT20250418192106-013-004'