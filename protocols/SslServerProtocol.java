package protocols;

import java.util.ArrayList;
import java.io.FileInputStream;
import javax.crypto.*;
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
    int bytesRead;

    ArrayList<byte[]> bytes = new ArrayList<byte[]>();
    try {
      FileInputStream fis = new FileInputStream(_file);
      while((bytesRead = fis.read(file, 0, MAX_DATA_SIZE)) > 0) {
        byte[] buffer = new byte[bytesRead];
        System.arraycopy(file, 0, buffer, 0, bytesRead);
        bytes.add(buffer);
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

  public SecretKey getOutboundKey() {
    if(completedHandshake) {
      return encryptionKey2;
    }

    return encryptionKey0;
  }

  public SecretKey getInboundKey() {
    if(completedHandshake) {
      return encryptionKey1;
    }

    return encryptionKey0;
  }

  public Mac getOutboundMac() {
    if(completedHandshake) {
      return integrityMac2;
    }

    return integrityMac0;
  }

  public Mac getInboundMac() {
    if(completedHandshake) {
      return integrityMac1;
    }

    return integrityMac0;
  }

  public String processData(String input, String[] actions) {
    String response = null;

    String unencryptedInput = decryptMessage(input);
    printInput(certAlias, unencryptedInput);

    if(!sendingData) {
      dataBytes = fileToBytes(transferFile);
    }

    if(unencryptedInput.equals(success[0])) {
      response = sendDataResponse();
    }
    else {
      response = errors[0];
    }


    String log = "sending... header:0x" + getRecordHeader(response) ;
    response = encryptMessage(response);
    printOutput(certAlias, log);

    return response; 
  }

  public String processHandshake(String input, String[] actions) {
    // header:body:certificate
    // RESPONSE--ALIAS:body:certificate
    String unencryptedInput = decryptMessage(input);
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

    printOutput(certAlias, nextRequest);

    nextRequest = encryptMessage(nextRequest);

    if(authenticatedMessages) {
      completedHandshake = true;
    }

    recordMessage(header[1], input);
    recordMessage(certAlias, nextRequest);

    return nextRequest;
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
      authenticatedMessages = true;
      return sendMessageIntegrity();
    }

    disconnect = true;
    return null;
  }

  public String sendDataResponse() {
    byte[] header = { 0x17, 0x03, 0x00 };
    if(!sendingData || dataBytes.size() == 0) {
      sendingData = false;
      header = Util.concatByteArrays(header, Util.toByteArray((short)0));
      return Util.toHexString(header);
    }

    byte[] data = dataBytes.remove(0);
    header = Util.concatByteArrays(header, Util.toByteArray((short)data.length));

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

