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
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.context.Execution;

import com.celements.photo.plugin.CelementsPhotoPlugin.SupportedFormat;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.web.Utils;

public class MetaDataCacheCommand {

  private static final Log LOGGER = LogFactory.getFactory().getInstance(
      MetaDataCacheCommand.class);

  /**
   * Cache for already served images.
   */
  private Cache<Map<String, String>> imageMetaCache;

  private boolean initializedCache;

  /**
   * The size of the cache. This parameter can be configured using
   * the key <tt>xwiki.plugin.image.cache.capacity</tt>.
   */
  private int capacity = 50;

  /**
   * The time to live (seconds) of a cache entry after last access. This parameter can be
   * configured using the key <tt>xwiki.plugin.image.cache.ttl</tt>.
   */
  private Integer ttlConfig = 2500000;

  public MetaDataCacheCommand() {
    initializedCache = false;
  }

  Cache<Map<String, String>> getImageCache() {
    if (!initializedCache) {
      initCache();
    }
    return imageMetaCache;
  }

  /* Copy, Paste & Customize from com.xpn.xwiki.plugin.image */
  synchronized void initCache() {
    CacheConfiguration configuration = new CacheConfiguration();
    
    configuration.setConfigurationId("celements.photo.meta");
    
    // Set folder o store cache
    File tempDir = getContext().getWiki().getTempDirectory(getContext());
    File imgTempDir = new File(tempDir, configuration.getConfigurationId());
    try {
      imgTempDir.mkdirs();
    } catch (Exception ex) {
      LOGGER.warn("Cannot create temporary files", ex);
    }
    configuration.put("cache.path", imgTempDir.getAbsolutePath());
    
    // Set cache constraints
    LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
    capacity = readIntegerValue("xwiki.plugin.image.cache.capacity", capacity);
    lru.setMaxEntries(capacity);
    ttlConfig = readIntegerValue("xwiki.plugin.image.cache.ttl", ttlConfig);
    lru.setTimeToLive(ttlConfig);
    LOGGER.debug("creating an image cache with capacity [" + lru.getMaxEntries()
        + "] and ttl [" + lru.getTimeToLive() + "] and cache.path ["
        + lru.get("cache.path") + "].");
    configuration.put(LRUEvictionConfiguration.CONFIGURATIONID, lru);

    try {
      imageMetaCache = getCacheManager().createNewCache(configuration);
    } catch (CacheException exp) {
      LOGGER.error("Error initializing the image cache.", exp);
    }
    initializedCache = true;
  }

  private Integer readIntegerValue(String paramKey, Integer defaultValue) {
    String capacityParam = "";
    try {
      capacityParam = getContext().getWiki().Param(paramKey);
      if ((capacityParam != null) && (!capacityParam.equals(""))) {
        return Integer.parseInt(capacityParam);
      }
    } catch (NumberFormatException ex) {
      LOGGER.error("Error in ImagePlugin reading capacity: " + capacityParam, ex);
    }
    return defaultValue;
  }

  public Map<String, String> getImageForKey(String key) {
    if (getImageCache() != null) {
      return getImageCache().get(key);
    }
    return null;
  }
  
  public void addToCache(String key, Map<String, String> metaInfo) {
    if (getImageCache() != null) {
      getImageCache().set(key, metaInfo);
    } else {
      LOGGER.info("Caching of images meta data deactivated.");
    }
  }

  String getCacheKey(XWikiAttachment attachment) throws NoSuchAlgorithmException {
    String key = attachment.getId() 
        + "-" + attachment.getVersion()
        + "-" + getType(attachment.getMimeType(getContext()))
        + "-" + attachment.getDate().getTime();
    return key;
  }

  /**
   * @return the type of the image, as an integer code, used in the generation
   *  of the key of the image cache
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
    if ((initializedCache) && (imageMetaCache != null)) {
      imageMetaCache.removeAll();
    }
    imageMetaCache = null;
  }

  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }

  private CacheManager getCacheManager() {
    return Utils.getComponent(CacheManager.class);
  }

}
