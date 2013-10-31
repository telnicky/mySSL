abstract class TcpObject {
  abstract Protocol getProtocol();
  abstract Integer getPort();
  abstract void start();
  protected static String host = "localhost";
}
