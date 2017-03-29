package com.celements.photo.container.imageurl.helpers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.photo.container.ImageUrl;

public class ImageAreaSizeComparatorTest extends AbstractComponentTest {

  private ImageAreaSizeComparator comparator;

  @Before
  public void prepareTest() throws Exception {
    comparator = new ImageAreaSizeComparator();
  }

  @Test
  public void testCompare() throws Exception {
    String url = "/space/doc/file.png";
    String fileUrl = "/file" + url;
    List<ImageUrl> imgUrlList = new ArrayList<>();
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(200).height(1).build());
    imgUrlList.add(new ImageUrl.Builder("/download" + url).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(500).build());
    imgUrlList.add(new ImageUrl.Builder("/skin" + url).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(20).height(10).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(1800).height(400).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).height(500).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(250).height(1000).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(2000).height(800).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(900).height(800).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(2).height(100).build());
    imgUrlList.add(new ImageUrl.Builder(fileUrl).width(400).height(1800).build());
    Collections.sort(imgUrlList, comparator);
    assertEquals(12, imgUrlList.size());
    assertEquals("download", imgUrlList.get(0).getAction().get());
    assertEquals("skin", imgUrlList.get(1).getAction().get());
    assertEquals(200, (int) imgUrlList.get(2).getWidth().get());
    assertEquals(1, (int) imgUrlList.get(2).getHeight().get());
    assertEquals(20, (int) imgUrlList.get(3).getWidth().get());
    assertEquals(10, (int) imgUrlList.get(3).getHeight().get());
    assertEquals(2, (int) imgUrlList.get(4).getWidth().get());
    assertEquals(100, (int) imgUrlList.get(4).getHeight().get());
    assertEquals(500, (int) imgUrlList.get(5).getWidth().get());
    assertFalse(imgUrlList.get(5).getHeight().isPresent());
    assertFalse(imgUrlList.get(6).getWidth().isPresent());
    assertEquals(500, (int) imgUrlList.get(6).getHeight().get());
    assertEquals(250, (int) imgUrlList.get(7).getWidth().get());
    assertEquals(1000, (int) imgUrlList.get(7).getHeight().get());
    assertEquals(1800, (int) imgUrlList.get(8).getWidth().get());
    assertEquals(400, (int) imgUrlList.get(8).getHeight().get());
    assertEquals(900, (int) imgUrlList.get(9).getWidth().get());
    assertEquals(800, (int) imgUrlList.get(9).getHeight().get());
    assertEquals(400, (int) imgUrlList.get(10).getWidth().get());
    assertEquals(1800, (int) imgUrlList.get(10).getHeight().get());
    assertEquals(2000, (int) imgUrlList.get(11).getWidth().get());
    assertEquals(800, (int) imgUrlList.get(11).getHeight().get());
  }

}
