/**
 * 
 */
package com.celements.photo.utilities;

import java.util.Comparator;

import com.xpn.xwiki.doc.XWikiAttachment;

/**
 * Comparator to compare dates. This Comparator is used while sorting a List of
 * attachments. Sorts in descending order, i.e. from the newest to the oldest
 * attachment.
 * Usage: Collections.sort(myList, new FileDateComparator());
 */
public class FileDateComparator implements Comparator<XWikiAttachment> {
  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(XWikiAttachment attachmentOne, XWikiAttachment attachmentTwo) {
    int result = 0;
    
    if(attachmentOne.getDate().before(attachmentTwo.getDate())){
      result = 1;
    } else if(attachmentOne.getDate().after(attachmentTwo.getDate())){
      result = -1;
    }
    
    return result;
  }
}
