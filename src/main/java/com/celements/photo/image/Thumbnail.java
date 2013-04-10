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
package com.celements.photo.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;

import com.celements.photo.container.ImageDimensions;
import com.celements.photo.container.ImageLibStrings;
import com.celements.photo.utilities.AddAttachmentToDoc;
import com.celements.photo.utilities.BaseObjectHandler;
import com.celements.photo.utilities.FileNameManipulator;
import com.celements.photo.utilities.ZipAttachmentChanges;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Provides functions to access and handle thumbnails.
 */
public class Thumbnail {
  /**
   * Returns the URL to the thumbnail of a certain image in the specified
   * size. If the thumbnail does not exist it is created.
   * 
   * @param doc XWikiDocument of the album.
   * @param image Name of the image 
   * @param width Desired width for the thumb
   * @param height Desired height for the thumb
   * @param imageMethods To be able to get the original image from its zip 
   *              archive.
   * @param context XWikiContext
   * @return The download URL for the thumb
   * @throws XWikiException
   * @throws IOException
   */
  public String getUrl(XWikiDocument doc, String id, int width, int height, 
      XWikiContext context) throws XWikiException, IOException {
    String album = doc.getDocumentReference().getName();
    ZipAttachmentChanges zipAttChanges = new ZipAttachmentChanges();
    GenerateThumbnail thumbGenerator = new GenerateThumbnail();
    XWikiAttachment zipAttachment = zipAttChanges.getContainingZip(doc, id, context);
    
    ImageDimensions imgSize = getThumbnailDimensions(doc, id, width, height, 
        thumbGenerator, context);
    DocumentReference metaDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), album + "_img_" + id);
    XWikiDocument celeMetaDoc = context.getWiki().getDocument(metaDocRef, context);
    if(imgSize.isEmpty()){
      String dir = (new BaseObjectHandler()).getImageString(celeMetaDoc, 
          ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY);
      String image = (new BaseObjectHandler()).getImageString(celeMetaDoc, 
          ImageLibStrings.PHOTO_IMAGE_FILENAME);
      
      try{
        BufferedImage original = 
            thumbGenerator.decodeInputStream(zipAttChanges.getFromZip(zipAttachment, 
            dir + image, context));
        imgSize = thumbGenerator.getImageDimensions(original);
        writeImageDimensionsToMetadata(doc, id, imgSize, context);
        imgSize = thumbGenerator.getThumbnailDimensions(original, width, height);
      }catch(Exception e){
        throw new IOException(image + "^^" + id);
      }
    }
    String thumbImageName = (new FileNameManipulator()).addSizeToFileName(id, 
        (int)imgSize.getWidth(), (int)imgSize.getHeight());
    XWikiAttachment thumbnail = celeMetaDoc.getAttachment(thumbImageName);
    
    if((thumbnail == null) || (thumbnail.getDate().before(zipAttachment.getDate()))){
      deleteOutdated(thumbnail, celeMetaDoc, context);
      String dir = (new BaseObjectHandler()).getImageString(celeMetaDoc, 
          ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY);
      String filename = (new BaseObjectHandler()).getImageString(celeMetaDoc, 
          ImageLibStrings.PHOTO_IMAGE_FILENAME);
      BufferedImage original = 
          thumbGenerator.decodeInputStream(zipAttChanges.getFromZip(zipAttachment, 
          dir + filename, context));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      thumbGenerator.createThumbnail(original, out, width, height, 
          getWatermark(doc, context), getCopyright(doc, context), 
          zipAttachment.getMimeType(context), null);
      thumbnail = (new AddAttachmentToDoc()).addAtachment(celeMetaDoc, out, 
          thumbImageName, context);
    }
    
    String imageURL = celeMetaDoc.getAttachmentURL(thumbnail.getFilename(), 
        "download", context);
    
    return imageURL;
  }

  private void deleteOutdated(XWikiAttachment thumbnail,
      XWikiDocument celeMetaDoc, XWikiContext context) throws XWikiException {
    if(thumbnail != null){
      celeMetaDoc.deleteAttachment(thumbnail, context);
    }
  }
  
  /**
   * Get the String to add to thumbnails as a watermark.
   * 
   * @param doc XWikiDocument of the album.
   * @param context XWikiContext
   * @return String to add to the image as a watermark.
   * @throws XWikiException 
   */
  private String getWatermark(XWikiDocument doc, XWikiContext context
      ) throws XWikiException {
    return getObjctDescription(doc, ImageLibStrings.PHOTO_ALBUM_WATERMARK);
  }

  /**
   * Get the String to add to thumbnails as copyright information.
   * 
   * @param doc XWikiDocument of the album.
   * @param context XWikiContext
   * @return String to add to the image as copyright information.
   * @throws XWikiException 
   */
  private String getCopyright(XWikiDocument doc, XWikiContext context
      ) throws XWikiException {
    return getObjctDescription(doc, ImageLibStrings.PHOTO_ALBUM_COPYRIGHT);
  }

  /**
   * Gets the thumbnail's dimensions, using the image dimension information 
   * saved in the celements photo plugin metadata.
   * 
   * @param doc XWikiDocument of the album.
   * @param image Name of the image.
   * @param width Maximum allowed width.
   * @param height Maximum allowed height.
   * @param context XWikiContext
   * @return ImageDimensions object containing the dimensions of the thumbnail. 
   * @throws XWikiException
   */
  private ImageDimensions getThumbnailDimensions(XWikiDocument doc, String id, int width, 
      int height, GenerateThumbnail thumbGenerator, XWikiContext context
      ) throws XWikiException{
    DocumentReference imgDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_img_" + id);
    XWikiDocument imageDoc = context.getWiki().getDocument(imgDocRef, context);
    
    BaseObjectHandler handler = new BaseObjectHandler();
    
    int imgWidth = handler.getImageInteger(imageDoc, ImageLibStrings.PHOTO_IMAGE_WIDTH);
    int imgHeight = handler.getImageInteger(imageDoc, ImageLibStrings.PHOTO_IMAGE_HEIGHT);
    
    return thumbGenerator.getThumbnailDimensions(imgWidth, imgHeight, width, height);
  }
  
  /**
   * Caches the real image dimensions to the celements photo plugin metadata.
   * 
   * @param doc XWikiDocument of the album.
   * @param image Name of the image.
   * @param imgDim True dimensions of the image.
   * @param context XWikiContext
   * @throws XWikiException
   */
  private void writeImageDimensionsToMetadata(XWikiDocument doc, String id, 
      ImageDimensions imgDim, XWikiContext context) throws XWikiException{
    DocumentReference imgDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_img_" + id);
    XWikiDocument imageDoc = context.getWiki().getDocument(imgDocRef, context);
    BaseObject metainfoObj = imageDoc.getXObject(ImageLibStrings.getImageClassDocRef());
    metainfoObj.setIntValue(ImageLibStrings.PHOTO_IMAGE_WIDTH, (int)imgDim.getWidth());
    metainfoObj.setIntValue(ImageLibStrings.PHOTO_IMAGE_HEIGHT, (int)imgDim.getHeight());
    
    context.getWiki().saveDocument(imageDoc, context);
  }

  /**
   * Gets the specified Object's description (value) from a document. If the
   * tag does not exist it is added and initialised to the given value. 
   * 
   * @param doc The XWikiDocument of the album.
   * @param tagName The name of the tag to fetch.
   * @return The description of the specified tag.
   * @throws XWikiException
   */
  private String getObjctDescription(XWikiDocument doc, String tagName
      ) throws XWikiException {
    if(!doc.isNew()){
      List<BaseObject> tags = doc.getXObjects(ImageLibStrings.getAlbumClassDocRef());
      for (Iterator<BaseObject> iter = tags.iterator(); iter.hasNext();) {
        BaseObject tag = iter.next();
        if((tag != null)){
          return tag.getStringValue(tagName);
        }
      }
    }
    
    return "";
  }
}