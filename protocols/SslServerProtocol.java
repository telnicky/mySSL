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

  public SslServerProtocol(String _serverAlias) {
    certAlias = _serverAlias;
  }

  public String getMessage() {
    return null;
  }

  public String processInput(String input) {
    return processInput(input, requests);
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
      return sendMessageIntegrity();
    }

    disconnect = true;
    return null;
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

