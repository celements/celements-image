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

/**
 * 
 */
package com.celements.photo.plugin;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.photo.container.ImageDimensions;
import com.celements.photo.container.ImageLibStrings;
import com.celements.photo.container.ImageStrings;
import com.celements.photo.unpack.IUnpackComponentRole;
import com.celements.photo.utilities.ImportFileObject;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * The CelementsPhotoPluginAPI acts as an Interface between the functionality, 
 * implemented in Java and the frontend using it (Velocity). 
 * The photo plugin provides methods to create and manage galleries in XWiki.
 */
public class CelementsPhotoPluginAPI extends Api {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsPhotoPluginAPI.class);

  private CelementsPhotoPlugin photoPlugin;

  // PLUGIN .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.
  public CelementsPhotoPluginAPI(CelementsPhotoPlugin plugin, XWikiContext context) {
    super(context);
    photoPlugin = plugin;
  }
  
  public CelementsPhotoPlugin getPlugin() {
    return photoPlugin;
  }

  public void setPlugin(CelementsPhotoPlugin photoPlugin) {
    this.photoPlugin = photoPlugin;
  }
  
  // ALBUM .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
  /**
   * Returns a List of all albums in the photo space.
   * 
   * @return List of all albums in the photo space.
   */
  public List<String> getAlbumList(String space) throws XWikiException{
    List<String> albums = new Vector<String>();
    List<String> docs = context.getWiki().getSpaceDocsName(space, context);
    
    for (Iterator<String> iter = docs.iterator(); iter.hasNext();) {
      String docName = iter.next();
      DocumentReference docRef = new DocumentReference(context.getDatabase(), space, 
          docName);
      XWikiDocument doc = context.getWiki().getDocument(docRef, context);
      if(doc.getXObject(ImageLibStrings.getAlbumClassDocRef()) != null){
        albums.add(docName);
      }
    }
    
    return albums;
  }
  
  // IMAGE .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
  /**
   * Returns wether the specified image is marked as deleted or not.
   * 
   * @param doc Document of the album.
   * @param id Id of the image.
   * @return true if the image is tagged as deleted.
   * @throws XWikiException
   */
  public boolean isImageDeleted(Document album, String id) throws XWikiException{
    return photoPlugin.isImageDeleted(album.getDocument(), id, context);
  }

  /**
   * Set the "deleted" tag for the image to the specified value.
   * 
   * @param doc Document of the album.
   * @param id Id of the image.
   * @param deleted true to tag the image as deleted.
   * @throws XWikiException
   */
  public void setImageDeleted(Document album, String id, boolean deleted
      ) throws XWikiException{
    photoPlugin.setImageDeleted(album.getDocument(), id, deleted, context);
  }

  /**
   * Returns an array of ImageStrings for all images in the specified album,
   * excluding the link to a thumbnail. This method's primar use is to get
   * the image's id.
   * 
   * @see com.celements.photo.plugin.container.ImageStrings
   * @param doc Document of the album.
   * @return Array of ImageStrings.
   * @throws XWikiException
   * @throws IOException
   */
  public ImageStrings[] getImageListExclThumbs(Document album
      ) throws XWikiException, IOException{
    return photoPlugin.getImageListExclThumbs(album.getDocument(), context);
  }
  
  /**
   * Returns an array of ImageStrings for all images in the specified album.
   * 
   * @see com.celements.photo.plugin.container.ImageStrings
   * @param doc Document of the album.
   * @param width Desired maximum width of the thumbnails (aspect ratio is maintained).
   * @param height Desired maximum height of the thumbnails (aspect ratio is maintained).
   * @return Array of ImageStrings.
   * @throws XWikiException
   * @throws IOException
   */
  public ImageStrings[] getImageList(Document album, int width, int height
      ) throws XWikiException, IOException{
    return photoPlugin.getImageList(album.getDocument(), width, height, context);
  }
  
  // THUMBNAIL .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
  /**
   * Returns the URL to the thumbnail of a certain image in the specified
   * size. If the thumbnail does not exist it is created.
   * 
   * @param doc Document of the album.
   * @param id Id of the image.
   * @param width desired width for the thumb
   * @param height desired height for the thumb
   * @return The download URL for the thumb
   * @throws XWikiException
   * @throws IOException
   */
  public String getThumbnailURL(Document album, String id, int width, int height
      ) throws XWikiException, IOException{
    return photoPlugin.getThumbnailUrl(album.getDocument(), id, width, height, context);
  }
  
  // DATA MANIPULATION .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
  /**
   * Deletes the document with the metadata attached.
   * 
   * @param doc Document of the album.
   * @param id Id of the image.
   * @throws XWikiException
   * @throws IOException
   */
  public void forceClearMetadata(Document doc, String id
      ) throws XWikiException, IOException{
    photoPlugin.forceClearMetadata(doc.getDocument(), id, context);
  }
  
  // CELEMENTS USE .:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:
  /**
   * Get a List of all attachments in the specified archive and the suggested 
   * action when importing.
   * 
   * @param attachmentDoc Zip archive to check the files for the existence in the gallery.
   * @param attachmentName Name of the zip archive attachment.
   * @param galleryDoc Gallery Document to check for the files.
   * @return List of {@link ImportFileObject} for each file.
   * @throws XWikiException
   */
  public List<ImportFileObject> getAttachmentFileListWithActions(Document attachmentDoc, 
      String attachmentName, Document galleryDoc) throws XWikiException{
    XWikiAttachment zipFile = attachmentDoc.getDocument().getAttachment(attachmentName);
    return photoPlugin.getAttachmentFileListWithActions(zipFile, galleryDoc.getDocument(),
        context);
  }

  /**
   * Get a specified image file in a zip archive, extract it, change it to the 
   * desired size and save it as an attachment to the given page.
   * 
   * @param zipDoc File containing the image to extract.
   * @param attachmentName Filename of the image to extract.
   * @param attachToDoc Document to attach the extracted and resized image.
   * @param width Width (max - aspect ratio is maintained) to resize the image to.
   * @param height Height (max - aspect ratio is maintained) to resize the image to.
   * @throws XWikiException
   */
  @Deprecated
  public void unzipFileToAttachment(Document zipDoc, String attachmentName, 
      String unzipFileName, Document attachToDoc, int width, int height
      ) throws XWikiException, IOException{
    IUnpackComponentRole comp = (IUnpackComponentRole)Utils.getComponent(
        IUnpackComponentRole.class);
    comp.unzipFileToAttachment(zipDoc.getDocumentReference(), attachmentName, 
        unzipFileName, attachToDoc.getDocumentReference());
  }

  /**
   * @deprecated instead use ImageScriptService.getDimension(String) 
   */
  @Deprecated
  public ImageDimensions getDimension(String imageFullName) {
    try {
      return photoPlugin.getDimension(imageFullName, context);
    } catch (XWikiException exp) {
      LOGGER.warn("Failed to getDimension for [" + imageFullName + "].", exp);
    }
    return null;
  }
}