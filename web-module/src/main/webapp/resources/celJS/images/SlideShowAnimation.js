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
 * Celements presentation Slideshow animation
 * This is the Celements presentation Slideshow animation controller.
 **/
  if (typeof window.CELEMENTS == "undefined") { window.CELEMENTS = {}; }
  if (typeof window.CELEMENTS.presentation == "undefined") { window.CELEMENTS.presentation = {}; }

  //////////////////////////////////////////////////////////////////////////////
  // Celements presentation Slideshow Animation
  //////////////////////////////////////////////////////////////////////////////
  window.CELEMENTS.presentation.SlideShowAnimation = function (celSlideShowObj, timeout,
    slideShowEffect) {
    // constructor
    this._init(celSlideShowObj, timeout, slideShowEffect);
  };

  (function () {
    "use strict";

    CELEMENTS.presentation.SlideShowAnimation.prototype = {
      _restartDelayBind: undefined,
      _celSlideShowObj: undefined,
      _slideShowEffect: undefined,
      _timeout: undefined,
      _slideShowDelayedThread: undefined,
      _changeContentWithAnimationBind: undefined,
      _debug: false,
      _paused: false,

      _init: function (celSlideShowObj, timeout, slideShowEffect) {
        var _me = this;
        _me._celSlideShowObj = celSlideShowObj;
        _me._restartDelayBind = _me._restartDelay.bind(_me);
        _me._delayedNextBind = _me._delayedNext.bind(_me);
        _me._changeContentWithAnimationBind = _me._changeContentWithAnimation.bind(_me);
        _me._slideShowEffect = slideShowEffect || 'none';
        _me._timeout = timeout || 9999;
      },

      setSlideShowEffect: function (timeout) {
        var _me = this;
        _me._timeout = timeout || 9999;
      },

      setTimeout: function (slideShowEffect) {
        var _me = this;
        _me._slideShowEffect = slideShowEffect || 'none';
      },

      register: function () {
        var _me = this;
        _me._celSlideShowObj.getHtmlContainer().stopObserving(
          'cel_yuiOverlay:contentChanged', _me._restartDelayBind);
        _me._celSlideShowObj.getHtmlContainer().observe('cel_yuiOverlay:contentChanged',
          _me._restartDelayBind);
      },

      _restartDelay: function () {
        var _me = this;
        _me._cancelDelayedNext();
        _me._slideShowDelayedThread = _me._delayedNextBind.delay(_me._timeout);
      },

      /**
       * _delayedNext may only be called delayed by _restartDelay
       */
      _delayedNext: function () {
        var _me = this;
        _me._slideShowDelayedThread = undefined;
        if (!_me._paused) {
          if (_me._slideShowEffect != 'none') {
            _me._celSlideShowObj.getHtmlContainer().stopObserving(
              'cel_yuiOverlay:changeContent', _me._changeContentWithAnimationBind);
            _me._celSlideShowObj.getHtmlContainer().observe('cel_yuiOverlay:changeContent',
              _me._changeContentWithAnimationBind);
          }
          _me._celSlideShowObj._navObj.nextSlide();
        }
      },

      _cancelDelayedNext: function () {
        var _me = this;
        if (_me._slideShowDelayedThread) {
          window.clearTimeout(_me._slideShowDelayedThread);
          _me._slideShowDelayedThread = undefined;
        }
      },

      stopAnimation: function () {
        var _me = this;
        _me._paused = true;
        _me._cancelDelayedNext();
      },

      startAnimation: function (delayedStart) {
        if (!window.location.search.match(/xpage=(overlay|import|filebase)/)) {
          var _me = this;
          _me._paused = false;
          _me._cancelDelayedNext();
          if (delayedStart) {
            _me._restartDelay();
          } else {
            _me._delayedNext();
          }
        }
      },

      isStopped: function () {
        var _me = this;
        return _me._paused;
      },

      _changeContentWithAnimation: function (event) {
        var _me = this;
        _me._celSlideShowObj.getHtmlContainer().stopObserving(
          'cel_yuiOverlay:changeContent', _me._changeContentWithAnimationBind);
        var memoObj = event.memo;
        if (_me._slideShowEffect != 'none') {
          if (_me._slideShowEffect == 'fade') {
            var oldSlide = memoObj.slides[0];
            var newSlide = memoObj.slides[1];
            oldSlide.setStyle({ 'position': 'absolute' });
            new Effect.Parallel(
              [
                new Effect.Appear(newSlide, {
                  from: 0,
                  to: 1,
                  sync: true
                }),
                new Effect.Fade(oldSlide, {
                  from: 1,
                  to: 0,
                  sync: true
                })
              ],
              {
                'duration': 2.0,
                'slideShowAnimObj': memoObj,
                'afterFinish': function (effect) {
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

      _afterFinishTransition: function (effect) {
        var _me = this;
        //        var slideShowAnimObj = effect.options.slideShowAnimObj;
        //        console.log('_afterFinishTransition: ', slideShowAnimObj);
        _me._celSlideShowObj.getHtmlContainer().fire(
          'cel_slideShow:slideTransitionFinished');
      }

    };
  })();

})(window);
