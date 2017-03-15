package com.celements.photo.container;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;

@Immutable
public class ImageUrlDim {

  private final String url;
  private final int width;
  private final int height;

  public ImageUrlDim(String url, int width, int height) {
    this.url = url;
    this.width = width;
    this.height = height;
  }

  public String getUrl() {
    return url;
  }

  public Optional<String> getWidth() {
    if (width > 0) {
      return Optional.of(Integer.toString(width));
    }
    return Optional.absent();
  }

  public Optional<String> getHeight() {
    if (height > 0) {
      return Optional.of(Integer.toString(height));
    }
    return Optional.absent();
  }

  @Override
  public String toString() {
    return getUrl();
  }
}
