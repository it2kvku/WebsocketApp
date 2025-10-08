package com.websocketapp.chat;package com.websocketapp.chat;



import org.glassfish.tyrus.server.Server;import jakarta.websocket.DeploymentException;

import org.slf4j.Logger;import org.glassfish.tyrus.server.Server;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;

import java.io.IOException;import java.io.IOException;

import java.util.Scanner;import java.io.InputStreamReader;



public final class ChatServer {public final class ChatServer {



    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);    private ChatServer() {

    }

    private ChatServer() {

    }    public static void main(String[] args) {

        int port = parsePort(args);

    public static void main(String[] args) throws Exception {        Server server = new Server("0.0.0.0", port, "/ws", null, ChatEndpoint.class);

        int wsPort = integerProperty("chat.ws.port", "CHAT_WS_PORT", 8080);

        int httpPort = integerProperty("chat.http.port", "CHAT_HTTP_PORT", 8081);        try {

            server.start();

        Server websocketServer = new Server("0.0.0.0", wsPort, "/ws", ChatEndpoint.class);            System.out.printf("Chat server đang chạy tại ws://localhost:%d/ws/chat%n", port);

        StaticHttpServer httpServer = new StaticHttpServer(httpPort);            System.out.println("Nhấn Enter để dừng server...");

            waitForShutdown();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {        } catch (DeploymentException e) {

            logger.info("Đang tắt máy chủ...");            System.err.println("Không thể khởi chạy server: " + e.getMessage());

            try {        } finally {

                websocketServer.stop();            server.stop();

            } catch (Exception e) {            System.out.println("Server đã dừng.");

                logger.warn("Không thể dừng WebSocket server", e);        }

            }    }

            httpServer.stop();

        }));    private static void waitForShutdown() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

        try {            reader.readLine();

            websocketServer.start();        } catch (IOException ignored) {

            httpServer.start();        }

            logger.info("WebSocket sẵn sàng tại ws://localhost:{}/ws/chat", wsPort);    }

            logger.info("Mở http://localhost:{}/ để thử nghiệm giao diện web", httpPort);

            blockUntilExit();    private static int parsePort(String[] args) {

        } catch (Exception e) {        if (args.length == 0) {

            logger.error("Không thể khởi động máy chủ", e);            return 9002;

            websocketServer.stop();        }

            httpServer.stop();        try {

            throw e;            int port = Integer.parseInt(args[0]);

        }            if (port < 1 || port > 65535) {

    }                throw new IllegalArgumentException("Cổng phải nằm trong khoảng 1-65535.");

            }

    private static void blockUntilExit() throws IOException {            return port;

        logger.info("Nhấn ENTER để dừng server...");        } catch (NumberFormatException ex) {

        try (Scanner scanner = new Scanner(System.in)) {            System.err.println("Cổng không hợp lệ, dùng mặc định 9002.");

            scanner.nextLine();            return 9002;

        }        } catch (IllegalArgumentException ex) {

    }            System.err.println(ex.getMessage() + " Dùng mặc định 9002.");

            return 9002;

    private static int integerProperty(String systemProperty, String envVariable, int fallback) {        }

        String value = System.getProperty(systemProperty, System.getenv(envVariable));    }

        if (value == null || value.isBlank()) {}

            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            logger.warn("Giá trị không hợp lệ cho {} / {}: {}. Dùng mặc định {}", systemProperty, envVariable, value, fallback);
            return fallback;
        }
    }
}
