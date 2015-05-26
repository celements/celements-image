(function(window, undefined) {
  "use strict";

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

})(window);
