package authentication;

import javax.crypto.*;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.crypto.spec.*;

import util.Util;

public class AuthenticationManager {

  // keytool -genkey -keyalg RSA -alias bobCert -keystore keystore.jks -storepass password -validity 360 -keysize 2048
  private static KeyStore ks;
  private String certAlias;
  private static String keyStorePath = "authentication/certs/keystore.jks";
  private static String format = "DESede/CBC/PKCS5Padding";
  private static String encryptionAlgorithm = "DESede";
  private Cipher cipher = null;
  private SecretKey key = null;
  private SecureRandom rand = null;

  public AuthenticationManager(String _key, String _certAlias) {
    try {
      key = stringToKey(_key);
      rand = new SecureRandom();
      certAlias = _certAlias;
    }
    catch(Exception e) {
      Util.printException("Constructor", e);
    }
  }


  private static void cleanUp(FileInputStream _fis) {
    try {
      if(_fis != null) {
        _fis.close();
      }
    }
    catch(Exception e) {
      Util.printException("cleanUp", e);
    }
  }


  public String decrypt(byte[] cipher) {
    byte[] messageBytes = null;
    String message = null;

    try {
      messageBytes = getCipher(Cipher.DECRYPT_MODE).doFinal(cipher);
      message = new String(messageBytes);
    }
    catch(Exception e) {
      Util.printException("decrypt", e);
    }

    return message;
  }

  public byte[] encrypt(String message) {
    byte[] cipherText = null;

    try {
      //byte[] bytes = padMessage(message.getBytes("UTF-8"));
      byte[] bytes = message.getBytes("UTF-8");
      cipherText = getCipher(Cipher.ENCRYPT_MODE).doFinal(bytes);
    }
    catch(Exception e) {
      Util.printException("encrypt", e);
    }

    return cipherText;
  }

  public Cipher getCipher(int mode) {
    try {
      if(cipher == null) {
        cipher = Cipher.getInstance(format);
      }

      cipher.init(mode, key);
    } catch(Exception e) {
      Util.printException("getCipher", e);
    }

    return cipher;
  }

  public long getNonce() {
    byte[] nonce = new byte[64];
    rand.nextBytes(nonce);
    return ByteBuffer.wrap(nonce).getLong();
  }

  public String getKeyCertificate() {
    if(ks == null) {
      loadKeyStore();
    }

    try {
      Certificate cert = ks.getCertificate(certAlias);
      return Util.toHexString(cert.getEncoded());
    }
    catch(Exception e) {
      Util.printException("getKeyCertificate", e);
    }

    return null;
  }

  private static void loadKeyStore() {
    FileInputStream fis = null;
    try {
      ks = KeyStore.getInstance("JKS");
      fis = new java.io.FileInputStream(keyStorePath);
      ks.load(fis, "password".toCharArray());
    }
    catch(Exception e) {
      Util.printException("Auth Main", e);
    }
    finally {
      cleanUp(fis);
    }
  }

  public void printSecretKey() {
    System.out.println(Util.toHexString(key.getEncoded()));
  }

  // accepts a key as a string and returns a key
  public SecretKey stringToKey(String strKey) {
    SecretKey newKey = null;
    DESedeKeySpec spec;
    SecretKeyFactory factory;

    try {
      byte[] keyInBytes = strKey.getBytes("UTF-8");
      spec = new DESedeKeySpec(keyInBytes);
      factory = SecretKeyFactory.getInstance(encryptionAlgorithm);
      newKey = factory.generateSecret(spec);
    }
    catch(Exception e) {
      Util.printException("stringToKey", e);
    }

    return newKey;
  }

  public static void main(String[] args) {
    String testKey = "DEADBEEFDEADBEEFDEADBEEF";
    AuthenticationManager aM = new AuthenticationManager(testKey, "aliceCert");
    System.out.println(aM.getKeyCertificate());
    System.out.print("====");
    aM.printSecretKey();
    System.out.print("====");
    System.out.println(aM.getNonce());
  }
}

