package com.celements.photo.search;

import static com.celements.web.classcollections.IOldCoreClassConfig.*;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.context.ModelContext;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.packages.WebSearchPackage;
import com.celements.web.classcollections.IOldCoreClassConfig;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@Component(ImageWebSearchPackage.NAME)
public class ImageWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "image";

  @Requirement
  private IOldCoreClassConfig oldCoreClassConf;

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ModelContext context;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public boolean isRequired(XWikiDocument cfgDoc) {
    return false;
  }

  @Override
  public LuceneDocType getDocType() {
    return LuceneDocType.DOC;
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    DocumentReference classRef = oldCoreClassConf.getPhotoAlbumClassRef().getDocRef();
    grp.add(searchService.createFieldRestriction(classRef, PHOTO_ALBUM_TITLE, searchTerm));
    grp.add(searchService.createFieldRestriction(classRef, PHOTO_ALBUM_DESCRIPTION, searchTerm));
    return grp;
  }

  @Override
  public Optional<ClassReference> getLinkedClassRef() {
    return Optional.of(oldCoreClassConf.getPhotoAlbumClassRef());
  }

}
