package com.celements.photo.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.photo.container.ImageDimensions;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;

@ComponentRole
public interface IImageService {

  public ImageDimensions getDimension(String imageFullName) throws XWikiException;

  public ImageDimensions getDimension(AttachmentReference imgRef) throws XWikiException;

  public List<Attachment> getRandomImages(DocumentReference galleryRef, int num);  
  
}
