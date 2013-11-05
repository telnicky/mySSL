package protocols;
import util.Util;

public abstract class Protocol {
  public abstract boolean disconnect();
  public abstract String getMessage();
  public abstract String processInput(String input); 
  public abstract void cleanUp(); 

  public void printInput(String protocol, String input) {
    String message = protocol + " <- " + input;
    Util.write_to_file("ref_needham_schroeder.txt", message);
    System.out.println(message);
  }

  public void printOutput(String protocol, String output) {
    if(output != null) {
      String message = protocol + " -> " + output;
      Util.write_to_file("ref_needham_schroeder.txt", message);
      System.out.println(message);
    }
  }

  public boolean suspend() {
    return false;
  }
}
