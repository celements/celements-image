package com.celements.photo.utilities;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.photo.container.ImageUrlDim;

@ComponentRole
public interface ImageUrlExtractor {

  public @NotNull List<ImageUrlDim> extractImagesList(@NotNull String content);

}
