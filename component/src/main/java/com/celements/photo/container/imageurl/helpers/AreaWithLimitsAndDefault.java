package com.celements.photo.container.imageurl.helpers;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.photo.container.ImageUrl;
import com.google.common.base.Optional;

class AreaWithLimitsAndDefault {

  private static final Logger LOGGER = LoggerFactory.getLogger(AreaWithLimitsAndDefault.class);

  public static final int MAX_ALLOWED_DIM = 1000000000;

  /**
   * Return the image area (number of pixels) for an ImageUrl. If only one out of width and height
   * is set we assume a square image, thus squaring the availabe side length.
   * To prevent an overflow the side length is limited to MAX_ALLOWED_DIM
   *
   * @param imgUrl
   * @return
   */
  public @NotNull Optional<Long> getAreaKey(@NotNull ImageUrl imgUrl) {
    int w = checkBelowMaxAllowed(imgUrl.getWidth().or(-1));
    int h = checkBelowMaxAllowed(imgUrl.getHeight().or(-1));
    long area = (long) h * w;
    // if either h or w is -1 define the not -1 dimension squared as the area.
    if (area < 0) {
      area = area * area;
    } else if ((h == -1) && (w == -1)) {
      return Optional.absent();
    }
    return Optional.of(area);
  }

  int checkBelowMaxAllowed(int dim) {
    if (dim > MAX_ALLOWED_DIM) {
      LOGGER.warn("parseImgUrlDimension: dim [{}] is larger than maximum allowed "
          + "dimension of [{}] and has bin set to maximum allowed", dim, MAX_ALLOWED_DIM);
      return MAX_ALLOWED_DIM;
    }
    return dim;
  }
}
