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
package com.celements.photo.maxImageSize;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.web.Utils;

public class MaxImageSizeServiceTest extends AbstractBridgedComponentTestCase {

  private MaxImageSizeService maxImageSizeService;

  @Before
  public void setUp_MaxImageSizeServiceTest() throws Exception {
    maxImageSizeService = (MaxImageSizeService) Utils.getComponent(IMaxImageSizeServiceRole.class);
  }

  @Test
  public void testGetImgMatcher_noImages() {
    String pageContent = "<div><p>the content</p>\n" + "<p>second paragraph</p></div>";
    replayDefault();
    Matcher imgMatcher = maxImageSizeService.getImgMatcher(pageContent);
    assertFalse("Expect first match", imgMatcher.find());
    verifyDefault();
  }

  @Test
  public void testGetImgMatcher_oneImage() {
    String imgUrl = "/download/testSpace/test/fileName.jpg" + "?celwidth=600&celheight=500";
    String imgPart = "<img id=\"firstImage\" src=\"" + imgUrl + "\" />";
    String pageContent = "<div><p>the content</p>\n" + imgPart + "<p>second paragraph</p></div>";
    replayDefault();
    Matcher imgMatcher = maxImageSizeService.getImgMatcher(pageContent);
    assertTrue("Expect first match", imgMatcher.find());
    assertEquals("Expect complete image match", imgPart, imgMatcher.group(0));
    assertFalse("Expect no second match", imgMatcher.find());
    verifyDefault();
  }

  @Test
  public void testGetImgMatcher_twoImages() {
    String img1Url = "/download/testSpace/test/fileName1.jpg" + "?celwidth=600&celheight=500";
    String img1Part = "<img id=\"firstImage\" src=\"" + img1Url + "\" />";
    String img2Url = "/download/testSpace/test/fileName2.jpg" + "?celwidth=600&celheight=500";
    String img2Part = "<img id=\"secondImage\" src=\"" + img2Url + "\" />";
    String pageContent = "<div><p>the content</p>\n" + img1Part + "<p>second paragraph</p>"
        + img2Part + "</div>";
    replayDefault();
    Matcher imgMatcher = maxImageSizeService.getImgMatcher(pageContent);
    assertTrue("Expect first match", imgMatcher.find());
    assertEquals("Expect complete first image match", img1Part, imgMatcher.group(0));
    assertTrue("Expect second match", imgMatcher.find());
    assertEquals("Expect complete second image match", img2Part, imgMatcher.group(0));
    assertFalse("Expect no third match", imgMatcher.find());
    verifyDefault();
  }

  @Test
  public void testGetImgMatcher_multiline() {
    String img1Url = "/download/testSpace/test/fileName1.jpg" + "?celwidth=600&celheight=500";
    String img1Part = "<img id=\"firstImage\" src=\"" + img1Url + "\" />";
    String img2Url = "/download/testSpace/test/fileName2.jpg" + "?celwidth=600&celheight=500";
    String img2Part = "<img id=\"secondImage\" src=\"" + img2Url + "\" />";
    String pageContent = "<div>\n<p>the content</p>\n" + img1Part + "<p>second paragraph</p>\n"
        + img2Part + "\n</div>";
    replayDefault();
    Matcher imgMatcher = maxImageSizeService.getImgMatcher(pageContent);
    assertTrue("Expect first match", imgMatcher.find());
    assertEquals("Expect complete first image match", img1Part, imgMatcher.group(0));
    assertTrue("Expect second match", imgMatcher.find());
    assertEquals("Expect complete second image match", img2Part, imgMatcher.group(0));
    assertFalse("Expect no third match", imgMatcher.find());
    verifyDefault();
  }

  @Test
  public void testGetImgAttrMatcher_twoAttr() {
    String imgUrl = "/download/testSpace/test/fileName.jpg" + "?celwidth=600&celheight=500";
    String imgAttrPart = "id=\"firstImage\" src=\"" + imgUrl + "\"";
    replayDefault();
    Matcher imgMatcher = maxImageSizeService.getImgAttrMatcher(imgAttrPart);
    assertTrue("Expect first match", imgMatcher.find());
    List<String> allGroups = new ArrayList<String>();
    for (int i = 0; i <= imgMatcher.groupCount(); i++) {
      allGroups.add(imgMatcher.group(i));
    }
    String logStr = " (all groups: " + Arrays.deepToString(allGroups.toArray()) + ")";
    assertEquals("Expect first attribut name" + logStr, "id", imgMatcher.group(1));
    assertEquals("Expect first attribut value" + logStr, "firstImage", imgMatcher.group(2));
    assertEquals("Expect group count of 2", 2, imgMatcher.groupCount());
    assertTrue("Expect second match", imgMatcher.find());
    allGroups = new ArrayList<String>();
    for (int i = 0; i <= imgMatcher.groupCount(); i++) {
      allGroups.add(imgMatcher.group(i));
    }
    logStr = " (all groups: " + Arrays.deepToString(allGroups.toArray()) + ")";
    assertEquals("Expect second attribut name" + logStr, "src", imgMatcher.group(1));
    assertEquals("Expect second attribut value" + logStr, imgUrl, imgMatcher.group(2));
    assertEquals("Expect group count of 2", 2, imgMatcher.groupCount());
    assertFalse("Expect no third match", imgMatcher.find());
    verifyDefault();
  }

