package org.jdns.jfreechess.timeseal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class TimesealOutputStream extends OutputStream {
  private final ByteBuffer buffer = ByteBuffer.allocate(TimesealSocket.BUFFER_SIZE);
  private final OutputStream underlyingStream;
  private final TimeSource timeSource;

  public TimesealOutputStream(OutputStream underlyingStream) {
    this(underlyingStream, new SystemTimeSource());
  }

  TimesealOutputStream(OutputStream underlyingStream, TimeSource timeSource) {
    this.underlyingStream = underlyingStream;
    this.timeSource = timeSource;
  }

  @Override
  public void write(int b) throws IOException {
    if ('\n' == b) {
      flush();
      return;
    }

    buffer.put((byte) (b & 0xFF));

    if (buffer.remaining() == 0) {
      throw new IllegalStateException("Line too long");
    }
  }

  @Override
  public void flush() throws IOException {
    int length = buffer.position() - buffer.arrayOffset();
    byte[] cryptedBuffer = new byte[TimesealSocket.BUFFER_SIZE + 20];
    System.arraycopy(buffer.array(), buffer.arrayOffset(), cryptedBuffer, 0, length);
    int k = Timeseal.crypt(timeSource, cryptedBuffer, length);
    underlyingStream.write(cryptedBuffer, 0, k);

    int i;
    for (i = length, k = 0; i < length; i++, k++) {
      buffer.put(k, buffer.get(i));
    }

    underlyingStream.flush();
    buffer.clear();
  }
}
