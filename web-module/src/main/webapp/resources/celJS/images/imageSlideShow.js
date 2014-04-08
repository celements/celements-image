/**
 * Celements image Slideshow
 * This is the Celements image Slideshow controller.
 */
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.image=="undefined"){CELEMENTS.image={};};

(function(window, undefined) {
  "use strict";

  var CISS_BodySlideShowStarter = undefined;
  var CISS_SlideShowObjHash = new Hash();
  var CISS_SlideShowOverlayObjHash = new Hash();

  window.CELEMENTS.image.getSlideShowObj = function(slideShowElemId) {
    return CISS_SlideShowObjHash.get(slideShowElemId);
  };

  window.CELEMENTS.image.getSlideShowOverlayObj = function(slideShowElemId) {
    return CISS_SlideShowOverlayObjHash.get(slideShowElemId);
  };

  window.CELEMENTS.image.getBodySlideShowStarter = function() {
    if (!CISS_BodySlideShowStarter) {
      CISS_BodySlideShowStarter = new CELEMENTS.image.SlideShowStarter($(document.body));
    }
    return CISS_BodySlideShowStarter;
  };

  window.celAddOnBeforeLoadListener(function() {
    window.CELEMENTS.image.getBodySlideShowStarter().initializeSlideShow();
  });

//////////////////////////////////////////////////////////////////////////////
// Celements image Slideshow
//////////////////////////////////////////////////////////////////////////////
  window.CELEMENTS.image.SlideShowStarter = function(htmlElem) {
    // constructor
    this._init(htmlElem);
  };

(function() {
  "use strict";

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
            var overlayContainerObj = new CELEMENTS.image.OverlayContainer(slideShowElem);
            CISS_SlideShowOverlayObjHash.set(slideShowElem.id, overlayContainerObj);
          }
        });
      },
  
      initializeImageSlideShow : function() {
        var _me = this;
        _me._parentElem.select('.celimage_slideshow').each(function(slideShowElem) {
          var inlineContainerObj = new CELEMENTS.image.InlineContainer(slideShowElem);
          CISS_SlideShowObjHash.set(slideShowElem.id, inlineContainerObj);
        });
      },
      
      initializeSlideShow : function() {
        var _me = this;
        _me.initializeImageSlideShow();
        _me.initializeOverlayImageSlideShow();
        $(document.body).fire('celimage_slideshowstarter:replaceElem', _me._parentElem);
        $(document.body).fire('celimage_slideshowstarter:afterReplace', _me._parentElem);
        $(document.body).fire('celimage_slideshowstarter:afterInit', _me._parentElem);
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

/** deprecated **/
  window.CELEMENTS.image.SlideShowDeprecated = function(htmlElem) {
    // constructor
    this._init(htmlElem);
  };

(function() {
  "use strict";

  CELEMENTS.image.SlideShowDeprecated.prototype = {
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
//        $(document.body).observe('cel_yuiOverlay:loadFirstContent',
//            _me._imageSlideShowLoadFirstContentBind);
//        $(document.body).observe('cel_yuiOverlay:afterShowDialog_General',
//            _me._resizeOverlayBind);
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
          console.log('_initNonOverlaySlideShow: for wrapper ', slideShowImg);
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
            console.log('_initAnimation before _initManualStartButton: ',
                _me._currentHtmlElem, _me._wrapperHtmlElem);
            _me._initManualStartButton();
            _me.celSlideShowManualStartStop(isAutoStart, true);
          }
        }
      },

      _initManualStartButton : function() {
        var _me = this;
        //FIXME _me._wrapperHtmlElem is undefined in overlay
        var isCelimageOverlay = _me._wrapperHtmlElem.hasClassName('celimage_overlay');
        var startButtonDiv = new Element('div', { 'class' : 'slideshowButton' });
        startButtonDiv.hide();
        _me._wrapperHtmlElem.insert({ bottom : startButtonDiv });
        if (isCelimageOverlay) {
          console.log('_initManuelStartButton: init overlay starter for ',
              _me._wrapperHtmlElem);
          var overlaySlideShowObj = new CELEMENTS.image.SlideShow();
          //XXX is this still at the right place?
          overlaySlideShowObj.registerOpenInOverlay(_me._wrapperHtmlElem);
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
        if (!_me._currentHtmlElem.hasClassName('celimage_addCounterNone')) {
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
        }
      },

      _imageSlideShowLoadFirstContent_internal : function() {
        var _me = this;
        _me._getCelSlideShowObj().getHtmlContainer().observe(
            'cel_yuiOverlay:afterContentChanged', _me._addNavigationButtonsBind);
        _me._getCelSlideShowObj().getHtmlContainer().observe(
            'cel_yuiOverlay:beforeSlideInsert', _me._addSlideShowCounterBind);
        var gallerySpace = _me._getPart(_me._currentHtmlElem.id, 7, '');
//        _me._getCelSlideShowObj().getHtmlContainer().observe(
//            'cel_yuiOverlay:afterContentChanged', _me._initAnimationBind);
//        var startAt = _me._startAtSlideName || _me.getStartSlideNum();
//        _me._getCelSlideShowObj().loadMainSlides(gallerySpace, startAt);
        _me._getCelSlideShowObj().loadAndAddMainSlides(gallerySpace);
        _me._initAnimation();
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

//////////////////////////////////////////////////////////////////////////////
// image SlideShow container in a celYuiOverlay
//////////////////////////////////////////////////////////////////////////////
window.CELEMENTS.image.OverlayContainer = function(htmlElem) {
  // constructor
  this._init(htmlElem);
};

(function() {
  "use strict";

  CELEMENTS.image.OverlayContainer.prototype = {
      _htmlElemId : undefined,

      /**
       * Read all configuration information from the original element.
       * It might be replaced/changed later on and the configuration
       * information might geht lost. Only keep the htmlElem.id because
       * the element might get replaced.
       */
      _init : function(htmlElem) {
        var _me = this;
        _me._htmlElemId = htmlElem.id;
        _me._configReader = new CELEMENTS.image.ConfigReader(htmlElem, {
          'manualstart' : 'celimage_overlaymanualstart',
          'nonestart' : 'celimage_overlaynonestart',
          'autostart' : 'celimage_overlayautostart'
        });
      },

      /** before: registerOpenInOverlay **/
      /**
       * Register on element with same id as htmlElem.
       * The original element might be replaced in between.
       */
      registerOpenInOverlay : function(htmlElem) {
        var _me = this;
        console.log('registerOpenInOverlay:', htmlElem, _me._htmlElemId);
        console.warn('registerOpenInOverlay not yet implemented');
//        if (!_me._isOverlayRegistered) {
//          _me._isOverlayRegistered = true;
//          _me._celSlideShowObj = getCelSlideShowObj();
//          var bodyElem = $$('body')[0];
//          bodyElem.observe('cel_slideShow:shouldRegister',
//              _me._checkIsImageSlideShowOverlay.bind(_me));
//          bodyElem.observe('cel_yuiOverlay:hideEvent',
//              _me._removeIsImageSlideShowOverlay.bind(_me));
//          Event.observe(window, "resize", _me._resizeOverlayBind);
//          Event.observe(window, "orientationchange", _me._resizeOverlayBind);
//        }
//        htmlElem.observe('click', _me._openInOverlayClickHandlerBind);
//        htmlElem.observe('cel_ImageSlideShow:startSlideShow', _me._openInOverlayBind);
//        $(document.body).observe('cel_yuiOverlay:loadFirstContent',
//            _me._imageSlideShowLoadFirstContentBind);
//        $(document.body).observe('cel_yuiOverlay:afterShowDialog_General',
//            _me._resizeOverlayBind);
//        $(document.body).fire('cel_ImageSlideShow:finishedRegister', _me);
      }

  };
})();

//////////////////////////////////////////////////////////////////////////////
// image SlideShow container in a rich text context (inline)
//////////////////////////////////////////////////////////////////////////////
window.CELEMENTS.image.InlineContainer = function(htmlElem) {
  // constructor
  this._init(htmlElem);
};

(function() {
  "use strict";

  CELEMENTS.image.InlineContainer.prototype = {
      _htmlElemId : undefined,
      _configReader : undefined,
      _containerHtmlElem : undefined,
      _replaceElemHandlerBind : undefined,
      _imageSlideShowObj : undefined,
      _origStyleValues : undefined,

      /**
       * Read all configuration information from the original element.
       * It might be replaced/changed later on and the configuration
       * information might geht lost. Only keep the htmlElem.id because
       * the element might get replaced.
       */
      _init : function(htmlElem) {
        var _me = this;
        _me._htmlElemId = htmlElem.id;
        _me._configReader = new CELEMENTS.image.ConfigReader(htmlElem);
        _me._replaceElemHandlerBind = _me._replaceElemHandler.bind(_me);
        $(document.body).observe('celimage_slideshowstarter:replaceElem',
            _me._replaceElemHandlerBind);
      },

      _getHtmlElem : function() {
        var _me = this;
        return $(_me._htmlElemId);
      },

      /**
       * important that the start happens before document.ready to allow the slideshow
       * context menu being loaded
       */
      _replaceElemHandler : function(event) {
        var _me = this;
        var parentElem = event.memo;
        if (_me._getHtmlElem().descendantOf(parentElem)) {
          $(document.body).stopObserving('celimage_slideshowstarter:replaceElem',
              _me._replaceElemHandlerBind);
          _me.install();
        }
      },

      /** before: startNonOverlaySlideShow **/
      install : function() {
        var _me = this;
        _me._wrapSplashImage();
        _me._imageSlideShowObj = new CELEMENTS.image.SlideShow({
          'configReader' : _me._configReader,
          'containerHtmlElem' : _me._containerHtmlElem
        });
        _me._imageSlideShowObj.start();
      },

      _isSplashImageNotYetWrapped : function() {
        var _me = this;
        var slideShowImg = $(_me._htmlElemId);
        return !_me._containerHtmlElem && (slideShowImg.tagName.toLowerCase() == 'img');
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

      _getStartImage : function(slideShowImg) {
        var _me = this;
        if (_me._configReader.hasCustomStart()) {
          return slideShowImg;
        } else {
          var loadingImg = new Image();
          loadingImg.src = '/file/resources/celRes/ajax-loader.gif';
          loadingImg.height = 32;
          loadingImg.width = 32;
          loadingImg.setStyle({
            'display' : 'block',
            'marginLeft' : 'auto',
            'marginRight' : 'auto',
            'position' : 'relative',
            'top' : '48%'
          });
          slideShowImg.replace(loadingImg);
          return loadingImg;
        }
      },

      _wrapSplashImage : function() {
        var _me = this;
        var slideShowImg = $(_me._htmlElemId);
        if (_me._isSplashImageNotYetWrapped()) {
          var otherCssClassNames = $w(slideShowImg.className).without('celimage_slideshow'
              ).without('highslide-image');
          var divInnerWrapper = _me._getStartImage(slideShowImg).wrap('div', {
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
          divWrapper.setStyle({
            'height' : _me._configReader.getContainerAnimHeight() + 'px',
            'width' : _me._configReader.getContainerAnimWidth() + 'px'
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
          _me._containerHtmlElem = divWrapper;
          //XXX still in the right place!?!?
          slideShowImg.fire('celimage_slideshow:afterInit', _me);
//          console.log('_initNonOverlaySlideShow: for wrapper ', slideShowImg);
        } else if (!_me._containerHtmlElem) {
          _me._containerHtmlElem = slideShowImg;
          //XXX still in the right place!?!?
          slideShowImg.fire('celimage_slideshow:afterInit', _me);
        } else {
          if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
            console.log('skipping creating wrapperHTMLElem because already exists.',
                _me._containerHtmlElem);
          }
        }
      }

  };
})();

//////////////////////////////////////////////////////////////////////////////
//image SlideShow logic independent of container
//////////////////////////////////////////////////////////////////////////////
window.CELEMENTS.image.SlideShow = function(config) {
  // constructor
  this._init(config);
};

(function() {
  "use strict";

  CELEMENTS.image.SlideShow.prototype = {
      _configReader : undefined,
      _config : undefined,
      _celSlideShowObj : undefined,
      _startStopClickHandlerBind : undefined,
      _addNavigationButtonsBind : undefined,
      _addSlideShowCounterBind : undefined,

      _init : function(config) {
        var _me = this;
        _me._config = config;
        _me._configReader = config.configReader;
        _me._getCelSlideShowObj()._htmlContainer = _me.getContainerElement();
        _me._startStopClickHandlerBind = _me._startStopClickHandler.bind(_me);
        _me._addNavigationButtonsBind = _me._addNavigationButtons.bind(_me);
        _me._addSlideShowCounterBind = _me._addSlideShowCounter.bind(_me);
      },

      _getContainerElemId : function() {
        var _me = this;
        return _me._config.containerHtmlElem.id;
      },

      getContainerElement : function() {
        var _me = this;
        return _me._config.containerHtmlElem;
      },

      _getCelSlideShowObj : function(overwriteLayout) {
        var _me = this;
        if (!_me._celSlideShowObj) {
          _me._celSlideShowObj = new CELEMENTS.presentation.SlideShow(
              _me._getContainerElemId());
          overwriteLayout = overwriteLayout || 'SimpleLayout';
          _me._celSlideShowObj.setOverwritePageLayout(overwriteLayout);
        } else if (overwriteLayout) {
          _me._celSlideShowObj.setOverwritePageLayout(overwriteLayout);
        }
        return _me._celSlideShowObj;
      },

      _addSlideShowCounter : function(event) {
        var _me = this;
//        var slideWrapperElem = event.memo.newSlideWrapperElem;
        if (!_me._configReader.hasAddCounterNone()) {
          if (!_me.getContainerElement().down('> div.celPresSlideShow_countSlideNum')) {
            var countSlideNumElem = new Element('div').addClassName(
                'celPresSlideShow_countSlideNum');
            _me.getContainerElement().insert({'bottom' : countSlideNumElem});
          }
          if (!_me.getContainerElement().down('> div.celPresSlideShow_currentSlideNum')) {
            var currentSlideNumElem = new Element('div').addClassName(
                'celPresSlideShow_currentSlideNum');
            _me.getContainerElement().insert({'bottom' : currentSlideNumElem});
          }
        }
      },

      _addNavigationButtons : function(event) {
        var _me = this;
        if (_me._configReader.hasAddNavigation()) {
          if (!_me.getContainerElement().down('> div.celPresSlideShow_next')) {
            var nextButton = new Element('div').addClassName('celPresSlideShow_next');
            _me.getContainerElement().insert({'bottom' : nextButton});
          }
          if (!_me.getContainerElement().down('> div.celPresSlideShow_prev')) {
            var prevButton = new Element('div').addClassName('celPresSlideShow_prev');
            _me.getContainerElement().insert({'top' : prevButton});
          }
        }
      },

      _imageSlideShowLoadFirstContent_internal : function() {
        var _me = this;
        _me.getContainerElement().observe(
            'cel_yuiOverlay:afterContentChanged', _me._addNavigationButtonsBind);
        _me.getContainerElement().observe(
            'cel_yuiOverlay:beforeSlideInsert', _me._addSlideShowCounterBind);
        if (_me._configReader.hasCustomStart()) {
          var navObj = _me._getCelSlideShowObj()._navObj;
          _me._getCelSlideShowObj().loadAndAddMainSlides(
              _me._configReader.getGallerySpace(), function(allSlides) {
                navObj._nextIndex = _me._configReader.getStartSlideNum();
                navObj._preloadFunc(allSlides[navObj._nextIndex],
                    navObj._updateNextContent.bind(navObj));
              });
        } else {
          _me._getCelSlideShowObj().loadMainSlides(_me._configReader.getGallerySpace(),
              _me._configReader.getStartSlideNum());
        }
        _me._initAnimation();
      },

      _initAnimation : function(event) {
        var _me = this;
        if (typeof CELEMENTS.presentation.SlideShowAnimation != 'undefined') {
          _me._slideShowAnimation = new CELEMENTS.presentation.SlideShowAnimation(
              _me._getCelSlideShowObj(), _me._configReader.getTimeout(),
              _me._configReader.getSlideShowEffect());
          if (_me._configReader.hasAnimation()) {
            _me._slideShowAnimation.register();
            var isAutoStart = (_me._configReader.getStartMode() == 'auto');
            _me._initManualStartButton();
            _me.startStop(isAutoStart, true);
          }
        }
      },

      _initManualStartButton : function() {
        var _me = this;
        var startButtonDiv = new Element('div', { 'class' : 'slideshowButton' });
        startButtonDiv.hide();
        _me.getContainerElement().insert({ bottom : startButtonDiv });
        $(_me.getContainerElement()).stopObserving('click', _me._startStopClickHandlerBind);
        $(_me.getContainerElement()).observe('click', _me._startStopClickHandlerBind);
      },

      _startStopClickHandler : function(event) {
        var _me = this;
        event.stop();
        _me.startStop();
      },

      startStop : function(isStart, delayedStart) {
        var _me = this;
        if (typeof isStart === 'undefined') {
          isStart = _me._slideShowAnimation._paused;
        }
        if (typeof delayedStart === 'undefined') {
          delayedStart = false;
        }
        var slideShowButton = _me.getContainerElement().down('.slideshowButton');
        if (isStart) {
          if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
            console.log('animation started for image slideshow',
                _me._getContainerElemId());
          }
          _me._slideShowAnimation.startAnimation(delayedStart);
          if (slideShowButton) {
            Effect.Fade(slideShowButton, { duration : 1.0 });
          }
        } else {
          if ((typeof console != 'undefined') && (typeof console.log != 'undefined')) {
            console.log('animation stopped for image slideshow',
                _me._getContainerElemId());
          }
          _me._slideShowAnimation.stopAnimation();
          if (slideShowButton) {
            Effect.Appear(slideShowButton, { duration : 1.0 , to : 0.9 });
          }
        }
      },

      start : function() {
        var _me = this;
        _me._getCelSlideShowObj().setAutoresize(true);
        _me._getCelSlideShowObj().register();
        _me._imageSlideShowLoadFirstContent_internal();
      }

  };
})();

//////////////////////////////////////////////////////////////////////////////
//image SlideShow config reader
//////////////////////////////////////////////////////////////////////////////
window.CELEMENTS.image.ConfigReader = function(htmlElem, configDef) {
  // constructor
  var configDefParam = configDef || {};
  this._init(htmlElem, configDefParam);
};

(function() {
  "use strict";

  CELEMENTS.image.ConfigReader.prototype = {
      _htmlElemId : undefined,
      _htmlElemClasses : undefined,
      _configDef : undefined,
      _containerAnimWidth : undefined,
      _containerAnimHeight : undefined,
  
      _init : function(htmlElem, configDef) {
        var _me = this;
        _me._htmlElemId = htmlElem.id;
        _me._htmlElemClasses = $w(htmlElem.className);
        _me._configDef = new Hash({
          'manualstart' : 'celimage_manualstart',
          'nonestart' : 'celimage_nonestart',
          'autostart' : 'celimage_autostart',
          'addNavigation' : 'celimage_addNavigation',
          'addCounterNone' : 'celimage_addCounterNone',
          'randomStart' : 'celimage_slideshowRandomStart',
          'customStart' : 'celimage_customStartSlide'
         }).update(configDef).toObject();
        _me._containerAnimWidth = _me._getPart(8, htmlElem.getWidth());
        _me._containerAnimHeight = _me._getPart(9, htmlElem.getHeight());
      },

      _getPart : function(num, defaultvalue) {
        var _me = this;
        var parts = _me._htmlElemId.split(':');
        if ((num < parts.length) && (parts[num] != '')) {
          return parts[num];
        } else {
          return defaultvalue;
        }
      },

      _hasClassName : function(className) {
        var _me = this;
        return (_me._htmlElemClasses.indexOf(className) > -1);
      },

      getStartMode : function() {
        var _me = this;
        if (_me._hasClassName(_me._configDef.manualstart)) {
          return 'manual';
        } else if (_me._hasClassName(_me._configDef.nonestart)) {
          return 'none';
        } else if (_me._hasClassName(_me._configDef.autostart)) {
          return 'auto';
        }
        return 'auto';
      },

      hasAnimation : function() {
        var _me = this;
        return (_me.getStartMode() != 'none');
      },

      getContainerAnimWidth : function() {
        var _me = this;
        if ((_me._containerAnimWidth == '') || isNaN(parseInt(_me._containerAnimWidth))
            || (parseInt(_me._containerAnimWidth) <= 0)) {
          _me._containerAnimWidth = _me._getPart(8, htmlElem.getWidth());
        }
        return parseInt(_me._containerAnimWidth);
      },

      getContainerAnimHeight : function() {
        var _me = this;
        if ((_me._containerAnimHeight == '') || isNaN(parseInt(_me._containerAnimHeight))
            || (parseInt(_me._containerAnimHeight) <= 0)) {
          _me._containerAnimHeight = _me._getPart(9, htmlElem.getHeight());
        }
        return parseInt(_me._containerAnimHeight);
      },

      hasRandomStart : function() {
        var _me = this;
        return _me._hasClassName(_me._configDef.randomStart);
      },

      hasCustomStart : function() {
        var _me = this;
        return _me._hasClassName(_me._configDef.customStart);
      },

      getGallerySpace : function() {
        var _me = this;
        return _me._getPart(7, '');
      },

      getSlideShowEffect : function() {
        var _me = this;
        return _me._getPart(3, 'none');
      },

      getStartSlideNum : function() {
        var _me = this;
        return parseInt(_me._getPart(6, 1)) - 1;
      },

      getTimeout : function() {
        var _me = this;
        return _me._getPart(2, 3);
      },

      hasAddNavigation : function() {
        var _me = this;
        return _me._hasClassName(_me._configDef.addNavigation);
      },

      hasAddCounterNone : function() {
        var _me = this;
        return _me._hasClassName(_me._configDef.addCounterNone);
      }

  };
})();

})(window);
