  var loadedMetaTags = new Object();

  Event.observe(window, 'load', function() {
    $(document.body).observe('celimage:imageSelectionChanged', displayMetaSelection);
  });
  
  var loadMeta = function(imageId) {
    if(!loadedMetaTags[imageId]) {
      var galleryId = imageId.replace(/^.*:(.*?):.*$/g, '$1');
      var image = null;
      loadedGalleries.get(galleryId).getImages().each(function(galImg) {
        if(galImg.id == imageId) {
          image = galImg;
        }
      });
      new Ajax.Request(getCelHost(), {
          method: 'post',
          parameters: {
            'xpage' : 'celements_ajax',
            'ajax_mode' : 'getMetaTagsForImage',
            'imageDoc' : image.src.replace(/^\/download\/(.*?)\/(.*?)\/.*$/g, '$1.$2')
          },
          onComplete: function(transport) {
            if (transport.responseText.isJSON()) {
              loadedMetaTags[imageId] = transport.responseText.evalJSON();
            } else if ((typeof console != 'undefined')
                && (typeof console.warn != 'undefined')) {
              console.warn('getMetaTagsForImage: noJSON!!! ', transport.responseText);
            }
            displayMetaSelection();
          }
      });
    } else {
      displayMetaSelection();
    }
  };
  
  var displayMetaSelection = function() {
    var selected = $$('.bild.selected');
    selected.each(function(imgDiv) {
      loadMeta(imgDiv.id);
    });
    var tagContainer = $('metaTags');
    if(tagContainer) {
      console.log('meta tags:', loadedMetaTags);
      
      tagContainer.up().show();
    }
  };