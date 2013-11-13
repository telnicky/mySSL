package tcp.servers;
import java.net.*;
import protocols.*;

public class BobThread extends TcpServerThread {
 
  public BobThread(Socket _socket) {
    super(_socket);
  }

  public Protocol getProtocol() {
    SslServerProtocol ssl = new SslServerProtocol("bobCert");
    return ssl;
  }
}
