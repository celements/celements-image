package com.celements.photo.utilities;

public class ImportFileObject {
  public static final short ACTION_SKIP = -1;
  public static final short ACTION_OVERWRITE = 0;
  public static final short ACTION_ADD = 1;
  
  private String filename;
  private short action;
  
  public ImportFileObject(String filename, short action){
    this.filename = filename;
    this.action = action;
  }
  
  public String getFilename() {
    return filename;
  }
  
  public short getAction() {
    return action;
  }
}
