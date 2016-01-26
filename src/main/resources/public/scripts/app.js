var app = angular.module('habridge', [
	'ngRoute'
]);

app.config(function ($routeProvider) {
	$routeProvider.when('/#', {
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	}).when('/editor', {
		templateUrl: 'views/editor.html',
		controller: 'AddingController'		
	}).when('/editdevice', {
		templateUrl: 'views/editdevice.html',
		controller: 'AddingController'		
	}).when('/veradevices', {
		templateUrl: 'views/veradevice.html',
		controller: 'AddingController'		
	}).when('/verascenes', {
		templateUrl: 'views/verascene.html',
		controller: 'AddingController'		
	}).when('/harmonydevices', {
		templateUrl: 'views/harmonydevice.html',
		controller: 'AddingController'		
	}).when('/harmonyactivities', {
		templateUrl: 'views/harmonyactivity.html',
		controller: 'AddingController'		
	}).when('/nest', {
		templateUrl: 'views/nestactions.html',
		controller: 'AddingController'		
	}).otherwise({
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	})
});

app.run( function (bridgeService) {
	bridgeService.loadBridgeSettings();
	bridgeService.getHABridgeVersion();	
});

app.factory('BridgeSettings', function() {
	var BridgeSettings = {};
	
	BridgeSettings.upnpconfigaddress = "";
	BridgeSettings.serverport = "";
	BridgeSettings.upnpdevicedb = "";
	BridgeSettings.upnpresponseport = "";
	BridgeSettings.veraaddress = "";
	BridgeSettings.harmonyaddress = "";
	BridgeSettings.upnpstrict = "";
	BridgeSettings.traceupnp = "";
	BridgeSettings.devmode = "";
	BridgeSettings.nestconfigured = "";
	
	BridgeSettings.setupnpconfigaddress = function(aconfigaddress){
		BridgeSettings.upnpconfigaddress = aconfigaddress;
	};
	
	BridgeSettings.setserverport = function(aserverport){
		BridgeSettings.serverport = aserverport;
	};
	
	BridgeSettings.setupnpdevicedb = function(aupnpdevicedb){
		BridgeSettings.upnpdevicedb = aupnpdevicedb;
	};
	
	BridgeSettings.setupnpresponseport = function(aupnpresponseport){
		BridgeSettings.upnpresponseport = aupnpresponseport;
	};
	
	BridgeSettings.setveraaddress = function(averaaddress){
		BridgeSettings.veraaddress = averaaddress;
	};
	BridgeSettings.setharmonyaddress = function(aharmonyaddress){
		BridgeSettings.harmonyaddress = aharmonyaddress;
	};
	BridgeSettings.setupnpstrict = function(aupnpstrict){
		BridgeSettings.upnpstrict = aupnpstrict;
	};
	BridgeSettings.settraceupnp = function(atraceupnp){
		BridgeSettings.traceupnp = atraceupnp;
	};
	BridgeSettings.setdevmode = function(adevmode){
		BridgeSettings.devmode = adevmode;
	};
	
	BridgeSettings.setnestconfigured = function(anestconfigured){
		BridgeSettings.nestconfigured = anestconfigured;
	};
	
	return BridgeSettings;
});

