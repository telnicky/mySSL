package tcp.servers;

import java.lang.*;
import java.io.*;
import java.net.*;

import tcp.TcpObject;
import util.*;
import protocols.*;

abstract public class TcpServer implements TcpObject, Runnable  {
  Integer port;
  TcpServerThread thread;
  public boolean listening = false;

  public TcpServer(Integer _port) {
    port = _port;
  }

  abstract protected void acceptSocket(ServerSocket _socket);

  public void run() {
    start(port);
  }

  public void start(Integer serverPort) {
    ServerSocket serverSocket = null; 

    if(serverPort == null) {
      System.err.println("Must provide server port");
      System.exit(1);
    }

    try {
      serverSocket = new ServerSocket(serverPort);
      // Start accepting connections
      System.out.println("Listening...");
      listening = true;
      while(listening) {
        acceptSocket(serverSocket);
      }
    }
    catch(Exception e) {
      listening = false;
      Util.printException("TcpServer - start", e);
    }
  }
}

