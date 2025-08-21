package com.celements.photo.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.reference.RefBuilder;
import com.celements.photo.container.ImageDimensions;
import com.celements.sajson.Builder;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiURLFactory;

public class ImageScriptServiceTest extends AbstractComponentTest {

  private static final String _ATT_AUTHOR_NAME = "User Name";
  private static final String _IMG_FILENAME = "myImage.jpg";
  private static final String _IMG_URL_STR = "/download/mySpace/myDoc/" + _IMG_FILENAME;
  private static final String _IMG_ATTVERSION = "1.2";

  private XWikiContext context;
  private XWiki xwiki;
  private ImageScriptService imageScriptService;
  private XWikiURLFactory urlFactoryMock;
  private IImageService imageServiceMock;
  private IModelAccessFacade modelAccessMock;
  private RefBuilder refBuilder;
  private XWikiDocument theXDoc;

  @Before
  public void setUp_ImageScriptService() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    theXDoc = createDefaultMock(XWikiDocument.class);
    urlFactoryMock = createDefaultMock(XWikiURLFactory.class);
    context.setURLFactory(urlFactoryMock);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    refBuilder = registerComponentMock(RefBuilder.class);
    imageServiceMock = registerComponentMock(ImageService.class);
    imageScriptService = getBeanFactory().getBean(ImageScriptService.class);
  }

  @Test
  public void testAddImage_dictionary() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must be a dictionary. JSON: " + imgJSON, imgJSON.startsWith("{"));
    assertTrue("must be a dictionary. JSON: " + imgJSON, imgJSON.endsWith("}"));
    verifyDefault();
  }

  @Test
  public void testAddImage_src() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain src property. JSON: " + imgJSON, imgJSON.contains("\"src\" : \""
        + _IMG_URL_STR + "\""));
    verifyDefault();
  }

  @Test
  public void testAddImage_filename() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain filename property. JSON: " + imgJSON, imgJSON.contains(
        "\"filename\" : \"" + _IMG_FILENAME + "\""));
    verifyDefault();
  }

  @Test
  public void testAddImage_attversion() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain attversion property. JSON: " + imgJSON, imgJSON.contains(
        "\"attversion\" : \"" + _IMG_ATTVERSION + "\""));
    verifyDefault();
  }

  @Test
  public void testAddImage_lastChangedBy() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain lastChangedBy property. JSON: " + imgJSON, imgJSON.contains(
        "\"lastChangedBy\" : \"" + _ATT_AUTHOR_NAME + "\""));
    verifyDefault();
  }

  @Test
  public void testAddImage_no_Dimension() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertFalse("must NOT contain maxHeight property. JSON: " + imgJSON, imgJSON.contains(
        "\"maxHeight\" : 200"));
    assertFalse("must NOT contain maxWidth property. JSON: " + imgJSON, imgJSON.contains(
        "\"maxWidth\" : 100"));
    verifyDefault();
  }

  @Test
  public void testAddImage_Dimension() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment, true);
    verifyDefault();
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain maxHeight property. JSON: " + imgJSON, imgJSON.contains(
        "\"maxHeight\" : 302"));
    assertTrue("must contain maxWidth property. JSON: " + imgJSON, imgJSON.contains(
        "\"maxWidth\" : 318"));
  }

  @Test
  public void testAddImage_FileSize() throws Exception {
    context.setLanguage("de");
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain fileSize property. JSON: " + imgJSON, imgJSON.contains(
        "\"fileSize\" : \"2,0 MB\""));
    verifyDefault();
  }

  @Test
  public void testAddImage_MimeType() throws Exception {
    context.setLanguage("de");
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayDefault();
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    verifyDefault();
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain mime type property. JSON: " + imgJSON, imgJSON.contains(
        "\"mimeType\" : \"image/jpeg\""));
  }

  @Test
  public void test_useImageAnimations_cfg_true() {
    expect(getWikiMock().Param(eq(ImageScriptService.CFG_IMAGE_ANIMATION), eq("0"))).andReturn("1");
    expect(getWikiMock().getSpacePreference(eq(ImageScriptService.SPACEPREF_IMAGE_ANIMATION), eq(
        "1"), same(context))).andReturn("1");
    replayDefault();
    assertTrue(imageScriptService.useImageAnimations());
    verifyDefault();
  }

  @Test
  public void test_useImageAnimations_cfg_false() {
    expect(getWikiMock().Param(eq(ImageScriptService.CFG_IMAGE_ANIMATION), eq("0"))).andReturn("0");
    expect(getWikiMock().getSpacePreference(eq(ImageScriptService.SPACEPREF_IMAGE_ANIMATION), eq(
        "0"), same(context))).andReturn("0");
    replayDefault();
    assertFalse(imageScriptService.useImageAnimations());
    verifyDefault();
  }

  @Test
  public void test_useImageAnimations_wikiOrSpace_true() {
    expect(getWikiMock().Param(eq(ImageScriptService.CFG_IMAGE_ANIMATION), eq("0"))).andReturn("0");
    expect(getWikiMock().getSpacePreference(eq(ImageScriptService.SPACEPREF_IMAGE_ANIMATION), eq(
        "0"), same(context))).andReturn("1");
    replayDefault();
    assertTrue(imageScriptService.useImageAnimations());
    verifyDefault();
  }

  @Test
  public void test_useImageAnimations_wikiOrSpace_false() {
    expect(getWikiMock().Param(eq(ImageScriptService.CFG_IMAGE_ANIMATION), eq("0"))).andReturn("1");
    expect(getWikiMock().getSpacePreference(eq(ImageScriptService.SPACEPREF_IMAGE_ANIMATION), eq(
        "1"), same(context))).andReturn("0");
    replayDefault();
    assertFalse(imageScriptService.useImageAnimations());
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private XWikiAttachment createTestXAttachment() throws Exception {
    DocumentReference theDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    XWikiAttachment imgXAttachment = createDefaultMock(XWikiAttachment.class);
    expect(modelAccessMock.getOrCreateDocument(theDocRef)).andReturn(theXDoc).anyTimes();
    expect(theXDoc.getDocumentReference()).andReturn(theDocRef).anyTimes();
    expect(theXDoc.getAttachment(anyString())).andReturn(imgXAttachment).anyTimes();
    FileInputStream imgInputStream = new FileInputStream("src/test/resources/cmykTest_rgb.jpg");
    expect(imgXAttachment.getContentInputStream(context)).andReturn(imgInputStream).anyTimes();
    URL imgURL = new URL("http", "www.mytest.org", _IMG_URL_STR);
    expect(urlFactoryMock.createAttachmentURL(eq(_IMG_FILENAME), eq("mySpace"), eq("myDoc"), eq(
        "download"), eq(""), same(context))).andReturn(imgURL).anyTimes();
    expect(urlFactoryMock.getURL(eq(imgURL), same(context))).andReturn(_IMG_URL_STR).anyTimes();
    expect(xwiki.getLocalUserName(eq("XWiki.theUser"), (String) isNull(), eq(false), same(
        context))).andReturn(_ATT_AUTHOR_NAME).anyTimes();
    ImageDimensions imgDim = new ImageDimensions(100, 200);
    AttachmentReference imgRef = new AttachmentReference(_IMG_FILENAME, theDocRef);
    expect(imageServiceMock.getDimension(eq(imgRef))).andReturn(imgDim).anyTimes();
    return imgXAttachment;
  }

  private Attachment createTestAttachment() throws Exception {
    Attachment imgAttachment = createDefaultMock(Attachment.class);
    XWikiAttachment imgXAttachment = createTestXAttachment();
    Document theDoc = new Document(theXDoc, context);
    expect(imgAttachment.getAttachment()).andReturn(imgXAttachment).anyTimes();
    expect(imgAttachment.getVersion()).andReturn(_IMG_ATTVERSION).anyTimes();
    expect(imgAttachment.getAuthor()).andReturn("XWiki.theUser").anyTimes();
    expect(imgAttachment.getDocument()).andReturn(theDoc).anyTimes();
    expect(imgAttachment.getFilename()).andReturn(_IMG_FILENAME).anyTimes();
    expect(imgAttachment.getMimeType()).andReturn("image/jpeg").anyTimes();
    expect(imgAttachment.getFilesize()).andReturn(2 * 1000 * 1000).anyTimes();
    return imgAttachment;
  }

}
