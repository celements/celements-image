package com.celements.photo.utilities;

import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.photo.container.ImageUrl;
import com.google.common.base.Optional;

@ComponentRole
public interface ImageUrlExtractor {

  /**
   * Returns all relative (internal) image URLs extracted from an HTML snippet.
   *
   * @param content
   *          An HTML snippet.
   * @return list containing all the images in content
   */
  public @NotNull List<ImageUrl> extractImageUrlList(@NotNull String content);

  /**
   * Returns all relative (internal) image URLs that match certain size criteria to be shown on
   * social media sites. The list is sorted by image area size ascending.
   *
   * @param content
   *          An HTML snippet.
   * @return list containing all social media usable images
   */
  public @NotNull List<ImageUrl> extractImagesSocialMediaUrlList(@NotNull String content);

  /**
   * Filters out elements outside of size limits
   *
   * @param imageUrls
   *          list of image URLs
   * @param minSideLength
   *          only include images with a minimum side length
   * @param maxSideLength
   *          only include images with a maximum side length
   * @param minPixels
   *          remove map entries with a smaller key (default 1)
   * @param maxPixels
   *          remove map entries with a larger key (default max long)
   * @param keepUndefinedSize
   *          keep image URLs with indeterminable size by default
   * @return reduced map of image URLs
   */
  public @NotNull List<ImageUrl> filterMinMaxSize(@NotNull List<ImageUrl> groupedImageUrls,
      @Nullable Optional<Integer> minSideLength, @Nullable Optional<Integer> maxSideLength,
      @Nullable Optional<Long> minPixels, @Nullable Optional<Long> maxPixels,
      @Nullable boolean keepUndefinedSize);

}
