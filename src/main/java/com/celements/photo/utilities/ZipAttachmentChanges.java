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
package com.celements.photo.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.photo.container.ImageLibStrings;
import com.celements.photo.image.GenerateThumbnail;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.ListItem;
import com.xpn.xwiki.plugin.zipexplorer.ZipExplorerPluginAPI;
/*XXX
 * WARNING: Do NOT change the List you get using 
 * <XWikiDocument>.getAttachmentList()! Changes in this List are not local, but
 * change the original data in the XWikiDocument.
 */
public class ZipAttachmentChanges {

  static Log mLogger = LogFactory.getFactory().getInstance(
      ZipAttachmentChanges.class);

  /**
   * @param doc
   * @param album
   * @param context
   * @throws XWikiException
   * @throws IOException
   */
  public void checkZipAttatchmentChanges(XWikiDocument doc, XWikiContext context
      ) throws XWikiException, IOException {

    //1. are there new / deleted zips? -> yes: read / delete
    DocumentReference albumRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_alb_");
    XWikiDocument albumDoc = context.getWiki().getDocument(albumRef, context);

    /* This strange (and a bit ugly) construct of List and loops is
     * necessary because actualAttachments is not independant from
     * doc. Thus changes in this List will also change the attachments,
     * attached to doc. Do NOT alter actualAttachments.
     * ... and trust me, you won't like debugging errors grounded in such 
     * sideeffects ;-)
     */ 
    List<BaseObject> notDeletedAttachments = new Vector<BaseObject>();
    List<XWikiAttachment> notAddedAttachments = new Vector<XWikiAttachment>();

    List<BaseObject> savedAttachmentList = (new BaseObjectHandler()
        ).getAllFromBaseObjectList(albumDoc, ImageLibStrings.METATAG_ZIP_FILENAME);
    List<XWikiAttachment> actualAttachments = doc.getAttachmentList();
    
    if(savedAttachmentList != null){
      for (Iterator<BaseObject> iter = savedAttachmentList.iterator(); iter.hasNext();) {
        BaseObject attachmentObj = iter.next();
 
        if(attachmentObj != null && actualAttachments != null){
          for (Iterator<XWikiAttachment> iterator = actualAttachments.iterator(); 
              iterator.hasNext();) {
            XWikiAttachment attachment = iterator.next();
    
            if((attachment != null) && attachment.getFilename().equals(
                attachmentObj.getStringValue(ImageLibStrings.METAINFO_CLASS_DESCRIPTION))){
              notAddedAttachments.add(attachment);
              notDeletedAttachments.add(attachmentObj);
              break;
            }
          }
        }
      }
      
      removeDataFromRemovedZip(savedAttachmentList, notDeletedAttachments, doc, albumDoc, 
          context);
    }
    
    addRemainingZips(actualAttachments, notAddedAttachments, doc, albumDoc, context);
  }

  private void removeDataFromRemovedZip(List<BaseObject> savedAttachmentList,
      List<BaseObject> notDeletedAttachments, XWikiDocument doc,
      XWikiDocument albumDoc, XWikiContext context) throws XWikiException {
    if(savedAttachmentList.size() > notDeletedAttachments.size()){
      for (Iterator<BaseObject> iter = savedAttachmentList.iterator(); iter.hasNext();) {
        BaseObject element = iter.next();
        
        boolean delete = true;
        for (Iterator<BaseObject> iterator = notDeletedAttachments.iterator(); 
            iterator.hasNext();) {
          BaseObject ele = iterator.next();
          if(element.equals(ele)){
            delete = false;
            break;
          }
        }
        
        deleteImagesFromZip(element, delete, doc, albumDoc, context);
      }
    }
  }

  private void deleteImagesFromZip(BaseObject element, boolean delete,
      XWikiDocument doc, XWikiDocument albumDoc, XWikiContext context)
      throws XWikiException {
    if(delete){
      String filename = "";
      List<String> fileList = context.getWiki().getSpaceDocsName(
          ImageLibStrings.getPhotoSpace(doc), context);
      if(fileList != null){
        Iterator<String> iterator = fileList.iterator();
        while(iterator.hasNext() && !filename.equals(doc.getDocumentReference().getName(
            ) + "_zip_" + getFilenameHash(element.getStringValue(
            ImageLibStrings.METAINFO_CLASS_DESCRIPTION)))){
          filename = iterator.next();
        }
      }
      
      deleteThumbAndMetaForAll(element, doc, filename, albumDoc, context);
    }
  }

