package com.celements.photo.service;

import java.util.List;
import java.util.Map;

import org.xwiki.model.reference.DocumentReference;

import com.drew.metadata.Tag;
import com.xpn.xwiki.api.Attachment;

public interface IMetaInfoService {
  public List<Tag> getDirectoryTagsAsTagList(DocumentReference docRef, String filename, 
      String directory);

  public List<Tag> getDirectoryTagsAsTagList(Attachment attachment, String directory);

  public Map<String, String> getAllTags(DocumentReference docRef, String filename);

  public Map<String, String> getAllTags(Attachment attachment);
}
