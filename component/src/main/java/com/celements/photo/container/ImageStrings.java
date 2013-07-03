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

import com.celements.sajson.Builder;

/**
 * Container used to simplify the velocity access. Contains the name of the 
 * image, its URL and the URL to its thumbnail.
 */
public class ImageStrings {
  private String id;
  private String name;
  private String url;
  private String thumb;
  
  @SuppressWarnings("unused")
  private ImageStrings(){}
  
  /**
   * Initialises image's id, name, URL and thumbnail URL.
   * @param id Id of the image. 
   * @param name Name of the image.
   * @param url URL to the image.
   * @param thumb URL to the thumbnail of the image.
   */
  public ImageStrings(String id, String name, String url, String thumb){
    this.id = id;
    this.name = name;
    this.url = url;
    this.thumb = thumb;
  }
  
  /**
   * Get the id of the image. The id is a unique identifier String with a
   * length of 64 character.
   * @return id of the image.
   */
  public String getId(){
    return id;
  }

  /**
   * Get the name of the image.
   * @return Name of the image.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the URL to the thumbnail.
   * @return URL to the thumbnail.
   */
  public String getThumbURL() {
    return thumb;
  }

  /**
   * Get the URL to the image.
   * @return URL to the image.
   */
  public String getURL() {
    return url;
  }

  /**
   * Set the URL to the thumbnail
   * @param thumb URL to the thumbnail
   */
  public void setThumbURL(String thumb) {
    this.thumb = thumb;
  }
  
  /**
   * Get the JSON for the image
   * @return URL to the image.
   */
  public String getJSON() {
    Builder jsonBuilder = new Builder();
    jsonBuilder.openDictionary();
    jsonBuilder.addStringProperty("id", getId());
    jsonBuilder.addStringProperty("name", getName());
    jsonBuilder.addStringProperty("URL", getURL());
    jsonBuilder.addStringProperty("thumbURL", getThumbURL());
    jsonBuilder.closeDictionary();
    return jsonBuilder.getJSON();
  }
}
