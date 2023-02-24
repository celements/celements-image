package com.celements.photo.presentation;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.ICellWriter;
import com.celements.model.context.ModelContext;
import com.celements.navigation.INavigation;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.rendering.RenderCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("galleryOverview")
public class GalleryOverviewPresentationType implements IPresentationTypeRole<INavigation> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(GalleryOverviewPresentationType.class);

  private static final String _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS = "cel_cm_presentation_treenode";

  RenderCommand renderCmd;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  ModelContext context;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public void writeNodeContent(ICellWriter writer, DocumentReference docRef,
      INavigation navigation) {
    writeNodeContent(writer.getAsStringBuilder(), false, false, docRef, true, 0, navigation);
  }

  @Override
  public void writeNodeContent(StringBuilder outStream, boolean isFirstItem, boolean isLastItem,
      DocumentReference docRef, boolean isLeaf, int numItem, INavigation nav) {
    LOGGER.debug("writeNodeContent for [{}].", docRef);
    outStream.append("<div ");
    outStream.append(nav.addCssClasses(docRef, true, isFirstItem, isLastItem, isLeaf, numItem)
        + " ");
    outStream.append(nav.addUniqueElementId(docRef) + ">\n");
    outStream.append(getRenderGalleryOverviewScript(docRef));
    outStream.append("</div>\n");
  }

  private String getRenderGalleryOverviewScript(DocumentReference docRef) {
    String templatePath = webUtilsService.getInheritedTemplatedPath(getImageGalleryOverviewRef());
    try {
      VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
      XWikiDocument galleryDoc = getContext().getWiki().getDocument(docRef, getContext());
      vcontext.put("gallerydoc", galleryDoc.newDocument(getContext()));
      String defaultLang = context.getDefaultLanguage();
      return getRenderCommand().renderTemplatePath(templatePath,
          context.getLanguage().orElse(defaultLang), defaultLang);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to render template path [{}] for [{}].", templatePath, docRef, exp);
    }
    return "";
  }

  private DocumentReference getImageGalleryOverviewRef() {
    return new DocumentReference(getContext().getDatabase(), "Templates", "ImageGalleryOverview");
  }

  RenderCommand getRenderCommand() {
    if (renderCmd == null) {
      renderCmd = new RenderCommand();
    }
    return renderCmd;
  }

  @Override
  public String getDefaultCssClass() {
    return _CEL_CM_CPT_TREENODE_DEFAULT_CSSCLASS;
  }

  @Override
  public String getEmptyDictionaryKey() {
    return "cel_nav_empty_presentation";
  }

  @Override
  public SpaceReference getPageLayoutForDoc(DocumentReference docRef) {
    return null;
  }

}
