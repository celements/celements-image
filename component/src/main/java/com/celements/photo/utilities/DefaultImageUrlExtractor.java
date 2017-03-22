package com.celements.photo.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.photo.container.ImageUrl;
import com.celements.photo.exception.IllegalImageUrlException;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Immutable
@Singleton
@Component
public class DefaultImageUrlExtractor implements ImageUrlExtractor {

  public static final Logger LOGGER = LoggerFactory.getLogger(DefaultImageUrlExtractor.class);

  public static final int MAX_ALLOWED_DIM = 1000000000;
  public static final int MIN_SOCIAL_MEDIA_IMAGE_SIZE = 200;
  public static final long MIN_SOCIAL_MEDIA_AREA_SIZE = MIN_SOCIAL_MEDIA_IMAGE_SIZE
      * MIN_SOCIAL_MEDIA_IMAGE_SIZE;

  private final Pattern IMG_FROM_HTML_PATTERN = Pattern.compile("<img .*?src=['\"](.*?)['\"].*?/>");

  @Override
  public @NotNull List<ImageUrl> extractImageUrlList(@NotNull String content) {
    Preconditions.checkNotNull(content);
    List<ImageUrl> imageUrls = new ArrayList<>();
    Matcher m = IMG_FROM_HTML_PATTERN.matcher(content);
    while (m.find()) {
      try {
        imageUrls.add(new ImageUrl.Builder().url(m.group(1)).build());
      } catch (IllegalImageUrlException iiue) {
        LOGGER.info("ImageUrl only works for relative URLs. Failed for {}", iiue.getUrl());
      }
    }
    return imageUrls;
  }

  @Override
  public @NotNull List<ImageUrl> extractImagesSocialMediaUrlList(@NotNull String content) {
    List<ImageUrl> imageUrls = extractImageUrlList(content);
    return getImagesListForSizeMap(filterMinMaxSize(groupImageUrlsBySize(imageUrls), Optional.of(
        MIN_SOCIAL_MEDIA_IMAGE_SIZE), Optional.<Integer>absent(), Optional.of(
            MIN_SOCIAL_MEDIA_AREA_SIZE), Optional.<Long>absent(), Optional.of(-1L)), true, false);
  }

  @Override
  public @NotNull Map<Long, List<ImageUrl>> groupImageUrlsBySize(
      @NotNull List<ImageUrl> imageUrls) {
    Map<Long, List<ImageUrl>> articleImages = new TreeMap<>();
    for (ImageUrl imgUrl : imageUrls) {
      Long key = getImgUrlSizeKey(imgUrl);
      if (!articleImages.containsKey(key)) {
        articleImages.put(key, new ArrayList<ImageUrl>());
      }
      articleImages.get(key).add(imgUrl);
    }
    return articleImages;
  }

  @Override
  public @NotNull Map<Long, List<ImageUrl>> filterMinMaxSize(
      @NotNull Map<Long, List<ImageUrl>> groupedImageUrls,
      @Nullable Optional<Integer> minSideLength, @Nullable Optional<Integer> maxSideLength,
      @Nullable Optional<Long> minPixels, @Nullable Optional<Long> maxPixels,
      @Nullable Optional<Long> keepKeyAsDefault) {
    Map<Long, List<ImageUrl>> filteredMap = filterByPixels(groupedImageUrls, minPixels.or(1L),
        maxPixels.or(Long.MAX_VALUE), keepKeyAsDefault);
    filterBySideLength(filteredMap, minSideLength.or(1), maxSideLength.or(MAX_ALLOWED_DIM),
        keepKeyAsDefault);
    return filteredMap;
  }

