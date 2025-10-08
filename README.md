# Java WebSocket Chat# Java WebSocket Chat



Ứng dụng chat realtime nhỏ viết bằng Java (JDK 17) sử dụng **Jakarta WebSocket API** (Tyrus) và một HTTP server tích hợp để phục vụ trang web demo.Một ứng dụng chat realtime viết bằng Java, sử dụng [Tyrus](https://tyrus-project.github.io/) – hiện thực tham chiếu của Jakarta WebSocket.



## Tính năng## Kiến trúc

- Máy chủ WebSocket broadcast tin nhắn tới mọi phiên (đổi tên bằng lệnh `/name`).

- HTTP server nhẹ (dựa trên `com.sun.net.httpserver.HttpServer`) phục vụ file tĩnh `index.html`.- **Server:** `ChatServer` khởi chạy Tyrus trên cổng mặc định `9002` với endpoint `/ws/chat`.

- Đóng gói thành một fat-jar thông qua Maven Shade Plugin.- **Endpoint:** `ChatEndpoint` quản lý người dùng, phát tin nhắn, xử lý lệnh `/name` và sự kiện join/leave.

- **Client:** `web/index.html` là trang web tối giản kết nối tới server bằng WebSocket.

## Yêu cầu

- Java 17+ (JDK).## Yêu cầu

- Maven 3.8+.

- Java 17 trở lên (OpenJDK hoặc Oracle JDK).

## Cài đặt & chạy- Apache Maven (hoặc dùng trình quản lý gói của bạn để cài đặt). Trên Windows có thể `winget install Apache.Maven`.

```bash

mvn clean package## Cài đặt & build

java -jar target/websocket-chat-1.0.0.jar

``````bash

mvn clean package

Máy chủ chạy với mặc định:```

- WebSocket: `ws://localhost:8080/ws/chat`

- Trang web tĩnh: `http://localhost:8081/`Lệnh trên tạo file thực thi giàu phụ thuộc `target/websocket-chat-1.0.0-shaded.jar`.



> Có thể cấu hình lại bằng biến môi trường hoặc system property:## Chạy server

> - `CHAT_WS_PORT` (env) hoặc `-Dchat.ws.port=...`

> - `CHAT_HTTP_PORT` (env) hoặc `-Dchat.http.port=...````bash

java -jar target/websocket-chat-1.0.0-shaded.jar [port]

## Thử nghiệm```

1. Sau khi chạy jar, mở trình duyệt tới `http://localhost:8081/`.

2. Gửi tin nhắn, dùng `/name <tên>` để đổi nickname.- Tham số `port` là tùy chọn, mặc định `9002`.

- Nhấn Enter trong terminal để dừng server.

## Cấu trúc thư mục

```## Thử nghiệm nhanh

.

├── pom.xml1. Giữ server chạy.

├── README.md2. Mở file `web/index.html` trong trình duyệt.

├── src3. Gửi tin nhắn, hoặc gõ `/name <tên>` để đổi nickname.

│   ├── main

│   │   ├── java/com/websocketapp/chatBạn cũng có thể kiểm tra bằng `websocat` hay bất kỳ client WebSocket nào:

│   │   │   ├── ChatEndpoint.java

│   │   │   ├── ChatServer.java```bash

│   │   │   └── StaticHttpServer.javawebsocat ws://localhost:9002/ws/chat

│   │   └── resources/web/index.html```

└── target/ (được tạo sau khi build)

```## Ghi chú



## Giấy phép- Dự án sử dụng `slf4j-simple` để hiển thị log đơn giản trên console.

Dự án mẫu dùng cho mục đích học tập; bạn có thể tuỳ ý mở rộng.- Nếu muốn triển khai thật, bạn nên cấu hình lại logging, xác thực người dùng và thêm cơ chế persistence.

