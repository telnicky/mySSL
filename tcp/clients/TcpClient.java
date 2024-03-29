package tcp.clients;

import java.io.*;
import java.net.*;
import tcp.*;
import protocols.*;
import util.*;

abstract public class TcpClient implements TcpThreadObject, TcpObject {
  Integer port;
  public TcpClient(Integer _port) {
    port = _port;
  }

  public void start() {
    start(port);
  }

  public void start(Integer serverPort) {
    try {
      Socket socket = new Socket(HOST, serverPort);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      String inputLine, outputLine;
      Protocol protocol = getProtocol();

      // initiate conversation
      out.println(protocol.getMessage());

      while ((inputLine = in.readLine()) != null) {
        outputLine = protocol.processInput(inputLine);
        if (protocol.disconnect()) {
          System.out.println("Disconnecting...");
          break; 
        }

        if(!protocol.noResponse) {
          out.println(outputLine);
        }
      }

      protocol.cleanUp();
      out.close();
      in.close();
      socket.close();
    }
    catch(Exception e) {
      Util.printException("TcpClient - start", e);
    }
  }
}
