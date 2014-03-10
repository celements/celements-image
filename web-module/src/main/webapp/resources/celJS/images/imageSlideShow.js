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

  CELEMENTS.image.getSlideShowObj = function(slideShowElemId) {
    return CISS_SlideShowObjHash.get(slideShowElemId);
  };

  var initializeImageSlideShow = function() {
    $$('.celimage_slideshow').each(function(slideShowElem) {
      var imgSlideShow = new CELEMENTS.image.SlideShow(slideShowElem.id);
      CISS_SlideShowObjHash.set(slideShowElem.id, imgSlideShow);
      if (!slideShowElem.hasClassName('celimage_manualstart')) {
        //important that the start happens before document.ready to allow the slideshow
        // context menu beeing loaded
        imgSlideShow.startNonOverlaySlideShow();
      } else if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
        console.log('skip auto start image-slideshow because css class'
            + ' celimage_manualstart found on', slideShowElem.id);
      }
    });
  };

  celAddOnBeforeLoadListener(initializeImageSlideShow);

//////////////////////////////////////////////////////////////////////////////
// Celements image Slideshow
//////////////////////////////////////////////////////////////////////////////
CELEMENTS.image.SlideShow = function(htmlElem) {
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
      _imgLoadedReCenterStartSlideBind : undefined,
      _slideShowAnimation : undefined,
      _wrapperHtmlElem : undefined,
      _hasRandomStart : false,
      _autoresize : false,
      _debug : false,

      _init : function(htmlElem) {
        var _me = this;
        _me._autoresize = isMobile.iOS() || isMobile.Android();
        _me._currentHtmlElem = $(htmlElem) || null;
        _me._openInOverlayBind = _me.openInOverlay.bind(_me);
        _me._openInOverlayClickHandlerBind = _me._openInOverlayClickHandler.bind(_me);
        _me._imageSlideShowLoadFirstContentBind =
          _me._imageSlideShowLoadFirstContent.bind(_me);
        _me._addNavigationButtonsBind = _me._addNavigationButtons.bind(_me);
        _me._addSlideShowCounterBind = _me._addSlideShowCounter.bind(_me);
        _me._resizeOverlayBind = _me._resizeOverlay.bind(_me);
        _me._imgLoadedReCenterStartSlideBind = _me._imgLoadedReCenterStartSlide.bind(_me);
        if (_me._currentHtmlElem) {
          _me._hasRandomStart = _me._currentHtmlElem.hasClassName(
              'celimage_slideshowRandomStart');
          if (_me._currentHtmlElem.tagName.toLowerCase() == 'img') {
            _me._fixStartImage(); 
          } else if (_me._currentHtmlElem.down('.slideWrapper')) {
            //TODO check if recenter here still is needed after it is correctly done in CelementsSlideShow.js
            _me._currentHtmlElem.select('img').each(function(imgElem) {
              imgElem.observe('load', _me._imgLoadedReCenterStartSlideBind.curry(imgElem));
            });
            _me._centerStartSlide();
          }
        }
      },

      _imgLoadedReCenterStartSlide : function(imgElem, event) {
        var _me = this;
        imgElem.stopObserving('load', _me._imgLoadedReCenterStartSlideBind);
        _me._centerStartSlide();
      },

      _centerStartSlide : function() {
        var _me = this;
        var slideWrapper = _me._currentHtmlElem.down('.slideWrapper');
        slideWrapper.setStyle({
          'position' : 'absolute',
          'width' : 'auto',
          'height' : 'auto',
          'top' : 0,
          'marginLeft' : 0,
          'marginRight' : 0
        });
        var slideWidth = slideWrapper.getWidth();
        var slideHeight = slideWrapper.getHeight();
        var parentDiv = slideWrapper.up('.slideRoot') || slideWrapper;
        var parentHeight = parentDiv.getHeight();
        var topPos = (parentHeight - slideHeight) / 2;
        slideWrapper.setStyle({
          'position' : 'relative',
          'margin' : '0',
          'marginLeft' : 'auto',
          'marginRight' : 'auto',
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

      _getGallery : function(callbackFN, onlyFirstNumImages) {
        var _me = this;
        var galleryFN = _me._getPart(_me._currentHtmlElem.id, 1, '');
        if (!_me._gallery && (galleryFN != '')) {
          _me._gallery = new CELEMENTS.images.Gallery(galleryFN, callbackFN.bind(_me),
              onlyFirstNumImages);
        } else {
          _me._gallery.executeAfterLoad(callbackFN.bind(_me));
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
        } else if (_me._wrapperHtmlElem) {
          return _me._wrapperHtmlElem;
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

      _moveStyleToWrapper : function(divWrapper, element, styleName) {
        var newStyle = new Hash();
        newStyle.set(styleName, element.getStyle(styleName));
        divWrapper.setStyle(newStyle.toObject());
        newStyle.set(styleName, '');
        element.setStyle(newStyle.toObject());
      },

      _initNonOverlaySlideShowStarter : function(callbackFN) {
        var _me = this;
        var slideShowImg = $(_me._currentHtmlElem);
        if (!_me._wrapperHtmlElem) {
          if (!slideShowImg.complete) {
            var _initNonOverlaySlideShowStarterBind =
              _me._initNonOverlaySlideShowStarter.bind(_me);
            slideShowImg.observe('load', _initNonOverlaySlideShowStarterBind.curry(
                callbackFN));
          } else {
            _me._initNonOverlaySlideShow();
            if (callbackFN) {
              callbackFN();
            }
          }
        }
      },

      _initNonOverlaySlideShow : function() {
        var _me = this;
        var slideShowImg = $(_me._currentHtmlElem);
        if (!_me._wrapperHtmlElem) {
          var otherCssClassNames = $w(slideShowImg.className).without('celimage_slideshow'
              ).without('celimage_overlay').without('highslide-image');
          var divInnerWrapper = slideShowImg.wrap('div', {
            'id' : ('slideWrapper_' + slideShowImg.id),
            'class' : 'cel_slideShow_slideWrapper'
           }).setStyle({
             'position' : 'relative'
           });
          //to allow propper scaling we need to add a slideRoot element
          var divSlideRoot = divInnerWrapper.wrap('div', {
            'id' : ('slideRoot_' + slideShowImg.id),
            'class' : 'cel_slideShow_slideRoot'
           });
          var divWrapper = divSlideRoot.wrap('div', {
            'id' : ('slideContainer_' + slideShowImg.id),
            'class' : 'celimage_slideshow_wrapper cel_cm_celimage_slideshow'
           }).setStyle({
             'position' : 'relative'
           });
          //TODO get wrapper dimensions from where? Maybe we should make this flexible to
          //TODO choise for the user in the image picker...
          divWrapper.setStyle({
            'height' : slideShowImg.getHeight() + 'px',
            'width' : slideShowImg.getWidth() + 'px'
          });
          otherCssClassNames.each(function(className) {
                if (!className.startsWith('cel_effekt_')) {
                  divWrapper.addClassName(className);
                }
          });
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'float');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'margin-top');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'margin-bottom');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'margin-left');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'margin-right');
      // adding border to Wrapper leads to problems with double borders. Thus removed.
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'border-top');
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'border-bottom');
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'border-right');
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'border-left');
          _me._wrapperHtmlElem = divWrapper;
          _me._getCelSlideShowObj()._htmlContainer = _me._wrapperHtmlElem;
          _me._currentHtmlElem.fire('celimage_slideshow:afterInit', _me);
          }
      },

      startNonOverlaySlideShow : function() {
        var _me = this;
        _me._getGallery(_me._startNonOverlaySlideShowCallback.bind(_me));
      },

      _startNonOverlaySlideShowCallback : function(galleryObj) {
        var _me = this;
        _me._celSlideShowObj = null;
        var elemId = $(_me._currentHtmlElem).id;
        if (typeof CELEMENTS.presentation.SlideShowAnimation != 'undefined') {
          var slideShowEffect = _me._getPart(elemId, 3, 'none');
          var timeout = _me._getPart(elemId, 2, 3);
          _me._slideShowAnimation = new CELEMENTS.presentation.SlideShowAnimation(
              _me._getCelSlideShowObj(galleryObj.getLayoutName()), timeout,
              slideShowEffect);
        }
        _me._initNonOverlaySlideShowStarter(
            _me._initNonOverlaySlideShowStarterCallback.bind(_me));
      },

      _initNonOverlaySlideShowStarterCallback : function() {
        var _me = this;
        _me._getCelSlideShowObj().setAutoresize(true);
        _me._getCelSlideShowObj().register();
        _me._slideShowAnimation.register();
        _me._imageSlideShowLoadFirstContent_internal();
      },

      _addSlideShowCounter : function(event) {
        var slideWrapperElem = event.memo.newSlideWrapperElem;
        var countSlideNumElem = new Element('div').addClassName(
            'celPresSlideShow_countSlideNum');
        var currentSlideNumElem = new Element('div').addClassName(
            'celPresSlideShow_currentSlideNum');
        slideWrapperElem.insert({'bottom' : countSlideNumElem});
        slideWrapperElem.insert({'bottom' : currentSlideNumElem});
      },

      _imageSlideShowLoadFirstContent_internal : function() {
        var _me = this;
        _me._getCelSlideShowObj().getHtmlContainer().observe(
            'cel_yuiOverlay:afterContentChanged', _me._addNavigationButtonsBind);
        _me._getCelSlideShowObj().getHtmlContainer().observe(
            'cel_yuiOverlay:beforeSlideInsert', _me._addSlideShowCounterBind);
        var gallerySpace = _me._getPart(_me._currentHtmlElem.id, 7, '');
        var startAt = _me._startAtSlideName || _me.getStartSlideNum();
        _me._getCelSlideShowObj().loadMainSlides(gallerySpace, startAt);
      },

      _imageSlideShowLoadFirstContent : function(event) {
        var _me = this;
        var dialogConfig = event.memo;
        if (dialogConfig.slideShowElem) {
          _me._imageSlideShowLoadFirstContent_internal();
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
        if (isNaN(_me._startAtSlideName)) {
          _me._startAtSlideName = undefined;
        }
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
        }, 1);
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
        var startSlideNum = _me.getStartSlideNum();
        if (startSlideNum >= 0) {
          _me._currentHtmlElem.setStyle({
            'visibility' : 'hidden'
          });
          _me._getGallery(_me._replaceStartImage.bind(_me), startSlideNum + 1);
        } else if ((typeof console != 'undefined')
            && (typeof console.log != 'undefined')) {
          console.log('skip fix start image because start slide number is below zero',
              startSlideNum);
        }
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
        console.log('getStartSlideNum: ', _me._startSlideNum, _me._celSlideShowObj);
        if (!_me._startSlideNum) {
          if (_me._currentHtmlElem.hasClassName('celimage_slideshowRandomStart')) {
            if (_me._gallery && _me._gallery.getNumImages()) {
              var startSlideNum = Math.round(Math.random() * (
                  _me._gallery.getNumImages() - 1));
              console.log('getStartSlideNum: random start ', startSlideNum,
                  _me._gallery.getNumImages());
              _me.setStartSlideNum(startSlideNum);
            } else {
              return 0;
            }
          } else {
            _me.setStartSlideNum(_me._getStartSlideNumFromId() || 0);
          }
        }
        return _me._startSlideNum;
      },

      _replaceStartImage : function(galleryObj) {
        var _me = this;
        var images = galleryObj.getImages();
        console.log('_replaceStartImage: ', _me._startSlideNum, images.size());
        var startSlideNum = _me.getStartSlideNum();
        if (startSlideNum < 0) {
          startSlideNum = 0;
        } else if (startSlideNum >= galleryObj.getNumImages()) {
          startSlideNum = galleryObj.getNumImages() - 1;
        }
        console.log('_replaceStartImage: startSlideNum ', startSlideNum, images[startSlideNum]);
        if (images[startSlideNum]) {
          _me._currentHtmlElem.src = images[startSlideNum].getThumbURL();
          _me._currentHtmlElem.removeAttribute('width');
          _me._currentHtmlElem.removeAttribute('height');
          _me._currentHtmlElem.setStyle({
            'visibility' : '',
            'width' : '',
            'height' : ''
          });
        }
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
                'zoom' : zoomFactor,
                'transform' : 'scale(' + zoomFactor + ')',
                'transformOrigin' : '0 0 0',
                'height' : oldHeight + 'px',  // important for FF
                'width' : oldWidth + 'px' // important for FF
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
          } else if (!isMobile.Android()) {
            width = screen.width;
          }
        }
        return width;
      },

      _getInnerHeight : function() {
//        var _me = this;
        var height = window.innerHeight || document.documentElement.clientHeight;
//        if(isMobile.any()) {
//          if(isMobile.iOS() && _me._isOrientationLandscape()) {
//            height = screen.width;
//          } else {
//            height = screen.height;
//          }
//        }
        return height;
      }

  };
})();

})(window);
