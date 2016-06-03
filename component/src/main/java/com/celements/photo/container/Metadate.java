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

import com.drew.metadata.Tag;

/**
 * Container used to simplify the velocity access. Represents a metatag,
 * containing the tag's name and description.
 */
public class Metadate {

  private String name;
  private String description;

  /**
   * Initialises an empty metatag.
   */
  public Metadate() {
    name = null;
    description = "";
  }

  /**
   * Set the name and description of the metatag.
   * 
   * @param Tag
   *          to initialize with
   */
  public Metadate(Tag tag) {
    name = tag.getTagName();
    description = tag.getDescription();
  }

  /**
   * Set the name and description of the metatag.
   * 
   * @param name
   *          Name of the tag.
   * @param description
   *          Description (or value) of the tag.
   */
  public Metadate(String name, String description) {
    this.name = name;
    this.description = description;
  }

  /**
   * Get the description of the metatag.
   * 
   * @return The description of the metatag.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the name of the metatag.
   * 
   * @return The name of the metatag.
   */
  public String getName() {
    return (name == null) ? "" : name;
  }

  /**
   * Check wether the Metadate is empty (used if the user asks for a non
   * existing metatag).
   * 
   * @return true if this is an empty Metadate.
   */
  public boolean isEmpty() {
    return name == null;
  }
}
