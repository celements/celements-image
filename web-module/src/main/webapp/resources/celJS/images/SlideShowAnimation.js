/**
 * Celements presentation Slideshow animation
 * This is the Celements presentation Slideshow animation controller.
 */
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.presentation=="undefined"){CELEMENTS.presentation={};};

(function(window, undefined) {

//////////////////////////////////////////////////////////////////////////////
// Celements presentation Slideshow Animation
//////////////////////////////////////////////////////////////////////////////
CELEMENTS.presentation.SlideShowAnimation = function(celSlideShowObj, timeout,
    slideShowEffect) {
  // constructor
  this._init(celSlideShowObj, timeout, slideShowEffect);
};

(function() {
  CELEMENTS.presentation.SlideShowAnimation.prototype = {
      _restartDelayBind : undefined,
      _celSlideShowObj : undefined,
      _slideShowEffect : undefined,
      _timeout : undefined,
      _slideShowDelayedThread : undefined,
      _changeContentWithAnimationBind : undefined,
      _debug : false,

      _init : function(celSlideShowObj, timeout, slideShowEffect) {
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
        _me._slideShowDelayedThread = _me._delayedNextBind.delay(_me._timeout);
      },

      _delayedNext : function() {
        var _me = this;
        _me._slideShowDelayedThread = undefined;
        if (_me._slideShowEffect != 'none') {
          _me._celSlideShowObj.getHtmlContainer().stopObserving(
              'cel_yuiOverlay:changeContent', _me._changeContentWithAnimationBind);
          _me._celSlideShowObj.getHtmlContainer().observe('cel_yuiOverlay:changeContent',
              _me._changeContentWithAnimationBind);
        }
        _me._celSlideShowObj._navObj.nextSlide();
      },

      _changeContentWithAnimation : function(event) {
        var _me = this;
        _me._celSlideShowObj.getHtmlContainer().stopObserving(
            'cel_yuiOverlay:changeContent', _me._changeContentWithAnimationBind);
        var memoObj = event.memo;
        if (_me._slideShowEffect != 'none') {
          if (_me._slideShowEffect == 'fade') {
            var oldSlide = memoObj.slides[0];
            var newSlide = memoObj.slides[1];
            oldSlide.setStyle({ 'position' : 'absolute' });
            new Effect.Parallel(
                [
                  new Effect.Appear(newSlide, {
                    from : 0,
                    to : 1,
                    sync: true
                  }),
                  new Effect.Fade(oldSlide, {
                    from : 1,
                    to : 0,
                    sync: true
                  })
                ],
                {
                  'duration' : 2.0,
                  'slideShowAnimObj' : memoObj,
                  'afterFinish' : function(effect) {
                    _me._afterFinishTransition(effect);
                  }
                }
            );
            event.stop();
          } else if ((typeof console != 'undefined')
              && (typeof console.warn != 'undefined')) {
            console.warn('unsupported transition effect: ', _me._slideShowEffect,
                memoObj);
          }
        }
      },

      _afterFinishTransition : function(effect) {
        var _me = this;
//        var slideShowAnimObj = effect.options.slideShowAnimObj;
//        console.log('_afterFinishTransition: ', slideShowAnimObj);
        _me._celSlideShowObj.getHtmlContainer().fire(
            'cel_slideShow:slideTransitionFinished');
      }

  };
})();

})(window);
