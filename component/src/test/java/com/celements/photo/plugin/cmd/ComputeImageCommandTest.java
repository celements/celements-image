/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.photo.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.awt.Color;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class ComputeImageCommandTest extends AbstractComponentTest {

  private ComputeImageCommand computeImgCmd;
  private ImageCacheCommand mockImgCacheCmd;

  @Before
  public void setUp_ComputeImageCommandTest() throws Exception {
    mockImgCacheCmd = createMockAndAddToDefault(ImageCacheCommand.class);
    computeImgCmd = new ComputeImageCommand();
    computeImgCmd.injectImageCacheCmd(mockImgCacheCmd);
  }

  @Test
  public void testGetImageCacheCmd() {
    computeImgCmd.injectImageCacheCmd(null);
    assertNotNull(computeImgCmd.getImageCacheCmd());
  }

  @Test
  public void testFlushCache() {
    ImageCacheCommand cacheCmdBefore = computeImgCmd.getImageCacheCmd();
    mockImgCacheCmd.flushCache();
    expectLastCall().once();
    replayDefault();
    computeImgCmd.flushCache();
    assertNotSame(cacheCmdBefore, computeImgCmd.getImageCacheCmd());
    verifyDefault();
  }

  @Test
  public void testParseIntWithDefault_NumberFormatExcp() {
    assertEquals(new Integer(50), computeImgCmd.parseIntWithDefault("abc", 50));
  }

  @Test
  public void testParseIntWithDefault_mull() {
    assertNull(computeImgCmd.parseIntWithDefault("hi", null));
  }

  @Test
  public void testParseIntWithDefault_emptyString() {
    assertEquals(new Integer(0), computeImgCmd.parseIntWithDefault("", 0));
  }

  @Test
  public void testParseIntWithDefault_correctValue() {
    assertEquals(new Integer(65), computeImgCmd.parseIntWithDefault("65", 10));
  }

  @Test
  public void testParseIntWithDefault_null() {
    assertEquals(new Integer(0), computeImgCmd.parseIntWithDefault(null, 0));
  }

  @Test
  public void testGetBackgroundColour_null() {
    assertNull(computeImgCmd.getBackgroundColour(null, null));
  }

  @Test
  public void testGetBackgroundColour_noColour() {
    assertNull(computeImgCmd.getBackgroundColour(null, ""));
  }

  @Test
  public void testGetBackgroundColour_withoutAlpha() {
    Color col = computeImgCmd.getBackgroundColour(null, "ff000A");
    assertEquals(255, col.getRed());
    assertEquals(0, col.getGreen());
    assertEquals(10, col.getBlue());
    assertEquals(255, col.getAlpha());
  }

  @Test
  public void testGetBackgroundColour_withAlpha() {
    Color col = computeImgCmd.getBackgroundColour(null, "0ffD13A0");
    assertEquals(15, col.getRed());
    assertEquals(253, col.getGreen());
    assertEquals(19, col.getBlue());
    assertEquals(160, col.getAlpha());
  }
}
