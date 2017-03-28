package com.celements.photo.utilities;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.photo.container.ImageUrl;
import com.celements.photo.exception.IllegalImageUrlException;
import com.google.common.base.Optional;
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
  public void testExtractImageUrlList_noImage() {
    assertTrue(article.extractImageUrlList("<div>hi there, no images to see!</div>").isEmpty());
  }

  @Test
  public void testExtractImageUrlList_oneImage() {
    String action = "skin";
    String filename = "file.png";
    List<ImageUrl> imgUrls = article.extractImageUrlList("<div>hi there, no images to see!</div>"
        + "<p><img src=\"http://www.celements.ch/file/TestSpace/Doc/otherFile.png\" class=\"img\" "
        + "alst=\"the image\" /></p><p><img src=\"/" + action + "/TestSpace/Doc/" + filename
        + "\" class=\"img\" alst=\"the image\" /></p>");
    assertEquals(1, imgUrls.size());
    assertEquals(action, imgUrls.get(0).getAction().get());
    assertEquals(filename, imgUrls.get(0).getFilename().get());
  }

  @Test
  public void testExtractImageUrlList_multipleImage() {
    String action1 = "skin";
    String filename1 = "file1.png";
    String action2 = "file";
    String filename2 = "file2.png";
    String action3 = "download";
    String filename3 = "file3.png";
    List<ImageUrl> imgUrls = article.extractImageUrlList("<div>hi there, no images to see!</div>"
        + "<p><img src=\"/" + action1 + "/TestSpace/Doc/" + filename1 + "\" class=\"img\" alst=\""
        + "the image\" /></p><p><img src=\"/" + action2 + "/TestSpace/Doc/" + filename2
        + "\" class=\"img\" alst=\"the image\" /></p><p><img src=\"/" + action3 + "/TestSpace/Doc/"
        + filename3 + "?celwidth=800&celheight=600\" class=\"img\" alst=\"the image\" /></p>");
    assertEquals(3, imgUrls.size());
    assertEquals(action1, imgUrls.get(0).getAction().get());
    assertEquals(filename1, imgUrls.get(0).getFilename().get());
    assertEquals(action2, imgUrls.get(1).getAction().get());
    assertEquals(filename2, imgUrls.get(1).getFilename().get());
    assertEquals(action3, imgUrls.get(2).getAction().get());
    assertEquals(filename3, imgUrls.get(2).getFilename().get());
  }

  @Test
  public void testExtractImagesSocialMediaUrlList() throws Exception {
    String domain = "http://www.celements.ch";
    String url1 = getAndExpectUrl(domain, "skin", "Space", "Doc", "image1.png",
        "?celwidth=400&celheight=300", true);
    String url2 = getAndExpectUrl(domain, "download", "GallerySpace", "OtherDoc", "image2.png", "",
        true);
    String url3 = getAndExpectUrl(domain, "skin", "LastSpace", "OtherDoc", "thefile.jpg",
        "?celwidth=800&celheight=600&hi=there", true);
    String url4 = getAndExpectUrl(domain, "download", "ImageSpace", "TheDoc", "image3.png", "?",
        true);
    String url5 = getAndExpectUrl(domain, "file", "OtherSpace", "Bars", "borderbar.gif",
        "?celwidth=800&celheight=2", false);
    replayDefault();
    List<ImageUrl> imageList = article.extractImagesSocialMediaUrlList("<div>hi there, no images to"
        + " see!</div><p><img src=\"" + url1 + "\" class=\"img\" alst=\"the image1\" /></p><p><img "
        + "src=\"" + url2 + "\" " + "class=\"img\" alst=\"the image2\" /></p><p><img src=\"" + url3
        + "\" class=\"img\" " + "alst=\"the image3\" /></p><span><img src=\"" + url4 + "\"/></span>"
        + "<span><img src=\"" + url5 + "\"/></span>");
    assertEquals(4, imageList.size());
    assertEquals(domain + url4, imageList.get(0).getExternalUrl());
    assertEquals(domain + url2, imageList.get(1).getExternalUrl());
    assertEquals(domain + url1, imageList.get(2).getExternalUrl());
    assertEquals(domain + url3, imageList.get(3).getExternalUrl());
    verifyDefault();
  }

  String getAndExpectUrl(String domain, String action, String space, String docname,
      String filename, String query, boolean expectCall) throws Exception {
    String url = "/" + action + "/" + space + "/" + docname + "/" + filename + query;
    if (expectCall) {
      expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
          eq(action), eq(query.replaceAll("\\?", "")), eq(getContext().getDatabase()), same(
              getContext()))).andReturn(new URL(domain + url));
    }
    return url;
  }

  @Test
  public void testGetImgUrlSizeKey_externalUrl() {
    try {
      new ImageUrl.Builder().url("https://www.test.url/download/images/imgurl/test.jpg").build();
      fail("Expected IllegalImageUrlException");
    } catch (IllegalImageUrlException e) {
    }
  }

  @Test
  public void testFilterMinMaxSize() {
    List<ImageUrl> imgList = article.filterMinMaxSize(getImageUrlTestListWithDefaultValues(),
        Optional.of(100), Optional.of(1000), Optional.of(1000L), Optional.of(1000000L), false);
    assertEquals(4, imgList.size());
    assertEquals(500, (long) imgList.get(0).getWidth().get());
    assertFalse(imgList.get(0).getHeight().isPresent());
    assertFalse(imgList.get(1).getWidth().isPresent());
    assertEquals(500, (long) imgList.get(1).getHeight().get());
    assertEquals(250, (long) imgList.get(2).getWidth().get());
    assertEquals(1000, (long) imgList.get(2).getHeight().get());
    assertEquals(900, (long) imgList.get(3).getWidth().get());
    assertEquals(800, (long) imgList.get(3).getHeight().get());
  }

  @Test
  public void testFilterMinMaxSize_noLimits_withDefault() {
    List<ImageUrl> imgList = article.filterMinMaxSize(getImageUrlTestListWithDefaultValues(),
        Optional.<Integer>absent(), Optional.<Integer>absent(), Optional.<Long>absent(),
        Optional.<Long>absent(), true);
    assertEquals(12, imgList.size());
    assertFalse(imgList.get(0).getWidth().isPresent());
    assertFalse(imgList.get(0).getHeight().isPresent());
    assertFalse(imgList.get(1).getWidth().isPresent());
    assertFalse(imgList.get(1).getHeight().isPresent());
    assertEquals(200, (long) imgList.get(2).getWidth().get());
    assertEquals(1, (long) imgList.get(2).getHeight().get());
    assertEquals(2, (long) imgList.get(3).getWidth().get());
    assertEquals(100, (long) imgList.get(3).getHeight().get());
    assertEquals(20, (long) imgList.get(4).getWidth().get());
    assertEquals(10, (long) imgList.get(4).getHeight().get());
    assertEquals(500, (long) imgList.get(5).getWidth().get());
    assertFalse(imgList.get(5).getHeight().isPresent());
    assertFalse(imgList.get(6).getWidth().isPresent());
    assertEquals(500, (long) imgList.get(6).getHeight().get());
    assertEquals(250, (long) imgList.get(7).getWidth().get());
    assertEquals(1000, (long) imgList.get(7).getHeight().get());
    assertEquals(1800, (long) imgList.get(8).getWidth().get());
    assertEquals(400, (long) imgList.get(8).getHeight().get());
    assertEquals(400, (long) imgList.get(9).getWidth().get());
    assertEquals(1800, (long) imgList.get(9).getHeight().get());
    assertEquals(900, (long) imgList.get(10).getWidth().get());
    assertEquals(800, (long) imgList.get(10).getHeight().get());
    assertEquals(2000, (long) imgList.get(11).getWidth().get());
    assertEquals(800, (long) imgList.get(11).getHeight().get());
  }

  @Test
  public void testFilterByPixels_empty() {
    List<ImageUrl> imgUrlList = new ArrayList<>();
    assertTrue(article.filterByPixels(imgUrlList, 1000L, 1000000L, true).isEmpty());
  }

  @Test
  public void testFilterByPixels_withContent_hasDefault() {
    List<ImageUrl> imgUrlList = getImageUrlTestListWithDefaultValues();
    List<ImageUrl> newList = article.filterByPixels(imgUrlList, 1000L, 1000000L, true);
    assertEquals(8, newList.size());
    assertFalse(newList.get(0).getWidth().isPresent());
    assertFalse(newList.get(0).getHeight().isPresent());
    assertFalse(newList.get(1).getWidth().isPresent());
    assertFalse(newList.get(1).getHeight().isPresent());
    assertEquals(500, (long) newList.get(2).getWidth().get());
    assertFalse(newList.get(2).getHeight().isPresent());
    assertFalse(newList.get(3).getWidth().isPresent());
    assertEquals(500, (long) newList.get(3).getHeight().get());
    assertEquals(250, (long) newList.get(4).getWidth().get());
    assertEquals(1000, (long) newList.get(4).getHeight().get());
    assertEquals(1800, (long) newList.get(5).getWidth().get());
    assertEquals(400, (long) newList.get(5).getHeight().get());
    assertEquals(400, (long) newList.get(6).getWidth().get());
    assertEquals(1800, (long) newList.get(6).getHeight().get());
    assertEquals(900, (long) newList.get(7).getWidth().get());
    assertEquals(800, (long) newList.get(7).getHeight().get());
  }

  @Test
  public void testFilterByPixels_withContent_noDefault() {
    List<ImageUrl> imgUrlList = getImageUrlTestListWithDefaultValues();
    List<ImageUrl> newList = article.filterByPixels(imgUrlList, 1000L, 1000000L, false);
    assertEquals(6, newList.size());
    assertEquals(500, (long) newList.get(0).getWidth().get());
    assertFalse(newList.get(0).getHeight().isPresent());
    assertFalse(newList.get(1).getWidth().isPresent());
    assertEquals(500, (long) newList.get(1).getHeight().get());
    assertEquals(250, (long) newList.get(2).getWidth().get());
    assertEquals(1000, (long) newList.get(2).getHeight().get());
    assertEquals(1800, (long) newList.get(3).getWidth().get());
    assertEquals(400, (long) newList.get(3).getHeight().get());
    assertEquals(400, (long) newList.get(4).getWidth().get());
    assertEquals(1800, (long) newList.get(4).getHeight().get());
    assertEquals(900, (long) newList.get(5).getWidth().get());
    assertEquals(800, (long) newList.get(5).getHeight().get());
  }

  @Test
  public void testFilterByPixels_withContent_noDefault_noMax() {
    List<ImageUrl> imgUrlList = getImageUrlTestListWithDefaultValues();
    List<ImageUrl> newList = article.filterByPixels(imgUrlList, 1000L, Long.MAX_VALUE, false);
    assertEquals(7, newList.size());
    assertEquals(500, (long) newList.get(0).getWidth().get());
    assertFalse(newList.get(0).getHeight().isPresent());
    assertFalse(newList.get(1).getWidth().isPresent());
    assertEquals(500, (long) newList.get(1).getHeight().get());
    assertEquals(250, (long) newList.get(2).getWidth().get());
    assertEquals(1000, (long) newList.get(2).getHeight().get());
    assertEquals(1800, (long) newList.get(3).getWidth().get());
    assertEquals(400, (long) newList.get(3).getHeight().get());
    assertEquals(400, (long) newList.get(4).getWidth().get());
    assertEquals(1800, (long) newList.get(4).getHeight().get());
    assertEquals(900, (long) newList.get(5).getWidth().get());
    assertEquals(800, (long) newList.get(5).getHeight().get());
    assertEquals(2000, (long) newList.get(6).getWidth().get());
    assertEquals(800, (long) newList.get(6).getHeight().get());
  }

  @Test
  public void testFilterBySideLength_empty() {
    List<ImageUrl> imgUrlList = article.filterBySideLength(new ArrayList<ImageUrl>(), 100, 1000,
        true);
    assertTrue(imgUrlList.isEmpty());
  }

  @Test
  public void testFilterBySideLength_nonEmpty_withDefault() {
    List<ImageUrl> imgUrlList = article.filterBySideLength(getImageUrlTestListWithDefaultValues(),
        100, 1000, true);
    assertEquals(6, imgUrlList.size());
    assertFalse(imgUrlList.get(0).getWidth().isPresent());
    assertFalse(imgUrlList.get(0).getHeight().isPresent());
    assertFalse(imgUrlList.get(1).getWidth().isPresent());
    assertFalse(imgUrlList.get(1).getHeight().isPresent());
    assertEquals(500, (long) imgUrlList.get(2).getWidth().get());
    assertFalse(imgUrlList.get(2).getHeight().isPresent());
    assertFalse(imgUrlList.get(3).getWidth().isPresent());
    assertEquals(500, (long) imgUrlList.get(3).getHeight().get());
    assertEquals(250, (long) imgUrlList.get(4).getWidth().get());
    assertEquals(1000, (long) imgUrlList.get(4).getHeight().get());
    assertEquals(900, (long) imgUrlList.get(5).getWidth().get());
    assertEquals(800, (long) imgUrlList.get(5).getHeight().get());
  }

  @Test
  public void testFilterBySideLength_nonEmpty_withoutDefault() {
    List<ImageUrl> imgUrlList = article.filterBySideLength(getImageUrlTestListWithDefaultValues(),
        100, 1000, false);
    assertEquals(4, imgUrlList.size());
    assertEquals(500, (long) imgUrlList.get(0).getWidth().get());
    assertFalse(imgUrlList.get(0).getHeight().isPresent());
    assertFalse(imgUrlList.get(1).getWidth().isPresent());
    assertEquals(500, (long) imgUrlList.get(1).getHeight().get());
    assertEquals(250, (long) imgUrlList.get(2).getWidth().get());
    assertEquals(1000, (long) imgUrlList.get(2).getHeight().get());
    assertEquals(900, (long) imgUrlList.get(3).getWidth().get());
    assertEquals(800, (long) imgUrlList.get(3).getHeight().get());
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
    ImageUrl imgUrl = new ImageUrl.Builder().url(url).build();
    expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
        eq(action), eq(querystring), eq(getContext().getDatabase()), same(getContext()))).andReturn(
            new URL(domain + url));
    replayDefault();
    assertEquals(domain + url, imgUrl.getExternalUrl());
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
    ImageUrl imgUrl = new ImageUrl.Builder().url(url).build();
    expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
        eq(action), eq(querystring), eq(getContext().getDatabase()), same(getContext()))).andReturn(
            new URL(domain + url));
    replayDefault();
    assertEquals(domain + url, imgUrl.getExternalUrl());
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
    ImageUrl imgUrl = new ImageUrl.Builder().url(url).build();
    expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
        eq(action), eq(querystring), eq(getContext().getDatabase()), same(getContext()))).andReturn(
            new URL(domain + url));
    replayDefault();
    assertEquals(domain + url, imgUrl.getExternalUrl());
    verifyDefault();
  }

  private List<ImageUrl> getImageUrlTestListWithDefaultValues() {
    List<ImageUrl> imgUrlList = new ArrayList<>();
    imgUrlList.add(new ImageUrl.Builder().build());
    imgUrlList.add(new ImageUrl.Builder().build());
    imgUrlList.add(new ImageUrl.Builder().width(200).height(1).build());
    imgUrlList.add(new ImageUrl.Builder().width(2).height(100).build());
    imgUrlList.add(new ImageUrl.Builder().width(20).height(10).build());
    imgUrlList.add(new ImageUrl.Builder().width(500).build());
    imgUrlList.add(new ImageUrl.Builder().height(500).build());
    imgUrlList.add(new ImageUrl.Builder().width(250).height(1000).build());
    imgUrlList.add(new ImageUrl.Builder().width(1800).height(400).build());
    imgUrlList.add(new ImageUrl.Builder().width(400).height(1800).build());
    imgUrlList.add(new ImageUrl.Builder().width(900).height(800).build());
    imgUrlList.add(new ImageUrl.Builder().width(2000).height(800).build());
    return imgUrlList;
  }

}
