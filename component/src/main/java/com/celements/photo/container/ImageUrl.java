package com.celements.photo.container;

import static com.google.common.base.Preconditions.*;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.model.context.ModelContext;
import com.celements.photo.exception.IllegalImageUrlException;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class ImageUrl {

  // TODO CELDEV-466 There are some cases where the builder is not as predictable and flexible as
  // one might expect. For details see the improvement issue
  public static final class Builder {

    private String url;
    private String action;
    private String space;
    private String docname;
    private String filename;
    private String query;
    private Integer width;
    private Integer height;

    public Builder(@NotNull String url) throws IllegalImageUrlException {
      url(url);
    }

    public Builder(@NotNull String spaceName, @NotNull String docName, @NotNull String fileName) {
      space(spaceName);
      docname(docName);
      filename(fileName);
    }

    public @NotNull Builder url(@NotNull String url) throws IllegalImageUrlException {
      // TODO CELDEV- Check URL using Regexp (needed are space, docname and filename)
      if (checkNotNull(url).startsWith("/")) {
        this.url = url;
      } else {
        throw new IllegalImageUrlException(url);
      }
      return this;
    }

    public @NotNull Builder action(@NotNull String action) {
      this.action = checkNotNull(action);
      return this;
    }

    public @NotNull Builder space(@NotNull String space) {
      this.space = checkNotNull(space);
      return this;
    }

    public @NotNull Builder docname(@NotNull String docname) {
      this.docname = checkNotNull(docname);
      return this;
    }

    public @NotNull Builder filename(@NotNull String filename) {
      this.filename = checkNotNull(filename);
      return this;
    }

    public @NotNull Builder query(@NotNull String query) {
      this.query = checkNotNull(query);
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

    public @NotNull ImageUrl build() {
      return new ImageUrl(this);
    }

  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageUrl.class);

  private final String DEFAULT_ACTION = "download";
  private final String DEFAULT_QUERY = "";
  private final Pattern EXTRACT_URL_PART_PATTERN = Pattern.compile("/([^/?]*)");
  private final Pattern CELWIDTH_FROM_URL_PATTERN = Pattern.compile("^.*celwidth=(\\d+)(&.*)?$");
  private final Pattern CELHEIGHT_FROM_URL_PATTERN = Pattern.compile("^.*celheight=(\\d+)(&.*)?$");

  private boolean isParsed;
  private URL url;
  private String urlStr;
  private String action;
  private String space;
  private String docname;
  private String filename;
  private String query;
  private Integer width;
  private Integer height;

  private ImageUrl(Builder builder) {
    this.urlStr = builder.url;
    this.action = builder.action;
    this.space = builder.space;
    this.docname = builder.docname;
    this.filename = builder.filename;
    this.query = builder.query;
    this.width = builder.width;
    this.height = builder.height;
    this.isParsed = false;
  }

  public @NotNull String getUrl() {
    return getXWikiContext().getURLFactory().getURL(getUrlInternal(), getXWikiContext());
  }

  public @NotNull String getExternalUrl() {
    return getUrlInternal().toString();
  }

  public @NotNull Optional<String> getAction() {
    parseUrl();
    return Optional.fromNullable(action);
  }

  public @NotNull String getSpace() {
    parseUrl();
    return space;
  }

  public @NotNull String getDocname() {
    parseUrl();
    return docname;
  }

  public @NotNull String getFilename() {
    parseUrl();
    return filename;
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

  // TODO CELDEV-468 Implement hashCode/equals in ImageUrl

  @Override
  public String toString() {
    return getUrl();
  }

  // TODO CELDEV-464 Make ImageUrl.parseUrl() work with prefix URL configurations
  void parseUrl() {
    if (!isParsed && !Strings.isNullOrEmpty(urlStr)) {
      synchronized (this) {
        Matcher matcher = EXTRACT_URL_PART_PATTERN.matcher(urlStr);
        action = MoreObjects.firstNonNull(action, getNextMatchedPart(matcher));
        space = MoreObjects.firstNonNull(space, getNextMatchedPart(matcher));
        docname = MoreObjects.firstNonNull(docname, getNextMatchedPart(matcher));
        filename = MoreObjects.firstNonNull(filename, getNextMatchedPart(matcher));
        int queryStart = urlStr.indexOf('?');
        String extrQuery = "";
        if (queryStart >= 0) {
          extrQuery = urlStr.substring(queryStart + 1);
        }
        query = MoreObjects.firstNonNull(query, extrQuery);
        isParsed = true;
      }
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
    if (url == null) {
      url = getXWikiContext().getURLFactory().createAttachmentURL(getFilename(), getSpace(),
          getDocname(), getAction().or(DEFAULT_ACTION), getQuery().or(DEFAULT_QUERY),
          getXWikiContext().getDatabase(), getXWikiContext());
    }
    return url;
  }

  private XWikiContext getXWikiContext() {
    return Utils.getComponent(ModelContext.class).getXWikiContext();
  }
}
