package com.celements.photo.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.model.context.ModelContext;
import com.celements.photo.container.ImageUrlDim;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

@Immutable
@Singleton
@Component
public class DefaultImageUrlExtractor implements ImageUrlExtractor {

  public static final Logger LOGGER = LoggerFactory.getLogger(DefaultImageUrlExtractor.class);

  public static final int MAX_ALLOWED_DIM = 1000000000;
  public static final int MIN_SOCIAL_MEDIA_IMAGE_SIZE = 200;
  public static final int MIN_SOCIAL_MEDIA_AREA_SIZE = MIN_SOCIAL_MEDIA_IMAGE_SIZE
      * MIN_SOCIAL_MEDIA_IMAGE_SIZE;

  private final Pattern IMG_FROM_HTML_PATTERN = Pattern.compile("<img .*?src=['\"](.*?)['\"].*?/>");
  private final Pattern EXTRACT_URL_PART_PATTERN = Pattern.compile("/([^/?]*)");
  private final Pattern CELWIDTH_FROM_URL_PATTERN = Pattern.compile("^.*celwidth=(\\d+)(&.*)?$");
  private final Pattern CELHEIGHT_FROM_URL_PATTERN = Pattern.compile("^.*celheight=(\\d+)(&.*)?$");

  @Override
  public @NotNull List<ImageUrlDim> extractImagesList(@NotNull String content) {
    Map<Long, List<ImageUrlDim>> articleImages = new TreeMap<>();
    Matcher m = IMG_FROM_HTML_PATTERN.matcher(content);
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
      if ((imgArea == -1) || (imgArea >= MIN_SOCIAL_MEDIA_AREA_SIZE)) {
        Collections.reverse(articleImages.get(imgArea));
        sortedImages.addAll(articleImages.get(imgArea));
      }
    }
    return sortedImages;
  }

  ImageUrlDim getImgUrlExternal(String imgUrl) {
    if (imgUrl.startsWith("/")) {
      Matcher matcher = EXTRACT_URL_PART_PATTERN.matcher(imgUrl);
      String action = getNextMatchedPart(matcher);
      String space = getNextMatchedPart(matcher);
      String docname = getNextMatchedPart(matcher);
      String filename = getNextMatchedPart(matcher);
      String query = imgUrl.substring(Math.min(imgUrl.indexOf('?') + 1, imgUrl.length()));
      XWikiContext context = Utils.getComponent(ModelContext.class).getXWikiContext();
      imgUrl = context.getURLFactory().createAttachmentURL(filename, space, docname, action, query,
          context.getDatabase(), context).toString();
    }
    return new ImageUrlDim(imgUrl, parseImgUrlDimension(imgUrl, CELWIDTH_FROM_URL_PATTERN),
        parseImgUrlDimension(imgUrl, CELHEIGHT_FROM_URL_PATTERN));
  }

  String getNextMatchedPart(Matcher m) {
    if (m.find()) {
      return m.group(1);
    }
    return "";
  }

  Long getImgUrlSizeKey(String imgUrl) {
    long w = parseImgUrlDimension(imgUrl, CELWIDTH_FROM_URL_PATTERN);
    long h = parseImgUrlDimension(imgUrl, CELHEIGHT_FROM_URL_PATTERN);
    long area = h * w;
    if (area < 0) {
      area = area * area;
    } else if ((h == -1) && (w == -1)) {
      area = -1;
    }
    return area;
  }

  int parseImgUrlDimension(String imgUrl, Pattern dimensionPattern) {
    Matcher m = dimensionPattern.matcher(imgUrl);
    if (m.find()) {
      String str = m.group(1);
      try {
        int dim = Integer.parseInt(str);
        if (dim > MAX_ALLOWED_DIM) {
          LOGGER.warn("parseImgUrlDimension: Maximum allowed dimension of [{}] exceeded.",
              MAX_ALLOWED_DIM);
          dim = MAX_ALLOWED_DIM;
        }
        return dim;
      } catch (NumberFormatException nfe) {
        LOGGER.debug("Exception while parsing Integer from [{}]", str, nfe);
      }
    }
    return -1;
  }
}
