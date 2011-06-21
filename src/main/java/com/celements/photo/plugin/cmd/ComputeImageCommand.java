package com.celements.photo.plugin.cmd;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.photo.container.ImageDimensions;
import com.celements.photo.image.GenerateThumbnail;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;

public class ComputeImageCommand {

  private static final Log mLogger = LogFactory.getFactory().getInstance(
      ComputeImageCommand.class);

  private ImageCacheCommand imgCacheCmd;

  public XWikiAttachment computeImage(XWikiAttachment attachment,
      XWikiContext context, XWikiAttachment attachmentClone, String sheight,
      String swidth, String copyright, String watermark, Color defaultBg,
      String defaultBgString) {
    if((defaultBgString != null) && defaultBgString.matches("[0-9A-Fa-f]{6}")) {
      int r = Integer.parseInt(defaultBgString.substring(1, 3), 16);
      int g = Integer.parseInt(defaultBgString.substring(3, 5), 16);
      int b = Integer.parseInt(defaultBgString.substring(5), 16);
      defaultBg = new Color(r, g, b);
    }
    
    if ((sheight != null && sheight.length() > 0)
        || (swidth != null && swidth.length() > 0)) {      
      try {
        int height = 0;
        int width = 0;
        if (sheight != null) {
          height = Integer.parseInt(sheight);
        }
        if (swidth != null) {
          width = Integer.parseInt(swidth);
        }
        
        attachmentClone = (XWikiAttachment) attachment.clone();
        GenerateThumbnail thumbGen = new GenerateThumbnail();
        ByteArrayInputStream in = new ByteArrayInputStream(attachmentClone.getContent(
            context));
        BufferedImage img = thumbGen.decodeImage(in);
        in.close();
        
        //TODO default background mit einbeziehen
        ImageDimensions dimension = thumbGen.getThumbnailDimensions(img, width, height);
        mLogger.debug("dimension: target width=" + width + "; target height=" + height
            + "; resized width=" + dimension.getWidth() + "; resized height="
            + dimension.getHeight());
        String key = getImageCacheCmd(context).getCacheKey(attachmentClone, dimension,
            copyright, watermark, context);
        mLogger.debug("attachment key: '" + key + "'");
        
        byte[] data = getImageCacheCmd(context).getImageForKey(key, context);
        if (data != null) {
          mLogger.info("Found image in Cache.");
          attachmentClone.setContent(data);
        } else {
          mLogger.info("No cached image.");
          attachmentClone.setContent(getThumbAttachment(img, dimension, thumbGen, 
              attachmentClone.getMimeType(context), watermark, copyright, defaultBg));
          getImageCacheCmd(context).addToCache(key, attachmentClone, context);
        }
      } catch (Exception exp) {
        mLogger.error("Error, could not resize / cache image", exp);
        attachmentClone = attachment;
      }
    }
    return attachmentClone;
  }

  void injectImageCacheCmd(ImageCacheCommand mockImgCacheCmd) {
    imgCacheCmd = mockImgCacheCmd;
  }

  ImageCacheCommand getImageCacheCmd(XWikiContext context) {
    if (imgCacheCmd == null) {
      imgCacheCmd = new ImageCacheCommand();
    }
    return imgCacheCmd;
  }

  public void flushCache() {
    if (imgCacheCmd != null) {
      imgCacheCmd.flushCache();
      imgCacheCmd = null;
    }
  }

  private byte[] getThumbAttachment(BufferedImage img, ImageDimensions dim, 
      GenerateThumbnail thumbGen, String mimeType, String watermark, String copyright,
      Color defaultBg) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    thumbGen.createThumbnail(img, out, dim, watermark, copyright, 
        mimeType, defaultBg);
    return out.toByteArray();
  }

}
