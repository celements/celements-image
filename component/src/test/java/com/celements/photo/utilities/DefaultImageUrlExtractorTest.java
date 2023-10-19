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
    assertEquals(filename, imgUrls.get(0).getFilename());
  }

  @Test
  public void test_extractImageUrlList_crop() {
    List<ImageUrl> imgUrls = article.extractImageUrlList("<h5><span style=\"color: #333333;"
        + " font-family: FrutigerLTStd, Arial, sans-serif; font-size: 14px;\"><img width=\"120\""
        + " height=\"120\" class=\"celanim_addCounterNone celanim_autostartnostop"
        + " celanim_overlayautostart celanim_addCounterOverlayNone\" style=\"border: 0px;\""
        + " src=\"../../download/Content_attachments/FileBaseDoc/steien-edito-06-23.jpg"
        + "?celwidth=120&amp;celheight=120&amp;cropX=411&amp;cropY=292&amp;cropW=664&amp;"
        + "cropH=664&amp;\" border=\"0\" />Editorial aus <br />dem Juniheft 2023<br /></span><b>"
        + "</b></h5>\\n<p class=\"p1\"><span class=\"s1\">Bye-bye «Kinostrasse»</span></p>\\n<p>"
        + "<b><span style=\"color: black; font-family: CharterEF-Regular, Arial, sans-serif;"
        + " font-style: italic;\">Sabine Knosala</span></b></p>");
    assertEquals(1, imgUrls.size());
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
    assertEquals(filename1, imgUrls.get(0).getFilename());
    assertEquals(action2, imgUrls.get(1).getAction().get());
    assertEquals(filename2, imgUrls.get(1).getFilename());
    assertEquals(action3, imgUrls.get(2).getAction().get());
    assertEquals(filename3, imgUrls.get(2).getFilename());
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
      new ImageUrl.Builder("https://www.test.url/download/images/imgurl/test.jpg").build();
      fail("Expected IllegalImageUrlException");
    } catch (IllegalImageUrlException e) {}
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
    ImageUrl imgUrl = new ImageUrl.Builder(url).build();
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
    ImageUrl imgUrl = new ImageUrl.Builder(url).build();
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
    ImageUrl imgUrl = new ImageUrl.Builder(url).build();
    expect(getContext().getURLFactory().createAttachmentURL(eq(filename), eq(space), eq(docname),
        eq(action), eq(querystring), eq(getContext().getDatabase()), same(getContext()))).andReturn(
            new URL(domain + url));
    replayDefault();
    assertEquals(domain + url, imgUrl.getExternalUrl());
    verifyDefault();
  }

  private List<ImageUrl> getImageUrlTestListWithDefaultValues() throws Exception {
    String url = "/file/space/doc/file.png";
    List<ImageUrl> imgUrlList = new ArrayList<>();
    imgUrlList.add(new ImageUrl.Builder(url).build());
    imgUrlList.add(new ImageUrl.Builder(url).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(200).height(1).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(2).height(100).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(20).height(10).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(500).build());
    imgUrlList.add(new ImageUrl.Builder(url).height(500).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(250).height(1000).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(1800).height(400).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(400).height(1800).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(900).height(800).build());
    imgUrlList.add(new ImageUrl.Builder(url).width(2000).height(800).build());
    return imgUrlList;
  }

}