app.service('bridgeService', function ($http, $window, BridgeSettings) {
        var self = this;
        self.BridgeSettings = BridgeSettings;
        this.state = {base: window.location.origin + "/api/devices", upnpbase: window.location.origin + "/upnp/settings", huebase: window.location.origin + "/api", backups: [], devices: [], device: [], error: "", showVera: false, showHarmony: false, showNest: false, habridgeversion: ""};

        this.viewDevices = function () {
            this.state.error = "";
            return $http.get(this.state.base).then(
                function (response) {
                    self.state.devices = response.data;
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    } else {
                        self.state.error = "Some error occurred.";
                    }
                    console.log(error);
                }
            );
        };

        this.getHABridgeVersion = function () {
            this.state.error = "";
            return $http.get(this.state.base + "/habridge/version").then(
                function (response) {
                    self.state.habridgeversion = response.data.version;
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    } else {
                        self.state.error = "cannot get version";
                    }
                    console.log(error);
                }
            );
        };

        this.aContainsB = function (a, b) {
            return a.indexOf(b) >= 0;
        }

        this.updateShowVera = function () {
            if(this.aContainsB(self.BridgeSettings.veraaddress, "1.1.1.1") || self.BridgeSettings.veraaddress == "" || self.BridgeSettings.veraaddress == null)
            	this.state.showVera = false;
            else
            	this.state.showVera = true;
        	return;
        }
        
        this.updateShowNest = function () {
        	if(self.BridgeSettings.nestconfigured == true)
        		this.state.showNest = true;
        	else
        		this.state.showNest = false;
        	return;
        }
        
        this.updateShowHarmony = function () {
        	if(self.BridgeSettings.harmonyaddress.devices) {
	            if(this.aContainsB(self.BridgeSettings.harmonyaddress.devices[0].ip, "1.1.1.1") || self.BridgeSettings.harmonyaddress == "" || self.BridgeSettings.harmonyaddress == null)
	            	this.state.showHarmony = false;
	            else
	            	this.state.showHarmony = true;
        	}
            else
            	this.state.showHarmony = false;
       	return;
        }


        this.loadBridgeSettings = function () {
            this.state.error = "";
            return $http.get(this.state.upnpbase).then(
                function (response) {
                	self.BridgeSettings.setupnpconfigaddress(response.data.upnpconfigaddress);
                	self.BridgeSettings.setserverport(response.data.serverport);
                	self.BridgeSettings.setupnpdevicedb(response.data.upnpdevicedb);
                	self.BridgeSettings.setupnpresponseport(response.data.upnpresponseport);
                	self.BridgeSettings.setveraaddress(response.data.veraaddress);
                	self.BridgeSettings.setharmonyaddress(response.data.harmonyaddress);
                	self.BridgeSettings.settraceupnp(response.data.traceupnp);
                	self.BridgeSettings.setupnpstrict(response.data.upnpstrict);
                	self.BridgeSettings.setdevmode(response.data.devmode);
                	self.BridgeSettings.setnestconfigured(response.data.nestconfigured);                		
                	self.updateShowVera();
                	self.updateShowHarmony();
                	self.updateShowNest();
                },
                function (error) {
                    if (error.data) {
                    	$window.alert("Load Bridge Settings Error: " + error.data.message);
                    } else {
                    	$window.alert("Load Bridge Settings Error: unknown");
                    }
                    console.log(error);
                }
            );
        };

        this.viewBackups = function () {
            this.state.error = "";
            return $http.get(this.state.base + "/backup/available").then(
                function (response) {
                    self.state.backups = response.data;
                },
                function (error) {
                    if (error.data) {
                    	$window.alert("Get Backups Error: " + error.data.message);
                    } else {
                    	$window.alert("Get Backups Error: unknown");
                    }
                }
            );
        };
        
        this.viewNestItems = function () {
            this.state.error = "";
        	if(!this.state.showNest)
        		return;
            return $http.get(this.state.base + "/nest/items").then(
                function (response) {
                    self.state.nestitems = response.data;
                },
                function (error) {
                    if (error.data) {
                    	$window.alert("Get Nest Items Error: " + error.data.message);
                    } else {
                    	$window.alert("Get Nest Items Error: unknown");
                    }
                }
            );
        };
        
        this.viewVeraDevices = function () {
            this.state.error = "";
        	if(!this.state.showVera)
        		return;
            return $http.get(this.state.base + "/vera/devices").then(
                function (response) {
                    self.state.veradevices = response.data;
                },
                function (error) {
                    if (error.data) {
                    	$window.alert("Get Vera Devices Error: " + error.data.message);
                    } else {
                    	$window.alert("Get Vera Devices Error: unknown");
                    }
                }
            );
        };
        
        this.viewVeraScenes = function () {
            this.state.error = "";
        	if(!this.state.showVera)
        		return;
            return $http.get(this.state.base + "/vera/scenes").then(
                function (response) {
                    self.state.verascenes = response.data;
                },
                function (error) {
                    if (error.data) {
                    	$window.alert("Get Vera Scenes Error: " + error.data.message);
                    } else {
                    	$window.alert("Get Vera Scenes Error: unknown");
                    }
                }
            );
        };
        
        this.viewHarmonyActivities = function () {
            this.state.error = "";
        	if(!this.state.showHarmony)
        		return;
            return $http.get(this.state.base + "/harmony/activities").then(
                function (response) {
                    self.state.harmonyactivities = response.data;
                },
                function (error) {
                    if (error.data) {
                    	$window.alert("Get Harmony Activities Error: " + error.data.message);
                    } else {
                    	$window.alert("Get Harmony Activities Error: unknown");
                    }
                }
            );
        };

        this.viewHarmonyDevices = function () {
            this.state.error = "";
        	if(!this.state.showHarmony)
        		return;
            return $http.get(this.state.base + "/harmony/devices").then(
                function (response) {
                    self.state.harmonydevices = response.data;
                },
                function (error) {
                    if (error.data) {
                    	$window.alert("Get Harmony Devices Error: " + error.data.message);
                    } else {
                    	$window.alert("Get Harmony Devices Error: unknown");
                    }
                }
            );
        };

        this.findDeviceByMapId = function(id, target, type) {
        	for(var i = 0; i < this.state.devices.length; i++) {
        		if(this.state.devices[i].mapId == id && this.state.devices[i].mapType == type && this.state.devices[i].targetDevice == target)
        			return true;
        	}
        	return false;
        };
        
        this.findNestItemByMapId = function(id, type) {
        	for(var i = 0; i < this.state.devices.length; i++) {
        		if(this.state.devices[i].mapId == id && this.aContainsB(this.state.devices[i].mapType, type))
        			return true;
        	}
        	return false;
        };
        
        this.addDevice = function (device) {
            this.state.error = "";
            if(device.httpVerb != null && device.httpVerb != "")
            	device.deviceType = "custom";
            if (device.id) {
                var putUrl = this.state.base + "/" + device.id;
                return $http.put(putUrl, {
                    id: device.id,
                    name: device.name,
                    mapId: device.mapId,
                    mapType: device.mapType,
                    deviceType: device.deviceType,
                    targetDevice: device.targetDevice,
                    onUrl: device.onUrl,
                    offUrl: device.offUrl,
                    httpVerb: device.httpVerb,
                    contentType: device.contentType,
                    contentBody: device.contentBody,
                    contentBodyOff: device.contentBodyOff
                }).then(
                    function (response) {
                        self.viewDevices();
                    },
                    function (error) {
                        if (error.data) {
                        	$window.alert("Edit Device Error: " + error.data.message);
                        }
                        $window.alert("Edit Device Error: unknown");
                    }
                );
            } else {
            	if(device.deviceType == null || device.deviceType == "")
            		device.deviceType = "custom";
                if(device.httpVerb != null && device.httpVerb != "")
                	device.deviceType = "custom";
                return $http.post(this.state.base, {
                    name: device.name,
                    mapId: device.mapId,
                    mapType: device.mapType,
                    deviceType: device.deviceType,
                    targetDevice: device.targetDevice,
                    onUrl: device.onUrl,
                    offUrl: device.offUrl,
                    httpVerb: device.httpVerb,
                    contentType: device.contentType,
                    contentBody: device.contentBody,
                    contentBodyOff: device.contentBodyOff
                }).then(
                    function (response) {
                        self.viewDevices();
                    },
                    function (error) {
                        if (error.data) {
                        	$window.alert("Add new Device Error: " + error.data.message);
                        }
                        $window.alert("Add new Device Error: unknown");
                    }
                );
            }
        };

        this.backupDeviceDb = function (afilename) {
            this.state.error = "";
            return $http.put(this.state.base + "/backup/create", {
                filename: afilename
            }).then(
                function (response) {
                    self.viewBackups();
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    }
                    $window.alert("Backup Device Db Error: unknown");
                }
            );
        };

        this.restoreBackup = function (afilename) {
            this.state.error = "";
            return $http.post(this.state.base + "/backup/restore", {
                filename: afilename
            }).then(
                function (response) {
                    self.viewBackups();
                    self.viewDevices();
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    }
                    $window.alert("Backup Db Restore Error: unknown");
                }
            );
        };

        this.deleteBackup = function (afilename) {
            this.state.error = "";
            return $http.post(this.state.base + "/backup/delete", {
                filename: afilename
            }).then(
                function (response) {
                    self.viewBackups();
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    }
                    $window.alert("Backup Db Frlryr Error: unknown");
                }
            );
        };

        this.deleteDevice = function (id) {
            this.state.error = "";
            return $http.delete(this.state.base + "/" + id).then(
                function (response) {
                    self.viewDevices();
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    }
                    $window.alert("Delete Device Error: unknown");
                }
            );
        };

        this.deleteDeviceByMapId = function (id, type) {
        	for(var i = 0; i < this.state.devices.length; i++) {
        		if(this.state.devices[i].mapId == id && this.aContainsB(this.state.devices[i].mapType, type))
        			return self.deleteDevice(this.state.devices[i].id);
        	}
        };

        this.editDevice = function (device) {
            self.state.device = device;
        };

        this.testUrl = function (device, type) {
        	if(type == "on") {
       			$http.put(this.state.huebase + "/test/lights/" + device.id + "/state", "{\"on\":true}").then(
       	                function (response) {
       	                    $window.alert("Request Exceuted: " + response.statusText);
       	                },
       	                function (error) {
       	                    $window.alert("Request Error: " + error.statusText + ", with status: " + error.status + ", Pleae look in your console log.");
       	                }
       	            );
           		return;        		
        	}
        	else {
       			$http.put(this.state.huebase + "/test/lights/" + device.id + "/state", "{\"on\":false}").then(
       	                function (response) {
       	                    $window.alert("Request Exceuted: " + response.statusText);
       	                },
       	                function (error) {
       	                    $window.alert("Request Error: " + error.statusText + ", with status: " + error.status + ", Pleae look in your console log.");
       	                }
       	            );
           		return;        		
           	}
        };
    });

