package com.celements.photo.container.imageurl.helpers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.photo.container.ImageUrl;

public class AreaWithLimitsAndDefaultTest extends AbstractComponentTest {

  AreaWithLimitsAndDefault predicate;

  @Before
  public void prepareTest() throws Exception {
    predicate = new AreaWithLimitsAndDefault();
  }

  @Test
  public void testGetAreaKey_nosize() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url("/download/images/imgurl/test.jpg").build();
    assertFalse(predicate.getAreaKey(imgUrl).isPresent());
  }

  @Test
  public void testGetAreaKey_onlyWidth_smallerMin() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?test=bla&celwidth=3&celheight=").build();
    assertEquals(3 * 3L, (long) predicate.getAreaKey(imgUrl).get());
  }

  @Test
  public void testGetAreaKey_onlyWidth() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celwidth=1000").build();
    assertEquals(1000 * 1000L, (long) predicate.getAreaKey(imgUrl).get());
  }

  @Test
  public void testGetAreaKey_onlyHeight_smallerMin() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celheight=3&celwidth&").build();
    assertEquals(3 * 3L, (long) predicate.getAreaKey(imgUrl).get());
  }

  @Test
  public void testGetAreaKey_onlyHeight() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celheight=1000&celwidth").build();
    assertEquals(1000 * 1000L, (long) predicate.getAreaKey(imgUrl).get());
  }

  @Test
  public void testGetAreaKey_both_productSmallerMin() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celwidth=2&celheight=500&test=999999").build();
    assertEquals(2 * 500L, (long) predicate.getAreaKey(imgUrl).get());
  }

  @Test
  public void testGetAreaKey_both() throws Exception {
    ImageUrl imgUrl = new ImageUrl.Builder().url(
        "/download/images/imgurl/test.jpg?celwidth=1000&celheight=800").build();
    assertEquals(1000 * 800L, (long) predicate.getAreaKey(imgUrl).get());
  }

  @Test
  public void testGetAreaKey_maxSize() throws Exception {
    long max = AreaWithLimitsAndDefault.MAX_ALLOWED_DIM;
    ImageUrl imgUrl = new ImageUrl.Builder().url("/download/images/imgurl/test.jpg?celwidth=" + max
        + "&celheight=" + max).build();
    assertEquals(max * max, (long) predicate.getAreaKey(imgUrl).get());
  }

  @Test
  public void testGetAreaKey_exceedsMaxSize() throws Exception {
    long max = AreaWithLimitsAndDefault.MAX_ALLOWED_DIM;
    ImageUrl imgUrl = new ImageUrl.Builder().url("/download/images/imgurl/test.jpg?celwidth=" + (max
        + 101) + "&celheight=" + (max + 202)).build();
    assertEquals(max * max, (long) predicate.getAreaKey(imgUrl).get());
  }

}
