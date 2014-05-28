package org.jdns.jfreechess.timeseal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.Charsets;

/**
 * Implements a timeseal program using {@link TimesealSocket}.
 */
public class Timeseal {
  private static final Logger log = Logger.getLogger(Timeseal.class.getName());
  private static final byte[] KEY = "Timestamp (FICS) v1.0 - programmed by Henrik Gram.".getBytes();
  
  public static void main(String[] args) throws UnknownHostException, IOException {
    String host = "fics2.freechess.org";
    if (args.length > 0) {
      host = args[1];
    }

    int port = 5000;
    if (args.length > 1) {
      try {
        port = Integer.valueOf(args[1]);
      } catch (NumberFormatException e) {
        System.out.println("Usage: Timeseal [host [port]]");
      }
    }

    Socket socket = new TimesealSocket(InetAddress.getByName(host), port);
    new Thread(new Forwarder(System.in, socket.getOutputStream())).start();
    new Thread(new Forwarder(socket.getInputStream(), System.out)).start();
  }

  private static void swap(byte[] input, int a, int b) {
    input[b] ^= input[a] ^= input[b];
    input[a] ^= input[b];
  }

  public static byte[] crypt(long time, String message) {
    int index = message.length();
    byte[] input = new byte[12 + message.length()];
    System.arraycopy(message.getBytes(), 0, input, 0, index);
    input[index++] = 0x18;
    long seconds = TimeUnit.NANOSECONDS.toSeconds(time);
    long microseconds = TimeUnit.NANOSECONDS.toMicros(time) % 1000000;

    byte[] timeBytes = String.format("%d", (seconds % 10000) * 1000 + microseconds / 1000)
        .getBytes();
    System.arraycopy(timeBytes, 0, input, index, timeBytes.length);

    System.out.println("[crypt] before pad -> " + TimesealTest.toHexString(input));
    index += timeBytes.length;
    input[index++] = 0x19;
    for (; index % 12 != 0; index++) {
      input[index] = '1';
    }

    System.out.println("[crypt] before swap -> " + TimesealTest.toHexString(input));
    for (int n = 0; n < index; n += 12) {
      swap(input, n, n + 11);
      swap(input, n + 2, n + 9);
      swap(input, n + 4, n + 7);
    }

    System.out.println("[crypt] before xor -> " + TimesealTest.toHexString(input));
    for (int n = 0; n < index; n++) {
      input[n] = (byte) (((input[n] | 0x80) ^ KEY[n % KEY.length]) - 32);
    }

    input[index++] = (byte) 0x80;
    input[index++] = 0x0a;
    System.out.println("[crypt] done -> " + TimesealTest.toHexString(input));
    return input;
  }

  public static TimesealedMessage decrypt(byte[] input) {
    byte[] extendedInput = new byte[12 + input.length];
    System.arraycopy(input, 0, extendedInput, 0, input.length);
//    System.out.println("[decrypt] before xor -> " + Arrays.toString(input));
    int end = 0;

    // first, locate the end of the string
    for (; end < input.length; end++) {
      if (input[end] == 0x0a /* lf */) {
        break;
      }
    }

    int offset = input[end - 1] & 0xff;
//    System.out.println("offset=" + offset);
    
    for (int i = 0; i < end; i++) {
//      System.out.print(String.format("extendedInput[%d]=%d,", i, extendedInput[i]));
      int keyPosition = (i + offset - 0x80) % KEY.length;
      if (keyPosition < 0) {
        return null;
      }
      extendedInput[i] = (byte) (((extendedInput[i] + 32) ^ KEY[keyPosition]) & ~0x80);
//      System.out.println(String.format("out=%d", extendedInput[i]));
    }

//    System.out.println("[decrypt] before swap -> " + Arrays.toString(extendedInput));
    for (int i = 0; i < end; i += 12) {
      swap(extendedInput, i, i + 11);
      swap(extendedInput, i + 2, i + 9);
      swap(extendedInput, i + 4, i + 7);
    }

    long time = 0;
    int length = 0;
    for (int i = 0; i < extendedInput.length; i++) {
      if (extendedInput[i] == 0x18) {
        length = i;
        for (int j = i + 1; j < extendedInput.length; j++) {
          if (extendedInput[j] == 0x19) {
            time = Integer.parseInt(new String(extendedInput, i + 1, j - i - 1, Charsets.US_ASCII));
          }
        }
      }
    }

//    System.out.println("[decrypt] done -> " + Arrays.toString(extendedInput));
    return new TimesealedMessage(time, new String(extendedInput, 0, length));
  }

  private static class Forwarder implements Runnable {
    private final InputStream from;
    private final OutputStream to;

    public Forwarder(InputStream from, OutputStream to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public void run() {
      int read;

      try {
        while ((read = from.read()) != -1) {
          to.write(read);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
