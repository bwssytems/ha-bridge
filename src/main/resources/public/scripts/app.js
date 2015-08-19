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

        this.addDevice = function (id, name, type, onUrl, offUrl) {
            this.state.error = "";
            if (id) {
                var putUrl = this.state.base + "/" + id;
                return $http.put(putUrl, {
                    id: id,
                    name: name,
                    deviceType: type,
                    onUrl: onUrl,
                    offUrl: offUrl
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
                return $http.post(this.state.base, {
                    name: name,
                    deviceType: type,
                    onUrl: onUrl,
                    offUrl: offUrl
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

        this.editDevice = function (id, name, onUrl, offUrl) {
            self.state.device = {id: id, name: name, onUrl: onUrl, offUrl: offUrl};
        };
    });

app.controller('ViewingController', function ($scope, $location, bridgeService, BridgeSettings) {

		$scope.BridgeSettings = bridgeService.BridgeSettings;
        bridgeService.viewDevices();
        $scope.bridge = bridgeService.state;
        $scope.deleteDevice = function (device) {
            bridgeService.deleteDevice(device.id);
        };
        $scope.testUrl = function (url) {
            window.open(url, "_blank");
        };
        $scope.setBridgeUrl = function (url) {
            bridgeService.state.base = url;
            bridgeService.viewDevices();
        };
        $scope.editDevice = function (device) {
            bridgeService.editDevice(device.id, device.name, device.onUrl, device.offUrl);
            $location.path('/editdevice');
        };
    });

app.controller('AddingController', function ($scope, bridgeService, BridgeSettings) {

        $scope.device = {id: "", name: "", deviceType: "switch", onUrl: "", offUrl: ""};
        $scope.vera = {base: "", port: "3480", id: ""};
        $scope.vera.base = "http://" + BridgeSettings.veraaddress;
        bridgeService.device = $scope.device;
        bridgeService.viewVeraDevices();
        bridgeService.viewVeraScenes();
        $scope.bridge = bridgeService.state;
        $scope.device = bridgeService.state.device;
       
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
            window.open(url, "_blank");
        };

        $scope.addDevice = function () {
            bridgeService.addDevice($scope.device.id, $scope.device.name, $scope.device.deviceType, $scope.device.onUrl, $scope.device.offUrl).then(
                function () {
                    $scope.device.id = "";
                    $scope.device.name = "";
                    $scope.device.onUrl = "";
                    $scope.device.deviceType = "switch";
                    $scope.device.offUrl = "";
                },
                function (error) {
                }
            );
        }
        
    });

app.controller('ErrorsController', function ($scope, bridgeService) {
        $scope.bridge = bridgeService.state;
    });