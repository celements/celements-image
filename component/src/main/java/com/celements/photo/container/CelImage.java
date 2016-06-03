package com.celements.photo.container;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.metadata.IIOMetadata;

public class CelImage {

  List<CelImageInternal> images;

  public void addImage(BufferedImage image, IIOMetadata metadata) {
    if (images == null) {
      images = new ArrayList<CelImageInternal>();
    }
    CelImageInternal internalImg = new CelImageInternal();
    internalImg.setImage(image);
    internalImg.setMetaData(metadata);
    images.add(internalImg);
  }

  public boolean isEmpty() {
    return (images == null) || images.isEmpty();
  }

  public BufferedImage getImage(int pos) {
    return images.get(pos).getImage();
  }

  public BufferedImage getFirstImage() {
    return getImage(0);
  }

  public IIOMetadata getMetadata(int pos) {
    return images.get(pos).getMetaData();
  }

  public IIOMetadata getFirstMetadata() {
    return getMetadata(0);
  }

  private class CelImageInternal {

    BufferedImage image;
    IIOMetadata metaData;

    public BufferedImage getImage() {
      return image;
    }

    public void setImage(BufferedImage image) {
      this.image = image;
    }

    public IIOMetadata getMetaData() {
      return metaData;
    }

    public void setMetaData(IIOMetadata metaData) {
      this.metaData = metaData;
    }
  }
}
