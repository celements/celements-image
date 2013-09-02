(function(window, undefined) {

  var galleryHideDesc= function(event) {
    var galleryLink = event.findElement('.celements_gallery_link');
    if (galleryLink) {
      galleryLink.down('.text').hide();
      galleryLink.down('.background').hide();
    }
  };

  var galleryShowDesc = function(event) {
    var galleryLink = this;
    if (galleryLink) {
      galleryLink.down('.text').show();
      galleryLink.down('.background').show();
    }
  };

  var galleryMobileFix = function() {
    $$('.celements_gallery_link .text, .celements_gallery_link .background').each(
      function(galleryElem) {
        galleryElem.observe('click', galleryHideDesc);
        galleryElem.observe('mouseover', galleryHideDesc);
        galleryElem.up('.celements_gallery_link').observe('mouseout', galleryShowDesc);
    });
    $$('.celements_gallery_link').each(function(galleryElem) {
        galleryElem.observe('mouseout', galleryShowDesc);
    });
  };

  $j(document).ready(function() {
    $(document.body).fire('cel_ImageSlideShow:finishedRegister', galleryMobileFix);
    galleryMobileFix();
  });

})(window);