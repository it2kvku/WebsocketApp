#include "cwebsocket/server.h"

#include <signal.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#ifndef _WIN32
#include <syslog.h>
#else
#include <windows.h>
#define LOG_EMERG   0
#define LOG_ALERT   1
#define LOG_CRIT    2
#define LOG_ERR     3
#define LOG_WARNING 4
#define LOG_NOTICE  5
#define LOG_INFO    6
#define LOG_DEBUG   7
#endif

// Structure to keep track of connected clients
typedef struct chat_client {
    cwebsocket_connection *connection;
    unsigned long long id;
    char nickname[32];
    struct chat_client *next;
} chat_client;

static chat_client *clients_head = NULL;
static pthread_mutex_t clients_lock = PTHREAD_MUTEX_INITIALIZER;
static atomic_ullong next_client_id = ATOMIC_VAR_INIT(1ULL);

static void chat_log(int priority, const char *fmt, ...);
static void broadcast_message(const cwebsocket_connection *sender, const char *payload, opcode op);
static chat_client *find_client(const cwebsocket_connection *connection);
static void add_client(cwebsocket_connection *connection);
static void remove_client(const cwebsocket_connection *connection);
static void handle_text_message(chat_client *client, const char *payload);
static void send_private_message(cwebsocket_connection *connection, const char *payload);
static void chat_onopen(void *arg);
static void chat_onmessage(void *arg, cwebsocket_message *message);
static void chat_onclose(void *arg, int code, const char *message);
static void chat_onerror(void *arg, const char *error);

static cwebsocket_subprotocol chat_subprotocol = {
    .name = "cws-chat",
    .onopen = chat_onopen,
    .onmessage = chat_onmessage,
    .onclose = chat_onclose,
    .onerror = chat_onerror
};

static void handle_signal(int sig) {
    (void)sig;
    chat_log(LOG_INFO, "Shutdown requested, stopping server...");
    cwebsocket_server_shutdown();
}

static void chat_log(int priority, const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
#ifndef _WIN32
    vsyslog(priority, fmt, args);
#else
    char buffer[512];
    vsnprintf(buffer, sizeof(buffer), fmt, args);
    OutputDebugStringA(buffer);
    OutputDebugStringA("\n");
#endif
    va_end(args);
}

static chat_client *find_client(const cwebsocket_connection *connection) {
    pthread_mutex_lock(&clients_lock);
    chat_client *curr = clients_head;
    while (curr != NULL) {
        if (curr->connection == connection) {
            pthread_mutex_unlock(&clients_lock);
            return curr;
        }
        curr = curr->next;
    }
    pthread_mutex_unlock(&clients_lock);
    return NULL;
}

static void add_client(cwebsocket_connection *connection) {
    chat_client *client = calloc(1, sizeof(chat_client));
    if (!client) {
        chat_log(LOG_CRIT, "Out of memory when adding client");
        return;
    }
    client->connection = connection;
    client->id = atomic_fetch_add(&next_client_id, 1ULL);
    snprintf(client->nickname, sizeof(client->nickname), "User%llu", client->id);

    pthread_mutex_lock(&clients_lock);
    client->next = clients_head;
    clients_head = client;
    pthread_mutex_unlock(&clients_lock);
}

static void remove_client(const cwebsocket_connection *connection) {
    pthread_mutex_lock(&clients_lock);
    chat_client *prev = NULL;
    chat_client *curr = clients_head;
    while (curr != NULL) {
        if (curr->connection == connection) {
            if (prev == NULL) {
                clients_head = curr->next;
            } else {
                prev->next = curr->next;
            }
            pthread_mutex_unlock(&clients_lock);
            chat_log(LOG_INFO, "Connection %llu removed", curr->id);
            free(curr);
            return;
        }
        prev = curr;
        curr = curr->next;
    }
    pthread_mutex_unlock(&clients_lock);
}

static void broadcast_message(const cwebsocket_connection *sender, const char *payload, opcode op) {
    pthread_mutex_lock(&clients_lock);
    chat_client *curr = clients_head;
    while (curr != NULL) {
        if (sender == NULL || curr->connection != sender) {
            cwebsocket_server_write_data(curr->connection, payload, strlen(payload), op);
        }
        curr = curr->next;
    }
    pthread_mutex_unlock(&clients_lock);
}

static void send_private_message(cwebsocket_connection *connection, const char *payload) {
    cwebsocket_server_write_data(connection, payload, strlen(payload), TEXT_FRAME);
}

