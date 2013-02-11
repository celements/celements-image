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
package com.celements.photo.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.xpn.xwiki.doc.XWikiAttachment;

@ComponentRole
public interface IMetaInfoService {
  
  public Tag getMetaTag(DocumentReference docRef, String filename, String tagName);
  
  public Tag getMetaTag(XWikiAttachment imageFile, String tagName);
  
  public Tag getMetaTag(InputStream imageFile, String tagName);
  
  public List<Tag> getDirectoryTags(DocumentReference docRef, String filename, 
      String directory);
  
  public List<Tag> getDirectoryTags(DocumentReference docRef, String filename, 
      Class<Directory> directory);

  public List<Tag> getDirectoryTags(XWikiAttachment imageFile, String directory);
  
  public List<Tag> getDirectoryTags(XWikiAttachment imageFile, Class<Directory> directory);

  public List<Tag> getDirectoryTags(InputStream imageFile, String directory);
  
  public List<Tag> getDirectoryTags(InputStream imageFile, Class<Directory> directory);
  
  public Map<String, Tag> getAllTags(DocumentReference docRef, String filename);
  
  public Map<String, Tag> getAllTags(XWikiAttachment imageFile) ;
  
  public Map<String, Tag> getAllTags(InputStream imageFile) ;
}
