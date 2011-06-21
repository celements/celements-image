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

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;

import com.celements.photo.container.ImageDimensions;
import com.celements.photo.plugin.CelementsPhotoPlugin.SupportedFormat;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;

public class ImageCacheCommand {

  private static final Log mLogger = LogFactory.getFactory().getInstance(
      ImageCacheCommand.class);

  /**
   * Cache for already served images.
   */
  private Cache<byte[]> imageCache;

  private boolean initializedCache;

  /**
   * The size of the cache. This parameter can be configured using the key <tt>xwiki.plugin.image.cache.capacity</tt>.
   */
  private int capacity = 50;

  public ImageCacheCommand() {
    initializedCache = false;
  }

  Cache<byte[]> getImageCache(XWikiContext context) {
    if (!initializedCache) {
      initCache(context);
    }
    return imageCache;
  }

  /* Copy, Paste & Customize from com.xpn.xwiki.plugin.image */
  synchronized void initCache(XWikiContext context) {
    CacheConfiguration configuration = new CacheConfiguration();
    
    configuration.setConfigurationId("celements.photo");
    
    // Set folder o store cache
    File tempDir = context.getWiki().getTempDirectory(context);
    File imgTempDir = new File(tempDir, configuration.getConfigurationId());
    try {
      imgTempDir.mkdirs();
    } catch (Exception ex) {
      mLogger.warn("Cannot create temporary files", ex);
    }
    configuration.put("cache.path", imgTempDir.getAbsolutePath());
    
    // Set cache constraints
    LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
    configuration.put(LRUEvictionConfiguration.CONFIGURATIONID, lru);
    
    String capacityParam = "";
    try {
      capacityParam = context.getWiki().Param("xwiki.plugin.image.cache.capacity");
      if ((capacityParam != null) && (!capacityParam.equals(""))) {
        capacity = Integer.parseInt(capacityParam);
      }
    } catch (NumberFormatException ex) {
      mLogger.error("Error in ImagePlugin reading capacity: " + capacityParam, ex);
    }
    lru.setMaxEntries(capacity);
    
    try {
      imageCache = context.getWiki().getLocalCacheFactory().newCache(configuration);
    } catch (CacheException e) {
      mLogger.error("Error initializing the image cache", e);
    }
    initializedCache = true;
  }

  public void addToCache(String key, XWikiAttachment attachment, XWikiContext context
      ) throws XWikiException {
    if (getImageCache(context) != null) {
      getImageCache(context).set(key, attachment.getContent(context));
    } else {
      mLogger.info("Caching of images deactivated.");
    }
  }

  public byte[] getImageForKey(String key, XWikiContext context) {
    if (getImageCache(context) != null) {
      return getImageCache(context).get(key);
    }
    return null;
  }

  String getCacheKey(XWikiAttachment attachment,
      ImageDimensions dimension, String copyright, String watermark,
      XWikiContext context) throws NoSuchAlgorithmException {
    String securityHash = "";
    if(((watermark != null) && (watermark.length() > 0))
        || ((copyright != null) && (copyright.length() > 0))){
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update((watermark + copyright).getBytes());
      byte[] digest = md.digest();
      for(int i = 0; i < digest.length; i++){
        securityHash += Integer.toHexString(Math.abs((int)digest[i]));
      }
    }
    String key = attachment.getId() 
        + "-" + attachment.getVersion()
        + "-" + getType(attachment.getMimeType(context))
        + "-" + attachment.getDate().getTime()
        + "-" + dimension.getWidth()
        + "-" + dimension.getHeight()
        + "-" + securityHash;
    return key;
  }

  /**
   * @return the type of the image, as an integer code, used in the generation of the key of the image cache
   */
  public static int getType(String mimeType)
  {
    for (SupportedFormat f : SupportedFormat.values()) {
      if (f.getMimeType().equals(mimeType)) {
        return f.getCode();
      }
    }
    return 0;
  }

  public void flushCache() {
    if ((initializedCache) && (imageCache != null)) {
      imageCache.removeAll();
    }
    imageCache = null;
  }

}
