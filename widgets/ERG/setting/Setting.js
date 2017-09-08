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
    'dojo/_base/lang',
    'dojo/_base/array',
    'dojo/on',
    'dojo/string',
    'dojo/topic',
    'dijit/_WidgetsInTemplateMixin',
    'jimu/BaseWidgetSetting',
    'jimu/dijit/Message',
    'jimu/dijit/SimpleTable',
    './AddDialog',
    './DialogConfirm'
  ],
  function(
    declare,
    lang,
    array,
    on,
    string,
    topic,
    _WidgetsInTemplateMixin,
    BaseWidgetSetting,
    JimuMessage,
    SimpleTable,
    AddDialog,
    DialogConfirm) {
    return declare([BaseWidgetSetting, _WidgetsInTemplateMixin], {

      baseClass: 'jimu-widget-erg-setting',
      weatherFieldNameDlg: null,
      stationNameFieldDlg: null,
      dateTimeFieldDlg: null,
      facilitiesFieldsDlg: null,
      facilitiesLabelDlg: null,
      demoMediaDlg: null,
      demoLabelAddFieldDlg: null,
      mediaTable: null,
      tableFields: null,
      selectedDemoRow: null,

      postCreate: function() {
        this.inherited(arguments);

        this.tableFields = [{
          name: 'media',
          title: 'Media',
          type: 'text'
        }, {
          name: 'action',
          title: 'Action',
          type: 'actions',
          actions: ['up','down','edit','delete']
        }, {
          name: 'title',
          title: 'Title',
          type: 'text',
          hidden: true
        }, {          
          name: 'description',
          title: 'Description',
          type: 'text',
          hidden: true
        }, {
          name: 'chartType',
          title: 'Chart Type',
          type: 'text',
          hidden: true          
        },{          
          name: 'chartField',
          title: 'Field',
          type: 'text',
          hidden: true
        }];                

      },

      startup: function() {
        this.inherited(arguments);
        if (typeof(this.config) === 'undefined') {
          this.config = {};          
        }
        this.setConfig(this.config);  

        var equalsIndex = window.location.href.indexOf("=");
        var appId = window.location.href.substring(equalsIndex+1);
        var mod = string.substitute('jimu/loaderplugins/jquery-loader!./apps/${id}/widgets/ERG/setting/jquery-git1.min.js', {id: appId});

        require([mod], function($) {
          var $heads = $(".toggle-container .toggle-header");
          $heads.click(function () {
              var $this = $(this);
              $this.find('i').toggleClass('icon-chevron-sign-down icon-chevron-sign-up');
              $this.next().slideToggle("slow");
          });           

        });

        this.syncEvents();                      
        this.syncTopics();
      },

      syncTopics: function() {
        topic.subscribe("erg-setting-get-fields-completed", lang.hitch(this, function(isMedia) {
          if (isMedia) {
            this._editDemoData();
          }
        }));
      },      

      syncEvents: function() {
        this.own(on(this.windDirectionAddField, "click", lang.hitch(this, 
          this.onWindDirectionAddFieldClick)));
        this.own(on(this.stationNameAddField, "click", lang.hitch(this, 
          this.onStationNameFieldClick)));
        this.own(on(this.dateTimeAddField, "click", lang.hitch(this, this.onDateTimeFieldClick)));
        this.own(on(this.facilitiesAddFields, "click", lang.hitch(this, 
          this.onFacilitiesFieldsClick)));
        this.own(on(this.facilitiesLabelAddField, "click", lang.hitch(this, 
          this.onFacilitiesLabelAddFieldClick)));
        this.own(on(this.demoAddMedia, "click", lang.hitch(this, this.onDemoAddMediaClick)));
        this.own(on(this.demoLabelAddField, "click", lang.hitch(this, 
          this.onDemoLabelAddFieldClick)));

        this.own(on(this.mediaTable, 'row-up', lang.hitch(this, function() {
          this._refreshMediaTable();
        })));
        this.own(on(this.mediaTable, 'row-down', lang.hitch(this, function() {
          this._refreshMediaTable();
        })));        
        this.own(on(this.mediaTable, 'actions-edit', lang.hitch(this, function(tr) {
          this.selectedDemoRow = tr;          
          if (!this.demoMediaDlg) {
            this._initEditDemoDialog();
          } else{
            this._editDemoData();
          }    
        }))); 
        this.own(on(this.mediaTable, 'row-delete', lang.hitch(this, function() {
          this._refreshMediaTable();
        })));           

      },

      _editDemoData: function() {
        if (this.demoMediaDlg) {
          var rowData = this.mediaTable.getRowData(this.selectedDemoRow);
          this.demoMediaDlg.content.populateMediaFields(rowData);
          this.demoMediaDlg.show().then(lang.hitch(this, function() {
            var idx = this.selectedDemoRow.rowIndex;
            this.mediaTable.deleteRow(this.selectedDemoRow);
            var rowParam = {
              media: this.demoMediaDlg.content.getTitle(),
              chartField: this.demoMediaDlg.content.getSelectedFields()[0],
              title: this.demoMediaDlg.content.getTitle(),
              description: this.demoMediaDlg.content.getDescription(),
              chartType: this.demoMediaDlg.content.getChartType()
            };
            this.mediaTable.addRow(rowParam, idx);              
          })); 
        }
      },

      _refreshMediaTable: function() {
        this.mediaTable.updateUI();
      },

      onDemoLabelAddFieldClick: function() {
        if (this.demographicLayer.value.length !== 0) {
          var params = {
            windDirectionService: this.demographicLayer.value,
            dialog: this.demoLabelAddFieldDlg, 
            dialogTitle: "Label Field Name",
            showMediaFields: false,
            allowMultipleSelection: false,
            inputField: this.demographicLabelField,
            errorMessage: "Please enter a valid service for demographic layer"
          };
          this._showAddFieldDialog(params);
        } else {
          new JimuMessage({message: 'Please enter a valid service for demographic layer'});
        }
      },

      onDemoAddMediaClick: function() {
        if (this.demographicLayer.value.length !== 0) {
          var params = {
            windDirectionService: this.demographicLayer.value,
            dialog: this.demoMediaDlg, 
            dialogTitle: "Media Field Name",
            showMediaFields: true,
            allowMultipleSelection: false,
            inputField: null,
            errorMessage: "Please enter a valid service for demographic layer"
          };
          this._showAddFieldDialog(params);
        } else {
          new JimuMessage({message: 'Please enter a valid service for demographic layer'});
        }
      },

      onFacilitiesLabelAddFieldClick: function() {
        if (this.facilitiesLayer.value.length !== 0) {
          var params = {
            windDirectionService: this.facilitiesLayer.value,
            dialog: this.facilitiesLabelDlg, 
            dialogTitle: "Facilities Label Field Name",
            showMediaFields: false,
            allowMultipleSelection: false,
            inputField: this.facilitiesLabelField,
            errorMessage: "Please enter a valid service for facilities"
          };
          this._showAddFieldDialog(params);
        } else {
          new JimuMessage({message: 'Please enter a valid service for Facilities'});
        }
      },

      onFacilitiesFieldsClick: function() {
        if (this.facilitiesLayer.value.length !== 0) {
          var params = {
            windDirectionService: this.facilitiesLayer.value,
            dialog: this.facilitiesFieldsDlg, 
            dialogTitle: "Station Field Name",
            showMediaFields: false,
            allowMultipleSelection: true,
            inputField: this.facilitiesFields,
            errorMessage: "Please enter a valid service for facilities"
          };
          this._showAddFieldDialog(params);
        } else {
          new JimuMessage({message: 'Please enter a valid service for Facilities'});
        }
      },

      onDateTimeFieldClick: function() {
        if (this.windDirectionLayer.value.length !== 0) {
          var params = {
            windDirectionService: this.windDirectionLayer.value,
            dialog: this.dateTimeFieldDlg, 
            dialogTitle: "Station Field Name",
            showMediaFields: false,
            allowMultipleSelection: false,
            inputField: this.dateTimeField,
            errorMessage: "Please enter a valid service for Wind Direction"
          };
          this._showAddFieldDialog(params);
        } else {
          new JimuMessage({message: 'Please enter a valid service for Wind Direction'});
        }
      },

      onStationNameFieldClick: function() {
        if (this.windDirectionLayer.value.length !== 0) {
          var params = {
            windDirectionService: this.windDirectionLayer.value,
            dialog: this.stationNameFieldDlg, 
            dialogTitle: "Date Time Field Name",
            showMediaFields: false,
            allowMultipleSelection: false,
            inputField: this.stationNameField,
            errorMessage: "Please enter a valid service for Wind Direction"
          };
          this._showAddFieldDialog(params);
        } else {
          new JimuMessage({message: 'Please enter a valid service for Wind Direction'});
        }
      },

      onWindDirectionAddFieldClick: function() {
        if (this.windDirectionLayer.value.length !== 0) {
          var params = {
            windDirectionService: this.windDirectionLayer.value,
            dialog: this.weatherFieldNameDlg, 
            dialogTitle: "Wind Direction Field Name",
            showMediaFields: false,
            allowMultipleSelection: false,
            inputField: this.windDirectionField,
            errorMessage: "Please enter a valid service for Wind Direction"
          };
          this._showAddFieldDialog(params);
        } else {
          new JimuMessage({message: 'Please enter a valid service for Wind Direction'});
        }        
      },      

      setConfig: function(config) {
        this.config = config;
        this.chemicalGPService.set("value", this.config.chemicalGPService.url);
        this.placardGPService.set("value", this.config.placardGPService.url);
        this.weatherStationGPService.set("value", this.config.weatherStationGPService.url);
        this.windDirectionLayer.set("value", this.config.windDirectionLayer.url);
        this.facilitiesLayer.set("value", this.config.facilitiesLayer.url);
        this.demographicLayer.set("value", this.config.demographicLayer.url);
        this.windDirectionField.set("value", this.config.windDirectionLayer.windDirectionField);
        this.stationNameField.set("value", this.config.windDirectionLayer.stationNameField);
        this.dateTimeField.set("value", this.config.windDirectionLayer.dateTimeField);
        this.facilitiesLabelField.set("value", this.config.facilitiesLayer.labelField);
        this.facilitiesFields.set("value", this.config.facilitiesLayer.fields.join(","));
        this.demographicLabel.set("value", this.config.demographicLayer.label);
        this.demographicLabelField.set("value", this.config.demographicLayer.labelField);


        if (this.config.demographicLayer.medias && this.config.demographicLayer.medias.length > 0) {
          var mediaFieldArgs = {
            fields: this.tableFields,
            selectable: false
          };
          if (!this.mediaTable) {
            this.mediaTable = new SimpleTable(mediaFieldArgs);
            this.mediaTable.placeAt(this.tableFieldInfos);
            this.mediaTable.startup();
          } else {
            this.mediaTable.clear();
          }

          array.forEach(this.config.demographicLayer.medias, lang.hitch(this, function(media) {
            var rowParam = {
              media: media.title,
              chartField: media.chartField,
              title: media.title,
              description: media.description,
              chartType: media.type
            };
            this.mediaTable.addRow(rowParam);             
          }));
        }
      },

      getConfig: function() {
        this.config.chemicalGPService = {
          url: this.chemicalGPService.value
        };
        this.config.placardGPService = {
          url: this.placardGPService.value
        };
        this.config.weatherStationGPService = {
          url: this.weatherStationGPService.value
        };
        this.config.windDirectionLayer = {
          url: this.windDirectionLayer.value,
          windDirectionField: this.windDirectionField.value,
          stationNameField: this.stationNameField.value,
          dateTimeField: this.dateTimeField.value      
        };
        this.config.facilitiesLayer = {
          url: this.facilitiesLayer.value,
          fields: this.facilitiesFields.value.split(","),
          labelField: this.facilitiesLabelField.value          
        };

        if (this.mediaTable) {
          var medias = [];
          var rows = this.mediaTable.getRows();
          array.forEach(rows, function(row) {
            var rowData = this.mediaTable.getRowData(row);
            var media = {
              chartField: rowData.chartField,
              title: rowData.title,
              description: rowData.description,
              type: rowData.chartType
            };
            medias.push(media);
          }, this);          
        }

        this.config.demographicLayer = {
          url: this.demographicLayer.value,        
          label: this.demographicLabel.value,
          labelField: this.demographicLabelField.value,
          fields: ["*"],
          medias: medias
        };                                
        return this.config;
      },

      _initEditDemoDialog: function() {
        if (this.demographicLayer.value.length !== 0) {
          if (!this.demoMediaDlg) {
            this.demoMediaDlg = new DialogConfirm({
              title: "Media Field Name", 
              content: new AddDialog(this.demographicLayer.value, true, false), 
              style: "width: 400px",
              hasSkipCheckBox: false
            });            
          }
        } else {
          new JimuMessage({message: 'Please enter a valid service for demographic layer'});
        }        
      },      

      _showAddFieldDialog: function(addFieldParams) {
        /**
          @property windDirectionService
          @property dialog
          @property dialogTitle
          @property showMediaFields
          @property allowMultipleSelection
          @property inputField 
          @property errorMessage         
        */       
        if (!addFieldParams.dialog) {
          addFieldParams.dialog = new DialogConfirm({
            title: addFieldParams.dialogTitle, 
            content: new AddDialog(
              addFieldParams.windDirectionService, 
              addFieldParams.showMediaFields, 
              addFieldParams.allowMultipleSelection), 
            style: "width: 400px",
            hasSkipCheckBox: false
          });
        }
        if (addFieldParams.showMediaFields) {
          addFieldParams.dialog.content.clear();
        }
        addFieldParams.dialog.show().then(lang.hitch(this, function(item) {
          var fieldsList = addFieldParams.dialog.content.getSelectedFields();
          if (addFieldParams.inputField) {
            addFieldParams.inputField.set("value", fieldsList.join(","));
          }

          if (addFieldParams.showMediaFields) {
            var rowParam = {
              media: addFieldParams.dialog.content.getTitle(),
              chartField: fieldsList[0],
              title: addFieldParams.dialog.content.getTitle(),
              description: addFieldParams.dialog.content.getDescription(),
              chartType: addFieldParams.dialog.content.getChartType()
            };
            this.mediaTable.addRow(rowParam);
          }
        }, function(error) {
          new JimuMessage({message: error.message});
        }));
      }     
    });
  });