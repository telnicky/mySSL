package tcp.clients;
import protocols.*;

public class Alice extends TcpClient {

  public Alice(Integer _port) {
    super(_port);
  }

  public Protocol getProtocol() {
    return new SslClientProtocol("aliceCert");
  }
  
  public static void main(String[] args) {
    new Alice(DEFAULT_PORT).start();
  }
}

