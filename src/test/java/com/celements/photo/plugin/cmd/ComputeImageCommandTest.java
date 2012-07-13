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

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class ComputeImageCommandTest extends AbstractBridgedComponentTestCase {

  private ComputeImageCommand computeImgCmd;
  private XWikiContext context;
  private ImageCacheCommand mockImgCacheCmd;
  private XWiki xwiki;

  @Before
  public void setUp_ComputeImageCommandTest() throws Exception {
    context = getContext();
    computeImgCmd = new ComputeImageCommand();
    mockImgCacheCmd = createMock(ImageCacheCommand.class);
    computeImgCmd.injectImageCacheCmd(mockImgCacheCmd);
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testGetImageCacheCmd() {
    computeImgCmd.injectImageCacheCmd(null);
    replay(xwiki, mockImgCacheCmd);
    assertNotNull(computeImgCmd.getImageCacheCmd());
    verify(xwiki, mockImgCacheCmd);
  }

  @Test
  public void testFlushCache() {
    ImageCacheCommand cacheCmdBefore = computeImgCmd.getImageCacheCmd();
    mockImgCacheCmd.flushCache();
    expectLastCall().once();
    replay(xwiki, mockImgCacheCmd);
    computeImgCmd.flushCache();
    assertNotSame(cacheCmdBefore, computeImgCmd.getImageCacheCmd());
    verify(xwiki, mockImgCacheCmd);
  }

  @Test
  public void testParseIntWithDefault_NumberFormatExcp() {
    assertEquals(50, computeImgCmd.parseIntWithDefault("abc", 50));
  }

  @Test
  public void testParseIntWithDefault_emptyString() {
    assertEquals(0, computeImgCmd.parseIntWithDefault("", 0));
  }

  @Test
  public void testParseIntWithDefault_correctValue() {
    assertEquals(65, computeImgCmd.parseIntWithDefault("65", 10));
  }

  @Test
  public void testParseIntWithDefault_null() {
    assertEquals(0, computeImgCmd.parseIntWithDefault(null, 0));
  }

}
