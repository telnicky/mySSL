package tcp.servers;

import java.lang.*;
import java.io.*;
import java.net.*;

import tcp.TcpObject;
import util.*;
import protocols.*;

public class TcpServer implements TcpObject  {
  public void start(Integer serverPort) {
    ServerSocket serverSocket = null; 
    Socket clientSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;
    boolean listening = true;

    if(serverPort == null) {
      System.err.println("Must provide server port");
      System.exit(1);
    }

    try {
      serverSocket = new ServerSocket(serverPort);
      // Start accepting connections
      while(listening) { 
      TcpServerThread t;
      }
    }
    catch(Exception e) {
      Util.printException("TcpServer - start", e);
    }
  }
}

