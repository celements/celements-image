/*
*
* The celements javascript gallery framework
**/
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.images=="undefined"){CELEMENTS.images={};};

(function(window, undefined) {
  "use strict";

CELEMENTS.images.Gallery = function(galleryDocRef, callbackFN, onlyFirstNumImages,
    spaceImgs, sortImages) {
  // constructor
  this._init(galleryDocRef, callbackFN, onlyFirstNumImages, spaceImgs, sortImages);
};

var CiG = CELEMENTS.images.Gallery;

CiG.prototype = {
    _collDocRef : undefined,
    _galleryData : undefined,
    _imagesArray : undefined,
    _imagesHash : undefined,
    _afterLoadListener : undefined,
    _loading : undefined,
    _loadingSlice : undefined,
    _loaded : undefined,
    _sortImages : undefined,

  _init : function(collDocRef, callbackFN, onlyFirstNumImages, spaceImgs, sortImages) {
    var _me = this;
    _me._collDocRef = collDocRef;
    _me._afterLoadListener = new Array();
    _me._imagesHash = new Hash();
    _me._loading = false;
    _me._loaded = false;
    _me._loadingSlice = false;
    _me._sortImages = sortImages || '';
    _me.executeAfterLoad(callbackFN, onlyFirstNumImages, spaceImgs);
  },

  executeAfterLoad : function(callbackFN, onlyFirstNumImages, spaceImgs) {
    var _me = this;
    if (_me.loaded) {
      callbackFN(_me);
    } else {
      _me._afterLoadListener.push(callbackFN);
      if (!_me._loading) {
        _me._loading = true;
        _me._loadData(onlyFirstNumImages, spaceImgs);
      }
    }
  },

  getDocRef : function() {
    var _me = this;
    return _me._collDocRef;
  },

  _getGalleryURL : function(spaceImgs) {
    var _me = this;
    var port = '';
    if (window.location.port != '80') {
      port = window.location.port;
    }
    if (!spaceImgs) {
      //1.12.2013 FP; image picker does not use spaceImgs but calls galleryClass with callDocRef equals gallery FN
      var colDocRefSplit = _me._collDocRef.split('.');
      return window.location.protocol + '//' + window.location.host + '/'
        + colDocRefSplit[0] + '/' + colDocRefSplit[1];
    } else {
      return window.location.protocol + '//' + window.location.host + '/'
        + _me._collDocRef + '/WebHome';
    }
  },

  _loadData : function(onlyFirstNumImages, spaceImgs) {
    var _me = this;
    _me._loading = true;
    var params = {
        'xpage' : 'celements_ajax',
        'ajax_mode' : 'GalleryData',
        'sortImages' : _me._sortImages
      };
    if(onlyFirstNumImages) {
      params['onlyFirstNumImages'] = onlyFirstNumImages;
    }
    if(spaceImgs) {
      params['spaceImgs'] = _me._collDocRef;
    }
    new Ajax.Request(_me._getGalleryURL(spaceImgs), {
      method : "POST",
      parameters: params,
      onSuccess : function(transport) {
        if (transport.responseText.isJSON()) {
          var responseObject = transport.responseText.evalJSON();
          _me._galleryData = responseObject;
          _me._imagesArray = new Array();
          _me._galleryData.imageArray.each(function(imageObj) {
            var imageId = 'GI:' + _me._collDocRef + ':' + imageObj.filename;
            var image = new CELEMENTS.images.Image(imageObj, imageId);
            image.setThumbDimension(_me._galleryData.thumbWidth,
                _me._galleryData.thumbHeight);
            var index = _me._imagesArray.push(image);
            _me._imagesHash.set(imageId, index - 1);
          });
          _me._loaded = true;
          _me._afterLoadListener.each(function(callbackFN) {
            if (callbackFN) {
              try {
                callbackFN(_me);
              } catch (exp){
                if ((typeof console != 'undefined')
                    && (typeof console.error != 'undefined')) {
                  console.error('failed to execute afterLoadListener: ', callbackFN, exp);
                }
              }
            }
          });
          _me._afterLoadListener = new Array();
          _me._loading = false;
        } else if ((typeof console != 'undefined')
            && (typeof console.error != 'undefined')) {
          console.error('noJSON!!! ', transport.responseText);
        }
      }
    });
  },

  _loadImagesSlice : function(offsetNumImages, getNumImages, callbackFN) {
    var _me = this;
    _me._loadingSlice = true;
    var params = {
        'xpage' : 'celements_ajax',
        'ajax_mode' : 'GalleryData'
      };
    if(offsetNumImages) {
      params['offsetNumImages'] = offsetNumImages;
    }
    if(getNumImages) {
      params['getNumImages'] = getNumImages;
    }
    new Ajax.Request(_me._getGalleryURL(''), {
      method : "POST",
      parameters: params,
      onSuccess : function(transport) {
        if (transport.responseText.isJSON()) {
          var responseObject = transport.responseText.evalJSON();
          _me._galleryData = responseObject;
          var newArraySize = offsetNumImages + getNumImages - 1;
          if (newArraySize < _me._imagesArray.size()) {
            newArraySize = _me._imagesArray.size();
          }
          var newImagesArray = new Array(newArraySize);
          newImagesArray.splice(0, _me._imagesArray.size(), _me._imagesArray);
          _me._imagesArray = newImagesArray;
          var indexPreAdd = offsetNumImages - 1;
          _me._galleryData.imageArray.each(function(imageObj, loadImageIdx) {
            var index = indexPreAdd + loadImageIdx;
            var imageId = 'GI:' + _me._collDocRef + ':' + imageObj.filename;
            var image = new CELEMENTS.images.Image(imageObj, imageId);
            image.setThumbDimension(_me._galleryData.thumbWidth,
                _me._galleryData.thumbHeight);
            _me._imagesArray[index] = image;
            _me._imagesHash.set(imageId, index);
          });
          _me._loaded = true;
          if (callbackFN) {
            try {
              callbackFN(_me);
            } catch (exp){
              if ((typeof console != 'undefined')
                  && (typeof console.error != 'undefined')) {
                console.error('failed to execute _loadImagesSlice callbackFN: ',
                    callbackFN, exp);
              }
            }
          }
          _me._loadingSlice = false;
        } else if ((typeof console != 'undefined')
            && (typeof console.error != 'undefined')) {
          console.error('noJSON!!! ', transport.responseText);
        }
      }
    });
  },

  getImageForNum : function(imageNumber, callbackFN) {
    var _me = this;
    if (_me._imagesArray[imageNumber]) {
      callbackFN(_me._imagesArray[imageNumber]);
    } else {
      _me._loadImagesSlice(imageNumber + 1, 1, function() {
        callbackFN(_me._imagesArray[imageNumber]);
      });
    }
  },

  getImageForId : function(imageId) {
    var _me = this;
    if (typeof _me._imagesHash.get(imageId) != 'undefined') {
      return _me._imagesArray[_me._imagesHash.get(imageId)];
    }
    return undefined;
  },

  getImages : function() {
    var _me = this;
    if (_me._imagesArray) {
      return _me._imagesArray;
    }
    return undefined;
  },

  getNumImages : function() {
    var _me = this;
    if (_me._galleryData) {
      return _me._galleryData.numberOfImages;
    }
    return undefined;
  },

  getTitle : function() {
    var _me = this;
    if (_me._galleryData) {
      return _me._galleryData.title;
    }
    return undefined;
  },

  getDesc : function() {
    var _me = this;
    if (_me._galleryData) {
      return _me._galleryData.desc;
    }
    return undefined;
  },

  hasOverview : function() {
    var _me = this;
    if (_me._galleryData) {
      return _me._galleryData.hasOverview;
    }
    return undefined;
  },

  getTheme : function() {
    var _me = this;
    if (_me._galleryData) {
      return _me._galleryData.theme;
    }
    return undefined;
  },

  getThumbDimension : function() {
    var _me = this;
    if (_me._galleryData) {
      return {
        'width' : _me._galleryData.thumbWidth,
        'height' : _me._galleryData.thumbHeight
      };
    }
    return undefined;
  },

  getDimension : function() {
    var _me = this;
    if (_me._galleryData) {
      return {
        'width' : _me._galleryData.width,
        'height' : _me._galleryData.height
      };
    }
    return undefined;
  },

  isNewImageGallery : function() {
    var _me = this;
    if (_me._galleryData) {
      return _me._galleryData.isNewImageGallery;
    }
    return undefined;
  },

  getSpaceName : function() {
    var _me = this;
    if (_me._galleryData && _me.isNewImageGallery()) {
      return _me._galleryData.spaceName;
    }
    return undefined;
  },

  getLayoutName : function() {
    var _me = this;
    if (_me._galleryData && _me.isNewImageGallery()) {
      return _me._galleryData.galleryLayout;
    }
    return undefined;
  }

};

})();

