package com.celements.photo.utilities;

import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.photo.plugin.CelementsPhotoPlugin;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Used to add data as a new XWikiAttachment to an XWikiDocument.
 */
public class AddAttachmentToDoc {
  private static final Log mLogger = 
      LogFactory.getFactory().getInstance(AddAttachmentToDoc.class);
  
  /**
   * Converts the given ByteArrayOutputStream to an XWikiAttachment and adds
   * it to the specified XWikiDocument.
   * 
   * @param doc XWikiDocument to attach the attachment to.
   * @param data The attachment data.
   * @param filename The name for the attachment.
   * @param context XWikiContext. Needed to get the author and to save the
   *           attachment.
   * @return The attachment containing the given data.
   * @throws XWikiException
   */
  public XWikiAttachment addAtachment(XWikiDocument doc, ByteArrayOutputStream data, 
      String filename, XWikiContext context) throws XWikiException{
    return addAtachment(doc, data.toByteArray(), filename, context);
  }
  
  /**
   * Converts the given byte array to an XWikiAttachment and adds
   * it to the specified XWikiDocument.
   * 
   * @param doc XWikiDocument to attach the attachment to.
   * @param data The attachment data.
   * @param filename The name for the attachment.
   * @param context XWikiContext. Needed to get the author and to save the
   *           attachment.
   * @return The attachment containing the given data.
   * @throws XWikiException
   */
  public XWikiAttachment addAtachment(XWikiDocument doc, byte[] data, String filename, 
      XWikiContext context) throws XWikiException{
    XWikiDocument olddoc = (XWikiDocument) doc.clone();
    XWikiAttachment attachment = olddoc.getAttachment(filename);

    if (attachment==null) {
        attachment = new XWikiAttachment();
        olddoc.getAttachmentList().add(attachment);
    }
    
    mLogger.info("filename='" + filename + "' contentsize='" + data.length + "'");
    attachment.setContent(data);
    attachment.setFilename(filename);
    attachment.setAuthor(context.getUser());

    attachment.setDoc(olddoc);
    
    olddoc.setAuthor(context.getUser());
    if (olddoc.isNew()) {
        olddoc.setCreator(context.getUser());
    }
    
    olddoc.saveAttachmentContent(attachment, context);
        
    return attachment;
  }
}
