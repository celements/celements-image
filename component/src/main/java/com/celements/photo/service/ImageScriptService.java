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

import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.photo.container.ImageDimensions;
import com.celements.photo.image.ICropImage;
import com.celements.photo.maxImageSize.IMaxImageSizeServiceRole;
import com.celements.photo.unpack.IUnpackComponentRole;
import com.celements.photo.utilities.ImportFileObject;
import com.celements.sajson.Builder;
import com.celements.web.service.CelementsWebScriptService;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiURLFactory;

@Component("celementsphoto")
public class ImageScriptService implements ScriptService {

  public static final String SPACEPREF_IMAGE_ANIMATION = "celImageAnimation";
  public static final String CFG_IMAGE_ANIMATION = "celements.celImageAnimation";
  public static final String IMAGE_FILE_SIZE = "fileSize";
  public static final String IMAGE_HEIGHT = "maxHeight";
  public static final String IMAGE_WIDTH = "maxWidth";
  public static final String IMAGE_ATT_VERSION = "attversion";
  public static final String IMAGE_FILENAME = "filename";
  public static final String IMAGE_SRC = "src";
  public static final String IMAGE_CHANGED_BY = "lastChangedBy";
  public static final String IMAGE_MIME_TYPE = "mimeType";

  private static final Log LOGGER = LogFactory.getFactory().getInstance(ImageScriptService.class);

  @Requirement
  IImageService imageService;

  @Requirement("celementsweb")
  ScriptService celementsService;

  @Requirement
  ICropImage cropImage;

  @Requirement
  IUnpackComponentRole unpack;

  @Requirement
  IMaxImageSizeServiceRole maxImageSizeService;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private CelementsWebScriptService getCelWebService() {
    return (CelementsWebScriptService) celementsService;
  }

  public void addImage(Builder jsonBuilder, Attachment imgAttachment) {
    addImage(jsonBuilder, imgAttachment, false);
  }

