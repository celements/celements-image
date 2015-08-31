(function(window, undefined) {
  "use strict";

  /**
   * package CEL definition
   */
  if (typeof window.CEL == "undefined") { window.CEL={};};

  /**
   * Column constructor
   */
  CEL.Column = function(htmlElemId) {
    // constructor
    this._init(htmlElemId);
  };

  /**
   * class CEL.Column definition
   */
  CEL.Column.prototype = {
    _columnHTMLElem : undefined,
    _scrollSlideInner : undefined,
    _scrollSlide : undefined,
    _scrollWrapper : undefined,
    _swiper : undefined,
    _swiperScrollBind : undefined,
    _reInitScrollbarHandlerBind : undefined,
    _updateScrollbarBind : undefined,
    _endlessScroller : undefined,
    _endlessLoadActionBind : undefined,
    _getSwiperScrollOverflowBind : undefined,
    _offset : undefined,
    _curContentObj : undefined,
    _pageLoadCallbackFuncBind : undefined,
    _reInitScrollbarDelayedBind : undefined,
    _reInitScrollbarDelayedCall : undefined,
    _endlessLoadCallbackBind : undefined,
    _loadingImg : undefined,
    _loadingIndicator :undefined,
    _loaderCallbackFN : undefined,
    _loadedImagesOnPageLoad :undefined,
    _hasMore : undefined,
    
    _init : function(htmlElemId) {
      var _me = this;
//      _me._columnHTMLElem = $(htmlElemId);
      $$('.presentationList')[0].id = 'gallerySwiper';
      _me._columnHTMLElem = $$('.presentationList')[0];
      _me._swiperScrollBind = _me._swiperScroll.bind(_me);
      _me._reInitScrollbarHandlerBind = _me._reInitScrollbarHandler.bind(_me);
      _me._updateScrollbarBind = _me._updateScrollbar.bind(_me);
      _me._endlessLoadActionBind = _me._endlessLoadAction.bind(_me);
      _me._getSwiperScrollOverflowBind = _me._getSwiperScrollOverflow.bind(_me);
      _me._offset = 0;
      _me._pageLoadCallbackFuncBind = _me._pageLoadCallbackFunc.bind(_me);
      _me._reInitScrollbarDelayedBind = _me._reInitScrollbarDelayed.bind(_me);
      _me._endlessLoadCallbackBind = _me._endlessLoadCallback.bind(_me);
      _me._initLoadingImg();
      _me._loadedImagesOnPageLoad = _me._getScrollSlideInner().children.length;
    },
    
    _initLoadingImg : function() {
      var _me = this;
      _me._loadingImg = new Image();
      _me._loadingImg.alt = 'loading...';
      _me._loadingImg.src = '/file/resources/celRes/ajax-loader.gif';
      _me._loadingIndicator = new Element('div').addClassName('attListLoading');
      _me._loadingIndicator.setStyle({'padding-top': '10px'})
      _me._loadingIndicator.update(_me._loadingImg);
    },
    
    _swiperScroll : function(theSwiper, coords) {
      var _me = this;
      _me._getScrollContainer().fire('cel:scroll', {
        'x' : coords.x,
        'y' : coords.y
      });
    },
    
    _getSwiperScrollbarId : function() {
      var _me = this;
//      return _me.getColumnElem().id + '-swiper-scrollbar';
      return 'Gallery-swiper-scrollbar';
    },
    
    _getSwiperScrollbarContainer : function() {
      var _me = this;
      var swiperContainer = _me._getScrollContainer();
      var scrollbarContainerId = _me._getSwiperScrollbarId();
      var swiperScrollbarContainer = $(scrollbarContainerId);
      if (!swiperScrollbarContainer) {
        swiperScrollbarContainer = new Element('div', {
          'id' : scrollbarContainerId
        });
        swiperContainer.insert({ 'bottom' : swiperScrollbarContainer });
        console.log('_getSwiperScrollbarContainer: new scrollbar created. ',
            _me.getColIdPrefix(), swiperScrollbarContainer);
      }
      swiperScrollbarContainer.addClassName('swiper-scrollbar');
      return swiperScrollbarContainer;
    },
    
    _initSwiperScrollbar : function(desableSimulateTouch) {
      var _me = this;
      _me._getScrollSlideInner().addClassName('slide-inner');
      _me._getScrollSlide().addClassName('swiper-slide');
      _me._getScrollWrapper().addClassName('swiper-wrapper');
      var scrollContainer = _me._getScrollContainer();
      var swiperContainer = scrollContainer;
      swiperContainer.addClassName('swiper-container');
      var scrollbarContainerId = _me._getSwiperScrollbarContainer().id;
      console.log('_initSwiperScrollbar before new Swiper ', _me.getColIdPrefix(),
          _me._getScrollSlideInner(), _me._getScrollSlide(), _me._getScrollWrapper(),
          swiperContainer);
      var myDesableSimulateTouch = desableSimulateTouch
      if(desableSimulateTouch == null) {
        myDesableSimulateTouch = true;
      }
      _me._swiper = new Swiper(swiperContainer, {
        scrollContainer : true,
        mousewheelControl : true,
        mode : 'vertical',
        autoResize : false,
        slidePerGroup : 1,
        slidesPerView : 'auto',
        updateTranslate : true,
        simulateTouch : myDesableSimulateTouch,
        //Enable Scrollbar
        scrollbar: {
          container : '#' + scrollbarContainerId,
          hide : true,
          draggable : true,
          snapOnRelease : true //XXX does not work so far in swiper swipeReset()
        }
      });
      _me._swiper.addCallback('SetWrapperTransform', _me._swiperScrollBind);
      _me.getColumnElem().observe('cel:contentChanged',
          _me._reInitScrollbarHandlerBind);
      $(document.body).observe('cel:updateScrollbar',
          _me._reInitScrollbarHandlerBind);
      $(document.body).observe('cel:resize', _me._updateScrollbarBind);
      console.log('_getScrollContainer: trigger resize');
      $(document.body).fire('cel:forceResize');
      _me._endlessScroller = new CELEMENTS.anim.EndlessScroll(scrollContainer,
          _me._endlessLoadActionBind, {
        'executeOnInit' : false,
        'scrollEventName' : 'cel:scroll',
        'overlap' : 200
      });
      scrollContainer.observe('celEndlessScroll:ScrollPosEvent',
          _me._getSwiperScrollOverflowBind);
    },
    
    getColumnElem : function() {
      var _me = this;
      return _me._columnHTMLElem.down('.spaltenContent') || _me._columnHTMLElem;
    },

    getColumnInnerElem : function() {
      var _me = this;
      return _me._columnHTMLElem.down('.spaltenInnerContent') || _me._columnHTMLElem;
    },
    
    _getScrollContainer : function() {
      var _me = this;
      var scrollContainer = _me.getColumnInnerElem().up('.scrollcontainer');
      if (!scrollContainer) {
        scrollContainer = new Element('div');
        scrollContainer.addClassName('scrollcontainer');
        _me._getScrollWrapper().wrap(scrollContainer);
        scrollContainer.fire('cel:scrollContainerCreated', {
          'scrollContainer' : scrollContainer});
      }
      
      return scrollContainer;
    },
    
    _getScrollWrapper : function() {
      var _me = this;
      var scrollWrapper = _me.getColumnInnerElem().up('.scrollwrapper');
      if (!scrollWrapper) {
        scrollWrapper = new Element('div');
        scrollWrapper.addClassName('scrollwrapper');
        _me._getScrollSlide().wrap(scrollWrapper);
      }
      return scrollWrapper;
    },
    
    _getScrollSlide : function() {
      var _me = this;
      var scrollSlide = _me.getColumnInnerElem().up('.scrollslide');
      if (!scrollSlide) {
        scrollSlide = new Element('div');
        scrollSlide.addClassName('scrollslide');
        _me.getColumnInnerElem().wrap(scrollSlide);
      }
      return scrollSlide;
    },
    
    
    _getScrollSlideInner : function() {
      var _me = this;
      var scrollSlideInner = _me._getScrollSlide().down('ul');
      if (!scrollSlideInner) {
        scrollSlideInner = new Element('ul');
        scrollSlideInner.insert(_me.getColumnInnerElem().innerHTML);
        _me.getColumnInnerElem().update(scrollSlideInner);
      }
      scrollSlideInner.addClassName('scrollslideinner');
      return scrollSlideInner;
    },
    
    getColIdPrefix : function() {
      var _me = this;
      return _me.getColumnInnerElem().id;
    },
    
    _getSwiperScrollOverflow : function(event) {
      var _me = this;
      event.stop();
      var param = event.memo;
      //getWrapperTranslate returns negative values, Thus we must add these.
      param.currentScrollOverflow = _me._swiper.wrapper.getHeight() - _me._swiper.height
                                    + _me._swiper.getWrapperTranslate();
    },
    
    _reInitScrollbarHandler : function(event) {
      var _me = this;
      _me._startReInitScrollbarDelayed();
    },
    
    _clearReInitScrollbarDelayedCall : function() {
      var _me = this;
      if (_me._reInitScrollbarDelayedCall) {
        clearTimeout(_me._reInitScrollbarDelayedCall);
      }
    },
    
    _startReInitScrollbarDelayed : function(delayed) {
      var _me = this;
      var newDelayed = delayed || 0.1;
      newDelayed *= 2;
      _me._clearReInitScrollbarDelayedCall();
      _me._reInitScrollbarDelayedCall = _me._reInitScrollbarDelayedBind.delay(
          newDelayed, newDelayed);
    },
    
    _updateScrollbar : function() {
      var _me = this;
      _me._swiper.resizeFix();
    },
    
    _startReInitScrollbarDelayed : function(delayed) {
      var _me = this;
      var newDelayed = delayed || 0.1;
      newDelayed *= 2;
      _me._clearReInitScrollbarDelayedCall();
      _me._reInitScrollbarDelayedCall = _me._reInitScrollbarDelayedBind.delay(
          newDelayed);
    },

    _reInitScrollbarDelayed : function(delayed) {
      var _me = this;
      _me._swiper.reInit();
      var columnHeight = _me.getColumnInnerElem().getHeight();
      var scrollbarDrag = _me._getScrollContainer().down('.swiper-scrollbar-drag');
      var scrollConHeight = _me._getScrollContainer().getHeight();
      if ((columnHeight > scrollConHeight) && scrollbarDrag.getHeight() === 0) {
        console.log('_reInitScrollbar: failed to init scrollbar. retry in ', delayed,
            _me.getColIdPrefix(), columnHeight, scrollbarDrag.getHeight(), scrollConHeight);
        _me._startReInitScrollbarDelayed(delayed);
      } else {
        _me._reInitScrollbarDelayedCall = null;
      }
    },
    
    _endlessLoadAction : function(htmlElem, endlessScroller, callbackFN) {
      var _me = this;
      _me._getScrollSlide().down('div').insert({ 'bottom' : _me._loadingIndicator });
      _me._startReInitScrollbarDelayed();
      _me._swiper.resizeFix();
      _me.loadNextData(_me._endlessLoadCallbackBind.curry(callbackFN));
    },
    
    _pageLoadCallbackFunc : function(curContentObj) {
      var _me = this;
      _me._curContentObj = curContentObj;
      var htmlElems = _me._curContentObj.content;
      var listElements = new Element('div').update(htmlElems).down('ul').children;
      var listElementsArr = Array.prototype.slice.call(listElements);
      if (curContentObj.hasMore != null) {
        _me._hasMore = curContentObj.hasMore;        
      } else {
        _me._hadMore = false;
      }
      _me._loaderCallbackFN(listElementsArr);
    },
    
    _endlessLoadCallback : function(scrollerCallbackFN, newContent) {
      var _me = this;
      console.log('_endlessLoadCallback: ', _me.getColIdPrefix(), newContent);
      _me.getColumnInnerElem().select('.attListLoading').each(Element.remove);
      if (newContent) {
        if (newContent.size && newContent.each) {
          newContent.each(function(newElem) {
            newElem.addClassName('swiper-slide');
            _me._getScrollSlideInner().insert({ 'bottom' : newElem });
          });
        } else {
          _me._getScrollSlideInner().insert({ 'bottom' : newContent });
        }
        var slideShowStarter = new CELEMENTS.image.SlideShowStarter(_me._getScrollSlideInner());
        slideShowStarter.initializeSlideShow();
        _me.getColumnElem().fire('cel:imageGalleryChanged');
      } else {
        console.error('data provider issued callback with undefined content!');
      }
      scrollerCallbackFN(_me._hasMore);
      _me._startReInitScrollbarDelayed();
      _me._swiper.resizeFix();
    },
    
    loadNextData : function(callbackFN) {
      var _me = this;
      _me._loaderCallbackFN = callbackFN;
      var viewURL = window.location.href;
      var galleryNavConf = _me._getScrollContainer().up('div').id.split(':')[1];
      new Ajax.Request(viewURL, {
        method: "post",
        parameters : {
          xpage : "celements_ajax",
          ajax_mode : "getGalleryPage",
          ajax : 1,
          galleryNavConf : galleryNavConf,
          offset : _me._offset += _me._loadedImagesOnPageLoad
        }, onSuccess : function (transport) {
          if (transport.responseText.isJSON()) {
            var responseObject = transport.responseText.evalJSON();
            _me._pageLoadCallbackFuncBind(responseObject);
          } else if ((typeof console !== 'undefined')
              && (typeof console.error !== 'undefined')) {
            console.error('noJSON!!! ', transport.responseText);
          }
        }
      });
    }
  };
  
  
  celAddOnBeforeLoadListener(function() {
    var column = new CEL.Column('content');
    column._initSwiperScrollbar();
  });
  
})(window);
