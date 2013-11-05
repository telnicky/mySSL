package servers;

import java.lang.*;
import java.io.*;
import java.net.*;
import tcp.*;
import util.*;
import protocols.*;

public abstract class TcpServer extends TcpObject implements Runnable {
  private void cleanUp(ServerSocket ss, Socket cs, PrintWriter pw, BufferedReader br) {
    try {
      if(pw != null) {
        pw.close();
      }

      if(br != null) {
        br.close();
      }

      if(cs != null) {
        cs.close();
      }

      if(ss != null) {
        ss.close();
      }
    } catch(Exception e) {
      Util.printException("TcpServer - cleanUp", e);
    }
  }

  public void run() {
    start();
  }

  public void start() {
    start(getPort());
  }

  public void start(Integer serverPort) {
    ServerSocket serverSocket = null; 
    Socket clientSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    try {
      serverSocket = new ServerSocket(serverPort);
      // Start accepting connections
      while(true) { 
        clientSocket = serverSocket.accept();

        // we have connected... build input and output streams
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String inputLine, outputLine;
        // initiate conversation with client
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
        cleanUp(null, clientSocket, out, in);
      }
    }
    catch(Exception e) {
      Util.printException("TcpServer - start", e);
      cleanUp(serverSocket, clientSocket, out, in);
    }
  }
}

