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
CELEMENTS.image.SlideShow = function(containerId) {
  containerId = containerId || 'yuiOverlayContainer';
  // constructor
  this._init(containerId);
};

(function() {
  CELEMENTS.image.SlideShow.prototype = {
      _openInOverlayBind : undefined,
      _isOverlayRegistered : false,

      _init : function(containerId) {
        var _me = this;
        _me._openInOverlayBind = _me.openInOverlay.bind(_me);
      },

      registerOpenInOverlay : function(htmlElem) {
        var _me = this;
        if (!_me._isOverlayRegistered) {
          _me.isOverlayRegistered = true;
          var bodyElem = $$('body')[0];
          bodyElem.observe('cel_slideShow:shouldRegister',
              _me._checkIsImageSlideShowOverlay.bind(_me));
          bodyElem.observe('cel_yuiOverlay:afterShowDialog_General',
              _me._removeIsImageSlideShowOverlay.bind(_me));
        }
        htmlElem.observe('click', _me._openInOverlayBind);
        htmlElem.observe('cel_ImageSlideShow:startSlideShow', _me._openInOverlayBind);
      },

      _checkIsImageSlideShowOverlay : function(event) {
        var openDialog = CELEMENTS.presentation.getOverlayObj();
        if (openDialog.slideShowElem) {
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
        var hasCloseButton = htmlElem.hasClassName('celimage_overlay_addCloseButton');
        var openDialog = CELEMENTS.presentation.getOverlayObj({
          'close' : hasCloseButton,
          'slideShowElem' : htmlElem
        });
        openDialog.open();
      }

  };
})();

})();
