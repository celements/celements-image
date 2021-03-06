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
(function(window, undefined) {
  "use strict";

  /**
   * package CEL definition
   */
  if (typeof window.CEL == "undefined") { window.CEL={};};

  /**
   * Column constructor
   */
  CEL.Column = function(htmlElemCssPath) {
    // constructor
    this._init(htmlElemCssPath);
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
    _scrollButtonClickBind : undefined,
    _scrollButtonMouseClickedBind :undefined,
    _setTranslationNumberBind : undefined,
    _loadingImg : undefined,
    _loadingIndicator :undefined,
    _loaderCallbackFN : undefined,
    _loadedImagesOnPageLoad :undefined,
    _hasMore : undefined,
    _translateNumber : undefined,
    _periodicalExecuter : undefined,
    _scrollHeight : undefined, 
    
    _init : function(htmlElemCssPath) {
      var _me = this;
      $$(htmlElemCssPath)[0].id = 'gallerySwiper';
      _me._columnHTMLElem = $$(htmlElemCssPath)[0];
      _me._swiperScrollBind = _me._swiperScroll.bind(_me);
      _me._reInitScrollbarHandlerBind = _me._reInitScrollbarHandler.bind(_me);
      _me._updateScrollbarBind = _me._updateScrollbar.bind(_me);
      _me._endlessLoadActionBind = _me._endlessLoadAction.bind(_me);
      _me._getSwiperScrollOverflowBind = _me._getSwiperScrollOverflow.bind(_me);
      _me._scrollButtonClickBind = _me._scrollButtonClick.bind(_me);
      _me._scrollButtonMouseClickedBind = _me._scrollButtonMouseClicked.bind(_me);
      _me._setTranslationNumberBind = _me._setTranslationNumber.bind(_me);
      _me._offset = 0;
      _me._translateNumber = 0;
      _me._scrollHeight = 0;
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
      _me._loadingImg.src = CELEMENTS.getPathPrefix() + '/file/resources/celRes/ajax-loader.gif';
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
    
    _scrollButtonClick : function(event) {
      var _me = this;
      event.stop();
      _me._swiper.setWrapperTransition(300);
      var element = event.findElement('.swiper-button-next, .swiper-button-prev');
      if(element.hasClassName('swiper-button-next')) {
        _me._translateNumber = _me._translateNumber - _me._scrollHeight;
        var snapGridHeight = _me._swiper.snapGrid[1] || 0;
        if(_me._translateNumber <= snapGridHeight * -1) {
          _me._translateNumber = snapGridHeight * -1;
        }
      } else {
        element = event.findElement('.swiper-button-prev');
        _me._translateNumber = _me._translateNumber + _me._scrollHeight;
        if(_me._translateNumber > _me._swiper.snapGrid[0]) {
          _me._translateNumber = 0;
        }
      }
      _me._swiper.setWrapperTranslate(_me._translateNumber);
    },
        
    _scrollButtonMouseClicked : function(event) {
      var _me = this;
      if (event.type == 'mousedown'){
        event.stop();
        document.observe('mouseup', _me._scrollButtonMouseClickedBind);
        if(_me._periodicalExecuter) {
          _me._periodicalExecuter.stop();
        }
        _me._periodicalExecuter = new PeriodicalExecuter(
            _me._scrollButtonClickBind.curry(event), 0.1);
      } else if (_me._periodicalExecuter){
        document.stopObserving('mouseup', _me._scrollButtonMouseClickedBind);
        _me._periodicalExecuter.stop();
        _me._periodicalExecuter = null;
      }
    },
    
    _setTranslationNumber : function(swiper) {
      var _me = this;
      _me._translateNumber = swiper.getWrapperTranslate();
    },
    
    _initSwiperScrollbar : function(desableSimulateTouch) {
      var _me = this;
      _me._getScrollSlideInner().addClassName('slide-inner');
      _me._getScrollSlide().addClassName('swiper-slide');
      _me._getScrollWrapper().addClassName('swiper-wrapper');
      _me._getScrollSlideInner().select('li.ImageSlide').each(function(element) {
        element.addClassName('swiper-slide');
      });
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
        onSetWrapperTransition : _me._setTranslationNumberBind,
        //Enable Scrollbar
        scrollbar: {
          container : '#' + scrollbarContainerId,
          hide : true,
          draggable : true,
          snapOnRelease : true //XXX does not work so far in swiper swipeReset()
        }
      });
      _me._swiper.setWrapperTransition(300);
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
      if(($$('#sitecontainer .scrollcontainer').size() > 0) && 
          ($$('#sitecontainer .scrollcontainer')[0].getHeight() > 0)) {
        _me._scrollHeight = $$('#sitecontainer .scrollcontainer')[0].getHeight() * 0.8;
      } else {
        _me._scrollHeight = 200;
      }
      scrollContainer.observe('celEndlessScroll:ScrollPosEvent',
          _me._getSwiperScrollOverflowBind);
      $$('.swiper-button-prev, .swiper-button-next').each(function(element) {
        if(_me._swiper.snapGrid[1]) {
          element.observe('click', _me._scrollButtonClickBind);
          element.observe('mousedown', _me._scrollButtonMouseClickedBind);
        } else {
          element.addClassName("inactive");
        }
      });
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
      var listElementsArr = null;
      if ((htmlElems != '') && (htmlElems != null)) {
        var listElements = new Element('div').update(htmlElems).down('ul').children;
        var listElementsArr = Array.prototype.slice.call(listElements);
      }
      if (curContentObj.hasMore != null) {
        _me._hasMore = curContentObj.hasMore;
      } else {
        _me._hasMore = false;
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
        console.info('data provider issued callback with undefined content!');
      }
      scrollerCallbackFN(_me._hasMore);
      _me._startReInitScrollbarDelayed();
      _me._swiper.resizeFix();
      if (typeof initContextMenuAsync !== 'undefined') {
        initContextMenuAsync();
      }
      $$('.swiper-button-prev, .swiper-button-next').each(function(element) {
        if(_me._swiper.snapGrid[1]) {
          element.stopObserving('click', _me._scrollButtonClickBind);
          element.observe('click', _me._scrollButtonClickBind);
          element.stopObserving('mousedown', _me._scrollButtonMouseClickedBind);
          element.observe('mousedown', _me._scrollButtonMouseClickedBind);
          element.removeClassName('inactive');
        } else {
          element.addClassName('inactive');
        }
      });
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
    if($$('.presentationList').size() > 0) {
      var column = new CEL.Column('.presentationList');
      column._initSwiperScrollbar();
    }
  });
  
})(window);
