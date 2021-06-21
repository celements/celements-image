package com.celements.photo.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.BarcodeException;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.BarcodeUtil;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

@Component("barcode")
public class BarcodeScriptService implements ScriptService {

  private String MODULE_WIDTH = "0.33mm";
  private String MODULE_HEIGHT = "15mm";

  private static final Logger LOGGER = LoggerFactory.getLogger(BarcodeScriptService.class);

  @Requirement
  Execution execution;

  public int getEAN8Checksum(int number) {
    String nrStr = Integer.toString(number);
    while (nrStr.length() < 7) {
      nrStr = "0" + nrStr;
    }
    if (nrStr.length() > 7) {
      nrStr = nrStr.substring(0, 7);
    }
    byte[] nr = nrStr.getBytes();
    int s1 = nr[1] + nr[3] + nr[5];
    int s2 = 3 * (nr[0] + nr[2] + nr[4] + nr[6]);
    return (10 - ((s1 + s2) % 10)) % 10;
  }

  /**
   * Generates an image representation of a barcode
   *
   * @param number
   *          complete number including check sum
   * @param out
   *          image representation of the barcode
   */
  public void generate(String number, OutputStream out) {
    String moduleWidth = getContext().getRequest().get("moduleWidth");
    if ((moduleWidth == null) || "".equals(moduleWidth.trim())) {
      moduleWidth = MODULE_WIDTH;
    }
    String moduleHeight = getContext().getRequest().get("moduleHeight");
    if ((moduleHeight == null) || "".equals(moduleHeight.trim())) {
      moduleHeight = MODULE_HEIGHT;
    }
    generate(number, moduleWidth, moduleHeight, 150, Optional.<BarcodeDimension>absent(), out);
  }

  /**
   * Generates an image representation of a barcode
   *
   * @param number
   *          complete number including check sum
   * @param moduleWidth
   *          module height in the format '15mm'
   * @param moduleHeight
   *          module width in the format '0.3mm'
   * @param resolution
   *          resolution of the output image (DPI)
   * @param dim
   *          (optional) dimensions of the output
   * @param out
   *          image representation of the barcode
   */
  public void generate(String number, String moduleWidth, String moduleHeight, int resolution,
      Optional<BarcodeDimension> dim, OutputStream out) {
    BitmapCanvasProvider provider = new BitmapCanvasProvider(out, "image/png", resolution,
        BufferedImage.TYPE_BYTE_GRAY, true, 0);
    if (dim.isPresent()) {
      provider.establishDimensions(dim.get());
    }
    DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
    String xmlConf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><barcode><ean8>" + "<height>"
        + moduleHeight + "</height>" + "<module-width>" + moduleWidth + "</module-width>"
        + "</ean8></barcode>";
    InputStream in = new ByteArrayInputStream(xmlConf.getBytes());
    try {
      Configuration cfg = builder.build(in);
      in.close();
      BarcodeGenerator gen = BarcodeUtil.getInstance().createBarcodeGenerator(cfg);
      gen.generateBarcode(provider, number);
      provider.finish();
    } catch (ConfigurationException e) {
      LOGGER.error("Exception in configuration for barcode module.", e);
    } catch (SAXException e) {
      LOGGER.error("generate", e);
    } catch (IOException e) {
      LOGGER.error("generate", e);
    } catch (BarcodeException e) {
      LOGGER.error("Exception in barcode module.", e);
    }
  }

  private XWikiContext getContext() {
    return (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }
}
