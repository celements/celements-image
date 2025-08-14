package com.celements.photo.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponents;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.filebase.IAttachmentServiceRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.nextfreedoc.INextFreeDocRole;
import com.celements.photo.container.ImageDimensions;
import com.celements.photo.container.ImageLibStrings;
import com.celements.photo.image.GenerateThumbnail;
import com.celements.photo.utilities.ImportFileObject;
import com.celements.photo.utilities.Unzip;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

@Component
public class ImageService implements IImageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

  @Requirement
  private EntityReferenceResolver<String> stringRefResolver;

  @Requirement("celements.oldCoreClasses")
  private IClassCollectionRole oldCoreClasses;

  @Requirement
  private INavigationClassConfig navigationClassConfig;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  private ITreeNodeService treeNodeService;

  @Requirement
  private INextFreeDocRole nextFreeDocName;

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private IAttachmentServiceRole attService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IRightsAccessFacadeRole rightsAccess;

  @Requirement
  private ModelContext context;

  @Requirement
  private ModelUtils modelUtils;

  AttachmentURLCommand attURLCmd;

  private XWikiContext getContext() {
    return context.getXWikiContext();
  }

  private XWiki getXWiki() {
    return getContext().getWiki();
  }

  private OldCoreClasses getOldCoreClasses() {
    return (OldCoreClasses) oldCoreClasses;
  }


  @Override
  public BaseObject getPhotoAlbumObject(DocumentReference galleryDocRef) throws XWikiException {
    if (modelAccess.exists(galleryDocRef)) {
      XWikiDocument galleryDoc = modelAccess.getOrCreateDocument(galleryDocRef);
      BaseObject galleryObj = galleryDoc.getXObject(getOldCoreClasses().getPhotoAlbumClassRef(
        getContext().getDatabase()));
      return galleryObj;
    }
    return null;
  }

  @Override
  public BaseObject getPhotoAlbumNavObject(DocumentReference galleryDocRef) throws XWikiException,
      NoGalleryDocumentException {
    BaseObject navObj = null;
    if (modelAccess.exists(galleryDocRef)) {
      XWikiDocument galleryDoc = modelAccess.getOrCreateDocument(galleryDocRef);
      navObj = galleryDoc.getXObject(navigationClassConfig.getNavigationConfigClassRef(
          new RefBuilder().wiki(getContext().getDatabase()).build(WikiReference.class)));
      
    }
    if (navObj == null) {
        throw new NoGalleryDocumentException();
    }
    return navObj;
  }

  @Override
  public SpaceReference getPhotoAlbumSpaceRef(DocumentReference galleryDocRef)
      throws NoGalleryDocumentException {
    try {
      String spaceName = getPhotoAlbumNavObject(galleryDocRef).getStringValue(
          INavigationClassConfig.MENU_SPACE_FIELD);
      return new RefBuilder().with(galleryDocRef).space(spaceName).build(SpaceReference.class);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
    }
    return null;
  }

  public int getPhotoAlbumMaxHeight(DocumentReference galleryDocRef)
      throws NoGalleryDocumentException {
    try {
      int maxImageHeight = getPhotoAlbumObject(galleryDocRef).getIntValue("height2");
      // TODO allow template to reduce height
      if (!StringUtils.isEmpty(getContext().getRequest().getParameter("slideContent"))) {
        maxImageHeight = Math.max(maxImageHeight - 20, 0);
      }
      return maxImageHeight;
    } catch (XWikiException exp) {
      LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
    }
    return 2000;
  }

  public int getPhotoAlbumMaxWidth(DocumentReference galleryDocRef)
      throws NoGalleryDocumentException {
    try {
      return getPhotoAlbumObject(galleryDocRef).getIntValue("photoWidth");
    } catch (XWikiException exp) {
      LOGGER.error("Failed to getPhotoAlbumSpaceRef.", exp);
    }
    return 2000;
  }

  private DocumentReference getDocRefFromFullName(String collDocName) {
    DocumentReference eventRef = RefBuilder.from(stringRefResolver
        .resolve(collDocName, EntityType.DOCUMENT))
        .wiki(getContext().getDatabase())
        .build(DocumentReference.class);
    LOGGER.debug("getDocRefFromFullName: for [{}] got reference [{}].", collDocName, eventRef);
    return eventRef;
  }

  @Override
  public ImageDimensions getDimension(String imageFullName) throws XWikiException {
    String fullName = imageFullName.split(";")[0];
    String imageFileName = imageFullName.split(";")[1];
    DocumentReference docRef = getDocRefFromFullName(fullName);
    AttachmentReference imgRef = new AttachmentReference(imageFileName, docRef);
    return getDimension(imgRef);
  }

  @Override
  public ImageDimensions getDimension(AttachmentReference imgRef) throws XWikiException {
    DocumentReference docRef = (DocumentReference) imgRef.getParent();
    XWikiDocument theDoc = modelAccess.getOrCreateDocument(docRef);
    XWikiAttachment theAttachment = theDoc.getAttachment(imgRef.getName());
    ImageDimensions imageDimensions = null;
    GenerateThumbnail genThumbnail = new GenerateThumbnail();
    InputStream imageInStream = null;
    try {
      imageInStream = theAttachment.getContentInputStream(getContext());
      imageDimensions = genThumbnail.getImageDimensions(imageInStream);
    } finally {
      if (imageInStream != null) {
        try {
          imageInStream.close();
        } catch (IOException ioe) {
          LOGGER.error("Error closing InputStream.", ioe);
        }
      } else {
        imageDimensions = new ImageDimensions();
      }
    }
    return imageDimensions;
  }

  /**
   * getRandomImages computes a set of <num> randomly chosen images
   * from the given AttachmentList. It chooses the Images without duplicates if
   * possible.
   */
  @Override
  public List<Attachment> getRandomImages(DocumentReference galleryRef, int num) {
    Document imgDoc = modelAccess.getOrCreateDocument(galleryRef).newDocument(getContext());
    List<Attachment> allImagesList = webUtilsService.getAttachmentListSorted(imgDoc,
        "AttachmentAscendingNameComparator", true);
    if (allImagesList.size() > 0) {
      List<Attachment> preSetImgList = prepareMaxCoverSet(num, allImagesList);
      List<Attachment> imgList = new ArrayList<>(num);
      Random rand = new Random();
      for (int i = 1; i <= num; i++) {
        int nextimg = rand.nextInt(preSetImgList.size());
        imgList.add(preSetImgList.remove(nextimg));
      }
      return imgList;
    }
    return Collections.emptyList();
  }

  <T> List<T> prepareMaxCoverSet(int num, List<T> allImagesList) {
    List<T> preSetImgList = new ArrayList<>(num);
    preSetImgList.addAll(allImagesList);
    for (int i = 2; i <= coveredQuotient(allImagesList.size(), num); i++) {
      preSetImgList.addAll(allImagesList);
    }
    return preSetImgList;
  }

  int coveredQuotient(int divisor, int dividend) {
    if (dividend >= 0) {
      return (((dividend + divisor) - 1) / divisor);
    } else {
      return (dividend / divisor);
    }
  }

  private AttachmentURLCommand getAttURLCmd() {
    if (attURLCmd == null) {
      attURLCmd = new AttachmentURLCommand();
    }
    return attURLCmd;
  }

  @Override
  public boolean checkAddSlideRights(DocumentReference galleryDocRef) {
    try {
      DocumentReference newSlideDocRef = nextFreeDocName.getNextTitledPageDocRef(
          getPhotoAlbumSpaceRef(galleryDocRef), "Testname");
      return rightsAccess.hasAccessLevel(newSlideDocRef, EAccessLevel.EDIT);
    } catch (NoGalleryDocumentException exp) {
      LOGGER.debug("failed to checkAddSlideRights for no gallery document [" + galleryDocRef + "].",
          exp);
    }
    return false;
  }

  @Override
  public boolean addSlideFromTemplate(DocumentReference galleryDocRef, String slideBaseName,
      String attFullName) {
    try {
      String filename = attFullName.replaceAll("^.*;(.*)$", "$1");
      String clearedAttName = getXWiki().clearName(filename.replaceAll(
          "^(.*)\\.[a-zA-Z]{3,4}$", "$1"), true, true, getContext());
      String slideDocName = slideBaseName + clearedAttName;
      DocumentReference attDocRef = modelUtils.resolveRef(attFullName.replaceAll(
          "^(.*);.*$", "$1"), DocumentReference.class);
      Map<String, String> metaTagMap = getMetaTags(attDocRef, filename);
      if ((metaTagMap.size() > 0) && metaTagMap.keySet().contains("cleared_filename_short")) {
        slideDocName = slideBaseName + metaTagMap.get("cleared_filename_short");
      }
      DocumentReference newSlideDocRef = nextFreeDocName.getNextTitledPageDocRef(
          getPhotoAlbumSpaceRef(galleryDocRef), slideDocName);
      XWikiDocument newSlideDoc = modelAccess.getOrCreateDocument(newSlideDocRef);
      newSlideDoc.readFromTemplate(getImageSlideTemplateRef(), getContext());
      Optional<UriComponents> imgUriComponent = getAttURLCmd().getAttachmentURL(attFullName, "download", "");
      String imgURL = imgUriComponent.isPresent() ? imgUriComponent.get().toUriString() : "";
      String resizeParam = "celwidth=" + getPhotoAlbumMaxWidth(galleryDocRef) + "&celheight="
          + getPhotoAlbumMaxHeight(galleryDocRef);
      String fullImgURL = imgURL + ((imgURL.indexOf("?") < 0) ? "?" : "&") + resizeParam;
      addToVcontext(attFullName, metaTagMap, fullImgURL);
      DocumentReference slideContentRef = new DocumentReference(getContext().getDatabase(),
          "Templates", "ImageSlideImportContent");
      String slideContent = webUtilsService.renderInheritableDocument(slideContentRef,
          getContext().getLanguage(), context.getDefaultLanguage());
      newSlideDoc.setContent(slideContent);
      fixMenuItemPosition(newSlideDoc);
      modelAccess.saveDocument(newSlideDoc, "add default image slide content", true);
      return true;
    } catch (NoGalleryDocumentException exp) {
      LOGGER.error("failed to addSlideFromTemplate because no gallery doc.", exp);
    } catch (DocumentSaveException dse) {
      LOGGER.error("failed to addSlideFromTemplate - save document failed.", dse);
    } catch (XWikiException exp) {
      LOGGER.error("failed to addSlideFromTemplate.", exp);
    }
    return false;
  }
  
  private Map<String, String> getMetaTags(DocumentReference attDocRef, String filename) {
    DocumentReference centralFBDocRef = getCentralFBDocRef();
      if (modelAccess.exists(attDocRef) && !attDocRef.equals(centralFBDocRef)) {
        LOGGER.debug("get meta tags from attachment document {}", attDocRef);
        XWikiDocument attDoc = modelAccess.getOrCreateDocument(attDocRef);
        return getMetaTagObjectsFromDoc(attDoc);
      } else if (attDocRef.equals(centralFBDocRef)) {
        LOGGER.debug("get meta tags for central file base image {}", attDocRef);
        LuceneQuery query = getMetaTagsQuery(filename);
        LuceneSearchResult searchResult = searchService.search(query, null, null);
        List<EntityReference> resultList = Collections.emptyList();
        try {
          resultList = searchResult.getResults();
        } catch (LuceneSearchException lse) {
          LOGGER.error("Exception searching for imported images", lse);
        }
        LOGGER.debug("addSlideFromTemplate: lucene query = '{}'", query.getQueryString());
        LOGGER.debug("addSlideFromTemplate: DMS-Document for {} found: {}", filename, 
            resultList.size());
        Map<String, String> metaTagMap = Collections.emptyMap();
        if (resultList.size() > 0) {
          XWikiDocument separateDoc = modelAccess.getOrCreateDocument(
              RefBuilder.from(resultList.get(0)).build(DocumentReference.class));
           metaTagMap = getMetaTagObjectsFromDoc(separateDoc);
        } else {
          LOGGER.debug("getting meta tags for file [" + filename + "] on " + attDocRef);
          Map<String, String> map = getMetaInfoService().getAllTags(attDocRef, filename);
          metaTagMap = new HashMap<>();
          for (String key : map.keySet()) {
            metaTagMap.put(cleanMetaTagKey(key), cleanMetaTagValue(key, map.get(key)));
          }
        }
        return metaTagMap;
      } else {
        LOGGER.debug("don't get meta tags attachment doc [" + attDocRef + "] does not"
            + "exist and is not central file base " + centralFBDocRef);
        return Collections.emptyMap();
      }
  }

  private LuceneQuery getMetaTagsQuery(String filename) {
    LuceneQuery query = searchService.createQuery();
    query.add(searchService.createRestriction("Celements2.PageType.page_type",
        "\"DMS-Document\""));
    query.add(searchService.createRestriction("Classes.PhotoMetainfoClass.name",
        "\"cleared_filename\""));
    query.add(searchService.createRestriction("Classes.PhotoMetainfoClass.description",
        "\"" + filename + "\""));
    return query;
  }

  private DocumentReference getCentralFBDocRef() {
    String centralFB = getXWiki().getWebPreference("cel_centralfilebase",
        getContext());
    if ((centralFB != null) && !"".equals(centralFB.trim())) {
      return modelUtils.resolveRef(centralFB, DocumentReference.class);
    }
    return null;
  }

  private void addToVcontext(String attFullName, Map<String, String> metaTagMap, String fullImgURL) {
    VelocityContext vcontext = (VelocityContext) getContext().get("vcontext");
    vcontext.put("imageURL", fullImgURL);
    vcontext.put("attFullName", attFullName);
    vcontext.put("metaTagMap", metaTagMap);
  }

  Map<String, String> getMetaTagObjectsFromDoc(XWikiDocument attDoc) {
    Map<String, String> metaTagMap = new HashMap<>();
    DocumentReference tagClassRef = new RefBuilder().wiki(getContext().getDatabase())
        .space("Classes").doc("PhotoMetainfoClass").build(DocumentReference.class);
    List<BaseObject> metaObjs = attDoc.getXObjects(tagClassRef);
    if (metaObjs != null) {
      for (BaseObject tag : metaObjs) {
        if (tag != null) {
          metaTagMap.put(tag.getStringValue("name"), tag.getStringValue("description"));
        }
      }
    }
    return metaTagMap;
  }

  private boolean fixMenuItemPosition(XWikiDocument newSlideDoc) {
    if (treeNodeService.isTreeNode(newSlideDoc.getDocumentReference())) {
      BaseObject menuItemObj = newSlideDoc.getXObject(navigationClassConfig.getMenuItemClassRef(
          new RefBuilder().wiki(getContext().getDatabase()).build(WikiReference.class)));
      if (menuItemObj != null) {
        int numElem = treeNodeService.getSubNodesForParent(
            newSlideDoc.getDocumentReference().getLastSpaceReference(), "").size();
        menuItemObj.setIntValue(INavigationClassConfig.MENU_POSITION_FIELD, numElem);
        return true;
      }
    }
    return false;
  }

  String cleanMetaTagValue(String key, String value) {
    key = key.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
    key = key.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
    key = key.replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
    System.out.println(key);
    return value.replaceAll("^.*?" + key + " - (.*)$", "$1");
  }

  String cleanMetaTagKey(String key) {
    return key.replaceAll("^(\\[.*\\] )?(.*)$", "$2");
  }

  @Override
  public DocumentReference getImageSlideTemplateRef() {
    DocumentReference slideTemplateRef = new DocumentReference(getContext().getDatabase(),
        "ImageGalleryTemplates", "NewImageGallerySlide");
    if (!modelAccess.exists(slideTemplateRef)) {
      slideTemplateRef = new DocumentReference("celements2web", "ImageGalleryTemplates",
          "NewImageGallerySlide");
    }
    return slideTemplateRef;
  }

  @Override
  public Map<String, String> getImageURLinAllAspectRatios(XWikiAttachment ximage) {
    ImageDimensions dim = null;
    try {
      dim = (new GenerateThumbnail()).getThumbnailDimensions(ximage.getContentInputStream(
          getContext()), -1, -1, false, null);
    } catch (XWikiException xwe) {
      LOGGER.error("Exception reading image dimensions", xwe);
    }
    Map<String, String> urlMap = new HashMap<>();
    if (dim != null) {
      String baseURL = ximage.getDoc().getExternalAttachmentURL(ximage.getFilename(), "download",
          getContext());
      urlMap.put("orig", baseURL);
      if (baseURL.indexOf("?") < 0) {
        baseURL += "?";
      } else if (!baseURL.endsWith("&")) {
        baseURL += "&";
      }
      urlMap.put("1:1", baseURL + getFixedAspectURL(dim, 1, 1));
      urlMap.put("3:4", baseURL + getFixedAspectURL(dim, 3, 4));
      urlMap.put("4:3", baseURL + getFixedAspectURL(dim, 4, 3));
      urlMap.put("16:9", baseURL + getFixedAspectURL(dim, 16, 9));
      urlMap.put("16:10", baseURL + getFixedAspectURL(dim, 16, 10));
    }
    return urlMap;
  }

  String getFixedAspectURL(ImageDimensions dim, int xFact, int yFact) {
    double width = dim.getWidth();
    double height = dim.getHeight();
    double isAspRatio = width / height;
    double targetAspRatio = xFact / (double) yFact;
    double epsylon = 0.00001;
    String urlParams = "";
    // only crop if dim does not matche target aspect ratio
    if (Math.abs(isAspRatio - targetAspRatio) > epsylon) {
      if (isAspRatio < targetAspRatio) {
        urlParams += "cropX=0&cropW=" + (int) width;
        int newHeight = (int) Math.floor(width * (1 / targetAspRatio));
        int top = (int) Math.floor((height - newHeight) / 2);
        urlParams += "&cropY=" + top + "&cropH=" + newHeight;
      } else {
        int newWidth = (int) Math.floor(height * targetAspRatio);
        int left = (int) Math.floor((width - newWidth) / 2);
        urlParams += "cropX=" + left + "&cropW=" + newWidth;
        urlParams += "&cropY=0&cropH=" + (int) height;
      }
    }
    return urlParams;
  }

  private IMetaInfoService getMetaInfoService() {
    return Utils.getComponent(IMetaInfoService.class);
  }

  /**
   * Get a List of all attachments in the specified archive and the suggested
   * action when importing.
   *
   * @param importFile
   *          Zip archive to check the files for the existence in the gallery.
   * @param galleryDoc
   *          Gallery Document to check for the files.
   * @param context
   *          XWikiContext
   * @return List of {@link ImportFileObject} for each file.
   * @throws XWikiException
   */
  @Override
  public List<ImportFileObject> getAttachmentFileListWithActions(XWikiAttachment importFile,
      XWikiDocument galleryDoc) throws XWikiException {
    List<ImportFileObject> resultList = new ArrayList<>();

    if (importFile != null) {
      if (isZipFile(importFile)) {
        List<String> fileList;
        try {
          fileList = (new Unzip()).getZipContentList(IOUtils.toByteArray(
              importFile.getContentInputStream(getContext())));
          String fileSep = System.getProperty("file.separator");
          for (String string : fileList) {
            String fileName = string;
            if (!fileName.endsWith(fileSep) && !fileName.startsWith(".") && !fileName.contains(
                fileSep + ".")) {
              ImportFileObject file = new ImportFileObject(fileName, getActionForFile(fileName,
                  galleryDoc));
              resultList.add(file);
            }
          }
        } catch (IOException ioe) {
          LOGGER.error("Error reading file.", ioe);
        }
      } else if (isImgFile(importFile)) {
        ImportFileObject file = new ImportFileObject(importFile.getFilename(), getActionForFile(
            importFile.getFilename(), galleryDoc));
        resultList.add(file);
      }
    } else {
      LOGGER.error("zipFile='null' - galleryDoc='" + galleryDoc.getDocumentReference() + "'");
    }

    return resultList;
  }

  /**
   * For a given filename return if, in the specified gallery, its import
   * should be added, overwritten or skiped.
   *
   * @param fileName
   *          Filename of the file to check.
   * @param galleryDoc
   *          Document of the gallery to check if the file already exists.
   * @return action when importing: -1 skip, 0 overwrite, 1 add
   */
  short getActionForFile(String fileName, XWikiDocument galleryDoc) {
    short action = ImportFileObject.ACTION_SKIP;
    boolean isImportToFilebase = getContext().getDoc().getXObjectSize(getImportClassRef()) == 0;
    LOGGER.debug("getActionForFile [" + fileName + "] on gallery doc [" + galleryDoc
        + "], isImportToFilebase [" + isImportToFilebase + "]");
    if (isImgFile(fileName) || isImportToFilebase) {
      fileName = fileName.replace(System.getProperty("file.separator"), ".");
      fileName = getXWiki().clearName(fileName, false, true, getContext());
      if (!attService.existsAttachmentNameEqual(galleryDoc, fileName)) {
        action = ImportFileObject.ACTION_ADD;
      } else {
        action = ImportFileObject.ACTION_OVERWRITE;
      }
    }
    return action;
  }

  DocumentReference getImportClassRef() {
    return new RefBuilder().wiki(getContext().getDatabase()).space("Classes")
        .doc("ImportClass").build(DocumentReference.class);
  }

  private boolean isZipFile(XWikiAttachment file) {
    return file.getMimeType(getContext()).equalsIgnoreCase(ImageLibStrings.MIME_ZIP)
        || file.getMimeType(getContext()).equalsIgnoreCase(ImageLibStrings.MIME_ZIP_MICROSOFT);
  }

  private boolean isImgFile(String fileName) {
    return fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_BMP)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_GIF)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPE)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPG)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_JPEG)
        || fileName.toLowerCase().endsWith("." + ImageLibStrings.MIME_PNG);
  }

  private boolean isImgFile(XWikiAttachment file) {
    return file.getMimeType(getContext()).equalsIgnoreCase("image/" + ImageLibStrings.MIME_BMP)
        || file.getMimeType(getContext()).equalsIgnoreCase("image/" + ImageLibStrings.MIME_GIF)
        || file.getMimeType(getContext()).equalsIgnoreCase("image/" + ImageLibStrings.MIME_JPE)
        || file.getMimeType(getContext()).equalsIgnoreCase("image/" + ImageLibStrings.MIME_JPG)
        || file.getMimeType(getContext()).equalsIgnoreCase("image/" + ImageLibStrings.MIME_JPEG)
        || file.getMimeType(getContext()).equalsIgnoreCase("image/" + ImageLibStrings.MIME_PNG);
  }

}
