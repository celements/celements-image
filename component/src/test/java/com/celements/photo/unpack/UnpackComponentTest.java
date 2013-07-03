package com.celements.photo.unpack;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.doc.XWikiAttachment;

public class UnpackComponentTest extends AbstractBridgedComponentTestCase {
  UnpackComponent upc;

  @Before
  public void setUp_UnpackComponentTest() throws Exception {
    upc = new UnpackComponent();
  }

//TODO solve Execution / Context NullPointerException
//  @Test
//  public void testUnzipFileToAttachmentXWikiDocumentStringStringXWikiDocument() {
//    fail("Not yet implemented");
//  }
//
//  @Test
//  public void testUnzipFileToAttachmentXWikiAttachmentStringXWikiDocument() {
//    XWikiAttachment zipAtt = new XWikiAttachment();
//    upc.unzipFileToAttachment(zipAtt, "name.jpg", new DocumentReference(
//        getContext().getDatabase(), "Space", "DestDoc"));
//    fail("Not yet implemented");
//  }
//  
//  @Test
//  public void testIsZipFile_false() {
//    assertFalse(upc.isZipFile(null));
//    XWikiAttachment att = new XWikiAttachment(null, "name.txt");
//    assertFalse(upc.isZipFile(att));
//  }
//
//  @Test
//  public void testIsZipFile_true() {
//    XWikiAttachment att = new XWikiAttachment(null, "name.zip");
//    assertTrue(upc.isZipFile(att));
//  }

  @Test
  public void testIsImgFile_false() {
    assertFalse(upc.isImgFile(null));
    assertFalse(upc.isImgFile(""));
    assertFalse(upc.isImgFile("123.jzpg"));
    assertFalse(upc.isImgFile("jpg"));
    assertFalse(upc.isImgFile("1.png.txt"));
  }

  @Test
  public void testIsImgFile_true() {
    assertTrue(upc.isImgFile("123.png"));
    assertTrue(upc.isImgFile("123.txt.jpg"));
  }
}
