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
package com.celements.photo.plugin.cmd;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.photo.container.CelImage;
import com.celements.photo.container.ImageDimensions;
import com.celements.photo.image.GenerateThumbnail;
import com.celements.photo.image.ICropImage;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.web.Utils;

public class ComputeImageCommand {

  private static final Logger LOGGER = LoggerFactory.getLogger(ComputeImageCommand.class);

  private ImageCacheCommand imgCacheCmd;
  
  //TODO -> cache ALL images and convert them to e.g. from CMYK, exept if ?format=raw
  public XWikiAttachment computeImage(XWikiAttachment attachment,
      XWikiContext context, XWikiAttachment attachmentClone, String sheight,
      String swidth, String copyright, String watermark, Color defaultBg,
      String defaultBgStr, String filterStr, String overwriteOutputFormat) {
    // crop params
    int cropX = parseIntWithDefault(context.getRequest().get("cropX"), -1);
    int cropY = parseIntWithDefault(context.getRequest().get("cropY"), -1);
    int cropW = parseIntWithDefault(context.getRequest().get("cropW"), -1);
    int cropH = parseIntWithDefault(context.getRequest().get("cropH"), -1);
    boolean needsCropping = needsCropping(cropX, cropY, cropW, cropH);
    LOGGER.debug("Crop needed: " + needsCropping + " -> " + cropX + ":" + cropY + " " + 
        cropW + "x" + cropH);
    boolean blackAndWhite = parseIntWithDefault(context.getRequest().get("BnW"), 0) == 1;
    LOGGER.debug("Get image as Black & White: " + blackAndWhite);
    defaultBg = getBackgroundColour(defaultBg, defaultBgStr);
    boolean lowerBound = 1 == parseIntWithDefault(context.getRequest().get("lowBound"), 
        0);
    Integer lowerBoundPositioning = parseIntWithDefault(context.getRequest(
        ).get("lowBoundPos"), null);
    boolean raw = 1 == parseIntWithDefault(context.getRequest().get("raw"), 0);
    int height = parseIntWithDefault(sheight, 0);
    int width = parseIntWithDefault(swidth, 0);
    try {
      attachmentClone = (XWikiAttachment) attachment.clone();
//      mLogger.debug("dimension: target width=" + width + "; target height=" + height
//          + "; resized width=" + dimension.getWidth() + "; resized height="
//          + dimension.getHeight());
      String key = getImageCacheCmd().getCacheKey(attachmentClone, new ImageDimensions(
          width, height), copyright, watermark, cropX, cropY, cropW, cropH, blackAndWhite,
          defaultBg, lowerBound, lowerBoundPositioning, filterStr, overwriteOutputFormat,
          raw);
      LOGGER.debug("attachment key: '" + key + "'");
      InputStream data = getImageCacheCmd().getImageForKey(key);
      if (data != null) {
        LOGGER.info("Found image in Cache.");
        attachmentClone.setContent(data);
      } else {
        LOGGER.info("No cached image.");
        long timeLast = System.currentTimeMillis();
        if(!raw) {
          String mimeType = attachmentClone.getMimeType(context);
          GenerateThumbnail thumbGen = new GenerateThumbnail();
          LOGGER.info("start loading image " + timeLast);
          DecodeImageCommand decodeImageCommand = new DecodeImageCommand();
          CelImage img = decodeImageCommand.readImage(attachmentClone, context);
          timeLast = logRuntime(timeLast, "image decoded after ");
          if(needsCropping) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ICropImage cropComp = Utils.getComponent(ICropImage.class);
            //TODO accept CelImage instead of bufferedImage
            cropComp.crop(img.getFirstImage(), cropX, cropY, cropW, cropH, mimeType, 
                mimeType, out);
            attachmentClone.setContent(new ByteArrayInputStream(
                ((ByteArrayOutputStream)out).toByteArray()));
            img = decodeImageCommand.readImage(attachmentClone, context);
          }
          timeLast = logRuntime(timeLast, "image cropped after ");
          if ((height > 0) || (width > 0)) {
            //TODO accept CelImage instead of bufferedImage
            ImageDimensions dimension = thumbGen.getThumbnailDimensions(img.getFirstImage(
                ), width, height, lowerBound, defaultBg);
            timeLast = logRuntime(timeLast, "got image dimensions after ");
            //TODO accept CelImage instead of bufferedImage
            byte[] thumbImageData = getThumbAttachment(img.getFirstImage(), dimension, 
                thumbGen, mimeType, mimeType, watermark, copyright, defaultBg, lowerBound,
                lowerBoundPositioning);
            timeLast = logRuntime(timeLast, "resize done after ");
            attachmentClone.setContent(new ByteArrayInputStream(thumbImageData));
            timeLast = logRuntime(timeLast, "new attachment content set after ");
          }
          if(blackAndWhite) {
            img = decodeImageCommand.readImage(attachmentClone, context);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);  
            ColorConvertOp op = new ColorConvertOp(cs, null);
            //TODO accept CelImage instead of bufferedImage
            BufferedImage bNwImg = op.filter(img.getFirstImage(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //TODO accept CelImage instead of bufferedImage
            thumbGen.encodeImage(out, bNwImg, img.getFirstImage(), mimeType, mimeType);
            byte[] bNwImage = out.toByteArray();
            attachmentClone.setContent(new ByteArrayInputStream(bNwImage));
            timeLast = logRuntime(timeLast, "image changed to black & white after ");
          }
          //TODO prevent multiple de- and encoding
          if((filterStr != null) && !"".equals(filterStr)) {
            LOGGER.debug("Filter found [" + filterStr + "]");
            String[] filterParts = filterStr.split("[,;| ]+");
            LOGGER.debug("Filter definition has " + filterParts.length + " parts");
            if(filterParts.length > 2) {
              try {
                int kerWidth = Integer.parseInt(filterParts[0]);
                int kerHeight = Integer.parseInt(filterParts[1]);
                float[] kerMatrix = new float[kerWidth*kerHeight];
                for(int i = 0; i < kerWidth*kerHeight; i++) {
                  float x;
                  if(filterParts.length <= 4) {
                    x = Float.parseFloat(filterParts[2]);
                    if((filterParts.length == 4) && (i == ((kerWidth*kerHeight)-1)/2)) {
                      x = Float.parseFloat(filterParts[3]);
                    }
                  } else {
                    x = Float.parseFloat(filterParts[i+2]);
                  }
                  kerMatrix[i] = x;
                }
                img = decodeImageCommand.readImage(attachmentClone, context);
                //TODO accept CelImage instead of bufferedImage
                BufferedImage newSource = new BufferedImage(img.getFirstImage().getWidth() 
                    + (kerWidth - 1), img.getFirstImage().getHeight() + (kerHeight - 1), 
                    BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = newSource.createGraphics();
                int xOffset = (kerWidth - 1) / 2;
                int yOffset = (kerHeight - 1) / 2;
                //TODO accept CelImage instead of bufferedImage
                setBorderPixelsBeforeNeededForKernel(xOffset, yOffset, newSource, 
                    img.getFirstImage());
                //TODO loop for animated gif
                g2.drawImage(img.getFirstImage(), xOffset, yOffset, null);
                g2.dispose();
                Kernel kernel = new Kernel(kerWidth, kerHeight, kerMatrix);
                LOGGER.debug("Filtering with kernel configured as " + kerWidth + ", " 
                    + kerHeight + ", " + Arrays.toString(kerMatrix));
                BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null); 
                BufferedImage filteredImg = op.filter(newSource, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ICropImage cropComp = Utils.getComponent(ICropImage.class);
                //TODO accept CelImage instead of bufferedImage
                cropComp.crop(filteredImg, xOffset, yOffset, img.getFirstImage().getWidth(
                    ), img.getFirstImage().getHeight(), mimeType, mimeType,
                    out);
                attachmentClone.setContent(new ByteArrayInputStream(out.toByteArray()));
                timeLast = logRuntime(timeLast, "applied kernel filter [" + filterStr 
                    + "] after ");
              } catch(NumberFormatException nfe) {
                LOGGER.error("Exception parsing filter string [" + filterStr + "]", nfe);
              }
            }
          }
          //TODO fix in its own branch to avoid colour shift problems in dev branch
          boolean outFormatChange = !Strings.isNullOrEmpty(overwriteOutputFormat) && 
              !overwriteOutputFormat.equals(mimeType);
          if(outFormatChange) {
            img = decodeImageCommand.readImage(attachmentClone, context);
            //TODO accept CelImage instead of bufferedImage
            mimeType = overwriteOutputFormat;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            (new GenerateThumbnail()).encodeImage(out, img, img, mimeType, 
                overwriteOutputFormat);
            attachmentClone.setContent(new ByteArrayInputStream(out.toByteArray()));
            LOGGER.debug("Rewritten output image type to mimeType [{}]", mimeType);
          }
        } else {
          LOGGER.info("Raw image! No alterations done.");
        }
        getImageCacheCmd().addToCache(key, attachmentClone);
        timeLast = logRuntime(timeLast, "image in cache after ");
      }
    } catch (Exception exp) {
      LOGGER.error("Error, could not resize / cache image", exp);
      attachmentClone = attachment;
    }
    return attachmentClone;
  }

  void setBorderPixelsBeforeNeededForKernel(int xOffset, int yOffset,
      BufferedImage newSource, BufferedImage img) {
    //set top left corner
    for(int i = 0; i < xOffset; i++) {
      for(int k = 0; k < yOffset; k++) {
        newSource.setRGB(i, k, img.getRGB(0, 0));
      }
    }
    //set top and bottom border
    for(int i = 0; i < img.getWidth(); i++) {
      int x = i + xOffset;
      for(int k = 0; k < yOffset; k++) {
        newSource.setRGB(x, k, img.getRGB(i, 0));
        newSource.setRGB(x, (newSource.getHeight() - 1) - k, 
            img.getRGB(i, (img.getHeight() - 1)));
      }
    }
    //set top right corner
    for(int i = 0; i < xOffset; i++) {
      for(int k = 0; k < yOffset; k++) {
        newSource.setRGB((newSource.getWidth() - 1) - i, k, 
            img.getRGB((img.getWidth() - 1), 0));
      }
    }
    //set bottom left corner
    for(int i = 0; i < xOffset; i++) {
      for(int k = 0; k < yOffset; k++) {
        newSource.setRGB(i, (newSource.getHeight() - 1) - k, 
            img.getRGB(0, (img.getHeight() - 1)));
      }
    }
    //set left and right border
    for(int i = 0; i < img.getHeight(); i++) {
      int y = i + yOffset;
      for(int k = 0; k < xOffset; k++) {
        newSource.setRGB(k, y, img.getRGB(0, i));
        newSource.setRGB((newSource.getWidth() - 1) - k, y, 
            img.getRGB((img.getWidth() - 1), i));
      }
    }
    //set bottom right corner
    for(int i = 0; i < xOffset; i++) {
      for(int k = 0; k < yOffset; k++) {
        newSource.setRGB((newSource.getWidth() - 1) - i, 
            (newSource.getHeight() - 1) - k, 
            img.getRGB((img.getWidth() - 1), (img.getHeight() - 1)));
      }
    }
  }

  Color getBackgroundColour(Color defaultBg, String defaultBgStr) {
    if((defaultBgStr != null) && defaultBgStr.matches(
        "[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?")) {
      int r = Integer.parseInt(defaultBgStr.substring(0, 2), 16);
      int g = Integer.parseInt(defaultBgStr.substring(2, 4), 16);
      int b = Integer.parseInt(defaultBgStr.substring(4, 6), 16);
      if(defaultBgStr.length() == 8) {
        defaultBg = new Color(r, g, b, Integer.parseInt(defaultBgStr.substring(6), 16));
      } else {
        defaultBg = new Color(r, g, b);
      }
    }
    return defaultBg;
  }

  long logRuntime(long timeLast, String message) {
    long timeNow = System.currentTimeMillis();
    LOGGER.info(message + (timeNow - timeLast) + " milliseconds " + "(time: " + timeNow 
        + ")");
    return timeNow;
  }

  private boolean needsCropping(int cropX, int cropY, int cropW, int cropH) {
    return (cropX >= 0) && (cropY >= 0) && (cropW > 0) && (cropH > 0);
  }

  Integer parseIntWithDefault(String stringValue, Integer defValue) {
    Integer parsedValue = defValue;
    if((stringValue != null) && (stringValue.length() > 0)) {
      try {
        parsedValue = Integer.parseInt(stringValue);
      } catch (NumberFormatException numExp) {
        LOGGER.debug("Failed to parse height [" + stringValue + "].", numExp);
      }
    }
    return parsedValue;
  }

  void injectImageCacheCmd(ImageCacheCommand mockImgCacheCmd) {
    imgCacheCmd = mockImgCacheCmd;
  }

  ImageCacheCommand getImageCacheCmd() {
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
      GenerateThumbnail thumbGen, String mimeType, String overwriteOutputFormat, 
      String watermark, String copyright, Color defaultBg, boolean lowerBound, 
      Integer lowerBoundPositioning) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    thumbGen.createThumbnail(img, out, dim, watermark, copyright, mimeType, 
        defaultBg, lowerBound, lowerBoundPositioning, overwriteOutputFormat);
    return out.toByteArray();
  }

}
