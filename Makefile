TOP := $(shell pwd)
TCP_DIR := $(TOP)/tcp
AUTHENTICATION_DIR := $(TOP)/authentication
CLIENT_DIR := $(TCP_DIR)/clients
SERVER_DIR := $(TCP_DIR)/servers
PROTOCOL_DIR := $(TOP)/protocols
UTIL_DIR := $(TOP)/util

install: alice authenticationManager bob bobThread protocol sslClientProtocol tcpClient tcpObject tcpServer tcpServerThread tcpThreadObject util

alice: $(CLIENT_DIR)/Alice.java
	javac $(CLIENT_DIR)/Alice.java

authenticationManager: $(AUTHENTICATION_DIR)/AuthenticationManager.java
	javac $(AUTHENTICATION_DIR)/AuthenticationManager.java

bob: $(SERVER_DIR)/Bob.java
	javac $(SERVER_DIR)/Bob.java

bobThread: $(SERVER_DIR)/BobThread.java
	javac $(SERVER_DIR)/BobThread.java

protocol: $(PROTOCOL_DIR)/Protocol.java
	javac $(PROTOCOL_DIR)/Protocol.java

sslClientProtocol: $(PROTOCOL_DIR)/SslClientProtocol.java
	javac $(PROTOCOL_DIR)/SslClientProtocol.java

sslServerProtocol: $(PROTOCOL_DIR)/SslServerProtocol.java
	javac $(PROTOCOL_DIR)/SslServerProtocol.java

tcpClient: $(CLIENT_DIR)/TcpClient.java
	javac $(CLIENT_DIR)/TcpClient.java

tcpObject: $(TCP_DIR)/TcpObject.java
	javac $(TCP_DIR)/TcpObject.java

tcpServer: $(SERVER_DIR)/TcpServer.java
	javac $(SERVER_DIR)/TcpServer.java

tcpServerThread: $(SERVER_DIR)/TcpServerThread.java
	javac $(SERVER_DIR)/TcpServerThread.java

tcpThreadObject: $(TCP_DIR)/TcpThreadObject.java
	javac $(TCP_DIR)/TcpThreadObject.java

util: $(UTIL_DIR)/Util.java
	javac $(UTIL_DIR)/Util.java

