package com.celements.photo.utilities;

public class FileNameManipulator {
  /**
   * Generates a name for the thumbnails. These consist of 
   * <width>x<height>.<fileending>
   * 
   * @param fileName The name of the image, used to get the extention.
   * @return The file name containing the size.
   */
  public String addSizeToFileName(String fileName, int width, int height){
    String fileEnding = ".jpg";
    
    if(fileName.lastIndexOf(".") > 0){
      fileEnding = fileName.substring(fileName.lastIndexOf("."), fileName.length());
      fileName = fileName.substring(0, fileName.lastIndexOf("."));
    }
    
    // size is enaugh as distinction ... no name is needed anymore
    return /*fileName + ImageLibStrings.DOCUMENT_SEPARATOR +*/ width + "x" + height + fileEnding;
  }
}
