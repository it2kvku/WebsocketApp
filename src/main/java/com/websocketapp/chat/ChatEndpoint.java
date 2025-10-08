package com.websocketapp.chat;package com.websocketapp.chat;package com.websocketapp.chat;



import jakarta.websocket.CloseReason;

import jakarta.websocket.OnClose;

import jakarta.websocket.OnError;import jakarta.websocket.OnClose;import jakarta.websocket.CloseReason;

import jakarta.websocket.OnMessage;

import jakarta.websocket.OnOpen;import jakarta.websocket.OnError;import jakarta.websocket.OnClose;

import jakarta.websocket.Session;

import jakarta.websocket.server.ServerEndpoint;import jakarta.websocket.OnMessage;import jakarta.websocket.OnError;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;import jakarta.websocket.OnOpen;import jakarta.websocket.OnMessage;



import java.io.IOException;import jakarta.websocket.Session;import jakarta.websocket.OnOpen;

import java.time.LocalTime;

import java.time.format.DateTimeFormatter;import jakarta.websocket.server.ServerEndpoint;import jakarta.websocket.Session;

import java.util.Map;

import java.util.Set;import org.slf4j.Logger;import jakarta.websocket.server.ServerEndpoint;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.atomic.AtomicLong;import org.slf4j.LoggerFactory;



@ServerEndpoint("/chat")import java.io.IOException;

public class ChatEndpoint {

import java.io.IOException;import java.time.LocalTime;

    private static final Logger logger = LoggerFactory.getLogger(ChatEndpoint.class);

    private static final Set<Session> ACTIVE_SESSIONS = ConcurrentHashMap.newKeySet();import java.time.Instant;import java.time.format.DateTimeFormatter;

    private static final AtomicLong USER_SEQUENCE = new AtomicLong(1);

    private static final String NICKNAME_KEY = "nickname";import java.time.format.DateTimeFormatter;import java.util.Map;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

import java.util.Map;import java.util.Set;

    @OnOpen

