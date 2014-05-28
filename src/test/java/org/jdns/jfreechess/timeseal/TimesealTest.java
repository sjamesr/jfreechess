package org.jdns.jfreechess.timeseal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

public class TimesealTest {

  private static final long messageTime = TimeUnit.SECONDS.toNanos(1374900405);

  public static String toHexString(byte[] bytes) {
    List<String> wrappedBytes = Lists.newArrayListWithCapacity(bytes.length);
    for (byte theByte : bytes) {
      wrappedBytes.add(String.format("%02x", theByte));
    }

    return Joiner.on(", ").join(wrappedBytes);
  }

  @Test
  public void test() throws IOException {
    String raw = "TIMESTAMP|openseal|Running on an operating system|";
    byte[] encrypted = Timeseal.crypt(messageTime, raw.getBytes(), raw.getBytes().length);
    byte[] expected = Resources.toByteArray(Resources.getResource("resources/output.txt"));

    System.out.println(toHexString(expected) + " <- expected");
    System.out.println(toHexString(encrypted) + " <- actual");

    assertArrayEquals(expected, encrypted);
    TimesealedMessage decryptedMessage = Timeseal.decrypt(encrypted);
    assertEquals(raw, decryptedMessage.getMessage());
    assertEquals(1000 * (TimeUnit.NANOSECONDS.toSeconds(messageTime) % 1000),
        decryptedMessage.getTime());
  }

  @Test
  public void testBabas() throws IOException {
    System.out.println(System.getProperty("java.class.path"));
    byte[] byteArray = Resources.toByteArray(Resources.getResource("resources/babas.txt"));

    int start = 0, end = 0;

    do {
      if (byteArray[end] == '\n') {
        byte[] copy = Arrays.copyOfRange(byteArray, start, end);
        start = end + 1;
        System.out.println(Timeseal.decrypt(copy));
      }
      
      end++;
    } while (end < byteArray.length);
  }
}
