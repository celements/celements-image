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
import java.util.Hashtable;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import com.celements.photo.container.Metadate;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celmetatags")
public class MetaInfoScriptService implements ScriptService {
  public List<Tag> getDirectoryTagsAsTagArray(InputStream imageFile, Class directory) {
    return null;
  }
  
  public Hashtable<String, String> getAllTags(InputStream imageFile) {
    return null;
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
  public Metadate getTag(XWikiDocument doc, String id, String tag, XWikiContext context) {
    return null;
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
    return null;
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
    return null;
  }
  
  /**
   * Extract metadata from an image and write it to objects, attached to a 
   * XWikiDocument.
   * 
   * @param doc XWikiDocument of the album.
   * @param id Hashcode of the image.
   * @param imageMethods Used to get the image.
   * @param context XWikiContext
   * @throws XWikiException
   * @throws MetadataException
   * @throws IOException
   */
  public void extractMetaToDoc(XWikiDocument doc, String id, XWikiContext context) {
    
  }
  
  public Tag getMetaTag() {
    return null;
  }
}
