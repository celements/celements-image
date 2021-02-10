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
(function(window, undefined) {
  "use strict";

  var loadedMetaTags = new Object();
  var loading = 0;

  var loadMeta = function(imageId) {
    if(!loadedMetaTags[imageId]) {
      loading++;
      var galleryId = imageId.replace(/^.*:(.*?):.*$/g, '$1');
      var image = null;
      window.getLoadedGalleries().get(galleryId).getImages().each(function(galImg) {
        if(galImg.getId() == imageId) {
          image = galImg;
        }
      });
      new Ajax.Request(getCelHost(), {
          method: 'post',
          parameters: {
            'xpage' : 'celements_ajax',
            'ajax_mode' : 'getMetaTagsForImage',
            'imageDoc' : image.getSrc().replace(/^.*\/download\/(.*?)\/(.*?)\/.*$/g, '$1.$2'),
            'imageName' : image.getFilename()
          },
          onComplete: function(transport) {
            loading--;
            if (transport.responseText.isJSON()) {
              loadedMetaTags[imageId] = transport.responseText.evalJSON();
            } else if ((typeof console != 'undefined')
                && (typeof console.warn != 'undefined')) {
              console.warn('getMetaTagsForImage: noJSON!!! ', transport.responseText);
            }
            $(document.body).fire('celimage:imageLoadingDone');
          }
      });
    }
  };
  
  var loadMetaTags = function() {
    var selected = $$('.bild.selected');
    selected.each(function(imgDiv) {
      loadMeta(imgDiv.id);
    });
    if (loading == 0) {
      displayMetaSelection();
    }
  };

  var cpyToClipboardHandler = function(event) {
    window.prompt("Copy to clipboard: Ctrl+C, Enter", '{metatag:'
        + event.element().innerHTML + '}');
  };

  var displayMetaSelection = function() {
    if(loading == 0) {
      var tagContainer = $('metaTags');
      if(tagContainer) {
        tagContainer.update();
        var allTagArray = new Array();
        var allTagContent = new Object();
        $$('.bild.selected').each(function(img) {
          var tags = loadedMetaTags[img.id];
          for(var tagKey in tags) {
            if(allTagArray.indexOf(tagKey) < 0) {
              allTagArray.push(tagKey);
              allTagContent[tagKey] = { 
                  nr: 1, 
                  values: [tags[tagKey]]
              };
            } else {
              allTagContent[tagKey].nr = 1 + allTagContent[tagKey].nr;
              allTagContent[tagKey].values.push(tags[tagKey]);
            }
          }
        });
        allTagArray.sort();
        $(allTagArray).each(function(tag) {
          var tagContent = allTagContent[tag];
          var tagDom = new Element('div', { 'class' : 'tagOcurrences' });
          var tagSpan = new Element('span', { 'class' : 'tag', 'title' : JSON.stringify(tagContent.values) }).insert(tag);
          tagDom.insert(tagSpan);
          tagDom.insert(new Element('span', { 'class' : 'ocurrences' }).insert('(' + tagContent.nr + ')'));
          tagContainer.insert(tagDom);
          tagSpan.observe('click', cpyToClipboardHandler);
        });
        tagContainer.up().show();
      }
    }
  };

  Event.observe(window, 'load', function() {
    $(document.body).observe('celimage:imageSelectionChanged', loadMetaTags);
    $(document.body).observe('celimage:imageLoadingDone', displayMetaSelection);
  });
  
})(window);
