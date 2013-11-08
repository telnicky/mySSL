package protocols;

public class SslServerProtocol extends Protocol {

  public void cleanUp() {

  }

  public boolean disconnect() {
    return false;
  }

  public String getMessage() {
    return null;
  }

  public String processInput(String input) {
    return null;
  }
}

