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
package com.celements.photo.maxImageSize;

import com.celements.sajson.Builder;

/**
 * Container used to simplify the velocity access. Contains the name of the
 * image, its URL and the URL to its thumbnail.
 */
public class Image {

  private String id;
  private String name;
  private String thumb;
  private Integer maxWidth;
  private Integer maxHeight;
  private String origURL;
  private String imgURL;
  private String basisURL;

  /**
   * Initialises image's id, name, URL.
   * 
   * @param id
   *          Id of the image.
   * @param name
   *          Name of the image.
   * @param origURL
   *          URL to the image.
   */
  public Image(String id, String name, String origURL) {
    this.id = id;
    this.name = name;
    this.origURL = origURL;
    this.imgURL = origURL;
  }

  /**
   * Initialises image's id, name, URL and thumbnail URL.
   * 
   * @param id
   *          Id of the image.
   * @param name
   *          Name of the image.
   * @param url
   *          URL to the image.
   * @param thumb
   *          URL to the thumbnail of the image.
   */
  public Image(String id, String name, String url, String thumb) {
    this.id = id;
    this.name = name;
    this.origURL = url;
    this.imgURL = url;
    this.thumb = thumb;
  }

  /**
   * Get the id of the image. The id is a unique identifier String with a
   * length of 64 character.
   * 
   * @return id of the image.
   */
  public String getId() {
    return id;
  }

  /**
   * Get the name of the image.
   * 
   * @return Name of the image.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the URL to the thumbnail.
   * 
   * @return URL to the thumbnail.
   */
  public String getThumbURL() {
    return thumb;
  }

  /**
   * Get the URL to the image.
   * 
   * @return URL to the image.
   */
  public String getURL() {
    return imgURL;
  }

  /**
   * Set the URL to the thumbnail
   * 
   * @param thumb
   *          URL to the thumbnail
   */
  public void setThumbURL(String thumb) {
    this.thumb = thumb;
  }

  public void setMaxWidth(Integer maxWidth) {
    this.maxWidth = maxWidth;
    if (maxWidth != null) {
      String newMaxWidthStr = "celwidth=" + this.maxWidth;
      if (imgURL.matches("^.*\\?.*celwidth=(\\d*).*$")) {
        imgURL = imgURL.replaceAll("celwidth=(\\d*)", newMaxWidthStr);
      } else {
        if (imgURL.indexOf("?") < 0) {
          imgURL = imgURL + "?";
        } else {
          imgURL = imgURL + "&";
        }
        imgURL = imgURL + newMaxWidthStr;
      }
    } else {
      imgURL = imgURL.replaceAll("celwidth=(\\d*)&?", "");
      if (imgURL.endsWith("&")) {
        imgURL = imgURL.replaceAll("&+$", "");
      }
    }
  }

  public Integer getMaxWidth() {
    if (this.maxWidth == null) {
      if (getURL().matches("^.*celwidth=(\\d+).*$")) {
        maxWidth = Integer.parseInt(getURL().replaceAll("^.*celwidth=(\\d+).*$", "$1"));
      }
    }
    return this.maxWidth;
  }

  public void setMaxHeight(Integer maxHeight) {
    this.maxHeight = maxHeight;
    if (maxHeight != null) {
      String newMaxHeightStr = "celheight=" + this.maxHeight;
      if (imgURL.matches("^.*\\?.*celheight=(\\d*).*$")) {
        imgURL = imgURL.replaceAll("celheight=(\\d*)", newMaxHeightStr);
      } else {
        if (imgURL.indexOf("?") < 0) {
          imgURL = imgURL + "?";
        } else {
          imgURL = imgURL + "&";
        }
        imgURL = imgURL + newMaxHeightStr;
      }
    } else {
      imgURL = imgURL.replaceAll("celheight=(\\d*)&?", "");
      if (imgURL.endsWith("&")) {
        imgURL = imgURL.replaceAll("&+$", "");
      }
    }
  }

  public Integer getMaxHeight() {
    if (this.maxHeight == null) {
      if (getURL().matches("^.*celheight=(\\d+).*$")) {
        maxHeight = Integer.parseInt(getURL().replaceAll("^.*celheight=(\\d+).*$", "$1"));
      }
    }
    return this.maxHeight;
  }

  /**
   * Get the JSON for the image
   * 
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

  public String getBasisURL() {
    if (basisURL == null) {
      basisURL = this.imgURL.split("\\?")[0];
    }
    return basisURL;
  }

  public String getOrigURL() {
    return this.origURL;
  }

}
