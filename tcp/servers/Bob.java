
package tcp.servers;

import java.io.*;
import java.net.*;
import util.*;

public class Bob extends TcpServer {
  public Bob(Integer port) {
    super(port);
  }

  public void acceptSocket(ServerSocket socket) {
    try {
      new BobThread(socket.accept()).run();
    }
    catch(Exception e) {
      Util.printException("Accept Socket", e);
    }
  }
  
  public static void main(String[] args) {
    new Bob(DEFAULT_PORT).run();
  }

}
