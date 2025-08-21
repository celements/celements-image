package com.celements.photo.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.navigation.NavigationClasses;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.photo.container.ImageDimensions;
import com.celements.photo.utilities.ImportFileObject;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.XWikiRequest;

public class ImageServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private ImageService imageService;
  private XWiki xwiki;
  private IRightsAccessFacadeRole rightServiceMock;
  private ITreeNodeService treeNodeServiceMock;
  private INextFreeDocRole nextFreeDocMock;
  private IModelAccessFacade modelAccess;
  private ModelContext modelContext;

  @Before
  public void setUp() throws Exception {
    context = getXContext();
    xwiki = getMock(XWiki.class);
    rightServiceMock = registerComponentMock(IRightsAccessFacadeRole.class);
    treeNodeServiceMock = registerComponentMock(ITreeNodeService.class);
    nextFreeDocMock = registerComponentMock(INextFreeDocRole.class);
    modelAccess = registerComponentMock(IModelAccessFacade.class);
    modelContext = registerComponentMock(ModelContext.class);
    expect(modelContext.getXWikiContext()).andReturn(context).anyTimes();
    expect(modelContext.getWikiRef()).andReturn(new WikiReference(context.getDatabase())).anyTimes();
    imageService = getBeanFactory().getBean(ImageService.class);
  }

  @Test
  public void testGetPhotoAlbumNavObject() throws Exception {
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "galleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    BaseObject expectedPhotoAlbumNavObj = new BaseObject();
    expectedPhotoAlbumNavObj.setXClassReference(new NavigationClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    galleryDoc.addXObject(expectedPhotoAlbumNavObj);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).once();
    replayDefault();
    BaseObject photoAlbumNavObj = imageService.getPhotoAlbumNavObject(galleryDocRef);
    assertNotNull(photoAlbumNavObj);
    assertSame(expectedPhotoAlbumNavObj, photoAlbumNavObj);
    verifyDefault();
  }

  @Test
  public void testGetPhotoAlbumNavObject_noObject() throws Exception {
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "noGalleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).once();
    replayDefault();
    try {
      imageService.getPhotoAlbumNavObject(galleryDocRef);
      fail("expecting NoGalleryDocumentException");
    } catch (NoGalleryDocumentException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetPhotoAlbumSpaceRef_noGalleryDoc() throws Exception {
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "noGalleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).once();
    replayDefault();
    try {
      imageService.getPhotoAlbumSpaceRef(galleryDocRef);
    } catch (NoGalleryDocumentException exp) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetPhotoAlbumSpaceRef_galleryDoc() throws Exception {
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "galleryDoc");
    SpaceReference expectedSpaceRef = new SpaceReference("gallerySpace",
        (WikiReference) galleryDocRef.getLastSpaceReference().getParent());
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    BaseObject expectedPhotoAlbumNavObj = new BaseObject();
    expectedPhotoAlbumNavObj.setXClassReference(new NavigationClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    String gallerySpaceName = "gallerySpace";
    expectedPhotoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
    galleryDoc.addXObject(expectedPhotoAlbumNavObj);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).once();
    replayDefault();
    assertEquals(expectedSpaceRef, imageService.getPhotoAlbumSpaceRef(galleryDocRef));
    verifyDefault();
  }

  @Test
  public void testCheckAddSlideRights_noObjects_noGalleryDoc() throws Exception {
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "noGalleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).once();
    replayDefault();
    assertFalse("Expecting no addSlide rights if document is no gallery document.",
        imageService.checkAddSlideRights(galleryDocRef));
    verifyDefault();
  }

  @Test
  public void testGetImageSlideTemplateRef_local() throws Exception {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
        "ImageGalleryTemplates", "NewImageGallerySlide");
    expect(modelAccess.exists(eq(localTemplateRef))).andReturn(true).once();
    replayDefault();
    assertEquals(localTemplateRef, imageService.getImageSlideTemplateRef());
    verifyDefault();
  }

  @Test
  public void testGetImageSlideTemplateRef_central() throws Exception {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
        "ImageGalleryTemplates", "NewImageGallerySlide");
    DocumentReference centralTemplateRef = new DocumentReference("celements2web",
        "ImageGalleryTemplates", "NewImageGallerySlide");
    expect(modelAccess.exists(eq(localTemplateRef))).andReturn(false).once();
    replayDefault();
    assertEquals(centralTemplateRef, imageService.getImageSlideTemplateRef());
    verifyDefault();
  }

  @Test
  public void testCheckAddSlideRights_yes() throws Exception {
    String editorUser = "XWiki.myEditor";
    context.setUser(editorUser);
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "galleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    BaseObject photoAlbumNavObj = new BaseObject();
    photoAlbumNavObj.setXClassReference(new NavigationClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    String gallerySpaceName = "gallerySpace";
    SpaceReference spaceRef = new SpaceReference(gallerySpaceName, new WikiReference(
        context.getDatabase()));
    photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
    galleryDoc.addXObject(photoAlbumNavObj);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).once();
    DocumentReference testSlideDocRef = new DocumentReference(context.getDatabase(),
        gallerySpaceName, "Testname1");
    expect(rightServiceMock.hasAccessLevel(eq(testSlideDocRef), eq(EAccessLevel.EDIT)))
        .andReturn(true).once();
    expect(nextFreeDocMock.getNextTitledPageDocRef(spaceRef, "Testname")).andReturn(
        testSlideDocRef);
    replayDefault();
    assertTrue("Expecting addSlide rights if 'edit' rights on space available",
        imageService.checkAddSlideRights(galleryDocRef));
    verifyDefault();
  }

  @Test
  public void testCheckAddSlideRights_no() throws Exception {
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "galleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    BaseObject photoAlbumNavObj = new BaseObject();
    photoAlbumNavObj.setXClassReference(new NavigationClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    String gallerySpaceName = "gallerySpace";
    SpaceReference spaceRef = new SpaceReference(gallerySpaceName, new WikiReference(
        context.getDatabase()));
    photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
    galleryDoc.addXObject(photoAlbumNavObj);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).once();
    DocumentReference testSlideDocRef = new DocumentReference(context.getDatabase(),
        gallerySpaceName, "Testname1");
    expect(rightServiceMock.hasAccessLevel(eq(testSlideDocRef), eq(EAccessLevel.EDIT)))
        .andReturn(false).once();
    expect(nextFreeDocMock.getNextTitledPageDocRef(spaceRef, "Testname")).andReturn(
        testSlideDocRef);
    replayDefault();
    assertFalse("Expecting no addSlide rights if no 'edit' rights on space available",
        imageService.checkAddSlideRights(galleryDocRef));
    verifyDefault();
  }

  @Test
  public void testGetFixedAspectURL_isSquare_tarSquare() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(300, 300), 1, 1);
    assertEquals("Expecting no params if image matches target aspect ratio.", "", params);
  }

  @Test
  public void testGetFixedAspectURL_isPortrait_tarSquare() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(300, 400), 1, 1);
    assertTrue("Expecting no crop on left. [" + params + "]", params.indexOf("cropX=0") >= 0);
    assertTrue("Expecting full width. [" + params + "]", params.indexOf("cropW=300") >= 0);
    assertTrue("Expecting cropped border on top. [" + params + "]", params.indexOf(
        "cropY=50") >= 0);
    assertTrue("Expecting height equals full width. [" + params + "]", params.indexOf(
        "cropH=300") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_isLandscape_tarSquare() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(400, 300), 1, 1);
    assertTrue("Expecting cropped border on left. [" + params + "]", params.indexOf(
        "cropX=50") >= 0);
    assertTrue("Expecting width equals full height. [" + params + "]", params.indexOf(
        "cropW=300") >= 0);
    assertTrue("Expecting no crop on top. [" + params + "]", params.indexOf("cropY=0") >= 0);
    assertTrue("Expecting full height. [" + params + "]", params.indexOf("cropH=300") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_is3to4_tar3to4() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(300, 400), 3, 4);
    assertEquals("Expecting no params if image matches target aspect ratio.", "", params);
  }

  @Test
  public void testGetFixedAspectURL_isSquare_tar3to4() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(300, 300), 3, 4);
    assertTrue("Expecting cropped border on left. [" + params + "]", params.indexOf(
        "cropX=37") >= 0);
    assertTrue("Expecting width equals .75 * height. [" + params + "]", params.indexOf(
        "cropW=225") >= 0);
    assertTrue("Expecting no crop on top. [" + params + "]", params.indexOf("cropY=0") >= 0);
    assertTrue("Expecting full height. [" + params + "]", params.indexOf("cropH=300") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_isLandscape_tar3to4() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(400, 300), 3, 4);
    assertTrue("Expecting cropped border on left. [" + params + "]", params.indexOf(
        "cropX=87") >= 0);
    assertTrue("Expecting width equals .75 * height. [" + params + "]", params.indexOf(
        "cropW=225") >= 0);
    assertTrue("Expecting no crop on top. [" + params + "]", params.indexOf("cropY=0") >= 0);
    assertTrue("Expecting full height. [" + params + "]", params.indexOf("cropH=300") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_isPortraitWide_tar3to4() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(350, 400), 3, 4);
    assertTrue("Expecting cropped border on left. [" + params + "]", params.indexOf(
        "cropX=25") >= 0);
    assertTrue("Expecting width equals .75 * height. [" + params + "]", params.indexOf(
        "cropW=300") >= 0);
    assertTrue("Expecting no crop on top. [" + params + "]", params.indexOf("cropY=0") >= 0);
    assertTrue("Expecting full height. [" + params + "]", params.indexOf("cropH=400") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_isPortraitSmall_tar3to4() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(240, 400), 3, 4);
    assertTrue("Expecting no crop on left. [" + params + "]", params.indexOf("cropX=0") >= 0);
    assertTrue("Expecting full width. [" + params + "]", params.indexOf("cropW=240") >= 0);
    assertTrue("Expecting cropped border on top. [" + params + "]", params.indexOf(
        "cropY=40") >= 0);
    assertTrue("Expecting width * 4/3. [" + params + "]", params.indexOf("cropH=320") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_is4to3_tar4to3() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(400, 300), 4, 3);
    assertEquals("Expecting no params if image matches target aspect ratio.", "", params);
  }

  @Test
  public void testGetFixedAspectURL_isSquare_tar4to3() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(300, 300), 4, 3);
    assertTrue("Expecting no crop on left. [" + params + "]", params.indexOf("cropX=0") >= 0);
    assertTrue("Expecting full width. [" + params + "]", params.indexOf("cropW=300") >= 0);
    assertTrue("Expecting cropped border on top. [" + params + "]", params.indexOf(
        "cropY=37") >= 0);
    assertTrue("Expecting height equals .75 * width. [" + params + "]", params.indexOf(
        "cropH=225") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_isPortrait_tar4to3() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(300, 400), 4, 3);
    assertTrue("Expecting no crop on left. [" + params + "]", params.indexOf("cropX=0") >= 0);
    assertTrue("Expecting full width. [" + params + "]", params.indexOf("cropW=300") >= 0);
    assertTrue("Expecting cropped border on top. [" + params + "]", params.indexOf(
        "cropY=87") >= 0);
    assertTrue("Expecting height equals .75 * width. [" + params + "]", params.indexOf(
        "cropH=225") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_isLandscapeHigh_tar4to3() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(400, 350), 4, 3);
    assertTrue("Expecting no crop on left. [" + params + "]", params.indexOf("cropX=0") >= 0);
    assertTrue("Expecting full width. [" + params + "]", params.indexOf("cropW=400") >= 0);
    assertTrue("Expecting cropped border on top. [" + params + "]", params.indexOf(
        "cropY=25") >= 0);
    assertTrue("Expecting height equals .75 * width. [" + params + "]", params.indexOf(
        "cropH=300") >= 0);
  }

  @Test
  public void testGetFixedAspectURL_isLandscapeLow_tar4to3() {
    String params = imageService.getFixedAspectURL(new ImageDimensions(400, 240), 4, 3);
    assertTrue("Expecting cropped border on left. [" + params + "]", params.indexOf(
        "cropX=40") >= 0);
    assertTrue("Expecting width * 4/3. [" + params + "]", params.indexOf("cropW=320") >= 0);
    assertTrue("Expecting no crop on top. [" + params + "]", params.indexOf("cropY=0") >= 0);
    assertTrue("Expecting full height. [" + params + "]", params.indexOf("cropH=240") >= 0);
  }

  @Test
  public void testCleanMetaTagKey_clean() {
    String cleanTag = "Compression Type";
    assertEquals(cleanTag, imageService.cleanMetaTagKey(cleanTag));
  }

  @Test
  public void testCleanMetaTagKey_unclean() {
    String cleanTag = "Compression Type";
    String tag = "[Jpeg] Compression Type";
    assertEquals(cleanTag, imageService.cleanMetaTagKey(tag));
  }

  @Test
  public void testCleanMetaTagValue_clean() {
    String key = "Compression Type";
    String value = "8 bits";
    assertEquals(value, imageService.cleanMetaTagValue(key, value));
  }

  @Test
  public void testCleanMetaTagValue_unclean() {
    String key = "[Jpeg] Compression Type";
    String value = "8 bits";
    assertEquals(value, imageService.cleanMetaTagValue(key, key + " - " + value));
  }

  @Test
  public void testGetActionForFile_noAtts() {
    String fileName = "test.jpg";
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S",
        "D"));
    context.setDoc(doc);
    BaseObject importClassObj = new BaseObject();
    DocumentReference importClassRef = new DocumentReference(context.getDatabase(), "Classes", "ImportClass");
    importClassObj.setXClassReference(importClassRef);
    doc.addXObject(importClassObj);
    expect(xwiki.clearName(eq(fileName), eq(false), eq(true), same(context))).andReturn(
        fileName);
    replayDefault();
    assertEquals(ImportFileObject.ACTION_ADD, imageService.getActionForFile(fileName, doc));
    verifyDefault();
  }

  @Test
  public void testGetActionForFile_otherAtts() throws XWikiException {
    String fileName = "test.jpg";
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S",
        "D"));
    context.setDoc(doc);
    BaseObject importClassObj = new BaseObject();
    DocumentReference importClassRef = new DocumentReference(context.getDatabase(), "Classes", "ImportClass");
    importClassObj.setXClassReference(importClassRef);
    doc.addXObject(importClassObj);
    XWikiAttachment att = new XWikiAttachment();
    att.setFilename("otherFile.jpg");
    List<XWikiAttachment> attList = new ArrayList<>();
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(xwiki.clearName(eq(fileName), eq(false), eq(true), same(context))).andReturn(
        fileName);
    replayDefault();
    assertEquals(ImportFileObject.ACTION_ADD, imageService.getActionForFile(fileName, doc));
    verifyDefault();
  }

  @Test
  public void testGetActionForFile_hasAtt() throws XWikiException {
    String fileName = "test.jpg";
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S",
        "D"));
    context.setDoc(doc);
    BaseObject importClassObj = new BaseObject();
    DocumentReference importClassRef = new DocumentReference(context.getDatabase(), "Classes", "ImportClass");
    importClassObj.setXClassReference(importClassRef);
    doc.addXObject(importClassObj);
    List<XWikiAttachment> attList = new ArrayList<>();
    XWikiAttachment att = new XWikiAttachment();
    att.setFilename("otherFile.jpg");
    attList.add(att);
    att = new XWikiAttachment();
    att.setFilename(fileName);
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(xwiki.clearName(eq(fileName), eq(false), eq(true), same(context))).andReturn(
        fileName);
    replayDefault();
    assertEquals(ImportFileObject.ACTION_OVERWRITE, imageService.getActionForFile(fileName, doc));
    verifyDefault();
  }

  @Test
  public void testGetActionForFile_hasAttZip() throws XWikiException {
    String fileName = "test.jpg";
    XWikiDocument doc = new XWikiDocument(new DocumentReference(context.getDatabase(), "S",
        "D"));
    context.setDoc(doc);
    BaseObject importClassObj = new BaseObject();
    DocumentReference importClassRef = new DocumentReference(context.getDatabase(), "Classes", "ImportClass");
    importClassObj.setXClassReference(importClassRef);
    doc.addXObject(importClassObj);
    XWikiAttachment att = new XWikiAttachment();
    att.setFilename(fileName + ".zip");
    List<XWikiAttachment> attList = new ArrayList<>();
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(xwiki.clearName(eq(fileName), eq(false), eq(true), same(context))).andReturn(
        fileName);
    replayDefault();
    assertEquals(ImportFileObject.ACTION_ADD, imageService.getActionForFile(fileName, doc));
    verifyDefault();
  }
}
