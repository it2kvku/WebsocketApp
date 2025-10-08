# Ứng dụng chat realtime với C và cWebSocket

Ứng dụng mẫu này xây dựng một máy chủ WebSocket nhỏ bằng ngôn ngữ C dựa trên thư viện **cWebSocket** (MIT License). Máy chủ duy trì danh sách kết nối, phát tin nhắn theo thời gian thực tới toàn bộ người dùng, hỗ trợ đổi tên hiển thị và đi kèm một trang web tối giản để thử nghiệm.

## Thư mục dự án

```
.
├── Makefile                 # Biên dịch nhị phân `chat-server`
├── README.md                # Tài liệu hướng dẫn
├── src/
│   ├── chat_server.c        # Logic phòng chat và tích hợp cWebSocket
│   └── cwebsocket/          # Mã nguồn thư viện cWebSocket cần thiết
└── web/
    └── index.html           # Client HTML/JS kết nối tới máy chủ
```

## Yêu cầu hệ thống

- GCC/Clang hỗ trợ C11
- Thư viện [libev](http://software.schmorp.de/pkg/libev.html)
- OpenSSL (`libssl`, `libcrypto`)
- POSIX threads (`pthread`)

> 💡 Nếu đang dùng Windows, hãy chạy dự án trong WSL2 hoặc môi trường Linux tương thích (Ubuntu, Debian, v.v.).

Cài đặt phụ thuộc (ví dụ Ubuntu/Debian):

```bash
sudo apt update
sudo apt install build-essential libev-dev libssl-dev
```

## Biên dịch

Trong thư mục gốc dự án:

```bash
make
```

Lệnh trên tạo ra nhị phân `chat-server`.

Để dọn dẹp file biên dịch:

```bash
make clean
```

## Chạy máy chủ

```bash
./chat-server
```

Máy chủ lắng nghe tại cổng `9002` với sub-protocol `cws-chat`. Khi cần dừng, nhấn `Ctrl+C`.

## Thử nghiệm bằng client web

1. Mở file `web/index.html` trên trình duyệt (chạy local bằng `file://` hoặc thông qua máy chủ tĩnh).
2. Đảm bảo trình duyệt có thể kết nối tới `ws://localhost:9002`.
3. Nhập tin nhắn và gửi; thử lệnh `/name <tên>` để đổi biệt danh.

Bạn cũng có thể kết nối bằng công cụ dòng lệnh (ví dụ `websocat`):

```bash
websocat ws://localhost:9002 -H "Sec-WebSocket-Protocol: cws-chat"
```

## Ghi chú quan trọng

- Thư viện cWebSocket được giữ nguyên giấy phép MIT, xem phần header trong từng file nguồn.
- Mã nguồn hiện ưu tiên chạy trên môi trường Unix-like. Nếu cần chạy thuần Windows (WinSock), hãy cân nhắc port hoặc dùng WSL.
- Để triển khai thực tế bạn nên bổ sung xác thực, giới hạn kích thước thông điệp và logging mạnh hơn.

## Hướng phát triển thêm

- Thêm lưu trữ lịch sử tin nhắn (SQLite, tệp, v.v.).
- Viết client dòng lệnh bằng C sử dụng phần client của cWebSocket.
- Tích hợp chứng chỉ TLS để phục vụ `wss://` bảo mật.
