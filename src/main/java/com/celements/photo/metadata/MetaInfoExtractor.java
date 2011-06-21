package com.celements.photo.metadata;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.CanonMakernoteDirectory;
import com.drew.metadata.exif.CasioType1MakernoteDirectory;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.FujifilmMakernoteDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.exif.KodakMakernoteDirectory;
import com.drew.metadata.exif.KyoceraMakernoteDirectory;
import com.drew.metadata.exif.NikonType1MakernoteDirectory;
import com.drew.metadata.exif.NikonType2MakernoteDirectory;
import com.drew.metadata.exif.OlympusMakernoteDirectory;
import com.drew.metadata.exif.PanasonicMakernoteDirectory;
import com.drew.metadata.exif.PentaxMakernoteDirectory;
import com.drew.metadata.exif.SonyMakernoteDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.jpeg.JpegCommentDirectory;
import com.drew.metadata.jpeg.JpegDirectory;

/**
 * This class provides the metadata saved in a specified image. There are 
 * several methods to get certain parts of the data.
 */
public class MetaInfoExtractor {

  static Log mLogger = LogFactory.getFactory().getInstance(
      MetaInfoExtractor.class);

  // Directories
  public final static Class<ExifDirectory> EXIF = ExifDirectory.class;
  public final static Class<GpsDirectory> GPS = GpsDirectory.class;
  public final static Class<IptcDirectory> IPTC = IptcDirectory.class;
  public final static Class<JpegDirectory> JPEG = JpegDirectory.class;
  public final static Class<JpegCommentDirectory> JPEG_COMMENT = JpegCommentDirectory.class;
  public final static Class<CanonMakernoteDirectory> CANON = CanonMakernoteDirectory.class;
  public final static Class<CasioType1MakernoteDirectory> CASIO = CasioType1MakernoteDirectory.class;
  public final static Class<FujifilmMakernoteDirectory> FUJIFILM = FujifilmMakernoteDirectory.class;
  public final static Class<KodakMakernoteDirectory> KODAK = KodakMakernoteDirectory.class;
  public final static Class<KyoceraMakernoteDirectory> KYOCERO = KyoceraMakernoteDirectory.class;
  public final static Class<NikonType1MakernoteDirectory> NIKON_TYPE_1 = NikonType1MakernoteDirectory.class;
  public final static Class<NikonType2MakernoteDirectory> NIKON_TYPE_2 = NikonType2MakernoteDirectory.class;
  public final static Class<OlympusMakernoteDirectory> OLYMPUS = OlympusMakernoteDirectory.class;
  public final static Class<PanasonicMakernoteDirectory> PANASONIC = PanasonicMakernoteDirectory.class;
  public final static Class<PentaxMakernoteDirectory> PENTAX = PentaxMakernoteDirectory.class;
  public final static Class<SonyMakernoteDirectory> SONY = SonyMakernoteDirectory.class;
  
  /**
   * Extracts the metadata from the image file represented by an InputStream
   * 
   * @param imageFile InputStream of an image file.
   * @return Metadata containied in the specified image.
   */
  private Metadata getMetadata(InputStream imageFile){
    Metadata metadata = null;
    
    try {
      metadata = JpegMetadataReader.readMetadata(imageFile);
    } catch (JpegProcessingException e) {
      mLogger.error("Not able to load the meta data of " + imageFile, e);
    }
    return metadata;
  }
  
  /**
   * Get the specified Directory of metainformation from the given
   * Metadata.
   * 
   * @param data Metadata to extract the Directory from.
   * @param dir Class of the directory to extract.
   * @return The specified metadata Directory.
   */
  @SuppressWarnings("unchecked")
  private Directory getDir(Metadata data, Class dir){
    return data.getDirectory(dir);
  }
  
  /**
   * Returns an array of Tag elements representing the metainformation,
   * contained in the specified directory of the given file.
   * 
   * @param imageFile The file to extract the data from.
   * @param directory The desired directory's Class.
   * @return An arry of Tags.
   * @throws MetadataException
   */
  @SuppressWarnings("unchecked")
  public Tag[] getDirectoryTagsAsTagArray(InputStream imageFile, Class directory) throws MetadataException{    
    Metadata metadata = getMetadata(imageFile);
    Directory dir = getDir(metadata, directory);
    
    Iterator<Tag> tags = dir.getTagIterator();
    Tag[] data = new Tag[dir.getTagCount()];
    for(int i = 0; tags.hasNext(); i++) {
          data[i] = (Tag)tags.next();
      }
    
    return data;
  }
    
  /**
   * To get all meta tags possibly contained in an image.
   * 
   * @param imageFile File to extract the Metadata from.
   * @return Hashtable containing the directorys data.
   * @throws MetadataException
   */
  public Hashtable<String, String> getAllTags(InputStream imageFile) throws MetadataException{  
    Metadata data = getMetadata(imageFile);
    Hashtable<String, String> tags = new Hashtable<String, String>();
  
    tags.putAll(getDirsTags(getDir(data, CANON)));
    tags.putAll(getDirsTags(getDir(data, CASIO)));
    tags.putAll(getDirsTags(getDir(data, EXIF)));
    tags.putAll(getDirsTags(getDir(data, FUJIFILM)));
    tags.putAll(getDirsTags(getDir(data, GPS)));
    tags.putAll(getDirsTags(getDir(data, IPTC)));
    tags.putAll(getDirsTags(getDir(data, JPEG)));
    tags.putAll(getDirsTags(getDir(data, JPEG_COMMENT)));
    tags.putAll(getDirsTags(getDir(data, KODAK)));
    tags.putAll(getDirsTags(getDir(data, KYOCERO)));
    tags.putAll(getDirsTags(getDir(data, NIKON_TYPE_1)));
    tags.putAll(getDirsTags(getDir(data, NIKON_TYPE_2)));
    tags.putAll(getDirsTags(getDir(data, OLYMPUS)));
    tags.putAll(getDirsTags(getDir(data, PANASONIC)));
    tags.putAll(getDirsTags(getDir(data, PENTAX)));
    tags.putAll(getDirsTags(getDir(data, SONY)));
    return tags;
  }
  
  /**
   * Saves all tags contained in the specified directory to a Hashtable and 
   * returnes them.
   * 
   * @param dir Directory to extract the tags from.
   * @return Hashtable containing th metatags from the Directory.
   * @throws MetadataException
   */
  @SuppressWarnings("unchecked")
  private Hashtable<String, String> getDirsTags(Directory dir) throws MetadataException{
    Hashtable<String, String> metadata = new Hashtable<String, String>();

    Iterator<Tag> tags = dir.getTagIterator();
    while (tags.hasNext()) {
          Tag tag = (Tag)tags.next();
          metadata.put(tag.getTagName(), tag.getDescription());
      }
    
    return metadata;
  }
}
