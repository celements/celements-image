package com.celements.photo.presentation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.navigation.INavigation;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.rendering.RenderCommand;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("gallerySlidesOverview")
public class GalleryPresentationType implements IPresentationTypeRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      GalleryPresentationType.class);

  private static final String _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS =
    "cel_cm_presentation_treenode";

  RenderCommand renderCmd;

  @Requirement("celements.oldCoreClasses")
  IClassCollectionRole oldCoreClasses;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  private OldCoreClasses getOldCoreClasses() {
    return (OldCoreClasses) oldCoreClasses;
  }

  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem,
      boolean isLastItem, DocumentReference docRef, boolean isLeaf, int numItem,
      INavigation nav) {
    LOGGER.debug("writeNodeContent for [" + docRef + "].");
    outStream.append("<div ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf,
        numItem) + " ");
    outStream.append(nav.addUniqueElementId(docRef) + ">\n");
    outStream.append(getRenderGalleryOverviewScript(docRef));
    outStream.append("</div>\n");
  }

  private String getRenderGalleryOverviewScript(DocumentReference docRef) {
    String templatePath = webUtilsService.getInheritedTemplatedPath(
        getImageGalleryOverviewRef());
    try {
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      XWikiDocument slideDoc = getContext().getWiki().getDocument(docRef, getContext());
      vcontext.put("slidedoc", slideDoc.newDocument(getContext()));
      return getRenderCommand().renderTemplatePath(templatePath, getContext(
          ).getLanguage());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to render template path [" + templatePath + "] for ["
          + docRef + "].", exp);
    }
    return "";
  }

  private DocumentReference getImageGalleryOverviewRef() {
    return new DocumentReference(getContext().getDatabase(), "Templates",
        "ImageGallerySlidesOverview");
  }

  RenderCommand getRenderCommand() {
    if (renderCmd == null) {
      renderCmd = new RenderCommand();
    }
    return renderCmd;
  }

  public String getDefaultCssClass() {
    return _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS;
  }

  public String getEmptyDictionaryKey() {
    return "cel_nav_empty_presentation";
  }

  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    BaseObject albumObj = getContext().getDoc().getXObject(getOldCoreClasses(
        ).getPhotoAlbumClassRef(getContext().getDatabase()));
    if (albumObj != null) {
      String galleryLayout = albumObj.getStringValue(
          OldCoreClasses.PHOTO_ALBUM_GALLERY_LAYOUT);
      if (!StringUtils.isEmpty(galleryLayout)) {
        return new SpaceReference(galleryLayout, new WikiReference(
            getContext().getDatabase()));
      }
    }
    return null;
  }

}
