CC := gcc
CFLAGS := -std=c11 -Wall -Wextra -Wpedantic -O2 -Isrc -Isrc/cwebsocket
LDFLAGS := -lev -lssl -lcrypto -lpthread
SRC_DIR := src
CWS_DIR := $(SRC_DIR)/cwebsocket

SRCS := \
	$(SRC_DIR)/chat_server.c \
	$(CWS_DIR)/server.c \
	$(CWS_DIR)/common.c \
	$(CWS_DIR)/utf8.c

OBJS := $(SRCS:.c=.o)

all: chat-server

chat-server: $(OBJS)
	$(CC) $(OBJS) -o $@ $(LDFLAGS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -f $(OBJS) chat-server

.PHONY: all clean
