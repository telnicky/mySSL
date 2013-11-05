package tcp;
import protocols.Protocol;
public abstract class TcpObject {
  public abstract Protocol getProtocol();
  public abstract Integer getPort();
  public abstract void start();
  protected static String host = "localhost";
}
