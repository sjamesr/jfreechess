package org.jdns.jfreechess.timeseal;

public class TimesealedMessage {
  private final long time;
  private final String message;

  public TimesealedMessage(long time, String message) {
    this.time = time;
    this.message = message;
  }

  public long getTime() {
    return time;
  }

  public String getMessage() {
    return message;
  }
  
  @Override
  public String toString() {
    return String.format("%d: %s", time, message);
  }
}
