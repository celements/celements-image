package com.celements.photo.container;

public class ImageUrlDim {

  private String url;
  private String width = "";
  private String height = "";

  public ImageUrlDim(String url, int width, int height) {
    this.url = url;
    if (width > 0) {
      this.width = Integer.toString(width);
    }
    if (height > 0) {
      this.height = Integer.toString(height);
    }
  }

  public String getUrl() {
    return url;
  }

  public String getWidth() {
    return width;
  }

  public String getHeight() {
    return height;
  }

  @Override
  public String toString() {
    return getUrl();
  }
}
