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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.drew.metadata.Tag;

public class MetadateTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testMetadateTag() {
    Tag t = createMock(Tag.class);
    expect(t.getTagName()).andReturn("name").once();
    expect(t.getDescription()).andReturn("description").once();
    replay(t);
    Metadate m = new Metadate(t);
    verify(t);
    assertFalse(m.isEmpty());
    assertEquals("name", m.getName());
    assertEquals("description", m.getDescription());
  }

  @Test
  public void testGetDescription() {
    Metadate m = new Metadate("name", "description");
    assertEquals("description", m.getDescription());
  }

  @Test
  public void testGetName() {
    Metadate m = new Metadate("name", "description");
    assertEquals("name", m.getName());
  }

  @Test
  public void testIsEmpty() {
    Metadate m = new Metadate();
    assertTrue(m.isEmpty());
  }

}
