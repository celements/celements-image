package com.celements.photo.exception;

public class IllegalImageUrlException extends Exception {

  private static final long serialVersionUID = 4509586794718639006L;

  private final String url;

  public IllegalImageUrlException(String url) {
    super(url);
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
