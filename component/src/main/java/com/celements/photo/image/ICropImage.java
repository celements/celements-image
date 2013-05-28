package com.celements.photo.image;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;

@ComponentRole
public interface ICropImage {
  public OutputStream crop(Document doc, String filename, int x, int y, int w, int h,
      OutputStream out);
  
  public void crop(BufferedImage img, int x, int y, int w, int h, String type, 
      OutputStream out); 
  
  public OutputStream crop(XWikiAttachment xAtt, int x, int y, int w, int h);
  
  public OutputStream crop(XWikiAttachment xAtt, int x, int y, int w, int h, 
      OutputStream out);

  public void outputCroppedImage(Document srcDoc, String srcFilename, int x, int y,
      int w, int h);
}
