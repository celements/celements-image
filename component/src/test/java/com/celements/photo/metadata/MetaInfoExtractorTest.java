package com.celements.photo.metadata;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MetaInfoExtractorTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testCleanCtrlChars_realLiveExmpl() {
    assertEquals("%G", new MetaInfoExtractor().cleanCtrlChars("%G"));
  }

  @Test
  public void testCleanCtrlChars_filter() {
    char c = 0xFFFA;
    assertEquals("", new MetaInfoExtractor().cleanCtrlChars(String.valueOf(c)));
  }

  @Test
  public void testCleanCtrlChars_noFilterSimple() {
    String text = "Hi there you 2!";
    assertEquals(text, new MetaInfoExtractor().cleanCtrlChars(text));
  }

  // -> problem: tab und zeilenumbruch werden gefiltert
  @Test
  public void testCleanCtrlChars_noFilterComplex() {
    String text = "lkdjhfsaadsf;\'][{}23934!@#$%^&*()))_+±§}|\":{}<?>/.,÷≥≤`«æ…“‘œ∑´®†"
        + "¥¨ˆøπ“åß∂ƒ©˙ ∆˚¬…æ§¡™\r\n£¢∞§¶•ªº–≠\t";
    assertEquals(text, new MetaInfoExtractor().cleanCtrlChars(text));
  }

}
