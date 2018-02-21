package com.celements.photo.migrations;

import javax.validation.constraints.NotNull;

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
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.query.IQueryExecutionServiceRole;
import com.celements.web.classcollections.IOldCoreClassConfig;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
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

  @Requirement
  private IPageTypeResolverRole pageTypeResolver;

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
   * 22.12.2017 -> 2912 https://www.convertunits.com/dates/from/Jan+1,+2010/to/Dec+22,+2017
   */
  @Override
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(2912);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context)
      throws XWikiException {
    LOGGER.info("migrate: start");
    try {
      Query xwqlQuery = queryManager.createQuery(getPhotoAlbumClassXWQL(), Query.XWQL);
      for (DocumentReference docRef : queryExecutor.executeAndGetDocRefs(xwqlQuery)) {
        migrateGallery(docRef);
      }
      LOGGER.info("migrate: end");
    } catch (QueryException | DocumentSaveException exp) {
      throw new XWikiException(0, 0, "PhotoAlbumClassDescriptionMigrator failed", exp);
    } catch (Exception exc) {
      LOGGER.error("PhotoAlbumClassDescriptionMigrator failed", exc);
      throw exc;
    }
  }

  private void migrateGallery(DocumentReference docRef) throws DocumentSaveException {
    try {
      boolean docChanged = false;
      XWikiDocument galleryDoc = modelAccess.getDocument(docRef);
      String pageType = getPageTypeName(galleryDoc);
      Optional<BaseObject> photoAlbumClassObj = XWikiObjectEditor.on(galleryDoc).filter(
          new ClassReference(oldCoreClassConfig.getPhotoAlbumClassRef())).fetch().first();
      // TODO: ClassDefinition for PhotoAlbumClass will be implemented with the Ticket
      // CELDEV-614
      if (pageType.equals("ImageGallery") && photoAlbumClassObj.isPresent()) {
        docChanged = modelAccess.setProperty(photoAlbumClassObj.get(), "showDescription", 0);
      } else if (pageType.equals("Gallery") && photoAlbumClassObj.isPresent()) {
        docChanged = modelAccess.setProperty(photoAlbumClassObj.get(), "showDescription", 1);
      } else {
        if (!photoAlbumClassObj.isPresent()) {
          LOGGER.warn("No PhotoAlbumClass Object on doc with docRef {}", docRef);
        } else {
          LOGGER.warn("pageType '{}' is not 'Gallery' or 'ImageGallery' for '{}'", pageType,
              docRef);
        }
      }
      if (docChanged) {
        modelAccess.saveDocument(galleryDoc);
      }
    } catch (DocumentNotExistsException exp) {
      LOGGER.warn("Could not get Document with docRef {} ", docRef, exp);
    }
  }

  private @NotNull String getPageTypeName(XWikiDocument doc) {
    PageTypeReference pageTypeRef = pageTypeResolver.getPageTypeRefForDoc(doc);
    return pageTypeRef != null ? Strings.nullToEmpty(pageTypeRef.getConfigName()) : "";
  }

  private String getPhotoAlbumClassXWQL() {
    return "from doc.object(XWiki.PhotoAlbumClass) as obj";
  }

}
