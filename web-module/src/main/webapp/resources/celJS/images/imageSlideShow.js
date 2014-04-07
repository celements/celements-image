/**
 * Celements image Slideshow
 * This is the Celements image Slideshow controller.
 */
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.image=="undefined"){CELEMENTS.image={};};

(function(window, undefined) {

  var CISS_OverlaySlideShowObj = undefined;
  var CISS_BodySlideShowStarter = undefined;
  var CISS_SlideShowObjHash = new Hash();

  CELEMENTS.image.getSlideShowObj = function(slideShowElemId) {
    return CISS_SlideShowObjHash.get(slideShowElemId);
  };

  CELEMENTS.image.getBodySlideShowStarter = function() {
    if (!CISS_BodySlideShowStarter) {
      CISS_BodySlideShowStarter = new CELEMENTS.image.SlideShowStarter($(document.body));
    }
    return CISS_BodySlideShowStarter;
  };

  $j(document).ready(function() {
    if (!CISS_OverlaySlideShowObj) {
      CISS_OverlaySlideShowObj = new CELEMENTS.image.SlideShow();
    }
    CELEMENTS.image.getBodySlideShowStarter().initializeOverlayImageSlideShow();
  });

  celAddOnBeforeLoadListener(function() {
    CELEMENTS.image.getBodySlideShowStarter().initializeImageSlideShow();
  });

//////////////////////////////////////////////////////////////////////////////
// Celements image Slideshow
//////////////////////////////////////////////////////////////////////////////
CELEMENTS.image.SlideShowStarter = function(htmlElem) {
  // constructor
  this._init(htmlElem);
};

(function() {
  CELEMENTS.image.SlideShowStarter.prototype = {
      _parentElem: undefined,
  
      _init : function(htmlElem) {
        var _me = this;
        _me._parentElem = htmlElem || $(document.body);
      },
  
      initializeOverlayImageSlideShow : function() {
        var _me = this;
        _me._parentElem.select('.celimage_slideshow').each(function(slideShowElem) {
          if (slideShowElem.hasClassName('celimage_overlay')) {
            CISS_OverlaySlideShowObj.registerOpenInOverlay(slideShowElem);
          }
        });
      },
  
      initializeImageSlideShow : function() {
        var _me = this;
        _me._parentElem.select('.celimage_slideshow').each(function(slideShowElem) {
          var imgSlideShow = new CELEMENTS.image.SlideShow(slideShowElem.id);
          CISS_SlideShowObjHash.set(slideShowElem.id, imgSlideShow);
          //important that the start happens before document.ready to allow the slideshow
          // context menu being loaded
          if (!slideShowElem.hasClassName('celimage_customStartSlide')
              || !slideShowElem.hasClassName('celimage_overlay')) {
            imgSlideShow.startNonOverlaySlideShow();
          }
        });
      },
      
      initializeSlideShow : function() {
        var _me = this;
        _me.initializeImageSlideShow();
        _me.initializeOverlayImageSlideShow();
      },

      autoRegisterOnSlides : function() {
        var _me = this;
        $(document.body).observe('cel_slideShow:registerAfterContentChanged',
            _me.registerAfterContentChangedListener.bind(_me));
      },

      registerAfterContentChangedListener : function(event) {
        var htmlContainer = event.memo;
        if (htmlContainer) {
          var registerStoppEvent = $(document.body).fire(
              'celimage_slideshow:shouldAutoRegister', htmlContainer);
          if (registerStoppEvent.stopped) {
            var imgSlideShowStarter = new CELEMENTS.image.SlideShowStarter(htmlContainer);
            imgSlideShowStarter.initializeSlideShow();
          }
        }
      }

  };
})();

CELEMENTS.image.SlideShow = function(htmlElem) {
  // constructor
  this._init(htmlElem);
};

(function() {
  CELEMENTS.image.SlideShow.prototype = {
      _openInOverlayBind : undefined,
      _imageSlideShowLoadFirstContentBind : undefined,
      _addNavigationButtonsBind : undefined,
      _initAnimationBind : undefined,
      _celSlideShowObj : null,
      _isOverlayRegistered : false,
      _currentHtmlElem : undefined,
      _gallery : undefined,
      _startSlideNum : undefined,
      _startAtSlideName : undefined,
      _resizeOverlayBind : undefined,
      _imgLoadedReCenterStartSlideBind : undefined,
      _celSlideShowManualStartStopClickHandlerBind : undefined,
      _origStyleValues : undefined,
      _slideShowAnimation : undefined,
      _wrapperHtmlElem : undefined,
      _hasRandomStart : false,
      _autoresize : false,
      _debug : false,
      _mobileDim : undefined,

      _init : function(htmlElem) {
        var _me = this;
        _me._mobileDim = new CELEMENTS.mobile.Dimensions(); 
        _me._isMobile = _me._mobileDim.isMobile;
        _me._autoresize = _me._isMobile.iOS() || _me._isMobile.Android();
        _me._currentHtmlElem = $(htmlElem) || null;
        _me._openInOverlayBind = _me.openInOverlay.bind(_me);
        _me._openInOverlayClickHandlerBind = _me._openInOverlayClickHandler.bind(_me);
        _me._celSlideShowManualStartStopClickHandlerBind =
          _me._celSlideShowManualStartStopClickHandler.bind(_me);
        _me._imageSlideShowLoadFirstContentBind =
          _me._imageSlideShowLoadFirstContent.bind(_me);
        _me._addNavigationButtonsBind = _me._addNavigationButtons.bind(_me);
        _me._initAnimationBind = _me._initAnimation.bind(_me);
        _me._addSlideShowCounterBind = _me._addSlideShowCounter.bind(_me);
        _me._resizeOverlayBind = _me._resizeOverlay.bind(_me);
        _me._imgLoadedReCenterStartSlideBind = _me._imgLoadedReCenterStartSlide.bind(_me);
        if (_me._currentHtmlElem) {
          _me._hasRandomStart = _me._currentHtmlElem.hasClassName(
              'celimage_slideshowRandomStart');
          if (_me._currentHtmlElem.tagName.toLowerCase() == 'img') {
            if (!_me._currentHtmlElem.hasClassName('celimage_customStartSlide')) {
              _me._fixStartImage(); 
            }
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
          if (!_me._getContainerElement().down('> div.celPresSlideShow_next')) {
            var nextButton = new Element('div').addClassName('celPresSlideShow_next');
            _me._getContainerElement().insert({'bottom' : nextButton});
          }
          if (!_me._getContainerElement().down('> div.celPresSlideShow_prev')) {
            var prevButton = new Element('div').addClassName('celPresSlideShow_prev');
            _me._getContainerElement().insert({'top' : prevButton});
          }
        }
      },

      _moveStyleToWrapper : function(divWrapper, element, styleName) {
        var _me = this;
        var newStyle = new Hash();
        var elemStyleValue = _me._getOriginalStyleValues(element).get(styleName);
        if (!elemStyleValue || (elemStyleValue == '')) {
          elemStyleValue = element.getStyle(styleName);
        }
        newStyle.set(styleName, elemStyleValue);
        divWrapper.setStyle(newStyle.toObject());
        newStyle.set(styleName, '');
        element.setStyle(newStyle.toObject());
      },

      _initNonOverlaySlideShowStarter : function(callbackFN) {
        var _me = this;
        var slideShowImg = $(_me._currentHtmlElem);
        if (!_me._wrapperHtmlElem) {
          if ((slideShowImg.tagName.toLowerCase() == 'img') && !slideShowImg.complete) {
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

      _camelCase : function (input) { 
        return input.toLowerCase().replace(/-(.)/g, function(match, group1) {
            return group1.toUpperCase();
        });
      },

      /**
       * _getOriginalStyleValues gets the original element styles opposed to getStyle
       * from prototype-js which gets the browser resolved style.
       *   e.g. for margin-left: auto _getOriginalStyleValues returns 'auto' yet
       *   protoptype-js returns the real pixel value.
       */
      _getOriginalStyleValues : function(htmlElement) {
        var _me = this;
        if (!_me._origStyleValues) {
          var origStyles = new Hash();
          htmlElement.getAttribute('style').split(';').each(function(styleElem) {
            var styleElemSplit = styleElem.split(':');
            if (styleElemSplit.size() > 1) {
              origStyles.set(_me._camelCase(styleElemSplit[0].strip()),
                  styleElemSplit[1].strip());
            }
          });
          _me._origStyleValues = origStyles;
        }
        return _me._origStyleValues;
      },

      _initNonOverlaySlideShow : function() {
        var _me = this;
        var slideShowImg = $(_me._currentHtmlElem);
        if (!_me._wrapperHtmlElem && (slideShowImg.tagName.toLowerCase() == 'img')) {
          var otherCssClassNames = $w(slideShowImg.className).without('celimage_slideshow'
              ).without('highslide-image');
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
          var containerAnimWidth = _me._getPart(_me._currentHtmlElem.id, 8,
              slideShowImg.getWidth());
          var containerAnimHeight = _me._getPart(_me._currentHtmlElem.id, 9,
              slideShowImg.getHeight());
          divWrapper.setStyle({
            'height' : containerAnimHeight + 'px',
            'width' : containerAnimWidth + 'px'
          });
          otherCssClassNames.each(function(className) {
                if (!className.startsWith('cel_effekt_')) {
                  divWrapper.addClassName(className);
                } else {
                  divWrapper.addClassName(className.replace(/cel_effekt_/,
                      'cel_slideshow_effekt_'));
                }
          });
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'float');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'marginTop');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'marginBottom');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'marginLeft');
          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'marginRight');
      // adding border to Wrapper leads to problems with double borders. Thus removed.
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'borderTop');
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'borderBottom');
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'borderRight');
  //          _me._moveStyleToWrapper(divWrapper, slideShowImg, 'borderLeft');
          _me._wrapperHtmlElem = divWrapper;
          _me._getCelSlideShowObj()._htmlContainer = _me._wrapperHtmlElem;
          _me._currentHtmlElem.fire('celimage_slideshow:afterInit', _me);
        } else if (!_me._wrapperHtmlElem) {
          _me._wrapperHtmlElem = slideShowImg;
          _me._getCelSlideShowObj()._htmlContainer = _me._wrapperHtmlElem;
          _me._currentHtmlElem.fire('celimage_slideshow:afterInit', _me);
        } else {
          if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
            console.log('skipping creating wrapperHTMLElem because already exists.',
                _me._wrapperHtmlElem);
          }
        }
      },

      startNonOverlaySlideShow : function() {
        var _me = this;
        _me._getGallery(_me._startNonOverlaySlideShowCallback.bind(_me), 1);
      },

      _startNonOverlaySlideShowCallback : function(galleryObj) {
        var _me = this;
        _me._celSlideShowObj = null;
        _me._getCelSlideShowObj(galleryObj.getLayoutName());
//        console.log('before starting _initAnimation');
//        _me._initAnimation();
        _me._initNonOverlaySlideShowStarter(
            _me._initNonOverlaySlideShowStarterCallback.bind(_me));
      },

      _initNonOverlaySlideShowStarterCallback : function() {
        var _me = this;
        _me._getCelSlideShowObj().setAutoresize(true);
        _me._getCelSlideShowObj().register();
        _me._imageSlideShowLoadFirstContent_internal();
      },

      _initAnimation : function(event) {
        var _me = this;
        var elemId = $(_me._currentHtmlElem).id;
        if (_me._getCelSlideShowObj().getHtmlContainer()) {
          _me._getCelSlideShowObj().getHtmlContainer().stopObserving(
              'cel_yuiOverlay:afterContentChanged', _me._initAnimationBind);
        }
        if (typeof CELEMENTS.presentation.SlideShowAnimation != 'undefined') {
          var slideShowEffect = _me._getPart(elemId, 3, 'none');
          var timeout = _me._getPart(elemId, 2, 3);
          _me._slideShowAnimation = new CELEMENTS.presentation.SlideShowAnimation(
              _me._getCelSlideShowObj(), timeout, slideShowEffect);
          var hasAnimation = !$(_me._currentHtmlElem).hasClassName('celimage_nonestart');
          if (hasAnimation) {
            _me._slideShowAnimation.register();
            var isAutoStart = !$(_me._currentHtmlElem).hasClassName('celimage_manualstart')
                            || $(_me._currentHtmlElem).hasClassName('celimage_autostart');
            _me._initManualStartButton();
            _me.celSlideShowManualStartStop(isAutoStart, true);
          }
        }
      },

      _initManualStartButton : function() {
        var _me = this;
        var isCelimageOverlay = _me._wrapperHtmlElem.hasClassName('celimage_overlay');
        var startButtonDiv = new Element('div', { 'class' : 'slideshowButton' });
        startButtonDiv.hide();
        _me._wrapperHtmlElem.insert({ bottom : startButtonDiv });
        if (isCelimageOverlay) {
          //XXX is this still at the right place?
          CISS_OverlaySlideShowObj.registerOpenInOverlay(_me._wrapperHtmlElem);
        } else {
          $(_me._wrapperHtmlElem).stopObserving('click',
              _me._celSlideShowManualStartStopClickHandlerBind);
          $(_me._wrapperHtmlElem).observe('click',
              _me._celSlideShowManualStartStopClickHandlerBind);
        }
      },

      _celSlideShowManualStartStopClickHandler : function(event) {
        var _me = this;
        event.stop();
        _me.celSlideShowManualStartStop();
      },

      celSlideShowManualStartStop : function(isStart, delayedStart) {
        var _me = this;
        if (typeof isStart === 'undefined') {
          isStart = _me._slideShowAnimation._paused;
        }
        if (typeof delayedStart === 'undefined') {
          delayedStart = false;
        }
        var slideShowButton = undefined;
        if (_me._wrapperHtmlElem) {
          slideShowButton = _me._wrapperHtmlElem.down('.slideshowButton');
        }
        if (isStart) {
          if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
            console.log('animation started for image slideshow', _me._currentHtmlElem.id);
          }
          _me._slideShowAnimation.startAnimation(delayedStart);
          if (slideShowButton) {
            Effect.Fade(slideShowButton, { duration : 1.0 });
          }
        } else {
          if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
            console.log('animation stopped for image slideshow', _me._currentHtmlElem.id);
          }
          _me._slideShowAnimation.stopAnimation();
          if (slideShowButton) {
            Effect.Appear(slideShowButton, { duration : 1.0 , to : 0.9 });
          }
        }
      },

      _addSlideShowCounter : function(event) {
        var _me = this;
//        var slideWrapperElem = event.memo.newSlideWrapperElem;
        if (!_me._getContainerElement().down('> div.celPresSlideShow_countSlideNum')) {
          var countSlideNumElem = new Element('div').addClassName(
              'celPresSlideShow_countSlideNum');
          _me._getContainerElement().insert({'bottom' : countSlideNumElem});
        }
        if (!_me._getContainerElement().down('> div.celPresSlideShow_currentSlideNum')) {
          var currentSlideNumElem = new Element('div').addClassName(
              'celPresSlideShow_currentSlideNum');
          _me._getContainerElement().insert({'bottom' : currentSlideNumElem});
        }
      },

      _imageSlideShowLoadFirstContent_internal : function() {
        var _me = this;
        _me._getCelSlideShowObj().getHtmlContainer().observe(
            'cel_yuiOverlay:afterContentChanged', _me._initAnimationBind);
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
        if (openDialog._dialogConfig.slideShowElem
            && (event.memo.slideShow._htmlContainerId === openDialog.getContainerId())) {
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
        if (!_me._startSlideNum) {
          if (_me._currentHtmlElem.hasClassName('celimage_slideshowRandomStart')) {
            if (_me._gallery && _me._gallery.getNumImages()) {
              var startSlideNum = Math.round(Math.random() * (
                  _me._gallery.getNumImages() - 1));
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

      _replaceStartImageInsert : function(imageElem) {
        var _me = this;
        _me._currentHtmlElem.src = imageElem.getThumbURL();
        _me._currentHtmlElem.removeAttribute('width');
        _me._currentHtmlElem.removeAttribute('height');
        _me._currentHtmlElem.setStyle({
          'visibility' : '',
          'width' : '',
          'height' : ''
        });
      },

      _replaceStartImage : function(galleryObj) {
        var _me = this;
        var images = galleryObj.getImages();
        var startSlideNum = _me.getStartSlideNum();
        if (startSlideNum < 0) {
          startSlideNum = 0;
        } else if (startSlideNum >= galleryObj.getNumImages()) {
          startSlideNum = galleryObj.getNumImages() - 1;
        }
        galleryObj.getImageForNum(startSlideNum, _me._replaceStartImageInsert.bind(_me));
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
        if (oldWidth > _me._mobileDim._getInnerWidth()) {
          newWidth = _me._mobileDim._getInnerWidth() - 20; // take care of close button
        }
        var zoomWidthFactor = newWidth / oldWidth;
        var oldHeight = parseInt(openDialog.getHeight());
        var newHeight = oldHeight;
        if (oldHeight > _me._mobileDim._getInnerHeight()) {
          newHeight = _me._mobileDim._getInnerHeight() - 20; // take care of close button
        }
        var zoomHeightFactor = newHeight / oldHeight;
        var zoomFactor;
        if (zoomHeightFactor < zoomWidthFactor) {
          zoomFactor = zoomHeightFactor;
        } else {
          zoomFactor = zoomWidthFactor;
        }
        return zoomFactor;
      }

  };
})();

})(window);
