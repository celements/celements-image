package com.celements.photo.container;

/**
 * Container used to simplify the velocity access. Represents a metatag, 
 * containing the tag's name and description.
 */
public class Metadate {
  private String name;
  private String description;
  private boolean empty;
  
  /**
   * Initialises an empty metatag.
   */
  public Metadate(){
    name = "";
    description = "";
    empty = true;
  }
  
  /**
   * Set the name and description of the metatag.
   * 
   * @param name Name of the tag.
   * @param description Description (or value) of the tag.
   */
  public Metadate(String name, String description) {
    this.name = name;
    this.description = description;
    empty = false;
  }
  
  /**
   * Get the description of the metatag.
   * 
   * @return The description of the metatag.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the name of the metatag.
   * 
   * @return The name of the metatag.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Check wether the Metadate is empty (used if the user asks for a non 
   * existing metatag).
   * 
   * @return true if this is an empty Metadate.
   */
  public boolean isEmpty() {
    return empty;
  }
}
