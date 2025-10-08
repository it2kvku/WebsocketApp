package com.websocketapp.chat;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class StaticHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(StaticHttpServer.class);

    private final int port;
    private HttpServer httpServer;
    private ExecutorService executor;

    StaticHttpServer(int port) {
        this.port = port;
    }

    void start() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/", new IndexHandler());
        executor = Executors.newCachedThreadPool();
        httpServer.setExecutor(executor);
        httpServer.start();
        logger.info("HTTP demo chạy tại http://localhost:{}/", port);
    }

    void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private static class IndexHandler implements HttpHandler {

        private static final byte[] NOT_FOUND = "Not found".getBytes(StandardCharsets.UTF_8);

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (!Objects.equals("/", path) && !Objects.equals("/index.html", path)) {
                exchange.sendResponseHeaders(404, NOT_FOUND.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(NOT_FOUND);
                }
                return;
            }

            try (InputStream stream = getClass().getResourceAsStream("/web/index.html")) {
                if (stream == null) {
                    exchange.sendResponseHeaders(500, -1);
                    return;
                }
                Headers headers = exchange.getResponseHeaders();
                headers.add("Content-Type", "text/html; charset=UTF-8");
                byte[] body = stream.readAllBytes();
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        }
    }
}
