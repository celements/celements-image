package com.celements.photo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.krysalis.barcode4j.BarcodeException;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

@Component("barcode")
public class BarcodeScriptService implements ScriptService {
  private String MODULE_WIDTH = "0.33mm";
  private String MODULE_HEIGHT = "15mm";
  private String HUMAN_READABLE_FONT = "Helvetica";
  private String HUMAN_READABLE_SIZE = "8pt";
  
  @Requirement
  Execution execution;
  
  public void generate(String number, OutputStream out) {
    String moduleHeight = getContext().getRequest().get("moduleHeight");
    if((moduleHeight == null) || "".equals(moduleHeight.trim())) {
      moduleHeight = MODULE_HEIGHT;
    }
    String moduleWidth = getContext().getRequest().get("moduleWidth");
    if((moduleWidth == null) || "".equals(moduleWidth.trim())) {
      moduleWidth = MODULE_WIDTH;
    }
    String font = getContext().getRequest().get("font");
    if((font == null) || "".equals(font.trim())) {
      font = HUMAN_READABLE_FONT;
    }
    String size = getContext().getRequest().get("size");
    if((size == null) || "".equals(size.trim())) {
      size = HUMAN_READABLE_SIZE;
    }
    BitmapCanvasProvider provider = new BitmapCanvasProvider(
        out, "image/png", 300, BufferedImage.TYPE_BYTE_GRAY, true, 0);
    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
    String xmlConf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><barcode><ean8>" +
      "<height>" + moduleHeight + "</height>" +
      "<module-width>" + moduleWidth + "</module-width>" +
      "<human-readable>" +
      "<font-name>" + font + "</font-name>" +
      "<font-size>" + size + "</font-size>" +
      "</human-readable></ean8></barcode>";
    InputStream in = new ByteArrayInputStream(xmlConf.getBytes());
    try {
      Configuration cfg = builder.build(in);
      in.close();
      BarcodeGenerator gen = BarcodeUtil.getInstance().createBarcodeGenerator(cfg);
      gen.generateBarcode(provider, number);
      provider.finish();
    } catch (ConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BarcodeException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }
}
