package com.celements.photo.container;

import java.awt.Dimension;

/**
 * Container class. Stores two integers, representing width and height of an
 * image.
 */
public class ImageDimensions extends Dimension{
  private static final long serialVersionUID = 1L;

  /**
   * Initialises the Dimension to 0.
   */
  public ImageDimensions(){
    super(0, 0);
  }
  
  /**
   * Initialises the two values.
   * 
   * @param width An integer, representing the width.
   * @param height An integer, representing the height.
   */
  public ImageDimensions(int width, int height) {
    super(width, height);
  } 
  
  /**
   * Chech if there are width and height. Both are 0 if there is no 
   * thumbnail.
   * 
   * @return true if width and height are 0.
   */
  public boolean isEmpty(){
    if(width + height == 0){
      return true;
    }
    return false;
  }
}
