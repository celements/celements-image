package com.celements.photo.utilities;

public class Util {
  private static Util util = null;
  
  private Util(){}
  
  public static Util getUtil(){
    if(util == null){
      util = new Util();
    }
    
    return util;
  }
  
  public String hashToHex(String hash) {
    String hexHash = "";
    for(int i = 0; i < hash.length(); i++){
      String hex = Integer.toHexString((int)hash.charAt(i));
      if(hex.length() == 1){
        hex = "0" + hex;
      }
      hexHash = hexHash + hex;
    }
    return hexHash;
  }
}