(function() {

CELEMENTS.images.Image = function(imageData, id) {
  // constructor
  this._init(imageData, id);
};

var CiI = CELEMENTS.images.Image;

CiI.prototype = {
    _imageData : undefined,
    _thumbDim : undefined,
    _id : undefined,

  _init : function(imageData, id) {
    var _me = this;
    _me._imageData = imageData;
    _me._id = id;
  },

  getId : function() {
    var _me = this;
    return _me._id;
  },

  getThumbURL : function() {
    var _me = this;
    if (_me.getThumbDimension()) {
      return _me._getDimURL(_me.getThumbDimension());
    } else {
      return _me._getSrcWithVersion();
    }
  },

  getURL : function(maxWidth, maxHeight) {
    var _me = this;
    if (maxWidth || maxHeight) {
      return _me._getDimURL({ 'height' : maxHeight , 'width' : maxWidth });
    } else {
      return _me._getSrcWithVersion();
    }
  },

  _getDimURL : function(dimObj) {
    var _me = this;
    var height = dimObj.height || '';
    var width = dimObj.width || '';
    return (_me._getSrcWithVersion() + "&celwidth=" + width + "&celheight=" + height);
  },

  _getSrcWithVersion : function() {
    var _me = this;
    return _me._imageData.src + '?vers=' + _me.getVersion();
  },

  getSrc : function() {
    var _me = this;
    return _me._imageData.src;
  },

  setThumbDimension : function(width, height) {
    var _me = this;
    _me._thumbDim = { 'width' : width, 'height' : height };
  },

  getThumbDimension : function() {
    var _me = this;
    if (_me._thumbDim) {
      return _me._thumbDim;
    }
    return undefined;
  },

  getDimension : function() {
    var _me = this;
    return {
      'width' : _me._imageData.width,
      'height' : _me._imageData.height
    };
  },

  getFilename : function() {
    var _me = this;
    return _me._imageData.filename;
  },

  getLastChangedBy : function() {
    var _me = this;
    return _me._imageData.lastChangedBy;
  },

  getMimeType : function() {
    var _me = this;
    return _me._imageData.mimeType;
  },

  getFileSize : function() {
    var _me = this;
    return _me._imageData.fileSize;
  },

  getVersion : function() {
    var _me = this;
    return _me._imageData.attversion;
  }

};

})(window);
