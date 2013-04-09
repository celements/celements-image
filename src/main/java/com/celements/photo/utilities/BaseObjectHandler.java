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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.celements.photo.container.ImageLibStrings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class BaseObjectHandler {
  /**
   * Find the specified tag in a List of BaseObjects and return its 
   * description.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @return Description (value) of the searched tag.
   * @throws XWikiException
   */
  @SuppressWarnings("unchecked")
  public String getDescriptionFromBaseObjectList(XWikiDocument doc, String tagName) throws XWikiException {
    BaseObject obj = getBaseObjectFromBaseObjectList(doc, tagName);
    if(obj != null){
      return obj.getStringValue(ImageLibStrings.METAINFO_CLASS_DESCRIPTION);
    }
    
    return null;
  }
  
  /**
   * Find the specified tag in a List of BaseObjects and return it.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @return BaseObject of the searched tag.
   * @throws XWikiException
   */
  @SuppressWarnings("unchecked")
  public BaseObject getBaseObjectFromBaseObjectList(XWikiDocument doc, String tagName) throws XWikiException {
    List<BaseObject> metaTags = doc.getXObjects(ImageLibStrings.getMetainfoClassDocRef());
    if(metaTags != null){
      for (Iterator iter = metaTags.iterator(); iter.hasNext();) {
        BaseObject tag = (BaseObject) iter.next();
        
        if((tag != null) && tag.getStringValue(ImageLibStrings.METAINFO_CLASS_NAME).equals(tagName)){
          return tag;
        }
      }
    }
    return null;
  }

  /**
   * Find all BaseObjects matching the specified tag in a List of 
   * BaseObjects.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @return BaseObject of the searched tag.
   * @throws XWikiException
   */
  @SuppressWarnings("unchecked")
  public List<BaseObject> getAllFromBaseObjectList(XWikiDocument doc, String tagName){
    List<BaseObject> resultTags = new Vector<BaseObject>();
    
    List<BaseObject> metaTags = doc.getXObjects(ImageLibStrings.getMetainfoClassDocRef());
    if(metaTags != null){
      for (Iterator iter = metaTags.iterator(); iter.hasNext();) {
        BaseObject tag = (BaseObject) iter.next();
        
        if((tag != null) && tag.getStringValue(ImageLibStrings.METAINFO_CLASS_NAME).equals(tagName)){
          resultTags.add(tag);
        }
      }
    }
    
    return resultTags;
  }
  
  /**
   * Create an object and add it to a document.
   * 
   * @param doc XWikiDocument to attach the object to.
   * @param name Name of the objet to create.
   * @param value The value to set.
   * @param context XWikiContext
   * @return The created and attached BaseObject.
   * @throws XWikiException
   */
  public BaseObject addBaseObject(XWikiDocument doc, String name, String value, XWikiContext context) throws XWikiException{
    BaseObject celementsMeta = doc.newXObject(ImageLibStrings.getMetainfoClassDocRef(), 
        context);
    
    celementsMeta.set(ImageLibStrings.METAINFO_CLASS_NAME, name, context);
    celementsMeta.set(ImageLibStrings.METAINFO_CLASS_DESCRIPTION, value, context);
      
      context.getWiki().saveDocument(doc, context);
      
      return celementsMeta;
  }
  
  /**
   * Changes the specified BaseObject to the given values and saves the 
   * changes.
   * 
   * @param doc XWikiDocument, the object attends to.
   * @param obj The object needing an update.
   * @param value New value of the object.
   * @param context XWikiContext
   * @throws XWikiException 
   */
  public void updateBaseObject(XWikiDocument doc, BaseObject obj, String value, XWikiContext context) throws XWikiException{
    if(obj != null){    
      
      obj.set(ImageLibStrings.METAINFO_CLASS_DESCRIPTION, value, context);
      context.getWiki().saveDocument(doc, context);
    }
  }
  
  /**
   * Changes the specified BaseObject to the given values and saves the 
   * changes.
   * 
   * @param doc XWikiDocument, the object attends to.
   * @param obj The object needing an update.
   * @param name Name of the tag to update.
   * @param context XWikiContext
   * @throws XWikiException 
   */
  public void updateBaseObject(XWikiDocument doc, String name, String value, XWikiContext context) throws XWikiException{
    BaseObject obj = getBaseObjectFromBaseObjectList(doc, name);
    updateBaseObject(doc, obj, value, context);
  }

  /**
   * Find the specified tag in a List of BaseObjects and return its 
   * description.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @return Description (value) of the searched tag.
   * @throws XWikiException
   */
  public String getImageString(XWikiDocument doc, String tag) throws XWikiException {
    BaseObject obj = doc.getXObject(ImageLibStrings.getImageClassDocRef());
    if(obj != null){
      return obj.getStringValue(tag);
    }
    
    return null;
  }
  
  /**
   * Set the specified tag to the given value.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @param value Value to set the tag to.
   * @param context XWikiContext
   * @throws XWikiException
   */
  public void setImageString(XWikiDocument doc, String tag, String value, XWikiContext context) throws XWikiException {
    BaseObject obj = doc.getXObject(ImageLibStrings.getImageClassDocRef());
    if(obj != null){
      obj.setStringValue(tag, value);
        context.getWiki().saveDocument(doc, context);
    }
  }
  
  /**
   * Find the specified tag in a List of BaseObjects and return its 
   * description.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @return Description (value) of the searched tag.
   * @throws XWikiException
   */
  public boolean getImageBoolean(XWikiDocument doc, String tag) throws XWikiException {
    BaseObject obj = doc.getXObject(ImageLibStrings.getImageClassDocRef());
    if(obj != null){
      return obj.getIntValue(tag) == 1;
    }
    return false;
  }
  
  /**
   * Set the specified tag to the given value.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @param value Value to set the tag to.
   * @param context XWikiContext
   * @throws XWikiException
   */
  public void setImageBoolean(XWikiDocument doc, String tag, boolean value, 
      XWikiContext context) throws XWikiException {
    BaseObject obj = doc.getXObject(ImageLibStrings.getImageClassDocRef());
    if(obj != null){
      obj.setIntValue(tag, (value? 1 : 0));
        context.getWiki().saveDocument(doc, context);
    }
  }
  
  /**
   * Find the specified tag in a List of BaseObjects and return its 
   * description.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @return Description (value) of the searched tag.
   * @throws XWikiException
   */
  public int getImageInteger(XWikiDocument doc, String tag) throws XWikiException {
    BaseObject obj = doc.getXObject(ImageLibStrings.getImageClassDocRef());
    if(obj != null){
      return obj.getIntValue(tag);
    }
    
    return 0;
  }
  
  /**
   * Set the specified tag to the given value.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @param value Value to set the tag to.
   * @param context XWikiContext
   * @throws XWikiException
   */
  public void setImageInteger(XWikiDocument doc, String tag, int value, XWikiContext context) throws XWikiException {
    BaseObject obj = doc.getXObject(ImageLibStrings.getImageClassDocRef());
    if(obj != null){
      obj.setIntValue(tag, value);
        context.getWiki().saveDocument(doc, context);
    }
  }
  
  /**
   * Find the specified tag in a List of BaseObjects and return its 
   * description.
   * 
   * @param doc XWikiDocument with attached objects.
   * @param tagName Name of the tag.
   * @return Description (value) of the searched tag.
   * @throws XWikiException
   */
  public String getAlbumDataSpaceName(XWikiDocument doc) throws XWikiException {
    BaseObject obj = doc.getXObject(ImageLibStrings.getAlbumClassDocRef());
    if(obj != null){
      return obj.getStringValue(ImageLibStrings.PHOTO_ALBUM_SPACE_NAME);
    }
    
    return null;
  }
}
