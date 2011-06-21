package com.celements.photo.container;

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
    
    try {
      doc = xwiki.getDocument(ImageLibStrings.PHOTO_IMAGE_CLASS, context);
    } catch (XWikiException e) {
      doc = new XWikiDocument();
      String[] metainfoClass = ImageLibStrings.PHOTO_IMAGE_CLASS.split("\\.");
      doc.setSpace(metainfoClass[0]);
      doc.setName(metainfoClass[1]);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName(ImageLibStrings.PHOTO_IMAGE_CLASS);
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
