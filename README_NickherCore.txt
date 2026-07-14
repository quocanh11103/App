NICKHERCORE - ỨNG DỤNG XEM TỶ SỐ BÓNG ĐÁ

1. Mô tả đề tài
NickherCore là ứng dụng Android mô phỏng app xem tỷ số bóng đá như Flashscore/Sofascore ở mức cơ bản.
Ứng dụng dùng dữ liệu lưu trong SQLite local, không cần Internet và không cần API bên ngoài.

2. Chức năng chính
- Hiển thị danh sách trận đấu.
- Xem giải đấu, đội nhà, đội khách, tỷ số, thời gian và trạng thái trận.
- Xem chi tiết từng trận đấu.
- Thêm trận đấu mới.
- Khôi phục dữ liệu mẫu.
- Lưu dữ liệu bằng SQLite.

3. Các thành phần đúng yêu cầu môn học
- Class object: Match.java
- Database SQLite: NickherDbHelper.java
- Data source: MatchDataSource.java
- Adapter hiển thị dữ liệu: MatchAdapter.java
- Giao diện chính: MainActivity.java + activity_main.xml
- Giao diện thêm trận: AddMatchActivity.java + activity_add_match.xml
- Giao diện chi tiết: MatchDetailActivity.java + activity_detail.xml

4. Cấu trúc database
Tên database: nickhercore.db
Tên bảng: matches

Câu lệnh tạo bảng:
CREATE TABLE matches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    league TEXT NOT NULL,
    home_team TEXT NOT NULL,
    away_team TEXT NOT NULL,
    home_score INTEGER DEFAULT 0,
    away_score INTEGER DEFAULT 0,
    match_time TEXT,
    status TEXT
);

5. Cách mở trong Android Studio
Bước 1: Giải nén file NickherCore.zip.
Bước 2: Mở Android Studio.
Bước 3: Chọn File > Open.
Bước 4: Chọn thư mục NickherCore.
Bước 5: Chờ Gradle Sync xong.
Bước 6: Bấm Run để chạy app trên máy ảo hoặc điện thoại Android.

6. Nếu lỗi compileSdk
Nếu Android Studio báo chưa có SDK 35:
- Mở File > Project Structure > Modules > app.
- Đổi compileSdk và targetSdk về SDK đang có trong máy, ví dụ 34.
Hoặc vào SDK Manager để cài Android SDK 35.

7. Cách thuyết trình ngắn
Ứng dụng NickherCore được xây dựng bằng Android Studio và Java. Dữ liệu trận đấu được lưu bằng SQLite trong bảng matches. Lớp Match đại diện cho đối tượng trận đấu. Lớp NickherDbHelper tạo database và bảng dữ liệu. Lớp MatchDataSource đóng vai trò trung gian để đọc, thêm và khôi phục dữ liệu từ SQLite. Dữ liệu sau khi lấy ra được đưa vào MatchAdapter để hiển thị lên ListView trong MainActivity.

8. Lưu ý
Đây là app mô phỏng dữ liệu tỷ số, không phải app live score thật. Nếu muốn live score thật cần dùng API thể thao hoặc backend riêng.
