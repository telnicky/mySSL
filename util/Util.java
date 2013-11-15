package util;

import java.io.*;
import java.nio.*;

public class Util {
  public static byte[] concatByteArrays(byte[] a1, byte[] a2) {
    byte[] bytes = new byte[a1.length + a2.length];
    System.arraycopy(a1, 0, bytes, 0, a1.length);
    System.arraycopy(a2, 0, bytes, a1.length, a2.length);
    return bytes;
  }

  public static void printException(String methodName, Exception e) {
    System.out.println(methodName);
    System.out.println(e.getMessage());
    System.out.println(e.getClass());
  }

  public static byte[] toByteArray(short value) {
    byte[] bytes = new byte[2];
    bytes[0] = (byte)((value >> 8) & 0xff);
    bytes[1] = (byte)(value & 0xff);
    return bytes;
  }

  public static byte[] toByteArray(String hex) {
    int len = hex.length();
    byte[] bytes = new byte[len / 2];

    for(int i = 0; i < len; i += 2) {
      bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
          + Character.digit(hex.charAt(i+1), 16));
    }

    return bytes;
  }

  public static String toHexString(byte[] _bytes) {
    return toHexString(_bytes, false);
  }

  public static Integer toInteger(byte[] _bytes) {
    if(_bytes.length < 4) {
      byte[] padding = new byte[4 - _bytes.length];

      for(int i = 0; i < padding.length; i++) {
        padding[i] = 0;
      }

      _bytes = Util.concatByteArrays(padding, _bytes);
    }

    ByteBuffer buf = ByteBuffer.wrap(_bytes);
    return buf.getInt();
  }

  public static String toHexString(byte[] _bytes, boolean prependX) {
    String hex = "";

    if(prependX) {
      hex = "0x";
    }

    for(Byte b : _bytes) {
      hex = hex + String.format("%02X", b & 0xFF);
    }

    return hex;
  }

  public static void write_to_file(String filename, String content) {
    try {

      File file = new File(filename);
      if (!file.exists()) {
        file.createNewFile();
      }

      FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(content + "\n");
      bw.close();
 
      System.out.println(content);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
