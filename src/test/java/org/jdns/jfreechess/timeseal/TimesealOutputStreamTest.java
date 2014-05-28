package org.jdns.jfreechess.timeseal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link TimesealOutputStream}.
 *
 * @author James Ring (sjr@jdns.org)
 */
public class TimesealOutputStreamTest {

  @Test
  public void testCrypt() throws IOException {
    byte[] message = "TIMESTAMP|openseal|Running on an operating system|".getBytes();
    byte[] expected = new byte[] { (byte) 0x84, (byte) 0x80, 0x71, (byte) 0x80, (byte) 0x9e,
        (byte) 0x80, (byte) 0x80, (byte) 0x9e, (byte) 0x80, (byte) 0xcd, (byte) 0xa7, 0x72,
        (byte) 0x80, (byte) 0x8d, (byte) 0x9d, (byte) 0xac, (byte) 0xd2, 0x7a, (byte) 0xad,
        (byte) 0xaf, (byte) 0xa5, (byte) 0xb3, (byte) 0xa3, (byte) 0xa5, 0x75, 0x75, 0x60, 0x68,
        0x7c, (byte) 0xa1, 0x6c, 0x63, (byte) 0xa5, (byte) 0xa4, (byte) 0xb0, 0x6c, 0x7c,
        (byte) 0xa1, (byte) 0x9b, 0x6c, 0x7d, 0x75, (byte) 0xa9, 0x65, (byte) 0xb9, (byte) 0x93,
        0x66, 0x73, (byte) 0xbc, (byte) 0xb2, (byte) 0xc5, (byte) 0xbf, (byte) 0xba, (byte) 0xb5,
        (byte) 0xa1, (byte) 0xa3, (byte) 0xb7, (byte) 0xd5, (byte) 0xc9, (byte) 0xad, (byte) 0x80,
        0xa };
    Clock fakeClock = new Clock() {
      @Override public long nanoTime() {
        return 1342676702761615000L;
      }
    };
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    TimesealOutputStream outputStream = new TimesealOutputStream(byteStream, fakeClock);
    outputStream.write(message);
    outputStream.flush();

    byte[] actual = byteStream.toByteArray();
    Assert.assertArrayEquals(expected, actual);
  }

}
