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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.photo.container.Metadate;
import com.celements.photo.metadata.IMetadataDocumentRole;
import com.celements.photo.metadata.MetaInfoExtractor;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@Component("celmetatags")
public class MetaInfoScriptService implements ScriptService {
  
  @Requirement
  IMetadataDocumentRole metaDocComp;
  
  @Requirement
  IMetaInfoService metaInfoService;
  
  public List<Tag> getDirectoryTagsAsTagList(DocumentReference docRef, String filename, 
      String directory) {
    return metaInfoService.getDirectoryTagsAsTagList(docRef, filename, directory);
  }

  public List<Tag> getDirectoryTagsAsTagList(Attachment attachment, String directory) {
    return metaInfoService.getDirectoryTagsAsTagList(attachment, directory);
  }

  public Map<String, String> getAllTags(DocumentReference docRef, String filename) {
    return metaInfoService.getAllTags(docRef, filename);
  }

  public Map<String, String> getAllTags(Attachment attachment) {
    return metaInfoService.getAllTags(attachment);
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
  
  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }
}
