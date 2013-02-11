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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.photo.metadata.MetaInfoExtractor;
import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

@Component
public class MetaInfoService implements IMetaInfoService {
  private static Log LOGGER = LogFactory.getFactory().getInstance(MetaInfoService.class);
  
  @Requirement
  Execution execution;

  public Tag getMetaTag(DocumentReference docRef, String filename, String tagName) {
    return getMetaTag(getAttachment(docRef, filename), tagName);
  }

  public Tag getMetaTag(XWikiAttachment imageFile, String tagName) {
    return getMetaTag(getStreamForAttachment(imageFile), tagName);
  }

  public Tag getMetaTag(InputStream imageFile, String tagName) {
    return getAllTags(imageFile).get(tagName);
  }

  public List<Tag> getDirectoryTags(DocumentReference docRef, String filename,
      String directory) {
    return getDirectoryTags(getAttachment(docRef, filename), getDirClass(directory));
  }

  public List<Tag> getDirectoryTags(DocumentReference docRef, String filename,
      Class<Directory> directory) {
    return getDirectoryTags(getAttachment(docRef, filename), directory);
  }

  public List<Tag> getDirectoryTags(XWikiAttachment imageFile, String directory) {
    return getDirectoryTags(getStreamForAttachment(imageFile), getDirClass(directory));
  }

  public List<Tag> getDirectoryTags(XWikiAttachment imageFile, Class<Directory> directory
      ) {
    return getDirectoryTags(getStreamForAttachment(imageFile), directory);
  }
  
  public List<Tag> getDirectoryTags(InputStream imageFile, String directory) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public List<Tag> getDirectoryTags(InputStream imageFile, Class<Directory> directory) {
    try {
      return new MetaInfoExtractor().getDirectoryTagsAsTagList(imageFile, directory);
    } catch (MetadataException mde) {
      LOGGER.error("Exception extracting Metadata directory " + directory, mde);
    }
    return Collections.emptyList();
  }
  
  public Map<String, Tag> getAllTags(DocumentReference docRef, String filename) {
    return getAllTags(getAttachment(docRef, filename));
  }
  
  public Map<String, Tag> getAllTags(XWikiAttachment imageFile) {
    return getAllTags(getStreamForAttachment(imageFile));
  }
  
  public Map<String, Tag> getAllTags(InputStream imageFile) {
    try {
      return new MetaInfoExtractor().getAllTags(imageFile);
    } catch (MetadataException mde) {
      LOGGER.error("Exception extracting Metadata.", mde);
    }
    return Collections.emptyMap();
  }
  
  InputStream getInputStreamForAttachment(XWikiAttachment attachment) {
    try {
      return attachment.getContentInputStream(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Exception getting content of attachment '" + attachment.getFilename()
          + "'", e);
    }
    return null;
  }
  
  XWikiAttachment getAttachmentForDocRef(DocumentReference docRef, String filename) {
    try {
      XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
      return doc.getAttachment(filename);
    } catch (XWikiException e) {
      LOGGER.error("Exception getting Document for DocumentReference '" + docRef + "'", 
          e);
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  Class<Directory> getDirClass(String directory) {
    Class<Directory> dirClass = null;
    try {
      dirClass = (Class<Directory>)Class.forName(directory);
    } catch (ClassNotFoundException e) {
      try {
        dirClass = (Class<Directory>)Class.forName("com.drew.metadata.exif." + directory);
      } catch (ClassNotFoundException e1) {
        LOGGER.error("No directory found for '" + directory + "'");
      }
    }
    return dirClass;
  }

  XWikiAttachment getAttachment(DocumentReference docRef, String filename) {
    XWikiAttachment att = null;
    try {
      XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
      att = doc.getAttachment(filename);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception getting Document form DocumentReference " + docRef, xwe);
    }
    return att;
  }

  InputStream getStreamForAttachment(XWikiAttachment attachment) {
    try {
      return attachment.getContentInputStream(getContext());
    } catch (XWikiException xwe) {
      LOGGER.error("Exception while getting content of Attachment '" 
          + attachment.getFilename() + "'", xwe);
    }
    return null;
  }

  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }
}
