# Hướng dẫn cấu hình Firebase Google Sign-In

## Bước 1: Tạo dự án Firebase

1. Truy cập [https://console.firebase.google.com/](https://console.firebase.google.com/)
2. Click **"Add project"** → đặt tên project → nhấn Continue
3. Bật / tắt Google Analytics tuỳ ý → nhấn **Create project**

## Bước 2: Thêm ứng dụng Android

1. Trong Firebase Console, click biểu tượng **Android** (➕ Add app)
2. **Android package name**: `com.example.english_app`
3. **App nickname**: MinLish (tuỳ chọn)
4. **Debug signing certificate SHA-1**: lấy bằng lệnh dưới đây

```powershell
# Chạy lệnh này trong PowerShell để lấy SHA-1 của debug keystore
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

Sao chép dòng **SHA1:** và dán vào Firebase Console.

5. Click **Register app**

## Bước 3: Tải và thay thế google-services.json

1. Click **Download google-services.json**
2. Thay thế file `app/google-services.json` hiện tại bằng file vừa tải về

## Bước 4: Bật Google Sign-In trong Firebase Auth

1. Trong Firebase Console → **Authentication** → **Sign-in method**
2. Click **Google** → bật toggle **Enable**
3. Chọn **Project support email** (dùng email Google của bạn)
4. Click **Save**

## Bước 5: Build lại ứng dụng

Sau khi thay `google-services.json`, plugin `google-services` sẽ tự động sinh ra
`default_web_client_id` đúng giá trị. **Không cần** khai báo thủ công trong `strings.xml`.

```powershell
cd C:\HOC\Lap_Trinh_Di_Dong\English_App
.\gradlew clean assembleDebug
```

## Lưu ý

- File `google-services.json` chứa thông tin project Firebase (không phải secret key),
  nhưng tốt nhất **không nên commit** file này lên git public.
- Thêm vào `.gitignore`:
  ```
  app/google-services.json
  ```

