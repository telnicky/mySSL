package protocols;

import java.util.HashMap;
import java.util.ArrayList;
import javax.crypto.*;

import authentication.AuthenticationManager;
import util.Util;

public abstract class SslProtocol extends Protocol {
  protected AuthenticationManager authManager;
  protected String certAlias;
  protected boolean disconnect;
  protected HashMap<String, ArrayList<String>> exchangedMessages;
  protected SecretKey encryptionKey0;
  protected SecretKey encryptionKey1;
  protected SecretKey encryptionKey2;
  protected Mac integrityMac0;
  protected Mac integrityMac1;
  protected Mac integrityMac2;
  protected static String sharedKey = "foooooooooooooooooooobar";
  protected static String sharedMac = "foobar";
  protected long masterSecret;

  protected static String[] requests  = { "FORMAT", "MESSAGE_INTEGRITY" };
  protected static String[] responses = { "NO", "MESSAGE_INTEGRITY", "OK", "FORMAT"};

  public SslProtocol() {
    disconnect = false;
    exchangedMessages = new HashMap<String, ArrayList<String>>(); 
    encryptionKey0 = authManager.buildKey(sharedKey);
    integrityMac0 = authManager.buildMac(sharedMac);
  }

  public String buildMessage(String header, String body) {
    header = header + "--" + certAlias;
    return header + ":" + body + ":" + authManager.getCertificate(certAlias);
  }

  public void cleanUp() {
    disconnect = false;
  }

  public void clearExchangedMessages(String alias) {
    exchangedMessages.get(alias).clear();
  }

  public boolean disconnect() {
    return disconnect;
  }

  public void generateKeys() {
    integrityMac1 = authManager.buildMac(masterSecret);  
    integrityMac2 = authManager.buildMac(masterSecret + 1);  
    encryptionKey1 = authManager.buildKey(masterSecret);  
    encryptionKey2 = authManager.buildKey(masterSecret + 1);  
  }

  public String hashMessages(String alias, String prepend) {
    ArrayList<String> messages = exchangedMessages.get(alias);
    String hashedMessage = prepend;
    for(int i = 0; i < messages.size(); i++) {
      hashedMessage += messages.get(i);
    }

    byte[] hash = authManager.hash(integrityMac0, Util.toByteArray(hashedMessage));
    return Util.toHexString(hash);
  }

  public void recordMessage(String sender, String message) {
    ArrayList<String> messages = exchangedMessages.get(sender);

    if(messages == null) {
      messages = new ArrayList<String>();
    }

    messages.add(message);
  }

  public void validateCertificate(String alias, String cert) {
    try {
      authManager.validateCertificate(alias, cert);
    }
    catch(Exception e) {
      disconnect = true;
      Util.printException("validateCertificate", e);
    }
  }

  public boolean validateMessageHash(String fromAlias, String prepend, String messageHash) {
    String hash = hashMessages(fromAlias, prepend);
    if(hash.equals(messageHash)) {
      return true;
    }

    return false;
  }
}

