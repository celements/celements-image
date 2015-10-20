package com.celements.photo.image;


import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.photo.container.ImageDimensions;

public class GenerateThumbnailTest extends AbstractBridgedComponentTestCase{

  private GenerateThumbnail genThum;

  @Before
  public void setUp_GenerateThumbnailTest() throws Exception {
    genThum = new GenerateThumbnail();
  }

  @Test
  public void testGetThumbnailDimensions() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496, false, 
        null);
    assertEquals(498, imgDim.getSize().width);
    assertEquals(248, imgDim.getSize().height);
    imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496, true, null);
    assertEquals(498, imgDim.getSize().width);
    assertEquals(248, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_getWidth() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496, false, 
        null);
    assertEquals(498, (int)imgDim.getWidth());
    assertEquals(248, (int)imgDim.getHeight());
  }
  
  @Test
  public void testGetThumbnailDimensions_aspectRatio() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 496, 497, 247, false, 
        null);
    assertEquals(496, imgDim.getSize().width);
    assertEquals(247, imgDim.getSize().height);
  }
  
  @Test
  public void testGetThumbnailDimensions_lowerBoundsMax() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 496, 497, 247, true, 
        null);
    assertEquals(497, imgDim.getSize().width);
    assertEquals(247, imgDim.getSize().height);
  }
  
  @Test
  public void testGetThumbnailDimensions_lowerBoundsImg() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(150, 200, 497, 247, true, 
        null);
    assertEquals(150, imgDim.getSize().width);
    assertEquals(200, imgDim.getSize().height);
  }
  
  @Test
  public void testGetThumbnailDimensions_lowerBoundsMix() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(320, 640, 995, 496, true, 
        null);
    assertEquals(320, imgDim.getSize().width);
    assertEquals(496, imgDim.getSize().height);
  }
  
  @Test
  public void testGetThumbnailDimensions_lowerBoundsMixReverse() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 200, 497, 247, true, 
        null);
    assertEquals(497, imgDim.getSize().width);
    assertEquals(200, imgDim.getSize().height);
  }
  
  @Test
  public void testGetThumbnailDimensions_withBg() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(995, 200, 497, 247, true, 
        new Color(0));
    assertEquals(497, imgDim.getSize().width);
    assertEquals(247, imgDim.getSize().height);
  }

  @Test
  public void testCreateThumbnail() throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream("Home.Home2.jpg");
    BufferedImage img = genThum.decodeInputStream(in);
    in.close();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    genThum.createThumbnail(img, out, new ImageDimensions(500, 247), null, null, "PNG",
        null, false, null);
    BufferedImage outImg = genThum.decodeInputStream(new ByteArrayInputStream(
        out.toByteArray()));
    out.close();
    assertEquals(495, outImg.getWidth(null));
    assertEquals(247, outImg.getHeight(null));
  }

}
