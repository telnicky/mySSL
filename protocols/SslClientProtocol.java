package protocols;

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

public class SslClientProtocol extends SslProtocol {

  public SslClientProtocol(String _clientAlias) {
    certAlias = _clientAlias;
  }

  public SecretKey getOutboundKey() {
    if(completedHandshake) {
      return encryptionKey1;
    }

    return encryptionKey0;
  }

  public SecretKey getInboundKey() {
    if(completedHandshake) {
      return encryptionKey2;
    }

    return encryptionKey0;
  }

  public Mac getOutboundMac() {
    if(completedHandshake) {
      return integrityMac1;
    }

    return integrityMac0;
  }

  public Mac getInboundMac() {
    if(completedHandshake) {
      return integrityMac2;
    }

    return integrityMac0;
  }

  public String getMessage() {
    String output = sendFormat();
    byte[] bytes = authManager.encrypt(encryptionKey0, integrityMac0, output);
    String nextRequest = Util.toHexString(bytes);
    printOutput(certAlias, output);
    recordMessage(certAlias, nextRequest);
    return nextRequest;
  }

  public String processData(String input, String[] actions) {
    String response = null;
    String unencryptedInput = decryptMessage(input);

    String header = getRecordHeader(unencryptedInput);
    byte[] body = Util.toByteArray(getRecordBody(unencryptedInput));
    int size = getRecordSize(header);

    String log = "recieved " + size + " bytes" + " header:0x" + header;
    printInput(certAlias, log);

    if(size == 0) {
      response = validateFile();
      if(response.equals(success[0])) {
        System.out.println("Successfully transfered file!");
        disconnect = true;
      }
    }
    else {
      response = receiveData(body); 
    }

    String encryptedResponse = encryptMessage(response);
    return encryptedResponse;
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

    if(authenticatedMessages) {
      completedHandshake = true;
    }

    nextRequest = encryptMessage(nextRequest);
    recordMessage(header[1], input);
    recordMessage(certAlias, nextRequest);

    return nextRequest;
  }

  public String processInput(String input) {
    return processInput(input, responses);
  }

  public String receiveFormat(String body) {
    String[] message = body.split(secondLevelDelimiter); 

    if(message[0].equals(errors[0])) {
      disconnect = true;
      return sendFormat();
    }

    masterSecret = masterSecret ^ Long.valueOf(message[1]);
    generateKeys();
    return sendMessageIntegrity();
  }

  public String receiveData(byte[] body) {
    dataBytes.add(body);
    return success[0];
  }

  public String receiveMessageIntegrity(String fromAlias, String body) {
    if(validateMessageHash(fromAlias, "SERVER", body)) {
      authenticatedMessages = true;
      return success[0];
    }

    System.out.println("failed message integrity check");
    disconnect = true;
    return errors[0];
  }

  public String sendFormat() {
    masterSecret = authManager.getNonce();
    String header = requests[0];
    String body = authManager.getEncryptionFormat() + secondLevelDelimiter + authManager.getIntegrityFormat();
    body = body + secondLevelDelimiter + String.valueOf(masterSecret);
    return buildMessage(header, body);
  }

  public String sendMessageIntegrity() {
    String header = requests[1];
    return buildMessage(header, hashMessages(certAlias, "CLIENT"));
  }
}

