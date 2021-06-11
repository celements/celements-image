package com.celements.photo.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.photo.metadata.MetaInfoExtractor;
import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class MetaInfoService implements IMetaInfoService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetaInfoService.class);

  public List<Tag> getDirectoryTagsAsTagList(DocumentReference docRef, String filename,
      String directory) {
    return getDirectoryTagsAsTagListInternal(getStreamForAttachment(getAttachmentForDocRef(docRef,
        filename)), getDirClass(directory));
  }

  public List<Tag> getDirectoryTagsAsTagList(Attachment attachment, String directory) {
    return getDirectoryTagsAsTagListInternal(getStreamForAttachment(getXAttForAtt(attachment)),
        getDirClass(directory));
  }

  public Map<String, String> getAllTags(DocumentReference docRef, String filename) {
    return getAllTagsInternal(getInputStreamForAttachment(getAttachmentForDocRef(docRef,
        filename)));
  }

  public Map<String, String> getAllTags(Attachment attachment) {
    return getAllTagsInternal(getInputStreamForAttachment(getXAttForAtt(attachment)));
  }

  List<Tag> getDirectoryTagsAsTagListInternal(InputStream imageFile, Class<Directory> directory) {
    try {
      return new MetaInfoExtractor().getDirectoryTagsAsTagList(imageFile, directory);
    } catch (MetadataException mde) {
      LOGGER.error("Exception extracting Metadata directory " + directory, mde);
    }
    return Collections.emptyList();
  }

  Map<String, String> getAllTagsInternal(InputStream imageFile) {
    try {
      return new MetaInfoExtractor().getAllTags(imageFile);
    } catch (MetadataException mde) {
      LOGGER.error("Exception extracting Metadata.", mde);
    }
    return Collections.emptyMap();
  }

  InputStream getInputStreamForAttachment(XWikiAttachment attachment) {
    try {
      return attachment.getContentInputStream(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Exception getting content of attachment '" + attachment.getFilename() + "'", e);
    }
    return null;
  }

  XWikiAttachment getAttachmentForDocRef(DocumentReference docRef, String filename) {
    try {
      XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
      return doc.getAttachment(filename);
    } catch (XWikiException e) {
      LOGGER.error("Exception getting Document for DocumentReference '" + docRef + "'", e);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  Class<Directory> getDirClass(String directory) {
    Class<Directory> dirClass = null;
    try {
      dirClass = (Class<Directory>) Class.forName(directory);
    } catch (ClassNotFoundException e) {
      try {
        dirClass = (Class<Directory>) Class.forName("com.drew.metadata.exif." + directory);
      } catch (ClassNotFoundException e1) {
        LOGGER.error("No directory found for '" + directory + "'");
      }
    }
    return dirClass;
  }

  private XWikiAttachment getXAttForAtt(Attachment attachment) {
    return attachment.getAttachment();
  }

  private InputStream getStreamForAttachment(XWikiAttachment attachment) {
    try {
      return attachment.getContentInputStream(getContext());
    } catch (XWikiException xwe) {
      LOGGER.error("Exception while getting content of Attachment '" + attachment.getFilename()
          + "'", xwe);
    }
    return null;
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }
}
