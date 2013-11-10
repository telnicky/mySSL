package authentication;

import java.security.KeyStore;
import java.security.cert.Certificate;
import util.Util;
import java.io.FileInputStream;

public class AuthenticationManager {

  // keytool -genkey -keyalg RSA -alias bobCert -keystore keystore.jks -storepass password -validity 360 -keysize 2048
  private static KeyStore ks;
  private String certAlias;
  private static String keyStorePath = "authentication/certs/keystore.jks";

  public AuthenticationManager(String _certAlias) {
    certAlias = _certAlias;
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

  public static void main(String[] args) {
    System.out.println(new AuthenticationManager("aliceCert").getKeyCertificate());
  }
}

