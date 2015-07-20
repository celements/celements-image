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

  window.celOnBeforeInitializeSlideShowListenerArray = [];

  window.celAddOnBeforeInitializeSlideShowListener = function(listenerFunc) {
    window.celOnBeforeInitializeSlideShowListenerArray.push(listenerFunc);
  };

  window.celBeforeInitializeSlideShowHandler = function() {
    $A(window.celOnBeforeInitializeSlideShowListenerArray).each(function(listener) {
      try {
        listener();
      } catch (exp) {
        console.error('Failed to execute celOnBeforeInitializeSlideShow listener. ', exp);
      }
    });
  };

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
    window.celBeforeInitializeSlideShowHandler();
    var initEvent = $(document.body).fire('celimage_slideshowstarter:beforeAutoInitSlideShow');
    if (!initEvent.stopped) {
      window.CELEMENTS.image.getBodySlideShowStarter().initializeSlideShow();
    }
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
      _debug : undefined,
  
      _init : function(htmlElem) {
        var _me = this;
        _me._parentElem = htmlElem || $(document.body);
      },
  
      initializeOverlayImageSlideShow : function() {
        var _me = this;
        _me._parentElem.select('.celimage_slideshow').each(function(slideShowElem) {
          if (slideShowElem.hasClassName('celimage_overlay')
              && !slideShowElem.hasClassName('celimage_overlay_initalized')) {
            if (_me._debug) {
              console.log('overlay image initialization for ', slideShowElem.id);
            }
            var overlayContainerObj = new CELEMENTS.image.OverlayContainer(slideShowElem);
            overlayContainerObj._getHtmlElem().addClassName(
                'celimage_overlay_initalized');
            CISS_SlideShowOverlayObjHash.set(slideShowElem.id, overlayContainerObj);
          } else if (_me._debug
              && slideShowElem.hasClassName('celimage_overlay_initalized')) {
            console.log('skip double initialization for ', slideShowElem.id);
          }
        });
      },
  
      initializeImageSlideShow : function() {
        var _me = this;
        _me._parentElem.select('.celimage_slideshow').each(function(slideShowElem) {
          if (!slideShowElem.hasClassName('celimage_inline_initalized')) {
            if (_me._debug) {
              console.log('inline image initialization for ', slideShowElem.id);
            }
            var inlineContainerObj = new CELEMENTS.image.InlineContainer(slideShowElem);
            inlineContainerObj._getHtmlElem().addClassName('celimage_inline_initalized');
            CISS_SlideShowObjHash.set(slideShowElem.id, inlineContainerObj);
          } else if (_me._debug
              && slideShowElem.hasClassName('celimage_inline_initalized')) {
            console.log('skip double initialization for ', slideShowElem.id);
          }
        });
      },
      
      initializeSlideShow : function() {
        var _me = this;
        $(document.body).fire('celimage_slideshowstarter:beforeInitializeSlideShow',
            _me._parentElem);
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
      _containerHtmlElem : undefined,
      _configReader : undefined,
      _resizeOverlayBind : undefined,
      _registerHandlerBind : undefined,
      _replaceNotifyHandlerBind : undefined,
      _openInOverlayClickHandlerBind : undefined,
      _imageSlideShowLoadFirstContentBind : undefined,
      _removeIsImageSlideShowOverlayBind : undefined,
      _checkIsImageSlideShowOverlayBind : undefined,
      _openInOverlayBind : undefined,
      _imageSlideShowObj : undefined,
      _isOverlayRegistered : undefined,
      _debug : undefined,

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
          'autostart' : 'celimage_overlayautostart',
          'autostartnostop' : 'celimage_overlayautostartnostop',
          'addNavigation' : 'celimage_addNavigationOverlay',
          'customStart' : 'celimage_customStartSlideOverlay',
          'addCounterNone' : 'celimage_addCounterOverlayNone',
          'addCounterZeros' : 'celimage_addCounterOverlayZeros'
        });
        _me._isOverlayRegistered = false;
        _me._resizeOverlayBind = _me._resizeOverlay.bind(_me);
        _me._replaceNotifyHandlerBind = _me._replaceNotifyHandler.bind(_me);
        _me._registerHandlerBind = _me._registerHandler.bind(_me);
        _me._openInOverlayClickHandlerBind = _me._openInOverlayClickHandler.bind(_me);
        _me._openInOverlayBind = _me.openInOverlay.bind(_me);
        _me._imageSlideShowLoadFirstContentBind =
          _me._imageSlideShowLoadFirstContent.bind(_me);
        _me._removeIsImageSlideShowOverlayBind = _me._removeIsImageSlideShowOverlay.bind(
            _me);
        _me._checkIsImageSlideShowOverlayBind = _me._checkIsImageSlideShowOverlay.bind(
            _me);
        $(document.body).observe('celimage_slideshow:replaceNotify',
            _me._replaceNotifyHandlerBind);
        $(document.body).observe('celimage_slideshowstarter:afterReplace',
            _me._registerHandlerBind);
      },

      _getHtmlElem : function() {
        var _me = this;
        return $(_me._htmlElemId);
      },

      _replaceNotifyHandler : function(event) {
        var _me = this;
        var replaceNotice = event.memo;
        if (replaceNotice.oldHtmlId == _me._htmlElemId) {
          _me._htmlElemId = replaceNotice.newHtmlId;
        }
      },

      _registerHandler : function(event) {
        var _me = this;
        var parentElem = event.memo;
        if (_me._getHtmlElem() && _me._getHtmlElem().descendantOf(parentElem)) {
          $(document.body).stopObserving('celimage_slideshowstarter:afterReplace',
              _me._registerHandlerBind);
          _me.register();
        }
      },

      /** before: registerOpenInOverlay **/
      /**
       * Register on element with same id as htmlElem.
       * The original element might be replaced in between.
       */
      register : function() {
        var _me = this;
        if (!_me._isOverlayRegistered) {
          _me._isOverlayRegistered = true;
          $(document.body).observe('cel_slideShow:shouldRegister',
              _me._checkIsImageSlideShowOverlayBind);
          $(document.body).observe('cel_yuiOverlay:hideEvent',
              _me._removeIsImageSlideShowOverlayBind);
        }
        _me._getHtmlElem().stopObserving('click', _me._openInOverlayClickHandlerBind);
        _me._getHtmlElem().observe('click', _me._openInOverlayClickHandlerBind);
        _me._getHtmlElem().stopObserving('cel_ImageSlideShow:startSlideShow',
            _me._openInOverlayBind);
        _me._getHtmlElem().observe('cel_ImageSlideShow:startSlideShow',
            _me._openInOverlayBind);
        $(document.body).stopObserving('cel_yuiOverlay:loadFirstContent',
            _me._imageSlideShowLoadFirstContentBind);
        $(document.body).observe('cel_yuiOverlay:loadFirstContent',
            _me._imageSlideShowLoadFirstContentBind);
        $(document.body).stopObserving('cel_yuiOverlay:afterShowDialog_General',
            _me._resizeOverlayBind);
        $(document.body).observe('cel_yuiOverlay:afterShowDialog_General',
            _me._resizeOverlayBind);
        $(document.body).fire('cel_ImageSlideShow:finishedRegister', _me);
      },

      /**
       * TODO move to celYuiOverlay
       */
      _resizeOverlay : function() {
        var _me = this;
        if (_me._configReader.isAutoResize()) {
          console.log('_resizeOverlay: start for ', _me._htmlElemId);
          var openDialog = CELEMENTS.presentation.getOverlayObj();
          var zoomFactor = _me._configReader.computeZoomFactor();
          if (zoomFactor <= 1) {
            var oldWidth = parseInt(openDialog.getWidth());
            var oldHeight = parseInt(openDialog.getHeight());
            var newHeight = oldHeight * zoomFactor;
            var newWidth = oldWidth * zoomFactor;
            var eventMemo = {
                'fullWidth' : oldWidth,
                'fullHeight' : oldHeight,
                'zoomFactor' : zoomFactor,
                'newWidth' : newWidth,
                'newHeight' : newHeight
            };
            if (_me._debug) {
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
              $('yuiOverlayContainer').setStyle(_me._configReader.getZoomStyles(
                  zoomFactor, oldWidth, oldHeight));
            }
          } else {
            if (_me._debug) {
              console.log('no resize needed.', zoomFactor);
            }
          }
          openDialog._overlayDialog.center();
        } else {
          console.log('skip ResizeOverlay in imageSlideShow.');
        }
      },

      _removeIsImageSlideShowOverlay : function() {
        var _me = this;
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        Event.stopObserving(window, "resize", _me._resizeOverlayBind);
        Event.stopObserving(window, "orientationchange", _me._resizeOverlayBind);
        openDialog.updateOpenConfig({ 'slideShowElem' : null });
      },

      _checkIsImageSlideShowOverlay : function(event) {
        var _me = this;
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        if (openDialog._dialogConfig.slideShowElem
            && (event.memo.slideShow._htmlContainerId === openDialog.getContainerId())) {
          Event.observe(window, "resize", _me._resizeOverlayBind);
          Event.observe(window, "orientationchange", _me._resizeOverlayBind);
          event.stop();
        }
      },

      _getImageSlideShowObj : function() {
        var _me = this;
        if (!_me._imageSlideShowObj) {
          _me._imageSlideShowObj = new CELEMENTS.image.SlideShow({
            'configReader' : _me._configReader,
            'containerHtmlElem' : _me._containerHtmlElem
          });
        }
        return _me._imageSlideShowObj;
      },

      _imageSlideShowLoadFirstContent : function(event) {
        var _me = this;
        var dialogConfig = event.memo;
        if (dialogConfig.slideShowElem && (dialogConfig.slideShowElem === _me)) {
          _me._containerHtmlElem = $('yuiOverlayContainer');
          console.log('_imageSlideShowLoadFirstContent: start ', _me._htmlElemId,
              $(dialogConfig.containerId), _me._containerHtmlElem);
          //updateContainerElement -> important on reopening overlay.
          _me._getImageSlideShowObj().updateContainerElement(_me._containerHtmlElem);
          _me._configReader.loadOverlayLayoutName(function(galleryLayoutName) {
            _me._getImageSlideShowObj().start();
          });
          event.stop();
        }
      },

      _openInOverlayClickHandler : function(event) {
        var _me = this;
        event.stop();
        _me.openInOverlay(event);
      },

      openInOverlay : function(event) {
        var _me = this;
        var startAtSlideName = event.memo;
        // event.memo is an empty object if prototypejs fire is used without
        // a memo object
        if (startAtSlideName && (typeof startAtSlideName !== 'object')) {
          _me._configReader.setStartSlideNum(startAtSlideName);
        }
        var hasCloseButton = _me._configReader.hasCloseButton();
        var openDialog = CELEMENTS.presentation.getOverlayObj({
          'close' : hasCloseButton,
          'slideShowElem' : _me,
          'link' : null,
          'suppressDimFromId' : true,
          'width' : _me._configReader.getOverlayWidth() + 'px',
          'height' : _me._configReader.getOverlayHeight() + 'px',
          'fixedcenter' : !_me._configReader.isAutoResize()
        });
        openDialog.intermediatOpenHandler();
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

  var LoadingImagesClass = function() {
    // constructor
    this._init();
  };

  LoadingImagesClass.prototype = {
      _loadingImg : undefined,
      _loadingSmallImg : undefined,

      _init : function() {
        var _me = this;

        _me._loadingImg = new Image();
        _me._loadingImg.src = '/file/resources/celRes/ajax-loader.gif';
        _me._loadingImg.height = 32;
        _me._loadingImg.width = 32;
        _me._loadingImg.addClassName('celLoadingIndicator');
        _me._loadingImg.setStyle({
          'display' : 'block',
          'marginLeft' : 'auto',
          'marginRight' : 'auto',
          'position' : 'relative',
          'top' : '48%'
        });
        
        _me._loadingSmallImg = new Image();
        _me._loadingSmallImg.src = '/file/resources/celRes/ajax-loader-small.gif';
        _me._loadingSmallImg.height = 16;
        _me._loadingSmallImg.width = 16;
        _me._loadingSmallImg.addClassName('celLoadingIndicator');
        _me._loadingSmallImg.setStyle({
          'display' : 'block',
          'marginLeft' : 'auto',
          'marginRight' : 'auto',
          'position' : 'relative',
          'top' : '48%'
        });
      },

      getLoadingImg : function() {
        var _me = this;
        return _me._loadingImg.cloneNode(true);
      },

      getSmallLoadingImg : function() {
        var _me = this;
        return _me._loadingSmallImg.cloneNode(true);
      },
  };

  var loadingImages = new LoadingImagesClass();
  
  CELEMENTS.image.InlineContainer.prototype = {
      _htmlElemId : undefined,
      _configReader : undefined,
      _containerHtmlElem : undefined,
      _replaceElemHandlerBind : undefined,
      _centerSplashImageBind : undefined,
      _imageSlideShowObj : undefined,
      _origStyleValues : undefined,
      _debug : undefined,

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
        _me._centerSplashImageBind = _me._centerSplashImage.bind(_me);
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
          _me._getHtmlElem().observe('celimage_slideshow:afterWrapSplashImage',
              _me._centerSplashImageBind);
          _me.install();
        }
      },

      _getImageSlideShowObj : function() {
        var _me = this;
        if (!_me._imageSlideShowObj) {
//          console.log('_getImageSlideShowObj create imageSlideShowObj for: ',
//              _me._containerHtmlElem);
          _me._imageSlideShowObj = new CELEMENTS.image.SlideShow({
            'configReader' : _me._configReader,
            'containerHtmlElem' : _me._containerHtmlElem
          });
        }
        return _me._imageSlideShowObj;
      },

      /** before: startNonOverlaySlideShow **/
      install : function() {
        var _me = this;
//        console.log('start install image slideshow ');
        _me._wrapSplashImage();
        if (!_me._configReader.hasCustomStart() || _me._configReader.hasAnimation()
            || _me._configReader.hasAddNavigation()) {
          _me._getImageSlideShowObj().start();
        } else {
            console.log('skipping init image slide show (no nav and anim) for ',
                _me._htmlElemId);
        }
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
        if (!_me._origStyleValues && htmlElement.getAttribute('style')) {
          var origStyles = new Hash();
          htmlElement.getAttribute('style').split(';').each(function(styleElem) {
            var styleElemSplit = styleElem.split(':');
            if (styleElemSplit.size() > 1) {
              origStyles.set(_me._camelCase(styleElemSplit[0].strip()),
                  styleElemSplit[1].strip());
            }
          });
          _me._origStyleValues = origStyles;
        } else if (!htmlElement.getAttribute('style')) {
          return new Hash();
        }
        return _me._origStyleValues;
      },

      _getStartImage : function(slideShowImg) {
        var _me = this;
        if (_me._configReader.hasCustomStart()) {
          return slideShowImg;
        } else {
          var loadingImg = loadingImages.getLoadingImg();
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
          if (_me._debug) {
            console.log('_wrapSplashImage: set width, height ', _me._htmlElemId,
                _me._configReader.getContainerAnimWidth(),
                _me._configReader.getContainerAnimHeight());
          }
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
          $(document.body).fire('celimage_slideshow:replaceNotify', {
            'oldHtmlId' : _me._htmlElemId,
            'newHtmlId' : _me._containerHtmlElem.id
          });
          slideShowImg.fire('celimage_slideshow:afterWrapSplashImage', _me);
        } else if (!_me._containerHtmlElem) {
          _me._containerHtmlElem = slideShowImg;
          slideShowImg.fire('celimage_slideshow:afterWrapSplashImage', _me);
        } else {
          console.log('skipping creating wrapperHTMLElem because already exists.',
              _me._containerHtmlElem);
        }
      },

      _getPrecomputedZoomFactor : function(slideWrapper, inSlideWrapperStyles) {
        var _me = this;
        var slideWrapperStyles = inSlideWrapperStyles;
        if (!slideWrapperStyles) {
          _me._origStyleValues = null;
          slideWrapperStyles = _me._getOriginalStyleValues(slideWrapper);
        }
        var zoomFactor = slideWrapperStyles.get('zoom') || slideWrapperStyles.get(
            'MsZoom') || slideWrapperStyles.get('transform');
        if (zoomFactor) {
          zoomFactor = zoomFactor.replace(/[^.0-9]*/g,'');
        }
        return zoomFactor;
      },

      _prepareCenterSplashImage : function() {
        var _me = this;
        var slideWrapper = _me._containerHtmlElem.down('.cel_slideShow_slideWrapper');
        var slideRoot = slideWrapper.up('.cel_slideShow_slideRoot');
        _me._origStyleValues = null;
        var slideWrapperStyles = _me._getOriginalStyleValues(slideWrapper);
        if (!slideWrapperStyles.get('height') || !slideWrapperStyles.get('width')) {
          var zoomFactor = _me._getPrecomputedZoomFactor(slideWrapper,
              slideWrapperStyles) || '1.0';
          if (_me._debug) {
          console.log('_prepareCenterSplashImage: precomputed zoomFactor ', zoomFactor,
              slideWrapper.getWidth(), slideWrapper.getHeight(),
              slideWrapperStyles.get('height'), slideWrapperStyles.get('width'));
          }
          //FF has problem in getting the right width for slideWrapper
          // if slideWrapper is in position: relative
          var thumbContainer = _me._containerHtmlElem.down(
              '.cel_slideShow_thumbContainer');
          if (thumbContainer) {
            thumbContainer.setStyle({
              'position' : '',
              'zoom' : '',
              'transform' : '',
              'height' : '',
              'width' : '',
              'top' : '',
              'left' : ''
             });
          }
          slideWrapper.setStyle({
            'position' : 'absolute',
            'zoom' : '1',
            'transform' : 'scale(1)'
           });
          if (_me._debug) {
            console.log('_prepareCenterSplashImage: set width and height ', _me._htmlElemId,
                slideWrapper.getWidth(), slideWrapper.getHeight());
          }
          slideRoot.setStyle({
            'height' : (zoomFactor * slideWrapper.getHeight()) + 'px',
            'width' : (zoomFactor * slideWrapper.getWidth()) + 'px'
          });
          var stylesProp = _me._configReader.getZoomStyles(zoomFactor,
              slideWrapper.getWidth(), slideWrapper.getHeight());
          stylesProp['position'] = 'relative';
          slideWrapper.setStyle(stylesProp);
        }
      },

      _showSplashImage : function() {
        var _me = this;
        var slideWrapper = _me._containerHtmlElem.down('.cel_slideShow_slideWrapper');
        var slideRoot = slideWrapper.up('.cel_slideShow_slideRoot');
        slideRoot.setStyle({
          'visibility' : ''
        });
        _me._containerHtmlElem.select('.celLoadingIndicator').each(function(loaderImg) {
          if (_me._debug) {
            console.log('_showSplashImage: remove loader image ', loaderImg.up());
          }
          loaderImg.remove();
        });
      },

      _centerSplashImage : function() {
        var _me = this;
        if (_me._configReader.isCenterSplashImage()) {
          var celSlideShowObj = _me._getImageSlideShowObj()._getCelSlideShowObj();
          //image gallery overview slides have precomputed resize factor
          var slideWrapper = _me._containerHtmlElem.down('.cel_slideShow_slideWrapper');
          var precomputedZoomFactor = _me._getPrecomputedZoomFactor(slideWrapper);
          if (_me._debug) {
            console.log('_centerSplashImage: before setResizeSlide false ',
                precomputedZoomFactor);
          }
          if (precomputedZoomFactor) {
            celSlideShowObj.setResizeSlide(false);
            celSlideShowObj.setAutoresize(false);
          }
          _me._containerHtmlElem.observe('cel_slideShow:beforeResizeAndCenterSlide',
              _me._prepareCenterSplashImage.bind(_me));
          _me._containerHtmlElem.observe('cel_slideShow:afterResizeAndCenterSlide',
              _me._showSplashImage.bind(_me));
          var slideWrapper = _me._containerHtmlElem.down('.cel_slideShow_slideWrapper');
          var slideRoot = slideWrapper.up('.cel_slideShow_slideRoot');
          slideRoot.setStyle({
            'visibility' : 'hidden'
          });
          _me._containerHtmlElem.insert({ 'top' : loadingImages.getSmallLoadingImg() });
          if (_me._debug) {
            console.log('before _preloadImagesAndResizeCenterSlide for ',
                _me._containerHtmlElem, slideWrapper);
          }
          celSlideShowObj._preloadImagesAndResizeCenterSlide(slideWrapper,
              function() {
            if (_me._debug) {
              console.log('finished center splash slide for ',
                  _me._containerHtmlElem);
            }
          });
        }
      },

      changeContainerSize : function(newMaxWidth, newMaxHeight) {
        var _me = this;
        _me._getImageSlideShowObj()._getCelSlideShowObj().changeContainerSize(newMaxWidth,
            newMaxHeight);
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
      _debug : undefined,

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

      updateContainerElement : function(newContainerElement) {
        var _me = this;
        _me._config.containerHtmlElem = newContainerElement;
        _me._getCelSlideShowObj()._htmlContainer = _me.getContainerElement();
      },

      getContainerElement : function() {
        var _me = this;
        return _me._config.containerHtmlElem;
      },

      _getCelSlideShowObj : function() {
        var _me = this;
        var overwriteLayout = _me._configReader.getLayoutName() || '';
        if (!_me._celSlideShowObj) {
          _me._celSlideShowObj = new CELEMENTS.presentation.SlideShow(
              _me._getContainerElemId());
          _me._celSlideShowObj.setOverwritePageLayout(overwriteLayout);
          _me._celSlideShowObj.setPreloadSlideAjaxMode('ImageSlideWithLayout');
          _me._celSlideShowObj.addPreloadSlideParam({
            'galleryFN' : _me._configReader.getGalleryFN()
          });
        } else if (overwriteLayout) {
          _me._celSlideShowObj.setOverwritePageLayout(overwriteLayout);
        }
        _me._celSlideShowObj.setCounterLeadingZeros(_me._configReader.hasLeadingZeros());
        return _me._celSlideShowObj;
      },

      /**
       * adds slide counter if image animation config asks for.
       */
      _addSlideShowCounter : function(event) {
        var _me = this;
//        var slideWrapperElem = event.memo.newSlideWrapperElem;
        if (_me._debug) {
          console.log('_addSlideShowCounter: ', !_me._configReader.hasAddCounterNone());
        }
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
        if (_me._debug) {
          console.log('_addNavigationButtons: ', _me._configReader.hasAddNavigation());
        }
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
        if (_me._debug) {
          console.log('_imageSlideShowLoadFirstContent_internal: register addNav'
              + ' and addSlideCounter ', _me.getContainerElement());
        }
        _me.getContainerElement().stopObserving(
            'cel_yuiOverlay:beforeSlideInsert', _me._addNavigationButtonsBind);
        _me.getContainerElement().observe(
            'cel_yuiOverlay:beforeSlideInsert', _me._addNavigationButtonsBind);
        _me.getContainerElement().stopObserving(
            'cel_yuiOverlay:beforeSlideInsert', _me._addSlideShowCounterBind);
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
          var startIndex = 0;
          if (_me._configReader.hasRandomStart()) {
            startIndex = '!RANDOM!';
          } else if (!isNaN(_me._configReader.getStartSlideNum())) {
            startIndex = _me._configReader.getStartSlideNum();
          }
          _me._getCelSlideShowObj().loadMainSlides(_me._configReader.getGallerySpace(),
            startIndex);
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
            if (_me._configReader.hasManualButton()) {
              _me._initManualStartButton();
            }
            _me.startStop(_me._configReader.hasAutoStart(), true);
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
          console.log('animation started for image slideshow',
              _me._getContainerElemId());
          _me._slideShowAnimation.startAnimation(delayedStart);
          if (slideShowButton) {
            Effect.Fade(slideShowButton, { duration : 1.0 });
          }
        } else {
          console.log('animation stopped for image slideshow',
              _me._getContainerElemId());
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
      _overlayWidthDefault : undefined,
      _overlayHeightDefault : undefined,
      _overlayWidth : undefined,
      _overlayHeight : undefined,
      _mobileDim : undefined,
      _isMobile : undefined,
      _autoresize : undefined,
      _startSlideNum : undefined,
      _galleryFN : undefined,
      _galleryObj : undefined,
      _layoutName : undefined,
      _centerSplashImage : undefined,

      _init : function(htmlElem, configDef) {
        var _me = this;
        _me._htmlElemId = htmlElem.id;
        _me._htmlElemClasses = $w(htmlElem.className);
        _me._mobileDim = new CELEMENTS.mobile.Dimensions(); 
        _me._isMobile = _me._mobileDim.isMobile;
        _me._autoresize = _me._isMobile.iOS() || _me._isMobile.Android();
        _me._overlayWidthDefault = 800;
        _me._overlayHeightDefault = 800;
        _me._configDef = new Hash({
          'manualstart' : 'celimage_manualstart',
          'nonestart' : 'celimage_nonestart',
          'autostart' : 'celimage_autostart',
          'autostartnostop' : 'celimage_autostartnostop',
          'addNavigation' : 'celimage_addNavigation',
          'addCounterNone' : 'celimage_addCounterNone',
          'randomStart' : 'celimage_slideshowRandomStart',
          'customStart' : 'celimage_customStartSlide',
          'addCloseButton' : 'celimage_overlay_addCloseButton',
          'addCounterZeros' : 'celimage_addCounterZeros',
          'forceAutoResize' : 'celimage_forceAutoResize'
         }).update(configDef).toObject();
        _me._galleryFN = _me._getPart(1, null);
        _me._overlayWidth = _me._getPart(4, _me._overlayWidthDefault);
        _me._overlayHeight = _me._getPart(5, _me._overlayHeightDefault);
        _me._containerAnimWidth = _me._getPart(8, htmlElem.getWidth());
        _me._containerAnimHeight = _me._getPart(9, htmlElem.getHeight());
        _me._centerSplashImage = true;
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

      getGalleryFN : function() {
        var _me = this;
        return _me._galleryFN;
      },

      isAutoResize : function() {
        var _me = this;
        return _me._autoresize || _me._hasClassName(_me._configDef.forceAutoResize);
      },

      isCenterSplashImage : function() {
        var _me = this;
        return _me._centerSplashImage;
      },

      setCenterSplashImage : function(isCenter) {
        var _me = this;
        _me._centerSplashImage = (isCenter == true);
      },

      getStartMode : function() {
        var _me = this;
        if (_me._hasClassName(_me._configDef.manualstart)) {
          return 'manual';
        } else if (_me._hasClassName(_me._configDef.nonestart)) {
          return 'none';
        } else if (_me._hasClassName(_me._configDef.autostart)) {
          return 'auto';
        } else if (_me._hasClassName(_me._configDef.autostartnostop)) {
          return 'autonostop';
        }
        return 'autonostop';
      },

      hasAnimation : function() {
        var _me = this;
        return (_me.getStartMode() != 'none');
      },

      hasAutoStart : function() {
        var _me = this;
        return (_me.getStartMode() == 'auto')
          || (_me.getStartMode() == 'autonostop');
      },

      hasLeadingZeros : function() {
        var _me = this;
        return _me._hasClassName(_me._configDef.addCounterZeros);
      },

      hasManualButton : function() {
        var _me = this;
        return (_me.getStartMode() != 'autonostop');
      },

      getContainerAnimWidth : function() {
        var _me = this;
        if ((_me._containerAnimWidth == '') || isNaN(parseInt(_me._containerAnimWidth))
            || (parseInt(_me._containerAnimWidth) <= 0)) {
          _me._containerAnimWidth = _me._getPart(8, $(_me._htmlElemId).getWidth());
        }
        return parseInt(_me._containerAnimWidth);
      },

      getContainerAnimHeight : function() {
        var _me = this;
        if ((_me._containerAnimHeight == '') || isNaN(parseInt(_me._containerAnimHeight))
            || (parseInt(_me._containerAnimHeight) <= 0)) {
          _me._containerAnimHeight = _me._getPart(9, $(_me._htmlElemId).getHeight());
        }
        return parseInt(_me._containerAnimHeight);
      },

      getOverlayWidth : function() {
        var _me = this;
        if ((_me._overlayWidth == '') || isNaN(parseInt(_me._overlayWidth))
            || (parseInt(_me._overlayWidth) <= 0)) {
          _me._overlayWidth = _me._getPart(4, _me._overlayWidthDefault);
        }
        return parseInt(_me._overlayWidth);
      },

      getOverlayHeight : function() {
        var _me = this;
        if ((_me._overlayHeight == '') || isNaN(parseInt(_me._overlayHeight))
            || (parseInt(_me._overlayHeight) <= 0)) {
          _me._overlayHeight = _me._getPart(5, _me._overlayHeightDefault);
        }
        return parseInt(_me._overlayHeight);
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

      setStartSlideNum : function(startSlideNum) {
        var _me = this;
        _me._startSlideNum = startSlideNum;
      },

      getStartSlideNum : function() {
        var _me = this;
        if (_me._startSlideNum) {
          return _me._startSlideNum;
        }
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

      hasCloseButton : function() {
        var _me = this;
        return _me._hasClassName(_me._configDef.addCloseButton);
      },

      hasAddCounterNone : function() {
        var _me = this;
        return _me._hasClassName(_me._configDef.addCounterNone);
      },

      _getGalleryObj : function(callbackFN) {
        var _me = this;
        if (!_me._galleryObj) {
          _me._galleryObj = new CELEMENTS.images.Gallery(
              _me._galleryFN, callbackFN, 0);

        } else {
          _me._galleryObj.executeAfterLoad(callbackFN, 0);
        }
      },

      loadOverlayLayoutName : function(callbackFN) {
        var _me = this;
        _me._getGalleryObj(function(galleryObj) {
          _me._layoutName = galleryObj.getLayoutName(); 
          if (callbackFN) {
            callbackFN(_me._layoutName);
          }
        });
      },

      getLayoutName : function() {
        var _me = this;
        return _me._layoutName;
      },

      getZoomStyles : function(zoomFactor, fullWidth, fullHeight) {
        var _me = this;
        return _me._mobileDim.getZoomStyles(zoomFactor, fullWidth, fullHeight);
      },

      computeZoomFactor : function() {
        var _me = this;
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        var oldWidth = parseInt(openDialog.getWidth());
        var newWidth = oldWidth;
        if (oldWidth > _me._mobileDim.getInnerWidth()) {
          newWidth = _me._mobileDim.getInnerWidth() - 20; // take care of close button
        }
        var zoomWidthFactor = newWidth / oldWidth;
        var oldHeight = parseInt(openDialog.getHeight());
        var newHeight = oldHeight;
        if (oldHeight > _me._mobileDim.getInnerHeight()) {
          newHeight = _me._mobileDim.getInnerHeight() - 20; // take care of close button
        }
        var zoomHeightFactor = newHeight / oldHeight;
        var zoomFactor;
        if (zoomHeightFactor < zoomWidthFactor) {
          zoomFactor = zoomHeightFactor;
        } else {
          zoomFactor = zoomWidthFactor;
        }
        console.log('imageSlideShow: computeZoomFactor ', zoomFactor, newWidth,
            newHeight, _me._mobileDim.getInnerWidth(), _me._mobileDim.getInnerHeight());
        return zoomFactor;
      }

  };
})();

})(window);
