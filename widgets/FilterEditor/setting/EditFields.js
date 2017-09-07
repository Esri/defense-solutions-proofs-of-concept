///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2017 Esri All Rights Reserved.
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

define(
  ["dojo/_base/declare",
    "dojo/_base/lang",
    "dojo/_base/array",
    "dojo/text!./EditFields.html",
    'dijit/_TemplatedMixin',
    'jimu/BaseWidgetSetting',
    'jimu/dijit/SimpleTable',
    "jimu/dijit/Popup"
  ],
  function(
    declare,
    lang,
    array,
    template,
    _TemplatedMixin,
    BaseWidgetSetting,
    Table,
    Popup) {
    return declare([BaseWidgetSetting, _TemplatedMixin], {
      baseClass: "jimu-widget-edit-setting-fields",
      templateString: template,
      _layerInfo: null,

      postCreate: function() {
        this.inherited(arguments);
        this.nls = lang.mixin(this.nls, window.jimuNls.common);
        this._initFieldsTable();
        this._setFiedsTabele(this._layerInfo.fieldInfos);
      },

      popupEditPage: function() {
        var fieldsPopup = new Popup({
          titleLabel: this.nls.configureFields,
          width: 640,
          maxHeight: 600,
          autoHeight: true,
          content: this,
          buttons: [{
            label: this.nls.ok,
            onClick: lang.hitch(this, function() {
              this._resetFieldInfos();
              fieldsPopup.close();
            })
          }, {
            label: this.nls.cancel,
            onClick: lang.hitch(this, function() {
              fieldsPopup.close();
            })
          }],
          onClose: lang.hitch(this, function() {
          })
        });
      },

      _initFieldsTable: function() {
        var fields2 = [{
          name: 'isEditable',
          title: this.nls.edit,
          type: 'checkbox',
          'class': 'editable'
        }, {
          name: 'fieldName',
          title: this.nls.editpageName,
          type: 'text'
        }, {
          name: 'label',
          title: this.nls.editpageAlias,
          type: 'text',
          editable: true
        }, {
          name: 'actions',
          title: this.nls.actions,
          type: 'actions',
          actions: ['up', 'down'],
          'class': 'editable'
        }];
        var args2 = {
          fields: fields2,
          selectable: false,
          style: {
            'height': '300px',
            'maxHeight': '300px'
          }
        };
        this._fieldsTable = new Table(args2);
        this._fieldsTable.placeAt(this.fieldsTable);
        this._fieldsTable.startup();
      },

      _setFiedsTabele: function(fieldInfos) {
        array.forEach(fieldInfos, function(fieldInfo) {
          this._fieldsTable.addRow({
            fieldName: fieldInfo.fieldName,
            isEditable: fieldInfo.isEditable,
            label: fieldInfo.label
          });
        }, this);
      },

      _resetFieldInfos: function() {
        var newFieldInfos = [];
        var fieldsTableData =  this._fieldsTable.getData();
        array.forEach(fieldsTableData, function(fieldData) {
          newFieldInfos.push({
            "fieldName": fieldData.fieldName,
            "label": fieldData.label,
            "isEditable": fieldData.isEditable
          });
        });

        this._layerInfo.fieldInfos = newFieldInfos;
      }

    });
  });