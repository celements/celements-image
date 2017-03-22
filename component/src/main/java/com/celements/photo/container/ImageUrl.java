package com.celements.photo.container;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.model.context.ModelContext;
import com.celements.photo.exception.IllegalImageUrlException;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class ImageUrl {

  public static final class Builder {

    private String url;
    private String action;
    private String space;
    private String name;
    private String filename;
    private String query;
    private Integer width;
    private Integer height;

    public @NotNull Builder url(@NotNull String url) throws IllegalImageUrlException {
      if (Preconditions.checkNotNull(url).startsWith("/")) {
        this.url = url;
      } else {
        throw new IllegalImageUrlException(url);
      }
      return this;
    }

    public @NotNull Builder action(@NotNull String action) {
      this.action = Preconditions.checkNotNull(action);
      return this;
    }

    public @NotNull Builder space(@NotNull String space) {
      this.space = Preconditions.checkNotNull(space);
      return this;
    }

    public @NotNull Builder name(@NotNull String name) {
      this.name = Preconditions.checkNotNull(name);
      return this;
    }

    public @NotNull Builder filename(@NotNull String filename) {
      this.filename = Preconditions.checkNotNull(filename);
      return this;
    }

    public @NotNull Builder query(@NotNull String query) {
      this.query = Preconditions.checkNotNull(query);
      return this;
    }

    public @NotNull Builder width(int width) {
      this.width = width;
      return this;
    }

    public @NotNull Builder height(int height) {
      this.height = height;
      return this;
    }

    public ImageUrl build() {
      return new ImageUrl(this);
    }

  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageUrl.class);

  private final Pattern EXTRACT_URL_PART_PATTERN = Pattern.compile("/([^/?]*)");
  private final Pattern CELWIDTH_FROM_URL_PATTERN = Pattern.compile("^.*celwidth=(\\d+)(&.*)?$");
  private final Pattern CELHEIGHT_FROM_URL_PATTERN = Pattern.compile("^.*celheight=(\\d+)(&.*)?$");

  private boolean isParsed;
  private URL url;
  private String urlStr;
  private String action;
  private String space;
  private String name;
  private String filename;
  private String query;
  private Integer width;
  private Integer height;

  private ImageUrl(Builder builder) {
    this.urlStr = builder.url;
    this.action = builder.action;
    this.space = builder.space;
    this.name = builder.name;
    this.filename = builder.filename;
    this.query = builder.query;
    this.width = builder.width;
    this.height = builder.height;
    this.isParsed = false;
  }

  public String getUrl() {
    return getContext().getURLFactory().getURL(getUrlInternal(), getContext());
  }

  public String getExternalUrl() {
    return getUrlInternal().toString();
  }

  public @NotNull Optional<String> getAction() {
    parseUrl();
    return Optional.fromNullable(action);
  }

  public @NotNull Optional<String> getSpace() {
    parseUrl();
    return Optional.fromNullable(space);
  }

  public @NotNull Optional<String> getName() {
    parseUrl();
    return Optional.fromNullable(name);
  }

  public @NotNull Optional<String> getFilename() {
    parseUrl();
    return Optional.fromNullable(filename);
  }

  public @NotNull Optional<String> getQuery() {
    parseUrl();
    return Optional.fromNullable(query);
  }

  public @NotNull Optional<Integer> getWidth() {
    if (width == null) {
      width = parseQueryDimension(CELWIDTH_FROM_URL_PATTERN);
    }
    if (width > 0) {
      return Optional.of(width);
    }
    return Optional.absent();
  }

  public @NotNull Optional<Integer> getHeight() {
    if (height == null) {
      height = parseQueryDimension(CELHEIGHT_FROM_URL_PATTERN);
    }
    if (height > 0) {
      return Optional.of(height);
    }
    return Optional.absent();
  }

  @Override
  public String toString() {
    return getUrl();
  }

  void parseUrl() {
    if (!isParsed && !Strings.isNullOrEmpty(urlStr)) {
      Matcher matcher = EXTRACT_URL_PART_PATTERN.matcher(urlStr);
      action = getNextMatchedPart(matcher);
      space = getNextMatchedPart(matcher);
      name = getNextMatchedPart(matcher);
      filename = getNextMatchedPart(matcher);
      int queryStart = urlStr.indexOf('?');
      if (queryStart >= 0) {
        query = urlStr.substring(queryStart + 1);
      } else {
        query = "";
      }
      isParsed = true;
    }
  }

  Integer parseQueryDimension(Pattern dimensionPattern) {
    parseUrl();
    if (!Strings.isNullOrEmpty(query)) {
      Matcher m = dimensionPattern.matcher(query);
      if (m.find()) {
        String str = m.group(1);
        try {
          return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
          LOGGER.debug("Exception while parsing Integer from [{}]", str, nfe);
        }
      }
    }
    return -1;
  }

  String getNextMatchedPart(Matcher m) {
    if (m.find()) {
      return m.group(1);
    }
    return "";
  }

  URL getUrlInternal() {
    parseUrl();
    if (url == null) {
      url = getContext().getURLFactory().createAttachmentURL(filename, space, name, action, query,
          getContext().getDatabase(), getContext());
    }
    return url;
  }

  private XWikiContext getContext() {
    return Utils.getComponent(ModelContext.class).getXWikiContext();
  }
}