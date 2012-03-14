package com.celements.photo.service;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.photo.container.ImageDimensions;
import com.celements.sajson.Builder;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiURLFactory;

public class ImageScriptServiceTest extends AbstractBridgedComponentTestCase {

  private static final String _ATT_AUTHOR_NAME = "User Name";
  private static final String _IMG_FILENAME = "myImage.jpg";
  private static final String _IMG_URL_STR = "/download/mySpace/myDoc/" + _IMG_FILENAME;
  private static final String _IMG_ATTVERSION = "1.2";

  private XWikiContext context;
  private XWiki xwiki;
  private ImageScriptService imageScriptService;
  private XWikiURLFactory urlFactoryMock;
  private IImageService imageServiceMock;

  @Before
  public void setUp_ImageScriptService() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    imageScriptService = (ImageScriptService)Utils.getComponent(ScriptService.class,
        "celementsphoto");
    urlFactoryMock = createMock(XWikiURLFactory.class);
    context.setURLFactory(urlFactoryMock);
    imageServiceMock = createMock(IImageService.class);
    imageScriptService.imageService = imageServiceMock;
  }

  @Test
  public void testAddImage_dictionary() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must be a dictionary. JSON: " + imgJSON, imgJSON.startsWith("{"));
    assertTrue("must be a dictionary. JSON: " + imgJSON, imgJSON.endsWith("}"));
    verifyAll(imgAttachment);
  }

  @Test
  public void testAddImage_src() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain src property. JSON: " + imgJSON, imgJSON.contains(
        "\"src\" : \"" + _IMG_URL_STR + "\""));
    verifyAll(imgAttachment);
  }

  @Test
  public void testAddImage_filename() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain filename property. JSON: " + imgJSON, imgJSON.contains(
        "\"filename\" : \"" + _IMG_FILENAME + "\""));
    verifyAll(imgAttachment);
  }

  @Test
  public void testAddImage_attversion() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain attversion property. JSON: " + imgJSON, imgJSON.contains(
        "\"attversion\" : \"" + _IMG_ATTVERSION + "\""));
    verifyAll(imgAttachment);
  }

  @Test
  public void testAddImage_lastChangedBy() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain lastChangedBy property. JSON: " + imgJSON, imgJSON.contains(
        "\"lastChangedBy\" : \"" + _ATT_AUTHOR_NAME + "\""));
    verifyAll(imgAttachment);
  }

  @Test
  public void testAddImage_Dimension() throws Exception {
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain maxHeight property. JSON: " + imgJSON, imgJSON.contains(
        "\"maxHeight\" : 200"));
    assertTrue("must contain maxWidth property. JSON: " + imgJSON, imgJSON.contains(
        "\"maxWidth\" : 100"));
    verifyAll(imgAttachment);
  }

  @Test
  public void testAddImage_FileSize() throws Exception {
    context.setLanguage("de");
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain fileSize property. JSON: " + imgJSON, imgJSON.contains(
        "\"fileSize\" : \"2,0 MB\""));
    verifyAll(imgAttachment);
  }

  @Test
  public void testAddImage_MimeType() throws Exception {
    context.setLanguage("de");
    Builder jsonBuilder = new Builder();
    Attachment imgAttachment = createTestAttachment();
    replayAll(imgAttachment);
    imageScriptService.addImage(jsonBuilder, imgAttachment);
    String imgJSON = jsonBuilder.getJSON();
    assertTrue("must contain mime type property. JSON: " + imgJSON, imgJSON.contains(
        "\"mimeType\" : \"image/jpeg\""));
    verifyAll(imgAttachment);
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/
  
  private Attachment createTestAttachment() throws Exception {
    DocumentReference theDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    Attachment imgAttachment = createMock(Attachment.class);
    XWikiDocument theXDoc = new XWikiDocument(theDocRef);
    Document theDoc = new Document(theXDoc, context);
    expect(imgAttachment.getDocument()).andReturn(theDoc).anyTimes();
    expect(imgAttachment.getFilename()).andReturn(_IMG_FILENAME).anyTimes();
    URL imgURL = new URL("http", "www.mytest.org", _IMG_URL_STR);
    expect(urlFactoryMock.createAttachmentURL(eq(_IMG_FILENAME), eq("mySpace"),
        eq("myDoc"), eq("download"), eq(""), same(context))).andReturn(imgURL
            ).anyTimes();
    expect(urlFactoryMock.getURL(eq(imgURL), same(context))).andReturn(_IMG_URL_STR
            ).anyTimes();
    expect(imgAttachment.getVersion()).andReturn(_IMG_ATTVERSION).anyTimes();
    expect(imgAttachment.getAuthor()).andReturn("XWiki.theUser").anyTimes();
    expect(xwiki.getLocalUserName(eq("XWiki.theUser"), (String) isNull(), eq(false),
        same(context))).andReturn(_ATT_AUTHOR_NAME).anyTimes();
    ImageDimensions imgDim = new ImageDimensions(100, 200);
    AttachmentReference imgRef = new AttachmentReference(_IMG_FILENAME, theDocRef);
    expect(imageServiceMock.getDimension(eq(imgRef))).andReturn(imgDim
        ).anyTimes();
    expect(imgAttachment.getFilesize()).andReturn(2 * 1000 * 1000).anyTimes();
    expect(imgAttachment.getMimeType()).andReturn("image/jpeg").anyTimes();
    return imgAttachment;
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, urlFactoryMock, imageServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, urlFactoryMock, imageServiceMock);
    verify(mocks);
  }

}
