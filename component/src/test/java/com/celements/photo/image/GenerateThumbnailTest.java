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
  public void testCreateThumbnail_opaqueRgbResizeProducesPngWithoutAlpha() throws Exception {
    BufferedImage source = new BufferedImage(4, 2, BufferedImage.TYPE_INT_RGB);
    int sourceRgb = new Color(12, 34, 56).getRGB();
    for (int x = 0; x < source.getWidth(); x++) {
      for (int y = 0; y < source.getHeight(); y++) {
        source.setRGB(x, y, sourceRgb);
      }
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    genThum.createThumbnail(source, out, new ImageDimensions(2, 1), null, null, "image/jpeg", null,
        false, null, null);

    BufferedImage result = genThum.decodeInputStream(new ByteArrayInputStream(out.toByteArray()));
    assertFalse(result.getColorModel().hasAlpha());
    assertEquals(2, result.getWidth());
    assertEquals(1, result.getHeight());
    assertEquals(sourceRgb, result.getRGB(0, 0));
    assertEquals(sourceRgb, result.getRGB(1, 0));
  }

  @Test
  public void testCreateThumbnail_transparentSourcePreservesAlphaWithoutBackground()
      throws Exception {
    BufferedImage source = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(0, 0, new Color(10, 20, 30, 0).getRGB());
    source.setRGB(1, 0, new Color(40, 50, 60, 128).getRGB());
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    genThum.createThumbnail(source, out, new ImageDimensions(2, 1), null, null, "image/png", null,
        false, null, null);

    BufferedImage result = genThum.decodeInputStream(new ByteArrayInputStream(out.toByteArray()));
    assertTrue(result.getColorModel().hasAlpha());
    assertEquals(0, new Color(result.getRGB(0, 0), true).getAlpha());
    assertEquals(128, new Color(result.getRGB(1, 0), true).getAlpha());
  }

  @Test
  public void testCreateThumbnail_translucentBackgroundPreservesPaddingAlpha() throws Exception {
    BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
    Color sourceColor = new Color(10, 20, 30);
    for (int x = 0; x < source.getWidth(); x++) {
      for (int y = 0; y < source.getHeight(); y++) {
        source.setRGB(x, y, sourceColor.getRGB());
      }
    }
    Color background = new Color(40, 50, 60, 64);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    genThum.createThumbnail(source, out, new ImageDimensions(4, 4), null, null, "image/png",
        background, false, null, null);

    BufferedImage result = genThum.decodeInputStream(new ByteArrayInputStream(out.toByteArray()));
    assertTrue(result.getColorModel().hasAlpha());
    assertEquals(64, new Color(result.getRGB(0, 0), true).getAlpha());
    assertEquals(255, new Color(result.getRGB(1, 1), true).getAlpha());
  }

  @Test
  public void testCreateThumbnail_opaqueBackgroundFlattensTransparentSource() throws Exception {
    BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(0, 0, new Color(255, 0, 0, 0).getRGB());
    Color background = new Color(40, 50, 60);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    genThum.createThumbnail(source, out, new ImageDimensions(4, 4), null, null, "image/png",
        background, false, null, null);

    BufferedImage result = genThum.decodeInputStream(new ByteArrayInputStream(out.toByteArray()));
    assertFalse(result.getColorModel().hasAlpha());
    assertEquals(background.getRGB(), result.getRGB(0, 0));
    assertEquals(background.getRGB(), result.getRGB(1, 1));
  }

  @Test
  public void testCreateThumbnail_lowerBoundOpaqueBackgroundFlattensTransparentSource()
      throws Exception {
    BufferedImage source = new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB);
    Color sourceColor = new Color(200, 100, 50);
    source.setRGB(2, 0, sourceColor.getRGB());
    source.setRGB(2, 1, sourceColor.getRGB());
    Color background = new Color(40, 50, 60);
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    genThum.createThumbnail(source, out, new ImageDimensions(2, 2), null, null, "image/png",
        background, true, null, null);

    BufferedImage result = genThum.decodeInputStream(new ByteArrayInputStream(out.toByteArray()));
    assertFalse(result.getColorModel().hasAlpha());
    assertEquals(2, result.getWidth());
    assertEquals(2, result.getHeight());
    assertEquals(background.getRGB(), result.getRGB(0, 0));
    assertEquals(sourceColor.getRGB(), result.getRGB(1, 0));
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
