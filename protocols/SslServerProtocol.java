package protocols;

import java.util.ArrayList;
import java.io.FileInputStream;
import util.Util;
// Protocol:
//   Format: [REQUEST|RESPONSE]:MESSAGE:CERTIFICATE
//   Client sends DESede/CBC/PKCS5Padding HmacSHA1 with Ra
//   Server response ok with Rb or no
// 
//     * Ra xor Rb to create Master secret
// 
//   Client sends exchanged messages with CLIENT prepended
//   Server sends exchanged messages with SERVER prepended
// 
//     * generate four keys 
//       two each for encryption
//       two for authentication
// 
//   Exchage file using ssl record format
//   Decrypt file and verify

public class SslServerProtocol extends SslProtocol {
  private boolean sendingData;

  public SslServerProtocol(String _serverAlias) {
    certAlias = _serverAlias;
    sendingData = false;
  }

  public ArrayList<byte[]> fileToBytes(String _file) {
    byte[] file = new byte[MAX_DATA_SIZE]; 

    ArrayList<byte[]> bytes = new ArrayList<byte[]>();
    try {
      FileInputStream fis = new FileInputStream(_file);
      while(fis.read(file) > 0) {
        bytes.add(file);
        file = new byte[MAX_DATA_SIZE];
      }
    }
    catch(Exception e) {
      Util.printException("validateFile", e);
    }

    sendingData = true;
    return bytes;
  }

  public String getMessage() {
    return null;
  }

  public String processData(String input, String[] actions) {
    String response = null;
    byte[] inputBytes = Util.toByteArray(input);
// need to setup keys
    String unencryptedInput = authManager.decrypt(encryptionKey0, integrityMac0, inputBytes);
 
    if(!sendingData) { 
      dataBytes = fileToBytes(transferFile);
    }
    
    if(unencryptedInput.equals(success[0])) {
      response = sendDataResponse();
    }
    else {
      response = errors[0];
    }
    
    printInput(certAlias, unencryptedInput);
    response = Util.toHexString(authManager.encrypt(encryptionKey0, integrityMac0, response));
    return response; 
  }
  public String processInput(String input) {
    return processInput(input, requests);
  }
 
  public String receiveData(byte[] body) {
    dataBytes.add(body);
    return success[0];
  }

 public String receiveFormat(String body) {
    // ENCRYPTION--INTEGRITY--NONCE 
    String[] message = body.split(secondLevelDelimiter);
    long nonce = authManager.getNonce();

    if(message[0].equals(authManager.getEncryptionFormat())
        && message[1].equals(authManager.getIntegrityFormat())) {

      masterSecret = nonce ^ Long.valueOf(message[2]);
      generateKeys();
      return sendFormat(true, nonce);
    }

    return sendFormat(false, nonce);
  }

  public String receiveMessageIntegrity(String fromAlias, String body) {
    if(validateMessageHash(fromAlias, "CLIENT", body)) {
      completedHandshake = true;
      return sendMessageIntegrity();
    }

    disconnect = true;
    return null;
  }

  public String sendDataResponse() {
    byte[] header = { 0x17, 0x03, 0x00, 0x00 }; 
    if(!sendingData || dataBytes.size() == 0) {
      sendingData = false;
      return Util.toHexString(header);
    }

    byte[] data = dataBytes.remove(0);
    header[3] = (byte)data.length;
    
    byte[] message = Util.concatByteArrays(header, data); 

    return Util.toHexString(message);
  }

  public String sendFormat(boolean valid, long nonce) {
    if(!valid) {
      return errors[0];
    }

    String header = responses[0];
    String body = success[0] + secondLevelDelimiter + String.valueOf(nonce);

    return buildMessage(header, body);
  }

  public String sendMessageIntegrity() {
    String header = responses[1];
    return buildMessage(header, hashMessages(certAlias, "SERVER"));
  }
}