app.controller('ViewingController', function ($scope, $location, $http, $window, bridgeService, BridgeSettings) {

		$scope.BridgeSettings = bridgeService.BridgeSettings;
        bridgeService.viewDevices();
        bridgeService.viewBackups();
        $scope.bridge = bridgeService.state;
        $scope.optionalbackupname = "";
        $scope.visible = false;
        $scope.imgUrl = "glyphicon glyphicon-plus";
        $scope.visibleBk = false;
        $scope.imgBkUrl = "glyphicon glyphicon-plus";
        $scope.predicate = '';
        $scope.reverse = true;
        $scope.order = function(predicate) {
          $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;
          $scope.predicate = predicate;
        };
        $scope.deleteDevice = function (device) {
            bridgeService.deleteDevice(device.id);
        };
        $scope.testUrl = function (device, type) {
        	bridgeService.testUrl(device, type);
        };
        $scope.setBridgeUrl = function (url) {
            bridgeService.state.base = url;
            bridgeService.viewDevices();
        };
        $scope.goBridgeUrl = function (url) {
        	window.open(url, "_blank");
        };
        $scope.editDevice = function (device) {
            bridgeService.editDevice(device);
            $location.path('/editdevice');
        };
        $scope.backupDeviceDb = function (optionalbackupname) {
            bridgeService.backupDeviceDb(optionalbackupname);
        };
        $scope.restoreBackup = function (backupname) {
            bridgeService.restoreBackup(backupname);
        };
        $scope.deleteBackup = function (backupname) {
            bridgeService.deleteBackup(backupname);
        };
        $scope.toggle = function () {
            $scope.visible = !$scope.visible;
            if($scope.visible)
                $scope.imgUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgUrl = "glyphicon glyphicon-plus";
        };
        $scope.toggleBk = function () {
            $scope.visibleBk = !$scope.visibleBk;
            if($scope.visibleBk)
                $scope.imgBkUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgBkUrl = "glyphicon glyphicon-plus";
        };
    });

