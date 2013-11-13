package runners;

import tcp.servers.*;
import tcp.clients.*;

public class SslRunner {
  public static void main(String[] args) {
    int serverPort = 8899;
    Alice alice = new Alice(serverPort);
    Bob bob = new Bob(serverPort);

    Thread b = new Thread(bob);
    b.start();
    
    while(!bob.listening) {
      System.out.println("busy...");
    } 

    alice.start(); 
  }
}
