package com.celements.photo.search;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.search.lucene.LuceneUtils;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.web.packages.WebSearchPackage;
import com.celements.web.classcollections.IOldCoreClassConfig;
import com.google.common.base.Joiner;
import com.xpn.xwiki.web.Utils;

public class ImageWebSearchPackageTest extends AbstractComponentTest {

  private ImageWebSearchPackage webSearchPackage;

  @Before
  public void prepareTest() throws Exception {
    webSearchPackage = (ImageWebSearchPackage) Utils.getComponent(WebSearchPackage.class,
        ImageWebSearchPackage.NAME);
  }

  @Test
  public void test_getName() {
    assertEquals(ImageWebSearchPackage.NAME, webSearchPackage.getName());
  }

  @Test
  public void test_isDefault() {
    assertTrue(webSearchPackage.isDefault());
  }

  @Test
  public void test_isRequired() {
    assertFalse(webSearchPackage.isRequired(null));
  }

  @Test
  public void test_getDocType() {
    assertSame(LuceneDocType.DOC, webSearchPackage.getDocType());
  }

  @Test
  public void test_getQueryRestriction_empty() {
    String searchTerm = "";
    IQueryRestriction restriction = webSearchPackage.getQueryRestriction(null, searchTerm);
    assertNotNull(restriction);
    assertEquals("", restriction.getQueryString());
  }

  @Test
  public void test_getQueryRestriction_text_exact() {
    String searchTerm = LuceneUtils.exactify("find me");
    IQueryRestriction restriction = webSearchPackage.getQueryRestriction(null, searchTerm);
    assertNotNull(restriction);
    assertEquals(String.format("(XWiki.PhotoAlbumClass.title:(+%s) OR "
        + "XWiki.PhotoAlbumClass.description:(+%s))", searchTerm, searchTerm),
        restriction.getQueryString());
  }

  @Test
  public void test_getQueryRestriction_text_tokenized() {
    String searchTerm1 = "find";
    String searchTerm2 = "us";
    IQueryRestriction restriction = webSearchPackage.getQueryRestriction(null, Joiner.on(' ').join(
        searchTerm1, searchTerm2));
    assertNotNull(restriction);
    assertEquals(String.format("(XWiki.PhotoAlbumClass.title:(+%s* +%s*) OR "
        + "XWiki.PhotoAlbumClass.description:(+%s* +%s*))", searchTerm1, searchTerm2, searchTerm1,
        searchTerm2), restriction.getQueryString());
  }

  @Test
  public void test_getLinkedClassRef() {
    assertEquals(new ClassReference(IOldCoreClassConfig.PHOTO_ALBUM_CLASS_SPACE,
        IOldCoreClassConfig.PHOTO_ALBUM_CLASS_DOC), webSearchPackage.getLinkedClassRef().get());
  }

}
