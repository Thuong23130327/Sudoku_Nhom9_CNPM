# 🎮 SUDOKU JAVA PROJECT - [TÊN NHÓM CỦA BẠN]
> **Dự án môn học:** Nhập môn Công nghệ Phần mềm (SE) - Đại học Nông Lâm TP.HCM.
> **Mục tiêu:** Xây dựng ứng dụng Sudoku hoàn chỉnh trên Java Desktop theo quy trình Công nghệ phần mềm chuẩn.

---

## 👥 1. Đội ngũ phát triển & Phân công công việc (Traceability Matrix)

Hệ thống được chia thành 5 cụm chức năng chính. Mỗi thành viên chịu trách nhiệm xuyên suốt từ tài liệu (Requirements/Use Case) đến mã nguồn (Source code).

| MSSV | Họ và Tên | Vai trò | Use Case Đảm nhận | Requirement Ánh xạ |
| :--- | :--- | :--- | :--- | :--- |
| [MSSV1] | [Tên Trưởng Nhóm] | Team Leader | **[UC-03] Kiểm tra & Giải thuật** | UR-3.1 -> UR-3.4 |
| [MSSV2] | [Tên Bạn B] | Developer | **[UC-01] Quản lý vòng đời game** | UR-1.1 -> UR-1.5 |
| [MSSV3] | [Tên Bạn C] | Developer | **[UC-02] Tương tác bàn cờ** | UR-2.1 -> UR-2.5 |
| [MSSV4] | [Tên Bạn D] | Developer | **[UC-04] Hệ thống trợ giúp** | UR-4.1 -> UR-4.4 |
| [MSSV5] | [Tên Bạn E] | Developer | **[UC-05] Trạng thái & Thống kê** | UR-5.1 -> UR-5.5 |

---

## 🛠 2. Công nghệ & Kiến trúc
*   **Ngôn ngữ:** Java (JDK 17+)
*   **Giao diện:** Java Swing / JavaFX.
*   **Kiến trúc:** Model-View-Controller (MVC) đảm bảo tính độc lập khi code song song.
*   **Quản lý mã nguồn:** Git/GitHub theo mô hình Feature Branching.

---

## 📋 3. Danh mục Yêu cầu Người dùng (UR)
*Tuân thủ chuẩn đánh số phân cấp để kiểm soát mã nguồn.*

### [UC-01] Quản lý vòng đời ván đấu (Lifecycle)
*   **UR-1.1:** Lựa chọn 3 cấp độ khó (Easy, Medium, Hard).
*   **UR-1.2:** Sinh ma trận Sudoku $9 \times 9$ hợp lệ và duy nhất.
*   **UR-1.3:** Chức năng "Làm mới" (Reset) bàn chơi.
*   **UR-1.4:** Tự động lưu (Auto-save) khi thoát ứng dụng.

### [UC-02] Tương tác bàn cờ (Interaction)
*   **UR-2.1:** Chọn ô bằng chuột/bàn phím.
*   **UR-2.2:** Nhập số từ 1-9 và xóa số.
*   **UR-2.3:** Khóa các ô số mặc định của đề bài.

### [UC-03] Kiểm tra logic & Giải thuật (Logic)
*   **UR-3.1:** Kiểm tra tính hợp lệ tức thời (Hàng, Cột, Khối).
*   **UR-3.2:** Chức năng "Kiểm tra toàn bảng".
*   **UR-3.3:** Giải thuật tự động hoàn thành bảng (Backtracking).

### [UC-04] Hệ thống trợ giúp (Support)
*   **UR-4.1:** Gợi ý (Hint) số đúng cho ô đang chọn.
*   **UR-4.2:** Highlight các ô cùng giá trị và ô bị lỗi.

### [UC-05] Trạng thái & Thống kê (Monitor)
*   **UR-5.1:** Bộ đếm thời gian (Timer) & Tạm dừng (Pause).
*   **UR-5.2:** Thống kê số lỗi và thông báo kết thúc (Thắng/Thua).

---

## 🚀 4. Quy ước Code & Git (Team Rules)

### 💻 Quy ước viết mã (Coding Convention)
*   **Comment Header:** Mọi hàm phải có mã UR tương ứng để thầy dễ kiểm tra.
    > `// [UR-3.1] Hàm kiểm tra trùng số trên hàng ngang`
*   **Đặt tên:** `CamelCase` cho biến/hàm, `PascalCase` cho Class.

### 🌿 Quy trình Git (Workflow)
1.  **Không code trực tiếp trên nhánh `main`.**
2.  Mỗi người tạo nhánh riêng: `feature/UC0x-[Tên]`.
3.  Trước khi đẩy code, thực hiện: `git pull origin main` để cập nhật code mới nhất của đồng đội.
4.  Gửi **Pull Request (PR)** để Leader duyệt trước khi gộp vào bản chính.

---

## 📥 5. Hướng dẫn cài đặt
1.  Clone project: `git clone https://github.com/[username]/[repo-name].git`
2.  Mở project bằng IntelliJ IDEA hoặc Eclipse.
3.  Cấu hình JDK 17 hoặc mới hơn.
4.  Run file `src/main/java/com/sudoku/Main.java`.

---
*© 2026 - Nhóm [Tên Nhóm] - ĐH Nông Lâm TP.HCM*