app.controller('AddingController', function ($scope, $location, $http, bridgeService, BridgeSettings) {
	$scope.device = {id: "", name: "", deviceType: "custom", onUrl: "", offUrl: ""};
	$scope.clearDevice = function () {
        $scope.device.id = "";
        $scope.device.mapType = null;
        $scope.device.mapId = null;
        $scope.device.name = "";
        $scope.device.onUrl = "";
        $scope.device.deviceType = "custom";
        $scope.device.targetDevice = null;
        $scope.device.offUrl = "";
        $scope.device.httpVerb = null;
        $scope.device.contentType = null;
        $scope.device.contentBody = null;
        $scope.device.contentBodyOff = null;
    };


        $scope.clearDevice();
        bridgeService.device = $scope.device;
        $scope.vera = {base: "", port: "3480", id: ""};
        $scope.vera.base = "http://" + BridgeSettings.veraaddress;
        bridgeService.viewVeraDevices();
        bridgeService.viewVeraScenes();
        bridgeService.viewHarmonyActivities();
        bridgeService.viewHarmonyDevices();
        bridgeService.viewNestItems();
        $scope.bridge = bridgeService.state;
        $scope.device = bridgeService.state.device;
        $scope.imgButtonsUrl = "glyphicon glyphicon-plus";
        $scope.buttonsVisible = false;
        $scope.predicate = '';
        $scope.reverse = true;
        $scope.device_dim_control = "";
        $scope.order = function(predicate) {
          $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;
          $scope.predicate = predicate;
        };
          
        $scope.buildUrlsUsingDevice = function (dim_control) {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + $scope.vera.base;
            }
            $scope.device.deviceType = "switch";
            $scope.device.mapType = "veraDevice";
            $scope.device.mapId = $scope.vera.id;
            if(dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0)
            	$scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                	+ "/data_request?id=action&output_format=json&DeviceNum="
                	+ $scope.vera.id
                	+ "&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget="
                	+ dim_control;
            else
            	$scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                	+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
                	+ $scope.vera.id;
            $scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum="
                + $scope.vera.id;
        };

        $scope.buildUrlsUsingScene = function () {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + $scope.vera.base;
            }
            $scope.device.deviceType = "scene";
            $scope.device.mapType = "veraScene";
            $scope.device.mapId = $scope.vera.id;
            $scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
                + $scope.vera.id;
            $scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
                + $scope.vera.id;
        };

        $scope.buildDeviceUrls = function (veradevice, dim_control) {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + $scope.vera.base;
            }
            $scope.device.deviceType = "switch";
            $scope.device.name = veradevice.name;
            $scope.device.mapType = "veraDevice";
            $scope.device.mapId = veradevice.id;
            if(dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0)
            	$scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                	+ "/data_request?id=action&output_format=json&DeviceNum="
                	+ veradevice.id
                	+ "&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget="
                	+ dim_control;
            else
            	$scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                	+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
                	+ veradevice.id;
            $scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum="
                + veradevice.id;
        };

        $scope.buildSceneUrls = function (verascene) {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + $scope.vera.base;
            }
            $scope.device.deviceType = "scene";
            $scope.device.name = verascene.name;
            $scope.device.mapType = "veraScene";
            $scope.device.mapId = verascene.id;
            $scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
                + verascene.id;
            $scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
                + verascene.id;
        };

        $scope.buildActivityUrls = function (harmonyactivity) {
            $scope.device.deviceType = "activity";
            $scope.device.targetDevice = harmonyactivity.hub;
            $scope.device.name = harmonyactivity.activity.label;
            $scope.device.mapType = "harmonyActivity";
            $scope.device.mapId = harmonyactivity.activity.id;
            $scope.device.onUrl = "{\"name\":\"" + harmonyactivity.activity.id + "\"}";
            $scope.device.offUrl = "{\"name\":\"-1\"}";
        };

        $scope.buildButtonUrls = function (harmonydevice, onbutton, offbutton) {
        	var currentOn = $scope.device.onUrl;
        	var currentOff = $scope.device.offUrl;
        	if( $scope.device.mapType == "harmonyButton") {
	            $scope.device.onUrl = currentOn.substr(0, currentOn.indexOf("]")) + ",{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + onbutton + "\"}]";
	            $scope.device.offUrl = currentOff.substr(0, currentOff.indexOf("]")) + ",{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + offbutton + "\"}]";        		
        	}
        	else if ($scope.device.mapType == null || $scope.device.mapType == "") {
	            $scope.device.deviceType = "button";
	            $scope.device.targetDevice = harmonydevice.hub;
	            $scope.device.name = harmonydevice.device.label;
	            $scope.device.mapType = "harmonyButton";
	            $scope.device.mapId = harmonydevice.device.id + "-" + onbutton + "-" + offbutton;
	            $scope.device.onUrl = "[{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + onbutton + "\"}]";
	            $scope.device.offUrl = "[{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + offbutton + "\"}]";
	        }
        };

        $scope.buildNestHomeUrls = function (nestitem) {
            $scope.device.deviceType = "home";
            $scope.device.name = nestitem.name;
            $scope.device.mapType = "nestHomeAway";
            $scope.device.mapId = nestitem.id;
            $scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"away\":false,\"control\":\"status\"}";
            $scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"away\":true,\"control\":\"status\"}";
        };

        $scope.buildNestTempUrls = function (nestitem) {
            $scope.device.deviceType = "thermo";
            $scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Temperature";
            $scope.device.mapType = "nestThermoSet";
            $scope.device.mapId = nestitem.id + "-SetTemp";
            $scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"temp\",\"temp\":\"${intensity.percent}\"}";
            $scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"temp\",\"temp\":\"${intensity.percent}\"}";
        };

        $scope.buildNestHeatUrls = function (nestitem) {
            $scope.device.deviceType = "thermo";
            $scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Heat";
            $scope.device.mapType = "nestThermoSet";
            $scope.device.mapId = nestitem.id + "-SetHeat";
            $scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"heat\"}";
            $scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
        };

        $scope.buildNestCoolUrls = function (nestitem) {
            $scope.device.deviceType = "thermo";
            $scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Cool";
            $scope.device.mapType = "nestThermoSet";
            $scope.device.mapId = nestitem.id + "-SetCool";
            $scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"cool\"}";
            $scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
        };

        $scope.buildNestRangeUrls = function (nestitem) {
            $scope.device.deviceType = "thermo";
            $scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Range";
            $scope.device.mapType = "nestThermoSet";
            $scope.device.mapId = nestitem.id + "-SetRange";
            $scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"range\"}";
            $scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
        };

        $scope.buildNestOffUrls = function (nestitem) {
            $scope.device.deviceType = "thermo";
            $scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Thermostat";
            $scope.device.mapType = "nestThermoSet";
            $scope.device.mapId = nestitem.id + "-TurnOff";
            $scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"range\"}";
            $scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
        };

        $scope.buildNestFanUrls = function (nestitem) {
            $scope.device.deviceType = "thermo";
            $scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Fan";
            $scope.device.mapType = "nestThermoSet";
            $scope.device.mapId = nestitem.id + "-SetFan";
            $scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"fan-on\"}";
            $scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"fan-auto\"}";
        };

        $scope.testUrl = function (device, type) {
        	bridgeService.testUrl(device, type);
        };

        $scope.addDevice = function () {
        	if($scope.device.name == "" && $scope.device.onUrl == "")
        		return;
            bridgeService.addDevice($scope.device).then(
                function () {
                    $scope.device.id = "";
                    $scope.device.mapType = null;
                    $scope.device.mapId = null;
                    $scope.device.name = "";
                    $scope.device.onUrl = "";
                    $scope.device.deviceType = "custom";
                    $scope.device.targetDevice = null;
                    $scope.device.offUrl = "";
                    $scope.device.httpVerb = null;
                    $scope.device.contentType = null;
                    $scope.device.contentBody = null;
                    $scope.device.contentBodyOff = null;
                },
                function (error) {
                }
            );
        }

        $scope.toggleButtons = function () {
            $scope.buttonsVisible = !$scope.buttonsVisible;
            if($scope.buttonsVisible)
                $scope.imgButtonsUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgButtonsUrl = "glyphicon glyphicon-plus";
        };
       
        $scope.deleteDeviceByMapId = function (id, mapType) {
        	bridgeService.deleteDeviceByMapId(id, mapType);
        };
        
    });

