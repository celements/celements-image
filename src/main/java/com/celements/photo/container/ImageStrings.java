package com.celements.photo.container;

/**
 * Container used to simplify the velocity access. Contains the name of the 
 * image, its URL and the URL to its thumbnail.
 */
public class ImageStrings {
  private String id;
  private String name;
  private String url;
  private String thumb;
  
  @SuppressWarnings("unused")
  private ImageStrings(){}
  
  /**
   * Initialises image's id, name, URL and thumbnail URL.
   * 
   * @param id Id of the image. 
   * @param name Name of the image.
   * @param url URL to the image.
   * @param thumb URL to the thumbnail of the image.
   */
  public ImageStrings(String id, String name, String url, String thumb){
    this.id = id;
    this.name = name;
    this.url = url;
    this.thumb = thumb;
  }
  
  /**
   * Get the id of the image. The id is a unique identifier String with a
   * length of 64 character.
   * 
   * @return id of the image.
   */
  public String getId(){
    return id;
  }

  /**
   * Get the name of the image.
   * 
   * @return Name of the image.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the URL to the thumbnail.
   * 
   * @return URL to the thumbnail.
   */
  public String getThumb() {
    return thumb;
  }

  /**
   * Get the URL to the image.
   * 
   * @return URL to the image.
   */
  public String getUrl() {
    return url;
  }

  public void setThumb(String thumb) {
    this.thumb = thumb;
  }
}
