TOP := $(shell pwd)
TCP_DIR := $(TOP)/protocols
CLIENT_DIR := $(TOP)/$(TCP_DIR)/clients
SERVER_DIR := $(TOP)/$(TCP_DIR)/servers
PROTOCOL_DIR := $(TOP)/protocols
UTIL_DIR := $(TOP)/util

install: protocol tcpClient tcpObject tcpServer util

protocol: $(PROTOCOL_DIR)/Protocol.java
	javac protocols/Protocol.java

tcpClient: $(CLIENT_DIR)/TcpClient.java
	javac clients/TcpClient.java

tcpObject: $(TCP_DIR)/TcpObject.java
	javac tcp/TcpObject.java

tcpServer: $(SERVER_DIR)/TcpServer.java
	javac tcp/servers/TcpServer.java

util: $(UTIL_DIR)/Util.java
	javac util/Util.java