app.filter('availableHarmonyActivityId', function(bridgeService) {
        return function(input) {
            var out = [];
            if(input == null)
            	return out;
            for (var i = 0; i < input.length; i++) {
                if(!bridgeService.findDeviceByMapId(input[i].activity.id, input[i].hub, "harmonyActivity")){
                    out.push(input[i]);
                }
            }
            return out;
        }
    });

app.filter('unavailableHarmonyActivityId', function(bridgeService) {
    return function(input) {
        var out = [];
        if(input == null)
        	return out;
        for (var i = 0; i < input.length; i++) {
            if(bridgeService.findDeviceByMapId(input[i].activity.id, input[i].hub, "harmonyActivity")){
                out.push(input[i]);
            }
        }
        return out;
    }
});

app.filter('availableVeraDeviceId', function(bridgeService) {
    return function(input) {
        var out = [];
        if(input == null)
        	return out;
        for (var i = 0; i < input.length; i++) {
            if(!bridgeService.findDeviceByMapId(input[i].id, null, "veraDevice")){
                out.push(input[i]);
            }
        }
        return out;
    }
});

app.filter('unavailableVeraDeviceId', function(bridgeService) {
return function(input) {
    var out = [];
    if(input == null)
    	return out;
    for (var i = 0; i < input.length; i++) {
        if(bridgeService.findDeviceByMapId(input[i].id, null, "veraDevice")){
            out.push(input[i]);
        }
    }
    return out;
}
});

