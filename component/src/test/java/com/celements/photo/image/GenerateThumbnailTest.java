package com.celements.photo.image;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.imaging.ImageReadException;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.photo.container.ImageDimensions;
import com.celements.photo.plugin.cmd.DecodeImageCommand;
import com.xpn.xwiki.XWikiException;

public class GenerateThumbnailTest extends AbstractComponentTest {

  private GenerateThumbnail genThum;

  @Before
  public void setUp_GenerateThumbnailTest() throws Exception {
    genThum = new GenerateThumbnail();
  }

  @Test
  public void testGetThumbnailDimensions() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496, false, null);
    assertEquals(498, imgDim.getSize().width);
    assertEquals(248, imgDim.getSize().height);
    imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496, true, null);
    assertEquals(498, imgDim.getSize().width);
    assertEquals(248, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_getWidth() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496, false, null);
    assertEquals(498, (int) imgDim.getWidth());
    assertEquals(248, (int) imgDim.getHeight());
  }

  @Test
  public void testGetThumbnailDimensions_aspectRatio() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 496, 497, 247, false, null);
    assertEquals(496, imgDim.getSize().width);
    assertEquals(247, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_lowerBoundsMax() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 496, 497, 247, true, null);
    assertEquals(497, imgDim.getSize().width);
    assertEquals(247, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_lowerBoundsImg() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(150, 200, 497, 247, true, null);
    assertEquals(150, imgDim.getSize().width);
    assertEquals(200, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_lowerBoundsMix() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(320, 640, 995, 496, true, null);
    assertEquals(320, imgDim.getSize().width);
    assertEquals(496, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_lowerBoundsMixReverse() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 200, 497, 247, true, null);
    assertEquals(497, imgDim.getSize().width);
    assertEquals(200, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_withBg() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 200, 497, 247, true, new Color(0));
    assertEquals(497, imgDim.getSize().width);
    assertEquals(247, imgDim.getSize().height);
  }

  @Test
  public void testCreateThumbnail() throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream("Home.Home2.jpg");
    BufferedImage img = genThum.decodeInputStream(in);
    in.close();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    genThum.createThumbnail(img, out, new ImageDimensions(500, 247), null, null, "PNG", null, false,
        null, null);
    BufferedImage outImg = genThum.decodeInputStream(new ByteArrayInputStream(out.toByteArray()));
    out.close();
    assertEquals(495, outImg.getWidth(null));
    assertEquals(247, outImg.getHeight(null));
  }

  @Test
  public void test_decodeImage_ImageReadException() throws Exception {
    var is = new ByteArrayInputStream(new byte[0]);
    var cmdMock = createDefaultMock(DecodeImageCommand.class);
    expect(cmdMock.readImage(same(is), eq(""), same(null))).andThrow(new ImageReadException(""));
    replayDefault();
    assertThrows(XWikiException.class, () -> genThum.decodeImage(is, cmdMock));
    verifyDefault();
  }

}
