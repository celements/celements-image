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
      
    _initSwiperScrollbar : function(desableSimulateTouch) {
      var _me = this;
      if (!_me._isRooted) {
        console.warn('_getSwiperScrollbarContainer: UNROOTED !!!', _me._columnHTMLElem);
      }
      _me._destroySwiper();
      _me._getScrollSlideInner().addClassName('slide-inner');
      _me._getScrollSlide().addClassName('swiper-slide');
      _me._getScrollWrapper().addClassName('swiper-wrapper');
      var swiperContainer = _me._getScrollContainer();
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
      _me.getColumnElem().observe('proz:contentChanged',
          _me._reInitScrollbarHandlerBind);
      $(document.body).observe('proz:updateScrollbar',
          _me._reInitScrollbarHandlerBind);
      $(document.body).observe('proz:resize', _me._updateScrollbarBind);
      console.log('_getScrollContainer: trigger resize');
      $(document.body).fire('proz:forceResize');
    }
  };
  
})(window);

/*  
  var swiper;
  
  var initScroller = function() {
    if ($$('div.galleryViewScrollContainer').size() > 0) {
      $$('.swiper-container').each(function(container) {
        var slides = container.down('ul');
        if(slides) {
          slides.addClassName('swiper-wrapper');
          slides.select('li').each(function(slide) {
            slide.addClassName('swiper-slide');
          });

          swiper = new Swiper(sponsorenSwiperContainer, {
            mousewheelControl : true,
            mode : 'vertical',
            autoResize : false,
            slidePerGroup : 1,
            slidesPerView : 'auto'
          });
        }
      });
      
      $$('.presentationList').each(function(presentation) {
        var endlessScroller = new CELEMENTS.anim.EndlessScroll(presentation,
            endlessLoadAction, {
          'scrollEventName' : 'cel_gallery:scroll',
          'overlap' : 600
        });
        scrollContainer.observe('celEndlessScroll:ScrollPosEvent', swiperScrollOverflow);
      });
    }
  };
  
  var endlessLoadAction = function(htmlElem, endlessScroller, callbackFN) {
	  //TODO implement
    if (_me._dataProvider.hasMore()) {
      _me._getScrollSlideInner().insert({ 'bottom' : _me._loadingIndicator });
      _me.getColumnElem().fire('proz:contentChanged', { 'column' : _me });
      _me._dataProvider.loadNextData(_me._endlessLoadCallbackBind.curry(callbackFN));
      
      
      
      { 
        galleryNavConf
        offset
      }
      
      
      
    }
  };
  
  var swiperScrollOverflow = function(event) {
      event.stop();
      var param = event.memo;
      //getWrapperTranslate returns negative values, Thus we must add these.
      param.currentScrollOverflow = swiper.wrapper.getHeight() - swiper.height
                                    + swiper.getWrapperTranslate();
  };

  $j(document).ready(function() {
    $(document.body).observe('celImageGallery:initScroller', initScroller);
    var autoInitEv = $(document.body).fire('celImageGallery:autoInitScrollerOnReady');
    if (!autoInitEv.stopped) {
      initScroller();
    }
  });
 */