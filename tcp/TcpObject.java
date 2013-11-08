package tcp;
import protocols.Protocol;
public interface TcpObject {
  public abstract Protocol getProtocol();
  public abstract Integer getPort();
  public abstract void start();
  static String host = "localhost";
}
