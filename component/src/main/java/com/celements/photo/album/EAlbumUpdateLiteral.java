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
package com.celements.photo.album;

import com.celements.sajson.ECommand;
import com.celements.sajson.IGenericLiteral;

public enum EAlbumUpdateLiteral implements IGenericLiteral {
  VALUE_PROPERTY(ECommand.VALUE_COMMAND),
  ADD_REMOVE_ARRAY(ECommand.ARRAY_COMMAND, VALUE_PROPERTY),
  ADD_REMOVE_PROPERTY(ECommand.PROPERTY_COMMAND, ADD_REMOVE_ARRAY),
  REQUEST_DICT(ECommand.DICTIONARY_COMMAND, ADD_REMOVE_PROPERTY, VALUE_PROPERTY),
  REQUEST_ARRAY(ECommand.ARRAY_COMMAND, REQUEST_DICT);
  
  private EAlbumUpdateLiteral[] literals;
  private ECommand command;
  private int nextLiteral = 0;

  private EAlbumUpdateLiteral(ECommand command, EAlbumUpdateLiteral... literals) {
    this.literals = literals;
    this.command = command;
  }

  public ECommand getCommand() {
    return command;
  }

  public IGenericLiteral getNextLiteral() {
    nextLiteral = nextLiteral + 1;
    if (nextLiteral > literals.length) {
      return null;
    }
    return literals[nextLiteral - 1];
  }

  public IGenericLiteral getFirstLiteral() {
    nextLiteral = 1;
    return literals[0];
  }

  public IGenericLiteral getPropertyLiteralForKey(String key,
      IGenericLiteral placeholder) {
    return placeholder;
  }
}