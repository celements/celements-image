package com.celements.photo.unpack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.filebase.AddingAttachmentContentFailedException;
import com.celements.filebase.AttachmentToBigException;
import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.photo.container.ImageLibStrings;
import com.celements.photo.utilities.Unzip;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class UnpackComponent implements IUnpackComponentRole {
  @Requirement
  Execution execution;
  
  @Requirement
  IAttachmentServiceRole attService;
  
  XWikiContext inject_context = null;
  Unzip inject_unzip = null;
  
  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      UnpackComponent.class);
  
  public String unzipFileToAttachment(DocumentReference zipSrcDocRef, String attachmentName,
      String unzipFileName, DocumentReference destinationDoc) {
    String cleanName = "";
    try {
      XWikiDocument zipSourceDoc = getContext().getWiki().getDocument(zipSrcDocRef, 
          getContext());
      XWikiAttachment zipAtt = zipSourceDoc.getAttachment(attachmentName);
      cleanName = unzipFileToAttachment(zipAtt, unzipFileName, destinationDoc);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception getting zip source document", xwe);
    }
    return cleanName;
  }
  
  public String unzipFileToAttachment(XWikiAttachment zipSrcFile, String attName,
      DocumentReference destDocRef) {
    String cleanName = attName;
    if(zipSrcFile != null) {
      LOGGER.info("START unzip: zip='" + zipSrcFile.getFilename() + "' file='" + attName + 
          "'");
      ByteArrayOutputStream newAttOutStream = null;
      try {
        byte[] imageContent = null;
        if(isZipFile(zipSrcFile)){
          newAttOutStream = getUnzip().getFile(attName, 
              zipSrcFile.getContentInputStream(getContext()));
          imageContent = newAttOutStream.toByteArray();
          LOGGER.debug("zip import. image content array length '" + imageContent.length 
              + "'");
        } else if(isImgFile(zipSrcFile)) {
          imageContent = IOUtils.toByteArray(zipSrcFile.getContentInputStream(
              getContext()));
          LOGGER.debug("single image import. array length '" + imageContent.length + "'");
        } else {
          LOGGER.error("Try to import non zip or image file to gallery: zip='" 
              + zipSrcFile.getFilename() + "', file='" + attName + "', mime='" 
              + zipSrcFile.getMimeType(getContext()) + "'");
        }
        cleanName = attName.replace(System.getProperty("file.separator"), ".");
        cleanName = attService.clearFileName(cleanName);
        if(imageContent != null) {
          XWikiDocument destDoc = getContext().getWiki().getDocument(destDocRef, 
              getContext());
          XWikiAttachment att = attService.addAttachment(destDoc, imageContent, cleanName,
                getContext().getUser(), null);
          LOGGER.info("attachment='" + att.getFilename() + "', doc='" + att.getDoc(
              ).getDocumentReference() + "' size='" + att.getFilesize() + "'");
        }
      } catch (XWikiException|IOException exeption) {
        LOGGER.error("Exception while unpacking zip", exeption);
      } catch (DocumentSaveException dse) {
        LOGGER.error("Exception saving unpacked file to destination document.", dse);
      } catch (AttachmentToBigException atbe) {
        LOGGER.error("Unpacked file to big.", atbe);
      } catch (AddingAttachmentContentFailedException aacfe) {
        LOGGER.error("Exception adding unpacked content to new attachment.", aacfe);
      } finally {
        IOUtils.closeQuietly(newAttOutStream);
      }
    } else {
      LOGGER.error("Source document which should contain zip file is null: [" 
          + zipSrcFile + "]");
    }
    LOGGER.info("END unzip: file='" + attName + "', cleaned name is '" + cleanName + "'");
    return cleanName;
  }

  private Unzip getUnzip() {
    if(inject_unzip != null) {
      return inject_unzip;
    }
    return new Unzip();
  }

  boolean isZipFile(XWikiAttachment file) {
    return (file != null) && (file.getMimeType(getContext()).equalsIgnoreCase(
        ImageLibStrings.MIME_ZIP) || file.getMimeType(getContext()).equalsIgnoreCase(
        ImageLibStrings.MIME_ZIP_MICROSOFT));
  }
  
  boolean isImgFile(XWikiAttachment file) {
    if(file != null) {
      String ending = file.getFilename();
      String mimeType = file.getMimeType(getContext());
      if((mimeType != null) && !"".equals(mimeType)) {
        ending = mimeType.replaceAll("/", ".");
      }
      return isImgFile(ending);
    }
    return false;
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
    if(inject_context != null) {
      return inject_context;
    }
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }
}
