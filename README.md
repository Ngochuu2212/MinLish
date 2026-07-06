# MinLish - Ứng dụng Học Tiếng Anh Thông Minh

MinLish là một ứng dụng di động hỗ trợ học từ vựng tiếng Anh thông minh, trực quan và cá nhân hóa. Ứng dụng được xây dựng trên nền tảng Android sử dụng ngôn ngữ Kotlin và bộ công cụ xây dựng giao diện hiện đại Jetpack Compose. 

---

## 🌟 Tính năng nổi bật

### 1. Xác thực & Quản lý Người dùng
*   **Đăng ký & Đăng nhập**: Bảo mật bằng email/mật khẩu qua Firebase Authentication.
*   **Đăng nhập Google**: Hỗ trợ đăng nhập nhanh bằng tài khoản Google (`Google Sign-In`).
*   **Quản lý Hồ sơ**: Xem thông tin cá nhân, cập nhật ảnh đại diện và quản lý các thiết lập học tập.

### 2. Quản lý Bộ từ vựng Cá nhân (Vocabulary Management)
*   **Tạo bộ từ vựng**: Tạo và chỉnh sửa các bộ từ vựng theo chủ đề riêng biệt.
*   **Quản lý từ vựng**: Thêm từ mới, định nghĩa, phiên âm, từ loại và hình ảnh minh họa cho từ vựng.
*   **Danh sách trực quan**: Hiển thị danh sách bộ từ vựng và chi tiết từng từ một cách khoa học.

### 3. Phương pháp Học tập Khoa học (Learning & Flashcards)
*   **Học từ vựng**: Giao diện học tập trực quan giúp tiếp thu từ mới nhanh chóng.
*   **Thẻ ghi nhớ (Flashcards)**: Ôn tập từ vựng bằng hiệu ứng lật thẻ ghi nhớ, đánh dấu các từ đã thuộc và chưa thuộc để tối ưu hóa việc học.

### 4. Theo dõi Tiến trình (Progress Tracking)
*   **Thống kê chi tiết**: Theo dõi số từ đã học, thời gian học và tiến độ hoàn thành các bộ từ vựng.
*   **Trực quan hóa**: Biểu đồ hiển thị kết quả học tập giúp người dùng duy trì động lực hàng ngày.

### 5. Nhắc nhở Thông minh (Smart Notifications)
*   **Lập lịch nhắc nhở**: Gửi thông báo nhắc nhở học tập hàng ngày qua `WorkManager` ngay cả khi ứng dụng đã đóng.
*   **Tự động thiết lập lại**: Tự động khôi phục lịch thông báo khi điện thoại khởi động lại.

---

## 🛠️ Công nghệ Phát triển

Ứng dụng được phát triển bằng những công nghệ hiện đại nhất trong hệ sinh thái Android:

*   **Ngôn ngữ**: Kotlin (v2.1.0)
*   **Giao diện (UI)**: Jetpack Compose (BOM v2026.02.01) & Material Design 3
*   **Kiến trúc**: MVVM (Model-View-ViewModel) kết hợp phân tầng Clean/Layered Architecture
*   **Cơ sở dữ liệu Local**: Room Database (v2.7.1)
*   **Lưu trữ cấu hình**: Android Jetpack DataStore Preferences (v1.1.4)
*   **Xử lý tác vụ nền**: WorkManager (v2.10.1) & BroadcastReceiver
*   **Đăng nhập & Cloud**: Firebase Auth & Google Play Services Auth (v21.3.0)
*   **Tải hình ảnh**: Coil Compose (v2.7.0)
*   **Quản lý thư viện**: Gradle Version Catalog (`libs.versions.toml`) & Kotlin DSL (`.kts`)

---

## 📁 Cấu trúc Thư mục Mã nguồn

Mã nguồn được tổ chức sạch sẽ và rõ ràng trong package `com.example.english_app`:

```text
com.example.english_app/
├── data/
│   ├── local/              # Room Database (AppDatabase), DataStore (UserPreferences), DAOs, Entities
│   └── repository/         # Các kho dữ liệu xử lý logic từ vựng, tài khoản, tiến trình học
├── domain/                 # Các thực thể và quy tắc nghiệp vụ cốt lõi
├── notification/           # Trình quản lý thông báo (NotificationHelper, DailyReminderWorker, BootReceiver)
├── ui/
│   ├── navigation/         # Quản lý định tuyến màn hình (AppNavigation)
│   ├── screens/            # Giao diện và ViewModel của từng chức năng:
│   │   ├── auth/           # Login, Register
│   │   ├── home/           # Màn hình chính
│   │   ├── learning/       # Màn hình học, Flashcard
│   │   ├── profile/        # Quản lý hồ sơ
│   │   ├── progress/       # Tiến trình học tập
│   │   └── vocabulary/     # Thêm/sửa/chi tiết bộ từ vựng
│   └── theme/              # Định nghĩa màu sắc, phông chữ, giao diện Material 3 (Theme, Color, Type)
├── MainActivity.kt         # Entry point của ứng dụng
└── MinLishApp.kt           # Lớp khởi chạy ứng dụng (Application class)
```

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy

### Yêu cầu hệ thống
*   **Android Studio** Ladybug (hoặc mới hơn)
*   **JDK 11** hoặc **JDK 17**
*   **Android SDK API 24** (Android 7.0) trở lên

### Các bước thực hiện
1.  **Clone dự án về máy**:
    ```bash
    git clone https://github.com/Ngochuu2212/MinLish.git
    ```
2.  **Cấu hình Firebase**:
    *   Tạo một dự án Android trên [Firebase Console](https://console.firebase.google.com/).
    *   Bật tính năng **Authentication** (Email/Password và Google Sign-In).
    *   Tải tệp `google-services.json` và đặt vào thư mục `app/` của dự án.
3.  **Mở dự án trong Android Studio**:
    *   Chọn **File > Open** và dẫn đến thư mục chứa dự án.
    *   Đợi dự án đồng bộ hóa Gradle (`Sync Project with Gradle Files`).
4.  **Chạy ứng dụng**:
    *   Kết nối thiết bị Android thật (đã bật USB Debugging) hoặc khởi động máy ảo (Emulator).
    *   Nhấn nút **Run** (biểu tượng ▶️) trong Android Studio để cài đặt và trải nghiệm.