  @Override
  public @NotNull List<ImageUrl> getImagesListForSizeMap(
      @NotNull Map<Long, List<ImageUrl>> groupedImageUrls, boolean imageSizeAsc,
      boolean imageEncounterAsc) {
    if (!(groupedImageUrls instanceof SortedMap)) {
      groupedImageUrls = new TreeMap<>(groupedImageUrls);
    }
    List<ImageUrl> sortedImages = new ArrayList<>();
    for (Long imgArea : groupedImageUrls.keySet()) {
      if (imageEncounterAsc != imageSizeAsc) { // xor
        Collections.reverse(groupedImageUrls.get(imgArea));
      }
      sortedImages.addAll(groupedImageUrls.get(imgArea));
    }
    if (!imageSizeAsc) {
      Collections.reverse(sortedImages);
    }
    return sortedImages;
  }

  @NotNull
  Long getImgUrlSizeKey(ImageUrl imgUrl) {
    int w = checkBelowMaxAllowed(imgUrl.getWidth().or(-1));
    int h = checkBelowMaxAllowed(imgUrl.getHeight().or(-1));
    long area = (long) h * w;
    // if either h or w is -1 define the not -1 dimension squared as the area.
    if (area < 0) {
      area = area * area;
    } else if ((h == -1) && (w == -1)) {
      area = -1;
    }
    return area;
  }

  int checkBelowMaxAllowed(int dim) {
    if (dim > MAX_ALLOWED_DIM) {
      LOGGER.warn("parseImgUrlDimension: dim [{}] is larger than maximum allowed "
          + "dimension of [{}] and has bin set to maximum allowed", dim, MAX_ALLOWED_DIM);
      return MAX_ALLOWED_DIM;
    }
    return dim;
  }

  Map<Long, List<ImageUrl>> filterByPixels(Map<Long, List<ImageUrl>> groupedImageUrls,
      long minPixels, long maxPixels, Optional<Long> keepKeyAsDefault) {
    Predicate<Long> predicate = getPixelSizeFilterPredicate(minPixels, maxPixels, keepKeyAsDefault);
    Map<Long, List<ImageUrl>> filteredMap = new TreeMap<>(groupedImageUrls);
    for (Long key : Iterables.filter(groupedImageUrls.keySet(), predicate)) {
      filteredMap.remove(key);
    }
    return filteredMap;
  }

  Predicate<Long> getPixelSizeFilterPredicate(final long minPixels, final long maxPixels,
      final Optional<Long> keepKeyAsDefault) {
    Predicate<Long> predicate = new Predicate<Long>() {

      @Override
      public boolean apply(Long key) {
        return (!keepKeyAsDefault.isPresent() || (key != keepKeyAsDefault.get()))
            && ((key < minPixels) || (maxPixels < key));
      }
    };
    return predicate;
  }

  void filterBySideLength(Map<Long, List<ImageUrl>> filteredMap, Integer minSideLength,
      Integer maxSideLength, Optional<Long> keepKeyAsDefault) {
    for (Long key : ImmutableSet.copyOf(filteredMap.keySet())) {
      if (!keepKeyAsDefault.isPresent() || (key != keepKeyAsDefault.get())) {
        List<ImageUrl> imageUrls = new ArrayList<>(filteredMap.get(key));
        for (ImageUrl imgUrl : Iterables.filter(filteredMap.get(key), getSideLengthFilterPredicate(
            minSideLength, maxSideLength))) {
          imageUrls.remove(imgUrl);
        }
        if (imageUrls.isEmpty()) {
          filteredMap.remove(key);
        } else {
          filteredMap.put(key, imageUrls);
        }
      }
    }
  }

  Predicate<ImageUrl> getSideLengthFilterPredicate(final Integer minSideLength,
      final Integer maxSideLength) {
    Predicate<ImageUrl> predicate = new Predicate<ImageUrl>() {

      @Override
      public boolean apply(ImageUrl imgUrl) {
        Optional<Integer> w = imgUrl.getWidth();
        Optional<Integer> h = imgUrl.getHeight();
        return (!w.isPresent() && !h.isPresent()) || (w.isPresent() && ((w.get() < minSideLength)
            || (maxSideLength < w.get()))) || (h.isPresent() && ((h.get() < minSideLength)
                || (maxSideLength < h.get())));
      }
    };
    return predicate;
  }

}
