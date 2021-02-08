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

  let galleryHideDesc= function(event) {
    let galleryLink = event.findElement('.celements_gallery_link');
    if (galleryLink) {
      galleryLink.down('.text').hide();
      galleryLink.down('.background').hide();
    }
  };

  let galleryShowDesc = function(event) {
    let galleryLink = this;
    if (galleryLink) {
      galleryLink.down('.text').show();
      galleryLink.down('.background').show();
    }
  };

  let galleryMobileFix = function() {
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