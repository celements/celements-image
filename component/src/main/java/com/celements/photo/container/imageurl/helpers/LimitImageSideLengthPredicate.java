package com.celements.photo.container.imageurl.helpers;

import javax.annotation.concurrent.Immutable;

import com.celements.photo.container.ImageUrl;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * Returns true if both side lengths of the ImageUrls are within [minSideLength,maxSideLength]
 * An exception is made for ImageUrls where either width OR height is missing. For those the
 * only the available side is checked. Depending on keepEmptyAsDefault true / false is returned for
 * ImageUrls with width AND height missing.
 */
@Immutable
public class LimitImageSideLengthPredicate implements Predicate<ImageUrl> {

  private final int minSideLength;
  private final int maxSideLength;
  private final boolean keepEmptyAsDefault;

  public LimitImageSideLengthPredicate(int minSideLength, int maxSideLength,
      boolean keepEmptyAsDefault) {
    this.minSideLength = minSideLength;
    this.maxSideLength = maxSideLength;
    this.keepEmptyAsDefault = keepEmptyAsDefault;
  }

  @Override
  public boolean apply(ImageUrl imgUrl) {
    Optional<Integer> w = imgUrl.getWidth();
    Optional<Integer> h = imgUrl.getHeight();
    return keepEmptyDefault(w, h) || (existsInRange(w) && notOutOfRange(h)) || (notOutOfRange(w)
        && existsInRange(h));
  }

  boolean keepEmptyDefault(Optional<Integer> w, Optional<Integer> h) {
    return keepEmptyAsDefault && !w.isPresent() && !h.isPresent();
  }

  boolean notOutOfRange(Optional<Integer> x) {
    return !x.isPresent() || existsInRange(x);
  }

  boolean existsInRange(Optional<Integer> x) {
    return x.isPresent() && (minSideLength <= x.get()) && (x.get() <= maxSideLength);
  }
}
