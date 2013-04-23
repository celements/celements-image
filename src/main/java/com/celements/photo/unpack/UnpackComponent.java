package com.celements.photo.unpack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.photo.container.ImageDimensions;
import com.celements.photo.container.ImageLibStrings;
import com.celements.photo.image.GenerateThumbnail;
import com.celements.photo.utilities.AddAttachmentToDoc;
import com.celements.photo.utilities.Unzip;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class UnpackComponent implements UnpackComponentRole {
  
  @Requirement
  Execution execution;
  
  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      UnpackComponent.class);
  
  public void unzipFileToAttachment(DocumentReference zipSrcDocRef, String attachmentName,
      String unzipFileName, DocumentReference destinationDoc) {
    try {
      XWikiDocument zipSourceDoc = getContext().getWiki().getDocument(zipSrcDocRef, 
          getContext());
      XWikiAttachment zipAtt = zipSourceDoc.getAttachment(attachmentName);
      unzipFileToAttachment(zipAtt, attachmentName, destinationDoc);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception getting zip source document", xwe);
    }
  }
  
  public void unzipFileToAttachment(XWikiAttachment zipSrcFile, String attachmentName,
      DocumentReference destDocRef) {
    LOGGER.info("START: zip='" + zipSrcFile.getFilename() + "' file='" + attachmentName + 
        "'");
    ByteArrayInputStream newAttInStream = null;
    ByteArrayOutputStream outStream = null;
    try {
      if(isZipFile(zipSrcFile)){
        newAttInStream = new ByteArrayInputStream((new Unzip()).getFile(
            IOUtils.toByteArray(zipSrcFile.getContentInputStream(getContext())), 
            attachmentName).toByteArray());
      }
      String mimeType = "png";
      if((attachmentName.lastIndexOf('.') > -1) && (!attachmentName.endsWith("."))) {
        mimeType = attachmentName.substring(attachmentName.lastIndexOf('.') + 1);
      }
      LOGGER.debug("unzip mimetype is " + mimeType);
      outStream = new ByteArrayOutputStream();
      ImageDimensions id = (new GenerateThumbnail()).getImageDimensions(newAttInStream);
      LOGGER.info("width='" + id.width + "' height='" + id.height + "'");
      LOGGER.info("output stream size: " + outStream.size());
      attachmentName = attachmentName.replace(System.getProperty("file.separator"), ".");
      attachmentName = getContext().getWiki().clearName(attachmentName, false, true, 
          getContext());
      XWikiDocument destDoc = getContext().getWiki().getDocument(destDocRef, 
          getContext());
      XWikiAttachment att = (new AddAttachmentToDoc()).addAtachment(destDoc, 
          outStream.toByteArray(), attachmentName, getContext());
      LOGGER.info("attachment='" + att.getFilename() + "', gallery='" + att.getDoc(
          ).getDocumentReference() + "' size='" + att.getFilesize() + "'");
    } catch (IOException ioe) {
      LOGGER.error("Exception while unpacking zip", ioe);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception while unpacking zip", xwe);
    } finally {
      if(newAttInStream != null) {
        try {
          newAttInStream.close();
        } catch (IOException ioe) {
          LOGGER.error("Could not close input stream.", ioe);
        }
      }
      if(outStream != null) {
        try {
          outStream.close();
        } catch (IOException ioe) {
          LOGGER.error("Could not close output stream.", ioe);
        }
      }
    }
    LOGGER.info("END file='" + attachmentName + "'");
  }
  
  boolean isZipFile(XWikiAttachment file) {
    return (file != null) && (file.getMimeType(getContext()).equalsIgnoreCase(
        ImageLibStrings.MIME_ZIP) || file.getMimeType(getContext()).equalsIgnoreCase(
        ImageLibStrings.MIME_ZIP_MICROSOFT));
  }
  
  boolean isImgFile(String fileName) {
    return (fileName != null) 
        && (fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_BMP)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_GIF)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPE)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPG)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPEG)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_PNG));
  }
  
  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }
}