  @Test
  public void testGetImageAttrMap_twoAttr() {
    String imgUrl = "/download/testSpace/test/fileName.jpg" + "?celwidth=600&celheight=500";
    String imgAttrPart = "id=\"firstImage\" src=\"" + imgUrl + "\"";
    replayDefault();
    Map<String, String> imgAttrMap = maxImageSizeService.getImageAttrMap(imgAttrPart);
    assertTrue("Expect id in keys", imgAttrMap.containsKey("id"));
    assertTrue("Expect src in keys", imgAttrMap.containsKey("src"));
    assertEquals("Expect two keys", 2, imgAttrMap.keySet().size());
    assertEquals("Expect id value", "firstImage", imgAttrMap.get("id"));
    assertEquals("Expect src value", imgUrl, imgAttrMap.get("src"));
    verifyDefault();
  }

  @Test
  public void testGetAllImagesInSource_noImages() {
    String pageContent = "<div><p>the content</p>\n" + "<p>second paragraph</p></div>";
    replayDefault();
    assertEquals(Collections.emptyList(), maxImageSizeService.getAllImagesInSource(pageContent));
    verifyDefault();
  }

  @Test
  public void testGetAllImagesInSource_oneImage() {
    String basisURL = "/download/testSpace/test/fileName.jpg";
    String urlQueryPart = "?celwidth=600&celheight=500";
    String imgURL = basisURL + urlQueryPart;
    String pageContent = "<div><p>the content</p>\n" + "<img id=\"firstImage\" src=\"" + imgURL
        + "\" />" + "<p>second paragraph</p></div>";
    Map<String, Image> expImages = new HashMap<String, Image>();
    Image firstImage = new Image("firstImage", "firstImage", imgURL);
    expImages.put(firstImage.getId(), firstImage);
    replayDefault();
    List<Image> allImagesInSource = maxImageSizeService.getAllImagesInSource(pageContent);
    assertEquals(1, allImagesInSource.size());
    for (Image theImage : allImagesInSource) {
      Image expectedImage = expImages.get(theImage.getId());
      assertEquals(expectedImage.getId(), theImage.getId());
      assertEquals(expectedImage.getURL(), theImage.getURL());
      assertEquals(expectedImage.getMaxHeight(), theImage.getMaxHeight());
      assertEquals(expectedImage.getMaxWidth(), theImage.getMaxWidth());
      assertEquals(basisURL, theImage.getBasisURL());
    }
    verifyDefault();
  }

  @Test
  public void testGetAllImagesInSource_oneImage_noParamsInSrc() {
    String basisURL = "/download/testSpace/test/fileName.jpg";
    String pageContent = "<div><p>the content</p>\n" + "<img id=\"firstImage\" src=\"" + basisURL
        + "\" />" + "<p>second paragraph</p></div>";
    Map<String, Image> expImages = new HashMap<String, Image>();
    Image firstImage = new Image("firstImage", "firstImage",
        "/download/testSpace/test/fileName.jpg");
    expImages.put(firstImage.getId(), firstImage);
    replayDefault();
    List<Image> allImagesInSource = maxImageSizeService.getAllImagesInSource(pageContent);
    assertEquals(1, allImagesInSource.size());
    for (Image theImage : allImagesInSource) {
      Image expectedImage = expImages.get(theImage.getId());
      assertEquals(expectedImage.getId(), theImage.getId());
      assertEquals(expectedImage.getURL(), theImage.getURL());
      assertEquals(expectedImage.getMaxHeight(), theImage.getMaxHeight());
      assertEquals(expectedImage.getMaxWidth(), theImage.getMaxWidth());
      assertEquals(basisURL, theImage.getBasisURL());
    }
    verifyDefault();
  }

}
