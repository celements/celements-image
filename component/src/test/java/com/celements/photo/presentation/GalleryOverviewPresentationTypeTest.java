package com.celements.photo.presentation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.INavigation;
import com.celements.navigation.presentation.IPresentationTypeRole;
import com.celements.rendering.RenderCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.render.XWikiRenderer;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.render.XWikiVirtualMacro;
import com.xpn.xwiki.web.Utils;

public class GalleryOverviewPresentationTypeTest extends AbstractComponentTest {

  private XWikiContext context;
  private INavigation nav;
  private XWiki xwiki;
  private DocumentReference currentDocRef;
  private XWikiDocument currentDoc;
  private GalleryOverviewPresentationType vtPresType;
  private TestRenderEngine testRenderEngine;
  private RenderCommand renderCmdMock;

  @Before
  public void setUp_RenderedContentPresentationTypeTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    context = getContext();
    currentDocRef = new DocumentReference(context.getDatabase(), "MySpace", "MyCurrentDoc");
    currentDoc = new XWikiDocument(currentDocRef);
    context.setDoc(currentDoc);
    nav = createMockAndAddToDefault(INavigation.class);
    xwiki = getWikiMock();
    context.setWiki(xwiki);
    testRenderEngine = new TestRenderEngine();
    expect(xwiki.getRenderingEngine()).andReturn(testRenderEngine).anyTimes();
    vtPresType = (GalleryOverviewPresentationType) Utils.getComponent(IPresentationTypeRole.class,
        "galleryOverview");
    renderCmdMock = createMockAndAddToDefault(RenderCommand.class);
    vtPresType.renderCmd = renderCmdMock;
  }

  @Test
  public void testGetRenderCommand_default() {
    vtPresType.renderCmd = null;
    assertNotNull(vtPresType.getRenderCommand());
  }

  @Test
  public void testGetRenderCommand_inject() {
    RenderCommand testMock = new RenderCommand();
    vtPresType.renderCmd = testMock;
    assertSame(testMock, vtPresType.getRenderCommand());
  }

  @Test
  public void testWriteNodeContent() throws Exception {
    context.setLanguage("fr");
    context.put("vcontext", new VelocityContext());
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "MyPage");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    StringBuilder outStream = new StringBuilder();
    boolean isFirstItem = true;
    boolean isLastItem = false;
    boolean isLeaf = true;
    String expectedNodeContent = "expected rendered content for node";
    expect(nav.addUniqueElementId(eq(currentDocRef))).andReturn(
        "id=\"N3:Content:Content.MyPage\"").once();
    expect(nav.addCssClasses(eq(currentDocRef), eq(true), eq(isFirstItem), eq(isLastItem), eq(
        isLeaf), eq(1))).andReturn("class=\"cel_cm_navigation_menuitem"
            + " first cel_nav_isLeaf RichText\"").once();
    DocumentReference templateRef = new DocumentReference(context.getDatabase(), "Templates",
        "ImageGalleryOverview");
    expect(getMock(IModelAccessFacade.class).exists(eq(templateRef))).andReturn(true).once();
    expect(renderCmdMock.renderTemplatePath(eq("Templates.ImageGalleryOverview"), eq(
        "fr"))).andReturn(expectedNodeContent).once();
    expect(xwiki.getDocument(eq(currentDocRef), same(context))).andReturn(currentDoc).atLeastOnce();
    replayDefault();
    vtPresType.writeNodeContent(outStream, isFirstItem, isLastItem, currentDocRef, isLeaf, 1, nav);
    assertEquals("<div class=\"cel_cm_navigation_menuitem first cel_nav_isLeaf RichText\""
        + " id=\"N3:Content:Content.MyPage\">\n" + expectedNodeContent + "</div>\n",
        outStream.toString());
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    Document galleryDoc = (Document) vcontext.get("gallerydoc");
    assertEquals(currentDocRef, galleryDoc.getDocumentReference());
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  public class TestRenderEngine implements XWikiRenderingEngine {

    private List<VelocityContext> storedVelocityContext;
    private XWikiRenderingEngine mockRenderEngine;

    public TestRenderEngine() {
      mockRenderEngine = createMockAndAddToDefault(XWikiRenderingEngine.class);
    }

    public XWikiRenderingEngine getMock() {
      return mockRenderEngine;
    }

    @Override
    public void addRenderer(String name, XWikiRenderer renderer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String convertMultiLine(String macroname, String params, String data, String allcontent,
        XWikiVirtualMacro macro, XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String convertSingleLine(String macroname, String params, String allcontent,
        XWikiVirtualMacro macro, XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void flushCache() {
      throw new UnsupportedOperationException();
    }

    @Override
    public XWikiRenderer getRenderer(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<XWikiRenderer> getRendererList() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getRendererNames() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String interpretText(String text, XWikiDocument includingdoc, XWikiContext context) {
      VelocityContext velocityContext = (VelocityContext) context.get("vcontext");
      storedVelocityContext.add((VelocityContext) velocityContext.clone());
      return mockRenderEngine.interpretText(text, includingdoc, context);
    }

    @Override
    public String renderDocument(XWikiDocument doc, XWikiContext context) throws XWikiException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String renderDocument(XWikiDocument doc, XWikiDocument includingdoc,
        XWikiContext context) throws XWikiException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String renderText(String text, XWikiDocument includingdoc, XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String renderText(String text, XWikiDocument contentdoc, XWikiDocument includingdoc,
        XWikiContext context) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void virtualInit(XWikiContext context) {
      throw new UnsupportedOperationException();
    }

  }

}