  private void deleteThumbAndMetaForAll(BaseObject element, XWikiDocument doc,
      String filename, XWikiDocument albumDoc, XWikiContext context)
      throws XWikiException {
    DocumentReference zipDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), filename);
    XWikiDocument deletedZip = context.getWiki().getDocument(zipDocRef, context);
    List<BaseObject> imagesInZip = (new BaseObjectHandler()).getAllFromBaseObjectList(
        deletedZip, ImageLibStrings.METATAG_IMAGE_HASH);
    for (Iterator<BaseObject> iterator = imagesInZip.iterator(); iterator.hasNext();) {
      BaseObject imageHashObj = iterator.next();
      
      String imageHash = imageHashObj.getStringValue(
          ImageLibStrings.METAINFO_CLASS_DESCRIPTION);
      
      deleteThumbnail(doc, imageHash, context);
      //ii. loesche meta
      deleteMetainfo(doc, imageHash, context);
      //iii. loesche celements meta oder - falls mehrere versionen vorhanden: update
      updateOrDeleteMetaData(imageHash, doc, context);
    }
    
    context.getWiki().deleteDocument(deletedZip, context);
    albumDoc.removeXObject(element);
    context.getWiki().saveDocument(albumDoc, context);
  }

  private void updateOrDeleteMetaData(String imageHash, XWikiDocument doc,
      XWikiContext context) throws XWikiException {
    DocumentReference metaDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_img_" + imageHash);
    XWikiDocument celementsMetaDoc = context.getWiki().getDocument(metaDocRef, context);
    int revision = (new BaseObjectHandler()).getImageInteger(celementsMetaDoc, 
        ImageLibStrings.PHOTO_IMAGE_REVISION);
    if(revision <= 1){
      context.getWiki().deleteDocument(celementsMetaDoc, context);
    } else{
      BaseObjectHandler handler = new BaseObjectHandler();
      handler.setImageInteger(celementsMetaDoc, ImageLibStrings.PHOTO_IMAGE_REVISION, 
          revision-1, context);
      List<XWikiAttachment> attachmentList = doc.getAttachmentList();
      Collections.sort(attachmentList, new FileDateComparator());
      String filenameInMeta = null;
      XWikiAttachment att = null;
      for (Iterator<XWikiAttachment> attIter = attachmentList.iterator(); attIter.hasNext(
          );) {
        att = attIter.next();
        DocumentReference zipDocRef = new DocumentReference(context.getDatabase(), 
            ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
            "_zip_" + getFilenameHash(att.getFilename()));
        XWikiDocument zipAttDoc = context.getWiki().getDocument(zipDocRef, context);
        filenameInMeta = handler.getDescriptionFromBaseObjectList(zipAttDoc, imageHash);
        if(filenameInMeta != null){
          break;
        }
      }
      setNameAndDirectory(filenameInMeta, att, handler, celementsMetaDoc, context);
    }
  }

  private void setNameAndDirectory(String filenameInMeta, XWikiAttachment att,
      BaseObjectHandler handler, XWikiDocument celementsMetaDoc,
      XWikiContext context) throws XWikiException {
    // ==null might occur when there is no existing version anymore and has not been removed correctly
    if(filenameInMeta != null){ 
      handler.setImageString(celementsMetaDoc, ImageLibStrings.PHOTO_IMAGE_ZIPNAME, 
          att.getFilename(), context);
      String [] path = filenameInMeta.split(System.getProperty("file.separator"));
      String dir = "";
      for(int i = 0; i < (path.length-1); i++){
        dir = dir + path[i] + "/";
      }
      handler.setImageString(celementsMetaDoc, ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY, 
          dir, context);
      handler.setImageString(celementsMetaDoc, ImageLibStrings.PHOTO_IMAGE_FILENAME, 
          path[path.length-1], context);
    } else{
      context.getWiki().deleteDocument(celementsMetaDoc, context);
    }
  }

  private void addRemainingZips(List<XWikiAttachment> actualAttachments,
      List<XWikiAttachment> notAddedAttachments, XWikiDocument doc,
      XWikiDocument albumDoc, XWikiContext context) throws XWikiException,
      IOException {
    if(actualAttachments.size() > notAddedAttachments.size()){
      for (Iterator<XWikiAttachment> iter = actualAttachments.iterator(); iter.hasNext();
          ) {
        XWikiAttachment element = iter.next();
        boolean added = true;  
        
        for (Iterator<XWikiAttachment> iterator = notAddedAttachments.iterator(); 
            iterator.hasNext();) {
          XWikiAttachment ele = iterator.next();
          if(element.equals(ele)){
            added = false;
          }
        }
        
        getImagesFromZip(element, added, doc, albumDoc, context);
      }
    }
  }

  private void getImagesFromZip(XWikiAttachment element, boolean added,
      XWikiDocument doc, XWikiDocument albumDoc, XWikiContext context)
      throws XWikiException, IOException {
    String mimeType = element.getMimeType(context);
    
    if(added && ((mimeType.equalsIgnoreCase(ImageLibStrings.MIME_ZIP) || 
        mimeType.equalsIgnoreCase(ImageLibStrings.MIME_ZIP_MICROSOFT)))){  
      BaseObjectHandler handler = new BaseObjectHandler();
      List<String> hashes = generateListOfHashes(element, handler, doc, context);
      
      XWikiDocument zipMetaDoc = writeHashesToMetaDoc(element, doc, context);
      for (Iterator<String> iterator = hashes.iterator(); iterator.hasNext();) {
        String hash = iterator.next();
        DocumentReference imgDocRef = new DocumentReference(context.getDatabase(), 
            ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
            "_img_" + hash);
        XWikiDocument imgDoc = context.getWiki().getDocument(imgDocRef, context);
        String dir = handler.getImageString(imgDoc, 
            ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY);
        String filename = handler.getImageString(imgDoc, 
            ImageLibStrings.PHOTO_IMAGE_FILENAME);
        
        handler.addBaseObject(zipMetaDoc, ImageLibStrings.METATAG_IMAGE_HASH, hash, 
            context);
        handler.addBaseObject(zipMetaDoc, hash, dir + filename, context);
      }
      
      handler.addBaseObject(albumDoc, ImageLibStrings.METATAG_ZIP_FILENAME, 
          element.getFilename(), context);
    }
  }

  private List<String> generateListOfHashes(XWikiAttachment element, 
      BaseObjectHandler handler, XWikiDocument doc,
      XWikiContext context) throws XWikiException, IOException {
    List<String> hashes = new Vector<String>();
    List<ListItem> fileList = getZipExplorerPluginApi(context).getFileTreeList(
        new Document(doc, context), element.getFilename());
    for (Iterator<ListItem> fileIter = fileList.iterator(); fileIter.hasNext();) {
      ListItem file = (ListItem) fileIter.next();
      String dir = file.getParent();
      String filename = file.getValue();
      String extention = filename.substring(filename.lastIndexOf(".")+1);
      
      if(file.getId().endsWith(System.getProperty("file.separator")) || 
          dir.contains("__MACOSX") ||
          !((extention.equalsIgnoreCase(ImageLibStrings.MIME_JPG) || 
          extention.equalsIgnoreCase(ImageLibStrings.MIME_JPEG)))){
        continue;
      }
      InputStream image = getFromZip(element, dir + filename, context);
      String hash = (new GenerateThumbnail()).hashImage(image);
      hashes.add(hash);
      DocumentReference metaDocRef = new DocumentReference(context.getDatabase(),
          ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
          "_img_" + hash);
      XWikiDocument imgMetaDoc = context.getWiki().getDocument(metaDocRef, context);
      
      if(imgMetaDoc.isNew()){
        imgMetaDoc.newXObject(ImageLibStrings.getImageClassDocRef(), context);
        handler.setImageString(imgMetaDoc, ImageLibStrings.PHOTO_IMAGE_HASH, hash, context
            );
      }else if(imgMetaDoc.getXObject(ImageLibStrings.getImageClassDocRef()) != null){
        deleteThumbnail(doc, hash, context);
        deleteMetainfo(doc, hash, context);
      }
      updateMetaObject(element, dir, filename, handler, imgMetaDoc, context);
    }
    return hashes;
  }

  private XWikiDocument writeHashesToMetaDoc(XWikiAttachment element,
      XWikiDocument doc, XWikiContext context) throws XWikiException {
    DocumentReference metaDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_zip_" + getFilenameHash(element.getFilename()));
    XWikiDocument zipMetaDoc = context.getWiki().getDocument(metaDocRef, context);
    if(!zipMetaDoc.isNew()){
      context.getWiki().deleteDocument(zipMetaDoc, context);
      zipMetaDoc = context.getWiki().getDocument(metaDocRef, context);
    }
    return zipMetaDoc;
  }

  private int updateMetaObject(XWikiAttachment element, String dir,
      String filename, BaseObjectHandler handler, XWikiDocument imgMetaDoc,
      XWikiContext context) throws XWikiException {
    int revision = handler.getImageInteger(imgMetaDoc, 
        ImageLibStrings.PHOTO_IMAGE_REVISION);
    handler.setImageInteger(imgMetaDoc, ImageLibStrings.PHOTO_IMAGE_REVISION, revision+1, 
        context);
    handler.setImageString(imgMetaDoc, ImageLibStrings.PHOTO_IMAGE_ZIPNAME, 
        element.getFilename(), context);
    handler.setImageString(imgMetaDoc, ImageLibStrings.PHOTO_IMAGE_ZIPDIRECTORY, dir, 
        context);
    handler.setImageString(imgMetaDoc, ImageLibStrings.PHOTO_IMAGE_FILENAME, filename, 
        context);
    return revision;
  }
  
  /**
   * Extract the specified image from the zip file and return it as an 
   * InputStream.
   * 
   * @param album XWikiDocument of the album the image is part of.
   * @param image Name of the image.
   * @param id Id of the image.
   * @param context XWikiContext
   * @return ImputStream representation of the image.
   * @throws XWikiException
   * @throws IOException
   */
  public InputStream getFromZip(XWikiDocument album, String image, String id, 
      XWikiContext context) throws XWikiException, IOException {
    XWikiAttachment attachment = getContainingZip(album, id, context);
    return getFromZip(attachment, image, context);
  }  
  
  /**
   * Get the zip attachment containing the specified image.
   * 
   * @param doc XWikiDocument of the album.
   * @param id Id of the image.
   * @param context XWikiContext
   * @return Returns an XWikiAttachment representation of the zip file, 
   *        containing the specified image.
   * @throws XWikiException
   */
  public XWikiAttachment getContainingZip(XWikiDocument doc, String id, 
      XWikiContext context) throws XWikiException {
    DocumentReference metaDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_img_" + id);
    XWikiDocument imageMeta = context.getWiki().getDocument(metaDocRef, context);
    String zipName = (new BaseObjectHandler()).getImageString(imageMeta, 
        ImageLibStrings.PHOTO_IMAGE_ZIPNAME);
    
    List<XWikiAttachment> attachments = doc.getAttachmentList();
    for (Iterator<XWikiAttachment> iter = attachments.iterator(); iter.hasNext();) {
      XWikiAttachment attachment = iter.next();
      if(attachment.getFilename().equals(zipName)){
        return attachment;
      }
    }
    
    return null;
  }
  
  /**
   * Extract the specified image from the zip file and return it as an 
   * XWikiAttachment.
   * 
   * @param zip XWikiAttachment representation of the zip archive the image
   *         is archived in.
   * @param image Name of the image.
   * @param context XWikiContext
   * @return InputStream representation of the image.
   * @throws XWikiException
   * @throws IOException
   */
  public InputStream getFromZip(XWikiAttachment zip, String image, XWikiContext context
      ) throws XWikiException, IOException {
    if(image != null){
      byte[] imageArray;
      imageArray = (new Unzip()).getFile(IOUtils.toByteArray(zip.getContentInputStream(
          context)), image).toByteArray();
      return new ByteArrayInputStream(imageArray);
    }
    return null;
  }  
  
  /**
   * Deletes the metainformation of the specified image (not the celements
   * specific image information).
   * 
   * @param doc XWikiDocument of the album.
   * @param imageHash Hash of the image
   * @param context XWikiContext
   * @throws XWikiException
   */
  private void deleteMetainfo(XWikiDocument doc, String imageHash, XWikiContext context
      ) throws XWikiException {
    DocumentReference metaDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_img_" + imageHash);
    XWikiDocument metaDocument = context.getWiki().getDocument(metaDocRef, context);
    if(!metaDocument.isNew()){
      
      metaDocument.removeXObjects(ImageLibStrings.getMetainfoClassDocRef());
      context.getWiki().saveDocument(metaDocument, context);
    }
  }

  /**
   * Deletes all thumbnails of the specified image.
   * 
   * @param doc XWikiDocument of the album.
   * @param imageHash Hash of the image
   * @param context XWikiContext
   * @throws XWikiException
   */
  private void deleteThumbnail(XWikiDocument doc, String imageHash, XWikiContext context
      ) throws XWikiException {
    DocumentReference imgDocRef = new DocumentReference(context.getDatabase(), 
        ImageLibStrings.getPhotoSpace(doc), doc.getDocumentReference().getName() + 
        "_img_" + imageHash);
    XWikiDocument thumbDoc = context.getWiki().getDocument(imgDocRef, context);
  
    List<XWikiAttachment> thumbnails = thumbDoc.getAttachmentList();  
    for (Iterator<XWikiAttachment> deleteIter = thumbnails.iterator(); deleteIter.hasNext(
        );) {
      XWikiAttachment delete = (XWikiAttachment) deleteIter.next();
      thumbDoc.deleteAttachment(delete, context);
    }
  }
  
  private String getFilenameHash(String filename){
    try {
      MessageDigest digest = MessageDigest.getInstance(ImageLibStrings.HASHING_ALGORITHM);
      digest.update(filename.getBytes());
      String hashraw = new String(digest.digest());
      return Util.getUtil().hashToHex(hashraw);
    } catch (NoSuchAlgorithmException e) {
      mLogger.error(e);
    }
    
    return filename.replaceAll("_", "__");
  }
  
  /**
   * Returns the ZipExplorerPluginApi from the active XWiki.
   * 
   * @param context The XWikiContext.
   * @return The ZipExplorerPluginApi.
   */
  public ZipExplorerPluginAPI getZipExplorerPluginApi(XWikiContext context) {
    return (ZipExplorerPluginAPI)context.getWiki().getPluginApi("zipexplorer", context);
  }
}
