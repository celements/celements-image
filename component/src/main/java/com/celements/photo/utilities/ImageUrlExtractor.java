package com.celements.photo.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.model.context.ModelContext;
import com.celements.photo.container.ImageUrlDim;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class ImageUrlExtractor {

  public static final Logger LOGGER = LoggerFactory.getLogger(ImageUrlExtractor.class);

  public static int MIN_SOCIAL_MEDIA_IMAGE_SIZE = 200;
  public static int MIN_SOCIAL_MEDIA_AREA_SIZE = MIN_SOCIAL_MEDIA_IMAGE_SIZE
      * MIN_SOCIAL_MEDIA_IMAGE_SIZE;

  public List<ImageUrlDim> extractImagesList(String content) {
    String regex = "<img .*?src=['\"](.*?)['\"].*?/>";
    Map<Long, List<ImageUrlDim>> articleImages = new TreeMap<>();
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(content);
    while (m.find()) {
      String imgUrl = m.group(1);
      Long key = getImgUrlSizeKey(imgUrl);
      if (!articleImages.containsKey(key)) {
        articleImages.put(key, new ArrayList<ImageUrlDim>());
      }
      articleImages.get(key).add(getImgUrlExternal(imgUrl));
    }
    return filterImagesByMissingOrSmallSize(articleImages);
  }

  List<ImageUrlDim> filterImagesByMissingOrSmallSize(Map<Long, List<ImageUrlDim>> articleImages) {
    List<ImageUrlDim> sortedImages = new ArrayList<>();
    for (Long imgArea : articleImages.keySet()) {
      // Image size not extractable is in key == '-1' Don't include too small images.
      if ((imgArea == -1) || (imgArea >= (MIN_SOCIAL_MEDIA_AREA_SIZE))) {
        Collections.reverse(articleImages.get(imgArea));
        sortedImages.addAll(articleImages.get(imgArea));
      }
    }
    return sortedImages;
  }

  ImageUrlDim getImgUrlExternal(String imgUrl) {
    if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
      String action = imgUrl.replaceAll("^.*?/(.*?)/.*$", "$1");
      String space = imgUrl.replaceAll("^(.*?/){2}(.*?)/.*$", "$2");
      String docname = imgUrl.replaceAll("^(.*?/){3}(.*?)/.*$", "$2");
      String filename = imgUrl.replaceAll("^(.*?/){4}(.*?)(|\\?.*)$", "$2");
      String query = imgUrl.replaceAll("^.*\\?(.*)$", "$1");
      XWikiContext context = Utils.getComponent(ModelContext.class).getXWikiContext();
      imgUrl = context.getURLFactory().createAttachmentURL(filename, space, docname, action, query,
          context.getDatabase(), context).toString();
    }
    return new ImageUrlDim(imgUrl, parseImgUrlDimension(imgUrl, "celwidth"), parseImgUrlDimension(
        imgUrl, "celheight"));
  }

  Long getImgUrlSizeKey(String imgUrl) {
    int w = Math.min(parseImgUrlDimension(imgUrl, "celwidth"), 1000000000);
    int h = Math.min(parseImgUrlDimension(imgUrl, "celheight"), 1000000000);
    Long area = new Long(h * w);
    if (area < 0) {
      area = area * area;
    } else if ((h == -1) && (w == -1)) {
      area = -1l;
    }
    return area;
  }

  int parseImgUrlDimension(String imgUrl, String dimension) {
    String str = imgUrl.replaceAll("^.*" + dimension + "=(\\d+)(&.*)?$", "$1");
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException nfe) {
      LOGGER.debug("Exception while parsing Integer from [{}]", str, nfe);
    }
    return -1;
  }
}
