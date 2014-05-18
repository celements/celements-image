package com.celements.photo.maxImageSize;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ImageTest {

  private static final Integer IMAGE_MAX_WIDTH = 600;
  private static final Integer IMAGE_MAX_HEIGHT = 500;
  private static final String IMAGE_URL_QUERY_PART = "?celwidth=" + IMAGE_MAX_WIDTH
      + "&celheight=" + IMAGE_MAX_HEIGHT;
  private static final String IMAGE_BASIS_URL = "/download/testSpace/test/fileName.jpg";
  private static final String IMAGE_ORIG_URL = IMAGE_BASIS_URL + IMAGE_URL_QUERY_PART;
  private static final String IMAGE_NAME = "firstImageName";
  private static final String IMAGE_ID = "firstImageId";

  private Image testImage;

  @Before
  public void setUp() throws Exception {
    testImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_ORIG_URL);
  }

  @Test
  public void testGetId() {
    assertEquals(IMAGE_ID, testImage.getId());
  }

  @Test
  public void testGetName() {
    assertEquals(IMAGE_NAME, testImage.getName());
  }

  @Test
  public void testGetURL() {
    assertEquals(IMAGE_ORIG_URL, testImage.getURL());
  }

  @Test
  public void testSet_Get_ThumbURL() {
    assertNull(testImage.getThumbURL());
    String thumbURL = "thumbURL";
    testImage.setThumbURL(thumbURL);
    assertEquals(thumbURL, testImage.getThumbURL());
  }

  @Test
  public void testGetBasisURL() {
    assertEquals(IMAGE_BASIS_URL, testImage.getBasisURL());
  }

  @Test
  public void testGetMaxWidth_noCelWith() {
    testImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_BASIS_URL);
    assertNull(testImage.getMaxWidth());
  }

  @Test
  public void testGetMaxWidth() {
    assertEquals(IMAGE_MAX_WIDTH, testImage.getMaxWidth());
  }

  @Test
  public void testSetMaxWidth() {
    Integer newMaxWidth = IMAGE_MAX_WIDTH + 100;
    testImage.setMaxWidth(newMaxWidth);
    assertEquals(newMaxWidth, testImage.getMaxWidth());
    assertEquals(IMAGE_BASIS_URL + "?celwidth=" + newMaxWidth + "&celheight="
        + IMAGE_MAX_HEIGHT, testImage.getURL());
  }

  @Test
  public void testSetMaxWidth_noQueryPart() {
    Integer newMaxWidth = IMAGE_MAX_WIDTH + 100;
    testImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_BASIS_URL);
    testImage.setMaxWidth(newMaxWidth);
    assertEquals(newMaxWidth, testImage.getMaxWidth());
    assertEquals(IMAGE_BASIS_URL + "?celwidth=" + newMaxWidth, testImage.getURL());
  }

  @Test
  public void testSetMaxWidth_noCelWidth() {
    Integer newMaxWidth = IMAGE_MAX_WIDTH + 100;
    testImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_BASIS_URL + "?celheight="
        + IMAGE_MAX_HEIGHT);
    testImage.setMaxWidth(newMaxWidth);
    assertEquals(newMaxWidth, testImage.getMaxWidth());
    assertEquals(IMAGE_BASIS_URL + "?celheight=" + IMAGE_MAX_HEIGHT + "&celwidth="
        + newMaxWidth, testImage.getURL());
  }

  @Test
  public void testGetMaxHeight_noCelWith() {
    testImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_BASIS_URL);
    assertNull(testImage.getMaxHeight());
  }

  @Test
  public void testGetMaxHeight() {
    assertEquals(IMAGE_MAX_HEIGHT, testImage.getMaxHeight());
  }

  @Test
  public void testSetMaxHeight() {
    Integer newMaxHeight = IMAGE_MAX_HEIGHT + 100;
    testImage.setMaxHeight(newMaxHeight);
    assertEquals(newMaxHeight, testImage.getMaxHeight());
    assertEquals(IMAGE_BASIS_URL + "?celwidth=" + IMAGE_MAX_WIDTH + "&celheight="
        + newMaxHeight, testImage.getURL());
  }

  @Test
  public void testSetMaxHeight_noQueryPart() {
    Integer newMaxHeight = IMAGE_MAX_HEIGHT + 100;
    testImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_BASIS_URL);
    testImage.setMaxHeight(newMaxHeight);
    assertEquals(newMaxHeight, testImage.getMaxHeight());
    assertEquals(IMAGE_BASIS_URL + "?celheight=" + newMaxHeight, testImage.getURL());
  }

  @Test
  public void testSetMaxHeight_noCelHeight() {
    Integer newMaxHeight = IMAGE_MAX_HEIGHT + 100;
    testImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_BASIS_URL + "?celwidth="
        + IMAGE_MAX_WIDTH);
    testImage.setMaxHeight(newMaxHeight);
    assertEquals(newMaxHeight, testImage.getMaxHeight());
    assertEquals(IMAGE_BASIS_URL + "?celwidth=" + IMAGE_MAX_WIDTH + "&celheight="
        + newMaxHeight, testImage.getURL());
  }

  @Test
  public void testGetJSON() {
    String expectedJSON = "{\"id\" : \"" + IMAGE_ID + "\", \"name\" : \""
        + IMAGE_NAME + "\", \"URL\" : \"" + IMAGE_ORIG_URL + "\", \"thumbURL\" : null}";
    assertEquals(expectedJSON, testImage.getJSON());
  }

  @Test
  public void testGetJSON_includeThumb() {
    String thumbURL = "thumbURL";
    testImage.setThumbURL(thumbURL);
    String expectedJSON = "{\"id\" : \"" + IMAGE_ID + "\", \"name\" : \""
        + IMAGE_NAME + "\", \"URL\" : \"" + IMAGE_ORIG_URL + "\", \"thumbURL\" : \""
        + thumbURL + "\"}";
    assertEquals(expectedJSON, testImage.getJSON());
  }

  @Test
  public void testImageStringStringString() {
    assertEquals(IMAGE_ID, testImage.getId());
    assertEquals(IMAGE_NAME, testImage.getName());
    assertEquals(IMAGE_ORIG_URL, testImage.getURL());
    assertNull(testImage.getThumbURL());
  }

  @Test
  public void testImageStringStringStringString() {
    String thumbURL = "thumbURL";
    Image testThumbImage = new Image(IMAGE_ID, IMAGE_NAME, IMAGE_ORIG_URL, thumbURL);
    assertEquals(IMAGE_ID, testThumbImage.getId());
    assertEquals(IMAGE_NAME, testThumbImage.getName());
    assertEquals(IMAGE_ORIG_URL, testThumbImage.getURL());
    assertEquals(thumbURL, testThumbImage.getThumbURL());
  }

}
