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
 * Generates the PhotoMetainfoClass with the fields 'name' and 'description'
 */
public class PhotoMetainfoClass {
  /**
   * Generates the PhotoMetainfoClass with the fields 'name' and 'description'.
   * 
   * @param context The XWikiContext used to get the xwiki and save.
   * @return A BaseObject of the PhotoMetainfoClass
   * @throws XWikiException
   */
  public BaseClass getNewPhotoMetainfoClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    DocumentReference classDocRef = ImageLibStrings.getMetainfoClassDocRef();
    
    try {
      doc = xwiki.getDocument(classDocRef, context);
    } catch (XWikiException e) {
      doc = new XWikiDocument(classDocRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classDocRef);
    needsUpdate |= bclass.addTextField(ImageLibStrings.METAINFO_CLASS_NAME, ImageLibStrings.METAINFO_CLASS_NAME_PRETTY, 50);
    needsUpdate |= bclass.addTextAreaField(ImageLibStrings.METAINFO_CLASS_DESCRIPTION, ImageLibStrings.METAINFO_CLASS_DESCRIPTION_PRETTY, 50, 10);
    needsUpdate |= bclass.addTextField("lang", "Language", 50);
    needsUpdate |= bclass.addTextField("source", "Tag Source", 50);
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    
    return bclass;
  }
}
