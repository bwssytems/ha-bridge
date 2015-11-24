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
	}).otherwise({
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	})
});

app.run( function (bridgeService) {
	bridgeService.loadBridgeSettings();
	bridgeService.updateShowVera();
	bridgeService.updateShowHarmony();
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
	
	return BridgeSettings;
});

app.service('bridgeService', function ($http, $window, BridgeSettings) {
        var self = this;
        self.BridgeSettings = BridgeSettings;
        this.state = {base: window.location.origin + "/api/devices", upnpbase: window.location.origin + "/upnp/settings", huebase: window.location.origin + "/api",  devices: [], device: [], error: "", showVera: false, showHarmony: false, habridgeversion: ""};

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
                    if(self.BridgeSettings.veraaddress == "1.1.1.1" || self.BridgeSettings.veraaddress == "")
                    	self.state.showVera = false;
                    else
                    	self.state.showVera = true;
                    if(self.BridgeSettings.harmonyaddress == "1.1.1.1" || self.BridgeSettings.harmonyaddress == "")
                    	self.state.showHarmony = false;
                    else
                    	self.state.showHarmony = true;
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

        this.updateShowVera = function () {
            if(self.BridgeSettings.veraaddress == "1.1.1.1" || self.BridgeSettings.veraaddress == "")
            	this.state.showVera = false;
            else
            	this.state.showVera = true;
        	return;
        }
        
        this.updateShowHarmony = function () {
            if(self.BridgeSettings.harmonyaddress == "1.1.1.1" || self.BridgeSettings.harmonyaddress == "")
            	this.state.showHarmony = false;
            else
            	this.state.showHarmony = true;
        	return;
        }

        this.viewVeraDevices = function () {
            this.state.error = "";
        	if(!this.state.showVera)
        		return;
            this.state.error = "";
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

        this.findDeviceByMapId = function(id) {
        	for(var i = 0; i < this.state.devices.length; i++) {
        		if(this.state.devices[i].mapId == id)
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
            		device.deviceType = "switch";
                if(device.httpVerb != null && device.httpVerb != "")
                	device.deviceType = "custom";
                return $http.post(this.state.base, {
                    name: device.name,
                    mapId: device.mapId,
                    mapType: device.mapType,
                    deviceType: device.deviceType,
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

        this.deleteDeviceByMapId = function (id) {
        	for(var i = 0; i < this.state.devices.length; i++) {
        		if(this.state.devices[i].mapId == id)
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
        $scope.bridge = bridgeService.state;
        bridgeService.updateShowVera();
        bridgeService.updateShowHarmony();
        $scope.visible = false;
        $scope.imgUrl = "glyphicon glyphicon-plus";
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
        $scope.toggle = function () {
            $scope.visible = !$scope.visible;
            if($scope.visible)
                $scope.imgUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgUrl = "glyphicon glyphicon-plus";
        };
    });

app.controller('AddingController', function ($scope, $location, $http, bridgeService, BridgeSettings) {

        $scope.device = {id: "", name: "", deviceType: "switch", onUrl: "", offUrl: ""};
        $scope.vera = {base: "", port: "3480", id: ""};
        $scope.vera.base = "http://" + BridgeSettings.veraaddress;
        bridgeService.device = $scope.device;
        bridgeService.viewVeraDevices();
        bridgeService.viewVeraScenes();
        bridgeService.viewHarmonyActivities();
        bridgeService.viewHarmonyDevices();
        $scope.bridge = bridgeService.state;
        bridgeService.updateShowVera();
        bridgeService.updateShowHarmony();
        $scope.device = bridgeService.state.device;
        $scope.activitiesVisible = false;
        $scope.imgButtonsUrl = "glyphicon glyphicon-plus";
        $scope.buttonsVisible = false;
        $scope.imgActivitiesUrl = "glyphicon glyphicon-plus";
        $scope.devicesVisible = false;
        $scope.imgDevicesUrl = "glyphicon glyphicon-plus";
        $scope.scenesVisible = false;
        $scope.imgScenesUrl = "glyphicon glyphicon-plus";
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
            $scope.devoce.mapType = "veraScene";
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
            $scope.device.name = harmonyactivity.label;
            $scope.device.mapType = "harmonyActivity";
            $scope.device.mapId = harmonyactivity.id;
            $scope.device.onUrl = "{\"name\":\"" + harmonyactivity.id + "\"}";
            $scope.device.offUrl = "{\"name\":\"-1\"}";
        };

        $scope.buildButtonUrls = function (harmonydevice, onbutton, offbutton) {
            $scope.device.deviceType = "button";
            $scope.device.name = harmonydevice.label;
            $scope.device.mapType = "harmonyButton";
            $scope.device.mapId = harmonydevice.id + "-" + onbutton + "-" + offbutton;
            $scope.device.onUrl = "{\"device\":\"" + harmonydevice.id + "\",\"button\":\"" + onbutton + "\"}";
            $scope.device.offUrl = "{\"device\":\"" + harmonydevice.id + "\",\"button\":\"" + offbutton + "\"}";
        };

        $scope.testUrl = function (device, type) {
        	bridgeService.testUrl(device, type);
        };

        $scope.addDevice = function () {
            bridgeService.addDevice($scope.device).then(
                function () {
                    $scope.device.id = "";
                    $scope.device.mapType = null;
                    $scope.device.mapId = null;
                    $scope.device.name = "";
                    $scope.device.onUrl = "";
                    $scope.device.deviceType = "switch";
                    $scope.device.offUrl = "";
                    $scope.device.httpVerb = null;
                    $scope.device.contentType = null;
                    $scope.device.contentBody = null;
                    $scope.device.contentBodyOff = null;
                    $location.path('/#');
                },
                function (error) {
                }
            );
        }

        $scope.toggleActivities = function () {
            $scope.activitiesVisible = !$scope.activitiesVisible;
            if($scope.activitiesVisible)
                $scope.imgActivitiesUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgActivitiesUrl = "glyphicon glyphicon-plus";
        };
        
        $scope.toggleButtons = function () {
            $scope.buttonsVisible = !$scope.buttonsVisible;
            if($scope.buttonsVisible)
                $scope.imgButtonsUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgButtonsUrl = "glyphicon glyphicon-plus";
        };
        
        $scope.toggleDevices = function () {
            $scope.devicesVisible = !$scope.devicesVisible;
            if($scope.devicesVisible)
                $scope.imgDevicesUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgDevicesUrl = "glyphicon glyphicon-plus";
        };
        
        $scope.toggleScenes = function () {
            $scope.scenesVisible = !$scope.scenesVisible;
            if($scope.scenesVisible)
                $scope.imgScenesUrl = "glyphicon glyphicon-minus";
            else
                $scope.imgScenesUrl = "glyphicon glyphicon-plus";
        };
        
        $scope.deleteDeviceByMapId = function (id) {
        	bridgeService.deleteDeviceByMapId(id);
        };
        
    });

app.filter('availableId', function(bridgeService) {
        return function(input) {
            var out = [];
            if(input == null)
            	return out;
            for (var i = 0; i < input.length; i++) {
                if(!bridgeService.findDeviceByMapId(input[i].id)){
                    out.push(input[i]);
                }
            }
            return out;
        }
    });

app.filter('unavailableId', function(bridgeService) {
    return function(input) {
        var out = [];
        if(input == null)
        	return out;
        for (var i = 0; i < input.length; i++) {
            if(bridgeService.findDeviceByMapId(input[i].id)){
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