  public Attachment getImageAttachment(AttachmentReference imageRef) {
    Attachment imageAttachment = null;
    try {
      XWikiDocument imageDoc = getContext().getWiki().getDocument(imageRef.getDocumentReference(),
          getContext());
      XWikiAttachment imageAtt = imageDoc.getAttachment(imageRef.getName());
      imageAttachment = new Attachment(imageDoc.newDocument(getContext()), imageAtt, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get image for attachment reference [" + imageRef + "]", exp);
    }
    return imageAttachment;
  }

  public void addImage(Builder jsonBuilder, Attachment imgAttachment,
      boolean includeImgDimensions) {
    Document theDoc = imgAttachment.getDocument();
    XWikiURLFactory urlFactory = getContext().getURLFactory();
    URL theAttUrl = urlFactory.createAttachmentURL(imgAttachment.getFilename(), theDoc.getSpace(),
        theDoc.getName(), "download", "", getContext());
    jsonBuilder.openDictionary();
    try {
      jsonBuilder.addStringProperty(IMAGE_SRC, urlFactory.getURL(theAttUrl, getContext()));
      jsonBuilder.addStringProperty(IMAGE_FILENAME, imgAttachment.getFilename());
      jsonBuilder.addStringProperty(IMAGE_ATT_VERSION, imgAttachment.getVersion());
      jsonBuilder.addStringProperty(IMAGE_CHANGED_BY, getContext().getWiki().getLocalUserName(
          imgAttachment.getAuthor(), null, false, getContext()));
      if (includeImgDimensions) {
        AttachmentReference imgRef = new AttachmentReference(imgAttachment.getFilename(),
            theDoc.getDocumentReference());
        try {
          ImageDimensions imgDim = imageService.getDimension(imgRef);
          if (imgDim != null) {
            jsonBuilder.openProperty(IMAGE_HEIGHT);
            jsonBuilder.addInteger((int) Math.floor(imgDim.getHeight()));
            jsonBuilder.openProperty(IMAGE_WIDTH);
            jsonBuilder.addInteger((int) Math.floor(imgDim.getWidth()));
          } else {
            LOGGER.error("unable to read dimension for image [" + imgRef + "].");
          }
        } catch (XWikiException exp) {
          LOGGER.error("Failed to get image dimensions for image [" + imgRef + "].", exp);
        }
      }
      jsonBuilder.addStringProperty(IMAGE_FILE_SIZE, getCelWebService().getHumanReadableSize(
          imgAttachment.getFilesize(), true));
      jsonBuilder.addStringProperty(IMAGE_MIME_TYPE, imgAttachment.getMimeType());
    } catch (Exception exp) {
      LOGGER.error("Failed to addImage [" + imgAttachment.getFilename() + "] to json.", exp);
    }
    jsonBuilder.closeDictionary();
  }

  public ImageDimensions getDimension(String imageFullName) {
    try {
      return imageService.getDimension(imageFullName);
    } catch (XWikiException exp) {
      LOGGER.warn("Failed to getDimension for [" + imageFullName + "].", exp);
    }
    return null;
  }

  public void crop(Document srcDoc, String srcFilename, String overwriteOutputFormat,
      OutputStream out) {
    int x = Integer.parseInt(getContext().getRequest().get("cropX"));
    if (x < 0) {
      x = 0;
    }
    int y = Integer.parseInt(getContext().getRequest().get("cropY"));
    if (y < 0) {
      y = 0;
    }
    int w = Integer.parseInt(getContext().getRequest().get("cropW"));
    if (w <= 0) {
      w = 1;
    }
    int h = Integer.parseInt(getContext().getRequest().get("cropH"));
    if (h <= 0) {
      h = 1;
    }
    LOGGER.debug("Cropping to " + x + ":" + y + " " + w + "x" + h);
    cropImage.crop(srcDoc, srcFilename, x, y, w, h, overwriteOutputFormat, out);
  }

  public void crop(Document srcDoc, String srcFilename, int x, int y, int w, int h,
      String overwriteOutputFormat, OutputStream out) {
    cropImage.crop(srcDoc, srcFilename, x, y, w, h, overwriteOutputFormat, out);
  }

  public void outputCroppedImage(Document srcDoc, String srcFilename,
      String overwriteOutputFormat) {
    int x = Integer.parseInt(getContext().getRequest().get("cropX"));
    if (x < 0) {
      x = 0;
    }
    int y = Integer.parseInt(getContext().getRequest().get("cropY"));
    if (y < 0) {
      y = 0;
    }
    int w = Integer.parseInt(getContext().getRequest().get("cropW"));
    if (w <= 0) {
      w = 1;
    }
    int h = Integer.parseInt(getContext().getRequest().get("cropH"));
    if (h <= 0) {
      h = 1;
    }
    cropImage.outputCroppedImage(srcDoc, srcFilename, x, y, w, h, overwriteOutputFormat);
  }

  /**
   * Get a specified image file in a zip archive, extract it, change it to the desired
   * size and save it as an attachment to the given page.
   *
   * @param zipSourceDoc
   *          D
   * @param attachmentName
   *          Filename of the zip archive file.
   * @param unzipFileName
   *          Filename of the file to extract from the zip.
   * @param destinationDoc
   *          Document to attach the extracted file to.
   * @return The final attachment name (with added zip folder info and cleared by xwiki).
   */
  public String unzipFileToAttachment(DocumentReference zipSrcDocRef, String attachmentName,
      String unzipFileName, DocumentReference destDocRef) {
    XWikiAttachment zipFile;
    try {
      zipFile = getContext().getWiki().getDocument(zipSrcDocRef, getContext()).getAttachment(
          attachmentName);
      return unpack.unzipFileToAttachment(zipFile, unzipFileName, destDocRef);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception getting zip attachment document.", xwe);
      return unzipFileName;
    }
  }

  public boolean addSlideFromTemplate(DocumentReference galleryDocRef, String slideBaseName,
      String attFullName) {
    if (imageService.checkAddSlideRights(galleryDocRef)) {
      return imageService.addSlideFromTemplate(galleryDocRef, slideBaseName, attFullName);
    }
    return false;
  }

  public boolean addSlidesFromTemplate(DocumentReference galleryDocRef, String slideBaseName,
      List<String> attFullNameList) {
    if (imageService.checkAddSlideRights(galleryDocRef)) {
      boolean successfullAdded = true;
      for (Object attFullName : attFullNameList) {
        if (attFullName != null) {
          successfullAdded &= imageService.addSlideFromTemplate(galleryDocRef, slideBaseName,
              attFullName.toString());
        }
      }
      return successfullAdded;
    } else {
      LOGGER.debug("Not enaught rigths to add slide to " + galleryDocRef);
    }
    return false;
  }

  public boolean addSlidesFromTemplate(DocumentReference galleryDocRef, String slideBaseName,
      String[] attFullNameList) {
    return addSlidesFromTemplate(galleryDocRef, slideBaseName, Arrays.asList(attFullNameList));
  }

  /**
   * Returns a map containing the external URLs to a specified image attachment in
   * different aspect ratios (crops) "all" aspect ratios as of now meaning 1:1, 3:4, 4:3,
   * 16:9, 16:10
   *
   * @return Map containing external URLs to the specified image.
   */
  public Map<String, String> getImageURLinAllAspectRatios(Attachment image) {
    Map<String, String> urls = Collections.emptyMap();
    try {
      XWikiDocument doc = getContext().getWiki().getDocument(
          image.getDocument().getDocumentReference(), getContext());
      XWikiAttachment xatt = doc.getAttachment(image.getFilename());
      urls = imageService.getImageURLinAllAspectRatios(xatt);
    } catch (Exception ex) {
      // Catching Exception since this method is used to build an XML export which
      // should not throw an exception simply because one of many images can't be read
      // correctly e.g. due to a JAI problem.
      LOGGER.error("Could not get image URLs in different aspect ratios for attachment ["
          + ((image != null) ? image.getFilename() : "null") + "]", ex);
    }
    return urls;
  }

  /**
   * Get a List of all attachments in the specified archive and the suggested action when
   * importing.
   *
   * @param attachmentDoc
   *          Zip archive to check the files for the existence in the gallery.
   * @param attachmentName
   *          Name of the zip archive attachment.
   * @param galleryDoc
   *          Gallery Document to check for the files.
   * @return List of {@link ImportFileObject} for each file.
   */
  public List<ImportFileObject> getAttachmentFileListWithActions(DocumentReference attachmentDocRef,
      String attachmentName, DocumentReference galleryDocRef) {
    try {
      XWikiDocument attachmentDoc = getContext().getWiki().getDocument(attachmentDocRef,
          getContext());
      XWikiDocument galleryDoc = getContext().getWiki().getDocument(galleryDocRef, getContext());
      XWikiAttachment zipFile = attachmentDoc.getAttachment(attachmentName);
      return imageService.getAttachmentFileListWithActions(zipFile, galleryDoc);
    } catch (Exception exp) {
      LOGGER.error("Failed to getAttachmentFileListWithActions for attachmentDocRef ["
          + attachmentDocRef + "], attachmentName [" + attachmentName + "], galleryDocRef ["
          + galleryDocRef + "].", exp);
    }
    return Collections.emptyList();
  }

  public String fixMaxImageSizes(String pageContent, int maxWidth, int maxHeight) {
    return maxImageSizeService.fixMaxImageSizes(pageContent, maxWidth, maxHeight);
  }

  @Deprecated
  public List<Attachment> getRandomImages(String fullName, int num) {
    return WebUtils.getInstance().getRandomImages(fullName, num, getContext());
  }

  public List<Attachment> getRandomImages(DocumentReference docRef, int num) {
    return imageService.getRandomImages(docRef, num);
  }

  public boolean useImageAnimations() {
    String defaultValue = getContext().getWiki().Param(CFG_IMAGE_ANIMATION, "0");
    return "1".equals(getContext().getWiki().getSpacePreference(SPACEPREF_IMAGE_ANIMATION,
        defaultValue, getContext()));
  }

  public String fileNameToDocName(String filename) {
    String cleaned = filename.replace(System.getProperty("file.separator"), ".");
    return getContext().getWiki().clearName(cleaned, true, true, getContext());
  }

}
