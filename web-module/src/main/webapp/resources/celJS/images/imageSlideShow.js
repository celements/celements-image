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
(function (window, undefined) {
  "use strict";

  /**
   * Celements image Slideshow
   * This is the Celements image Slideshow controller.
   */
  if (typeof window.CELEMENTS == "undefined") { window.CELEMENTS = {}; }
  if (typeof CELEMENTS.image == "undefined") { window.CELEMENTS.image = {}; }

  let CISS_BodySlideShowStarter = undefined;
  const CISS_SlideShowObjHash = new Hash();
  const CISS_SlideShowOverlayObjHash = new Hash();

  window.celOnBeforeInitializeSlideShowListenerArray = [];

  window.celAddOnBeforeInitializeSlideShowListener = function (listenerFunc) {
    window.celOnBeforeInitializeSlideShowListenerArray.push(listenerFunc);
  };

  window.celBeforeInitializeSlideShowHandler = function () {
    $A(window.celOnBeforeInitializeSlideShowListenerArray).each(function (listener) {
      try {
        listener();
      } catch (exp) {
        console.error('Failed to execute celOnBeforeInitializeSlideShow listener. ', exp);
      }
    });
  };

  window.CELEMENTS.image.getSlideShowObj = function (slideShowElemId) {
    return CISS_SlideShowObjHash.get(slideShowElemId);
  };

  window.CELEMENTS.image.getSlideShowOverlayObj = function (slideShowElemId) {
    return CISS_SlideShowOverlayObjHash.get(slideShowElemId);
  };

  window.CELEMENTS.image.getBodySlideShowStarter = function () {
    if (!CISS_BodySlideShowStarter) {
      CISS_BodySlideShowStarter = new CELEMENTS.image.SlideShowStarter($(document.body));
    }
    return CISS_BodySlideShowStarter;
  };

  window.celAddOnBeforeLoadListener(function () {
    window.celBeforeInitializeSlideShowHandler();
    const initCelEvent = window.CELEMENTS.image.getBodySlideShowStarter().celFire(
      'celimage_slideshowstarter:beforeAutoInitSlideShow');
    const initEvent = $(document.body).fire('celimage_slideshowstarter:beforeAutoInitSlideShow');
    if (!initCelEvent.stopped && !initEvent.stopped) {
      window.CELEMENTS.image.getBodySlideShowStarter().initializeSlideShow();
    }
  });

  //////////////////////////////////////////////////////////////////////////////
  // Celements image Slideshow
  //////////////////////////////////////////////////////////////////////////////
  if (typeof window.CELEMENTS.image.SlideShowStarter === 'undefined') {
    window.CELEMENTS.image.SlideShowStarter = Class.create({
      _parentElem: undefined,
      _debug: undefined,

      initialize: function (htmlElem) {
        const _me = this;
        _me._debug = true;
        _me._parentElem = htmlElem || $(document.body);
      },

      initializeOverlayImageSlideShow: function () {
        const _me = this;
        _me._parentElem.select('.celimage_slideshow').each(function (slideShowElem) {
          if (slideShowElem.hasClassName('celimage_overlay')
            && !slideShowElem.hasClassName('celimage_overlay_initalized')) {
            if (_me._debug) {
              console.log('overlay image initialization for ', slideShowElem.id);
            }
            const overlayContainerObj = new CELEMENTS.image.OverlayContainer(slideShowElem, _me);
            overlayContainerObj._getHtmlElem().addClassName(
              'celimage_overlay_initalized');
            CISS_SlideShowOverlayObjHash.set(slideShowElem.id, overlayContainerObj);
          } else if (_me._debug
            && slideShowElem.hasClassName('celimage_overlay_initalized')) {
            console.log('skip double initialization for ', slideShowElem.id);
          }
        });
      },

      initializeImageSlideShow: function () {
        const _me = this;
        _me._parentElem.select('.celimage_slideshow').each(function (slideShowElem) {
          if (!slideShowElem.hasClassName('celimage_inline_initalized')) {
            if (_me._debug) {
              console.log('inline image initialization for ', slideShowElem.id);
            }
            const inlineContainerObj = new CELEMENTS.image.InlineContainer(slideShowElem, _me);
            inlineContainerObj._debug = true;
            inlineContainerObj._getHtmlElem().addClassName('celimage_inline_initalized');
            CISS_SlideShowObjHash.set(slideShowElem.id, inlineContainerObj);
          } else if (_me._debug
            && slideShowElem.hasClassName('celimage_inline_initalized')) {
            console.log('skip double initialization for ', slideShowElem.id);
          }
        });
      },

      initializeSlideShow: function () {
        const _me = this;
        _me.celFire('celimage_slideshowstarter:beforeInitializeSlideShow', _me._parentElem);
        $(document.body).fire('celimage_slideshowstarter:beforeInitializeSlideShow',
            _me._parentElem);
        _me.initializeImageSlideShow();
        _me.initializeOverlayImageSlideShow();
        $(document.body).fire('celimage_slideshowstarter:replaceElem', _me._parentElem);
        _me.celFire('celimage_slideshowstarter:replaceElem', _me._parentElem);
        $(document.body).fire('celimage_slideshowstarter:afterReplace', _me._parentElem);
        _me.celFire('celimage_slideshowstarter:afterReplace', _me._parentElem);
        $(document.body).fire('celimage_slideshowstarter:afterInit', _me._parentElem);
        _me.celFire('celimage_slideshowstarter:afterInit', _me._parentElem);
      },

      autoRegisterOnSlides: function () {
        const _me = this;
        $(document.body).observe('cel_slideShow:registerAfterContentChanged',
          _me.registerAfterContentChangedListener.bind(_me));
      },

      registerAfterContentChangedListener: function (event) {
        const htmlContainer = event.memo;
        if (htmlContainer) {
        const registerStoppCelEvent = _me.celFire('celimage_slideshow:shouldAutoRegister',
            htmlContainer);
        const registerStoppEvent = $(document.body).fire('celimage_slideshow:shouldAutoRegister',
            htmlContainer);
          if (registerStoppCelEvent.stopped || registerStoppEvent.stopped) {
            const imgSlideShowStarter = new CELEMENTS.image.SlideShowStarter(htmlContainer);
            imgSlideShowStarter.initializeSlideShow();
          }
        }
      }

    });
    CELEMENTS.image.SlideShowStarter.prototype = Object.extend(
      CELEMENTS.image.SlideShowStarter.prototype, CELEMENTS.mixins.Observable);
  }

  //////////////////////////////////////////////////////////////////////////////
  // image SlideShow container in a celYuiOverlay
  //////////////////////////////////////////////////////////////////////////////
  if (typeof window.CELEMENTS.image.OverlayContainer === 'undefined') {
    window.CELEMENTS.image.OverlayContainer = Class.create({
      _htmlElemId: undefined,
      _containerHtmlElem: undefined,
      _configReader: undefined,
      _resizeOverlayBind: undefined,
      _registerHandlerBind: undefined,
      _replaceNotifyHandlerBind: undefined,
      _openInOverlayClickHandlerBind: undefined,
      _imageSlideShowLoadFirstContentBind: undefined,
      _removeIsImageSlideShowOverlayBind: undefined,
      _checkIsImageSlideShowOverlayBind: undefined,
      _openInOverlayBind: undefined,
      _imageSlideShowObj: undefined,
      _isOverlayRegistered: undefined,
      _starterObj: undefined,
      _debug: undefined,

      /**
       * Read all configuration information from the original element.
       * It might be replaced/changed later on and the configuration
       * information might geht lost. Only keep the htmlElem.id because
       * the element might get replaced.
       */
      initialize: function (htmlElem, starterObj) {
        const _me = this;
        _me._htmlElemId = htmlElem.id;
        _me._starterObj = starterObj;
        _me._configReader = new CELEMENTS.image.ConfigReader(htmlElem, {
          'manualstart': 'celimage_overlaymanualstart',
          'nonestart': 'celimage_overlaynonestart',
          'autostart': 'celimage_overlayautostart',
          'autostartnostop': 'celimage_overlayautostartnostop',
          'addNavigation': 'celimage_addNavigationOverlay',
          'customStart': 'celimage_customStartSlideOverlay',
          'addCounterNone': 'celimage_addCounterOverlayNone',
          'addCounterZeros': 'celimage_addCounterOverlayZeros'
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
        _me._starterObj.celObserve('celimage_slideshowstarter:afterReplace',
          _me._registerHandlerBind);
      },

      _getHtmlElem: function () {
        const _me = this;
        return $(_me._htmlElemId);
      },

      _replaceNotifyHandler: function (event) {
        const _me = this;
        const replaceNotice = event.memo;
        if (replaceNotice.oldHtmlId == _me._htmlElemId) {
          _me._htmlElemId = replaceNotice.newHtmlId;
        }
      },

      _registerHandler: function (event) {
        const _me = this;
        const parentElem = event.memo;
        if (_me._getHtmlElem() && _me._getHtmlElem().descendantOf(parentElem)) {
          _me._starterObj.celStopObserving('celimage_slideshowstarter:afterReplace',
            _me._registerHandlerBind);
          _me.register();
        }
      },

      /** before: registerOpenInOverlay **/
      /**
       * Register on element with same id as htmlElem.
       * The original element might be replaced in between.
       */
      register: function () {
        const _me = this;
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
      _resizeOverlay: function (event) {
        const _me = this;
        if (!_me._configReader.isAutoResize()) {
          console.log('skip ResizeOverlay in imageSlideShow.');
        } else if (!event || (event.memo.getDialogId() === 'modal dialog')) {
          console.log('_resizeOverlay: start for ', _me._htmlElemId);
          const openDialog = CELEMENTS.presentation.getOverlayObj();
          const zoomFactor = _me._configReader.computeZoomFactor();
          if (zoomFactor <= 1) {
            const oldWidth = parseInt(openDialog.getWidth());
            const oldHeight = parseInt(openDialog.getHeight());
            const newHeight = oldHeight * zoomFactor;
            const newWidth = oldWidth * zoomFactor;
            const eventMemo = {
              'fullWidth': oldWidth,
              'fullHeight': oldHeight,
              'zoomFactor': zoomFactor,
              'newWidth': newWidth,
              'newHeight': newHeight
            };
            if (_me._debug) {
              console.log('final resize factor: ', eventMemo);
            }
            $(document.body).fire('cel_imageSlideShow:beforeResizeDialog_General', eventMemo);
            openDialog._overlayDialog.cfg.setProperty('width', newWidth + 'px');
            openDialog._overlayDialog.cfg.setProperty('height', newHeight + 'px');
            $(document.body).fire('cel_imageSlideShow:afterResizeDialog_General', eventMemo);
            const resizeEvent = $('yuiOverlayContainer').fire(
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
        }
      },

      _removeIsImageSlideShowOverlay: function () {
        const _me = this;
        const openDialog = CELEMENTS.presentation.getOverlayObj();
        Event.stopObserving(window, "resize", _me._resizeOverlayBind);
        Event.stopObserving(window, "orientationchange", _me._resizeOverlayBind);
        openDialog.updateOpenConfig({
          'slideShowElem': null,
          'additionalCssClass': ''
        });
      },

      _checkIsImageSlideShowOverlay: function (event) {
        const _me = this;
        const openDialog = CELEMENTS.presentation.getOverlayObj();
        if (openDialog._dialogConfig.slideShowElem
          && (event.memo.slideShow._htmlContainerId === openDialog.getContainerId())) {
          Event.observe(window, "resize", _me._resizeOverlayBind);
          Event.observe(window, "orientationchange", _me._resizeOverlayBind);
          event.stop();
        }
      },

      _getImageSlideShowObj: function () {
        const _me = this;
        if (!_me._imageSlideShowObj) {
          _me._imageSlideShowObj = new CELEMENTS.image.SlideShow({
            'configReader': _me._configReader,
            'containerHtmlElem': _me._containerHtmlElem
          });
        }
        return _me._imageSlideShowObj;
      },

      _imageSlideShowLoadFirstContent: function (event) {
        const _me = this;
        const dialogConfig = event.memo;
        if (dialogConfig.slideShowElem && (dialogConfig.slideShowElem === _me)) {
          _me._containerHtmlElem = $('yuiOverlayContainer');
          console.log('_imageSlideShowLoadFirstContent: start ', _me._htmlElemId,
            $(dialogConfig.containerId), _me._containerHtmlElem);
          //updateContainerElement -> important on reopening overlay.
          _me._getImageSlideShowObj().updateContainerElement(_me._containerHtmlElem);
          _me._configReader.loadOverlayLayoutName(function (galleryLayoutName) {
            _me._getImageSlideShowObj().start();
          });
          event.stop();
        }
      },

      _openInOverlayClickHandler: function (event) {
        const _me = this;
        event.stop();
        _me.openInOverlay(event);
      },

      openInOverlay: function (event) {
        const _me = this;
        const startAtSlideName = event.memo;
        // event.memo is an empty object if prototypejs fire is used without
        // a memo object
        if (startAtSlideName && (typeof startAtSlideName !== 'object')) {
          _me._configReader.setStartSlideNum(startAtSlideName);
        }
        const hasCloseButton = _me._configReader.hasCloseButton();
        const openDialog = CELEMENTS.presentation.getOverlayObj({
          'close': hasCloseButton,
          'slideShowElem': _me,
          'link': null,
          'suppressDimFromId': true,
          'width': _me._configReader.getOverlayWidth() + 'px',
          'height': _me._configReader.getOverlayHeight() + 'px',
          'fixedcenter': !_me._configReader.isAutoResize(),
          'additionalCssClass': 'cel-ImageGalleryOverlay'
        });
        openDialog.intermediatOpenHandler();
      }
    });
  }

  //////////////////////////////////////////////////////////////////////////////
  // image SlideShow container in a rich text context (inline)
  //////////////////////////////////////////////////////////////////////////////
  let LoadingImagesClass = Class.create({
    _loadingImg: undefined,
    _loadingSmallImg: undefined,

    initialize: function () {
      const _me = this;

      _me._loadingImg = new Image();
      _me._loadingImg.src = CELEMENTS.getUtils().getPathPrefix() + '/file/resources/celRes/ajax-loader.gif';
      _me._loadingImg.height = 32;
      _me._loadingImg.width = 32;
      _me._loadingImg.addClassName('celLoadingIndicator');
      _me._loadingImg.setStyle({
        'display': 'block',
        'marginLeft': 'auto',
        'marginRight': 'auto',
        'position': 'relative',
        'top': '48%'
      });

      _me._loadingSmallImg = new Image();
      _me._loadingSmallImg.src = CELEMENTS.getUtils().getPathPrefix()
        + '/file/resources/celRes/ajax-loader-small.gif';
      _me._loadingSmallImg.height = 16;
      _me._loadingSmallImg.width = 16;
      _me._loadingSmallImg.addClassName('celLoadingIndicator');
      _me._loadingSmallImg.setStyle({
        'display': 'block',
        'marginLeft': 'auto',
        'marginRight': 'auto',
        'position': 'relative',
        'top': '48%'
      });
    },

    getLoadingImg: function () {
      const _me = this;
      return _me._loadingImg.cloneNode(true);
    },

    getSmallLoadingImg: function () {
      const _me = this;
      return _me._loadingSmallImg.cloneNode(true);
    },
  });

  let loadingImages = new LoadingImagesClass();

  if (typeof window.CELEMENTS.image.InlineContainer === 'undefined') {
    window.CELEMENTS.image.InlineContainer = Class.create({
      _htmlElemId: undefined,
      _configReader: undefined,
      _containerHtmlElem: undefined,
      _replaceElemHandlerBind: undefined,
      _centerSplashImageBind: undefined,
      _responsiveResizeBind: undefined,
      _imageSlideShowObj: undefined,
      _origStyleValues: undefined,
      _starterObj : undefined,
      _debug: undefined,

      /**
       * Read all configuration information from the original element.
       * It might be replaced/changed later on and the configuration
       * information might geht lost. Only keep the htmlElem.id because
       * the element might get replaced.
       */
      initialize: function (htmlElem, starterObj) {
        const _me = this;
        _me._htmlElemId = htmlElem.id;
        _me._starterObj = starterObj;
        _me._configReader = new CELEMENTS.image.ConfigReader(htmlElem);
        _me._replaceElemHandlerBind = _me._replaceElemHandler.bind(_me);
        _me._centerSplashImageBind = _me._centerSplashImage.bind(_me);
        _me._responsiveResizeBind = _me._responsiveResize.bind(_me);
        _me._starterObj.celObserve('celimage_slideshowstarter:replaceElem',
           _me._replaceElemHandlerBind);
      },

      _getHtmlElem: function () {
        const _me = this;
        return $(_me._htmlElemId);
      },

      /**
       * important that the start happens before document.ready to allow the slideshow
       * context menu being loaded
       */
      _replaceElemHandler: function (event) {
        const _me = this;
        const parentElem = event.memo;
        if (_me._getHtmlElem().descendantOf(parentElem)) {
          _me._starterObj.celStopObserving('celimage_slideshowstarter:replaceElem',
            _me._replaceElemHandlerBind);
          _me._getHtmlElem().observe('celimage_slideshow:afterWrapSplashImage',
            _me._centerSplashImageBind);
          _me.install();
        }
      },

      _getImageSlideShowObj: function () {
        const _me = this;
        if (!_me._imageSlideShowObj) {
          //          console.log('_getImageSlideShowObj create imageSlideShowObj for: ',
          //              _me._containerHtmlElem);
          _me._imageSlideShowObj = new CELEMENTS.image.SlideShow({
            'configReader': _me._configReader,
            'containerHtmlElem': _me._containerHtmlElem
          });
        }
        return _me._imageSlideShowObj;
      },

      /** before: startNonOverlaySlideShow **/
      install: function () {
        const _me = this;
        //        console.log('start install image slideshow ');
        _me._wrapSplashImage();
        $(document.body).stopObserving('celements:delayedWindowResize', _me._responsiveResizeBind);
        $(document.body).observe('celements:delayedWindowResize', _me._responsiveResizeBind);
        if (!_me._configReader.hasCustomStart() || _me._configReader.hasAnimation()
          || _me._configReader.hasAddNavigation()) {
          _me._getImageSlideShowObj().start();
        } else {
          console.log('skipping init image slide show (no nav and anim) for ',
            _me._htmlElemId);
        }
      },

      _isSplashImageNotYetWrapped: function () {
        const _me = this;
        const slideShowImg = $(_me._htmlElemId);
        return !_me._containerHtmlElem && (slideShowImg.tagName.toLowerCase() == 'img');
      },

      _moveStyleToWrapper: function (divWrapper, element, styleName) {
        const _me = this;
        const newStyle = new Hash();
        let elemStyleValue = _me._getOriginalStyleValues(element).get(styleName);
        if (!elemStyleValue || (elemStyleValue == '')) {
          elemStyleValue = element.getStyle(styleName);
        }
        newStyle.set(styleName, elemStyleValue);
        divWrapper.setStyle(newStyle.toObject());
        newStyle.set(styleName, '');
        element.setStyle(newStyle.toObject());
      },

      _camelCase: function (input) {
        return input.toLowerCase().replace(/-(.)/g, function (match, group1) {
          return group1.toUpperCase();
        });
      },

      /**
       * _getOriginalStyleValues gets the original element styles opposed to getStyle
       * from prototype-js which gets the browser resolved style.
       *   e.g. for margin-left: auto _getOriginalStyleValues returns 'auto' yet
       *   protoptype-js returns the real pixel value.
       */
      _getOriginalStyleValues: function (htmlElement) {
        const _me = this;
        if (!_me._origStyleValues && htmlElement.getAttribute('style')) {
          const origStyles = new Hash();
          htmlElement.getAttribute('style').split(';').each(function (styleElem) {
            const styleElemSplit = styleElem.split(':');
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

      _getStartImage: function (slideShowImg) {
        const _me = this;
        if (_me._configReader.hasCustomStart()) {
          return slideShowImg;
        } else {
          const loadingImg = loadingImages.getLoadingImg();
          slideShowImg.replace(loadingImg);
          return loadingImg;
        }
      },

      _wrapSplashImage: function () {
        const _me = this;
        const slideShowImg = $(_me._htmlElemId);
        if (_me._isSplashImageNotYetWrapped()) {
          const otherCssClassNames = $w(slideShowImg.className).without('celimage_slideshow'
          ).without('highslide-image');
          const divInnerWrapper = _me._getStartImage(slideShowImg).wrap('div', {
            'id': ('slideWrapper_' + slideShowImg.id),
            'class': 'cel_slideShow_slideWrapper'
          }).setStyle({
            'position': 'relative'
          });
          //to allow propper scaling we need to add a slideRoot element
          const divSlideRoot = divInnerWrapper.wrap('div', {
            'id': ('slideRoot_' + slideShowImg.id),
            'class': 'cel_slideShow_slideRoot'
          });
          const divWrapper = divSlideRoot.wrap('div', {
            'id': ('slideContainer_' + slideShowImg.id),
            'class': 'celimage_slideshow_wrapper cel_cm_celimage_slideshow'
          }).setStyle({
            'position': 'relative'
          });
          if (_me._debug) {
            console.log('_wrapSplashImage: set width, height ', _me._htmlElemId,
              _me._configReader.getContainerAnimWidth(),
              _me._configReader.getContainerAnimHeight());
          }
          divWrapper.setStyle({
            'height': _me._configReader.getContainerAnimHeight() + 'px',
            'width': _me._configReader.getContainerAnimWidth() + 'px'
          });
          otherCssClassNames.each(function (className) {
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
            'oldHtmlId': _me._htmlElemId,
            'newHtmlId': _me._containerHtmlElem.id
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

      _getPrecomputedZoomFactor: function (slideWrapper, inSlideWrapperStyles) {
        const _me = this;
        let slideWrapperStyles = inSlideWrapperStyles;
        if (!slideWrapperStyles) {
          _me._origStyleValues = null;
          slideWrapperStyles = _me._getOriginalStyleValues(slideWrapper);
        }
        let zoomFactor = slideWrapperStyles.get('transform');
        if (zoomFactor) {
          zoomFactor = zoomFactor.replace(/[^.0-9]*/g, '');
        }
        return zoomFactor;
      },

      _prepareCenterSplashImage: function () {
        const _me = this;
        const slideWrapper = _me._containerHtmlElem.down('.cel_slideShow_slideWrapper');
        const slideRoot = slideWrapper.up('.cel_slideShow_slideRoot');
        _me._origStyleValues = null;
        const slideWrapperStyles = _me._getOriginalStyleValues(slideWrapper);
        if (!slideWrapperStyles.get('height') || !slideWrapperStyles.get('width')) {
          const zoomFactor = _me._getPrecomputedZoomFactor(slideWrapper, slideWrapperStyles
            ) || '1.0';
          if (_me._debug) {
            console.log('_prepareCenterSplashImage: precomputed zoomFactor ', zoomFactor,
              slideWrapper.getWidth(), slideWrapper.getHeight(),
              slideWrapperStyles.get('height'), slideWrapperStyles.get('width'));
          }
          //FF has problem in getting the right width for slideWrapper
          // if slideWrapper is in position: relative
          const thumbContainer = _me._containerHtmlElem.down('.cel_slideShow_thumbContainer');
          if (thumbContainer) {
            thumbContainer.setStyle({
              'position': '',
              'transform': '',
              'height': '',
              'width': '',
              'top': '',
              'left': ''
            });
          }
          const resetStylesProp = _me._configReader.getZoomStyles(1, '', '');
          resetStylesProp['position'] = 'absolute';
          slideWrapper.setStyle(resetStylesProp);
          if (_me._debug) {
            console.log('_prepareCenterSplashImage: set width and height ', _me._htmlElemId,
              slideWrapper.getWidth(), slideWrapper.getHeight());
          }
          const rootHeight = (zoomFactor * slideWrapper.getHeight());
          const rootWidth = (zoomFactor * slideWrapper.getWidth());
          console.debug("_prepareCenterSplashImage: root height, width ", zoomFactor, rootHeight,
              rootWidth, slideRoot);
          slideRoot.setStyle({
            'height': rootHeight + 'px',
            'width': rootWidth + 'px'
          });
          const stylesProp = _me._configReader.getZoomStyles(zoomFactor,
            _me._configReader.getOverlayWidth(), _me._configReader.getOverlayHeight());
          stylesProp['position'] = 'relative';
          if (_me._debug) {
            console.debug('_prepareCenterSplashImage: stylesProperty ', _me._htmlElemId,
              stylesProp);
          }
          slideWrapper.setStyle(stylesProp);
        }
      },

      _showSplashImage: function () {
        const _me = this;
        const slideWrapper = _me._containerHtmlElem.down('.cel_slideShow_slideWrapper');
        const slideRoot = slideWrapper.up('.cel_slideShow_slideRoot');
        slideRoot.setStyle({
          'visibility': ''
        });
        _me._containerHtmlElem.select('.celLoadingIndicator').each(function (loaderImg) {
          if (_me._debug) {
            console.log('_showSplashImage: remove loader image ', loaderImg.up());
          }
          loaderImg.remove();
        });
      },

      _centerSplashImage: function () {
        const _me = this;
        if (_me._configReader.isCenterSplashImage()) {
          const celSlideShowObj = _me._getImageSlideShowObj()._getCelSlideShowObj();
          //image gallery overview slides have precomputed resize factor
          const slideWrapper = _me._containerHtmlElem.down('.cel_slideShow_slideWrapper');
          const precomputedZoomFactor = _me._getPrecomputedZoomFactor(slideWrapper);
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
          const slideRoot = slideWrapper.up('.cel_slideShow_slideRoot');
          slideRoot.setStyle({
            'visibility': 'hidden'
          });
          _me._containerHtmlElem.insert({ 'top': loadingImages.getSmallLoadingImg() });
          if (_me._debug) {
            console.log('before _preloadImagesAndResizeCenterSlide for ', _me._containerHtmlElem,
              slideWrapper);
          }
          celSlideShowObj._preloadImagesAndResizeCenterSlide(slideWrapper,
            function () {
              if (_me._debug) {
                console.log('finished center splash slide for ',
                  _me._containerHtmlElem);
              }
            });
        }
      },

      changeContainerSize: function (newMaxWidth, newMaxHeight) {
        const _me = this;
        _me._getImageSlideShowObj()._getCelSlideShowObj().changeContainerSize(newMaxWidth,
          newMaxHeight);
      },

      _removeSlideShowDimension: function () {
        const _me = this;
        console.log('_removeSlideShowDimension: ', _me._htmlElemId);
        if (_me._containerHtmlElem) {
          _me._containerHtmlElem.setStyle({
            'width': '',
            'height': ''
          });
          _me._containerHtmlElem.select('.cel_slideShow_slideRoot').each(
            function (slideRoot) {
              slideRoot.setStyle({
                'width': '',
                'height': '',
                'position': 'relative',
                'left': '0',
                'top': '0'
              });
            });
        } else {
          console.warn('_removeSlideShowDimension skipped, no container initialized');
        }
      },

      _responsiveResize: function () {
        const _me = this;
        _me._removeSlideShowDimension();
        let origWidth = _me._configReader.getContainerAnimWidth();
        let origHeight = _me._configReader.getContainerAnimHeight();
        let newWidth = _me._containerHtmlElem.getWidth();
        let newHeight = newWidth * origHeight / origWidth;
        _me.changeContainerSize(newWidth, newHeight);
      }

    });
  }

  //////////////////////////////////////////////////////////////////////////////
  //image SlideShow logic independent of container
  //////////////////////////////////////////////////////////////////////////////
  if (typeof window.CELEMENTS.image.SlideShow === 'undefined') {
    window.CELEMENTS.image.SlideShow = Class.create({
      _configReader: undefined,
      _config: undefined,
      _celSlideShowObj: undefined,
      _startStopClickHandlerBind: undefined,
      _addNavigationButtonsBind: undefined,
      _addSlideShowCounterBind: undefined,
      _debug: undefined,
      _menuDiv: undefined,
      _contextMenuSlideShowListItemClickedBind: undefined,
      _isPaused: undefined,
      _effect: undefined,

      initialize: function (config) {
        const _me = this;
        _me._config = config;
        _me._configReader = config.configReader;
        _me._getCelSlideShowObj()._htmlContainer = _me.getContainerElement();
        _me._startStopClickHandlerBind = _me._startStopClickHandler.bind(_me);
        _me._addNavigationButtonsBind = _me._addNavigationButtons.bind(_me);
        _me._addSlideShowCounterBind = _me._addSlideShowCounter.bind(_me);
        _me._contextMenuSlideShowListItemClickedBind =
          _me._contextMenuSlideShowListItemClicked.bind(_me);
      },

      _getContainerElemId: function () {
        const _me = this;
        return _me._config.containerHtmlElem.id;
      },

      updateContainerElement: function (newContainerElement) {
        const _me = this;
        _me._config.containerHtmlElem = newContainerElement;
        _me._getCelSlideShowObj()._htmlContainer = _me.getContainerElement();
      },

      getContainerElement: function () {
        const _me = this;
        return _me._config.containerHtmlElem;
      },

      _getCelSlideShowObj: function () {
        const _me = this;
        const overwriteLayout = _me._configReader.getLayoutName() || '';
        if (!_me._celSlideShowObj) {
          _me._celSlideShowObj = new CELEMENTS.presentation.SlideShow(
            _me._getContainerElemId());
          _me._celSlideShowObj.setOverwritePageLayout(overwriteLayout);
          _me._celSlideShowObj.setPreloadSlideAjaxMode('ImageSlideWithLayout');
          _me._celSlideShowObj.addPreloadSlideParam({
            'galleryFN': _me._configReader.getGalleryFN()
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
      _addSlideShowCounter: function (event) {
        const _me = this;
        if (_me._debug) {
          console.log('_addSlideShowCounter: ', !_me._configReader.hasAddCounterNone());
        }
        if (!_me._configReader.hasAddCounterNone()) {
          if (!_me.getContainerElement().down('> div.celPresSlideShow_countSlideNum')) {
            const countSlideNumElem = new Element('div').addClassName(
              'celPresSlideShow_countSlideNum');
            _me.getContainerElement().insert({ 'bottom': countSlideNumElem });
          }
          if (!_me.getContainerElement().down('> div.celPresSlideShow_currentSlideNum')) {
            const currentSlideNumElem = new Element('div').addClassName(
              'celPresSlideShow_currentSlideNum');
            _me.getContainerElement().insert({ 'bottom': currentSlideNumElem });
          }
        }
      },

      _addNavigationButtons: function (event) {
        const _me = this;
        if (_me._debug) {
          console.log('_addNavigationButtons: ', _me._configReader.hasAddNavigation());
        }
        if (_me._configReader.hasAddNavigation()) {
          if (!_me.getContainerElement().down('> div.celPresSlideShow_next')) {
            const nextButton = new Element('div').addClassName('celPresSlideShow_next');
            _me.getContainerElement().insert({ 'bottom': nextButton });
          }
          if (!_me.getContainerElement().down('> div.celPresSlideShow_prev')) {
            const prevButton = new Element('div').addClassName('celPresSlideShow_prev');
            _me.getContainerElement().insert({ 'top': prevButton });
          }
        }
      },

      _imageSlideShowLoadFirstContent_internal: function () {
        const _me = this;
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
          const navObj = _me._getCelSlideShowObj()._navObj;
          _me._getCelSlideShowObj().loadAndAddMainSlides(
            _me._configReader.getGallerySpace(), function (allSlides) {
              navObj._nextIndex = _me._configReader.getStartSlideNum();
              navObj._preloadFunc(allSlides[navObj._nextIndex],
                navObj._updateNextContent.bind(navObj));
            });
        } else {
          let startIndex = 0;
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

      _initAnimation: function (event) {
        const _me = this;
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
        _me._isPaused = _me._slideShowAnimation._paused;
      },

      _initManualStartButton: function () {
        const _me = this;
        const startButtonDiv = new Element('div', { 'class': 'slideshowButton' });
        startButtonDiv.hide();
        _me.getContainerElement().insert({ bottom: startButtonDiv });
        $(_me.getContainerElement()).stopObserving('click', _me._startStopClickHandlerBind);
        $(_me.getContainerElement()).observe('click', _me._startStopClickHandlerBind);
      },

      _startStopClickHandler: function (event) {
        const _me = this;
        event.stop();
        const clickedElement = event.findElement();
        let linkHref = '';
        if (clickedElement.up('div.cel_slideShow_slideWrapper a')) {
          linkHref = clickedElement.up('div.cel_slideShow_slideWrapper a').href;
        }
        $(document.body).stopObserving('click', _me._contextMenuSlideShowListItemClickedBind);
        if (linkHref && (linkHref !== '')) {
          $(document.body).observe('click', _me._contextMenuSlideShowListItemClickedBind);
          const mouseCoord = _me._getMousePos(event);
          const x = mouseCoord[0] - 3;
          const y = mouseCoord[1] - 6;
          _me._menuDiv = $$('body')[0].down('.contextMenuSlideShow');
          if (_me._menuDiv == null) {
            _me._menuDiv = _me._generateMenuDiv(clickedElement);
            $$('body')[0].insert(_me._menuDiv);
          } else {
            _me._menuDiv.show();
          }
          $$('.contextMenuSlideShowListItem').each(function (element) {
            element.stopObserving('click', _me._contextMenuSlideShowListItemClickedBind);
            element.observe('click', _me._contextMenuSlideShowListItemClickedBind);
          });
          _me._setPosition(x, y);
          _me.startStop(false, undefined, true);
        } else if (!clickedElement.up('div.cel_slideShow_slideWrapper a') &&
          clickedElement.hasClassName('celimage_slideshow_wrapper')) {
          _me._hideContextMenu();
          if (_me._isPaused) {
            _me.startStop(false, true);
          } else {
            _me.startStop(true, true);
          }
        } else {
          _me._hideContextMenu();
          _me.startStop();
        }
      },

      _contextMenuSlideShowListItemClicked: function (event) {
        const _me = this;
        event.stop();
        const clickedElement = event.findElement();
        const linkHref = clickedElement.readAttribute('data-href')
          || clickedElement.down('div').readAttribute('data-href');
        const target = clickedElement.readAttribute('data-target')
          || clickedElement.down('div').readAttribute('data-target');
        if ((linkHref != null) && (linkHref != '')) {
          window.open(linkHref, target);
          if (_me._isPaused) {
            _me.startStop(false, true);
          } else {
            _me.startStop(true, true);
          }
        } else if (clickedElement.hasClassName('continueSlideshowContainer')
          || (clickedElement.hasClassName('contextMenuSlideShowListItem')
            && clickedElement.down('div.continueSlideshowContainer'))) {
          _me.startStop(true);
        } else if (clickedElement.hasClassName('stopSlideshowContainer')
          || (clickedElement.hasClassName('contextMenuSlideShowListItem')
            && clickedElement.down('div.stopSlideshowContainer'))) {
          _me.startStop(false);
        } else {
          if (_me._isPaused) {
            _me.startStop(false, true);
          } else {
            _me.startStop(true, true);
          }
        }
        _me._hideContextMenu();
        $$('.contextMenuSlideShowListItem').each(function (element) {
          element.stopObserving('click', _me._contextMenuSlideShowListItemClickedBind);
        });
        $(document.body).stopObserving('click', _me._contextMenuSlideShowListItemClickedBind);
      },

      _hideContextMenu: function () {
        const _me = this;
        if (_me._menuDiv != null) {
          _me._menuDiv.hide();
          _me._menuDiv.remove();
          _me._menuDiv = null;
        }
      },

      _generateMenuDiv: function (clickedElement) {
        const _me = this;
        const linkHref = clickedElement.up(0).href;
        const menuDiv = new Element('div', {
          'class': 'contextMenuSlideShow'
        }).setStyle({
          'z-index': 999,
          'position': 'absolute'
        });
        const list = new Element('ul');
        let listElement = new Element('li', { 'class': 'contextMenuSlideShowListItem' }
        ).insert(new Element('div', {
          'data-href': linkHref,
          'data-target': clickedElement.up(0).target,
          'title': (celMessages.celslideshow.cmOpenLink || 'Open Link')
        }));
        list.insert(listElement);
        if (_me._slideShowAnimation._paused) {
          listElement = new Element('li', { 'class': 'contextMenuSlideShowListItem' }
          ).insert(new Element('div', {
            'class': 'continueSlideshowContainer',
            'title': (celMessages.celslideshow.cmContinue || 'Continue Slideshow')
          }));
          list.insert(listElement);
        } else {
          listElement = new Element('li', { 'class': 'contextMenuSlideShowListItem' }
          ).insert(new Element('div', {
            'class': 'stopSlideshowContainer',
            'title': (celMessages.celslideshow.cmPause || 'Pause Slideshow')
          }));
          list.insert(listElement);
        }
        menuDiv.insert(list);
        return menuDiv;
      },

      _getMousePos: function (event) {
        const theEvent = event || window.event;
        const tmpCoord = new Array(0, 0);
        let posx = 0;
        let posy = 0;

        if (theEvent.pageX || theEvent.pageY) { // Firefox & co.
          posx = theEvent.pageX;
          posy = theEvent.pageY;
        }
        else if (theEvent.clientX || theEvent.clientY) { // IE
          // NOTE: Explorer must be in strict mode for documentElement, otherwise use document.body.scrollLeft!
          posx = theEvent.clientX + document.documentElement.scrollLeft - 1;
          posy = theEvent.clientY + document.documentElement.scrollTop + 2;
        }
        tmpCoord[0] = posx;
        tmpCoord[1] = posy;
        return tmpCoord;
      },

      _setPosition: function (x, y) {
        const _me = this;
        _me._menuDiv.setStyle({
          'left': x + 'px',
          'top': y + 'px'
        });
      },

      startStop: function (isStart, delayedStart, isFreez) {
        const _me = this;
        if (typeof isStart === 'undefined') {
          isStart = _me._slideShowAnimation._paused;
        }
        if (typeof delayedStart === 'undefined') {
          delayedStart = false;
        }
        const slideShowButton = _me.getContainerElement().down('.slideshowButton');
        if (isStart) {
          console.log('animation started for image slideshow',
            _me._getContainerElemId());
          _me._isPaused = false;
          _me._slideShowAnimation.startAnimation(delayedStart);
          if (slideShowButton && !isFreez) {
            if ((_me._effect) && (_me._effect.state != 'finished')) {
              _me._effect.cancel();
            }
            _me._effect = Effect.Fade(slideShowButton, { duration: 1.0 });
          }
        } else {
          console.log('animation stopped for image slideshow',
            _me._getContainerElemId());
          _me._slideShowAnimation.stopAnimation();
          if (!isFreez) {
            _me._isPaused = true;
          }
          if (slideShowButton && !isFreez) {
            if ((_me._effect) && (_me._effect.state != 'finished')) {
              _me._effect.cancel();
            }
            _me._effect = Effect.Appear(slideShowButton, { duration: 1.0, to: 0.9 });
          }
        }
      },

      start: function () {
        const _me = this;
        _me._getCelSlideShowObj().setAutoresize(true);
        _me._getCelSlideShowObj().register();
        _me._imageSlideShowLoadFirstContent_internal();
      }

    });
  }

  //////////////////////////////////////////////////////////////////////////////
  //image SlideShow config reader
  //////////////////////////////////////////////////////////////////////////////
  if (typeof window.CELEMENTS.image.ConfigReader === 'undefined') {
    window.CELEMENTS.image.ConfigReader = Class.create({
      _htmlElemId: undefined,
      _htmlElemClasses: undefined,
      _configDef: undefined,
      _containerAnimWidth: undefined,
      _containerAnimHeight: undefined,
      _overlayWidthDefault: undefined,
      _overlayHeightDefault: undefined,
      _overlayWidth: undefined,
      _overlayHeight: undefined,
      _mobileDim: undefined,
      _isMobile: undefined,
      _autoresize: undefined,
      _startSlideNum: undefined,
      _galleryFN: undefined,
      _galleryObj: undefined,
      _layoutName: undefined,
      _centerSplashImage: undefined,

      initialize: function (htmlElem, configDef) {
        const _me = this;
        const configDefParam = configDef || {};
        _me._htmlElemId = htmlElem.id;
        _me._htmlElemClasses = $w(htmlElem.className);
        _me._mobileDim = new CELEMENTS.mobile.Dimensions();
        _me._isMobile = _me._mobileDim.isMobile;
        _me._autoresize = _me._isMobile.iOS() || _me._isMobile.Android();
        _me._overlayWidthDefault = 800;
        _me._overlayHeightDefault = 800;
        _me._configDef = new Hash({
          'manualstart': 'celimage_manualstart',
          'nonestart': 'celimage_nonestart',
          'autostart': 'celimage_autostart',
          'autostartnostop': 'celimage_autostartnostop',
          'addNavigation': 'celimage_addNavigation',
          'addCounterNone': 'celimage_addCounterNone',
          'randomStart': 'celimage_slideshowRandomStart',
          'customStart': 'celimage_customStartSlide',
          'addCloseButton': 'celimage_overlay_addCloseButton',
          'addCounterZeros': 'celimage_addCounterZeros',
          'forceAutoResize': 'celimage_forceAutoResize'
        }).update(configDefParam).toObject();
        _me._galleryFN = _me._getPart(1, null);
        _me._overlayWidth = _me._getPart(4, _me._overlayWidthDefault);
        _me._overlayHeight = _me._getPart(5, _me._overlayHeightDefault);
        _me._containerAnimWidth = _me._getPart(8, htmlElem.getWidth());
        _me._containerAnimHeight = _me._getPart(9, htmlElem.getHeight());
        _me._centerSplashImage = true;
      },

      _getPart: function (num, defaultvalue) {
        const _me = this;
        const parts = _me._htmlElemId.split(':');
        if ((num < parts.length) && (parts[num] != '')) {
          return parts[num];
        } else {
          return defaultvalue;
        }
      },

      _hasClassName: function (className) {
        const _me = this;
        return (_me._htmlElemClasses.indexOf(className) > -1);
      },

      getGalleryFN: function () {
        const _me = this;
        return _me._galleryFN;
      },

      isAutoResize: function () {
        const _me = this;
        return _me._autoresize || _me._hasClassName(_me._configDef.forceAutoResize);
      },

      isCenterSplashImage: function () {
        const _me = this;
        return _me._centerSplashImage;
      },

      setCenterSplashImage: function (isCenter) {
        const _me = this;
        _me._centerSplashImage = (isCenter == true);
      },

      getStartMode: function () {
        const _me = this;
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

      hasAnimation: function () {
        const _me = this;
        return (_me.getStartMode() != 'none');
      },

      hasAutoStart: function () {
        const _me = this;
        return (_me.getStartMode() == 'auto')
          || (_me.getStartMode() == 'autonostop');
      },

      hasLeadingZeros: function () {
        const _me = this;
        return _me._hasClassName(_me._configDef.addCounterZeros);
      },

      hasManualButton: function () {
        const _me = this;
        return (_me.getStartMode() != 'autonostop');
      },

      getContainerAnimWidth: function () {
        const _me = this;
        if ((_me._containerAnimWidth == '') || isNaN(parseInt(_me._containerAnimWidth))
          || (parseInt(_me._containerAnimWidth) <= 0)) {
          _me._containerAnimWidth = _me._getPart(8, $(_me._htmlElemId).getWidth());
        }
        return parseInt(_me._containerAnimWidth);
      },

      getContainerAnimHeight: function () {
        const _me = this;
        if ((_me._containerAnimHeight == '') || isNaN(parseInt(_me._containerAnimHeight))
          || (parseInt(_me._containerAnimHeight) <= 0)) {
          _me._containerAnimHeight = _me._getPart(9, $(_me._htmlElemId).getHeight());
        }
        return parseInt(_me._containerAnimHeight);
      },

      getOverlayWidth: function () {
        const _me = this;
        if ((_me._overlayWidth == '') || isNaN(parseInt(_me._overlayWidth))
          || (parseInt(_me._overlayWidth) <= 0)) {
          _me._overlayWidth = _me._getPart(4, _me._overlayWidthDefault);
        }
        return parseInt(_me._overlayWidth);
      },

      getOverlayHeight: function () {
        const _me = this;
        if ((_me._overlayHeight == '') || isNaN(parseInt(_me._overlayHeight))
          || (parseInt(_me._overlayHeight) <= 0)) {
          _me._overlayHeight = _me._getPart(5, _me._overlayHeightDefault);
        }
        return parseInt(_me._overlayHeight);
      },

      hasRandomStart: function () {
        const _me = this;
        return _me._hasClassName(_me._configDef.randomStart);
      },

      hasCustomStart: function () {
        const _me = this;
        return _me._hasClassName(_me._configDef.customStart);
      },

      getGallerySpace: function () {
        const _me = this;
        return _me._getPart(7, '');
      },

      getSlideShowEffect: function () {
        const _me = this;
        return _me._getPart(3, 'none');
      },

      setStartSlideNum: function (startSlideNum) {
        const _me = this;
        _me._startSlideNum = startSlideNum;
      },

      getStartSlideNum: function () {
        const _me = this;
        if (_me._startSlideNum) {
          return _me._startSlideNum;
        }
        return parseInt(_me._getPart(6, 1)) - 1;
      },

      getTimeout: function () {
        const _me = this;
        return _me._getPart(2, 3);
      },

      hasAddNavigation: function () {
        const _me = this;
        return _me._hasClassName(_me._configDef.addNavigation);
      },

      hasCloseButton: function () {
        const _me = this;
        return _me._hasClassName(_me._configDef.addCloseButton);
      },

      hasAddCounterNone: function () {
        const _me = this;
        return _me._hasClassName(_me._configDef.addCounterNone);
      },

      _getGalleryObj: function (callbackFN) {
        const _me = this;
        if (!_me._galleryObj) {
          _me._galleryObj = new CELEMENTS.images.Gallery(
            _me._galleryFN, callbackFN, 0);

        } else {
          _me._galleryObj.executeAfterLoad(callbackFN, 0);
        }
      },

      loadOverlayLayoutName: function (callbackFN) {
        const _me = this;
        _me._getGalleryObj(function (galleryObj) {
          _me._layoutName = galleryObj.getLayoutName();
          if (callbackFN) {
            callbackFN(_me._layoutName);
          }
        });
      },

      getLayoutName: function () {
        const _me = this;
        return _me._layoutName;
      },

      getZoomStyles: function (zoomFactor, fullWidth, fullHeight) {
        const _me = this;
        return _me._mobileDim.getZoomStyles(zoomFactor, fullWidth, fullHeight);
      },

      computeZoomFactor: function () {
        const _me = this;
        let openDialog = CELEMENTS.presentation.getOverlayObj();
        let oldWidth = parseInt(openDialog.getWidth());
        let newWidth = oldWidth;
        if (oldWidth > _me._mobileDim.getInnerWidth()) {
          newWidth = _me._mobileDim.getInnerWidth() - 20; // take care of close button
        }
        let zoomWidthFactor = newWidth / oldWidth;
        let oldHeight = parseInt(openDialog.getHeight());
        let newHeight = oldHeight;
        if (oldHeight > _me._mobileDim.getInnerHeight()) {
          newHeight = _me._mobileDim.getInnerHeight() - 20; // take care of close button
        }
        let zoomHeightFactor = newHeight / oldHeight;
        let zoomFactor;
        if (zoomHeightFactor < zoomWidthFactor) {
          zoomFactor = zoomHeightFactor;
        } else {
          zoomFactor = zoomWidthFactor;
        }
        console.log('imageSlideShow: computeZoomFactor ', zoomFactor, newWidth,
          newHeight, _me._mobileDim.getInnerWidth(), _me._mobileDim.getInnerHeight());
        return zoomFactor;
      }

    });
  }
})(window);
