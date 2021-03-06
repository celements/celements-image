package com.celements.photo.unpack;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.doc.XWikiAttachment;

@ComponentRole
public interface IUnpackComponentRole {

  public String unzipFileToAttachment(DocumentReference zipSourceDoc, String attachmentName,
      String unzipFileName, DocumentReference destinationDoc);

  public String unzipFileToAttachment(XWikiAttachment zipSrcFile, String attachmentName,
      DocumentReference destDoc);
}
