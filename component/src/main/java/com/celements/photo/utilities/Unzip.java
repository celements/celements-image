/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.photo.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiException;

public class Unzip {
  private static final int BUFFER = 1024;
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(Unzip.class);
  
  public Unzip(){}
  
  /**
   * Extracts the specified file from a given zip archive.
   * 
   * @param zipFile byte array representation of a zip archive.
   * @param filename Name of the file which should be extracted.
   * @return A ByteArrayOutputStream of the extractes file.
   * @throws XWikiException
   * @throws IOException
   */
  public ByteArrayOutputStream getFile(byte[] zipFile, String filename
      ) throws XWikiException, IOException{
    return findAndExtractFile(filename, getZipInputStream(zipFile));
  }

  public ByteArrayOutputStream getFile(String filename, InputStream zipFile
      ) throws XWikiException, IOException{
    return findAndExtractFile(filename, getZipInputStream(zipFile));
  }
  
  private ByteArrayOutputStream findAndExtractFile(String filename, ZipInputStream zipIn
      ) throws IOException {
    ByteArrayOutputStream out = null;
    for(ZipEntry entry = zipIn.getNextEntry(); zipIn.available()>0; entry = 
        zipIn.getNextEntry()){
      if(!entry.isDirectory() && entry.getName().equals(filename)){
        // read the data and write it to the OutputStream
        int count;
        byte[] data = new byte[BUFFER];
        out = new ByteArrayOutputStream();
        BufferedOutputStream byteOut = new BufferedOutputStream(out, BUFFER);
        while ((count = zipIn.read(data, 0, BUFFER)) != -1) {
          byteOut.write(data, 0, count);
        }
        byteOut.flush();
        break;
      }
    }
    
    zipIn.close();
    return out;
  }
  
  /**
   * Get a List of names of all files contained in the zip file.
   * 
   * @param zipFile byte array of the zip file.
   * @return List of all filenames (and directory names - ending with a file seperator) 
   *              contained in the zip file.
   */
  public List<String> getZipContentList(byte[] zipFile){
    String fileSep = System.getProperty("file.separator");
    List<String> contentList = new ArrayList<String>();
    ZipInputStream zipStream = getZipInputStream(zipFile);
    
    try {
      while(zipStream.available() > 0){
        ZipEntry entry = zipStream.getNextEntry();
        if(entry != null){
          String fileName = entry.getName();
          if(entry.isDirectory() && !fileName.endsWith(fileSep)){
            fileName += fileSep;
          }
          contentList.add(fileName);
        }
      }
    } catch (IOException e) {
      LOGGER.error(e);
    }
    
    return contentList;
  }
  
  /*
   * Get a ZiInputStream for the specified file.
   * 
   * @param srcFile byte array representation of a zip file.
   * @return A ZipInputStream for the file.
   */
  private ZipInputStream getZipInputStream(byte[] srcFile) {
    ByteArrayInputStream in = new ByteArrayInputStream(srcFile);
    return getZipInputStream(in);
  }

  private ZipInputStream getZipInputStream(InputStream in) {
    //historically zip supported only IBM Code Page 437 encoding. Today others can occur,
    // like e.g. UTF-8 or OS defaults
    return new ZipInputStream(new BufferedInputStream(in), Charset.forName("UTF-8"));//"Cp437"));
  }
}
