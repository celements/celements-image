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
import com.celements.model.reference.RefBuilder;
import com.celements.navigation.NavigationClasses;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.photo.container.ImageDimensions;
import com.celements.photo.utilities.ImportFileObject;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.XWikiRequest;

public class ImageServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private ImageService imageService;
  private XWiki xwiki;
  private XWikiRightService rightServiceMock;
  private ITreeNodeService treeNodeServiceMock;
  private INextFreeDocRole nextFreeDocMock;
  private IModelAccessFacade modelAccess;
  private ModelContext modelContext;
  private RefBuilder refBuilder;

  @Before
  public void setUp() throws Exception {
    context = getXContext();
    xwiki = getMock(XWiki.class);
    rightServiceMock = createDefaultMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    treeNodeServiceMock = registerComponentMock(ITreeNodeService.class);
    nextFreeDocMock = registerComponentMock(INextFreeDocRole.class);
    modelAccess = registerComponentMock(IModelAccessFacade.class);
    modelContext = registerComponentMock(ModelContext.class);
    expect(modelContext.getXWikiContext()).andReturn(context).anyTimes();
    expect(modelContext.getWikiRef()).andReturn(new WikiReference(context.getDatabase())).anyTimes();
    refBuilder = registerComponentMock(RefBuilder.class);
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
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty());
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
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty());
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
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty());
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
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty());
    expectRefBuildWith(galleryDocRef, expectedSpaceRef, gallerySpaceName);
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
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty());
    replayDefault();
    assertFalse("Expecting no addSlide rights if document is no gallery document.",
        imageService.checkAddSlideRights(galleryDocRef));
    verifyDefault();
  }

  @Test
  public void testGetImageSlideTemplateRef_local() throws Exception {
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
        "ImageGalleryTemplates", "NewImageGallerySlide");
    expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(true).once();
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
    expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(false).once();
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
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty());
    expectRefBuildWith(galleryDocRef, spaceRef, gallerySpaceName);
    DocumentReference testSlideDocRef = new DocumentReference(context.getDatabase(),
        gallerySpaceName, "Testname1");
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(editorUser), eq(
        "xwikidb:gallerySpace.Testname1"), same(context))).andReturn(true).once();
    expect(nextFreeDocMock.getNextTitledPageDocRef(spaceRef, "Testname")).andReturn(
        testSlideDocRef);
    replayDefault();
    assertTrue("Expecting addSlide rights if 'edit' rights on space available",
        imageService.checkAddSlideRights(galleryDocRef));
    verifyDefault();
  }

  @Test
  public void testCheckAddSlideRights_no() throws Exception {
    String editorUser = "XWiki.myNoEditor";
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
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty());
    expectRefBuildWith(galleryDocRef, spaceRef, gallerySpaceName);
    DocumentReference testSlideDocRef = new DocumentReference(context.getDatabase(),
        gallerySpaceName, "Testname1");
    expect(rightServiceMock.hasAccessLevel(eq("edit"), eq(editorUser), eq(
        "xwikidb:gallerySpace.Testname1"), same(context))).andReturn(false).once();
    expect(nextFreeDocMock.getNextTitledPageDocRef(spaceRef, "Testname")).andReturn(
        testSlideDocRef);
    replayDefault();
    assertFalse("Expecting no addSlide rights if no 'edit' rights on space available",
        imageService.checkAddSlideRights(galleryDocRef));
    verifyDefault();
  }

  @Test
  public void testAddSlideFromTemplate() throws Exception {
    String editorUser = "XWiki.myEditor";
    context.setUser(editorUser);
    AttachmentURLCommand attURLCmdMock = createDefaultMock(AttachmentURLCommand.class);
    imageService.attURLCmd = attURLCmdMock;
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "galleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    BaseObject photoAlbumNavObj = new BaseObject();
    photoAlbumNavObj.setXClassReference(new NavigationClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    String gallerySpaceName = "gallerySpace";
    photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
    galleryDoc.addXObject(photoAlbumNavObj);
    BaseObject photoAlbumObj = new BaseObject();
    photoAlbumObj.setXClassReference(new OldCoreClasses().getPhotoAlbumClassRef(
        context.getDatabase()));
    int maxWidth = 800;
    int maxHeight = 800;
    photoAlbumObj.setIntValue("height2", maxHeight);
    photoAlbumObj.setIntValue("photoWidth", maxWidth);
    galleryDoc.addXObject(photoAlbumObj);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).atLeastOnce();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).atLeastOnce();
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty(), 2);
    DocumentReference slideDocRef = new DocumentReference(context.getDatabase(), gallerySpaceName,
        "Slide1");
    XWikiDocument slideDoc = new XWikiDocument(slideDocRef);
    expect(modelAccess.getOrCreateDocument(eq(slideDocRef))).andReturn(slideDoc).once();
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
        "ImageGalleryTemplates", "NewImageGallerySlide");
    expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(true).once();
    expect(xwiki.copyDocument(eq(localTemplateRef), eq(slideDocRef), eq(true), same(
        context))).andReturn(true).once();
    String attFilename = "myImg.png";
    String attFilenameNoExtension = "myImg";
    String clearedAttFilename = "myImg";
    String attFullName = "ContentAttachment.FileBaseDoc;" + attFilename;
    String imgAttURL = "/download/ContentAttachment/FileBaseDoc/" + attFilename;
    Optional<UriComponents> imgAttURIComp = Optional.of(UriComponentsBuilder.fromUriString(imgAttURL).build());
    expect(attURLCmdMock.getAttachmentURL(eq(attFullName), eq("download"), eq(""))).andReturn(imgAttURIComp).once();
    imageService.webUtilsService = createDefaultMock(IWebUtilsService.class);
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(),
        "ContentAttachment", "FileBaseDoc");
    IWebUtilsService webUtils = imageService.webUtilsService;
    XWikiDocument attDoc = new XWikiDocument(new DocumentReference("a", "b", "c"));
    SpaceReference spaceRef = new SpaceReference(gallerySpaceName, new WikiReference(
        context.getDatabase()));
    expect(modelAccess.exists(eq(attDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(attDocRef))).andReturn(attDoc).once();
    expectRefBuildWith(galleryDocRef, spaceRef, gallerySpaceName, 2);
    xwiki.saveDocument(same(slideDoc), eq("add default image slide content"), eq(true), same(
        context));
    expectLastCall().once();
    expect(webUtils.getWikiRef((DocumentReference) anyObject())).andReturn(
        attDocRef.getWikiReference()).anyTimes();
    expect(modelContext.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(modelContext.getDefaultLanguage((EntityReference) anyObject())).andReturn("de").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), eq("gallerySpace"), eq(""), same(
        context))).andReturn("de").anyTimes();
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference imgImportContentRef = new DocumentReference(context.getDatabase(),
        "Templates", "ImageSlideImportContent");
    expect(webUtils.renderInheritableDocument(eq(imgImportContentRef), eq(context.getLanguage()),
        eq("de"))).andReturn("content").once();
    expect(xwiki.getWebPreference(eq("cel_centralfilebase"), same(context))).andReturn(
        "").once();
    expect(treeNodeServiceMock.isTreeNode(eq(slideDocRef))).andReturn(false).anyTimes();
    XWikiRequest mockRequest = createDefaultMock(XWikiRequest.class);
    expect(mockRequest.getParameter(eq("slideContent"))).andReturn("");
    context.setRequest(mockRequest);
    expect(nextFreeDocMock.getNextTitledPageDocRef(eq(spaceRef), eq("Slide"
        + clearedAttFilename))).andReturn(slideDocRef);
    expect(xwiki.clearName(eq(attFilenameNoExtension), eq(true), eq(true), same(
        context))).andReturn(clearedAttFilename).once();
    DocumentReference tagClassRef = new DocumentReference(context.getDatabase(), "Classes", "PhotoMetainfoClass");
    expectRefBuild(DocumentReference.class, tagClassRef, Optional.empty(), 
        Optional.of("Classes"), Optional.of("PhotoMetainfoClass"));
    replayDefault();
    assertTrue("Expecting successful adding slide", imageService.addSlideFromTemplate(galleryDocRef,
        "Slide", attFullName));
    String expectedImgURL = imgAttURL + "?celwidth=" + maxWidth + "&celheight=" + maxHeight;
    verifyDefault();
    assertEquals(expectedImgURL, vcontext.get("imageURL"));
    assertEquals(attFullName, vcontext.get("attFullName"));
    assertEquals(0, ((Map<?, ?>) vcontext.get("metaTagMap")).size());
  }

  @Test
  public void testAddSlideFromTemplate_slideContentNull() throws Exception {
    String editorUser = "XWiki.myEditor";
    context.setUser(editorUser);
    AttachmentURLCommand attURLCmdMock = createDefaultMock(AttachmentURLCommand.class);
    imageService.attURLCmd = attURLCmdMock;
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "galleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    BaseObject photoAlbumNavObj = new BaseObject();
    photoAlbumNavObj.setXClassReference(new NavigationClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    String gallerySpaceName = "gallerySpace";
    photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
    galleryDoc.addXObject(photoAlbumNavObj);
    BaseObject photoAlbumObj = new BaseObject();
    photoAlbumObj.setXClassReference(new OldCoreClasses().getPhotoAlbumClassRef(
        context.getDatabase()));
    int maxWidth = 800;
    int maxHeight = 800;
    photoAlbumObj.setIntValue("height2", maxHeight);
    photoAlbumObj.setIntValue("photoWidth", maxWidth);
    galleryDoc.addXObject(photoAlbumObj);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).atLeastOnce();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).atLeastOnce();
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty(), 2);
    DocumentReference slideDocRef = new DocumentReference(context.getDatabase(), gallerySpaceName,
        "Slide1");
    XWikiDocument slideDoc = new XWikiDocument(slideDocRef);
    expect(modelAccess.getOrCreateDocument(eq(slideDocRef))).andReturn(slideDoc).once();
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
        "ImageGalleryTemplates", "NewImageGallerySlide");
    expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(true).once();
    expect(xwiki.copyDocument(eq(localTemplateRef), eq(slideDocRef), eq(true), same(
        context))).andReturn(true).once();
    String attFilename = "myImg.png";
    String attFilenameNoExtension = "myImg";
    String clearedAttFilename = "myImg";
    String attFullName = "ContentAttachment.FileBaseDoc;" + attFilename;
    String imgAttURL = "/download/ContentAttachment/FileBaseDoc/" + attFilename;
    Optional<UriComponents> imgAttURIComp = Optional.of(UriComponentsBuilder.fromUriString(imgAttURL).build());
    expect(attURLCmdMock.getAttachmentURL(eq(attFullName), eq("download"), eq(""))).andReturn(imgAttURIComp).once();
    imageService.webUtilsService = createDefaultMock(IWebUtilsService.class);
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(),
        "ContentAttachment", "FileBaseDoc");
    IWebUtilsService webUtils = imageService.webUtilsService;
    XWikiDocument attDoc = new XWikiDocument(new DocumentReference("a", "b", "c"));
    expect(modelAccess.exists(eq(attDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(attDocRef))).andReturn(attDoc).once();
    xwiki.saveDocument(same(slideDoc), eq("add default image slide content"), eq(true), same(
        context));
    expectLastCall().once();
    expect(webUtils.getWikiRef((DocumentReference) anyObject())).andReturn(
        attDocRef.getWikiReference()).anyTimes();
    expect(modelContext.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(modelContext.getDefaultLanguage((EntityReference) anyObject())).andReturn("de").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), eq("gallerySpace"), eq(""), same(
        context))).andReturn("de").anyTimes();
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference imgImportContentRef = new DocumentReference(context.getDatabase(),
        "Templates", "ImageSlideImportContent");
    expect(webUtils.renderInheritableDocument(eq(imgImportContentRef), eq(context.getLanguage()),
        eq("de"))).andReturn("content").once();
    expect(xwiki.getWebPreference(eq("cel_centralfilebase"), same(context))).andReturn(
        "").once();
    expect(treeNodeServiceMock.isTreeNode(eq(slideDocRef))).andReturn(false).anyTimes();
    XWikiRequest mockRequest = createDefaultMock(XWikiRequest.class);
    expect(mockRequest.getParameter(eq("slideContent"))).andReturn(null);
    context.setRequest(mockRequest);
    SpaceReference spaceRef = new SpaceReference(gallerySpaceName, new WikiReference(
        context.getDatabase()));
    expectRefBuildWith(galleryDocRef, spaceRef, gallerySpaceName, 2);
    expect(nextFreeDocMock.getNextTitledPageDocRef(eq(spaceRef), eq("Slide"
        + clearedAttFilename))).andReturn(slideDocRef);
    expect(xwiki.clearName(eq(attFilenameNoExtension), eq(true), eq(true), same(
        context))).andReturn(clearedAttFilename).once();
    DocumentReference tagClassRef = new DocumentReference(context.getDatabase(), "Classes", "PhotoMetainfoClass");
    expectRefBuild(DocumentReference.class, tagClassRef, Optional.empty(), 
        Optional.of("Classes"), Optional.of("PhotoMetainfoClass"));
    replayDefault();
    assertTrue("Expecting successful adding slide", imageService.addSlideFromTemplate(galleryDocRef,
        "Slide", attFullName));
    String expectedImgURL = imgAttURL + "?celwidth=" + maxWidth + "&celheight=" + maxHeight;
    verifyDefault();
    assertEquals(expectedImgURL, vcontext.get("imageURL"));
    assertEquals(attFullName, vcontext.get("attFullName"));
    assertEquals(0, ((Map<?, ?>) vcontext.get("metaTagMap")).size());
  }

  @Test
  public void testAddSlideFromTemplate_with_caption() throws Exception {
    String editorUser = "XWiki.myEditor";
    context.setUser(editorUser);
    AttachmentURLCommand attURLCmdMock = createDefaultMock(AttachmentURLCommand.class);
    imageService.attURLCmd = attURLCmdMock;
    DocumentReference galleryDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "galleryDoc");
    XWikiDocument galleryDoc = new XWikiDocument(galleryDocRef);
    BaseObject photoAlbumNavObj = new BaseObject();
    photoAlbumNavObj.setXClassReference(new NavigationClasses().getNavigationConfigClassRef(
        context.getDatabase()));
    String gallerySpaceName = "gallerySpace";
    photoAlbumNavObj.setStringValue("menu_space", gallerySpaceName);
    galleryDoc.addXObject(photoAlbumNavObj);
    BaseObject photoAlbumObj = new BaseObject();
    photoAlbumObj.setXClassReference(new OldCoreClasses().getPhotoAlbumClassRef(
        context.getDatabase()));
    int maxWidth = 800;
    int maxHeight = 800;
    photoAlbumObj.setIntValue("height2", maxHeight);
    photoAlbumObj.setIntValue("photoWidth", maxWidth);
    galleryDoc.addXObject(photoAlbumObj);
    expect(modelAccess.exists(eq(galleryDocRef))).andReturn(true).atLeastOnce();
    expect(modelAccess.getOrCreateDocument(eq(galleryDocRef))).andReturn(galleryDoc).atLeastOnce();
    expectRefBuild(WikiReference.class, new WikiReference(context.getDatabase()), 
        Optional.of(context.getDatabase()), Optional.empty(), Optional.empty(), 2);
    DocumentReference slideDocRef = new DocumentReference(context.getDatabase(), gallerySpaceName,
        "Slide1");
    XWikiDocument slideDoc = new XWikiDocument(slideDocRef);
    expect(modelAccess.getOrCreateDocument(eq(slideDocRef))).andReturn(slideDoc).once();
    DocumentReference localTemplateRef = new DocumentReference(context.getDatabase(),
        "ImageGalleryTemplates", "NewImageGallerySlide");
    expect(xwiki.exists(eq(localTemplateRef), same(context))).andReturn(true).once();
    expect(xwiki.copyDocument(eq(localTemplateRef), eq(slideDocRef), eq(true), same(
        context))).andReturn(true).once();
    String attFilename = "myImg.png";
    String attFilenameNoExtension = "myImg";
    String clearedAttFilename = "myImg";
    String attFullName = "ContentAttachment.FileBaseDoc;" + attFilename;
    String imgAttURL = "/download/ContentAttachment/FileBaseDoc/" + attFilename;
    Optional<UriComponents> imgAttURIComp = Optional.of(UriComponentsBuilder.fromUriString(imgAttURL).build());
    expect(attURLCmdMock.getAttachmentURL(eq(attFullName), eq("download"), eq(""))).andReturn(imgAttURIComp).once();
    imageService.webUtilsService = createDefaultMock(IWebUtilsService.class);
    DocumentReference attDocRef = new DocumentReference(context.getDatabase(),
        "ContentAttachment", "FileBaseDoc");
    IWebUtilsService webUtils = imageService.webUtilsService;
    XWikiDocument attDoc = new XWikiDocument(new DocumentReference("a", "b", "c"));
    expect(modelAccess.exists(eq(attDocRef))).andReturn(true).once();
    expect(modelAccess.getOrCreateDocument(eq(attDocRef))).andReturn(attDoc).once();
    xwiki.saveDocument(same(slideDoc), eq("add default image slide content"), eq(true), same(
        context));
    expectLastCall().once();
    expect(webUtils.getWikiRef((DocumentReference) anyObject())).andReturn(
        attDocRef.getWikiReference()).anyTimes();
    expect(modelContext.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(modelContext.getDefaultLanguage((EntityReference) anyObject())).andReturn("de").anyTimes();
    expect(xwiki.getSpacePreference(eq("default_language"), eq("gallerySpace"), eq(""), same(
        context))).andReturn("de").anyTimes();
    VelocityContext vcontext = new VelocityContext();
    context.put("vcontext", vcontext);
    DocumentReference imgImportContentRef = new DocumentReference(context.getDatabase(),
        "Templates", "ImageSlideImportContent");
    expect(webUtils.renderInheritableDocument(eq(imgImportContentRef), eq(context.getLanguage()),
        eq("de"))).andReturn("content").once();
    expect(xwiki.getWebPreference(eq("cel_centralfilebase"), same(context))).andReturn(
        "").once();
    expect(treeNodeServiceMock.isTreeNode(eq(slideDocRef))).andReturn(false).anyTimes();
    XWikiRequest mockRequest = createDefaultMock(XWikiRequest.class);
    expect(mockRequest.getParameter(eq("slideContent"))).andReturn("test content line");
    context.setRequest(mockRequest);
    SpaceReference spaceRef = new SpaceReference(gallerySpaceName, new WikiReference(
        context.getDatabase()));
    expectRefBuildWith(galleryDocRef, spaceRef, gallerySpaceName, 2);
    expect(nextFreeDocMock.getNextTitledPageDocRef(eq(spaceRef), eq("Slide"
        + clearedAttFilename))).andReturn(slideDocRef);
    expect(xwiki.clearName(eq(attFilenameNoExtension), eq(true), eq(true), same(
        context))).andReturn(clearedAttFilename).once();
    DocumentReference tagClassRef = new DocumentReference(context.getDatabase(), "Classes", "PhotoMetainfoClass");
    expectRefBuild(DocumentReference.class, tagClassRef, Optional.empty(), 
        Optional.of("Classes"), Optional.of("PhotoMetainfoClass"));
    replayDefault();
    assertTrue("Expecting successful adding slide", imageService.addSlideFromTemplate(galleryDocRef,
        "Slide", attFullName));
    String expectedImgURL = imgAttURL + "?celwidth=" + maxWidth + "&celheight=" + (maxHeight - 20);
    verifyDefault();
    assertEquals(expectedImgURL, vcontext.get("imageURL"));
    assertEquals(attFullName, vcontext.get("attFullName"));
    assertEquals(0, ((Map<?, ?>) vcontext.get("metaTagMap")).size());
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
    expectRefBuild(DocumentReference.class, importClassRef, Optional.empty(),
        Optional.of("Classes"), Optional.of("ImportClass"));
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
    expectRefBuild(DocumentReference.class, importClassRef, Optional.empty(),
        Optional.of("Classes"), Optional.of("ImportClass"));
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
    expectRefBuild(DocumentReference.class, importClassRef, Optional.empty(),
        Optional.of("Classes"), Optional.of("ImportClass"));
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
    expectRefBuild(DocumentReference.class, importClassRef, Optional.empty(),
        Optional.of("Classes"), Optional.of("ImportClass"));
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
  
  private void expectRefBuildWith(DocumentReference galleryDocRef, SpaceReference expectedSpaceRef, String gallerySpaceName) {
    expectRefBuildWith(galleryDocRef, expectedSpaceRef, gallerySpaceName, 1);
  }
  
  private void expectRefBuildWith(DocumentReference galleryDocRef, SpaceReference expectedSpaceRef, 
      String gallerySpaceName, int times) {
    expect(refBuilder.with(eq(galleryDocRef))).andReturn(refBuilder).times(times);
    expect(refBuilder.space(eq(gallerySpaceName))).andReturn(refBuilder).times(times);
    expect(refBuilder.build(eq(SpaceReference.class))).andReturn(expectedSpaceRef).times(times);
  }

  @SuppressWarnings("unchecked")
  private <T extends EntityReference> void expectRefBuild(Class clazz, T ref, Optional<String> wiki, 
        Optional<String> space, Optional<String> doc) {
    expectRefBuild(clazz, ref, wiki, space, doc, 1);
  }

  @SuppressWarnings("unchecked")
  private <T extends EntityReference> void expectRefBuild(Class clazz, T ref, Optional<String> wiki, 
        Optional<String> space, Optional<String> doc, int times) {
    if (wiki.isPresent()) {
      expect(refBuilder.wiki((String) wiki.get())).andReturn(refBuilder).times(times);
    }
    if (space.isPresent()) {
      expect(refBuilder.space((String) space.get())).andReturn(refBuilder).times(times);
    }
    if (doc.isPresent()) {
      expect(refBuilder.doc((String) doc.get())).andReturn(refBuilder).times(times);
    }
    expect(refBuilder.build(same(clazz))).andReturn(ref).times(times);
  }
}
