package com.celements.photo.container;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import com.celements.common.test.AbstractComponentTest;

public class CelImageTest extends AbstractComponentTest {

  CelImage image;

  @Before
  public void setUp_CelImageTest() throws Exception {
    image = new CelImage();
  }

  @Test
  public void testIsEmpty() {
    assertTrue(image.isEmpty());
    image.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), getNewIIOMetadata());
    assertFalse(image.isEmpty());
  }

  @Test
  public void testGetImage() {
    BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    IIOMetadata metadata = getNewIIOMetadata();
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    assertNotNull(image.getImage(1));
    assertNotNull(image.getImage(4));
  }

  @Test
  public void testGetFirstImage() {
    BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    image.addImage(bufferedImage, getNewIIOMetadata());
    assertSame(bufferedImage, image.getFirstImage());
  }

  @Test
  public void testGetMetadata() {
    BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    IIOMetadata metadata = getNewIIOMetadata();
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    image.addImage(bufferedImage, metadata);
    assertNotNull(image.getMetadata(2));
    assertNotNull(image.getMetadata(4));
  }

  @Test
  public void testGetFirstMetadata() {
    IIOMetadata metadata = getNewIIOMetadata();
    image.addImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), metadata);
    assertSame(metadata, image.getFirstMetadata());
  }

  @Test
  public void testAddImage() {
    assertTrue(image.isEmpty());
    BufferedImage bufferedImage1 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    IIOMetadata metadata1 = getNewIIOMetadata();
    image.addImage(bufferedImage1, metadata1);
    BufferedImage bufferedImage2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    IIOMetadata metadata2 = getNewIIOMetadata();
    image.addImage(bufferedImage2, metadata2);
    assertFalse(image.isEmpty());
    assertSame(bufferedImage1, image.getFirstImage());
    assertSame(metadata1, image.getFirstMetadata());
    assertSame(bufferedImage2, image.getImage(1));
    assertSame(metadata2, image.getMetadata(1));
  }

  private IIOMetadata getNewIIOMetadata() {
    return new IIOMetadata() {

      @Override
      public void reset() {
      }

      @Override
      public void mergeTree(String formatName, Node root) throws IIOInvalidTreeException {
      }

      @Override
      public boolean isReadOnly() {
        return true;
      }

      @Override
      public Node getAsTree(String formatName) {
        return null;
      }
    };
  }

}
