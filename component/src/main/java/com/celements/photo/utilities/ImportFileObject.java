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

import org.xwiki.model.reference.DocumentReference;

public class ImportFileObject {
  public static final short ACTION_SKIP = -1;
  public static final short ACTION_OVERWRITE = 0;
  public static final short ACTION_ADD = 1;
  
  private DocumentReference docRef;
  private String filename;
  private short action;
  private boolean isImg;
  
  public ImportFileObject(DocumentReference docRef, String filename, short action, 
      boolean isImg) {
    this.docRef = docRef;
    this.filename = filename;
    this.action = action;
    this.isImg = isImg;
  }
  
  public String getFilename() {
    return filename;
  }
  
  public short getAction() {
    return action;
  }
  
  public DocumentReference getDocRef() {
    return docRef;
  }
  
  public boolean isImg() {
    return isImg;
  }
}