static void handle_text_message(chat_client *client, const char *payload) {
    if (strncmp(payload, "/name ", 6) == 0) {
        const char *new_name = payload + 6;
        while (*new_name == ' ') {
            ++new_name;
        }
        size_t len = strlen(new_name);
        while (len > 0 && (new_name[len - 1] == '\r' || new_name[len - 1] == '\n')) {
            --len;
        }
        if (len == 0) {
            send_private_message(client->connection, "Tên không được để trống.");
            return;
        }

        if (len >= sizeof(client->nickname)) {
            len = sizeof(client->nickname) - 1;
        }

        pthread_mutex_lock(&clients_lock);
        char old_name[sizeof(client->nickname)];
        snprintf(old_name, sizeof(old_name), "%s", client->nickname);
        snprintf(client->nickname, sizeof(client->nickname), "%.*s", (int)len, new_name);
        pthread_mutex_unlock(&clients_lock);

        char info[128];
        snprintf(info, sizeof(info), "%s đã đổi tên thành %s", old_name, client->nickname);
        broadcast_message(NULL, info, TEXT_FRAME);
        return;
    }

    time_t now = time(NULL);
    struct tm tm_now;
#if defined(_WIN32)
    localtime_s(&tm_now, &now);
#else
    localtime_r(&now, &tm_now);
#endif
    char timestamp[32];
    strftime(timestamp, sizeof(timestamp), "%H:%M:%S", &tm_now);

    char message[512];
    snprintf(message, sizeof(message), "[%s] %s: %s", timestamp, client->nickname, payload);
    broadcast_message(NULL, message, TEXT_FRAME);
}

static void chat_onopen(void *arg) {
    cwebsocket_connection *connection = (cwebsocket_connection *)arg;
    add_client(connection);

    chat_client *client = find_client(connection);
    if (!client) {
        chat_log(LOG_ERR, "Could not find client after adding");
        return;
    }

    char welcome[256];
    snprintf(welcome, sizeof(welcome), "Chào mừng %s đến với phòng chat!", client->nickname);
    send_private_message(connection, welcome);
    send_private_message(connection, "Gõ /name <tên mới> để đổi tên hiển thị.");

    char joined[128];
    snprintf(joined, sizeof(joined), "%s đã tham gia phòng chat.", client->nickname);
    broadcast_message(connection, joined, TEXT_FRAME);
}

static void chat_onmessage(void *arg, cwebsocket_message *message) {
    cwebsocket_connection *connection = (cwebsocket_connection *)arg;
    chat_client *client = find_client(connection);
    if (!client) {
        chat_log(LOG_ERR, "Received message from unknown connection");
        return;
    }

    if (message->opcode != TEXT_FRAME) {
        chat_log(LOG_WARNING, "Non-text frame received; ignoring");
        return;
    }

    handle_text_message(client, message->payload);
}

static void chat_onclose(void *arg, int code, const char *message) {
    cwebsocket_connection *connection = (cwebsocket_connection *)arg;
    chat_client *client = find_client(connection);
    if (client) {
        char left[128];
        snprintf(left, sizeof(left), "%s đã rời phòng chat.", client->nickname);
        remove_client(connection);
        broadcast_message(connection, left, TEXT_FRAME);
    }
    chat_log(LOG_INFO, "Connection closed (%d): %s", code, message ? message : "");
}

static void chat_onerror(void *arg, const char *error) {
    cwebsocket_connection *connection = (cwebsocket_connection *)arg;
    chat_client *client = find_client(connection);
    if (client) {
        chat_log(LOG_ERR, "Lỗi cho %s: %s", client->nickname, error);
    } else {
        chat_log(LOG_ERR, "Lỗi không xác định: %s", error);
    }
}

int main(void) {
#ifndef _WIN32
    openlog("cws-chat", LOG_PID | LOG_CONS, LOG_USER);
#endif

    signal(SIGINT, handle_signal);
    signal(SIGTERM, handle_signal);

    cwebsocket_subprotocol *protocols[] = { &chat_subprotocol };
    cwebsocket_server_init(9002, protocols, 1);
    chat_log(LOG_INFO, "Server chạy trên cổng 9002");

    if (cwebsocket_server_listen() == -1) {
        chat_log(LOG_CRIT, "Không thể khởi động server");
        return EXIT_FAILURE;
    }

#ifndef _WIN32
    closelog();
#endif
    return EXIT_SUCCESS;
}
