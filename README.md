# PhanTanJavaNhomGPT

## Giới thiệu

Đây là dự án môn học **Phân tán** thực hiện bởi nhóm GPT. Dự án mô phỏng hệ thống đặt vé tàu tại Ga Lạc Hồng, bao gồm hai thành phần chính: **Client** và **Server**. Toàn bộ source code sử dụng ngôn ngữ Java và được tổ chức rõ ràng theo từng module.

## Cấu trúc thư mục

- `Client_BanVeTauTaiGasLacHong/`: Thành phần client, giao diện người dùng để thao tác đặt vé, xem thông tin chuyến tàu, v.v.
- `Project_BanVeTauTaiGasLacHong/`: Thành phần server, xử lý logic nghiệp vụ, lưu trữ và quản lý dữ liệu đặt vé, kết nối với client.

> Lưu ý: Danh sách file/thư mục có thể chưa đầy đủ. Xem chi tiết tại [Client_BanVeTauTaiGasLacHong](https://github.com/dhViet2004/PhanTanJavaNhomGPT/tree/master/Client_BanVeTauTaiGasLacHong) và [Project_BanVeTauTaiGasLacHong](https://github.com/dhViet2004/PhanTanJavaNhomGPT/tree/master/Project_BanVeTauTaiGasLacHong).

## Công nghệ sử dụng

- Java 100%
- Maven (cấu hình thông qua file `pom.xml`)
- Kiến trúc client-server, socket hoặc RMI
- IDE đề xuất: IntelliJ IDEA

## Hướng dẫn chạy thử

1. **Clone repository:**
   ```bash
   git clone https://github.com/dhViet2004/PhanTanJavaNhomGPT.git
   ```
2. **Import vào IDE Java** (IntelliJ/Eclipse/VSCode) dưới dạng Maven Project.
3. **Build project:**
   - Mở terminal tại thư mục client/server và chạy:
     ```bash
     mvn clean install
     ```
4. **Chạy server:**  
   Vào thư mục `Project_BanVeTauTaiGasLacHong`, chạy class chứa hàm `main` khởi tạo server.
5. **Chạy client:**  
   Vào thư mục `Client_BanVeTauTaiGasLacHong`, chạy class chứa hàm `main` khởi tạo client.
6. **Sử dụng ứng dụng:**  
   Thực hiện đặt vé, tra cứu, ... thông qua giao diện client.

## Đóng góp

- Mọi đóng góp, issue hoặc pull request đều được hoan nghênh!

## Liên hệ

- Thành viên nhóm GPT  
- Email: dviet037@gmail.com

---

*README này được tạo tự động. Vui lòng cập nhật thông tin chi tiết về kiến trúc, chức năng hoặc hướng dẫn sử dụng nếu cần.*
