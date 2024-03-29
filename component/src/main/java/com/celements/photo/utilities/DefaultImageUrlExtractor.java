package com.celements.photo.utilities;

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.photo.container.ImageUrl;
import com.celements.photo.container.imageurl.helpers.ImageAreaSizeComparator;
import com.celements.photo.container.imageurl.helpers.LimitImageAreaSizePredicate;
import com.celements.photo.container.imageurl.helpers.LimitImageSideLengthPredicate;
import com.celements.photo.exception.IllegalImageUrlException;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@Immutable
@Singleton
@Component
public class DefaultImageUrlExtractor implements ImageUrlExtractor {

  public static final Logger LOGGER = LoggerFactory.getLogger(DefaultImageUrlExtractor.class);

  private final int MIN_SOCIAL_MEDIA_IMAGE_SIZE = 200;
  private final long MIN_SOCIAL_MEDIA_AREA_SIZE = MIN_SOCIAL_MEDIA_IMAGE_SIZE
      * MIN_SOCIAL_MEDIA_IMAGE_SIZE;

  private final Pattern IMG_FROM_HTML_PATTERN = Pattern.compile("<img .*?src=['\"](.*?)['\"].*?/>");

  @Override
  public List<ImageUrl> extractImageUrlList(String content) {
    Preconditions.checkNotNull(content);
    List<ImageUrl> imageUrls = new ArrayList<>();
    Matcher m = IMG_FROM_HTML_PATTERN.matcher(content);
    while (m.find()) {
      try {
        imageUrls.add(new ImageUrl.Builder(convertToAbsoluteImageUrl(m.group(1))).build());
      } catch (IllegalImageUrlException iiue) {
        LOGGER.info("ImageUrl only works for relative URLs. Failed for {}", iiue.getUrl());
      }
    }
    return imageUrls;
  }

  private String convertToAbsoluteImageUrl(String url) {
    if (checkNotNull(url).startsWith("../../download/")) {
      return url.replace("../../download/", "/download/");
    }
    return url;
  }

  // TODO CELDEV-474 On reuse resolve the issue
  @Override
  public List<ImageUrl> extractImagesSocialMediaUrlList(String content) {
    List<ImageUrl> imageUrls = extractImageUrlList(content);
    Collections.reverse(imageUrls);
    Collections.sort(imageUrls, new ImageAreaSizeComparator());
    Predicate<ImageUrl> predicateArea = new LimitImageAreaSizePredicate(MIN_SOCIAL_MEDIA_AREA_SIZE,
        Long.MAX_VALUE, true);
    Predicate<ImageUrl> predicateSideLength = new LimitImageSideLengthPredicate(
        MIN_SOCIAL_MEDIA_IMAGE_SIZE, Integer.MAX_VALUE, true);
    return FluentIterable.from(FluentIterable.from(imageUrls).filter(
        predicateArea).toList()).filter(predicateSideLength).toList();
  }
}
