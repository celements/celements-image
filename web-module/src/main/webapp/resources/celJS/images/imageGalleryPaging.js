(function(window, undefined) {
  "use strict";

  var initScroller = function() {
    if ($$('div.galleryViewScrollContainer').size() > 0) {
      //TODO init Swiper -> copy from PZ oder CFI
      //TODO init EndlessScroller -> copy from PZ 
    }
  };

  $j(document).ready(function() {
    $(document.body).observe('celImageGallery:initScroller', initScroller);
    var autoInitEv = $(document.body).fire('celImageGallery:autoInitScrollerOnReady');
    if (!autoInitEv.stopped) {
      initScroller();
    }
  });

})(window);
