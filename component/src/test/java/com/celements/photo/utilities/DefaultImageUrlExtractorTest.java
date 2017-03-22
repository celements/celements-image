package com.celements.photo.utilities;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
  public void testGroupImageUrlsBySize() {
    ImageUrl url1 = new ImageUrl.Builder().width(500).build();
    ImageUrl url2 = new ImageUrl.Builder().build();
    ImageUrl url3 = new ImageUrl.Builder().width(250).height(1000).build();
    ImageUrl url4 = new ImageUrl.Builder().width(4000).height(3000).build();
    Map<Long, List<ImageUrl>> urlMap = article.groupImageUrlsBySize(Arrays.asList(url1, url2, url3,
        url4));
    assertEquals(3, urlMap.size());
    assertSame(url2, urlMap.get(-1L).get(0));
    assertSame(url1, urlMap.get(250000L).get(0));
    assertSame(url3, urlMap.get(250000L).get(1));
    assertSame(url4, urlMap.get(12000000L).get(0));
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
    Map<Long, List<ImageUrl>> resultMap = article.filterMinMaxSize(
        getImageUrlTestMapWithDefaultValues(), Optional.of(100), Optional.of(1000), Optional.of(
            1000L), Optional.of(1000000L), Optional.<Long>absent());
    assertEquals(2, resultMap.size());
    assertEquals(3, resultMap.get(500 * 500L).size());
    assertEquals(1, resultMap.get(900 * 800L).size());
  }

  @Test
  public void testGetImagesListForSizeMap_true_true() {
    List<ImageUrl> imgList = article.getImagesListForSizeMap(getImageUrlTestMapWithDefaultValues(),
        true, true);
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
  public void testGetImagesListForSizeMap_true_false() {
    List<ImageUrl> imgList = article.getImagesListForSizeMap(getImageUrlTestMapWithDefaultValues(),
        true, false);
    assertEquals(12, imgList.size());
    assertFalse(imgList.get(0).getWidth().isPresent());
    assertFalse(imgList.get(0).getHeight().isPresent());
    assertFalse(imgList.get(1).getWidth().isPresent());
    assertFalse(imgList.get(1).getHeight().isPresent());
    assertEquals(20, (long) imgList.get(2).getWidth().get());
    assertEquals(10, (long) imgList.get(2).getHeight().get());
    assertEquals(2, (long) imgList.get(3).getWidth().get());
    assertEquals(100, (long) imgList.get(3).getHeight().get());
    assertEquals(200, (long) imgList.get(4).getWidth().get());
    assertEquals(1, (long) imgList.get(4).getHeight().get());
    assertEquals(250, (long) imgList.get(5).getWidth().get());
    assertEquals(1000, (long) imgList.get(5).getHeight().get());
    assertFalse(imgList.get(6).getWidth().isPresent());
    assertEquals(500, (long) imgList.get(6).getHeight().get());
    assertEquals(500, (long) imgList.get(7).getWidth().get());
    assertFalse(imgList.get(7).getHeight().isPresent());
    assertEquals(900, (long) imgList.get(8).getWidth().get());
    assertEquals(800, (long) imgList.get(8).getHeight().get());
    assertEquals(400, (long) imgList.get(9).getWidth().get());
    assertEquals(1800, (long) imgList.get(9).getHeight().get());
    assertEquals(1800, (long) imgList.get(10).getWidth().get());
    assertEquals(400, (long) imgList.get(10).getHeight().get());
    assertEquals(2000, (long) imgList.get(11).getWidth().get());
    assertEquals(800, (long) imgList.get(11).getHeight().get());
  }

  @Test
  public void testGetImagesListForSizeMap_false_true() {
    HashMap<Long, List<ImageUrl>> unsortedMap = new HashMap<>(
        getImageUrlTestMapWithDefaultValues());
    List<ImageUrl> imgList = article.getImagesListForSizeMap(unsortedMap, false, true);
    assertEquals(12, imgList.size());
    assertEquals(2000, (long) imgList.get(0).getWidth().get());
    assertEquals(800, (long) imgList.get(0).getHeight().get());
    assertEquals(1800, (long) imgList.get(1).getWidth().get());
    assertEquals(400, (long) imgList.get(1).getHeight().get());
    assertEquals(400, (long) imgList.get(2).getWidth().get());
    assertEquals(1800, (long) imgList.get(2).getHeight().get());
    assertEquals(900, (long) imgList.get(3).getWidth().get());
    assertEquals(800, (long) imgList.get(3).getHeight().get());
    assertEquals(500, (long) imgList.get(4).getWidth().get());
    assertFalse(imgList.get(4).getHeight().isPresent());
    assertFalse(imgList.get(5).getWidth().isPresent());
    assertEquals(500, (long) imgList.get(5).getHeight().get());
    assertEquals(250, (long) imgList.get(6).getWidth().get());
    assertEquals(1000, (long) imgList.get(6).getHeight().get());
    assertEquals(200, (long) imgList.get(7).getWidth().get());
    assertEquals(1, (long) imgList.get(7).getHeight().get());
    assertEquals(2, (long) imgList.get(8).getWidth().get());
    assertEquals(100, (long) imgList.get(8).getHeight().get());
    assertEquals(20, (long) imgList.get(9).getWidth().get());
    assertEquals(10, (long) imgList.get(9).getHeight().get());
    assertFalse(imgList.get(10).getWidth().isPresent());
    assertFalse(imgList.get(10).getHeight().isPresent());
    assertFalse(imgList.get(11).getWidth().isPresent());
    assertFalse(imgList.get(11).getHeight().isPresent());
  }

  @Test
  public void testGetImagesListForSizeMap_false_false() {
    List<ImageUrl> imgList = article.getImagesListForSizeMap(getImageUrlTestMapWithDefaultValues(),
        false, false);
    assertEquals(12, imgList.size());
    assertEquals(2000, (long) imgList.get(0).getWidth().get());
    assertEquals(800, (long) imgList.get(0).getHeight().get());
    assertEquals(900, (long) imgList.get(1).getWidth().get());
    assertEquals(800, (long) imgList.get(1).getHeight().get());
    assertEquals(400, (long) imgList.get(2).getWidth().get());
    assertEquals(1800, (long) imgList.get(2).getHeight().get());
    assertEquals(1800, (long) imgList.get(3).getWidth().get());
    assertEquals(400, (long) imgList.get(3).getHeight().get());
    assertEquals(250, (long) imgList.get(4).getWidth().get());
    assertEquals(1000, (long) imgList.get(4).getHeight().get());
    assertFalse(imgList.get(5).getWidth().isPresent());
    assertEquals(500, (long) imgList.get(5).getHeight().get());
    assertEquals(500, (long) imgList.get(6).getWidth().get());
    assertFalse(imgList.get(6).getHeight().isPresent());
    assertEquals(20, (long) imgList.get(7).getWidth().get());
    assertEquals(10, (long) imgList.get(7).getHeight().get());
    assertEquals(2, (long) imgList.get(8).getWidth().get());
    assertEquals(100, (long) imgList.get(8).getHeight().get());
    assertEquals(200, (long) imgList.get(9).getWidth().get());
    assertEquals(1, (long) imgList.get(9).getHeight().get());
    assertFalse(imgList.get(10).getWidth().isPresent());
    assertFalse(imgList.get(10).getHeight().isPresent());
    assertFalse(imgList.get(11).getWidth().isPresent());
    assertFalse(imgList.get(11).getHeight().isPresent());
  }

  @Test
  public void testGetImgUrlSizeKey_nosize() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url("/download/images/imgurl/test.jpg").build();
    assertEquals(-1L, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyWidth_smallerMin() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?test=bla&celwidth=3&celheight=").build();
    assertEquals(3 * 3L, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyWidth() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celwidth=1000").build();
    assertEquals(1000 * 1000L, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyHeight_smallerMin() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celheight=3&celwidth&").build();
    assertEquals(3 * 3L, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_onlyHeight() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celheight=1000&celwidth").build();
    assertEquals(1000 * 1000L, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_both_productSmallerMin() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celwidth=2&celheight=500&test=999999").build();
    assertEquals(2 * 500L, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_both() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celwidth=1000&celheight=800").build();
    assertEquals(1000 * 800L, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_maxSize() throws Exception {
    long max = DefaultImageUrlExtractor.MAX_ALLOWED_DIM;
    ImageUrl imgUrl = new ImageUrl.Builder().url("/download/images/imgurl/test.jpg?celwidth=" + max
        + "&celheight=" + max).build();
    assertEquals(max * max, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testGetImgUrlSizeKey_exceedsMaxSize() throws Exception {
    long max = DefaultImageUrlExtractor.MAX_ALLOWED_DIM;
    ImageUrl imgUrl = new ImageUrl.Builder().url("/download/images/imgurl/test.jpg?celwidth=" + (max
        + 101) + "&celheight=" + (max + 202)).build();
    assertEquals(max * max, (long) article.getImgUrlSizeKey(imgUrl));
  }

  @Test
  public void testFilterByPixels_empty() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = new TreeMap<>();
    assertTrue(article.filterByPixels(imgUrlMap, 1000L, 1000000L, Optional.of(-1L)).isEmpty());
  }

  @Test
  public void testFilterByPixels_withContent_hasDefault() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = getImageUrlTestMapWithDefaultValues();
    Map<Long, List<ImageUrl>> newMap = article.filterByPixels(imgUrlMap, 1000L, 1000000L,
        Optional.of(-1L));
    assertEquals(3, newMap.size());
    assertNotNull(newMap.get(-1L));
    assertNotNull(newMap.get(500 * 500L));
    assertNotNull(newMap.get(900 * 800L));
  }

  @Test
  public void testFilterByPixels_withContent_noDefault() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = getImageUrlTestMapWithDefaultValues();
    Map<Long, List<ImageUrl>> newMap = article.filterByPixels(imgUrlMap, 1000L, 1000000L,
        Optional.<Long>absent());
    assertEquals(2, newMap.size());
    assertNotNull(newMap.get(500 * 500L));
    assertNotNull(newMap.get(900 * 800L));
  }

  @Test
  public void testFilterByPixels_withContent_noDefault_noMax() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = getImageUrlTestMapWithDefaultValues();
    Map<Long, List<ImageUrl>> newMap = article.filterByPixels(imgUrlMap, 1000L, Long.MAX_VALUE,
        Optional.<Long>absent());
    assertEquals(3, newMap.size());
    assertNotNull(newMap.get(500 * 500L));
    assertNotNull(newMap.get(900 * 800L));
    assertNotNull(newMap.get(2000 * 800L));
  }

  @Test
  public void testFilterBySideLength_empty() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = new TreeMap<>();
    article.filterBySideLength(imgUrlMap, 100, 1000, Optional.of(-1L));
    assertTrue(imgUrlMap.isEmpty());
  }

  @Test
  public void testFilterBySideLength_nonEmpty_withDefault() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = getImageUrlTestMapWithDefaultValues();
    article.filterBySideLength(imgUrlMap, 100, 1000, Optional.of(-1L));
    assertEquals(3, imgUrlMap.size());
    assertEquals(2, imgUrlMap.get(-1L).size());
    assertEquals(3, imgUrlMap.get(500 * 500L).size());
    assertEquals(1, imgUrlMap.get(900 * 800L).size());
  }

  @Test
  public void testFilterBySideLength_nonEmpty_withoutDefault() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = getImageUrlTestMapWithDefaultValues();
    article.filterBySideLength(imgUrlMap, 100, 1000, Optional.<Long>absent());
    assertEquals(2, imgUrlMap.size());
    assertEquals(3, imgUrlMap.get(500 * 500L).size());
    assertEquals(1, imgUrlMap.get(900 * 800L).size());
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

  private TreeMap<Long, List<ImageUrl>> getImageUrlTestMapWithDefaultValues() {
    TreeMap<Long, List<ImageUrl>> imgUrlMap = new TreeMap<>();
    imgUrlMap.put(-1L, Arrays.asList(new ImageUrl.Builder().build(),
        new ImageUrl.Builder().build()));
    imgUrlMap.put(20 * 10L, Arrays.asList(new ImageUrl.Builder().width(200).height(1).build(),
        new ImageUrl.Builder().width(2).height(100).build(), new ImageUrl.Builder().width(
            20).height(10).build()));
    imgUrlMap.put(500 * 500L, Arrays.asList(new ImageUrl.Builder().width(500).build(),
        new ImageUrl.Builder().height(500).build(), new ImageUrl.Builder().width(250).height(
            1000).build()));
    imgUrlMap.put(900 * 800L, Arrays.asList(new ImageUrl.Builder().width(1800).height(400).build(),
        new ImageUrl.Builder().width(400).height(1800).build(), new ImageUrl.Builder().width(
            900).height(800).build()));
    imgUrlMap.put(2000 * 800L, Arrays.asList(new ImageUrl.Builder().width(2000).height(
        800).build()));
    return imgUrlMap;
  }

}
