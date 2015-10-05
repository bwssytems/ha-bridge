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
	}).otherwise({
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	})
});

app.run( function (bridgeService) {
	bridgeService.loadBridgeSettings();
});

app.factory('BridgeSettings', function() {
	var BridgeSettings = {};
	
	BridgeSettings.upnpconfigaddress = "";
	BridgeSettings.serverport = "";
	BridgeSettings.upnpdevicedb = "";
	BridgeSettings.upnpresponseport = "";
	BridgeSettings.veraaddress = "";
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

app.service('bridgeService', function ($http, BridgeSettings) {
        var self = this;
        self.BridgeSettings = BridgeSettings;
        this.state = {base: window.location.origin + "/api/devices", upnpbase: window.location.origin + "/upnp/settings", devices: [], device: [], error: ""};

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
                	self.BridgeSettings.settraceupnp(response.data.traceupnp);
                	self.BridgeSettings.setupnpstrict(response.data.upnpstrict);
                	self.BridgeSettings.setvtwocompatibility(response.data.vtwocompatibility);
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    } else {
                        self.state.error = "If you're not seeing any settings, you may be running into problems with CORS. " +
                            "You can work around this by running a fresh launch of Chrome with the --disable-web-security flag.";
                    }
                    console.log(error);
                }
            );
        };

        this.viewVeraDevices = function () {
            this.state.error = "";
            return $http.get(this.state.base + "/vera/devices").then(
                function (response) {
                    self.state.veradevices = response.data;
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    } else {
                        self.state.error = "If you're not seeing any address, you may be running into problems with CORS. " +
                            "You can work around this by running a fresh launch of Chrome with the --disable-web-security flag.";
                    }
                    console.log(error);
                }
            );
        };

        this.viewVeraScenes = function () {
            this.state.error = "";
            return $http.get(this.state.base + "/vera/scenes").then(
                function (response) {
                    self.state.verascenes = response.data;
                },
                function (error) {
                    if (error.data) {
                        self.state.error = error.data.message;
                    } else {
                        self.state.error = "If you're not seeing any address, you may be running into problems with CORS. " +
                            "You can work around this by running a fresh launch of Chrome with the --disable-web-security flag.";
                    }
                    console.log(error);
                }
            );
        };

        this.addDevice = function (id, name, type, onUrl, offUrl, httpVerb, contentType, contentBody, contentBodyOff) {
            this.state.error = "";
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
                            self.state.error = error.data.message;
                        }
                        console.log(error);
                    }
                );
            } else {
            	if(type == null || type == "")
            		type = "switch";
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
                            self.state.error = error.data.message;
                        }
                        console.log(error);
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
                    console.log(error);
                }
            );
        };

        this.editDevice = function (id, name, onUrl, offUrl, httpVerb, contentType, contentBody, contentBodyOff) {
            self.state.device = {id: id, name: name, onUrl: onUrl, offUrl: offUrl, httpVerb: httpVerb, contentType: contentType, contentBody: contentBody, contentBodyOff: contentBodyOff};
        };
    });

app.controller('ViewingController', function ($scope, $location, $http, bridgeService, BridgeSettings) {

		$scope.BridgeSettings = bridgeService.BridgeSettings;
        bridgeService.viewDevices();
        $scope.bridge = bridgeService.state;
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
        	if(type == "on") {
        		if(device.httpVerb == "PUT")
        			$http.put(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
        	                }
        	            );
        		else
        			window.open(device.onUrl, "_blank");
        	}
        	else {
        		if(device.httpVerb == "PUT")
        			$http.put(device.offUrl, device.contentBodyOff).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.offUrl, device.contentBody).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
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
        $scope.bridge = bridgeService.state;
        $scope.device = bridgeService.state.device;
        $scope.predicate = '';
        $scope.reverse = true;
        $scope.order = function(predicate) {
          $scope.reverse = ($scope.predicate === predicate) ? !$scope.reverse : false;
          $scope.predicate = predicate;
        };
          
        $scope.buildUrlsUsingDevice = function () {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + $scope.vera.base;
            }
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

        $scope.buildDeviceUrls = function (veradevice) {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + $scope.vera.base;
            }
            $scope.device.deviceType = "switch";
            $scope.device.name = veradevice.name;
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

        $scope.testUrl = function (url) {
        	if(type == "on") {
        		if(device.httpVerb == "PUT")
        			$http.put(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.onUrl, device.contentBody).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
        	                }
        	            );
        		else
        			window.open(device.onUrl, "_blank");
        	}
        	else {
        		if(device.httpVerb == "PUT")
        			$http.put(device.offUrl, device.contentBodyOff).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
        	                }
        	            );
        		else if(device.httpVerb == "POST")
        			$http.post(device.offUrl, device.contentBody).then(
        	                function (response) {
        	                    $scope.responsedata = response.data;
        	                },
        	                function (error) {
        	                    console.log(error);
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