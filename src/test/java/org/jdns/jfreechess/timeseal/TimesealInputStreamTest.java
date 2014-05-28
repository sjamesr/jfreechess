package org.jdns.jfreechess.timeseal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;

import org.junit.Test;

/**
 * Tests for {@link TimesealInputStream}.
 */
public class TimesealInputStreamTest {

  @Test
  public void recognizesDemandString() throws IOException {
    ByteArrayOutputStream wrappedOutputStream = new ByteArrayOutputStream();
    TimesealOutputStream outputStream = new TimesealOutputStream(wrappedOutputStream);
    TimesealInputStream stream = new TimesealInputStream(outputStream, new ByteArrayInputStream(
        "hello world\n\r[G]\n\rblahface".getBytes()));

    while (stream.read() != -1);

    // reading should have generated some reply
    Assert.assertTrue(wrappedOutputStream.toByteArray().length > 0);
  }
}
