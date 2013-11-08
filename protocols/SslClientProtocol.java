package protocols;

public class SslClientProtocol extends Protocol {

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

