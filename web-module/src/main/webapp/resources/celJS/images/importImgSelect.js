(function(window, undefined) {
  "use strict";

  var activeGalleries = new Array();
  var loadedGalleries = new Hash();
  var finishedLoadingGalleries = new Array();
  var mouseOverEleId = "";
  var zoomed = -1;
  var zoomedEle = null;
  var activePos = 0;
  var hovering = false;
  var onThumb = false;
  var defaultImgName = '';
  var defaultImgInfo = '';
  var isMotivePicker = false;

  window.getLoadedGalleries = function() {
    return loadedGalleries;
  };

  var observeImgs = function(event) {
    var loaderimg = new Element('img', { 'src': '/skin/resources/celRes/ajax-loader.gif' });
    $$('.bilder').each(function(imgContainer) {
      imgContainer.update(loaderimg);
    });
    isMotivePicker = (typeof($('productConfigForm')) != 'undefined') && ($('productConfigForm') != null);
    document.observe('keydown', navigateImages);
    $$('.resetSelection').each(function(ele) {
      ele.observe('click', resetSelection);
    });
    $$('.motiveSubmit').each(function(ele) {
      ele.observe('click', pickMotive);
    });
    $(document.body).observe('celimage:finishLoadingAllGalleries', function(event) {
      showHideGallery();
    });
  //  var aGal = undefined;
    
    var fileBaseLink = $('filebaseLink').value;
    var allActiveGalleries = [fileBaseLink];
    var loadingCallbackFN = function(theGallery) {
      finishedLoadingGalleries.push(theGallery._collDocRef);
      if (finishedLoadingGalleries.size() >= allActiveGalleries.size()) {
        $(document.body).fire('celimage:finishLoadingAllGalleries');
      }
    };
  
    loadedGalleries.set(fileBaseLink, new CELEMENTS.images.Gallery(
        fileBaseLink, loadingCallbackFN, false,
        (fileBaseLink.split('.').size() < 2), 'NameAsc'));
    addToGalleriesList(fileBaseLink);
  };
  
  var mouseIsOver = function(event) {
    onThumb = true;
    var ele = event.element();
    mouseOverEleId = ele.id;
    hoverImg.delay(1.5, ele.id);
    activePos = getImgNrInList(mouseOverEleId);
  };
  
  var mouseIsOutDelayed = function(event) {
    onThumb = false;
    mouseIsOut.delay(0.005, event);
  };
  
  var mouseIsOut = function(event) {
    if(!hovering && !onThumb) {
      $$('.zoomedImg').each(function(ele) {
        zoomed = -1;
        showZoomedElement(false);
  //      ele.remove();
      });
      mouseOverEleId = "";
      setImgName(defaultImgName, false);
      setImgInfo(defaultImgInfo, false);
    }
  };
  
  var hoverImg = function(id, forceZoom) {
    if((mouseOverEleId == id) || forceZoom) {
      var gal = getGalObjForImgId(id);
      var img = gal.getImageForId(id);
      var url = img.getURL() + ((img.getURL().indexOf('?') < 0)?'?':'&') 
          + 'celwidth=254&celheight=251';
      url += '&background=00000022';
      setZoomedElement(url, id, true);
      zoomed = getImgNrInList(id);
      setImgName(img.getFilename(), false);
      setImgInfo(gal.getDesc(), false);
    }
  };
  
  var setZoomedElement = function(url, eleId, show) {
    if(zoomedEle == null) {
      zoomedEle = new Element('div', {
        'id' : 'zoomedImg',
        'class' : 'zoomedImg'
      });
     zoomedEle.setStyle({
          'border' : '2px solid #C00',
          'position' : 'absolute', 
          'lineHeight' : '0px',
          'fontSize' : '0px',
          'backgroundColor' : '#FFF',
          'backgroundPosition' : 'center',
          'backgroundRepeat' : 'no-repeat'
      });
      zoomedEle.observe('mouseover', function(event) { hovering = true; event.stop(); });
      zoomedEle.observe('mouseout', function(event) { 
        hovering = false;
        mouseIsOut.delay(0.005, event);
        event.stop();
      });
      zoomedEle.observe('click', function(event) {
        hovering = false;
        onThumb = false;
        mouseIsOut(event);
        var thumbId = event.element().id.replace(/_zoom/, '');
        selectImgEle($(thumbId));
        event.stop();
      });
      $$('.bilder').each(function(imgContainer) { imgContainer.insert(zoomedEle); });
    }
    if(zoomedEle.getStyle('background-image').indexOf(url) < 0) {
      zoomedEle.setStyle({ 'backgroundImage' : 'url(' + url + ')' });
      zoomedEle.clonePosition($(eleId));
      zoomedEle.id = eleId + "_zoom";
      showZoomedElement(true);
      morphToPos(zoomedEle);
    }
    return zoomedEle;
  };

  var showZoomedElement = function(show) {
    if(show) {
      zoomedEle.show();
    } else {
      zoomedEle.hide();
    }
  };
  
  var getImgNrInList = function(id) {
    var nr = -1;
    var i = 0;
    $$('.bild').each(function(img) {
      if(img.id == id) {
        nr = i;
      }
      i++;
    });
    return nr;
  };
  
  var getImgWithNr = function(nr) {
    return $$('.bild')[nr];
  };
  
  var morphToPos = function(zoomedEle) {
    var morphStyles = "margin-left : 40px; margin-top : -60px; width : 254px; height : 251px;";
    new Effect.Morph(zoomedEle, { style : morphStyles, duration : 0.5 });
  };
  
  var selectImg = function(event) {
    selectImgEle(event.element());
    event.stop();
  };
  
  var selectImgEle = function(selectElement) {
    if(isProductConfig(selectElement)) {
      // once selected it should not be possible to unselect without selecting a new one at 
      // the same time. Thus After selecting a first image it should not be possible to 
      //  select no image at all.
      $$('.bild').each(function(ele) {
        ele.removeClassName('selected');
      });
      selectElement.toggleClassName('selected');
    } else {
      selectElement.toggleClassName('selected');
    }
    var nrActive = $$('.bild.selected').length;
    if(nrActive == 1) {
  //    $$('.highResButton, .downloadButton, .motiveSubmit').each(function(button) {
  //      button.removeClassName('dark');
  //      button.addClassName('dark');
  //    });
      var gal = getGalObjForImgId($$('.bild.selected')[0].id);
      var img = gal.getImageForId($$('.bild.selected')[0].id);
      setImgName(img.getFilename(), true);
      setImgInfo(gal.getDesc(), true);
    } else if(nrActive > 1) {
  //    $$('.highResButton, .downloadButton').each(function(button) {
  //      button.removeClassName('dark');
  //      button.addClassName('dark');
  //    });
  //    $$('.motiveSubmit').each(function(button) {
  //      button.removeClassName('dark');
  //    });
      setImgName('-', true);
      var multiInfo = '';
      if($('galleryMultiSelectMessage')) {
        multiInfo = $('galleryMultiSelectMessage').clone(true);
        multiInfo.down('.imgsSelected').update($$('.bild.selected').length);
        multiInfo.show();
      }
      setImgInfo(multiInfo, true);
    } else {
  //    $$('.highResButton, .downloadButton, .motiveSubmit').each(function(button) {
  //      button.removeClassName('dark');
  //    });
      setImgName('', true);
      setImgInfo('', true);
    }
    $(document.body).fire('celimage:imageSelectionChanged');
  };
  
  var setImgName = function(imgName, setDefault) {
    $$('.infoName').each(function(ele) {
      ele.update(imgName);
    });
    if(setDefault) {
      defaultImgName = imgName;
    }
  };
  
  var setImgInfo = function(imgInfo, setDefault) {
    $$('.infoInfo').each(function(ele) {
      ele.update(imgInfo);
    });
    if(setDefault) {
      defaultImgInfo = imgInfo;
    }
  };
  
  var linkMIClicked = function(event) {
    var chkBox = event.element().next('input');
    chkBox.checked = !chkBox.checked;
    handleMIChange(chkBox);
    event.stop();
  };
  
  var galleryMIClicked = function(event) {
    handleMIChange(event.element());
  };
  
  var handleMIChange = function(chkBox) {
    var boxes = $$('.galMenuItem');
    if(boxes.size() > 0) {
      var checkedCount = 0;
      boxes.each(function(boxEle) {
        if(((boxEle != chkBox) && boxEle.checked) || ((boxEle == chkBox) && !boxEle.checked)) {
          checkedCount++;
        }
      });
      if(boxes.size() == checkedCount) {
        activeGalleries = new Array();
        boxes.each(function(boxEle) {
          boxEle.checked = !boxEle.checked;
        });
      }
    }
    var activeString = ',' + activeGalleries.toString() + ',';
    var idx = activeString.indexOf(',' + chkBox.value + ',');
    if(chkBox.checked) {
      if(idx < 0) {
        activeGalleries = new Array();
        $$('li.cel_nav_hasChildren.active').each(function(activeGalTree) {
          if(activeGalTree.hasClassName('Gallery')) {
            addToGalleriesList(activeGalTree.down('input').value);
          }
          var newActiveGalleries = activeGalTree.select('li.Gallery.cel_nav_isLeaf input');
          newActiveGalleries.each(function(galEle) {
            if(galEle.checked) {
              addToGalleriesList(galEle.value);
            }
          });
        });
        showHideGallery();
      }
    } else {
      if(idx >= 0) {
        activeGalleries = activeGalleries.without(chkBox.value);
        showHideGallery();
      }
    }
  };
  
  var addToGalleriesList = function(pushValue) {
    if($A(activeGalleries).indexOf(pushValue) < 0) {
      activeGalleries.push(pushValue);
    }
  };
  
  var showHideGallery = function() {
    var selectedList = new Array();
    $$('.bilder').each(function(imgContainer) {
      imgContainer.select('.selected').each(function(selElem) {
        selectedList.push(selElem.id);
      });
      imgContainer.update('');
    });
    activeGalleries.each(function(galleryFullName) {
      loadedGalleries.get(galleryFullName).getImages().each(function(image) {
        $$('.bilder').each(function(imgContainer) {
          var thumbURL = image.getThumbURL();
          var defaultDim = 75;
          if(!image.getThumbDimension().width && !image.getThumbDimension().height) {
              thumbURL = thumbURL.replace(/^(.*celwidth=)($|(&.*)$)/g, '$1' + defaultDim + '$2');
              thumbURL = thumbURL.replace(/^(.*celheight=)($|(&.*)$)/g, '$1' + defaultDim + '$2');
          }
          thumbURL += '&background=00000022';
          var img = new Element('div', {
            'id' : image.getId()
          }).addClassName('bild'
              ).setStyle({
            'backgroundImage' : 'url(' + thumbURL + ')'
          });
          if(selectedList.indexOf(image.getId()) >= 0) {
            img.addClassName('selected');
          }
          imgContainer.insert(img);
        });
      });
    });
    $$('.bilder').each(function(imgContainer) {
      imgContainer.insert(new Element('div', { 'style' : 'clear:left;' }));
    });
    $$('.bild').each(function(ele) {
      ele.observe('click', selectImg);
      ele.observe('mouseover', mouseIsOver);
      ele.observe('mouseout', mouseIsOutDelayed);
    });
    activePos = 0;
  };
  
  var navigateImages = function(event) {
    switch (event.keyCode) {
      case Event.KEY_LEFT:
          event.stop();
          navigateImage(-1, event);
          break;
      case Event.KEY_UP:
          event.stop();
          navigateImage(-5, event);
          break;
      case Event.KEY_RIGHT:
          event.stop();
          navigateImage(1, event);
          break;
      case Event.KEY_DOWN:
          event.stop();
          navigateImage(5, event);
          break;
    }
  };
  
  var navigateImage = function(change, event) {
    if((zoomed >= 0) && (zoomed+change >= 0) && (zoomed+change < $$('.bild').length)) {
  //    var elem = getImgWithNr(activePos);
      zoomed += change;
      activePos = zoomed;
      var newImg = getImgWithNr(zoomed);
      hovering = false;
      onThumb = false;
      mouseIsOut(event);
        hoverImg(newImg.id, true);
    }
  };
  
  var getGalObjForImgId = function(id) {
    var retval = undefined;
    loadedGalleries.values().each(function(gal) {
      var image = gal.getImageForId(id);
      if(image && (typeof(image) !== 'undefined')) {
        retval = gal;
      }
    });
    return retval;
  };
  
  var getImgObjForId = function(id) {
    var retval = undefined;
    loadedGalleries.values().each(function(gal) {
      var img = gal.getImageForId(id);
      if(typeof(img) != 'undefined') {
        retval = img;
      }
    });
    return retval;
  };
  
  var pickMotive = function(event) {
    event.stop();
    $$('.motiveSubmit').each(function(ele) {
      ele.hide();
      var loader = new Element('img', { 'src': '/skin/resources/celRes/ajax-loader.gif', 
          'class': 'importLoader'});
      ele.insert({ after : loader });
    });
    var url = null;
    $$('.galleries').each(function(ele) {
      if(ele.checked || (ele.type == 'hidden')) {
        url = ele.value;
        throw $break;
      }
    });
    if(url) {
      var imageFNs = new Array();
      $$('.bild.selected').each(function(bild) {
        var galleryId = bild.id.replace(/^.*:(.*?):.*$/g, '$1');
        console.log('galleryId: ', galleryId);
        var gallery = loadedGalleries.get(galleryId);
        var image = gallery.getImageForId(bild.id);
        var imageDocFN = image.getURL().replace(/^\/download\/(.*?)\/(.*?)\/.*$/g, '$1.$2');
        imageFNs.push(imageDocFN + ';' + image.getFilename());
      });
      var slideContent = $('slideContent').value.strip();
      if(imageFNs.length > 0) {
        var params = {
            'xpage' : 'celements_ajax',
            'ajax_mode' : 'generateImageSlides',
            'slideContent' : slideContent,
            'imageFN' : imageFNs
        };
        new Ajax.Request(url, {
          method : "POST",
          parameters: params,
          onComplete : function(transport) {
            if (transport.responseText.isJSON()) {
              var responseObject = transport.responseText.evalJSON();
              if(responseObject.successful) {
                alert('Slide(s) erfolgreich erstellt.');
              } else {
                alert('Fehlgeschlagen: "' + responseObject.errMsg + '"');
              }
            } else if ((typeof console != 'undefined')
                && (typeof console.error != 'undefined')) {
              console.error('noJSON!!! ', transport.responseText);
            }

            $$('.motiveSubmit').each(function(ele) {
              ele.next('.importLoader').remove();
              ele.show();
            });
          }
        });
      } else {
        alert('Bild(er) auswaehlen.');
      }
    } else {
      alert('Galerie auswaehlen.');
    }
  };
  
  var resetSelection = function(event) {
    $$('.bild.selected').each(function(ele) {
      ele.removeClassName('selected');
    });
    $$('.highResButton, .downloadButton').each(function(button) {
      button.removeClassName('dark');
    });
    setImgName('', true);
    setImgInfo('', true);
    $(document.body).fire('celimage:imageSelectionChanged');
    event.stop();
  };
  
  var isProductConfig = function(ele) {
    return typeof(ele.up('div.shop')) != 'undefined';
  };
  
  Event.observe(window, 'load', observeImgs);

})(window);
