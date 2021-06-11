package com.celements.photo.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sanselan.ImageReadException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.photo.container.CelImage;
import com.celements.photo.plugin.cmd.DecodeImageCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class CropImage implements ICropImage {

  private static final Logger LOGGER = LoggerFactory.getLogger(CropImage.class);

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public void crop(BufferedImage img, int x, int y, int w, int h, String type, String overwriteType,
      OutputStream out) {
    BufferedImage buffCropped = img;
    w = Math.min(w, (img.getWidth() - x));
    h = Math.min(h, (img.getHeight() - y));
    if ((x < img.getWidth()) && (y < img.getHeight())) {
      buffCropped = img.getSubimage(x, y, w, h);
    }
    (new GenerateThumbnail()).encodeImage(out, buffCropped, img, type, overwriteType);
  }

  @Override
  public OutputStream crop(XWikiAttachment xAtt, int x, int y, int w, int h, String overwriteType) {
    OutputStream out = new ByteArrayOutputStream();
    crop(xAtt, x, y, w, h, overwriteType, out);
    return out;
  }

  @Override
  public OutputStream crop(Document doc, String filename, int x, int y, int w, int h,
      String overwriteType, OutputStream out) {
    XWikiAttachment xAtt = getAttachment(doc, filename);
    if (xAtt != null) {
      return crop((XWikiAttachment) xAtt.clone(), x, y, w, h, overwriteType, out);
    }
    return null;
  }

  @Override
  public OutputStream crop(XWikiAttachment xAtt, int x, int y, int w, int h, String overwriteType,
      OutputStream out) {
    try {
      CelImage img = (new DecodeImageCommand()).readImage(xAtt, getContext());
      crop(img.getFirstImage(), x, y, w, h, xAtt.getMimeType(getContext()), overwriteType, out);
    } catch (XWikiException xwe) {
      LOGGER.error("Error getting attachment content and decoding it into an " + "BufferedImage",
          xwe);
    } catch (ImageReadException ire) {
      LOGGER.error("Error while reading image.", ire);
    }
    return out;
  }

  XWikiAttachment getAttachment(Document srcDoc, String filename) {
    XWikiAttachment xAtt = null;
    if (srcDoc != null) {
      try {
        XWikiDocument xDoc = getContext().getWiki().getDocument(srcDoc.getDocumentReference(),
            getContext());
        xAtt = xDoc.getAttachment(filename);
      } catch (XWikiException e) {
        LOGGER.error("Exception getting XWikiDocument with crop Attachment", e);
      }
    }
    return xAtt;
  }

  @Override
  public void outputCroppedImage(Document srcDoc, String srcFilename, int x, int y, int w, int h,
      String overwriteType) {
    XWikiAttachment xAtt = getAttachment(srcDoc, srcFilename);
    if (xAtt != null) {
      try {
        getContext().getResponse().setContentType("image/png");// output is always png
        crop((XWikiAttachment) xAtt.clone(), x, y, w, h, overwriteType,
            getContext().getResponse().getOutputStream());
      } catch (IOException e) {
        LOGGER.error("Error writing cropped image to response stream.", e);
      }
    }
  }
}
