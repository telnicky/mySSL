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
    return sendFormat();
  }

  public String processInput(String input) {
    // header:body:certificate
    // RESPONSE--ALIAS:body:certificate
    byte[] inputBytes = Util.toByteArray(input);
    String unencryptedInput = authManager.decrypt(encryptionKey0, integrityMac0, inputBytes);

    String[] response = unencryptedInput.split(":");
    String[] header = response[0].split("--");
    validateCertificate(header[1], response[2]);

    String nextRequest = null;
    // FORMAT--ALIAS:OK--NONCE:CERT
    if(header[0].equals(responses[3])) {
      nextRequest = receiveFormat(response[1]);
    }
    // MESSAGE_INTEGRITY--ALIAS:HASH:CERT
    else if(header[0].equals(responses[1])) {
      nextRequest = receiveMessageIntegrity(header[1], response[1]);
    }
    else {
      disconnect = true;
    }

    recordMessage(certAlias, input);
    return nextRequest;
  }

  public String receiveFormat(String body) {
    String[] message = body.split("--"); 

    if(message[0].equals(responses[0])) {
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
    String body = authManager.getEncryptionFormat() + "--" + authManager.getIntegrityFormat();
    body = body + "--" + String.valueOf(masterSecret);
    return buildMessage(header, body);
  }

  public String sendMessageIntegrity() {
    return hashMessages(certAlias, "CLIENT");
  }
}

