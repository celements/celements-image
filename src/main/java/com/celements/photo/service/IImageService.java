package com.celements.photo.service;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.AttachmentReference;

import com.celements.photo.container.ImageDimensions;
import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface IImageService {

  public ImageDimensions getDimension(String imageFullName) throws XWikiException;

  public ImageDimensions getDimension(AttachmentReference imgRef) throws XWikiException;

}
