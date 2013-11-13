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
  private static int HASH_LENGTH = 20;
  private static int MIN_KEY_SIZE = 24;
  private static String keyStorePath = "authentication/certs/keystore.jks";
  private static String format = "DESede/CBC/PKCS5Padding";
  private static String encryptionAlgorithm = "DESede";
  private static String hashAlgorithm = "HmacSHA1";
  private static byte[] ivMaterial = new byte[] { 4, 8, 15, 16, 23, 42, 4, 8 };
  private static IvParameterSpec ivParameters = new IvParameterSpec(ivMaterial);
  
  private Cipher cipher = null;
  private SecureRandom rand = null;

  public AuthenticationManager(String _encryptionKey, String _integrityKey) {
    try {
      rand = new SecureRandom();
    }
    catch(Exception e) {
      Util.printException("Constructor", e);
    }
  }

  public SecretKey buildKey(long _key) {
    String newKey = String.valueOf(_key);
    return buildKey(newKey);
  }

  // accepts a key as a string and returns a key
  public SecretKey buildKey(String strKey) {
    SecretKey newKey = null;
    SecretKeyFactory factory;
    strKey = padKey(strKey);
    
    try {
      byte[] keyInBytes = strKey.getBytes("UTF-8");
      DESedeKeySpec spec = new DESedeKeySpec(keyInBytes);
      factory = SecretKeyFactory.getInstance(encryptionAlgorithm);
      newKey = factory.generateSecret(spec);
    }
    catch(Exception e) {
      Util.printException("buildKey", e);
    }

    return newKey;
  }

  public Mac buildMac(long _key) {
    return buildMac(String.valueOf(_key));
  }

  public Mac buildMac(String _key) {
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

  public String decrypt(SecretKey key, Mac mac, byte[] cipher) {
    byte[] messageBytes = null;
    String message = null;

    try {
      validateHash(mac, cipher);
      cipher = Arrays.copyOfRange(cipher, 0, cipher.length - HASH_LENGTH);
      messageBytes = getCipher(key, Cipher.DECRYPT_MODE).doFinal(cipher);
      message = new String(messageBytes);
    }
    catch(Exception e) {
      Util.printException("decrypt", e);
    }

    return message;
  }

  public byte[] encrypt(SecretKey key, Mac mac, String message) {
    byte[] cipherText = null;

    try {
      byte[] bytes = message.getBytes("UTF-8");
      cipherText = getCipher(key, Cipher.ENCRYPT_MODE).doFinal(bytes);
      System.out.println("l1: " + cipherText.length);
      cipherText = hashMessage(mac, cipherText);
      System.out.println("l2: " + cipherText.length);
    }
    catch(Exception e) {
      Util.printException("encrypt", e);
    }

    return cipherText;
  }

  public Cipher getCipher(SecretKey key, int mode) {
    try {
      if(cipher == null) {
        cipher = Cipher.getInstance(format);
      }

      cipher.init(mode, key, ivParameters);
    } catch(Exception e) {
      Util.printException("getCipher", e);
    }

    return cipher;
  }

  public String getCertificate(String alias) {
    if(ks == null) {
      loadKeyStore();
    }

    try {
      Certificate cert = ks.getCertificate(alias);
      return Util.toHexString(cert.getEncoded());
    }
    catch(Exception e) {
      Util.printException("getKeyCertificate", e);
    }

    return null;
  }

  public String getEncryptionFormat() {
    return format;
  }

  public String getIntegrityFormat() {
    return hashAlgorithm;
  }

  public long getNonce() {
    byte[] nonce = new byte[64];
    rand.nextBytes(nonce);
    return ByteBuffer.wrap(nonce).getLong();
  }

  // hash is 160 bits
  public byte[] hash(Mac mac, byte[] data) {
    return hash(mac, data, 0, data.length);
  }

  public byte[] hash(Mac mac, byte[] data, int offset, int length) {
    mac.update(data, offset, length);
    byte[] hashed_data = mac.doFinal();

    return hashed_data;
  }

  private byte[] hashMessage(Mac mac, byte[] cipher) {
    byte[] messageDigest = hash(mac, cipher);
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

  public String padKey(String _key) {
    for(int i = _key.length(); i < MIN_KEY_SIZE; i++) {
      _key += "P";
    }

    return _key;
  }

  public void validateCertificate(String alias, String _cert) throws Exception {
    String cert = getCertificate(alias);
    if(!cert.equals(_cert)) {
      throw new Exception("Invalid Certificate");
    }
  }

  private void validateHash(Mac mac, byte[] cipher) throws Exception {
    byte[] originalHash = new byte[HASH_LENGTH];
    System.arraycopy(cipher, cipher.length - HASH_LENGTH, originalHash, 0, HASH_LENGTH);
    byte[] computedHash = hash(mac, cipher, 0, cipher.length - HASH_LENGTH);

    if(!Arrays.equals(originalHash, computedHash)) {
      throw new Exception("Data integrity check failed");
    }
  }


  public static void main(String[] args) {
    String key1 = "DEADBEEFDEADBEEFDEADBEEF";
    String key2 = "DEADBEEF";
    long ms = 8901234;
    AuthenticationManager aM = new AuthenticationManager(key1, key2);
    
    try {
    } catch (Exception e) {
      Util.printException("main", e);
    }
  }
}

