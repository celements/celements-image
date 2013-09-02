/**
 * Celements image Slideshow
 * This is the Celements image Slideshow controller.
 */
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.image=="undefined"){CELEMENTS.image={};};

(function(window, undefined) {

  var isMobile = {
      Android: function() {
        return navigator.userAgent.match(/Android/i);
      },
      BlackBerry: function() {
        return navigator.userAgent.match(/BlackBerry/i);
      },
      iOS: function() {
        return navigator.userAgent.match(/iPhone|iPad|iPod/i);
      },
      iPhone: function() {
        return navigator.userAgent.match(/iPhone/i);
      },
      iPod: function() {
        return navigator.userAgent.match(/iPod/i);
      },
      iPad: function() {
        return navigator.userAgent.match(/iPad/i);
      },
      Opera: function() {
        return navigator.userAgent.match(/Opera Mini/i);
      },
      Windows: function() {
        return navigator.userAgent.match(/IEMobile/i);
      },
      Simulator: function() {
        // http://iphone4simulator.com/ maybe
        return (window.top != window);
      },
      any: function() {
        return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() 
            || isMobile.Opera() || isMobile.Windows());
      }
    };

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
      _resizeOverlayBind : undefined,
      _autoresize : true,
      _debug : false,

      _init : function(htmlElem) {
        var _me = this;
        _me._currentHtmlElem = $(htmlElem) || null;
        _me._openInOverlayBind = _me.openInOverlay.bind(_me);
        _me._openInOverlayClickHandlerBind = _me._openInOverlayClickHandler.bind(_me);
        _me._imageSlideShowLoadFirstContentBind =
          _me._imageSlideShowLoadFirstContent.bind(_me);
        _me._addNavigationButtonsBind = _me._addNavigationButtons.bind(_me);
        _me._resizeOverlayBind = _me._resizeOverlay.bind(_me);
        if (_me._currentHtmlElem) {
          if (_me._currentHtmlElem.tagName.toLowerCase() == 'img') {
            _me._fixStartImage(); 
          } else if (_me._currentHtmlElem.down('.slideWrapper')) {
            _me._centerStartSlide();
          }
        }
      },

      _centerStartSlide : function() {
        var _me = this;
        var slideWrapper = _me._currentHtmlElem.down('.slideWrapper');
        slideWrapper.setStyle({
          'position' : 'absolute',
          'width' : 'auto'
          });
        var slideWidth = slideWrapper.getWidth();
        var slideHeight = slideWrapper.getHeight();
        var parentDiv = slideWrapper.up('.slideRoot') || slideWrapper;
        var parentHeight = parentDiv.getHeight();
        var topPos = (parentHeight - slideHeight) / 2;
        slideWrapper.setStyle({
          'position' : 'relative',
          'margin' : '0',
          'margin-left' : 'auto',
          'margin-right' : 'auto',
          'width' : slideWidth + 'px',
          'height' : slideHeight + 'px',
          'top' : topPos + 'px'
          });
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
          Event.observe(window, "resize", _me._resizeOverlayBind);
          Event.observe(window, "orientationchange", _me._resizeOverlayBind);
        }
        htmlElem.observe('click', _me._openInOverlayClickHandlerBind);
        htmlElem.observe('cel_ImageSlideShow:startSlideShow', _me._openInOverlayBind);
        $(document.body).observe('cel_yuiOverlay:loadFirstContent',
            _me._imageSlideShowLoadFirstContentBind);
        $(document.body).observe('cel_yuiOverlay:afterShowDialog_General',
            _me._resizeOverlayBind);
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
        var htmlElem = event.findElement('.celimage_overlay') || event.element();
        _me._currentHtmlElem = htmlElem;
        _me._startAtSlideName = event.memo;
        _me._startSlideNum = undefined;
        var hasCloseButton = htmlElem.hasClassName('celimage_overlay_addCloseButton');
        var openDialog = CELEMENTS.presentation.getOverlayObj({
          'close' : hasCloseButton,
          'slideShowElem' : htmlElem,
          'link' : htmlElem,
          fixedcenter: !_me._autoresize
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
      },

      _resizeOverlay : function() {
        var _me = this;
        if (_me._autoresize) {
          var openDialog = CELEMENTS.presentation.getOverlayObj();
          var zoomFactor = _me._computeZoomFactor();
          if (zoomFactor <= 1) {
            var oldWidth = parseInt(openDialog.getWidth());
            var oldHeight = parseInt(openDialog.getHeight());
            newHeight = oldHeight * zoomFactor;
            newWidth = oldWidth * zoomFactor;
            var eventMemo = {
                'fullWidth' : oldWidth,
                'fullHeight' : oldHeight,
                'zoomFactor' : zoomFactor,
                'newWidth' : newWidth,
                'newHeight' : newHeight
            };
            if (_me._debug && (typeof console != 'undefined')
                && (typeof console.log != 'undefined')) {
              console.log('final resize factor: ', eventMemo);
            }
            $(document.body).fire('cel_imageSlideShow:beforeResizeDialog_General',
                eventMemo);
            openDialog._overlayDialog.cfg.setProperty('width', newWidth + 'px');
            openDialog._overlayDialog.cfg.setProperty('height', newHeight + 'px');
            $(document.body).fire('cel_imageSlideShow:afterResizeDialog_General',
                eventMemo);
            var resizeEvent = $('yuiOverlayContainer').fire(
                'cel_imageSlideShow:resizeDialogContent', eventMemo);
            if (!resizeEvent.stopped) {
              $('yuiOverlayContainer').setStyle({
                'height' : newHeight + 'px',
                'zoom' : zoomFactor
              });
            }
          } else {
            if (_me._debug && (typeof console != 'undefined')
                && (typeof console.log != 'undefined')) {
              console.log('no resize needed.', zoomFactor);
            }
          }
          openDialog._overlayDialog.center();
        }
      },

      _computeZoomFactor : function() {
        var _me = this;
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        var oldWidth = parseInt(openDialog.getWidth());
        var newWidth = oldWidth;
        if (oldWidth > _me._getInnerWidth()) {
          newWidth = _me._getInnerWidth() - 20; // take care of close button
        }
        var zoomWidthFactor = newWidth / oldWidth;
        var oldHeight = parseInt(openDialog.getHeight());
        var newHeight = oldHeight;
        if (oldHeight > _me._getInnerHeight()) {
          newHeight = _me._getInnerHeight() - 20; // take care of close button
        }
        var zoomHeightFactor = newHeight / oldHeight;
        var zoomFactor;
        if (zoomHeightFactor < zoomWidthFactor) {
          zoomFactor = zoomHeightFactor;
        } else {
          zoomFactor = zoomWidthFactor;
        }
        return zoomFactor;
      },

      _isOrientationLandscape : function() {
//        var _me = this;
        var innerWidth = window.innerWidth || document.documentElement.clientWidth;
        var innerHeight = window.innerHeight || document.documentElement.clientHeight;
        //window.orientation works only correct on load, but has whimsical behavior when 
        //  rotating 
        return innerWidth > innerHeight;
      },

      _getInnerWidth : function() {
        var _me = this;
        var width = window.innerWidth || document.documentElement.clientWidth;
        if(isMobile.any()) {
          if(isMobile.iOS() && _me._isOrientationLandscape()) {
            width = screen.height;
          } else {
            width = screen.width;
          }
        }
        return width;
      },

      _getInnerHeight : function() {
        var _me = this;
        var height = window.innerHeight || document.documentElement.clientHeight;
        if(isMobile.any()) {
          if(isMobile.iOS() && _me._isOrientationLandscape()) {
            height = screen.width;
          } else {
            height = screen.height;
          }
        }
        return height;
      }

  };
})();

})(window);
