angular.module('habridge', [])
    .service('bridgeService', ["$http", function ($http) {
        var self = this;
        this.state = {base: window.location.origin + "/api/devices", upnpbase: window.location.origin + "/upnp/settings", devices: [], error: ""};

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

        this.viewBridgeSettings = function () {
            this.state.error = "";
            return $http.get(this.state.upnpbase).then(
                function (response) {
                    self.state.upnpconfigaddress = response.data.upnpconfigaddress;
                    self.state.serverport = response.data.serverport;
                    self.state.upnpdevicedb = response.data.upnpdevicedb;
                    self.state.upnpresponseport = response.data.upnpresponseport;
                    self.state.veraaddress = response.data.veraaddress;
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

        this.editDevice = function (id, name, type, onUrl, offUrl) {
            this.device.id = id;
            this.device.name = name;
            this.device.onUrl = onUrl;
            this.device.offUrl = offUrl;
        };
    }])

    .controller('ViewingController', ["$scope", "bridgeService", function ($scope, bridgeService) {
        bridgeService.viewDevices();
        bridgeService.viewBridgeSettings();
        bridgeService.viewVeraDevices();
        bridgeService.viewVeraScenes();
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
            bridgeService.editDevice(device.id, device.name, device.type, device.onUrl, device.offUrl);
        };
    }])

    .controller('AddingController', ["$scope", "bridgeService", function ($scope, bridgeService) {
        $scope.device = {id: "", name: "", type: "switch", onUrl: "", offUrl: ""};
        $scope.vera = {base: "", port: "3480", id: ""};
        bridgeService.device = $scope.device;
        
        $scope.buildUrls = function () {
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

        $scope.buildDeviceUrls = function (veradevice, veraaddr) {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + veraaddr;
            }
            $scope.device.name = veradevice.name;
            $scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
                + veradevice.id;
            $scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum="
                + veradevice.id;
        };

        $scope.buildSceneUrls = function (verascene, veraaddr) {
            if ($scope.vera.base.indexOf("http") < 0) {
                $scope.vera.base = "http://" + veraaddr;
            }
            $scope.device.name = verascene.name;
            $scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
                + verascene.id;
            $scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
                + "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
                + verascene.id;
        };

        $scope.setVeraAddress = function (anAddress) {
        	$scope.vera.base = "http://" + anAddress;
        };

        $scope.testUrl = function (url) {
            window.open(url, "_blank");
        };

        $scope.addDevice = function () {
            bridgeService.addDevice($scope.device.id, $scope.device.name, $scope.device.type, $scope.device.onUrl, $scope.device.offUrl).then(
                function () {
                    $scope.device.id = "";
                    $scope.device.name = "";
                    $scope.device.onUrl = "";
                    $scope.device.offUrl = "";
                },
                function (error) {
                }
            );
        }
        
    }])

    .controller('ErrorsController', ["$scope", "bridgeService", function ($scope, bridgeService) {
        $scope.bridge = bridgeService.state;
    }]);