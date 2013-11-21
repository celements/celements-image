  var loadedMetaTags = new Object();
  var loading = 0;

  Event.observe(window, 'load', function() {
    $(document.body).observe('celimage:imageSelectionChanged', loadMetaTags);
    $(document.body).observe('celimage:imageLoadingDone', displayMetaSelection);
  });
  
  var loadMeta = function(imageId) {
    if(!loadedMetaTags[imageId]) {
      loading++;
      var galleryId = imageId.replace(/^.*:(.*?):.*$/g, '$1');
      var image = null;
      loadedGalleries.get(galleryId).getImages().each(function(galImg) {
        if(galImg.getId() == imageId) {
          image = galImg;
        }
      });
      new Ajax.Request(getCelHost(), {
          method: 'post',
          parameters: {
            'xpage' : 'celements_ajax',
            'ajax_mode' : 'getMetaTagsForImage',
            'imageDoc' : image.getSrc().replace(/^\/download\/(.*?)\/(.*?)\/.*$/g, '$1.$2')
          },
          onComplete: function(transport) {
            if (transport.responseText.isJSON()) {
              loadedMetaTags[imageId] = transport.responseText.evalJSON();
            } else if ((typeof console != 'undefined')
                && (typeof console.warn != 'undefined')) {
              console.warn('getMetaTagsForImage: noJSON!!! ', transport.responseText);
            }
            loading--;
            $(document.body).fire('celimage:imageLoadingDone');
          }
      });
    }
  };
  
  var loadMetaTags = function() {
    if(loading == 0) {
      var selected = $$('.bild.selected');
      selected.each(function(imgDiv) {
        loadMeta(imgDiv.id);
      });
      displayMetaSelection();
    }
  }
  
  var displayMetaSelection = function() {
    if(loading == 0) {
      var tagContainer = $('metaTags');
      if(tagContainer) {
        tagContainer.update();
        var allTagArray = new Array();
        var allTagContent = new Object();
        for(key in loadedMetaTags) {
          var tags = loadedMetaTags[key];
          for(tagKey in tags) {
            if(allTagArray.indexOf(tagKey) < 0) {
              allTagArray.push(tagKey);
              allTagContent[tagKey] = { 
                  nr: 1, 
                  values: [tags[tagKey]]
              };
            } else {
              allTagContent[tagKey] = { 
                  nr: (allTagContent[tagKey].nr + 1), 
                  values: allTagContent[tagKey].values.push(tags[tagKey])
              };
            }
          }
        }
        allTagArray.sort();
        $(allTagArray).each(function(tag) {
          var tagContent = allTagContent[tag];
          var tagDom = new Element('div', { 'class' : 'tagOcurrences' });
          tagDom.insert(new Element('span', { 'class' : 'tag', 'title' : JSON.stringify(tagContent.values) }).insert(tag));
          tagDom.insert(new Element('span', { 'class' : 'ocurrences' }).insert('(' + tagContent.nr + ')'));
          tagContainer.insert(tagDom);
        });
        tagContainer.up().show();
      }
    }
  };