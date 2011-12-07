package com.celements.photo.image;


import static org.junit.Assert.*;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.celements.photo.container.ImageDimensions;

public class GenerateThumbnailTest {

  private GenerateThumbnail genThum;

  @Before
  public void setUp() throws Exception {
    genThum = new GenerateThumbnail();
  }

  @Test
  public void testGetThumbnailDimensions() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496);
    assertEquals(498, imgDim.getSize().width);
    assertEquals(248, imgDim.getSize().height);
  }

  @Test
  public void testGetThumbnailDimensions_getWidth() {
    ImageDimensions imgDim = genThum.getThumbnailDimensions(498, 248, 995, 496);
    assertEquals(498, (int)imgDim.getWidth());
    assertEquals(248, (int)imgDim.getHeight());
  }
  
//  @Test
//  public void testGetThumbnailDimensions_aspectRatio() {
//    ImageDimensions imgDim = genThum.getThumbnailDimensions(515, 248, 995, 496);
//    assertEquals(498, imgDim.getSize().width);
//    assertEquals(248, imgDim.getSize().height);
//  }

  @Test
  public void testCreateThumbnail() throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream("Home.Home2.jpg");
    BufferedImage img = genThum.decodeImage(in);
    in.close();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    genThum.createThumbnail(img, out, new ImageDimensions(498, 248), null, null, "PNG",
        null);
    BufferedImage outImg = genThum.decodeImage(new ByteArrayInputStream(out.toByteArray(
        )));
    out.close();
    assertEquals(498, outImg.getWidth(null));
    assertEquals(248, outImg.getHeight(null));
  }

}
