///////////////////////////////////////////////////////////////////////////
// Copyright © 2014 Esri. All Rights Reserved.
//
// Licensed under the Apache License Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////

define([
    'dojo/_base/declare',
    'dojo/_base/array',
    'dojo/_base/lang',
    'dojo/query', 
    'dojo/dom-style',
    'dojo/dom-construct',
    'dojo/text!./AddDialog.html',
    'dojo/NodeList-dom',
    'dojo/Deferred',
    'dojo/topic',

    'dijit/_WidgetsInTemplateMixin',
    'dijit/_TemplatedMixin',
    'dijit/form/MultiSelect',

    'esri/request',
    'esri/dijit/util/busyIndicator',    

    'jimu/BaseWidgetSetting',
    'jimu/dijit/Message'    
  ],
  function(
    declare,
    array,
    lang,
    query,
    domStyle,
    domConstruct,    
    AddDialog,
    nodeListDom,
    Deferred,
    topic,
    _WidgetsInTemplateMixin,
    _TemplatedMixin, 
    MultiSelect,
    esriRequest,
    busyIndicator,    
    BaseWidgetSetting,
    JimuMessage) {
    return declare([BaseWidgetSetting, _TemplatedMixin, _WidgetsInTemplateMixin], {
      templateString: AddDialog,

      showMediaFields: false,
      mapServiceUrl: null,
      allowMultipleSelection: null,
      _busyIndicator: null,
      _selectDiv: null,
      _fieldsList: null,

      constructor: function(mapServiceUrl, showMediaFields, allowMultipleSelection) {
        this.showMediaFields = showMediaFields || false;
        this.allowMultipleSelection = allowMultipleSelection || false;
        this.mapServiceUrl = mapServiceUrl;          
      },

      postCreate: function() {
        this.inherited(arguments);

        this._toggleMediaFields(this.showMediaFields);

        var busyIndicatorParams = {
          target: this.addDialog
        };
        this._busyIndicator = busyIndicator.create(busyIndicatorParams);        
        this._busyIndicator.show();

        var fieldRequest = this._getFields(this.mapServiceUrl);
        fieldRequest.then(lang.hitch(this, function(results) {
          if (results) {
            this._populateFieldsList(results);
            topic.publish("erg-setting-get-fields-completed", {isMedia: this.showMediaFields});
          } else {
            new JimuMessage({message: 'Unable to retrieve fields from specified map service'});
          }
          this._busyIndicator.hide();
        }), function(error) {
          console.log(error.message);
        });                          
      },

      getSelectedFields: function() {    
        var fldArr = array.filter(this.selectFields.options, function(option) {
          if (option.selected) {
            return option;
          }
        });
        var returnArr = array.map(fldArr, function(fld) {
          return fld.value;
        })
        return returnArr;
      },

      getTitle: function() {
        return this.titleInput.value;
      },

      getDescription: function() {
        return this.descriptionInput.value;
      },

      getChartType: function() {
        return this.chartType.options[this.chartType.selectedIndex].value;
      },

      clear: function() {
        this.titleInput.value = "";
        this.descriptionInput.value = "";
        this.chartType.selectedIndex = 0;
        this.selectFields.selectedIndex = 0;
      },

      populateMediaFields: function(data) {
        if (data.title) {
          this.titleInput.value = data.title;
        }
        if (data.description) {
          this.descriptionInput.value = data.description;
        }
        if (data.chartType) {
          for(var i=0; i<this.chartType.options.length; i++) {
            if (this.chartType.options[i].value === data.chartType) {
              this.chartType.selectedIndex = i;
              break;
            }
          }
        }

        if (data.chartField) {       
          for(var j=0; j<this.selectFields.options.length; j++) {
            if (this.selectFields.options[j].value === data.chartField) {
              this.selectFields.selectedIndex = j;
              break;
            }
          }          
        }
      },

      _populateFieldsList: function(fields) {  
        if (this.selectFields) {          
          var fieldsOptions = [];
          array.forEach(fields, function(field){  
             var opt = document.createElement("option");
             opt.value = field.name;
             opt.innerHTML = field.name;
             this.selectFields.appendChild(opt);
          }, this);     
          if (this.allowMultipleSelection) {
            this.selectFields.setAttribute("multiple", this.allowMultipleSelection);
          } 
        }       
      },

      _getFields: function (mapServiceUrl) {
        var deferred = new Deferred();
        if (!mapServiceUrl) {
          deferred.resolve(null);
        } else if (mapServiceUrl.length === 0) {
          deferred.resolve(null);
        } else {        
          var mapRequest = esriRequest({
            url : mapServiceUrl,
            content : {f : 'json'},
            handleAs: 'json',
            callbackParamName : 'callback'
          });
          mapRequest.then(lang.hitch(this, function(response) {
            //show field names and aliases
            if (response.hasOwnProperty('fields')) {
              var fieldInfo = array.map(response.fields, lang.hitch(this, function(f) {
                return {
                  label : f.alias,
                  name : f.name,
                  fieldType : f.type
                };
              }));
              deferred.resolve(fieldInfo);
            }
            deferred.resolve(null);
          }), function() {
            //error callback
            deferred.resolve(null);
          });
        }
        return deferred.promise;
      },

      _toggleMediaFields: function(show) {
        if (!show) {
          var nodeList = query(".mediaFields", this.addDialog);
          array.forEach(nodeList, function(node) {
            domStyle.set(node, "display", "none");
          }, this);
        }
      },

      _setShowMediaFieldsAttr: function(show) {
        this._set("showMediaFields", show);
        this._toggleMediaFields(this.showMediaFields);
      },

      _setAllowMultipleSelectionAttr: function(show) {
        this._set("allowMultipleSelection", show);
      }

    });
  });