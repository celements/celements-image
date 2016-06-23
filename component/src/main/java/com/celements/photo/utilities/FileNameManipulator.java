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
package com.celements.photo.utilities;

public class FileNameManipulator {

  /**
   * Generates a name for the thumbnails. These consist of
   * <width>x<height>.<fileending>
   * 
   * @param fileName
   *          The name of the image, used to get the extention.
   * @return The file name containing the size.
   */
  public String addSizeToFileName(String fileName, int width, int height) {
    String fileEnding = ".jpg";

    if (fileName.lastIndexOf(".") > 0) {
      fileEnding = fileName.substring(fileName.lastIndexOf("."), fileName.length());
      fileName = fileName.substring(0, fileName.lastIndexOf("."));
    }

    // size is enaugh as distinction ... no name is needed anymore
    return /* fileName + ImageLibStrings.DOCUMENT_SEPARATOR + */ width + "x" + height + fileEnding;
  }
}
