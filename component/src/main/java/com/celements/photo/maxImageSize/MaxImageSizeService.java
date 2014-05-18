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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.component.annotation.Component;

@Component
public class MaxImageSizeService implements IMaxImageSizeServiceRole {

  private static final String IMG_ATTR_REGEX = "(\\w+)=[\"'](\\S*)[\"']";
  Pattern imgPattern = Pattern.compile("<img(?: " + IMG_ATTR_REGEX + ")+\\s*/>",
      Pattern.MULTILINE);
  Pattern imgAttrPattern = Pattern.compile(IMG_ATTR_REGEX);

  public String fixMaxImageSizes(String pageContent, int maxWidth, int maxHeight) {
    List<Image> allImages = getAllImagesInSource(pageContent);
    //TODO get greatest resize factor over all images
    //TODO reduce max sizes of all images and apply to pageContent
    return pageContent;
  }

  List<Image> getAllImagesInSource(String pageContent) {
    List<Image> imagesList = new Vector<Image>();
    Matcher imgMatcher = getImgMatcher(pageContent);
    while (imgMatcher.find()) {
      Map<String, String> imgAttrMap = getImageAttrMap(imgMatcher.group());
      imagesList.add(new Image(imgAttrMap.get("id"), imgAttrMap.get("name"),
          imgAttrMap.get("src")));
    }
    return imagesList;
  }

  Map<String, String> getImageAttrMap(String imgAttrStr) {
    Map<String, String> imgAttrMap = new HashMap<String, String>();
    Matcher imgAttrMatcher = getImgAttrMatcher(imgAttrStr);
    while (imgAttrMatcher.find()) {
      imgAttrMap.put(imgAttrMatcher.group(1).toLowerCase(), imgAttrMatcher.group(2));
    }
    return imgAttrMap;
  }

  Matcher getImgMatcher(String pageContent) {
    return imgPattern.matcher(pageContent);
  }

  Matcher getImgAttrMatcher(String imgAttrPart) {
    return imgAttrPattern.matcher(imgAttrPart);
  }

}
