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

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(XWikiAttachment attachmentOne, XWikiAttachment attachmentTwo) {
    int result = 0;

    if (attachmentOne.getDate().before(attachmentTwo.getDate())) {
      result = 1;
    } else if (attachmentOne.getDate().after(attachmentTwo.getDate())) {
      result = -1;
    }

    return result;
  }
}
