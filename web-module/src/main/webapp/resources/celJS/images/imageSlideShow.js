/**
 * Celements image Slideshow
 * This is the Celements image Slideshow controller.
 */
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.image=="undefined"){CELEMENTS.image={};};

(function() {

  var CISS_OverlaySlideShowObj = undefined;

  $j(document).ready(function() {
    if (!CISS_OverlaySlideShowObj) {
      CISS_OverlaySlideShowObj = new CELEMENTS.image.SlideShow();
    }
    $$('.celimage_slideshow').each(function(slideShowElem) {
      if (slideShowElem.hasClassName('celimage_overlay')) {
        CISS_OverlaySlideShowObj.registerOpenInOverlay(slideShowElem);
      }
    });
  });

  var CISS_SlideShowObjHash = new Hash();

  var initializeImageSlideShow = function() {
    $$('.celimage_slideshow').each(function(slideShowElem) {
      CISS_SlideShowObjHash.set(slideShowElem.id, new CELEMENTS.image.SlideShow(
          slideShowElem.id));
    });
  };

  celAddOnBeforeLoadListener(initializeImageSlideShow);

//////////////////////////////////////////////////////////////////////////////
// Celements image Slideshow
//////////////////////////////////////////////////////////////////////////////
CELEMENTS.image.SlideShow = function(htmlElem) {
  containerId = htmlElem;
  // constructor
  this._init(htmlElem);
};

(function() {
  CELEMENTS.image.SlideShow.prototype = {
      _openInOverlayBind : undefined,
      _imageSlideShowLoadFirstContentBind : undefined,
      _addNavigationButtonsBind : undefined,
      _celSlideShowObj : null,
      _isOverlayRegistered : false,
      _currentHtmlElem : undefined,
      _gallery : undefined,
      _startSlideNum : undefined,
      _startAtSlideName : undefined,

      _init : function(htmlElem) {
        var _me = this;
        _me._currentHtmlElem = $(htmlElem) || null;
        _me._openInOverlayBind = _me.openInOverlay.bind(_me);
        _me._openInOverlayClickHandlerBind = _me._openInOverlayClickHandler.bind(_me);
        _me._imageSlideShowLoadFirstContentBind =
          _me._imageSlideShowLoadFirstContent.bind(_me);
        _me._addNavigationButtonsBind = _me._addNavigationButtons.bind(_me);
        if(_me._currentHtmlElem) {
         _me._fixStartImage(); 
        }
      },

      registerOpenInOverlay : function(htmlElem) {
        var _me = this;
        if (!_me._isOverlayRegistered) {
          _me._isOverlayRegistered = true;
          _me._celSlideShowObj = getCelSlideShowObj();
          var bodyElem = $$('body')[0];
          bodyElem.observe('cel_slideShow:shouldRegister',
              _me._checkIsImageSlideShowOverlay.bind(_me));
          bodyElem.observe('cel_yuiOverlay:hideEvent',
              _me._removeIsImageSlideShowOverlay.bind(_me));
        }
        htmlElem.observe('click', _me._openInOverlayClickHandlerBind);
        htmlElem.observe('cel_ImageSlideShow:startSlideShow', _me._openInOverlayBind);
        $(document.body).observe('cel_yuiOverlay:loadFirstContent',
            _me._imageSlideShowLoadFirstContentBind);
        $(document.body).fire('cel_ImageSlideShow:finishedRegister', _me);
      },

      _openInOverlayClickHandler : function(event) {
        var _me = this;
        event.stop();
        _me.openInOverlay(event);
      },

      _getGallery : function(callbackFN) {
        var _me = this;
        var galleryFN = _me._getPart(_me._currentHtmlElem.id, 1, '');
        if (!_me._gallery && (galleryFN != '')) {
          _me._gallery = new CELEMENTS.images.Gallery(galleryFN, callbackFN);
        } else {
          callbackFN(_me._gallery);
        }
      },

      _getCelSlideShowObj : function(overwriteLayout) {
        var _me = this;
        if (!_me._celSlideShowObj) {
          _me._celSlideShowObj = new CELEMENTS.presentation.SlideShow(
              _me._currentHtmlElem.id);
          overwriteLayout = overwriteLayout || 'SimpleLayout';
          _me._celSlideShowObj.setOverwritePageLayout(overwriteLayout);
        } else if (overwriteLayout) {
          _me._celSlideShowObj.setOverwritePageLayout(overwriteLayout);
        }
        return _me._celSlideShowObj;
      },

      _getContainerElement : function() {
        var _me = this;
        if (_me._isOverlayRegistered) {
          return _me._getCelSlideShowObj().getHtmlContainer();
        } else {
          return _me._currentHtmlElem;
        }
      },

      _addNavigationButtons : function(event) {
        var _me = this;
        if (_me._currentHtmlElem.hasClassName('celimage_addNavigation')) {
          var nextButton = new Element('div').addClassName('celPresSlideShow_next');
          var prevButton = new Element('div').addClassName('celPresSlideShow_prev');
          _me._getContainerElement().insert({'bottom' : nextButton});
          _me._getContainerElement().insert({'top' : prevButton});
        }
      },

      _imageSlideShowLoadFirstContent : function(event) {
        var _me = this;
        var dialogConfig = event.memo;
        if (dialogConfig.slideShowElem) {
          _me._getCelSlideShowObj().getHtmlContainer().observe(
              'cel_yuiOverlay:afterContentChanged', _me._addNavigationButtonsBind);
          var gallerySpace = _me._getPart(_me._currentHtmlElem.id, 7, '');
          var startAt = _me._startAtSlideName || _me.getStartSlideNum();
          _me._getCelSlideShowObj().loadMainSlides(gallerySpace, startAt);
          event.stop();
        }
      },

      _checkIsImageSlideShowOverlay : function(event) {
//        var _me = this;
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        if (openDialog._dialogConfig.slideShowElem) {
          event.stop();
        }
      },

      _removeIsImageSlideShowOverlay : function() {
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        openDialog.updateOpenConfig({ 'slideShowElem' : null });
      },

      openInOverlay : function(event) {
        var _me = this;
        var htmlElem = event.element();
        _me._currentHtmlElem = htmlElem;
        _me._startAtSlideName = event.memo;
        var hasCloseButton = htmlElem.hasClassName('celimage_overlay_addCloseButton');
        var openDialog = CELEMENTS.presentation.getOverlayObj({
          'close' : hasCloseButton,
          'slideShowElem' : htmlElem,
          'link' : htmlElem
        });
        _me._getGallery(function(galleryObj) {
          _me._getCelSlideShowObj(galleryObj.getLayoutName());
          openDialog.intermediatOpenHandler();
        });
      },

      _getPart : function(elemId, num, defaultvalue) {
        var parts = elemId.split(':');
        if ((num < parts.length) && (parts[num] != '')) {
          return parts[num];
        } else {
          return defaultvalue;
        }
      },

      _fixStartImage : function() {
        var _me = this;
        _me._currentHtmlElem.setStyle({
          'visibility' : 'hidden'
        });
        _me._getGallery(_me._replaceStartImage.bind(_me));
      },

      _getStartSlideNumFromId : function() {
        var _me = this;
        return parseInt(_me._getPart(_me._currentHtmlElem.id, 6, 1)) - 1;
      },

      setStartSlideNum : function(newStartSlideNum) {
        var _me = this;
        _me._startSlideNum = newStartSlideNum;
      },

      getStartSlideNum : function() {
        var _me = this;
        if (!_me._startSlideNum) {
          _me.setStartSlideNum(_me._getStartSlideNumFromId() || 0);
        }
        return _me._startSlideNum;
      },

      _replaceStartImage : function(galleryObj) {
        var _me = this;
        var images = galleryObj.getImages();
        var startSlideNum = _me.getStartSlideNum();
        if (startSlideNum < 0) {
          startSlideNum = 0;
        } else if (startSlideNum >= images.size()) {
          startSlideNum = images.size() - 1;
        }
        _me._currentHtmlElem.src = images[startSlideNum].getThumbURL();
        _me._currentHtmlElem.removeAttribute('width');
        _me._currentHtmlElem.removeAttribute('height');
        _me._currentHtmlElem.setStyle({
          'visibility' : '',
          'width' : '',
          'height' : ''
        });
      }

  };
})();

})();
