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
    assertNotNull(computeImgCmd.getImageCacheCmd(context));
    verify(xwiki, mockImgCacheCmd);
  }

  @Test
  public void testFlushCache() {
    ImageCacheCommand cacheCmdBefore = computeImgCmd.getImageCacheCmd(context);
    mockImgCacheCmd.flushCache();
    expectLastCall().once();
    replay(xwiki, mockImgCacheCmd);
    computeImgCmd.flushCache();
    assertNotSame(cacheCmdBefore, computeImgCmd.getImageCacheCmd(context));
    verify(xwiki, mockImgCacheCmd);
  }

}
