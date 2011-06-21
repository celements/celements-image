package com.celements.photo.container;

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
    
    try {
      doc = xwiki.getDocument(ImageLibStrings.METAINFO_CLASS, context);
    } catch (XWikiException e) {
      doc = new XWikiDocument();
      String[] metainfoClass = ImageLibStrings.METAINFO_CLASS.split("\\.");
      doc.setSpace(metainfoClass[0]);
      doc.setName(metainfoClass[1]);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(ImageLibStrings.METAINFO_CLASS);
    needsUpdate |= bclass.addTextField(ImageLibStrings.METAINFO_CLASS_NAME, ImageLibStrings.METAINFO_CLASS_NAME_PRETTY, 50);
    needsUpdate |= bclass.addTextAreaField(ImageLibStrings.METAINFO_CLASS_DESCRIPTION, ImageLibStrings.METAINFO_CLASS_DESCRIPTION_PRETTY, 50, 10);
    
    if (needsUpdate){
      xwiki.saveDocument(doc, context);
    }
    
    return bclass;
  }
}
