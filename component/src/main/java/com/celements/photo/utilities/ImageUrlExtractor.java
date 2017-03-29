package com.celements.photo.utilities;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.photo.container.ImageUrl;

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
   * TODO CELDEV-474 On reuse resolve the issue
   * Returns all relative (internal) image URLs that match certain size criteria to be shown on
   * social media sites. The list is sorted by image area size ascending.
   *
   * @param content
   *          An HTML snippet.
   * @return list containing all social media usable images
   */
  public @NotNull List<ImageUrl> extractImagesSocialMediaUrlList(@NotNull String content);

}
