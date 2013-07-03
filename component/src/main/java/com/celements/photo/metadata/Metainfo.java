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
package com.celements.photo.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.celements.photo.container.ImageLibStrings;
import com.celements.photo.container.Metadate;
import com.celements.photo.utilities.BaseObjectHandler;
import com.celements.photo.utilities.ZipAttachmentChanges;
import com.drew.metadata.MetadataException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Provides methods to access metadata.
 */
@Deprecated 
public class Metainfo {
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
  public Metadate getTag(XWikiDocument doc, String id, String tag, XWikiContext context
      ) throws XWikiException, MetadataException, IOException {
    List<BaseObject> metainfos = getMetadataList(doc, id, context);

    Metadate metatag = new Metadate();
    
    for (int i = 0; i < metainfos.size(); i++) {
      BaseObject metadate = (BaseObject) metainfos.get(i);
      if((metadate != null) && (metadate.getStringValue(
          ImageLibStrings.METAINFO_CLASS_NAME).equals(tag))){
        metatag = new Metadate(metadate.getStringValue(
            ImageLibStrings.METAINFO_CLASS_NAME), metadate.getStringValue(
            ImageLibStrings.METAINFO_CLASS_DESCRIPTION));
        break;
      }
    }

    return metatag;
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
      String conditionTag, XWikiContext context) throws XWikiException, 
      MetadataException, IOException {
    List<BaseObject> metainfos = getMetadataList(doc, id, context);
    List<Metadate> resultList = new Vector<Metadate>();
    boolean condition = false;
    
    if((conditionTag == null) || conditionTag.equals("")){
      condition = true;
    }
    
    getAvailableNotExcludedTags(conditionTag, condition, metainfos, resultList);

    return resultList.toArray(new Metadate[resultList.size()]);
  }

  private void getAvailableNotExcludedTags(String conditionTag, boolean condition, 
      List<BaseObject> metainfos, List<Metadate> resultList) {
    for (Iterator<BaseObject> iter = metainfos.iterator(); iter.hasNext();) {
      BaseObject metadate = (BaseObject) iter.next();
      if((metadate != null) && (condition || !metadate.getStringValue(
          ImageLibStrings.METAINFO_CLASS_NAME).startsWith(conditionTag))){
        resultList.add(new Metadate(metadate.getStringValue(
            ImageLibStrings.METAINFO_CLASS_NAME), metadate.getStringValue(
            ImageLibStrings.METAINFO_CLASS_DESCRIPTION)));
      }
    }
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
      XWikiContext context) throws XWikiException, MetadataException, IOException {
    extractMetaToDoc(doc, id, context);
    
    XWikiDocument metaDoc = context.getWiki().getDocument(
        ImageLibStrings.getPhotoSpace(doc), doc.getName() + "_img_" + id, context);
    List<BaseObject> metainfos = metaDoc.getXObjects(
        ImageLibStrings.getMetainfoClassDocRef());
    //return an empty List if no metainfo objects are attached.
    if(metainfos == null){
      metainfos = java.util.Collections.emptyList();
    }
    return metainfos;
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
  public void extractMetaToDoc(XWikiDocument doc, String id, XWikiContext context
      ) throws XWikiException, MetadataException, IOException{
    String album = doc.getName();
    XWikiDocument celeMeta = context.getWiki().getDocument(ImageLibStrings.getPhotoSpace(
        doc), album + "_img_" + id, context);
    String dir = (new BaseObjectHandler()).getImageString(celeMeta, 
        ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY);
    String image = (new BaseObjectHandler()).getImageString(celeMeta, 
        ImageLibStrings.PHOTO_IMAGE_FILENAME);
    if(image == null){
      (new ZipAttachmentChanges()).checkZipAttatchmentChanges(doc, context);
    }
  
    clearOutdatedMetadata(doc, id, context);
    // TODO ugly, but otherwise updating files seems not to work properly
    context.getWiki().flushCache(context);
    
    List<BaseObject> metadataObjs = celeMeta.getXObjects(
        ImageLibStrings.getMetainfoClassDocRef());
    if(metadataObjs == null || metadataObjs.size() == 0){
      Map<String, String> metadata = extractAllMetadata(doc, dir + image, id, context);
      
      for(String key : metadata.keySet()) {
        initNewObject(celeMeta, key, (String)metadata.get(key), context);
      }
      
      //If there is no metadata, this prevents from trying to extract metadata on each 
      //image load.
      if(metadata.size() == 0){
        celeMeta.newXObject(ImageLibStrings.getMetainfoClassDocRef(), context);
      }
      context.getWiki().saveDocument(celeMeta, context);
    }
  }

  private void initNewObject(XWikiDocument celeMeta, String key, String value, 
      XWikiContext context) throws XWikiException {
    BaseObject metainfoObj = celeMeta.newXObject(ImageLibStrings.getMetainfoClassDocRef(),
        context);
    metainfoObj.set(ImageLibStrings.METAINFO_CLASS_NAME, key, context);
    metainfoObj.set(ImageLibStrings.METAINFO_CLASS_DESCRIPTION, value, context);
  }
  
  /**
   * Extracts all metadata directories of an image and returns them in a
   * Hashtable.
   * 
   * @param albumDoc XWikiDocument of the album.
   * @param image Name of the image.
   * @param id Id of the image.
   * @param imageMethods Used to get the image.
   * @param context XWikiContext
   * @return Hashtable containing all the metadata extracted from the image.
   * @throws MetadataException
   * @throws XWikiException
   * @throws IOException
   */
  private Map<String, String> extractAllMetadata(XWikiDocument albumDoc, String image, 
      String id, XWikiContext context) throws MetadataException, XWikiException, 
      IOException{
    InputStream imgAttachment = (new ZipAttachmentChanges()).getFromZip(albumDoc, image, 
        id, context);
    if(imgAttachment != null){
      return (new MetaInfoExtractor()).getAllTags(imgAttachment);
    }
    return Collections.emptyMap();
  }
  
  /**
   * Deletes the document with the metadata attached if it is older than its 
   * image (only the metadata extracted from the image is deleted, but not 
   * the data generated by CelementsPhotoPlugin).
   * 
   * @param doc XWikiDocument of the album.
   * @param image Name of the image the metadata belongs to.
   * @param imageMethods To be able to get the original image from its zip 
   *              archive.
   * @param context XWikiContext
   * @throws XWikiException
   * @throws IOException
   */
  private void clearOutdatedMetadata(XWikiDocument doc, String id, XWikiContext context
      ) throws XWikiException, IOException{
    XWikiDocument metadataDoc = context.getWiki().getDocument(
        ImageLibStrings.getPhotoSpace(doc), doc.getName() + "_img_" + id, context);

    if(!metadataDoc.isNew()){
      XWikiAttachment containingZip = (new ZipAttachmentChanges()
          ).getContainingZip(doc, id, context);
      Date metaDate = metadataDoc.getDate();
      Date zipDate = containingZip.getDate();
      
      if(metaDate.before(zipDate)){
        metadataDoc.removeXObjects(ImageLibStrings.getMetainfoClassDocRef());
        context.getWiki().saveDocument(metadataDoc, context);
      }
    }
  }
}
