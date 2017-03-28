package com.celements.photo.container.imageurl.helpers;

import java.io.Serializable;
import java.util.Comparator;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.photo.container.ImageUrl;
import com.google.common.base.Optional;

/**
 * Sorts images by their size (width * height) ascending. Images with the same size remain in the
 * order of their appearance.
 */
@Immutable
public class SocialMedialComparator implements Comparator<ImageUrl>, Serializable {

  private static final long serialVersionUID = 2816775271883495599L;

  @Override
  public int compare(@NotNull ImageUrl img1, @NotNull ImageUrl img2) {
    AreaWithLimitsAndDefault areaHelper = new AreaWithLimitsAndDefault();
    Optional<Long> area1 = areaHelper.getAreaKey(img1);
    Optional<Long> area2 = areaHelper.getAreaKey(img2);
    if ((!area1.isPresent() && area2.isPresent()) || (area1.isPresent() && area2.isPresent()
        && (area1.get() < area2.get()))) {
      return -1;
    } else if ((area1.isPresent() && !area2.isPresent()) || (area1.isPresent() && area2.isPresent()
        && (area1.get() > area2.get()))) {
      return 1;
    }
    return 0;
  }

}
