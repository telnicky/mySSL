TOP := $(shell pwd)
TCP_DIR := $(TOP)/tcp
CLIENT_DIR := $(TCP_DIR)/clients
SERVER_DIR := $(TCP_DIR)/servers
PROTOCOL_DIR := $(TOP)/protocols
UTIL_DIR := $(TOP)/util

install: protocol tcpClient tcpObject tcpServer tcpServerThread util

protocol: $(PROTOCOL_DIR)/Protocol.java
	javac protocols/Protocol.java

tcpClient: $(CLIENT_DIR)/TcpClient.java
	javac $(CLIENT_DIR)/TcpClient.java

tcpObject: $(TCP_DIR)/TcpObject.java
	javac $(TCP_DIR)/TcpObject.java

tcpServer: $(SERVER_DIR)/TcpServer.java
	javac $(SERVER_DIR)/TcpServer.java

tcpServerThread: $(SERVER_DIR)/TcpServerThread.java
	javac $(SERVER_DIR)/TcpServerThread.java

util: $(UTIL_DIR)/Util.java
	javac $(UTIL_DIR)/Util.java

