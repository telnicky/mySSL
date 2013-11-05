package clients;

import java.io.*;
import java.net.*;
import tcp.*;

abstract class TcpClient extends TcpObject {
  public void start(Integer serverPort) {
    try {
      Socket socket = new Socket(host, serverPort);
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      String inputLine, outputLine;
      Protocol protocol = getProtocol();

      // initiate conversation
      out.println(protocol.getMessage());
      while ((inputLine = in.readLine()) != null) {
        outputLine = protocol.processInput(inputLine);
        if (protocol.disconnect()) {
          break; 
        }
        out.println(outputLine);
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
