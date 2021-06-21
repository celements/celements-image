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

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.filebase.AddingAttachmentContentFailedException;
import com.celements.filebase.AttachmentService;
import com.celements.filebase.AttachmentToBigException;
import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.exception.DocumentSaveException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * Used to add data as a new XWikiAttachment to an XWikiDocument.
 */
@Deprecated
public class AddAttachmentToDoc {

  private static final Logger LOGGER = LoggerFactory.getLogger(AddAttachmentToDoc.class);

  /**
   * Converts the given ByteArrayOutputStream to an XWikiAttachment and adds
   * it to the specified XWikiDocument.
   * 
   * @param doc
   *          XWikiDocument to attach the attachment to.
   * @param data
   *          The attachment data.
   * @param filename
   *          The name for the attachment.
   * @param context
   *          XWikiContext. Needed to get the author and to save the
   *          attachment.
   * @return The attachment containing the given data.
   * @throws XWikiException
   * @deprecated since 1.42 instead use {@link IAttachmentServiceRole.addAttachment(
   *             XWikiDocument, InputStream, String, String, String)}
   */
  @Deprecated
  public XWikiAttachment addAtachment(XWikiDocument doc, ByteArrayOutputStream data,
      String filename, XWikiContext context) throws XWikiException {
    return addAtachment(doc, data.toByteArray(), filename, context);
  }

  /**
   * Converts the given byte array to an XWikiAttachment and adds
   * it to the specified XWikiDocument.
   * 
   * @param doc
   *          XWikiDocument to attach the attachment to.
   * @param data
   *          The attachment data.
   * @param filename
   *          The name for the attachment.
   * @param context
   *          XWikiContext. Needed to get the author and to save the
   *          attachment.
   * @return The attachment containing the given data.
   * @deprecated since 1.42 instead use {@link IAttachmentServiceRole.addAttachment(
   *             XWikiDocument, InputStream, String, String, String)}
   */
  @Deprecated
  public XWikiAttachment addAtachment(XWikiDocument doc, byte[] data, String filename,
      XWikiContext context) throws XWikiException {
    try {
      return ((AttachmentService) Utils.getComponent(IAttachmentServiceRole.class)).addAttachment(
          doc, data, filename, context.getUser(), null);
    } catch (DocumentSaveException dse) {
      LOGGER.error("Exception saving document with added attachment '" + filename + "'", dse);
    } catch (AttachmentToBigException atbe) {
      LOGGER.error("Attachment '" + filename + "' is to big", atbe);
    } catch (AddingAttachmentContentFailedException aacfe) {
      LOGGER.error("Faild to add attachment content for '" + filename + "'", aacfe);
    }
    return null;
  }
}
