package tcp;
import protocols.Protocol;
public interface TcpObject {
  public abstract void start(Integer port);
  static String host = "localhost";
}
