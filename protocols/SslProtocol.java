package protocols;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileInputStream;
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
  protected boolean completedHandshake = false;
  protected ArrayList<byte[]> dataBytes;

  protected static String[] requests  = { "FORMAT", "MESSAGE_INTEGRITY" };
  protected static String[] responses = { "FORMAT", "MESSAGE_INTEGRITY" };
  protected static String[] errors = { "NO" };
  protected static String[] success = { "OK" };
  protected static String topLevelDelimiter = ":";
  protected static String secondLevelDelimiter = "#";
  protected static String transferFile = "solutions/backbone.js";
  protected static int HASH_LENGTH = 20;
  protected static int MAX_TRANSFER_SIZE = 16386;
  protected static int MAX_DATA_SIZE = MAX_TRANSFER_SIZE - HASH_LENGTH;

  public abstract String processData(String input, String[] actions);
  public abstract String receiveFormat(String body); 
  public abstract String receiveMessageIntegrity(String fromAlias, String body);

  public SslProtocol() {
    disconnect = false;
    exchangedMessages = new HashMap<String, ArrayList<String>>(); 
    authManager = new AuthenticationManager();
    dataBytes = new ArrayList<byte[]>();
    encryptionKey0 = authManager.buildKey(sharedKey);
    integrityMac0 = authManager.buildMac(sharedMac);
  }

  public String buildMessage(String header, String body) {
    header = header + secondLevelDelimiter + certAlias;
    return header + topLevelDelimiter + body + topLevelDelimiter + authManager.getCertificate(certAlias);
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
  
  public String processHandshake(String input, String[] actions) {
    // header:body:certificate
    // RESPONSE--ALIAS:body:certificate
    byte[] inputBytes = Util.toByteArray(input);
    String unencryptedInput = authManager.decrypt(encryptionKey0, integrityMac0, inputBytes);

    printInput(certAlias, unencryptedInput);

    String[] response = unencryptedInput.split(topLevelDelimiter);
    String[] header = response[0].split(secondLevelDelimiter);
    validateCertificate(header[1], response[2]);

    String nextRequest = null;
    // FORMAT--ALIAS:OK--NONCE:CERT
    if(header[0].equals(actions[0])) {
      nextRequest = receiveFormat(response[1]);
    }
    // MESSAGE_INTEGRITY--ALIAS:HASH:CERT
    else if(header[0].equals(actions[1])) {
      nextRequest = receiveMessageIntegrity(header[1], response[1]);
    }
    else {
      disconnect = true;
    }

    if(disconnect()) {
      return null;
    }
    
    recordMessage(header[1], input);
    printOutput(certAlias, nextRequest);
    nextRequest = Util.toHexString(authManager.encrypt(encryptionKey0, integrityMac0, nextRequest));
    recordMessage(certAlias, nextRequest);

    return nextRequest;
  }

   
  public String processInput(String input, String[] actions) {
    String nextRequest = null; 
    if(!completedHandshake) {
      nextRequest = processHandshake(input, actions);
    }
    else {
      nextRequest = processData(input, actions);  
    }  
    
    return nextRequest;
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

  public String receiveData(byte[] body) {
    dataBytes.add(body);
    return success[0];
  }

  public void recordMessage(String sender, String message) {
    if(exchangedMessages.get(sender) == null) {
      exchangedMessages.put(sender, new ArrayList<String>());
    }

    exchangedMessages.get(sender).add(message);
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

  public String validateFile() {
    byte[] file = dataBytes.get(0); 
    byte[] originalFile = null;

    for(int i = 1; i < dataBytes.size(); i++) {
      file = Util.concatByteArrays(file, dataBytes.get(i));
    }

    try {
      FileInputStream fis = new FileInputStream(transferFile);
      originalFile = new byte[file.length];
      fis.read(originalFile);
    }
    catch(Exception e) {
      Util.printException("validateFile", e);
    }

    String fileStr = Util.toHexString(file);
    String originalFileStr = Util.toHexString(originalFile);

    if(fileStr.equals(originalFileStr)) {
      return success[0];
    }

    return errors[0];
  }

  public boolean validateMessageHash(String fromAlias, String prepend, String messageHash) {
    String hash = hashMessages(fromAlias, prepend);
    if(hash.equals(messageHash)) {
      completedHandshake = true;
      return true;
    }

    return false;
  }
}

