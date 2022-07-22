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

  const closePreimport = function(event){
    if(!confirm($('c3_not_yet_imported_msg').value)){
      event.stop();
    }
  };
  
  const nothingToImport = function(){
    alert($('c3_nothing_to_import_msg').value);
  };
  
  const overwriteOnImport = function(event){
    if(!confirm($('c3_overwrite_on_import_msg').value)){
      event.stop();
    }
  };
  
  const doesOverwrite = function(){
    let hasOverwrite = false;
    $$('.cel_photo_overwrite').each(function(ele){
      hasOverwrite |= !(ele.getStyle('display') == 'none');
    });
    return hasOverwrite;
  };
  
  const countChecked = function(){
    const checkboxes = $$('.c3_import_checkbox_element');
    let checkedFiles = 0;
    checkboxes.each(function(chkbox){
      if(chkbox.checked){ checkedFiles++; }
    });
    $('c3_import_count_files').innerHTML = checkedFiles;
    $('c3_import_count_total').innerHTML = checkboxes.size();
    
    //stop observing to prevent double observes
    $('c3_import_button_div').stopObserving('click', nothingToImport);
    $('c3_import_button').stopObserving('click', overwriteOnImport);
    if(checkedFiles > 0){
      console.debug('activate import button');
      $('c3_import_button').disabled = false;
      if(doesOverwrite()){
        $('c3_import_button').observe('click', overwriteOnImport);
      }
    } else{
      $('c3_import_button').disabled = true;
      $('c3_import_button_div').observe('click', nothingToImport);
    }
  };
  
  const changeImportAction = function(elem) {
    const chkboxspan = elem.up('.c3_import_row');
    const actionspan = chkboxspan.down('.c3_import_action', 0);
    const actionspanskip = chkboxspan.down('.c3_import_action', 1);
    if(elem.checked){
      console.debug('show action, hide skip');
      actionspan.setStyle({display: ''});
      actionspanskip.setStyle({display: 'none'});
    } else {
      console.debug('show skip, hide action');
      actionspan.setStyle({display: 'none'});
      actionspanskip.setStyle({display: ''});
    }
  };
  
  const changeImportActionEvent = function(event) {
    changeImportAction(event.element());
  };
  
  const changeAndCount = function(event){
    console.debug('clicked check box');
    changeImportActionEvent(event);
    countChecked();
  };
  
  const preimport = function(event){
    const origEvent = event.memo;
    let src = "";
    let filename = "";
    const element = origEvent.target.down('.c3_file_link');
    element.siblings().each(function(sibl){
      if(sibl.name == 'c3_fb_file_src'){
        src = sibl.value;
      } else if(sibl.name == 'c3_fb_full_file_name'){
        filename = sibl.value;
      }
    });
    
    const url = $('c3_preimport_url').value + "&attDoc=" + src + "&filename=" + filename;
    getProgressBar($('c3_title_preimport').value);
    new Ajax.Request(url, { 
      method: 'post', 
      onComplete: function(transport){
        $('c3_import_box').innerHTML = transport.responseText;
        $('c3_import_box').fire("preimport:changed");
        resizeTab();
    }});
  };
  
  const importNow = function(event) {
    event.stop();
    console.debug('start progress bar');
    getProgressBar($('c3_title_importing').value);
    console.debug('start ajax');
    console.debug('url is: "' + $('c3_import_url').value + '"');
    console.debug('params are: "' + $('importForm').serialize(true) + '"');
    new Ajax.Request($('c3_import_url').value, {
      parameters : $('importForm').serialize(true),
      onComplete : function(transport){
        $('c3_import_box').innerHTML = transport.responseText;
        $('c3_import_box').fire("preimport:changed");
        resizeTab();
      }
    });
  };

  const preimportChanged = function() {
    console.debug('import box content is ready');
    $$('.c3_import_checkbox_element').each(function(chkbox){
      chkbox.observe('click', changeAndCount);
    });
    if($('importForm')){
      $('closebutton').observe('click', closePreimport);
      $('importForm').observe('submit', importNow);
      countChecked();
    } else {
      $('closebutton').stopObserving('click', closePreimport);
    }
    updateObservers();
    if($('check_all')){
      $('check_all').observe('click', function(){
        $$('.c3_import_checkbox_element').each(function(chkbox){
          chkbox.checked=true;
          changeImportAction(chkbox);
        });
        countChecked();
      });
    }
    if($('check_none')){
      $('check_none').observe('click', function() {
        $$('.c3_import_checkbox_element').each(function(chkbox){
          chkbox.checked=false;
          changeImportAction(chkbox);
        });
        countChecked();
      });
    }
  };
  
  const registerListener = function() {
    $('c3_import_box').stopObserving('preimport:changed', preimportChanged);
    $('c3_import_box').observe('preimport:changed', preimportChanged);
    $('c3_import_box').stopObserving('preimport:clickOnRow', preimport);
    $('c3_import_box').observe('preimport:clickOnRow', preimport);
  };

  $j(document).ready(function() {
    $(document.body).observe('preimport:beforeShowBox', registerListener);
  });

})(window);