    public void onOpen(Session session) {import java.util.Set;import java.util.concurrent.ConcurrentHashMap;

        ACTIVE_SESSIONS.add(session);

        String nickname = "User" + USER_SEQUENCE.getAndIncrement();import java.util.concurrent.ConcurrentHashMap;import java.util.concurrent.atomic.AtomicLong;

        session.getUserProperties().put(NICKNAME_KEY, nickname);

import java.util.concurrent.atomic.AtomicLong;

        send(session, "Chào mừng " + nickname + " đến với phòng chat!");

        send(session, "Gõ /name <tên mới> để đổi tên hiển thị.");@ServerEndpoint(value = "/chat")

        broadcastExcept(session, nickname + " đã tham gia phòng chat.");

        logger.info("Session {} connected as {}", session.getId(), nickname);@ServerEndpoint("/chat")public class ChatEndpoint {

    }

public class ChatEndpoint {

    @OnMessage

    public void onMessage(Session session, String message) {    private static final Logger logger = LoggerFactory.getLogger(ChatEndpoint.class);    private static final Set<Session> ACTIVE_SESSIONS = ConcurrentHashMap.newKeySet();

        if (message == null) {

            return;    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();    private static final Map<String, String> NICKNAMES = new ConcurrentHashMap<>();

        }

        String trimmed = message.trim();    private static final AtomicLong nextId = new AtomicLong(1);    private static final AtomicLong USER_SEQUENCE = new AtomicLong(1);

        if (trimmed.isEmpty()) {

            return;    private static final String NICKNAME_KEY = "nickname";    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

        }



        if (trimmed.startsWith("/name")) {

            handleRename(session, trimmed);    @OnOpen    @OnOpen

            return;

        }    public void onOpen(Session session) {    public void onOpen(Session session) {



        String nickname = nickname(session);        sessions.add(session);        ACTIVE_SESSIONS.add(session);

        String payload = String.format("[%s] %s: %s",

                TIME_FORMAT.format(LocalTime.now()), nickname, trimmed);        String nickname = "User" + nextId.getAndIncrement();        String nickname = "User" + USER_SEQUENCE.getAndIncrement();

        broadcast(payload);

    }        session.getUserProperties().put(NICKNAME_KEY, nickname);        NICKNAMES.put(session.getId(), nickname);



    @OnClose

    public void onClose(Session session, CloseReason reason) {

        ACTIVE_SESSIONS.remove(session);        send(session, "Chào mừng " + nickname + "! Gõ /name <tên> để đổi tên.");        send(session, "Chào mừng " + nickname + " đến với phòng chat!");

        String nickname = nickname(session);

        broadcastExcept(session, nickname + " đã rời phòng chat.");        broadcastExcluding(session, nickname + " đã tham gia phòng chat.");        send(session, "Gõ /name <tên mới> để đổi tên hiển thị.");

        logger.info("Session {} disconnected ({})", session.getId(), reason);

    }        logger.info("Session {} connected as {}", session.getId(), nickname);        broadcastExcept(session, nickname + " đã tham gia phòng chat.");



    @OnError    }    }

    public void onError(Session session, Throwable throwable) {

        String id = session != null ? session.getId() : "unknown";

        logger.error("Lỗi trên session {}", id, throwable);

    }    @OnMessage    @OnMessage



    private void handleRename(Session session, String command) {    public void onMessage(Session session, String message) {    public void onMessage(Session session, String message) {

        String[] parts = command.split("\\s+", 2);

        if (parts.length < 2) {        String trimmed = message == null ? "" : message.trim();        if (message == null) {

            send(session, "Cú pháp: /name <tên mới>");

            return;        if (trimmed.isEmpty()) {            return;

        }

            return;        }

        String desired = parts[1].trim();

        if (desired.isEmpty()) {        }        String trimmed = message.trim();

            send(session, "Tên không được để trống.");

            return;        if (trimmed.isEmpty()) {

        }

        if (desired.length() > 24) {        if (trimmed.startsWith("/name")) {            send(session, "Tin nhắn trống không được chấp nhận.");

            send(session, "Tên quá dài (tối đa 24 ký tự).");

            return;            handleRename(session, trimmed);            return;

        }

        } else {        }

        String previous = nickname(session);

        session.getUserProperties().put(NICKNAME_KEY, desired);            String nickname = nickname(session);

        send(session, "Bạn đã đổi tên thành " + desired + ".");

        broadcastExcept(session, previous + " đã đổi tên thành " + desired + ".");            String payload = String.format("[%s] %s: %s", DateTimeFormatter.ISO_LOCAL_TIME.format(Instant.now()), nickname, trimmed);        if (trimmed.startsWith("/name")) {

    }

            broadcast(payload);            handleRenameCommand(session, trimmed);

    private void broadcast(String message) {

        for (Session session : ACTIVE_SESSIONS) {        }            return;

            send(session, message);

        }    }        }

    }



    private void broadcastExcept(Session excluded, String message) {

        for (Session session : ACTIVE_SESSIONS) {    @OnClose        String nickname = nicknameOf(session);

            if (!session.equals(excluded)) {

                send(session, message);    public void onClose(Session session) {        String timestamp = LocalTime.now().format(TIME_FORMAT);

            }

        }        sessions.remove(session);        broadcast(String.format("[%s] %s: %s", timestamp, nickname, trimmed));

    }

        String nickname = nickname(session);    }

    private void send(Session session, String message) {

        if (session == null || !session.isOpen()) {        broadcast(nickname + " đã rời phòng chat.");

            return;

        }        logger.info("Session {} disconnected", session.getId());    @OnClose

        try {

            session.getBasicRemote().sendText(message);    }    public void onClose(Session session, CloseReason reason) {

        } catch (IOException e) {

            logger.warn("Không thể gửi tin nhắn tới session {}", session.getId(), e);        ACTIVE_SESSIONS.remove(session);

        }

    }    @OnError        String nickname = NICKNAMES.remove(session.getId());



    private String nickname(Session session) {    public void onError(Session session, Throwable throwable) {        if (nickname != null) {

        Map<String, Object> props = session.getUserProperties();

        Object value = props.get(NICKNAME_KEY);        String id = session != null ? session.getId() : "unknown";            broadcast(String.format("%s đã rời phòng chat. (mã: %s)", nickname, reason.getCloseCode()));

        return value != null ? value.toString() : "Người lạ";

    }        logger.error("Lỗi trên session {}", id, throwable);        }

}

    }    }



    private void handleRename(Session session, String command) {    @OnError

        String[] parts = command.split("\\s+", 2);    public void onError(Session session, Throwable throwable) {

        if (parts.length < 2) {        String nickname = session != null ? nicknameOf(session) : "Không xác định";

            send(session, "Cú pháp: /name <tên mới>");        System.err.printf("Lỗi cho %s: %s%n", nickname, throwable.getMessage());

            return;    }

        }

        String desired = parts[1].trim();    private void handleRenameCommand(Session session, String command) {

        if (desired.isEmpty()) {        String[] parts = command.split("\\s+", 2);

            send(session, "Tên không được để trống.");        if (parts.length < 2) {

            return;            send(session, "Cú pháp: /name <tên mới>");

        }            return;

        if (desired.length() > 24) {        }

            send(session, "Tên quá dài (tối đa 24 ký tự).");        String desired = parts[1].trim();

            return;        if (desired.isEmpty()) {

        }            send(session, "Tên không được để trống.");

        String old = nickname(session);            return;

        session.getUserProperties().put(NICKNAME_KEY, desired);        }

        send(session, "Bạn đã đổi tên thành " + desired + ".");        if (desired.length() > 24) {

        broadcastExcluding(session, old + " đã đổi tên thành " + desired + ".");            send(session, "Tên quá dài (tối đa 24 ký tự).");

    }            return;

        }

    private void broadcast(String message) {

        for (Session session : sessions) {        String previous = nicknameOf(session);

            send(session, message);        NICKNAMES.put(session.getId(), desired);

        }        send(session, "Bạn đã đổi tên thành " + desired + ".");

    }        broadcastExcept(session, previous + " đã đổi tên thành " + desired + ".");

    }

    private void broadcastExcluding(Session excluded, String message) {

        for (Session session : sessions) {    private void broadcast(String payload) {

            if (!session.equals(excluded)) {        for (Session client : ACTIVE_SESSIONS) {

                send(session, message);            send(client, payload);

            }        }

        }    }

    }

    private void broadcastExcept(Session ignored, String payload) {

    private void send(Session session, String message) {        for (Session client : ACTIVE_SESSIONS) {

        if (session == null || !session.isOpen()) {            if (!client.equals(ignored)) {

            return;                send(client, payload);

        }            }

        try {        }

            session.getBasicRemote().sendText(message);    }

        } catch (IOException e) {

            logger.warn("Không thể gửi tin nhắn tới session {}", session.getId(), e);    private void send(Session session, String payload) {

        }        if (session == null || !session.isOpen()) {

    }            return;

        }

    private String nickname(Session session) {        try {

        Map<String, Object> props = session.getUserProperties();            session.getBasicRemote().sendText(payload);

        Object value = props.get(NICKNAME_KEY);        } catch (IOException e) {

        return value != null ? value.toString() : "Người lạ";            System.err.printf("Không thể gửi tin nhắn tới %s: %s%n", nicknameOf(session), e.getMessage());

    }        }

}    }


    private String nicknameOf(Session session) {
        return NICKNAMES.getOrDefault(session.getId(), "Ẩn danh");
    }
}
