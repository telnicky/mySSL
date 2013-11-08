package tcp.servers;

import java.net.*;
import java.io.*;
import protocols.*;
import util.*;
import tcp.TcpThreadObject;

public class TcpServerThread extends Thread implements TcpThreadObject {
  protected Socket socket = null;

  public TcpServerThread(Socket _socket) {
    super("TcpServerThread");
    socket = _socket;
  }

  private void cleanUp(PrintWriter pw, BufferedReader br) {
    try {
      if(pw != null) {
        pw.close();
      }

      if(br != null) {
        br.close();
      }

      if(socket != null) {
        socket.close();
      }
    } catch(Exception e) {
      Util.printException("TcpServer - cleanUp", e);
    }
  }

  public Protocol getProtocol() {
    return null;
  }

  public void run() {
    PrintWriter out = null;
    BufferedReader in = null;

    try {
      // we have connected... build input and output streams
      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      String inputLine, outputLine;
      Protocol protocol = getProtocol();

      while ((inputLine = in.readLine()) != null) {
        outputLine = protocol.processInput(inputLine);
        if (protocol.disconnect()) {
          System.out.println("Disconnected");
          break;
        }

        out.println(outputLine);
      }

      // clean up
      protocol.cleanUp();
      cleanUp(out, in);
    }
    catch(Exception e) {
      Util.printException("TcpServerThread - run", e);
      cleanUp(out, in);
    }
  }
}

