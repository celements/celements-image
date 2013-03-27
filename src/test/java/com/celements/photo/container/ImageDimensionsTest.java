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

public class ImageDimensionsTest {
  public final float epsilon = 0.00001f;
  
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testImageDimensions() {
    ImageDimensions id = new ImageDimensions();
    assertTrue(id.getWidth() < epsilon);
    assertTrue(id.getHeight() < epsilon);
  }

  @Test
  public void testImageDimensionsIntInt() {
    ImageDimensions id = new ImageDimensions(1, 2);
    assertFalse(id.isEmpty());
    assertTrue(Math.abs(1 - id.getWidth()) < epsilon);
    assertTrue(Math.abs(2 - id.getHeight()) < epsilon);
  }

  @Test
  public void testIsEmpty() {
    ImageDimensions id = new ImageDimensions();
    assertTrue(id.isEmpty());
  }

}
