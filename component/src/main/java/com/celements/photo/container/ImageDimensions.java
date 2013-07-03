/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
    return width + height == 0;
  }
}
