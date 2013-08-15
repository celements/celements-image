/**
 * Celements image Slideshow
 * This is the Celements image Slideshow controller.
 */
if(typeof CELEMENTS=="undefined"){var CELEMENTS={};};
if(typeof CELEMENTS.image=="undefined"){CELEMENTS.image={};};

(function() {

  var CISS_OverlaySlideShowObj = undefined;

  $j(document).ready(function() {
    if (!CISS_OverlaySlideShowObj) {
      CISS_OverlaySlideShowObj = new CELEMENTS.image.SlideShow();
    }
    $$('.celimage_slideshow').each(function(slideShowElem) {
      if (slideShowElem.hasClassName('celimage_overlay')) {
        CISS_OverlaySlideShowObj.registerOpenInOverlay(slideShowElem);
      }
    });
  });

//////////////////////////////////////////////////////////////////////////////
// Celements image Slideshow
//////////////////////////////////////////////////////////////////////////////
CELEMENTS.image.SlideShow = function(htmlElem) {
  containerId = htmlElem;
  // constructor
  this._init(htmlElem);
};

(function() {
  CELEMENTS.image.SlideShow.prototype = {
      _openInOverlayBind : undefined,
      _imageSlideShowLoadFirstContentBind : undefined,
      _addNavigationButtonsBind : undefined,
      _celSlideShowObj : null,
      _isOverlayRegistered : false,
      _currentHtmlElem : undefined,

      _init : function(htmlElem) {
        var _me = this;
        _me._currentHtmlElem = $(htmlElem) || null;
        _me._openInOverlayBind = _me.openInOverlay.bind(_me);
        _me._imageSlideShowLoadFirstContentBind =
          _me._imageSlideShowLoadFirstContent.bind(_me);
        _me._addNavigationButtonsBind = _me._addNavigationButtons.bind(_me);
      },

      registerOpenInOverlay : function(htmlElem) {
        var _me = this;
        if (!_me._isOverlayRegistered) {
          _me._isOverlayRegistered = true;
          _me._celSlideShowObj = getCelSlideShowObj();
          _me._celSlideShowObj.setOverwritePageLayout('SimpleLayout');
          var bodyElem = $$('body')[0];
          bodyElem.observe('cel_slideShow:shouldRegister',
              _me._checkIsImageSlideShowOverlay.bind(_me));
          bodyElem.observe('cel_yuiOverlay:hideEvent',
              _me._removeIsImageSlideShowOverlay.bind(_me));
        }
        htmlElem.observe('click', _me._openInOverlayBind);
        htmlElem.observe('cel_ImageSlideShow:startSlideShow', _me._openInOverlayBind);
        $(document.body).observe('cel_yuiOverlay:loadFirstContent',
            _me._imageSlideShowLoadFirstContentBind);
      },

      _getCelSlideShowObj : function() {
        var _me = this;
        if (!_me._celSlideShowObj) {
          _me._celSlideShowObj = new new CELEMENTS.presentation.SlideShow(
              _me._currentHtmlElem.id);
          _me._celSlideShowObj.setOverwritePageLayout('SimpleLayout');
        }
        return _me._celSlideShowObj;
      },

      _getContainerElement : function() {
        var _me = this;
        if (_me._isOverlayRegistered) {
          return _me._getCelSlideShowObj().getHtmlContainer();
        } else {
          return _me._currentHtmlElem;
        }
      },

      _addNavigationButtons : function(event) {
        var _me = this;
        if (_me._currentHtmlElem.hasClassName('celimage_addNavigation')) {
          var nextButton = new Element('div').addClassName('celPresSlideShow_next');
          var prevButton = new Element('div').addClassName('celPresSlideShow_prev');
          _me._getContainerElement().insert({'bottom' : nextButton});
          _me._getContainerElement().insert({'top' : prevButton});
        }
      },

      _imageSlideShowLoadFirstContent : function(event) {
        var _me = this;
        var dialogConfig = event.memo;
        if (dialogConfig.slideShowElem) {
          _me._getCelSlideShowObj().getHtmlContainer().observe(
              'cel_yuiOverlay:afterContentChanged', _me._addNavigationButtonsBind);
          var gallerySpace = _me._getPart(_me._currentHtmlElem.id, 6, '');
          _me._getCelSlideShowObj().loadMainSlides(gallerySpace);
          event.stop();
        }
      },

      _checkIsImageSlideShowOverlay : function(event) {
        var _me = this;
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        if (openDialog._dialogConfig.slideShowElem) {
          event.stop();
        }
      },

      _removeIsImageSlideShowOverlay : function() {
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        openDialog.updateOpenConfig({ 'slideShowElem' : null });
      },

      openInOverlay : function(event) {
        var _me = this;
        var htmlElem = event.element();
        _me._currentHtmlElem = htmlElem;
        var hasCloseButton = htmlElem.hasClassName('celimage_overlay_addCloseButton');
        var openDialog = CELEMENTS.presentation.getOverlayObj({
          'close' : hasCloseButton,
          'slideShowElem' : htmlElem,
          'link' : htmlElem
        });
        openDialog.intermediatOpenHandler();
      },

      _getPart : function(elemId, num, defaultvalue) {
        var parts = elemId.split(':');
        if ((num < parts.length) && (parts[num] != '')) {
          return parts[num];
        } else {
          return defaultvalue;
        }
      }

  };
})();

})();
