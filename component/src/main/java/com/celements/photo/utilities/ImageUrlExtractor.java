package com.celements.photo.utilities;

import java.util.List;
import java.util.Map;

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
   * Takes a List of images and groups them by their size (pixels) as parsed from celwidth|celheight
   *
   * @param imageUrls
   *          list of image URLs
   * @return sorted map (@see java.util.TreeMap) returning all image URLs grouped by the image size.
   *         Images from whom the size could not be parsed are grouped in size -1
   */
  public @NotNull Map<Long, List<ImageUrl>> groupImageUrlsBySize(@NotNull List<ImageUrl> imageUrls);

  /**
   * Filters out elements with a map key outside of the interval [minSize,maxSize]
   *
   * @param imageUrls
   *          map of image URLs grouped by their size
   * @param minSideLength
   *          only include images with a minimum side length
   * @param maxSideLength
   *          only include images with a maximum side length
   * @param minPixels
   *          remove map entries with a smaller key (default 1)
   * @param maxPixels
   *          remove map entries with a larger key (default max long)
   * @param keepKeyAsDefault
   *          set to define one key that should be retained, even though it's outside of the
   *          interval (e.g. used for default values)
   * @return reduced map of image URLs
   */
  public @NotNull Map<Long, List<ImageUrl>> filterMinMaxSize(
      @NotNull Map<Long, List<ImageUrl>> groupedImageUrls,
      @Nullable Optional<Integer> minSideLength, @Nullable Optional<Integer> maxSideLength,
      @Nullable Optional<Long> minPixels, @Nullable Optional<Long> maxPixels,
      @Nullable Optional<Long> keepKeyAsDefault);

  /**
   * merge a map into a list, sorted by the key, with direction according to the parameters
   *
   * @param imageUrls
   *          map of image URLs grouped by their size
   * @param imageSizeAsc
   *          return the list sorted by size in ascending order
   * @param imageEncounterAsc
   *          return multiple URLs with the same size in ascending order of their occurrence in the
   *          map value
   * @return list of image URLs in the defined order
   */
  public @NotNull List<ImageUrl> getImagesListForSizeMap(
      @NotNull Map<Long, List<ImageUrl>> groupedImageUrls, boolean imageSizeAsc,
      boolean imageEncounterAsc);

}
