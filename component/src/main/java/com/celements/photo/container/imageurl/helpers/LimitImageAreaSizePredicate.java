package com.celements.photo.container.imageurl.helpers;

import javax.annotation.concurrent.Immutable;

import com.celements.photo.container.ImageUrl;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

@Immutable
public class LimitImageAreaSizePredicate implements Predicate<ImageUrl> {

  private final long minPixels;
  private final long maxPixels;
  private final boolean keepUndefinedSize;

  public LimitImageAreaSizePredicate(long minPixels, long maxPixels, boolean keepUndefinedSize) {
    this.minPixels = minPixels;
    this.maxPixels = maxPixels;
    this.keepUndefinedSize = keepUndefinedSize;
  }

  @Override
  public boolean apply(ImageUrl key) {
    Optional<Long> area = new AreaWithLimitsAndDefault().getAreaKey(key);
    return keepUndefined(area) || (area.isPresent() && (minPixels <= area.get())
        && (area.get() <= maxPixels));
  }

  boolean keepUndefined(Optional<Long> area) {
    return keepUndefinedSize && !area.isPresent();
  }

}
