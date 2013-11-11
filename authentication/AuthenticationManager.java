package authentication;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;

import util.Util;

public class AuthenticationManager {

  // keytool -genkey -keyalg RSA -alias bobCert -keystore keystore.jks -storepass password -validity 360 -keysize 2048
  private static KeyStore ks;
  private String certAlias;
  private static int HASH_LENGTH = 20;
  private static String keyStorePath = "authentication/certs/keystore.jks";
  private static String format = "DESede/CBC/PKCS5Padding";
  private static String encryptionAlgorithm = "DESede";
  private static String hashAlgorithm = "HmacSHA1";
  private static byte[] ivMaterial = new byte[] { 4, 8, 15, 16, 23, 42, 4, 8 };
  private Cipher cipher = null;
  private static IvParameterSpec ivParameters = new IvParameterSpec(ivMaterial);
  private SecretKey encryptionKey = null;
  private SecureRandom rand = null;
  private Mac mac = null;

  public AuthenticationManager(String _encryptionKey, String _integrityKey, String _certAlias) {
    try {
      encryptionKey = stringToEncryptionKey(_encryptionKey);
      mac = buildMac(_integrityKey);
      rand = new SecureRandom();
      certAlias = _certAlias;
    }
    catch(Exception e) {
      Util.printException("Constructor", e);
    }
  }

  private Mac buildMac(String _key) {
    Mac newMac = null;
    try {
      SecretKeySpec spec = new SecretKeySpec(_key.getBytes(), hashAlgorithm);
      newMac = Mac.getInstance(hashAlgorithm);
      newMac.init(spec);
    }
    catch(Exception e) {
      Util.printException("buildMac", e);
    }

    return newMac;
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
      validateHash(cipher);
      cipher = Arrays.copyOfRange(cipher, 0, cipher.length - HASH_LENGTH);
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
      byte[] bytes = message.getBytes("UTF-8");
      cipherText = getCipher(Cipher.ENCRYPT_MODE).doFinal(bytes);
      System.out.println("l1: " + cipherText.length);
      cipherText = hashMessage(cipherText);
      System.out.println("l2: " + cipherText.length);
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

      cipher.init(mode, encryptionKey, ivParameters);
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

  // hash is 160 bits
  public byte[] hash(byte[] data) {
    return hash(data, 0, data.length);
  }

  public byte[] hash(byte[] data, int offset, int length) {
    mac.update(data, offset, length);
    byte[] hashed_data = mac.doFinal();

    return hashed_data;
  }

  private byte[] hashMessage(byte[] cipher) {
    byte[] messageDigest = hash(cipher);
    return Util.concatByteArrays(cipher, messageDigest);
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

  public void printKeys() {
    System.out.println("Encryption key:");
    System.out.println(Util.toHexString(encryptionKey.getEncoded()));
  }

  // accepts a key as a string and returns a key
  public SecretKey stringToEncryptionKey(String strKey) {
    SecretKey newKey = null;
    SecretKeyFactory factory;

    try {
      byte[] keyInBytes = strKey.getBytes("UTF-8");
      DESedeKeySpec spec = new DESedeKeySpec(keyInBytes);
      factory = SecretKeyFactory.getInstance(encryptionAlgorithm);
      newKey = factory.generateSecret(spec);
    }
    catch(Exception e) {
      Util.printException("stringToKey", e);
    }

    return newKey;
  }

  private void validateHash(byte[] cipher) throws Exception {
    byte[] originalHash = new byte[HASH_LENGTH];
    System.arraycopy(cipher, cipher.length - HASH_LENGTH, originalHash, 0, HASH_LENGTH);
    byte[] computedHash = hash(cipher, 0, cipher.length - HASH_LENGTH);

    if(!Arrays.equals(originalHash, computedHash)) {
      throw new Exception("Data integrity check failed");
    }
  }

  public static void main(String[] args) {
    String key1 = "DEADBEEFDEADBEEFDEADBEEF";
    String key2 = "DEADBEEF";
    AuthenticationManager aM = new AuthenticationManager(key1, key2, "aliceCert");
    System.out.println(aM.getKeyCertificate());
    System.out.print("====");
    aM.printKeys();
    System.out.print("====");
    System.out.println(aM.getNonce());
    System.out.println("====");
    System.out.println(aM.hash("foose".getBytes()));
    System.out.println("====");
    System.out.println(Util.toHexString(aM.encrypt("goosefraba")));
    System.out.println(aM.decrypt(aM.encrypt("goosefraba")));
  }
}

