package com.celements.photo.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class BarcodeScriptServiceTest extends AbstractBridgedComponentTestCase {

  BarcodeScriptService bss;

  @Before
  public void setUp_BarcodeScriptServiceTest() throws Exception {
    bss = new BarcodeScriptService();
  }

  @Test
  public void testGetEAN8Checksum() {
    assertEquals(0, bss.getEAN8Checksum(1234567));
    assertEquals(8, bss.getEAN8Checksum(1234));
    assertEquals(0, bss.getEAN8Checksum(123456789));
  }

}
