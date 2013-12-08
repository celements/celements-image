/**
 * Celements presentation Slideshow animation
 * This is the Celements presentation Slideshow animation controller.
 */
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.presentation=="undefined"){CELEMENTS.presentation={};};

(function(window, undefined) {

  $j(document).ready(function() {
    if (!CISS_OverlaySlideShowObj) {
      CISS_OverlaySlideShowObj = new CELEMENTS.presentation.SlideShow();
    }
    $$('.celimage_slideshow').each(function(slideShowElem) {
      if (slideShowElem.hasClassName('celimage_overlay')) {
        CISS_OverlaySlideShowObj.registerOpenInOverlay(slideShowElem);
      } else {
        var imgSlideShow = CELEMENTS.presentation.getSlideShowObj(slideShowElem.id);
        imgSlideShow.startNonOverlaySlideShow();
      }
    });
  });

//////////////////////////////////////////////////////////////////////////////
// Celements presentation Slideshow Animation
//////////////////////////////////////////////////////////////////////////////
CELEMENTS.presentation.SlideShowAnimation = function(celSlideShowObj) {
  // constructor
  this._init(celSlideShowObj);
};

(function() {
  CELEMENTS.presentation.SlideShowAnimation.prototype = {
      _restartDelayBind : undefined,
      _celSlideShowObj : undefined,
      _slideShowEffect : undefined,
      _timeout : undefined,
      _slideShowDelayedThread : undefined,
      _debug : false,

      _init : function(celSlideShowObj, timeout, slideShowEffect ) {
        var _me = this;
        _me._celSlideShowObj = celSlideShowObj;
        _me._restartDelayBind = _me._restartDelay.bind(_me);
        _me._delayedNextBind = _me._delayedNext.bind(_me);
        _me._changeContentWithAnimationBind = _me._changeContentWithAnimation.bind(_me);
        _me._slideShowEffect = slideShowEffect || 'none';
        _me._timeout = timeout || 9999;
      },

      setSlideShowEffect : function(timeout) {
        var _me = this;
        _me._timeout = timeout || 9999;
      },

      setTimeout : function(slideShowEffect) {
        var _me = this;
        _me._slideShowEffect = slideShowEffect || 'none';
      },

      register : function() {
        var _me = this;
        _me._celSlideShowObj.getHtmlContainer().stopObserving(
            'cel_yuiOverlay:contentChanged', _me._restartDelayBind);
        _me._celSlideShowObj.getHtmlContainer().observe('cel_yuiOverlay:contentChanged',
            _me._restartDelayBind);
      },

      _restartDelay : function() {
        var _me = this;
        if (_me._slideShowDelayedThread) {
          window.clearTimeout(_me._slideShowDelayedThread);
          _me._slideShowDelayedThread = undefined;
        }
        _me._celSlideShowObj.getHtmlContainer().stopObserving(
            'cel_yuiOverlay:changeContent', _me._changeContentWithAnimationBind);
        _me._celSlideShowObj.getHtmlContainer().observe('cel_yuiOverlay:changeContent',
            _me._changeContentWithAnimation);
        _me._slideShowDelayedThread = _me._delayedNextBind.delay(_me._timeout);
      },

      _delayedNext : function() {
        var _me = this;
        _me._slideShowDelayedThread = undefined;
        _me._celSlideShowObj._navObj.nextSlide();
      },

      _changeContentWithAnimation : function(event) {
        var _me = this;
        var memoObj = event.memo;
        console.log('transition fade: ', memoObj);
      }

  };
})();

})(window);
