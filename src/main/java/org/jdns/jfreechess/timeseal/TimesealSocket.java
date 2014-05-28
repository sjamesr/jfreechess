package org.jdns.jfreechess.timeseal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import com.google.common.annotations.VisibleForTesting;

/**
 * A Java implementation of the timeseal protocol (see <a
 * href="http://www.freechess.org/Help/HelpFiles/timeseal.html">FICS timeseal help</a>). This
 * implementation is copied from the C version by Marcello Mamino, published <a
 * href="http://linuz.sns.it/~m2/openseal.c">here</a>.
 */
public class TimesealSocket extends Socket {
  static final int BUFFER_SIZE = 1024;

  private static final Logger log = Logger.getLogger(TimesealSocket.class.getName());

  private InputStream inputStream;
  private OutputStream outputStream;

  @VisibleForTesting
  TimesealSocket(InetAddress address, int port) throws IOException {
    super(address, port);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (inputStream == null) {
      inputStream = new TimesealInputStream((TimesealOutputStream) getOutputStream(),
          super.getInputStream());
    }

    return inputStream;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (outputStream == null) {
      outputStream = new TimesealOutputStream(super.getOutputStream());
      outputStream.write("TIMESTAMP|openseal|Running on an operating system|".getBytes());
      outputStream.flush();
    }

    return outputStream;
  }
}
