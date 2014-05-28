package org.jdns.jfreechess.timeseal;

import java.io.IOException;
import java.io.InputStream;

/*
 * Implements an InputStream that understands the timeseal protocol.
 */

public class TimesealInputStream extends InputStream {
  private static final byte[] TIMESEAL_DEMAND = "[G]\n".getBytes();
  private final TimesealOutputStream outputStream;
  private final InputStream wrappedStream;
  private int demandPosition = -1;

  public TimesealInputStream(TimesealOutputStream outputStream, InputStream wrappedStream) {
    this.outputStream = outputStream;
    this.wrappedStream = wrappedStream;
  }

  @Override
  public int read() throws IOException {
    int result;

    do {
      result = wrappedStream.read();
    } while (result == '\r');

    if (TIMESEAL_DEMAND[demandPosition + 1] == result) {
      demandPosition++;
    } else if (!responseRequired()) {
      demandPosition = -1;
    }

    if (responseRequired()) {
      outputStream.write(new byte[] { 0x02, '9' });
      outputStream.flush();
      demandPosition = -1;
    }

    return result;
  }

  private boolean responseRequired() {
    return demandPosition == TIMESEAL_DEMAND.length - 1;
  }
}
