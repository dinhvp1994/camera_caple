DVR Plus - app xem/ghi camera USB (UVC) có: chỉnh sáng/tương phản/bão hòa/gamma/gain và LẬT HÌNH.

Build:
1. Cài Android Studio (Hedgehog trở lên), chọn File > Open > thư mục DVRPlus.
2. Đợi Gradle sync (cần mạng, dùng jitpack.io).
3. Build > Build APK(s) -> app/build/outputs/apk/debug/app-debug.apk
Lưu ý: nếu sync báo lỗi API của thư viện libausbc, gửi nguyên lỗi cho mình để sửa.

Build không cần cài gì (GitHub Actions):
1. Tạo repo mới trên github.com, upload TOÀN BỘ nội dung thư mục này (kể cả thư mục .github).
2. Vào tab Actions > "Build APK" > đợi chạy xong (~5 phút).
3. Mở lần chạy đó, tải mục "DVRPlus-apk" ở cuối trang -> giải nén ra app-debug.apk.
4. Chép vào đầu màn hình/điện thoại, cho phép "cài từ nguồn không rõ" rồi cài.
