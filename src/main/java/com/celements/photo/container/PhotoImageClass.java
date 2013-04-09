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

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Generates the PhotoImageClass with fields for different fields to cash
 * information about the image.
 */
public class PhotoImageClass {
  /**
   * Generates the PhotoImageClass with fields for different fields to cash
   * information about the image.
   * 
   * @param context The XWikiContext used to get the xwiki and save.
   * @return A BaseObject of the PhotoAlbumClass
   * @throws XWikiException
   */
  public BaseClass getNewPhotoImageClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    DocumentReference classDocRef = ImageLibStrings.getImageClassDocRef();
    
    try {
      doc = xwiki.getDocument(classDocRef, context);
    } catch (XWikiException e) {
      doc = new XWikiDocument(classDocRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classDocRef);
    needsUpdate |= bclass.addTextField(ImageLibStrings.PHOTO_IMAGE_FILENAME, ImageLibStrings.PHOTO_IMAGE_FILENAME_PRETTY, 50);
    needsUpdate |= bclass.addTextField(ImageLibStrings.PHOTO_IMAGE_HASH, ImageLibStrings.PHOTO_IMAGE_HASH_PRETTY, 50);
    needsUpdate |= bclass.addTextField(ImageLibStrings.PHOTO_IMAGE_ZIPNAME, ImageLibStrings.PHOTO_IMAGE_ZIPNAME_PRETTY, 50);
    needsUpdate |= bclass.addTextField(ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY, ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY_PRETTY, 50);
    needsUpdate |= bclass.addNumberField(ImageLibStrings.PHOTO_IMAGE_REVISION, ImageLibStrings.PHOTO_IMAGE_REVISION_PRETTY, 30, "integer");
    needsUpdate |= bclass.addBooleanField(ImageLibStrings.PHOTO_IMAGE_DELETED, ImageLibStrings.PHOTO_IMAGE_DELETED_PRETTY, "yesno");
    needsUpdate |= bclass.addNumberField(ImageLibStrings.PHOTO_IMAGE_WIDTH, ImageLibStrings.PHOTO_IMAGE_WIDTH_PRETTY, 30, "integer");
    needsUpdate |= bclass.addNumberField(ImageLibStrings.PHOTO_IMAGE_HEIGHT, ImageLibStrings.PHOTO_IMAGE_HEIGHT_PRETTY, 30, "integer");
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    
    return bclass;
  }
}
