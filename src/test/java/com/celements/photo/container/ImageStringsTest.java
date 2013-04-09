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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ImageStringsTest {
  private final String ID = "id123";
  private final String NAME = "imgname.jpg";
  private final String URL = "/download/My/Img/imagname.jpg";
  private final String THUMB_URL = "/download/My/Img/imagname.jpg?celwidth=100px";
  ImageStrings is;
  
  @Before
  public void setUp() throws Exception {
    is = new ImageStrings(ID, NAME, URL, THUMB_URL);
  }

  @Test
  public void testGetId() {
    assertEquals(ID, is.getId());
  }

  @Test
  public void testGetName() {
    assertEquals(NAME, is.getName());
  }

  @Test
  public void testGetThumbURL() {
    assertEquals(URL, is.getURL());
  }

  @Test
  public void testGetURL() {
    assertEquals(THUMB_URL, is.getThumbURL());
  }

  @Test
  public void testSetThumb() {
    String test = "TestChange";
    is.setThumbURL(test);
    assertEquals(test, is.getThumbURL());
  }

  @Test
  public void testGetJSON() {
    assertEquals(getTestJSON(), is.getJSON());
  }
  
  private String getTestJSON() {
    return "{\"id\" : \"" + ID + "\", \"name\" : \"" + NAME + "\", \"URL\" : \"" + URL + 
        "\", \"thumbURL\" : \"" + THUMB_URL + "\"}";
  }

}
