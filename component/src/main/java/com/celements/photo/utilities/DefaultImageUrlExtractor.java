package com.celements.photo.utilities;

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
import com.celements.photo.container.imageurl.helpers.LimitImageAreaSizePredicate;
import com.celements.photo.container.imageurl.helpers.LimitImageSideSizePredicate;
import com.celements.photo.container.imageurl.helpers.SocialMedialComparator;
import com.celements.photo.exception.IllegalImageUrlException;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

@Immutable
@Singleton
@Component
public class DefaultImageUrlExtractor implements ImageUrlExtractor {

  public static final Logger LOGGER = LoggerFactory.getLogger(DefaultImageUrlExtractor.class);

  private final Pattern IMG_FROM_HTML_PATTERN = Pattern.compile("<img .*?src=['\"](.*?)['\"].*?/>");

  @Override
  public List<ImageUrl> extractImageUrlList(String content) {
    Preconditions.checkNotNull(content);
    List<ImageUrl> imageUrls = new ArrayList<>();
    Matcher m = IMG_FROM_HTML_PATTERN.matcher(content);
    while (m.find()) {
      try {
        imageUrls.add(new ImageUrl.Builder(m.group(1)).build());
      } catch (IllegalImageUrlException iiue) {
        LOGGER.info("ImageUrl only works for relative URLs. Failed for {}", iiue.getUrl());
      }
    }
    return imageUrls;
  }

  @Override
  public List<ImageUrl> extractImagesSocialMediaUrlList(String content) {
    final int MIN_SOCIAL_MEDIA_IMAGE_SIZE = 200;
    final long MIN_SOCIAL_MEDIA_AREA_SIZE = MIN_SOCIAL_MEDIA_IMAGE_SIZE
        * MIN_SOCIAL_MEDIA_IMAGE_SIZE;
    List<ImageUrl> imageUrls = extractImageUrlList(content);
    Collections.reverse(imageUrls);
    Collections.sort(imageUrls, new SocialMedialComparator());
    return filterMinMaxSize(imageUrls, Optional.of(MIN_SOCIAL_MEDIA_IMAGE_SIZE),
        Optional.<Integer>absent(), Optional.of(MIN_SOCIAL_MEDIA_AREA_SIZE),
        Optional.<Long>absent(), true);
  }

  @Override
  public List<ImageUrl> filterMinMaxSize(List<ImageUrl> groupedImageUrls,
      Optional<Integer> minSideLength, Optional<Integer> maxSideLength, Optional<Long> minPixels,
      Optional<Long> maxPixels, boolean keepUndefinedSize) {
    List<ImageUrl> filteredList = filterByPixels(groupedImageUrls, minPixels.or(1L), maxPixels.or(
        Long.MAX_VALUE), keepUndefinedSize);
    return filterBySideLength(filteredList, minSideLength.or(1), maxSideLength.or(
        Integer.MAX_VALUE), keepUndefinedSize);
  }

  List<ImageUrl> filterByPixels(List<ImageUrl> imageUrls, long minPixels, long maxPixels,
      boolean keepUndefinedSize) {
    Predicate<ImageUrl> predicate = new LimitImageAreaSizePredicate(minPixels, maxPixels,
        keepUndefinedSize);
    return FluentIterable.from(imageUrls).filter(predicate).toList();
  }

  List<ImageUrl> filterBySideLength(List<ImageUrl> filteredList, Integer minSideLength,
      Integer maxSideLength, boolean keepEmptyAsDefault) {
    LimitImageSideSizePredicate predicate = new LimitImageSideSizePredicate(minSideLength,
        maxSideLength, keepEmptyAsDefault);
    return FluentIterable.from(filteredList).filter(predicate).toList();
  }

}
