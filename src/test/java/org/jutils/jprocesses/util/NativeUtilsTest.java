package org.jutils.jprocesses.util;

import org.junit.Test;

import java.text.ParseException;
import java.util.Locale;

import static org.junit.Assert.*;

@SuppressWarnings("Since15")
public class NativeUtilsTest {
  @Test
  public void getCustomDateFormat() throws Exception {
      NativeUtils utils = new NativeUtils();
      assertEquals("10/23/2016 08:30:00", utils.parseUnixLongTimeToFullDate("oct 23 08:30:00 2016"));
      try {
          utils.parseUnixLongTimeToFullDate("23 okt 2016 08:30:00");
        fail();
      } catch (ParseException e) {}
      utils.setCustomDateFormat("dd MMM yyyy HH:mm:ss");
      utils.setCustomLocale(Locale.forLanguageTag("no"));
      assertEquals("10/23/2016 08:30:00", utils.parseUnixLongTimeToFullDate("23 okt 2016 08:30:00"));
  }

}