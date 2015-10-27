var app = angular.module('habridge', [
	'ngRoute'
]);

app.config(function ($routeProvider) {
	$routeProvider.when('/#', {
		templateUrl: 'views/nonconfiguration.html',
		controller: 'ViewingController'
	}).when('/show', {
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
	}).when('/harmonyactivities', {
		templateUrl: 'views/harmonyactivity.html',
		controller: 'AddingController'		
	}).otherwise({
		templateUrl: 'views/nonconfiguration.html',
		controller: 'ViewingController'
	})
});

app.run( function (bridgeService) {
	bridgeService.loadBridgeSettings();
	bridgeService.updateShowVera();
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
	BridgeSettings.vtwocompatibility = "";
	
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
	BridgeSettings.setvtwocompatibility = function(avtwocompatibility){
		BridgeSettings.vtwocompatibility = avtwocompatibility;
	};
	
	return BridgeSettings;
});

app.service('bridgeService', function ($http, $window, BridgeSettings) {
        var self = this;
        self.BridgeSettings = BridgeSettings;
        this.state = {base: window.location.origin + "/api/devices", upnpbase: window.location.origin + "/upnp/settings", devices: [], device: [], error: "", showVera: false, showHarmony: false};

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
                        self.state.error = "If you're not seeing any devices, you may be running into problems with CORS. " +
                            "You can work around this by running a fresh launch of Chrome with the --disable-web-security flag.";
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
                	self.BridgeSettings.setvtwocompatibility(response.data.vtwocompatibility);
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

        this.addDevice = function (id, name, type, onUrl, offUrl, httpVerb, contentType, contentBody, contentBodyOff) {
            this.state.error = "";
            if(httpVerb != null && httpVerb != "")
            	type = "custom";
            if (id) {
                var putUrl = this.state.base + "/" + id;
                return $http.put(putUrl, {
                    id: id,
                    name: name,
                    deviceType: type,
                    onUrl: onUrl,
                    offUrl: offUrl,
                    httpVerb: httpVerb,
                    contentType: contentType,
                    contentBody: contentBody,
                    contentBodyOff: contentBodyOff
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
            	if(type == null || type == "")
            		type = "switch";
                if(httpVerb != null && httpVerb != "")
                	type = "custom";
                return $http.post(this.state.base, {
                    name: name,
                    deviceType: type,
                    onUrl: onUrl,
                    offUrl: offUrl,
                    httpVerb: httpVerb,
                    contentType: contentType,
                    contentBody: contentBody,
                    contentBodyOff: contentBodyOff
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

        this.editDevice = function (id, name, onUrl, offUrl, httpVerb, contentType, contentBody, contentBodyOff) {
            self.state.device = {id: id, name: name, onUrl: onUrl, offUrl: offUrl, httpVerb: httpVerb, contentType: contentType, contentBody: contentBody, contentBodyOff: contentBodyOff};
        };
    });

app.controller('ViewingController', function ($scope, $location, $http, $window, bridgeService, BridgeSettings) {

		$scope.BridgeSettings = bridgeService.BridgeSettings;
        bridgeService.viewDevices();
        $scope.bridge = bridgeService.state;
        bridgeService.updateShowVera();
        bridgeService.updateShowHarmony();
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
        	if(device.deviceType == "activity")
        		return;
        	if(type == "on") {
        		if(device.httpVerb == "PUT")
        			$http.put(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else
        			window.open(device.onUrl, "_blank");
        	}
        	else {
        		if(device.httpVerb == "PUT")
        			$http.put(device.offUrl, device.contentBodyOff).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.offUrl, device.contentBody).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else
            		window.open(device.offUrl, "_blank");
        	}
        };
        $scope.setBridgeUrl = function (url) {
            bridgeService.state.base = url;
            bridgeService.viewDevices();
        };
        $scope.editDevice = function (device) {
            bridgeService.editDevice(device.id, device.name, device.onUrl, device.offUrl, device.httpVerb, device.contentType, device.contentBody, device.contentBodyOff);
            $location.path('/editdevice');
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
        $scope.bridge = bridgeService.state;
        bridgeService.updateShowVera();
        bridgeService.updateShowHarmony();
        $scope.device = bridgeService.state.device;
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
            $scope.device.onUrl = harmonyactivity.id;
            $scope.device.offUrl = "-1";
        };

        $scope.testUrl = function (device, type) {
        	if(device.deviceType == "activity")
        		return;
        	if(type == "on") {
        		if(device.httpVerb == "PUT")
        			$http.put(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else
        			window.open(device.onUrl, "_blank");
        	}
        	else {
        		if(device.httpVerb == "PUT")
        			$http.put(device.offUrl, device.contentBodyOff).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.offUrl, device.contentBody).then(
        	                function (response) {
        	                    $window.alert("Request Exceuted: " + response.statusText);
        	                },
        	                function (error) {
        	                    $window.alert("Request Error: " + error.data.message);
        	                }
        	            );
        		else
            		window.open(device.offUrl, "_blank");
        	}
        };

        $scope.addDevice = function () {
            bridgeService.addDevice($scope.device.id, $scope.device.name, $scope.device.deviceType, $scope.device.onUrl, $scope.device.offUrl, $scope.device.httpVerb, $scope.device.contentType, $scope.device.contentBody, $scope.device.contentBodyOff).then(
                function () {
                    $scope.device.id = "";
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
        
    });

app.controller('ErrorsController', function ($scope, bridgeService) {
        $scope.bridge = bridgeService.state;
    });