package com.celements.photo.metadata;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IMetadataDocumentRole {
  /**
   * Extract meta data from an image file and attach an object for each tag to the 
   * destination document.
   * 
   * @param source DocumentReference of the document where the image is attached.
   * @param filename
   * @param destination Destination DocumentReference. Where the tags should be attached.
   * @param filteredImport Ignore "Unknown <...>" tags. (default: true)
   */
  public void extractMetadataToDocument(DocumentReference source, String filename, 
      DocumentReference destination, Boolean filteredImport);
}
