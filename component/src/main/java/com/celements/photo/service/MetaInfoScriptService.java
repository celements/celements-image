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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.photo.container.Metadate;
import com.celements.photo.metadata.IMetadataDocumentRole;
import com.celements.photo.metadata.MetaInfoExtractor;
import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@Component("celmetatags")
public class MetaInfoScriptService implements ScriptService {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      MetaInfoScriptService.class);
  
  @Requirement
  Execution execution;
  
  @Requirement
  IMetadataDocumentRole metaDocComp;
  
  public List<Tag> getDirectoryTagsAsTagList(DocumentReference docRef, String filename, 
      String directory) {
    return getDirectoryTagsAsTagListInternal(getStreamForAttachment(
        getAttachmentForDocRef(docRef, filename)), getDirClass(directory));
  }

  public List<Tag> getDirectoryTagsAsTagList(Attachment attachment, String directory) {
    return getDirectoryTagsAsTagListInternal(getStreamForAttachment(getXAttForAtt(
        attachment)), getDirClass(directory));
  }

  public Map<String, String> getAllTags(DocumentReference docRef, String filename) {
    return getAllTagsInternal(getInputStreamForAttachment(getAttachmentForDocRef(docRef, 
        filename)));
  }

  public Map<String, String> getAllTags(Attachment attachment) {
    return getAllTagsInternal(getInputStreamForAttachment(getXAttForAtt(attachment)));
  }

  /**
   * Get a specified Metadate from an image.
   * 
   * @param doc XWikiDocument of the album.
   * @param id Hashcode of the image.
   * @param imageMethods Used to get the image.
   * @param tag Name of the metatag to get.
   * @param context XWikiContext
   * @return The Metadate of the specified Tag.
   * @throws XWikiException
   * @throws MetadataException
   * @throws IOException
   */
  public Metadate getTag(Document doc, String id, String tag) {
    //TODO implement
    throw new NotImplementedException();
  }
  
  /**
   * Returns an array of Metadate objects, excluding the tags starting with
   * the specified condition. If the condition is an empty String or null the
   * all metadata available is returned.
   * 
   * @param doc XWikiDocument of the album.
   * @param id Hashcode of the image.
   * @param conditionTag Tags starting with this String are excluded from the result List.
   * @param imageMethods Used to get the image.
   * @param context XWikiContext
   * @return Array of Metadate objects.
   * @throws XWikiException
   * @throws MetadataException
   * @throws IOException
   */
  public Metadate[] getMetadataWithCondition(XWikiDocument doc, String id, 
      String conditionTag, XWikiContext context) {
    //TODO implement
    throw new NotImplementedException();
  }
  
  /**
   * Get all metainformation contained in a specified image.
   * 
   * @param doc XWikiDocument of the album.
   * @param id Hashcode of the image.
   * @param imageMethods Used to get the image.
   * @param context XWikiContext
   * @return A List containing all the metadata from the specified image.
   * @throws XWikiException
   * @throws MetadataException
   * @throws IOException 
   */
  public List<BaseObject> getMetadataList(XWikiDocument doc, String id, 
      XWikiContext context) {
    //TODO implement
    throw new NotImplementedException();
  }
  
  public Tag getMetaTag() {
    //TODO implement
    throw new NotImplementedException();
  }

  public void extractMetadataToDocument(DocumentReference source, String filename, 
      DocumentReference destination, boolean filteredImport) {
    metaDocComp.extractMetadataToDocument(source, filename, destination, filteredImport);
  }

  public void extractMetadataToDocument(DocumentReference source, String filename, 
      DocumentReference destination) {
    extractMetadataToDocument(source, filename, destination, true);
  }
  
  public String cleanCtrlChars(String tag) {
    if(!getContext().containsKey("nInfoExtractor")) {
      getContext().put("nInfoExtractor", new MetaInfoExtractor());
    }
    MetaInfoExtractor extractor = (MetaInfoExtractor)getContext().get("nInfoExtractor");
    return extractor.cleanCtrlChars(tag);
  }
  
  
  //TODO move out of ScriptService
  
  List<Tag> getDirectoryTagsAsTagListInternal(InputStream imageFile, 
      Class<Directory> directory) {
    try {
      return new MetaInfoExtractor().getDirectoryTagsAsTagList(imageFile, directory);
    } catch (MetadataException mde) {
      LOGGER.error("Exception extracting Metadata directory " + directory, mde);
    }
    return Collections.emptyList();
  }
  
  Map<String, String> getAllTagsInternal(InputStream imageFile) {
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

  private XWikiAttachment getXAttForAtt(Attachment attachment) {
    return attachment.getAttachment();
  }

  private InputStream getStreamForAttachment(XWikiAttachment attachment) {
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
