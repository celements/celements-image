package com.celements.photo.utilities;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class DefaultImageUrlExtractorTest extends AbstractComponentTest {

  private DefaultImageUrlExtractor article;

  @Before
  public void preapareTest() throws Exception {
    getContext().setURLFactory(registerComponentMock(XWikiURLFactory.class));
    article = (DefaultImageUrlExtractor) Utils.getComponent(ImageUrlExtractor.class);
  }

  @Test
  public void testGetImgUrlSizeKey_nosize() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg";
    assertEquals(-1l, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyWidth_smallerMin() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg?test=bla&celwidth=3&celheight=";
    assertEquals(3 * 3l, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyWidth() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celwidth=1000";
    assertEquals(1000 * 1000l, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyHeight_smallerMin() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celheight=3&celwidth&";
    assertEquals(3 * 3l, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyHeight() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celheight=1000&celwidth";
    assertEquals(1000 * 1000l, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_both_productSmallerMin() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celwidth=2&celheight=500&"
        + "test=999999";
    assertEquals(2 * 500l, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_both() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celwidth=1000&celheight=800";
    assertEquals(1000 * 800l, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_maxSize() {
    long max = DefaultImageUrlExtractor.MAX_ALLOWED_DIM;
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celwidth=" + max
        + "&celheight=" + max;
    assertEquals(max * max, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlSizeKey_exceedsMaxSize() {
    long max = DefaultImageUrlExtractor.MAX_ALLOWED_DIM;
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celwidth=" + (max + 101)
        + "&celheight=" + (max + 202);
    assertEquals(max * max, (long) article.getImgUrlSizeKey(url));
  }

  @Test
  public void testGetImgUrlExternal_http() {
    String url = "http://www.test.url/download/images/imgurl/test.jpg?celwidth=1000&celheight=800";
    replayDefault();
    assertEquals(url, article.getImgUrlExternal(url).getUrl());
    verifyDefault();
  }

  @Test
  public void testGetImgUrlExternal_https() {
    String url = "https://www.test.url/download/images/imgurl/test.jpg?celwidth=1000&celheight=800";
    replayDefault();
    assertEquals(url, article.getImgUrlExternal(url).getUrl());
    verifyDefault();
  }

  @Test
  public void testGetImgUrlExternal_downloadAction() throws Exception {
    String domain = "https://www.test.url";
    String space = "ArtSpc";
    String docname = "Doc";
    String filename = "test.jpg";
    String action = "download";
    String querystring = "celwidth=1000&celheight=800";
    String url = "/" + action + "/" + space + "/" + docname + "/" + filename + "?" + querystring;
    expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
        eq(action), eq(querystring), eq(getContext().getDatabase()), same(getContext()))).andReturn(
            new URL(domain + url));
    replayDefault();
    assertEquals(domain + url, article.getImgUrlExternal(url).getUrl());
    verifyDefault();
  }

  @Test
  public void testGetImgUrlExternal_skinAction() throws Exception {
    String domain = "https://www.test.url";
    String space = "ArtSpc";
    String docname = "Doc";
    String filename = "test.jpg";
    String action = "skin";
    String querystring = "celwidth=1000&celheight=800&cropX=30";
    String url = "/" + action + "/" + space + "/" + docname + "/" + filename + "?" + querystring;
    expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
        eq(action), eq(querystring), eq(getContext().getDatabase()), same(getContext()))).andReturn(
            new URL(domain + url));
    replayDefault();
    assertEquals(domain + url, article.getImgUrlExternal(url).getUrl());
    verifyDefault();
  }

  @Test
  public void testGetImgUrlExternal_fileAction() throws Exception {
    String domain = "https://www.test.url";
    String space = "ArtSpc";
    String docname = "Doc";
    String filename = "test.jpg";
    String action = "file";
    String querystring = "celwidth=1000&celheight=800";
    String url = "/" + action + "/" + space + "/" + docname + "/" + filename + "?" + querystring;
    expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
        eq(action), eq(querystring), eq(getContext().getDatabase()), same(getContext()))).andReturn(
            new URL(domain + url));
    replayDefault();
    assertEquals(domain + url, article.getImgUrlExternal(url).getUrl());
    verifyDefault();
  }

}
