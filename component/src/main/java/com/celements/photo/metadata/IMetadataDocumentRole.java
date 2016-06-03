package com.celements.photo.metadata;

import java.util.Map;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IMetadataDocumentRole {

  /**
   * Extract meta data from an image file and attach an object for each tag to the
   * destination document.
   * 
   * @param source
   *          DocumentReference of the document where the image is attached.
   * @param filename
   * @param destination
   *          Destination DocumentReference. Where the tags should be attached.
   * @param filteredImport
   *          Ignore "Unknown <...>" tags. (default: true)
   */
  public void extractMetadataToDocument(DocumentReference source, String filename,
      DocumentReference destination, Boolean filteredImport);

  /**
   * Adds a given map of tag - value pairs as objects to a document
   * 
   * @param tags
   * @param destinationDoc
   * @param filteredImport
   * @return document needs to be saved
   * @throws XWikiException
   */
  public Boolean addTagsToDoc(Map<String, String> tags, XWikiDocument destinationDoc,
      Boolean filteredImport) throws XWikiException;
}
