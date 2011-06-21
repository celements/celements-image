package com.celements.photo.container;

public class ZipArchiveStrings {
  private String zipFilename;
  private String zipDocname;
  
  public ZipArchiveStrings(String zipFilename, String zipDocname){
    this.zipFilename = zipFilename;
    this.zipDocname = zipDocname;
  }
  
  public String getZipDocname() {
    return zipDocname;
  }
  
  public void setZipDocname(String zipDocname) {
    this.zipDocname = zipDocname;
  }
  
  public String getZipFilename() {
    return zipFilename;
  }
  
  public void setZipFilename(String zipFilename) {
    this.zipFilename = zipFilename;
  }
}
