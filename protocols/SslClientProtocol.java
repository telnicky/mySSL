package protocols;

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

  public String getMessage() {
    String output = sendFormat();
    byte[] bytes = authManager.encrypt(encryptionKey0, integrityMac0, output);
    String nextRequest = Util.toHexString(bytes);
    printOutput(certAlias, output);
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

  public String receiveMessageIntegrity(String fromAlias, String body) {
    if(validateMessageHash(fromAlias, "SERVER", body)) {

    }

    disconnect = true;
    return null;
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

