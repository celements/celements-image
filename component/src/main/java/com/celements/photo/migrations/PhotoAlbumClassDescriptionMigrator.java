package com.celements.photo.migrations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.migrator.AbstractCelementsHibernateMigrator;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.field.FieldAccessor;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.query.IQueryExecutionServiceRole;
import com.celements.web.classcollections.IOldCoreClassConfig;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component("PhotoAlbumClassDescriptionMigrator")
public class PhotoAlbumClassDescriptionMigrator extends AbstractCelementsHibernateMigrator {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      PhotoAlbumClassDescriptionMigrator.class);

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IOldCoreClassConfig oldCoreClassConfig;

  @Requirement
  private IQueryExecutionServiceRole queryExecutor;

  @Requirement(XObjectFieldAccessor.NAME)
  private FieldAccessor<BaseObject> xObjFieldAccessor;

  @Override
  public String getName() {
    return "PhotoAlbumClassDescriptionMigrator";
  }

  @Override
  public String getDescription() {
    return "Set Value for Boolean 'showDescription' in the PhotoAlbumClass";
  }

  /**
   * getVersion is using days since 1.1.2010 until the day of committing this migration
   * 21.08.2017 -> 2912 https://www.convertunits.com/dates/from/Jan+1,+2010/to/Dec+22,+2017
   */
  @Override
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(2912);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    migratePhotoAlbumClassObjects();
  }

  private void migratePhotoAlbumClassObjects() throws XWikiException {
    try {
      String xwql = getPhotoAlbumClassXWQL();
      Query xwqlQuery = queryManager.createQuery(xwql, Query.XWQL);
      for (DocumentReference docRef : queryExecutor.executeAndGetDocRefs(xwqlQuery)) {
        try {
          XWikiDocument galleryDoc = modelAccess.getDocument(docRef);
          Optional<BaseObject> imgGalPageTypeObj = XWikiObjectEditor.on(galleryDoc).filter(
              PageTypeClass.FIELD_PAGE_TYPE, "ImageGallery").fetch().first();
          Optional<BaseObject> galPageTypeObj = XWikiObjectEditor.on(galleryDoc).filter(
              PageTypeClass.FIELD_PAGE_TYPE, "Gallery").fetch().first();
          Optional<BaseObject> photoAlbumClassObj = XWikiObjectEditor.on(galleryDoc).filter(
              new ClassReference(oldCoreClassConfig.getPhotoAlbumClassRef())).fetch().first();
          if (imgGalPageTypeObj.isPresent() && photoAlbumClassObj.isPresent()) {
            modelAccess.setProperty(photoAlbumClassObj.get(), "showDescription", 0);
          } else if (galPageTypeObj.isPresent() && photoAlbumClassObj.isPresent()) {
            modelAccess.setProperty(photoAlbumClassObj.get(), "showDescription", 1);
          }
          modelAccess.saveDocument(galleryDoc);
        } catch (DocumentNotExistsException exp) {
          LOGGER.error("Could not get Document with docRef {} ", docRef, exp);
        } catch (DocumentSaveException exp) {
          LOGGER.error("Could not save Document with docRef {} ", docRef, exp);
        }
      }
    } catch (QueryException qexc) {
      throw new XWikiException(0, 0,
          "Error while searching ProgonEvent with Progon.URLClass Object", qexc);
    }
  }

  private String getPhotoAlbumClassXWQL() {
    return "from doc.object(XWiki.PhotoAlbumClass) as obj";
  }

}
