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

public class SslServerProtocol extends SslProtocol {

  public SslServerProtocol(String _clientAlias) {
    certAlias = _clientAlias;
  }

  public String getMessage() {
    return null;
  }

  public String processInput(String input) {
    // header:body:certificate
    // REQUEST--ALIAS:body:certificate
    byte[] inputBytes = Util.toByteArray(input);
    String unencryptedInput = authManager.decrypt(encryptionKey0, integrityMac0, inputBytes);

    String[] request = unencryptedInput.split(":");
    String[] header = request[0].split("--");
    validateCertificate(header[1], request[2]);

    String nextResponse = null;
    // FORMAT--ALIAS:ENCFORMAT--INTFORMAT--NONCE:CERT
    if(header[0].equals(requests[0])) {
      nextResponse = receiveFormat(request[1]);
    }
    // MESSAGE_INTEGRITY--ALIAS:HASH:CERT
    else if(header[0].equals(requests[1])) {
      nextResponse = receiveMessageIntegrity(header[1], request[1]);
    }
    else {
      disconnect = true;
    }

    recordMessage(certAlias, input);
    return nextResponse;
  }

  public String receiveFormat(String body) {
    // ENCRYPTION--INTEGRITY--NONCE 
    String[] message = body.split("--");
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
      return sendMessageIntegrity();
    }

    disconnect = true;
    return null;
  }

  public String sendFormat(boolean success, long nonce) {
    if(!success) {
      return responses[0];
    }
    
    String header = responses[3];
    String body = responses[0] + "--" + String.valueOf(nonce);
    
    return buildMessage(header, body);
  }

  public String sendMessageIntegrity() {
    return hashMessages(certAlias, "SERVER");
  }
}