app.filter('availableVeraSceneId', function(bridgeService) {
    return function(input) {
        var out = [];
        if(input == null)
        	return out;
        for (var i = 0; i < input.length; i++) {
            if(!bridgeService.findDeviceByMapId(input[i].id, null, "veraScene")){
                out.push(input[i]);
            }
        }
        return out;
    }
});

app.filter('unavailableVeraSceneId', function(bridgeService) {
return function(input) {
    var out = [];
    if(input == null)
    	return out;
    for (var i = 0; i < input.length; i++) {
        if(bridgeService.findDeviceByMapId(input[i].id, null, "veraScene")){
            out.push(input[i]);
        }
    }
    return out;
}
});

app.filter('availableNestItemId', function(bridgeService) {
    return function(input) {
        var out = [];
        if(input == null)
        	return out;
        for (var i = 0; i < input.length; i++) {
            if(!bridgeService.findNestItemByMapId(input[i].id, "nestHomeAway")){
                out.push(input[i]);
            }
        }
        return out;
    }
});

app.filter('unavailableNestItemId', function(bridgeService) {
return function(input) {
    var out = [];
    if(input == null)
    	return out;
    for (var i = 0; i < input.length; i++) {
        if(input[i].mapType != null && bridgeService.aContainsB(input[i].mapType, "nest")){
            out.push(input[i]);
        }
    }
    return out;
}
});

app.filter('configuredButtons', function() {
    return function(input) {
        var out = [];
        if(input == null)
        	return out;
        for (var i = 0; i < input.length; i++) {
            if(input[i].mapType == "harmonyButton"){
                out.push(input[i]);
            }
        }
        return out;
    }
});


app.controller('ErrorsController', function ($scope, bridgeService) {
        $scope.bridge = bridgeService.state;
    });

app.controller('VersionController', function ($scope, bridgeService) {
    $scope.bridge = bridgeService.state;
});